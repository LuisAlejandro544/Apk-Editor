package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CodeBackground
import com.example.ui.theme.CoralDanger
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

@Composable
fun CacheManagerScreen(
  viewModel: ApkViewModel,
  onNavigateBack: () -> Unit
) {
  val storageInfo by viewModel.storageInfo.collectAsState()
  val projects by viewModel.projects.collectAsState()

  Scaffold(
    containerColor = SlateDark,
    topBar = {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(SlateNavy)
          .padding(horizontal = 16.dp, vertical = 12.dp),
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

        Column {
          Text(
            text = "Gestor de Caché y Memoria",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
          Text(
            text = "Protección de RAM y almacenamiento temporal",
            style = MaterialTheme.typography.bodySmall,
            color = CyanGlow
          )
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Memory Architecture Explanation Card
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = SlateNavy),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MintSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Memory,
                  contentDescription = null,
                  tint = MintSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "Arquitectura de Bajo Consumo RAM",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Para evitar congelamientos o errores 'Out of Memory' al manipular APKs pesados, la app utiliza un flujo en streaming con un búfer estricto de 32 KB. Ningún archivo APK completo se carga en la memoria RAM: cada archivo se escribe directamente a la partición de caché temporal.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(CodeBackground)
                .padding(12.dp),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "Caché de APKs",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = storageInfo.formattedCacheUsed,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = MintSecondary
                )
              }

              Box(
                modifier = Modifier
                  .width(1.dp)
                  .height(32.dp)
                  .background(SlateCardBorder)
              )

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "Almacenamiento Libre",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = storageInfo.formattedAvailable,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = CyanPrimary
                )
              }
            }
          }
        }
      }

      // Wipe Cache CTA
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = SlateCard),
          shape = RoundedCornerShape(16.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CoralDanger.copy(alpha = 0.3f))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.CleaningServices,
                contentDescription = null,
                tint = CoralDanger,
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = "Limpieza de Caché Temporal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = "Elimina de forma segura todas las carpetas temporales de APKs extraídos para liberar espacio de disco inmediatamente.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
              onClick = {
                viewModel.clearAllCache()
              },
              enabled = projects.isNotEmpty(),
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("wipe_cache_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = CoralDanger,
                contentColor = SlateDark
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Limpiar Toda la Caché (${storageInfo.formattedCacheUsed})",
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Projects list header
      item {
        Text(
          text = "Carpetas Temporales en Disco (${projects.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
      }

      if (projects.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = SlateNavy.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No hay archivos temporales de APK en caché.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
              )
            }
          }
        }
      } else {
        items(projects, key = { it.id }) { project ->
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
                  text = project.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "${project.fileCount} archivos • ${project.formattedSize}",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = AmberAccent
                )
              }

              OutlinedButton(
                onClick = { viewModel.deleteProject(project.id) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralDanger),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(text = "Eliminar", fontSize = 12.sp)
              }
            }
          }
        }
      }
    }
  }
}
