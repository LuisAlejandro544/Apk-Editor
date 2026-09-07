package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExtractedFileItem
import com.example.model.FileCategory
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
fun FileExplorerScreen(
  projectId: String,
  subPath: String,
  viewModel: ApkViewModel,
  onNavigateToSubfolder: (subPath: String) -> Unit,
  onNavigateToFileDetail: (relativePath: String) -> Unit,
  onNavigateBack: () -> Unit
) {
  val project by viewModel.currentProject.collectAsState()
  val items by viewModel.currentFolderItems.collectAsState()
  val isLoading by viewModel.isFolderLoading.collectAsState()

  var searchQuery by remember { mutableStateOf("") }

  LaunchedEffect(projectId, subPath) {
    viewModel.loadProjectFolder(projectId, subPath)
  }

  val filteredItems = remember(items, searchQuery) {
    if (searchQuery.isBlank()) {
      items
    } else {
      items.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }
  }

  // Breadcrumb path segments
  val segments = remember(subPath) {
    if (subPath.isBlank() || subPath == "/") {
      emptyList()
    } else {
      subPath.split("/").filter { it.isNotEmpty() }
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
              text = project?.name ?: "Explorador de Archivos",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = "${project?.fileCount ?: 0} archivos en caché",
              style = MaterialTheme.typography.bodySmall,
              color = CyanGlow
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("explorer_search_input"),
          placeholder = { Text("Filtrar archivos o carpetas...", color = TextMuted, fontSize = 14.sp) },
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

        // Breadcrumbs row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Root Crumb
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (segments.isEmpty()) CyanPrimary.copy(alpha = 0.2f) else SlateCard)
              .clickable { onNavigateToSubfolder("") }
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = null,
                tint = if (segments.isEmpty()) CyanPrimary else TextSecondary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "raíz (APK)",
                style = MaterialTheme.typography.labelSmall,
                color = if (segments.isEmpty()) CyanPrimary else TextSecondary,
                fontWeight = if (segments.isEmpty()) FontWeight.Bold else FontWeight.Normal
              )
            }
          }

          var accumulatedPath = ""
          segments.forEachIndexed { index, segment ->
            accumulatedPath = if (accumulatedPath.isEmpty()) segment else "$accumulatedPath/$segment"
            val targetPath = accumulatedPath
            val isLast = index == segments.size - 1

            Icon(
              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
              contentDescription = null,
              tint = TextMuted,
              modifier = Modifier.size(16.dp)
            )

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isLast) CyanPrimary.copy(alpha = 0.2f) else SlateCard)
                .clickable { onNavigateToSubfolder(targetPath) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = segment,
                style = MaterialTheme.typography.labelSmall,
                color = if (isLast) CyanPrimary else TextSecondary,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                fontFamily = FontFamily.Monospace
              )
            }
          }
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
        CircularProgressIndicator(color = CyanPrimary)
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // "Go to parent folder" button if inside a subfolder
        if (segments.isNotEmpty()) {
          item {
            val parentPath = segments.dropLast(1).joinToString("/")
            Card(
              colors = CardDefaults.cardColors(containerColor = SlateNavy.copy(alpha = 0.8f)),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToSubfolder(parentPath) }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.ArrowUpward,
                  contentDescription = "Subir nivel",
                  tint = MintSecondary,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                  text = ".. (Subir un nivel)",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold,
                  color = MintSecondary
                )
              }
            }
          }
        }

        // Summary item
        item {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${filteredItems.size} elementos",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted
            )
            if (searchQuery.isNotBlank()) {
              Text(
                text = "Filtrando por: \"$searchQuery\"",
                style = MaterialTheme.typography.labelSmall,
                color = AmberAccent
              )
            }
          }
        }

        if (filteredItems.isEmpty()) {
          item {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (searchQuery.isNotBlank()) "No se encontraron archivos con \"$searchQuery\"" else "Carpeta vacía",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
              )
            }
          }
        } else {
          items(filteredItems, key = { it.absolutePath }) { item ->
            FileListItem(
              item = item,
              onClick = {
                if (item.isDirectory) {
                  onNavigateToSubfolder(item.relativePath)
                } else {
                  onNavigateToFileDetail(item.relativePath)
                }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
fun FileListItem(
  item: ExtractedFileItem,
  onClick: () -> Unit
) {
  val icon: ImageVector = when {
    item.isDirectory -> Icons.Default.Folder
    item.category == FileCategory.MANIFEST -> Icons.Default.IntegrationInstructions
    item.category == FileCategory.DEX_BYTECODE -> Icons.Default.Terminal
    item.category == FileCategory.RESOURCES_ARSC -> Icons.Default.Settings
    item.category == FileCategory.IMAGE -> Icons.Default.Image
    item.category == FileCategory.CODE_OR_SCRIPT -> Icons.Default.Code
    else -> Icons.Default.Description
  }

  val iconTint: Color = when {
    item.isDirectory -> AmberAccent
    else -> Color(item.category.badgeColorHex)
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = SlateNavy),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder)),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .testTag("file_item_${item.name}")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(iconTint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = iconTint,
          modifier = Modifier.size(20.dp)
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = item.name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (item.isDirectory) FontWeight.SemiBold else FontWeight.Normal,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          fontFamily = if (item.isDirectory) FontFamily.Default else FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(item.category.badgeColorHex).copy(alpha = 0.15f))
              .padding(horizontal = 6.dp, vertical = 1.dp)
          ) {
            Text(
              text = if (item.isDirectory) "Carpeta" else item.category.label,
              style = MaterialTheme.typography.labelSmall,
              color = Color(item.category.badgeColorHex),
              fontSize = 10.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = item.formattedSize,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = TextMuted,
        modifier = Modifier.size(18.dp)
      )
    }
  }
}
