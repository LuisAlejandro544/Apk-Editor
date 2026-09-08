package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CpuStressLevel
import com.example.model.CpuStressMetrics
import com.example.model.ThreadInfoItem
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CoralDanger
import com.example.ui.theme.CyanGlow
import com.example.ui.theme.MintSecondary
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateCardBorder
import com.example.ui.theme.SlateDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.ApkViewModel
import com.pluto.Pluto

@Composable
fun ThreadCpuProfilerScreen(
  viewModel: ApkViewModel,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  val metrics by viewModel.cpuMetrics.collectAsState()
  val threadList by viewModel.threadList.collectAsState()

  // Iniciar el muestreador periódico mientras la pantalla esté activa
  DisposableEffect(Unit) {
    viewModel.startProfilerMonitoring()
    onDispose {
      viewModel.stopProfilerMonitoring()
    }
  }

  var selectedFilter by remember { mutableStateOf("Todos") }
  val filters = listOf(
    "Todos",
    "Activos (RUNNABLE)",
    "Hilo Principal (UI)",
    "Cálculo / Corrutinas",
    "Entrada / Salida (I/O)",
    "Motor JADX Decompiler",
    "Diagnóstico / Herramientas"
  )

  val filteredThreads = remember(threadList, selectedFilter) {
    when (selectedFilter) {
      "Activos (RUNNABLE)" -> threadList.filter { it.isCpuActive }
      "Hilo Principal (UI)" -> threadList.filter { it.isMainThread }
      "Cálculo / Corrutinas" -> threadList.filter { it.category == "Cálculo / Corrutinas" }
      "Entrada / Salida (I/O)" -> threadList.filter { it.category == "Entrada / Salida (I/O)" }
      "Motor JADX Decompiler" -> threadList.filter { it.category == "Motor JADX Decompiler" }
      "Diagnóstico / Herramientas" -> threadList.filter {
        it.category.contains("Pluto", ignoreCase = true) ||
            it.category.contains("DoraemonKit", ignoreCase = true) ||
            it.category.contains("LeakCanary", ignoreCase = true)
      }
      else -> threadList
    }
  }

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
              text = "Monitor de Hilos y Estrés CPU",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "Auditoría en tiempo real de hilos y procesador",
              style = MaterialTheme.typography.bodySmall,
              color = CyanGlow
            )
          }

          // Botón para forzar actualización de snapshot
          IconButton(
            onClick = { viewModel.refreshProfilerSnapshot() },
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(SlateCard)
          ) {
            Icon(
              imageVector = Icons.Default.Refresh,
              contentDescription = "Actualizar métricas",
              tint = CyanGlow
            )
          }
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .navigationBarsPadding(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. Tarjeta de Acceso Rápido a Suites de Depuración Móvil (Pluto, DoraemonKit, LeakCanary)
      item {
        DebugSuitesHeaderCard(
          onOpenPluto = {
            try {
              Pluto.open()
            } catch (e: Throwable) {
              Toast.makeText(context, "Pluto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
          }
        )
      }

      // 2. Tarjeta del Medidor de Estrés de CPU y Hardware
      item {
        CpuStressCard(metrics = metrics)
      }

      // 3. Tarjeta del Hilo Principal (Main/UI Thread)
      item {
        MainThreadFocusCard(metrics = metrics)
      }

      // 4. Selector de Filtros de Hilos
      item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "HILOS DE EJECUCIÓN (${filteredThreads.size} de ${threadList.size})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextMuted
          )

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            filters.forEach { filter ->
              val isSelected = selectedFilter == filter
              FilterChip(
                selected = isSelected,
                onClick = { selectedFilter = filter },
                label = { Text(filter, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                  selectedContainerColor = CyanGlow.copy(alpha = 0.2f),
                  selectedLabelColor = CyanGlow,
                  containerColor = SlateCard,
                  labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                  enabled = true,
                  selected = isSelected,
                  borderColor = if (isSelected) CyanGlow else SlateCardBorder
                )
              )
            }
          }
        }
      }

      // 5. Lista de Hilos
      if (filteredThreads.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No se encontraron hilos que coincidan con el filtro seleccionado.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted
            )
          }
        }
      } else {
        items(filteredThreads, key = { it.id }) { threadItem ->
          ThreadRowCard(threadItem = threadItem)
        }
      }
    }
  }
}

@Composable
private fun DebugSuitesHeaderCard(
  onOpenPluto: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SlateCard),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Tune,
          contentDescription = null,
          tint = AmberAccent,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "HERRAMIENTAS DE DEPURACIÓN EN DISPOSITIVO",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = AmberAccent
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      Text(
        text = "Pluto, LeakCanary y DoraemonKit están integrados y operativos sin requerir conexión a PC ni Android Studio.",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Botón para invocar la suite Pluto
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onOpenPluto() },
          color = CyanGlow.copy(alpha = 0.15f),
          border = androidx.compose.foundation.BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f)),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.BugReport,
              contentDescription = null,
              tint = CyanGlow,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Abrir Pluto",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = CyanGlow
            )
          }
        }

        // Indicador de LeakCanary
        Surface(
          modifier = Modifier
            .weight(1f),
          color = SlateDark,
          border = androidx.compose.foundation.BorderStroke(1.dp, SlateCardBorder),
          shape = RoundedCornerShape(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(MintSecondary)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "LeakCanary Activo",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Medium,
              color = TextPrimary
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CpuStressCard(metrics: CpuStressMetrics) {
  val stressColor = when (metrics.stressLevel) {
    CpuStressLevel.LOW -> MintSecondary
    CpuStressLevel.MODERATE -> CyanGlow
    CpuStressLevel.HIGH -> AmberAccent
    CpuStressLevel.CRITICAL -> CoralDanger
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SlateCard),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Speed,
            contentDescription = null,
            tint = stressColor,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "ESTRÉS DE CPU Y RECURSOS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
          )
        }

        Surface(
          color = stressColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, stressColor.copy(alpha = 0.4f))
        ) {
          Text(
            text = metrics.stressLevel.label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = stressColor
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Indicador de Porcentaje de CPU
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Consumo estimado del proceso",
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary
        )
        Text(
          text = "${String.format("%.1f", metrics.processCpuUsagePercent)}%",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = stressColor
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { (metrics.processCpuUsagePercent / 100f).coerceIn(0f, 1f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp)),
        color = stressColor,
        trackColor = SlateDark
      )

      Spacer(modifier = Modifier.height(16.dp))

      // Cuadrícula de Métricas de Hardware
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        MetricBox(
          modifier = Modifier.weight(1f),
          title = "Núcleos CPU",
          value = "${metrics.availableProcessors} núcleos",
          highlight = CyanGlow
        )
        MetricBox(
          modifier = Modifier.weight(1f),
          title = "Memoria Usada",
          value = "${metrics.usedMemoryMb} MB",
          subtitle = "de ${metrics.maxMemoryMb} MB máx",
          highlight = TextPrimary
        )
        MetricBox(
          modifier = Modifier.weight(1f),
          title = "Hilos Activos",
          value = "${metrics.runningThreadsCount} / ${metrics.activeThreadsCount}",
          subtitle = "en ejecución",
          highlight = if (metrics.runningThreadsCount > 4) AmberAccent else MintSecondary
        )
      }
    }
  }
}

@Composable
private fun MetricBox(
  modifier: Modifier = Modifier,
  title: String,
  value: String,
  subtitle: String? = null,
  highlight: Color
) {
  Surface(
    modifier = modifier,
    color = SlateDark,
    shape = RoundedCornerShape(10.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, SlateCardBorder)
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextMuted,
        maxLines = 1
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        color = highlight,
        maxLines = 1
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = TextMuted,
          fontSize = 10.sp,
          maxLines = 1
        )
      }
    }
  }
}

@Composable
private fun MainThreadFocusCard(metrics: CpuStressMetrics) {
  val isRunning = metrics.mainThreadState == Thread.State.RUNNABLE
  val isBlocked = metrics.mainThreadState == Thread.State.BLOCKED

  val stateColor = when {
    isBlocked -> CoralDanger
    isRunning -> MintSecondary
    else -> CyanGlow
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = SlateCard),
    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SlateCardBorder))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = null,
            tint = CyanGlow,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "HILO PRINCIPAL (MAIN / UI THREAD)",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = CyanGlow
          )
        }

        Surface(
          color = stateColor.copy(alpha = 0.15f),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, stateColor.copy(alpha = 0.4f))
        ) {
          Text(
            text = metrics.mainThreadState.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = stateColor
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = "Actividad actual del hilo de interfaz:",
        style = MaterialTheme.typography.bodySmall,
        color = TextSecondary
      )
      Spacer(modifier = Modifier.height(4.dp))
      Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SlateDark,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, SlateCardBorder)
      ) {
        Text(
          text = metrics.mainThreadStackSummary.ifBlank { "En espera de eventos táctiles" },
          modifier = Modifier.padding(10.dp),
          style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
          color = TextPrimary,
          maxLines = 3,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
private fun ThreadRowCard(threadItem: ThreadInfoItem) {
  var isExpanded by remember { mutableStateOf(false) }

  val stateColor = when (threadItem.state) {
    Thread.State.RUNNABLE -> MintSecondary
    Thread.State.BLOCKED -> CoralDanger
    Thread.State.TIMED_WAITING -> CyanGlow
    Thread.State.WAITING -> TextMuted
    Thread.State.NEW -> AmberAccent
    Thread.State.TERMINATED -> TextMuted
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = SlateCard),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(
        if (threadItem.isMainThread) CyanGlow.copy(alpha = 0.5f) else SlateCardBorder
      )
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { isExpanded = !isExpanded }
        .padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = threadItem.name,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = if (threadItem.isMainThread) CyanGlow else TextPrimary,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "prio ${threadItem.priority}",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted,
              fontSize = 10.sp
            )
          }

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = threadItem.category,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            color = stateColor.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, stateColor.copy(alpha = 0.4f))
          ) {
            Text(
              text = threadItem.state.name,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = stateColor,
              fontSize = 11.sp
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Contraer" else "Expandir",
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Top Stack Frame (Línea actual de código en ejecución)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Code,
          contentDescription = null,
          tint = TextMuted,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = threadItem.topStackFrame,
          style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
          color = TextSecondary,
          fontSize = 11.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      // Detalle desplegable con el Stack Trace completo
      AnimatedVisibility(visible = isExpanded) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
        ) {
          Text(
            text = "Pila de llamadas completa (Stack Trace):",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = TextMuted
          )
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SlateDark,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SlateCardBorder)
          ) {
            Column(modifier = Modifier.padding(8.dp)) {
              if (threadItem.fullStackTrace.isEmpty()) {
                Text(
                  text = "Sin marcos de pila disponibles.",
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted
                )
              } else {
                threadItem.fullStackTrace.forEach { frame ->
                  Text(
                    text = frame,
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = if (frame.contains("com.example")) CyanGlow else TextMuted,
                    fontSize = 11.sp
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
