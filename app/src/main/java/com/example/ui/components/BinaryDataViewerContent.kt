package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import com.example.data.BinaryDataParser
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
import com.example.viewmodel.BinaryEditMode
import com.example.viewmodel.FileDetailState

@Composable
fun BinaryDataViewerContent(
  state: FileDetailState,
  viewModel: ApkViewModel,
  projectId: String,
  relativePath: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val binaryResult = state.binaryDataResult

  // Estado local para edición
  var isEditMode by remember { mutableStateOf(false) }
  var hexContent by remember(state.binaryHexEditable) { mutableStateOf(state.binaryHexEditable) }
  var textContent by remember(state.binaryTextEditable) { mutableStateOf(state.binaryTextEditable) }

  // Filtrado de strings
  var stringsFilter by remember { mutableStateOf("") }

  val hasHexChanges = remember(hexContent, state.binaryHexEditable) { hexContent != state.binaryHexEditable }
  val hasTextChanges = remember(textContent, state.binaryTextEditable) { textContent != state.binaryTextEditable }
  val hasUnsavedChanges = if (state.binaryEditMode == BinaryEditMode.HEX) hasHexChanges else if (state.binaryEditMode == BinaryEditMode.TEXT) hasTextChanges else false

  // Validación de hex
  val hexValidation by remember(hexContent) {
    derivedStateOf {
      val clean = hexContent.replace(Regex("[^0-9A-Fa-f]"), "")
      if (clean.length % 2 != 0) {
        "Longitud impar (${clean.length} caracteres). Cada byte requiere 2 dígitos hexadecimales."
      } else {
        null
      }
    }
  }

  val estimatedBytesCount by remember(hexContent) {
    derivedStateOf {
      val clean = hexContent.replace(Regex("[^0-9A-Fa-f]"), "")
      clean.length / 2
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = CodeBackground),
    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(if (hasUnsavedChanges) AmberAccent else SlateCardBorder)),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier.fillMaxSize()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // 1. Barra de herramientas superior
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(SlateNavy)
          .padding(horizontal = 12.dp, vertical = 8.dp)
      ) {
        // Fila A: Badge de formato detectado y Selector de modo
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Selector de Modo (Editor Hex, Texto/UTF-8, Inspector)
          Row(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(SlateCard)
              .padding(2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            BinaryEditMode.values().forEach { mode ->
              val isSelected = state.binaryEditMode == mode
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSelected) CyanPrimary else SlateCard)
                  .clickable {
                    viewModel.switchBinaryEditMode(mode)
                  }
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

          // Badge de Formato
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(AmberAccent.copy(alpha = 0.2f))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = binaryResult?.detectedFormat ?: "Binario .dat/.bin",
              color = AmberAccent,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Fila B: Acciones según modo (Formatear, Guardar, Modo Edición)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            when (state.binaryEditMode) {
              BinaryEditMode.HEX -> {
                Text(
                  text = "$estimatedBytesCount bytes",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = CyanGlow,
                  fontSize = 11.sp
                )
                // Botón Formatear Hex
                Button(
                  onClick = {
                    hexContent = BinaryDataParser.formatHexForDisplay(hexContent)
                    viewModel.updateBinaryHexContent(hexContent)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = SlateCard),
                  shape = RoundedCornerShape(6.dp),
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp)
                ) {
                  Icon(Icons.Default.AutoFixHigh, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(12.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Formatear", color = CyanPrimary, fontSize = 10.sp)
                }

                // Botón Copiar Hex
                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Hex Dump", hexContent))
                    Toast.makeText(context, "Bytes hexadecimales copiados", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copiar Hex", tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
              }
              BinaryEditMode.TEXT -> {
                val linesCount = remember(textContent) { textContent.lines().size }
                Text(
                  text = "$linesCount líneas • UTF-8/Raw",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = MintSecondary,
                  fontSize = 11.sp
                )
              }
              BinaryEditMode.INSPECTOR -> {
                Text(
                  text = "Entropía: ${binaryResult?.entropy ?: 0.0}/8.0",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = AmberAccent,
                  fontSize = 11.sp
                )
              }
            }
          }

          // Botones Guardar y Modo Edición
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            if (hasUnsavedChanges) {
              IconButton(
                onClick = {
                  if (state.binaryEditMode == BinaryEditMode.HEX) {
                    hexContent = state.binaryHexEditable
                  } else {
                    textContent = state.binaryTextEditable
                  }
                },
                modifier = Modifier.size(30.dp)
              ) {
                Icon(Icons.Default.Refresh, contentDescription = "Deshacer", tint = AmberAccent, modifier = Modifier.size(16.dp))
              }

              Button(
                onClick = {
                  if (state.binaryEditMode == BinaryEditMode.HEX && hexValidation != null) {
                    Toast.makeText(context, hexValidation, Toast.LENGTH_LONG).show()
                    return@Button
                  }

                  val contentToSave = if (state.binaryEditMode == BinaryEditMode.HEX) hexContent else textContent
                  viewModel.saveBinaryContent(projectId, relativePath, contentToSave, state.binaryEditMode) { success, msg ->
                    Toast.makeText(
                      context,
                      if (success) "Archivo binario guardado exitosamente" else (msg ?: "Error al guardar binario"),
                      Toast.LENGTH_SHORT
                    ).show()
                  }
                },
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = MintSecondary),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
              ) {
                if (state.isSaving) {
                  CircularProgressIndicator(color = SlateDark, modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                } else {
                  Icon(Icons.Default.Save, contentDescription = null, tint = SlateDark, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Guardar", color = SlateDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }

            if (state.binaryEditMode != BinaryEditMode.INSPECTOR) {
              Button(
                onClick = { isEditMode = !isEditMode },
                colors = ButtonDefaults.buttonColors(containerColor = if (isEditMode) CyanPrimary else SlateCard),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp)
              ) {
                Icon(
                  if (isEditMode) Icons.Default.Visibility else Icons.Default.Edit,
                  contentDescription = null,
                  tint = if (isEditMode) SlateDark else CyanPrimary,
                  modifier = Modifier.size(12.dp)
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

        // Advertencia de validación hex si aplica
        if (state.binaryEditMode == BinaryEditMode.HEX && hexValidation != null) {
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(AmberAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(hexValidation ?: "", color = AmberAccent, fontSize = 11.sp)
          }
        }
      }

      // 2. Contenido principal según la pestaña de edición
      when (state.binaryEditMode) {
        BinaryEditMode.HEX -> {
          // Editor Hexadecimal con scroll
          val verticalScroll = rememberScrollState()
          val horizontalScroll = rememberScrollState()

          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(8.dp)
              .verticalScroll(verticalScroll)
          ) {
            BasicTextField(
              value = hexContent,
              onValueChange = {
                if (isEditMode) {
                  hexContent = it
                  viewModel.updateBinaryHexContent(it)
                }
              },
              readOnly = !isEditMode,
              textStyle = TextStyle(
                color = if (isEditMode) CyanGlow else TextPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 22.sp
              ),
              cursorBrush = SolidColor(CyanPrimary),
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .padding(8.dp)
                .testTag("binary_hex_text_field")
            )
          }
        }

        BinaryEditMode.TEXT -> {
          // Editor de texto / UTF-8
          val verticalScroll = rememberScrollState()
          val horizontalScroll = rememberScrollState()
          val lineCount = remember(textContent) { maxOf(1, textContent.lines().size) }
          val lineNumbers = remember(lineCount) { (1..lineCount).joinToString("\n") }

          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(8.dp)
              .verticalScroll(verticalScroll)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
                .padding(vertical = 6.dp)
            ) {
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
                  .width(36.dp)
                  .padding(end = 8.dp)
              )

              Box(
                modifier = Modifier
                  .width(1.dp)
                  .height(20.dp * lineCount)
                  .background(SlateCardBorder)
              )

              Spacer(modifier = Modifier.width(8.dp))

              BasicTextField(
                value = textContent,
                onValueChange = {
                  if (isEditMode) {
                    textContent = it
                    viewModel.updateBinaryTextContent(it)
                  }
                },
                readOnly = !isEditMode,
                textStyle = TextStyle(
                  color = if (isEditMode) TextPrimary else TextSecondary,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  lineHeight = 20.sp
                ),
                cursorBrush = SolidColor(MintSecondary),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(end = 16.dp)
                  .testTag("binary_text_field")
              )
            }
          }
        }

        BinaryEditMode.INSPECTOR -> {
          // Inspector de estructura binaria, Entropía y Cadenas legibles
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(12.dp)
          ) {
            // Card de Diagnóstico de Estructura Binaria
            Card(
              colors = CardDefaults.cardColors(containerColor = SlateCard),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "ANÁLISIS DE ESTRUCTURA BINARIA",
                  style = MaterialTheme.typography.labelSmall,
                  color = CyanPrimary,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Formato Identificado:", color = TextSecondary, fontSize = 12.sp)
                  Text(binaryResult?.detectedFormat ?: "Desconocido", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("MIME (Apache Tika):", color = TextSecondary, fontSize = 12.sp)
                  Text(binaryResult?.mimeType ?: "application/octet-stream", color = TextPrimary, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Entropía de Shannon:", color = TextSecondary, fontSize = 12.sp)
                  Text("${binaryResult?.entropy ?: 0.0} / 8.00", color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = binaryResult?.entropyDescription ?: "",
                  color = TextMuted,
                  fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  Text("Tamaño en Disco:", color = TextSecondary, fontSize = 12.sp)
                  Text(formatBytes(binaryResult?.fileSize ?: 0L), color = TextPrimary, fontSize = 12.sp)
                }
                if (binaryResult?.isTruncated == true) {
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Nota: Archivo extenso; cargados los primeros ${formatBytes(binaryResult.loadedBytesCount.toLong())} para optimizar el rendimiento táctil.",
                    color = AmberAccent,
                    fontSize = 11.sp
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Buscador de Cadenas Legibles (Strings)
            OutlinedTextField(
              value = stringsFilter,
              onValueChange = { stringsFilter = it },
              modifier = Modifier.fillMaxWidth(),
              placeholder = { Text("Filtrar cadenas legibles...", color = TextMuted, fontSize = 12.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(16.dp)) },
              trailingIcon = {
                if (stringsFilter.isNotEmpty()) {
                  IconButton(onClick = { stringsFilter = "" }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextMuted, modifier = Modifier.size(16.dp))
                  }
                }
              },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SlateNavy,
                unfocusedContainerColor = SlateNavy,
                focusedBorderColor = CyanPrimary,
                unfocusedBorderColor = SlateCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
              ),
              shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            val allStrings = binaryResult?.printableStrings ?: emptyList()
            val filteredStrings = remember(allStrings, stringsFilter) {
              if (stringsFilter.isBlank()) allStrings else allStrings.filter { it.contains(stringsFilter, ignoreCase = true) }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "CADENAS LEGIBLES (${filteredStrings.size})",
                style = MaterialTheme.typography.labelSmall,
                color = CyanPrimary,
                fontWeight = FontWeight.Bold
              )

              if (allStrings.isNotEmpty()) {
                Text(
                  text = "Toca para copiar",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted,
                  fontSize = 10.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (filteredStrings.isEmpty()) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(SlateCard, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = if (allStrings.isEmpty()) "No se encontraron cadenas de texto legibles" else "Ninguna cadena coincide con '$stringsFilter'",
                  color = TextMuted,
                  fontSize = 12.sp
                )
              }
            } else {
              LazyColumn(
                modifier = Modifier
                  .fillMaxSize()
                  .background(SlateCard, RoundedCornerShape(8.dp))
                  .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                items(filteredStrings) { str ->
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .clip(RoundedCornerShape(6.dp))
                      .background(CodeBackground)
                      .clickable {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("String", str))
                        Toast.makeText(context, "Copiado: $str", Toast.LENGTH_SHORT).show()
                      }
                      .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = str,
                      style = MaterialTheme.typography.bodySmall,
                      fontFamily = FontFamily.Monospace,
                      color = TextPrimary,
                      fontSize = 12.sp,
                      modifier = Modifier.weight(1f)
                    )
                    Icon(
                      Icons.Default.ContentCopy,
                      contentDescription = "Copiar",
                      tint = TextMuted,
                      modifier = Modifier.size(14.dp)
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
