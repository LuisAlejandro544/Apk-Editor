package com.example.model

/**
 * Representa la información detallada de un hilo de ejecución en el proceso.
 */
data class ThreadInfoItem(
  val id: Long,
  val name: String,
  val isMainThread: Boolean,
  val state: Thread.State,
  val priority: Int,
  val topStackFrame: String,
  val fullStackTrace: List<String>,
  val category: String,
  val isCpuActive: Boolean
)

/**
 * Nivel de estrés y saturación de la CPU.
 */
enum class CpuStressLevel(val label: String) {
  LOW("Bajo (Fluido)"),
  MODERATE("Moderado"),
  HIGH("Alto (Carga pesada)"),
  CRITICAL("Crítico (Riesgo de ANR)")
}

/**
 * Métricas consolidadas de consumo de CPU, estrés y memoria del proceso.
 */
data class CpuStressMetrics(
  val availableProcessors: Int = Runtime.getRuntime().availableProcessors(),
  val processCpuUsagePercent: Float = 0f,
  val stressLevel: CpuStressLevel = CpuStressLevel.LOW,
  val totalMemoryMb: Long = 0L,
  val freeMemoryMb: Long = 0L,
  val usedMemoryMb: Long = 0L,
  val maxMemoryMb: Long = 0L,
  val activeThreadsCount: Int = 0,
  val runningThreadsCount: Int = 0,
  val mainThreadState: Thread.State = Thread.State.RUNNABLE,
  val mainThreadStackSummary: String = ""
)
