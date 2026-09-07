package com.example.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Gestor dinámico de hilos secundarios y despacho concurrente para la aplicación.
 * Asigna la máxima potencia de procesamiento (núcleos dinámicos del CPU del teléfono)
 * a las tareas intensivas como el escaneo de aplicaciones y desensamblado de código,
 * evitando cualquier bloqueo o lag en el hilo principal (Main Thread).
 */
object AppDispatchers {

  /**
   * Cantidad dinámica de núcleos de procesamiento disponibles en el dispositivo móvil.
   */
  val availableCores: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)

  /**
   * Pool de hilos secundarios de ALTO RENDIMIENTO (High Priority / Heavy Compute).
   * Configurado con prioridad Thread.NORM_PRIORITY + 2 (o MAX_PRIORITY - 1)
   * y dimensionado dinámicamente según la cantidad de núcleos del procesador del móvil
   * para que el escaneo de apps, descompresión y desensamblado operen a máxima velocidad.
   */
  val ScannerDispatcher: CoroutineDispatcher = run {
    val threadCounter = AtomicInteger(1)
    val poolSize = (availableCores * 2).coerceIn(4, 16)
    val executor = Executors.newFixedThreadPool(poolSize, object : ThreadFactory {
      override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, "APK-ScannerPool-${threadCounter.getAndIncrement()}")
        // Asignar prioridad elevada para procesamiento rápido
        thread.priority = (Thread.NORM_PRIORITY + 2).coerceAtMost(Thread.MAX_PRIORITY)
        thread.isDaemon = true
        return thread
      }
    })
    executor.asCoroutineDispatcher()
  }

  /**
   * Pool de hilos secundarios de I/O RÁPIDO para carga inmediata de archivos,
   * cálculo de metadatos, lecturas de AXML y generación de Hex Dump.
   */
  val FastIODispatcher: CoroutineDispatcher = run {
    val threadCounter = AtomicInteger(1)
    val poolSize = (availableCores * 2).coerceIn(4, 32)
    val executor = Executors.newFixedThreadPool(poolSize, object : ThreadFactory {
      override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, "APK-FastIO-${threadCounter.getAndIncrement()}")
        thread.priority = Thread.NORM_PRIORITY
        thread.isDaemon = true
        return thread
      }
    })
    executor.asCoroutineDispatcher()
  }

  /**
   * Hilo para operaciones de cómputo estándar o CPU bound (DEX parsing, hashing).
   */
  val ComputeDispatcher: CoroutineDispatcher = Dispatchers.Default

  /**
   * Hilo para I/O general del sistema.
   */
  val IODispatcher: CoroutineDispatcher = Dispatchers.IO
}
