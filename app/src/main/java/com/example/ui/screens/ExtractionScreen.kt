package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
fun ExtractionScreen(
  viewModel: ApkViewModel,
  onNavigateToExplorer: (projectId: String) -> Unit,
  onNavigateBack: () -> Unit
) {
  val progress by viewModel.extractionProgress.collectAsState()

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .statusBarsPadding()
        .navigationBarsPadding()
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        // Status indicator icon
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
              when {
                progress.isCompleted -> MintSecondary.copy(alpha = 0.15f)
                progress.error != null -> CoralDanger.copy(alpha = 0.15f)
                else -> CyanPrimary.copy(alpha = 0.15f)
              }
            )
            .border(
              1.5.dp,
              when {
                progress.isCompleted -> MintSecondary
                progress.error != null -> CoralDanger
                else -> CyanPrimary
              },
              CircleShape
            ),
          contentAlignment = Alignment.Center
        ) {
          when {
            progress.isCompleted -> {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Completado",
                tint = MintSecondary,
                modifier = Modifier.size(44.dp)
              )
            }
            progress.error != null -> {
              Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                tint = CoralDanger,
                modifier = Modifier.size(44.dp)
              )
            }
            else -> {
              CircularProgressIndicator(
                color = CyanPrimary,
                trackColor = SlateCard,
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title text
        Text(
          text = when {
            progress.isCompleted -> "¡Extracción Completa!"
            progress.error != null -> "Fallo en la Extracción"
            else -> "Copiando Archivos del APK"
          },
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = when {
            progress.isCompleted -> "Todos los archivos han sido copiados a la caché temporal sin saturar la memoria RAM."
            progress.error != null -> progress.error ?: "Ocurrió un error inesperado"
            else -> "Extrayendo entradas del archivo APK mediante flujo seguro..."
          },
          style = MaterialTheme.typography.bodyMedium,
          color = if (progress.error != null) CoralDanger else TextSecondary,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Live Extraction Stats Card
        Card(
          colors = CardDefaults.cardColors(containerColor = SlateNavy),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            // Streaming indicator
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Memory,
                  contentDescription = null,
                  tint = MintSecondary,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Protección de Memoria RAM",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MintSecondary
                )
              }
              Text(
                text = "Búfer: 32 KB",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = CyanGlow
              )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Linear streaming progress bar
            if (!progress.isCompleted && progress.error == null) {
              LinearProgressIndicator(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = CyanPrimary,
                trackColor = SlateCard
              )
              Spacer(modifier = Modifier.height(16.dp))
            }

            // Stats row
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
                  text = "Archivos Extraídos",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = "${progress.extractedFilesCount}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MintSecondary
                )
              }

              Box(
                modifier = Modifier
                  .width(1.dp)
                  .height(36.dp)
                  .background(SlateCardBorder)
              )

              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                  text = "Tamaño en Caché",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = progress.formattedBytes,
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = AmberAccent
                )
              }
            }

            // Current extracting file name
            if (progress.currentFileName.isNotBlank() && !progress.isCompleted) {
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Copiando archivo:",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
              )
              Text(
                text = progress.currentFileName,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons
        if (progress.isCompleted && progress.projectId != null) {
          Button(
            onClick = { onNavigateToExplorer(progress.projectId!!) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("open_extracted_explorer_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = CyanPrimary,
              contentColor = SlateDark
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(
              text = "Explorar Archivos del APK",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(text = "Volver a Inicio")
          }
        } else if (progress.error != null) {
          Button(
            onClick = onNavigateBack,
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = SlateCard,
              contentColor = TextPrimary
            ),
            shape = RoundedCornerShape(12.dp)
          ) {
            Text(text = "Volver")
          }
        } else {
          // Cancel button during extraction
          OutlinedButton(
            onClick = {
              viewModel.cancelExtraction()
              onNavigateBack()
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("cancel_extraction_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralDanger),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Cancelar Operación")
          }
        }
      }
    }
  }
}
