package com.example.data

import android.os.Looper
import android.os.Process
import android.os.SystemClock
import com.example.model.CpuStressLevel
import com.example.model.CpuStressMetrics
import com.example.model.ThreadInfoItem

/**
 * Servicio de diagnóstico y telemetría interna para auditar el uso de CPU,
 * nivel de estrés del procesador y actividad detallada de los hilos de ejecución
 * (Hilo Principal vs Hilos Secundarios).
 */
class ThreadCpuProfilerService {

  private var lastCpuTimeMs: Long = Process.getElapsedCpuTime()
  private var lastSampleTimeMs: Long = SystemClock.elapsedRealtime()
  private val numCores: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

  /**
   * Captura una instantánea en tiempo real de las métricas de CPU y memoria del proceso.
   */
  fun sampleCpuAndMemory(): CpuStressMetrics {
    val currentCpuTimeMs = Process.getElapsedCpuTime()
    val currentSampleTimeMs = SystemClock.elapsedRealtime()

    val deltaCpu = (currentCpuTimeMs - lastCpuTimeMs).coerceAtLeast(0L)
    val deltaWall = (currentSampleTimeMs - lastSampleTimeMs).coerceAtLeast(1L)

    lastCpuTimeMs = currentCpuTimeMs
    lastSampleTimeMs = currentSampleTimeMs

    // Porcentaje de CPU utilizado por el proceso respecto al total disponible
    val cpuPercent = ((deltaCpu.toDouble() / (deltaWall * numCores).toDouble()) * 100.0)
      .coerceIn(0.0, 100.0)
      .toFloat()

    val stressLevel = when {
      cpuPercent < 25f -> CpuStressLevel.LOW
      cpuPercent < 55f -> CpuStressLevel.MODERATE
      cpuPercent < 80f -> CpuStressLevel.HIGH
      else -> CpuStressLevel.CRITICAL
    }

    val runtime = Runtime.getRuntime()
    val totalMemMb = runtime.totalMemory() / (1024 * 1024)
    val freeMemMb = runtime.freeMemory() / (1024 * 1024)
    val usedMemMb = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
    val maxMemMb = runtime.maxMemory() / (1024 * 1024)

    val allStackTraces = Thread.getAllStackTraces()
    val activeCount = allStackTraces.size
    val runningCount = allStackTraces.keys.count { it.state == Thread.State.RUNNABLE }

    val mainThread = Looper.getMainLooper().thread
    val mainStack = allStackTraces[mainThread]
    val mainSummary = if (!mainStack.isNullOrEmpty()) {
      val frame = mainStack.firstOrNull { f ->
        !f.className.startsWith("android.os.MessageQueue") &&
            !f.className.startsWith("android.os.Looper")
      } ?: mainStack.first()
      "${frame.className.substringAfterLast('.')}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})"
    } else {
      "En espera de eventos de interfaz táctil"
    }

    return CpuStressMetrics(
      availableProcessors = numCores,
      processCpuUsagePercent = cpuPercent,
      stressLevel = stressLevel,
      totalMemoryMb = totalMemMb,
      freeMemoryMb = freeMemMb,
      usedMemoryMb = usedMemMb,
      maxMemoryMb = maxMemMb,
      activeThreadsCount = activeCount,
      runningThreadsCount = runningCount,
      mainThreadState = mainThread.state,
      mainThreadStackSummary = mainSummary
    )
  }

  /**
   * Obtiene la lista completa de hilos de ejecución activos clasificados por rol y prioridad.
   */
  fun getActiveThreads(): List<ThreadInfoItem> {
    val mainThreadId = Looper.getMainLooper().thread.id
    val traces = Thread.getAllStackTraces()

    return traces.map { (thread, stack) ->
      val isMain = thread.id == mainThreadId || thread.name.equals("main", ignoreCase = true)
      val name = thread.name
      val state = thread.state

      val category = when {
        isMain -> "Hilo Principal (UI)"
        name.contains("ComputeDispatcher", ignoreCase = true) -> "Cálculo / Corrutinas"
        name.contains("FastIODispatcher", ignoreCase = true) || name.contains("IO", ignoreCase = false) -> "Entrada / Salida (I/O)"
        name.contains("DefaultDispatcher", ignoreCase = true) -> "Cálculo / Corrutinas"
        name.contains("jadx", ignoreCase = true) -> "Motor JADX Decompiler"
        name.contains("OkHttp", ignoreCase = true) -> "Red / Conectividad"
        name.contains("ExoPlayer", ignoreCase = true) || name.contains("MediaCodec", ignoreCase = true) -> "Multimedia / Audio"
        name.contains("RenderThread", ignoreCase = true) || name.contains("hwui", ignoreCase = true) -> "Renderizado GPU"
        name.contains("LeakCanary", ignoreCase = true) -> "Auditoría LeakCanary"
        name.contains("pluto", ignoreCase = true) -> "Inspector Pluto"
        name.contains("dokit", ignoreCase = true) -> "Monitor DoraemonKit"
        else -> "Fondo / Sistema"
      }

      val topFrame = if (stack.isNotEmpty()) {
        val meaningful = stack.firstOrNull { f ->
          !f.className.startsWith("java.lang.Object") &&
              !f.className.startsWith("android.os.MessageQueue") &&
              !f.className.startsWith("android.os.Looper") &&
              !f.className.startsWith("sun.misc.Unsafe")
        } ?: stack.first()
        "${meaningful.className.substringAfterLast('.')}.${meaningful.methodName}:${meaningful.lineNumber}"
      } else {
        "Sin actividad en pila (Inactivo)"
      }

      val fullTraceList = stack.map { f ->
        "  en ${f.className}.${f.methodName}(${f.fileName ?: "Desconocido"}:${f.lineNumber})"
      }

      ThreadInfoItem(
        id = thread.id,
        name = thread.name,
        isMainThread = isMain,
        state = state,
        priority = thread.priority,
        topStackFrame = topFrame,
        fullStackTrace = fullTraceList,
        category = category,
        isCpuActive = (state == Thread.State.RUNNABLE)
      )
    }.sortedWith(
      compareByDescending<ThreadInfoItem> { it.isMainThread }
        .thenByDescending { it.isCpuActive }
        .thenBy { it.category }
        .thenBy { it.name }
    )
  }
}
