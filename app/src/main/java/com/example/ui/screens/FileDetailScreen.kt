package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.DataObject
import com.example.ui.components.AudioPlayerView
import com.example.ui.components.ImageViewerView
import com.example.ui.components.ArscViewerContent
import com.example.ui.components.BinaryDataViewerContent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.example.data.DexClassItem
import com.example.data.ElfSymbolItem
import com.example.data.SoViewMode
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
import com.example.viewmodel.DexViewMode
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
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

  var showClassPickerSheet by remember { mutableStateOf(false) }
  var classSearchFilter by remember { mutableStateOf("") }

  var showSymbolPickerSheet by remember { mutableStateOf(false) }
  var symbolSearchFilter by remember { mutableStateOf("") }
  var jniOnlyFilter by remember { mutableStateOf(false) }

  LaunchedEffect(projectId, relativePath) {
    viewModel.loadFileDetail(projectId, relativePath)
  }

  // Si es un archivo decodificado por apk-parser, DEX, ELF (.so), ARSC, imagen, audio, binario de datos (.dat/.bin) o editable, abrir directamente en la pestaña del visor/editor
  LaunchedEffect(state.isEditable, state.isAxmlDecoded, state.isDex, state.isSo, state.isImage, state.isAudio, state.isArsc, state.isBinaryData) {
    if (state.isAxmlDecoded || state.isEditable || state.isDex || state.isSo || state.isImage || state.isAudio || state.isArsc || state.isBinaryData || relativePath.endsWith(".xml", ignoreCase = true) || relativePath.endsWith(".so", ignoreCase = true) || relativePath.endsWith(".arsc", ignoreCase = true) || relativePath.endsWith(".dat", ignoreCase = true) || relativePath.endsWith(".bin", ignoreCase = true)) {
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
              text = when {
                state.isSo && state.selectedSoSymbol != null -> "${state.relativePath}  •  ${state.selectedSoSymbol?.displayName}"
                state.isDex && state.selectedDexClass != null -> "${state.relativePath}  •  ${state.selectedDexClass?.simpleClassName}"
                else -> state.relativePath
              },
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
                Icon(
                  imageVector = when {
                    state.isImage -> Icons.Default.Image
                    state.isAudio -> Icons.Default.MusicNote
                    state.isSo -> Icons.Default.IntegrationInstructions
                    state.isArsc -> Icons.Default.TableChart
                    state.isBinaryData -> Icons.Default.DataObject
                    else -> Icons.Default.Code
                  },
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  when {
                    state.isImage -> "Visor Imagen"
                    state.isAudio -> "Reproductor Audio"
                    state.isDex -> "DEX / Código"
                    state.isSo -> "ELF (.so) / ASM"
                    state.isArsc -> "Recursos (ARSC)"
                    state.isBinaryData -> "Editor Binario"
                    state.isAxmlDecoded || state.isEditable -> "Editor / Código"
                    else -> "Texto"
                  },
                  fontSize = 13.sp
                )
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
            // Renderizado multimedia nativo (Coil para imágenes y Media3 ExoPlayer para audio)
            if (state.isImage) {
              ImageViewerView(
                imageFile = File(state.absolutePath),
                fileSizeFormatted = com.example.model.formatBytes(state.sizeBytes),
                modifier = Modifier.fillMaxSize()
              )
            } else if (state.isAudio) {
              AudioPlayerView(
                audioFile = File(state.absolutePath),
                fileName = state.fileName,
                fileSizeFormatted = com.example.model.formatBytes(state.sizeBytes),
                modifier = Modifier.fillMaxSize()
              )
            } else if (state.isArsc) {
              ArscViewerContent(
                state = state,
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
              )
            } else if (state.isBinaryData) {
              BinaryDataViewerContent(
                state = state,
                viewModel = viewModel,
                projectId = projectId,
                relativePath = relativePath,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              // Editor / Visor de Código (Soporta DEX con Smali/Java y ELF .so con Goblin/Capstone)
              val isBinaryNotice = state.textContent.startsWith("Este archivo contiene datos binarios")
              if (isBinaryNotice && !state.isDex && !state.isSo) {
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
                  // Barra de herramientas especializada
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .background(SlateNavy)
                      .padding(horizontal = 12.dp, vertical = 8.dp)
                  ) {
                    // Fila 1: Selectores de Formato DEX (Smali / Java) y Selector de Clase
                    if (state.isDex) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        // Selector de modo: Crudo (Smali) vs Java
                        Row(
                          modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateCard)
                            .padding(2.dp),
                          verticalAlignment = Alignment.CenterVertically
                        ) {
                          Box(
                            modifier = Modifier
                              .clip(RoundedCornerShape(6.dp))
                              .background(if (state.dexViewMode == DexViewMode.SMALI) CyanPrimary else SlateCard)
                              .clickable { viewModel.switchDexViewMode(DexViewMode.SMALI) }
                              .padding(horizontal = 10.dp, vertical = 4.dp)
                          ) {
                            Text(
                              text = "⚙️ Crudo (Smali)",
                              color = if (state.dexViewMode == DexViewMode.SMALI) SlateDark else TextSecondary,
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }

                          Box(
                            modifier = Modifier
                              .clip(RoundedCornerShape(6.dp))
                              .background(if (state.dexViewMode == DexViewMode.JAVA) CyanPrimary else SlateCard)
                              .clickable { viewModel.switchDexViewMode(DexViewMode.JAVA) }
                              .padding(horizontal = 10.dp, vertical = 4.dp)
                          ) {
                            Text(
                              text = "☕ Java",
                              color = if (state.dexViewMode == DexViewMode.JAVA) SlateDark else TextSecondary,
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }

                        // Botón para elegir clase dentro del DEX
                        Button(
                          onClick = { showClassPickerSheet = true },
                          colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                          shape = RoundedCornerShape(6.dp),
                          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                          modifier = Modifier.height(30.dp)
                        ) {
                          Icon(
                            imageVector = Icons.Default.IntegrationInstructions,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = "${state.dexClasses.size} Clases",
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }

                      Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Fila 1b: Selectores de Formato ELF .so (Goblin & Capstone)
                    if (state.isSo) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        // Selector de modo scrollable para pantalla táctil móvil
                        Row(
                          modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState())
                            .clip(RoundedCornerShape(8.dp))
                            .background(SlateCard)
                            .padding(2.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                          SoViewMode.values().forEach { mode ->
                            val isSelected = state.soViewMode == mode
                            Box(
                              modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) CyanPrimary else SlateCard)
                                .clickable { viewModel.switchSoViewMode(mode) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                              Text(
                                text = "${mode.iconBadge} ${mode.label}",
                                color = if (isSelected) SlateDark else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                              )
                            }
                          }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Botón para elegir símbolo dentro del .so
                        Button(
                          onClick = { showSymbolPickerSheet = true },
                          colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                          shape = RoundedCornerShape(6.dp),
                          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                          modifier = Modifier.height(30.dp)
                        ) {
                          Icon(
                            imageVector = Icons.Default.IntegrationInstructions,
                            contentDescription = null,
                            tint = CyanPrimary,
                            modifier = Modifier.size(14.dp)
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text(
                            text = "${state.soSymbols.size} Símbolos",
                            color = CyanPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                          )
                        }
                      }

                      Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Fila 2: Indicadores y Botones de Acción (Guardar / Modo Edición)
                    Row(
                      modifier = Modifier.fillMaxWidth(),
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
                        } else if (state.isDex) {
                          Box(
                            modifier = Modifier
                              .background(MintSecondary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                              .padding(horizontal = 6.dp, vertical = 2.dp)
                          ) {
                            Text(
                              text = if (state.dexViewMode == DexViewMode.SMALI) "Smali Bytecode" else "Java Decompiled",
                              color = MintSecondary,
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        } else if (state.isSo) {
                          Box(
                            modifier = Modifier
                              .background(CyanPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                              .padding(horizontal = 6.dp, vertical = 2.dp)
                          ) {
                            Text(
                              text = "ELF (Goblin & Capstone)",
                              color = CyanGlow,
                              fontSize = 11.sp,
                              fontWeight = FontWeight.Bold
                            )
                          }
                        }

                        if (state.isSoAnalyzing || state.isDexDecompiling) {
                          CircularProgressIndicator(
                            color = CyanPrimary,
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                          )
                          Text(
                            text = if (state.isSoAnalyzing) "Analizando ELF..." else "Descompilando...",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanPrimary,
                            fontSize = 11.sp
                          )
                        } else {
                          val lineCount = remember(editedText) { editedText.lines().size }
                          Text(
                            text = "$lineCount líneas",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = TextMuted,
                            fontSize = 11.sp
                          )
                        }
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

                  if (state.isDexDecompiling) {
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = CyanPrimary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Procesando bytecode DEX...", color = TextSecondary, fontSize = 12.sp)
                      }
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
                          .testTag("code_text_field")
                      )
                    }
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

              if (state.isDex) {
                MetadataCard(
                  title = "Estructura DEX Detectada",
                  value = "${state.dexClasses.size} clases compiladas en Dalvik Executable"
                )
              }

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

  // Modal Bottom Sheet para explorar y seleccionar clases dentro del DEX
  if (showClassPickerSheet) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
      onDismissRequest = { showClassPickerSheet = false },
      sheetState = sheetState,
      containerColor = SlateNavy
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "Clases en ${state.fileName}",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Selecciona una clase para desensamblar a Smali o ver en Java",
          style = MaterialTheme.typography.bodySmall,
          color = TextMuted
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = classSearchFilter,
          onValueChange = { classSearchFilter = it },
          placeholder = { Text("Filtrar clases...", color = TextMuted) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = SlateCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        val filteredClasses = remember(state.dexClasses, classSearchFilter) {
          if (classSearchFilter.isBlank()) {
            state.dexClasses
          } else {
            state.dexClasses.filter {
              it.prettyClassName.contains(classSearchFilter.trim(), ignoreCase = true)
            }
          }
        }

        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
        ) {
          items(filteredClasses, key = { it.typeDescriptor }) { item ->
            val isSelected = state.selectedDexClass?.typeDescriptor == item.typeDescriptor
            Card(
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else SlateCard
              ),
              border = CardDefaults.outlinedCardBorder().copy(
                brush = SolidColor(if (isSelected) CyanPrimary else SlateCardBorder)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  viewModel.switchDexClass(item)
                  showClassPickerSheet = false
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) CyanPrimary else SlateNavy),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.IntegrationInstructions,
                    contentDescription = null,
                    tint = if (isSelected) SlateDark else CyanPrimary,
                    modifier = Modifier.size(18.dp)
                  )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = item.simpleClassName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                  )
                  if (item.packageName.isNotEmpty()) {
                    Text(
                      text = item.packageName,
                      style = MaterialTheme.typography.labelSmall,
                      color = TextMuted,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Modal Bottom Sheet para explorar y seleccionar símbolos ELF dentro del .so (Goblin)
  if (showSymbolPickerSheet) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
      onDismissRequest = { showSymbolPickerSheet = false },
      sheetState = sheetState,
      containerColor = SlateNavy
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Símbolos ELF en ${state.fileName}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Motor Goblin (Rust) & Desensamblador Capstone",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted
            )
          }

          // Chip filtro solo JNI
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (jniOnlyFilter) CyanPrimary else SlateCard)
              .clickable { jniOnlyFilter = !jniOnlyFilter }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = "⭐ Solo JNI",
              color = if (jniOnlyFilter) SlateDark else TextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = symbolSearchFilter,
          onValueChange = { symbolSearchFilter = it },
          placeholder = { Text("Buscar símbolo o función...", color = TextMuted) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = SlateCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        Spacer(modifier = Modifier.height(12.dp))

        val filteredSymbols = remember(state.soSymbols, symbolSearchFilter, jniOnlyFilter) {
          state.soSymbols.filter { sym ->
            val matchesFilter = symbolSearchFilter.isBlank() || sym.name.contains(symbolSearchFilter.trim(), ignoreCase = true)
            val matchesJni = !jniOnlyFilter || sym.isJni
            matchesFilter && matchesJni
          }
        }

        if (filteredSymbols.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(180.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (jniOnlyFilter) "No se encontraron funciones JNI" else "No se encontraron símbolos coincidentes",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(350.dp)
          ) {
            items(filteredSymbols) { item ->
              val isSelected = state.selectedSoSymbol?.name == item.name
              Card(
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) CyanPrimary.copy(alpha = 0.15f) else SlateCard
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                  brush = SolidColor(if (isSelected) CyanPrimary else SlateCardBorder)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .clickable {
                    viewModel.switchSoSymbol(item)
                    showSymbolPickerSheet = false
                  }
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(32.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (item.isJni) CyanPrimary else (if (isSelected) CyanPrimary else SlateNavy)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = if (item.isJni) "JNI" else (if (item.isExport) "EXP" else "SYM"),
                      color = if (item.isJni || isSelected) SlateDark else CyanPrimary,
                      fontWeight = FontWeight.Bold,
                      fontSize = 10.sp
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = item.name,
                      style = MaterialTheme.typography.bodyMedium,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold,
                      color = if (item.isJni) CyanGlow else TextPrimary,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Text(
                      text = if (item.isJni) "Función Nativa Java/JNI" else if (item.isExport) "Símbolo Exportado" else "Símbolo Importado",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextMuted
                    )
                  }
                }
              }
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
    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
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
