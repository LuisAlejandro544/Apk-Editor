package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.model.formatBytes
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.CyanPrimary
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.SlateNavy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ApkViewModel
import java.io.File

@Composable
fun FileDetailScreen(
  projectId: String,
  relativePath: String,
  viewModel: ApkViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val state by viewModel.fileDetail.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) }
  var isEditMode by remember { mutableStateOf(false) }
  var editedText by remember(state.textContent) { mutableStateOf(state.textContent) }
  val hasUnsavedChanges = remember(editedText, state.textContent) { editedText != state.textContent }

  LaunchedEffect(projectId, relativePath) {
    viewModel.loadFileDetail(projectId, relativePath)
  }

  // Si es un archivo decodificado por apk-parser o editable, abrir directamente en la pestaña del editor
  LaunchedEffect(state.isEditable, state.isAxmlDecoded) {
    if (state.isAxmlDecoded || state.isEditable || relativePath.endsWith(".xml", ignoreCase = true)) {
      selectedTab = 1
    }
  }

  Scaffold(
    containerColor = SlateDark,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(SlateNavy)
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(SlateCard)
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Volver",
              tint = TextPrimary
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = state.fileName.ifEmpty { "Inspeccionar Archivo" },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = state.relativePath,
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = TextMuted,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }

          if (hasUnsavedChanges) {
            IconButton(
              onClick = {
                viewModel.saveFileContent(projectId, relativePath, editedText) { success ->
                  Toast.makeText(
                    context,
                    if (success) "Archivo guardado exitosamente" else "Error al guardar el archivo",
                    Toast.LENGTH_SHORT
                  ).show()
                }
              },
              enabled = !state.isSaving,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MintSecondary)
            ) {
              if (state.isSaving) {
                CircularProgressIndicator(color = SlateDark, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              } else {
                Icon(
                  imageVector = Icons.Default.Save,
                  contentDescription = "Guardar cambios",
                  tint = SlateDark
                )
              }
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          IconButton(
            onClick = {
              if (state.absolutePath.isNotEmpty()) {
                val file = File(state.absolutePath)
                if (file.exists()) {
                  val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_TEXT, "Archivo extraído: ${file.name}\nTamaño: ${formatBytes(file.length())}")
                  }
                  context.startActivity(Intent.createChooser(sendIntent, "Compartir detalles del archivo"))
                }
              }
            },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(SlateCard)
          ) {
            Icon(
              imageVector = Icons.Default.Share,
              contentDescription = "Compartir",
              tint = CyanPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = SlateNavy,
          contentColor = CyanPrimary,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = CyanPrimary,
              height = 3.dp
            )
          }
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Hex Dump", fontSize = 13.sp)
              }
            }
          )
          Tab(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (state.isAxmlDecoded || state.isEditable) "Editor / Código" else "Texto", fontSize = 13.sp)
              }
            }
          )
          Tab(
            selected = selectedTab == 2,
            onClick = { selectedTab = 2 },
            text = {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Metadatos", fontSize = 13.sp)
              }
            }
          )
        }
      }
    }
  ) { paddingValues ->
    if (state.isLoading) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center
      ) {
        CircularProgressIndicator(color = CyanPrimary)
      }
    } else {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(16.dp)
      ) {
        when (selectedTab) {
          0 -> {
            // Hex Viewer
            Card(
              colors = CardDefaults.cardColors(containerColor = CodeBackground),
              border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = "OFFSET    00 01 02 03 04 05 06 07  08 09 0A 0B 0C 0D 0E 0F  ASCII",
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = CyanGlow,
                    fontSize = 11.sp
                  )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .horizontalScroll(rememberScrollState())
                ) {
                  Text(
                    text = state.hexDump.ifEmpty { "[Sin datos para mostrar]" },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                  )
                }
              }
            }
          }

          1 -> {
            // Text & Code Editor con números de línea y apk-parser
            val isBinaryNotice = state.textContent.startsWith("Este archivo contiene datos binarios")
            if (isBinaryNotice) {
              Card(
                colors = CardDefaults.cardColors(containerColor = CodeBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                Column(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                  verticalArrangement = Arrangement.Center,
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(
                    Icons.Default.Terminal,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(48.dp)
                  )
                  Spacer(modifier = Modifier.height(12.dp))
                  Text(
                    text = "Archivo binario compilado",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = state.textContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Button(
                    onClick = { selectedTab = 0 },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
                  ) {
                    Text("Abrir en Hex Dump", color = SlateDark, fontWeight = FontWeight.Bold)
                  }
                }
              }
            } else {
              Card(
                colors = CardDefaults.cardColors(containerColor = CodeBackground),
                border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (hasUnsavedChanges) AmberAccent else SlateCardBorder)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxSize()
              ) {
                Column(modifier = Modifier.fillMaxSize()) {
                  // Barra de herramientas del editor
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(SlateNavy)
                      .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                      if (state.isAxmlDecoded) {
                        Box(
                          modifier = Modifier
                            .background(CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                          Text(
                            text = "AXML (apk-parser)",
                            color = CyanGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }
                      val lineCount = remember(editedText) { editedText.lines().size }
                      Text(
                        text = "$lineCount líneas",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = TextMuted,
                        fontSize = 11.sp
                      )
                    }

                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      if (hasUnsavedChanges) {
                        IconButton(
                          onClick = { editedText = state.textContent },
                          modifier = Modifier.size(32.dp)
                        ) {
                          Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Deshacer cambios",
                            tint = AmberAccent,
                            modifier = Modifier.size(18.dp)
                          )
                        }

                        Button(
                          onClick = {
                            viewModel.saveFileContent(projectId, relativePath, editedText) { success ->
                              Toast.makeText(
                                context,
                                if (success) "Archivo guardado exitosamente" else "Error al guardar el archivo",
                                Toast.LENGTH_SHORT
                              ).show()
                            }
                          },
                          enabled = !state.isSaving,
                          colors = ButtonDefaults.buttonColors(containerColor = MintSecondary),
                          shape = RoundedCornerShape(6.dp),
                          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                          modifier = Modifier.height(30.dp)
                        ) {
                          if (state.isSaving) {
                            CircularProgressIndicator(color = SlateDark, modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                          } else {
                            Icon(
                              Icons.Default.Save,
                              contentDescription = null,
                              tint = SlateDark,
                              modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                              "Guardar",
                              color = SlateDark,
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }
                      }

                      Button(
                        onClick = { isEditMode = !isEditMode },
                        colors = ButtonDefaults.buttonColors(
                          containerColor = if (isEditMode) CyanPrimary else SlateCard
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                      ) {
                        Icon(
                          if (isEditMode) Icons.Default.Visibility else Icons.Default.Edit,
                          contentDescription = null,
                          tint = if (isEditMode) SlateDark else CyanPrimary,
                          modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                          if (isEditMode) "Lectura" else "Editar",
                          color = if (isEditMode) SlateDark else CyanPrimary,
                          fontSize = 11.sp,
                          fontWeight = FontWeight.Bold
                        )
                      }
                    }
                  }

                  if (hasUnsavedChanges) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .background(AmberAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                      Text(
                        text = "● Tienes cambios sin guardar en este archivo.",
                        color = AmberAccent,
                        fontSize = 11.sp
                      )
                    }
                  }

                  // Editor de código con números de línea
                  val lineCount = remember(editedText) { maxOf(1, editedText.lines().size) }
                  val lineNumbers = remember(lineCount) {
                    (1..lineCount).joinToString("\n")
                  }
                  val verticalScrollState = rememberScrollState()
                  val horizontalScrollState = rememberScrollState()

                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .verticalScroll(verticalScrollState)
                  ) {
                    Row(
                      modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(horizontalScrollState)
                        .padding(vertical = 10.dp)
                    ) {
                      // Columna de números de línea
                      Text(
                        text = lineNumbers,
                        style = TextStyle(
                          color = TextMuted.copy(alpha = 0.5f),
                          fontFamily = FontFamily.Monospace,
                          fontSize = 12.sp,
                          lineHeight = 20.sp,
                          textAlign = TextAlign.End
                        ),
                        modifier = Modifier
                          .width(42.dp)
                          .padding(end = 8.dp)
                      )

                      // Divisor vertical
                      Box(
                        modifier = Modifier
                          .width(1.dp)
                          .height(20.dp * lineCount)
                          .background(SlateCardBorder)
                      )

                      Spacer(modifier = Modifier.width(10.dp))

                      // Entrada o visualización del código
                      BasicTextField(
                        value = editedText,
                        onValueChange = {
                          if (isEditMode) {
                            editedText = it
                          }
                        },
                        readOnly = !isEditMode,
                        textStyle = TextStyle(
                          color = if (isEditMode) TextPrimary else TextSecondary,
                          fontFamily = FontFamily.Monospace,
                          fontSize = 12.sp,
                          lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(CyanPrimary),
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(end = 24.dp)
                      )
                    }
                  }
                }
              }
            }
          }

          2 -> {
            // Metadata Viewer
            Column(
              modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              MetadataCard(
                title = "Nombre del Archivo",
                value = state.fileName,
                onCopy = { copyToClipboard(context, state.fileName, "Nombre copiado") }
              )

              MetadataCard(
                title = "Ruta Relativa en el APK",
                value = state.relativePath,
                onCopy = { copyToClipboard(context, state.relativePath, "Ruta copiada") }
              )

              MetadataCard(
                title = "Tamaño Descomprimido",
                value = "${state.sizeBytes} bytes (${formatBytes(state.sizeBytes)})"
              )

              MetadataCard(
                title = "Hash SHA-256 (Integridad)",
                value = state.sha256,
                isMonospace = true,
                onCopy = { copyToClipboard(context, state.sha256, "SHA-256 copiado") }
              )

              MetadataCard(
                title = "Ubicación en Caché Temporal",
                value = state.absolutePath,
                isMonospace = true,
                onCopy = { copyToClipboard(context, state.absolutePath, "Ruta local copiada") }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun MetadataCard(
  title: String,
  value: String,
  isMonospace: Boolean = false,
  onCopy: (() -> Unit)? = null
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = SlateNavy),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder)),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = value.ifEmpty { "N/A" },
          style = MaterialTheme.typography.bodyMedium,
          color = TextPrimary,
          fontFamily = if (isMonospace) FontFamily.Monospace else FontFamily.Default,
          fontSize = if (isMonospace) 12.sp else 14.sp
        )
      }

      if (onCopy != null) {
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
          onClick = onCopy,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copiar",
            tint = CyanPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
  val clip = ClipData.newPlainText("APK File Info", text)
  clipboard?.setPrimaryClip(clip)
  Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
