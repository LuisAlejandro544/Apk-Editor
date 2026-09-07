package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.withContext
import com.example.util.AppDispatchers
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.model.InstalledAppItem
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

@Composable
fun InstalledAppsScreen(
  viewModel: ApkViewModel,
  onNavigateToExtraction: () -> Unit,
  onNavigateBack: () -> Unit
) {
  val apps by viewModel.installedApps.collectAsState()
  val isLoading by viewModel.isInstalledAppsLoading.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var showOnlyUserApps by remember { mutableStateOf(true) }

  val filteredApps = remember(apps, searchQuery, showOnlyUserApps) {
    apps.filter { app ->
      val matchesSearch = searchQuery.isBlank() ||
        app.appName.contains(searchQuery.trim(), ignoreCase = true) ||
        app.packageName.contains(searchQuery.trim(), ignoreCase = true)

      val matchesFilter = if (showOnlyUserApps) !app.isSystemApp else true
      matchesSearch && matchesFilter
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
              text = "Extraer App Instalada",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Apps en uso en tu dispositivo",
              style = MaterialTheme.typography.bodySmall,
              color = CyanGlow
            )
          }

          IconButton(
            onClick = { viewModel.loadInstalledApps() },
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(SlateCard)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Actualizar",
              tint = CyanPrimary
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search field
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("installed_search_input"),
          placeholder = { Text("Buscar por nombre o paquete...", color = TextMuted, fontSize = 14.sp) },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Buscar",
              tint = CyanPrimary,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Limpiar",
                  tint = TextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = CodeBackground,
            unfocusedContainerColor = CodeBackground,
            focusedBorderColor = CyanPrimary,
            unfocusedBorderColor = SlateCardBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = showOnlyUserApps,
            onClick = { showOnlyUserApps = true },
            label = { Text("Apps de Usuario", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
              selectedLabelColor = CyanPrimary
            )
          )
          FilterChip(
            selected = !showOnlyUserApps,
            onClick = { showOnlyUserApps = false },
            label = { Text("Todas (Inc. Sistema)", fontSize = 12.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = AmberAccent.copy(alpha = 0.2f),
              selectedLabelColor = AmberAccent
            )
          )
        }
      }
    }
  ) { paddingValues ->
    if (isLoading) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(color = CyanPrimary)
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Leyendo paquetes del sistema...",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${filteredApps.size} aplicaciones disponibles",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted
            )
          }
        }

        if (filteredApps.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No se encontraron aplicaciones instaladas.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
              )
            }
          }
        } else {
          items(filteredApps, key = { it.packageName }) { appItem ->
            InstalledAppCard(
              app = appItem,
              onExtract = {
                viewModel.extractInstalledApp(appItem, onNavigateToExtraction)
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun InstalledAppCard(
  app: InstalledAppItem,
  onExtract: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(containerColor = SlateNavy),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder)),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onExtract)
      .testTag("app_card_${app.packageName}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // App Icon (Decodificado asíncronamente en hilo secundario para 60-120fps fluidos)
      var iconBitmap by remember(app.packageName) { mutableStateOf<ImageBitmap?>(null) }
      LaunchedEffect(app.iconDrawable) {
        if (app.iconDrawable != null) {
          val bmp = withContext(AppDispatchers.FastIODispatcher) {
            try {
              app.iconDrawable.toBitmap(width = 96, height = 96).asImageBitmap()
            } catch (_: Throwable) {
              null
            }
          }
          iconBitmap = bmp
        }
      }

      val currentIcon = iconBitmap
      if (currentIcon != null) {
        Image(
          bitmap = currentIcon,
          contentDescription = app.appName,
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
        )
      } else {
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(CyanPrimary.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Android,
            contentDescription = null,
            tint = CyanPrimary,
            modifier = Modifier.size(26.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = app.appName,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
          text = app.packageName,
          style = MaterialTheme.typography.labelSmall,
          fontFamily = FontFamily.Monospace,
          color = TextMuted,
          fontSize = 11.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "v${app.versionName} • ${app.formattedSize}",
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = AmberAccent,
            fontSize = 11.sp
          )

          if (app.isSystemApp) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(MintSecondary.copy(alpha = 0.15f))
                .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
              Text(
                text = "Sistema",
                fontSize = 9.sp,
                color = MintSecondary,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.width(8.dp))

      Button(
        onClick = onExtract,
        colors = ButtonDefaults.buttonColors(
          containerColor = CyanPrimary,
          contentColor = SlateDark
        ),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Download,
          contentDescription = "Extraer",
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = "Extraer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  }
}
