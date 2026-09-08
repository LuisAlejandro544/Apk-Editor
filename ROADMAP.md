# Roadmap del Proyecto APK Extractor

Este documento describe las fases y el estado actual de las características implementadas, destacando la incorporación de las suites de depuración móvil, el monitor de estrés de CPU e hilos y la caché persistente de navegación.

---

## Fase 1: Suite de Depuración Móvil en Dispositivo (Zero-PC Debugging) (Completada)
- [x] **Integración de Pluto SDK**:
  - Captura y diagnóstico de excepciones y caídas (`PlutoExceptionsPlugin`).
  - Visor y depurador de logs en pantalla táctil (`PlutoLoggerPlugin`).
  - Visor de `SharedPreferences` para inspeccionar y editar configuraciones locales (`PlutoSharePreferencesPlugin`).
  - Acceso directo y apertura modal de la suite mediante `Pluto.open()`.
- [x] **Integración de LeakCanary**:
  - Detección automática de memory leaks en segundo plano durante el ciclo de vida de actividades y vistas.
  - Generación de trazas de referencias retenidas y notificaciones en pantalla en el teléfono sin PC.
- [x] **Integración de DoraemonKit (DoKit)**:
  - Asistente flotante para análisis de rendimiento, métricas de hardware y auditoría visual de UI.
  - Resolución y compatibilidad de dependencias de red en Gradle (`volley:1.2.1` y repositorio JitPack).
- [x] **Inicialización y Configuración de Sistema**:
  - Creación de `ApkExtractorApplication.kt` con permiso `SYSTEM_ALERT_WINDOW` para ventanas flotantes de depuración.

---

## Fase 2: Monitor de Hilos y Estrés de Procesador (Completada)
- [x] **Servicio de Muestreo de CPU (`ThreadCpuProfilerService.kt`)**:
  - Muestreo periódico de consumo de CPU del proceso con normalización por núcleos lógicos.
  - Detección de frecuencias de reloj y clasificación dinámica de niveles de estrés (`LOW`, `MODERATE`, `HIGH`, `CRITICAL`).
  - Cálculo en tiempo real de memoria heap utilizada vs. memoria máxima disponible.
- [x] **Auditoría del Hilo Principal (UI Thread)**:
  - Supervisión en tiempo real del estado de ejecución del hilo principal de Android.
  - Extracción y formateo del Stack Trace del hilo principal para detectar bloqueos y ralentizaciones en la interfaz táctil.
- [x] **Monitor de Hilos Secundarios y Concurrencia**:
  - Listado dinámico de todos los hilos del proceso con ID, nombre, prioridad y estado.
  - Filtros táctiles por estado: *Todos*, *Solo Activos*, *Hilo Principal*, *Bloqueados*.
  - Vista expandible con Stack Trace completo de cualquier hilo secundario.
- [x] **Pantalla Interactiva (`ThreadCpuProfilerScreen.kt`)**:
  - Interfaz gráfica optimizada en Compose con tarjetas de resumen, gráficos de progreso y botón de actualización manual.
  - Accesos directos integrados en la barra superior de `HomeScreen` y dentro de `SettingsScreen`.

---

## Fase 3: Caché Persistente y Navegación Fluida del Explorador (Completada)
- [x] **Caché en Memoria de Carpetas y Proyectos (`ApkViewModel.kt`)**:
  - Implementación de `folderCache` y `projectCache` mediante `ConcurrentHashMap`.
  - Supresión de recargas y escaneos redundantes de disco al salir de archivos o navegar hacia atrás.
  - Carga instantánea de subcarpetas previamente leídas.
- [x] **Invalidación Inteligente y Control Manual**:
  - Vaciado selectivo de caché tras guardar modificaciones de archivos, eliminar proyectos o purgar memoria.
  - Incorporación de botón de recarga manual (`Refresh`) en la barra superior de `FileExplorerScreen`.

---

## Próximas Fases Planificadas
- [ ] **Consola Lua Embebida**: Editor en pantalla para ejecutar scripts `.lua` sobre el árbol de archivos extraído.
- [ ] **API de Modding en Lua**: Funciones nativas para buscar y reemplazar cadenas en archivos DEX y ARSC.
- [ ] **Auditoría Extendida de Firmas**: Detección de esquemas de firma v1, v2, v3 y v4.
