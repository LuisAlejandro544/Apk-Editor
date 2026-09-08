# AI Context (Contexto de Inteligencia Artificial)

Este documento condensa la información fundamental sobre las capacidades de depuración, monitorización de rendimiento del procesador y caché persistente de carpetas integradas en el proyecto.

---

## 1. Misión del Proyecto
Proporcionar un entorno de inspección, depuración e ingeniería inversa autónomo en Android que opere **100% en el dispositivo móvil sin depender de un PC**, ofreciendo herramientas avanzadas de diagnóstico de hilos, análisis de estrés de CPU en tiempo real, detección de fugas de memoria y navegación de archivos sin latencia.

## 2. Herramientas de Depuración en Dispositivo Integradas (Zero-PC Debugging)
- **Pluto SDK (`com.pluto:pluto`)**:
  - Plataforma de depuración on-device integrada en `ApkExtractorApplication.kt`.
  - Módulos activos: captura de excepciones (`PlutoExceptionsPlugin`), visor de logs de depuración (`PlutoLoggerPlugin`) y visor de SharedPreferences (`PlutoSharePreferencesPlugin`).
  - Invocable mediante llamada directa a `Pluto.open()` desde la interfaz de usuario en `SettingsScreen` y `ThreadCpuProfilerScreen`.
- **LeakCanary (`com.squareup.leakcanary:leakcanary-android`)**:
  - Detección automática en segundo plano de fugas de memoria RAM en actividades, fragmentos y vistas.
  - Generación de notificaciones locales y volcados de heap estructurados visibles directamente en la pantalla táctil del móvil.
- **DoraemonKit / DoKit (`com.didichuxing.doraemonkit:dokitx`)**:
  - Asistente móvil multifuncional inicializado mediante `DoKit.Builder(this).build()`.
  - Proporciona herramientas de control de rendimiento, inspección visual de layouts e información del sistema.
  - Resolución de dependencias: se utiliza `com.android.volley:volley:1.2.1` explícito y repositorio JitPack para evitar conflictos con versiones obsoletas.

## 3. Monitor de Hilos y Estrés de Procesador (Thread & CPU Profiler)
- **Servicio de Muestreo (`ThreadCpuProfilerService.kt`)**:
  - Muestreo periódico de métricas de proceso mediante `Process.getElapsedCpuTime()` y `System.nanoTime()`.
  - Cálculo de porcentaje de uso de CPU normalizado entre el número de procesadores lógicos disponibles (`Runtime.getRuntime().availableProcessors()`).
  - Clasificación de nivel de estrés: `LOW` (< 20%), `MODERATE` (20-55%), `HIGH` (55-80%) y `CRITICAL` (> 80%).
  - Inspección exhaustiva de todos los hilos del hilo grupal activo (`Thread.currentThread().threadGroup` / `Thread.getAllStackTraces()`).
- **Supervisión del Hilo Principal (UI Thread)**:
  - Detección de estados `RUNNABLE`, `BLOCKED`, `WAITING` o `TIMED_WAITING` en el hilo principal de Android (`Looper.getMainLooper().thread`).
  - Extracción y formateo de la pila de ejecución (Stack Trace) para diagnosticar bloqueos o cuellos de botella en la renderización táctil.
- **Supervisión de Hilos Secundarios**:
  - Seguimiento de hilos de corrutinas, workers de I/O y tareas de cómputo en segundo plano con filtros interactivos por estado.

## 4. Sistema de Caché Persistente del Explorador de Archivos (`ApkViewModel.kt`)
- **Problema Solucionado**:
  - Anteriormente, al salir de un archivo o retroceder de una carpeta, el explorador reiniciaba el estado y re-escaneaba el disco, provocando lentitud y pérdida del contexto de navegación.
- **Implementación**:
  - `folderCache`: Mapa concurrente (`ConcurrentHashMap<String, List<ExtractedFileItem>>`) con clave `"$projectId::$subPath"`.
  - `projectCache`: Mapa concurrente (`ConcurrentHashMap<String, ApkProject>`) indexado por ID de proyecto.
  - Al solicitar `loadProjectFolder(projectId, subPath, forceReload = false)`, el ViewModel verifica primero si la carpeta reside en `folderCache`. Si existe, se emite inmediatamente a `_currentFolderFiles` sin lecturas de disco.
  - **Invalidación Controlada**: Al guardar un archivo (`saveFileContent`), eliminar un proyecto (`deleteProject`) o purgar la caché (`clearAllCache`), las entradas de la caché se eliminan para mantener la coherencia de datos.
  - **Recarga Manual**: La pantalla `FileExplorerScreen` dispone de un botón de refresco forzado que invoca `loadProjectFolder(..., forceReload = true)`.
