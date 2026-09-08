# APK Extractor Mobile - Suite de Depuración y Monitor de Rendimiento

Entorno móvil nativo desarrollado en Android (Kotlin + Jetpack Compose) equipado con una suite integral de depuración en dispositivo (Zero-PC Debugging) con **Pluto**, **LeakCanary** y **DoraemonKit (DoKit)**, junto con un **Monitor de Hilos y Estrés de Procesador (CPU Profiler)** y un sistema de **Caché Persistente de Carpetas** para una navegación instantánea sin recargas de disco.

---

## Nuevas Capacidades y Módulos Integrados

### 1. Suite de Depuración en Dispositivo (Zero-PC Debugging)
Permite auditar, inspeccionar y diagnosticar la aplicación íntegramente desde la pantalla táctil del teléfono, sin cables USB, sin PC ni conexión a Android Studio:
- **Pluto SDK (`com.pluto:pluto`)**:
  - Panel superpuesto interactivo invocable desde la configuración o desde el monitor de rendimiento.
  - Inspector de preferencias compartidas (`SharedPreferences`), captura y diagnóstico de excepciones/crashes (`PlutoExceptionsPlugin`), visor de logs del sistema (`PlutoLoggerPlugin`) y auditoría en tiempo real.
- **LeakCanary (`com.squareup.leakcanary:leakcanary-android`)**:
  - Detección automática en segundo plano de fugas de memoria RAM (memory leaks), referencias retenidas en ciclo de vida y notificación inmediata con árbol de referencias en pantalla.
- **DoraemonKit / DoKit (`com.didichuxing.doraemonkit:dokitx`)**:
  - Herramienta flotante multifunción para diagnóstico de rendimiento móvil, control visual de jerarquías de interfaz, comprobación de FPS y métricas de dispositivo.
- **Inicialización Centralizada**:
  - Configurada en `ApkExtractorApplication.kt` con permiso de ventana del sistema (`SYSTEM_ALERT_WINDOW`) para overlays no intrusivos.

### 2. Monitor de Hilos y Estrés de Procesador (`ThreadCpuProfilerService.kt` & `ThreadCpuProfilerScreen.kt`)
Herramienta de análisis técnico para observar en tiempo real la carga de trabajo del procesador y el comportamiento de la concurrencia:
- **Medición de Estrés de CPU en Vivo**:
  - Cálculo del porcentaje de uso de CPU del proceso mediante diferenciales de tiempo de proceso y reloj del sistema.
  - Clasificación del nivel de estrés del procesador: **Bajo**, **Moderado**, **Alto** y **Crítico**, acompañado de indicadores cromáticos de alerta.
  - Reporte de especificaciones de hardware: recuento de núcleos lógicos disponibles y frecuencia de reloj del sistema.
  - Memoria RAM heap consumida vs. cuota máxima asignada a la aplicación por la máquina virtual de Android.
- **Auditoría Detallada del Hilo Principal (UI Thread)**:
  - Supervisión constante del hilo que dibuja la interfaz y gestiona los eventos táctiles.
  - Detección de estados de bloqueo (`BLOCKED`) o sobrecarga (`RUNNABLE` prolongado).
  - Resumen y trazado de pila de llamadas (Stack Trace) para identificar métodos causantes de congelamiento visual o caídas de cuadros.
- **Inspección de Hilos Secundarios y Tareas en Segundo Plano**:
  - Listado completo de hilos del proceso con ID, nombre, prioridad y estado exacto de ejecución (`RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `NEW`, `TERMINATED`).
  - Filtrado reactivo mediante chips táctiles: *Todos*, *Solo Activos*, *Hilo Principal*, *Bloqueados*.
  - Vista expandible con el Stack Trace completo de cualquier hilo secundario para analizar operaciones de I/O, descompresión o cómputo.
- **Acceso Rápido**:
  - Botón directo en la barra superior de `HomeScreen` (icono de velocímetro) y acceso dedicado dentro de `SettingsScreen`.

### 3. Caché Persistente y Navegación Fluida del Explorador de Archivos (`ApkViewModel.kt`)
Optimización de la experiencia de usuario táctil para eliminar bloqueos y tiempos de espera al navegar directorios:
- **Caché en Memoria (`ConcurrentHashMap`)**:
  - Almacenamiento indexado de proyectos (`projectCache`) y de estructuras de carpetas ya leídas (`folderCache`) utilizando claves compuestas `"$projectId::$subPath"`.
- **Cero Tiempos de Recarga al Regresar**:
  - Al salir de un archivo inspeccionado o subir niveles de carpetas en el árbol del APK, la aplicación muestra el contenido de forma inmediata desde memoria en lugar de reiniciar o volver a escanear el almacenamiento físico.
- **Invalidación Segura y Recarga Manual**:
  - La caché se invalida selectivamente cuando el usuario modifica o guarda un archivo, cuando se elimina un proyecto o se purga la memoria en el gestor de caché.
  - Se incorporó un botón de refresco forzado (`Refresh`) en la barra superior de `FileExplorerScreen` para sincronizar con disco únicamente cuando el usuario lo solicite explícitamente.

---

## Arquitectura de Código

```
app/src/main/java/com/example/
├── ApkExtractorApplication.kt         # Inicialización de Pluto, DoKit y ciclo de depuración
├── data/
│   ├── ThreadCpuProfilerService.kt    # Muestreo de CPU, cálculo de estrés y mapeo de hilos
│   └── ApkExtractorRepository.kt      # Operaciones de I/O y lectura de proyectos
├── model/
│   ├── ThreadCpuProfilerModel.kt      # Modelos de datos de hilos, métricas y niveles de estrés
│   └── ApkModel.kt                    # Modelos de proyecto y archivos
├── ui/
│   ├── navigation/
│   │   └── Screen.kt                  # Ruta registrada: Screen.ThreadCpuProfiler
│   └── screens/
│       ├── ThreadCpuProfilerScreen.kt # Pantalla interactiva del monitor de hilos y CPU
│       ├── FileExplorerScreen.kt      # Explorador con botón de recarga forzada y caché activa
│       ├── HomeScreen.kt              # Acceso directo al monitor de rendimiento
│       └── SettingsScreen.kt          # Acceso a suites de depuración (Pluto / DoKit)
└── viewmodel/
    └── ApkViewModel.kt                # folderCache, projectCache y StateFlows del profiler
```

---

## Compilación y Verificación

```bash
# Compilar APK de depuración
gradle :app:assembleDebug

# Ejecutar pruebas unitarias
gradle :app:testDebugUnitTest
```
