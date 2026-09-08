package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ArscResourceItem
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
import com.example.viewmodel.ArscViewMode
import com.example.viewmodel.FileDetailState

@Composable
fun ArscViewerContent(
  state: FileDetailState,
  viewModel: ApkViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val arsc = state.arscResult

  if (arsc == null) {
    Box(
      modifier = modifier.fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = "No se pudieron analizar los recursos con ARSCLib.",
        color = TextSecondary,
        style = MaterialTheme.typography.bodyMedium
      )
    }
    return
  }

  // Filtrado reactivo en memoria
  val filteredEntries by remember(arsc.entries, state.arscFilterType, state.arscSearchQuery) {
    derivedStateOf {
      var list = arsc.entries
      if (!state.arscFilterType.isNullOrBlank()) {
        list = list.filter { it.typeName.equals(state.arscFilterType, ignoreCase = true) }
      }
      if (state.arscSearchQuery.isNotBlank()) {
        val q = state.arscSearchQuery.trim().lowercase()
        list = list.filter {
          it.entryName.lowercase().contains(q) ||
            it.hexId.lowercase().contains(q) ||
            it.reference.lowercase().contains(q) ||
            it.value.lowercase().contains(q) ||
            it.typeName.lowercase().contains(q)
        }
      }
      list
    }
  }

  // Lista de tipos de recursos disponibles para los chips
  val allTypes by remember(arsc.packages) {
    derivedStateOf {
      arsc.packages.flatMap { it.types }.distinct().sorted()
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = CodeBackground),
    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
    shape = RoundedCornerShape(12.dp),
    modifier = modifier.fillMaxSize()
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Barra superior: Switch entre Entradas y Resumen
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(SlateNavy)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
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
              .background(if (state.arscViewMode == ArscViewMode.ENTRIES) CyanPrimary else SlateCard)
              .clickable { viewModel.switchArscViewMode(ArscViewMode.ENTRIES) }
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = null,
                tint = if (state.arscViewMode == ArscViewMode.ENTRIES) SlateDark else TextSecondary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "Recursos (${filteredEntries.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.arscViewMode == ArscViewMode.ENTRIES) SlateDark else TextSecondary
              )
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (state.arscViewMode == ArscViewMode.SUMMARY) CyanPrimary else SlateCard)
              .clickable { viewModel.switchArscViewMode(ArscViewMode.SUMMARY) }
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (state.arscViewMode == ArscViewMode.SUMMARY) SlateDark else TextSecondary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = "Resumen (${arsc.packages.size} paq)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (state.arscViewMode == ArscViewMode.SUMMARY) SlateDark else TextSecondary
              )
            }
          }
        }

        Text(
          text = "ARSCLib 1.4.0",
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = MintSecondary
        )
      }

      if (state.arscViewMode == ArscViewMode.ENTRIES) {
        // Buscador interactivo
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .background(SlateDark)
            .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
          OutlinedTextField(
            value = state.arscSearchQuery,
            onValueChange = { viewModel.setArscSearchQuery(it) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp),
            placeholder = { Text("Buscar ID, nombre, valor o tipo...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                tint = CyanGlow,
                modifier = Modifier.size(18.dp)
              )
            },
            trailingIcon = {
              if (state.arscSearchQuery.isNotEmpty()) {
                IconButton(onClick = { viewModel.setArscSearchQuery("") }) {
                  Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Limpiar",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = SlateNavy,
              unfocusedContainerColor = SlateNavy,
              focusedBorderColor = CyanPrimary,
              unfocusedBorderColor = SlateCardBorder,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            )
          )

          // Chips horizontales por tipo de recurso
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            FilterChip(
              selected = state.arscFilterType == null,
              onClick = { viewModel.filterArscByType(null) },
              label = { Text("Todos (${arsc.entries.size})", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = CyanPrimary,
                selectedLabelColor = SlateDark,
                containerColor = SlateCard,
                labelColor = TextSecondary
              ),
              border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = state.arscFilterType == null,
                borderColor = SlateCardBorder,
                selectedBorderColor = CyanPrimary
              )
            )

            allTypes.forEach { type ->
              val count = remember(type, arsc.entries) {
                arsc.entries.count { it.typeName.equals(type, ignoreCase = true) }
              }
              val isSelected = state.arscFilterType.equals(type, ignoreCase = true)
              FilterChip(
                selected = isSelected,
                onClick = {
                  if (isSelected) viewModel.filterArscByType(null)
                  else viewModel.filterArscByType(type)
                },
                label = { Text("$type ($count)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = getTypeColor(type),
                  selectedLabelColor = SlateDark,
                  containerColor = SlateCard,
                  labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = isSelected,
                  borderColor = SlateCardBorder,
                  selectedBorderColor = getTypeColor(type)
                )
              )
            }
          }
        }

        // Lista de Entradas
        if (filteredEntries.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(Icons.Default.FilterList, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
              Spacer(modifier = Modifier.height(8.dp))
              Text("No se encontraron recursos que coincidan con el filtro.", color = TextSecondary, fontSize = 13.sp)
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            items(filteredEntries, key = { "${it.packageName}:${it.hexId}:${it.entryName}" }) { item ->
              ArscResourceCard(
                item = item,
                onCopy = { textToCopy ->
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  val clip = ClipData.newPlainText("Recurso Android", textToCopy)
                  clipboard.setPrimaryClip(clip)
                  Toast.makeText(context, "Copiado: $textToCopy", Toast.LENGTH_SHORT).show()
                }
              )
            }
          }
        }
      } else {
        // Modo Resumen & Estructura
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Tarjeta de Paquetes
          Card(
            colors = CardDefaults.cardColors(containerColor = SlateNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "Paquetes Registrados (${arsc.packages.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = CyanGlow
              )
              Spacer(modifier = Modifier.height(8.dp))

              arsc.packages.forEach { pkg ->
                Column(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateCard)
                    .padding(10.dp)
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = pkg.packageName,
                      fontWeight = FontWeight.Bold,
                      color = TextPrimary,
                      fontSize = 14.sp
                    )
                    Text(
                      text = "ID: 0x${Integer.toHexString(pkg.packageId).uppercase()}",
                      fontFamily = FontFamily.Monospace,
                      color = MintSecondary,
                      fontSize = 12.sp
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Total de recursos: ${pkg.totalEntries}",
                    color = TextSecondary,
                    fontSize = 12.sp
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = "Tipos contenidos:",
                    color = TextMuted,
                    fontSize = 11.sp
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    pkg.types.forEach { t ->
                      Box(
                        modifier = Modifier
                          .clip(RoundedCornerShape(4.dp))
                          .background(SlateNavy)
                          .padding(horizontal = 6.dp, vertical = 2.dp)
                      ) {
                        Text(
                          text = t,
                          color = getTypeColor(t),
                          fontSize = 10.sp,
                          fontFamily = FontFamily.Monospace
                        )
                      }
                    }
                  }
                }
                Spacer(modifier = Modifier.height(8.dp))
              }
            }
          }

          // Tarjeta de StringPool y Métricas
          Card(
            colors = CardDefaults.cardColors(containerColor = SlateNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "StringPool Global & Estadísticas",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MintSecondary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Total cadenas en StringPool:", color = TextSecondary, fontSize = 12.sp)
                Text("${arsc.stringPoolCount}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text("Recursos indexados:", color = TextSecondary, fontSize = 12.sp)
                Text("${arsc.entries.size} de ${arsc.totalEntries}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }

          // Reporte Completo
          Card(
            colors = CardDefaults.cardColors(containerColor = SlateNavy),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Reporte Técnico de ARSCLib",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                IconButton(
                  onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Reporte ARSC", arsc.summaryReport))
                    Toast.makeText(context, "Reporte copiado", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copiar reporte",
                    tint = CyanPrimary,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(8.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .background(CodeBackground)
                  .padding(10.dp)
              ) {
                Text(
                  text = arsc.summaryReport,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = CyanGlow,
                  lineHeight = 16.sp
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ArscResourceCard(
  item: ArscResourceItem,
  onCopy: (String) -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = SlateNavy),
    shape = RoundedCornerShape(8.dp),
    border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Badge del tipo
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(getTypeColor(item.typeName).copy(alpha = 0.18f))
              .border(1.dp, getTypeColor(item.typeName).copy(alpha = 0.6f), RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = item.typeName,
              color = getTypeColor(item.typeName),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // ID Hexadecimal
          Text(
            text = item.hexId,
            fontFamily = FontFamily.Monospace,
            color = MintSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }

        // Botón copiar referencia
        IconButton(
          onClick = { onCopy(item.reference) },
          modifier = Modifier.size(28.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copiar referencia",
            tint = TextMuted,
            modifier = Modifier.size(14.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(4.dp))

      // Nombre y referencia
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = item.entryName,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          fontSize = 13.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )
      }

      Text(
        text = item.reference,
        fontFamily = FontFamily.Monospace,
        color = CyanGlow,
        fontSize = 11.sp
      )

      if (item.value.isNotBlank() && item.value != item.reference) {
        Spacer(modifier = Modifier.height(6.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(SlateDark)
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = item.value,
            color = TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

private fun getTypeColor(typeName: String): Color {
  return when (typeName.lowercase()) {
    "string" -> Color(0xFF64B5F6) // Azul suave
    "color" -> AmberAccent // Ámbar
    "drawable", "mipmap" -> MintSecondary // Menta
    "layout" -> Color(0xFFFFB74D) // Naranja
    "id" -> Color(0xFFBA68C8) // Púrpura
    "dimen" -> Color(0xFF4DB6AC) // Turquesa
    "bool" -> Color(0xFF81C784) // Verde
    "style" -> Color(0xFFF06292) // Rosa
    "attr" -> Color(0xFFE57373) // Rojo claro
    "array", "plurals" -> Color(0xFFFFD54F) // Amarillo
    "raw" -> Color(0xFFA1887F) // Marrón suave
    else -> CyanPrimary
  }
}
