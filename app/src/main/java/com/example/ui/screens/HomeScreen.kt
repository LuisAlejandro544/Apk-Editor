package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApkProject
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
fun HomeScreen(
  viewModel: ApkViewModel,
  onNavigateToExtraction: () -> Unit,
  onNavigateToInstalledApps: () -> Unit,
  onNavigateToExplorer: (projectId: String) -> Unit,
  onNavigateToCacheManager: () -> Unit,
  onNavigateToSettings: () -> Unit
) {
  val projects by viewModel.projects.collectAsState()
  val storageInfo by viewModel.storageInfo.collectAsState()

  val apkPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument()
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.extractApk(uri) {
        onNavigateToExtraction()
      }
    }
  }

  // Also support GetContent fallback launcher
  val fallbackPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      viewModel.extractApk(uri) {
        onNavigateToExtraction()
      }
    }
  }

  LaunchedEffect(Unit) {
    viewModel.refreshProjectsAndStorage()
  }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.background)
          .statusBarsPadding()
          .padding(horizontal = 20.dp, vertical = 16.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CyanPrimary.copy(alpha = 0.15f))
                .border(1.dp, CyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.FolderZip,
                contentDescription = "Icono APK",
                tint = CyanPrimary,
                modifier = Modifier.size(24.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "APK Extractor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Text(
                text = "Ingeniería Inversa & Prototipado",
                style = MaterialTheme.typography.bodySmall,
                color = CyanGlow
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = onNavigateToSettings,
              modifier = Modifier
                .testTag("settings_button")
                .size(44.dp)
                .clip(CircleShape)
                .background(SlateCard)
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configuración",
                tint = TextPrimary
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
              onClick = onNavigateToCacheManager,
              modifier = Modifier
                .testTag("cache_manager_button")
                .size(44.dp)
                .clip(CircleShape)
                .background(SlateCard)
            ) {
              Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = "Administrador de Caché",
                tint = TextPrimary
              )
            }
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .navigationBarsPadding()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Memory & Cache Safety Card
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = SlateNavy),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("ram_protection_card")
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Memory,
                  contentDescription = null,
                  tint = MintSecondary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Protección Anti-Crash (Streaming)",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MintSecondary
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(MintSecondary.copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "Búfer 32KB",
                  style = MaterialTheme.typography.labelSmall,
                  fontFamily = FontFamily.Monospace,
                  color = MintSecondary
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Los archivos del APK se copian por fragmentos continuos directamente a la memoria caché temporal, evitando saturar la memoria RAM del teléfono.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CodeBackground)
                .padding(horizontal = 12.dp, vertical = 8.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Caché de Proyectos",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = storageInfo.formattedCacheUsed,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = TextPrimary
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = "Espacio Libre Teléfono",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
                Text(
                  text = storageInfo.formattedAvailable,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  fontFamily = FontFamily.Monospace,
                  color = CyanPrimary
                )
              }
            }
          }
        }
      }

      // Action Banner: Select APK
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = SlateCard),
          shape = RoundedCornerShape(20.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(CyanPrimary.copy(alpha = 0.4f))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(CyanPrimary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.FileOpen,
                contentDescription = null,
                tint = CyanPrimary,
                modifier = Modifier.size(30.dp)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Elegir APK para Copiar Archivos",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Selecciona un archivo .apk de tus Descargas o Almacenamiento. Se extraerán todos sus recursos, código DEX y manifiesto.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = {
                try {
                  apkPickerLauncher.launch(arrayOf("application/vnd.android.package-archive", "application/octet-stream", "*/*"))
                } catch (_: Exception) {
                  fallbackPickerLauncher.launch("*/*")
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("select_apk_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = CyanPrimary,
                contentColor = SlateDark
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Seleccionar APK del Dispositivo",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }
        }
      }

      // Action Banner 2: Extract Installed App
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = SlateNavy),
          shape = RoundedCornerShape(20.dp),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MintSecondary.copy(alpha = 0.5f))),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MintSecondary.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MintSecondary,
                modifier = Modifier.size(30.dp)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = "Extraer App Instalada en el Dispositivo",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = "Extrae el APK completo de cualquier app instalada y en uso en tu teléfono sin requerir root ni conexión a PC.",
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
              onClick = onNavigateToInstalledApps,
              modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("extract_installed_app_button"),
              colors = ButtonDefaults.buttonColors(
                containerColor = MintSecondary,
                contentColor = SlateDark
              ),
              shape = RoundedCornerShape(12.dp)
            ) {
              Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Ver Apps Instaladas",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
            }
          }
        }
      }

      // Native Engine (C, C++, Rust, Lua) Status Card
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = CodeBackground),
          border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder)),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(MintSecondary)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Arquitectura Multilenguaje Lista",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = com.example.nativebridge.NativeEngineBridge.getEngineStatusString(),
              style = MaterialTheme.typography.labelSmall,
              fontFamily = FontFamily.Monospace,
              color = CyanGlow,
              fontSize = 11.sp
            )
          }
        }
      }

      // Header for projects
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Proyectos Extraídos (${projects.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )

          if (projects.isNotEmpty()) {
            Text(
              text = "En Caché Temporal",
              style = MaterialTheme.typography.labelSmall,
              color = MintSecondary
            )
          }
        }
      }

      // Empty State
      if (projects.isEmpty()) {
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = SlateNavy.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(
                imageVector = Icons.Default.FolderZip,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Text(
                text = "Ningún APK extraído todavía",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Pulsa el botón de arriba para elegir un APK descargado y descomprimir su estructura de archivos para prototipado.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              )
            }
          }
        }
      } else {
        items(projects, key = { it.id }) { project ->
          ProjectCardItem(
            project = project,
            onExplore = { onNavigateToExplorer(project.id) },
            onDelete = { viewModel.deleteProject(project.id) }
          )
        }
      }
    }
  }
}

@Composable
fun ProjectCardItem(
  project: ApkProject,
  onExplore: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = SlateNavy),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder)),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .fillMaxWidth()
      .testTag("project_item_${project.id}")
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(CyanPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.FolderZip,
              contentDescription = null,
              tint = CyanPrimary,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = project.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              maxLines = 1
            )
            Text(
              text = project.originalFileName,
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
              maxLines = 1
            )
          }
        }

        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Eliminar de caché",
            tint = CoralDanger.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(CodeBackground)
          .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Archivos: ",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
          )
          Text(
            text = "${project.fileCount}",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MintSecondary
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "Tamaño: ",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
          )
          Text(
            text = project.formattedSize,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = AmberAccent
          )
        }

        Text(
          text = project.formattedDate,
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Button(
        onClick = onExplore,
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp)
          .testTag("explore_button_${project.id}"),
        colors = ButtonDefaults.buttonColors(
          containerColor = SlateCard,
          contentColor = CyanGlow
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanPrimary.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text(
          text = "Explorar Archivos Extraídos",
          fontWeight = FontWeight.SemiBold,
          fontSize = 14.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
