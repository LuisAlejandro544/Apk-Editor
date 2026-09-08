package com.example.ui.screens

import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppThemeMode
import com.example.ui.theme.AmberAccent
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
fun SettingsScreen(
  viewModel: ApkViewModel,
  onNavigateBack: () -> Unit
) {
  val currentThemeMode by viewModel.themeMode.collectAsState()
  val isAndroid12OrHigher = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
              .testTag("settings_back_button")
              .size(44.dp)
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
              text = "Configuración",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Personalización y apariencia del sistema",
              style = MaterialTheme.typography.bodySmall,
              color = CyanGlow
            )
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
      contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Header Section: Personalización
      item {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Palette,
            contentDescription = null,
            tint = CyanPrimary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Tema y Paleta Cromática",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Selecciona cómo se visualizan las superficies, tarjetas y botones en la pantalla táctil de tu teléfono.",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
      }

      // Opción 1: Cyber Dark (Predeterminado)
      item {
        ThemeOptionCard(
          title = AppThemeMode.CYBER_DARK.title,
          subtitle = AppThemeMode.CYBER_DARK.subtitle,
          description = AppThemeMode.CYBER_DARK.description,
          isSelected = currentThemeMode == AppThemeMode.CYBER_DARK,
          testTag = "settings_theme_cyber_dark",
          previewColors = listOf(
            CyanPrimary,
            MintSecondary,
            AmberAccent,
            SlateNavy
          ),
          badgeText = "Predeterminado",
          badgeColor = CyanPrimary,
          onClick = { viewModel.setThemeMode(AppThemeMode.CYBER_DARK) }
        )
      }

      // Opción 2: Material You (Color Dinámico)
      item {
        val dynamicColors = if (isAndroid12OrHigher) {
          listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.surfaceVariant
          )
        } else {
          listOf(SlateNavy, SlateCard, CyanPrimary, TextSecondary)
        }

        ThemeOptionCard(
          title = AppThemeMode.MATERIAL_YOU.title,
          subtitle = AppThemeMode.MATERIAL_YOU.subtitle,
          description = AppThemeMode.MATERIAL_YOU.description,
          isSelected = currentThemeMode == AppThemeMode.MATERIAL_YOU,
          testTag = "settings_theme_material_you",
          previewColors = dynamicColors,
          badgeText = if (isAndroid12OrHigher) "Android 12+ Monet" else "Requiere Android 12+",
          badgeColor = if (isAndroid12OrHigher) MintSecondary else AmberAccent,
          isSupported = isAndroid12OrHigher,
          unsupportedNotice = if (!isAndroid12OrHigher) {
            "Tu versión de Android (${Build.VERSION.RELEASE}) no soporta paletas dinámicas del sistema. Se utilizará el tema Cyber Dark."
          } else null,
          onClick = { viewModel.setThemeMode(AppThemeMode.MATERIAL_YOU) }
        )
      }

      // Información de Pantalla Completa y Margen de Estado (Edge to Edge)
      item {
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Fullscreen,
            contentDescription = null,
            tint = MintSecondary,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Pantalla Completa y Ajuste de Bordes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }
      }

      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MintSecondary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.Smartphone,
                  contentDescription = null,
                  tint = MintSecondary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Ajuste Safe Drawing (Edge-to-Edge)",
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = TextPrimary
                )
                Text(
                  text = "Protección de barra de estado y navegación",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextSecondary
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
              text = "La aplicación se extiende por toda la pantalla aprovechando las esquinas y los bordes del teléfono. Las barras de herramientas ahora cuentan con espaciado superior dinámico (statusBarsPadding) para evitar que la hora, el porcentaje de batería o la muesca de la cámara (notch) tapen los títulos o botones.",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              StatusItem(label = "Versión Android", value = "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
              StatusItem(label = "Modo Insets", value = "Edge-to-Edge")
              StatusItem(label = "Táctil Móvil", value = "Activo")
            }
          }
        }
      }

      // Tarjeta de información técnica
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = SlateDark),
          border = CardDefaults.outlinedCardBorder().copy(brush = SolidColor(SlateCardBorder)),
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Info,
              contentDescription = null,
              tint = CyanPrimary,
              modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "APK Extractor & Reverse Engineering",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
              Text(
                text = "Diseñado para teléfonos móviles • Sin necesidad de PC",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ThemeOptionCard(
  title: String,
  subtitle: String,
  description: String,
  isSelected: Boolean,
  testTag: String,
  previewColors: List<Color>,
  badgeText: String,
  badgeColor: Color,
  isSupported: Boolean = true,
  unsupportedNotice: String? = null,
  onClick: () -> Unit
) {
  val borderColor = if (isSelected) CyanPrimary else SlateCardBorder
  val cardBackground = if (isSelected) SlateCard else MaterialTheme.colorScheme.surface

  Card(
    colors = CardDefaults.cardColors(containerColor = cardBackground),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = SolidColor(borderColor),
      width = if (isSelected) 1.5.dp else 1.dp
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = Modifier
      .testTag(testTag)
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(enabled = isSupported) { onClick() }
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
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
              .size(24.dp)
              .clip(CircleShape)
              .border(
                width = 2.dp,
                color = if (isSelected) CyanPrimary else TextSecondary,
                shape = CircleShape
              ),
            contentAlignment = Alignment.Center
          ) {
            if (isSelected) {
              Box(
                modifier = Modifier
                  .size(12.dp)
                  .clip(CircleShape)
                  .background(CyanPrimary)
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Text(
              text = title,
              style = MaterialTheme.typography.bodyLarge,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = subtitle,
              style = MaterialTheme.typography.bodySmall,
              color = TextSecondary
            )
          }
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(badgeColor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = badgeText,
            color = badgeColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted,
        lineHeight = 18.sp
      )

      if (unsupportedNotice != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = unsupportedNotice,
          style = MaterialTheme.typography.labelSmall,
          color = AmberAccent
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Muestra de colores de la paleta
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Text(
          text = "Paleta:",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary
        )
        previewColors.forEach { color ->
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(color)
              .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
          )
        }
        if (isSelected) {
          Spacer(modifier = Modifier.weight(1f))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Done,
              contentDescription = "Activo",
              tint = CyanPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Activo",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = CyanPrimary
            )
          }
        }
      }
    }
  }
}

@Composable
private fun StatusItem(label: String, value: String) {
  Column {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = TextSecondary
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      fontWeight = FontWeight.SemiBold,
      color = TextPrimary
    )
  }
}
