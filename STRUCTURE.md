# Estructura del Proyecto (Structure)

Mapa detallado de la arquitectura de directorios, capas de responsabilidad y componentes tecnológicos del proyecto.

```
apk-extractor/
├── app/
│   ├── build.gradle.kts                 # Configuración de Gradle, NDK y dependencias (Pluto, LeakCanary, DoraemonKit, Volley, JADX Core, BouncyCastle, ARSCLib, smali, Media3, Coil)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml      # Declaración de Application (ApkExtractorApplication), permisos (SYSTEM_ALERT_WINDOW, QUERY_ALL_PACKAGES) y Activities
│       │   ├── cpp/                     # Módulos de código nativo (C, C++, Rust y Lua)
│       │   │   ├── CMakeLists.txt       # Script de compilación CMake (C11 + C++17 + Lua C)
│       │   │   ├── native_bridge.c      # Conector JNI en C que enlaza el runtime de Android con Lua y Rust
│       │   │   ├── cpp_engine.cpp       # Motor C++ para parsing binario y análisis estructural
│       │   │   ├── lua/                 # Intérprete oficial en C puro de Lua 5.4.7
│       │   │   └── rust_core/           # Crate de Rust y C ABI para análisis ELF y ASM
│       │   ├── java/com/example/
│       │   │   ├── ApkExtractorApplication.kt # Inicialización de suites de depuración móvil (Pluto, DoKit)
│       │   │   ├── MainActivity.kt      # Actividad principal con registro de rutas Compose (incluyendo ThreadCpuProfiler)
│       │   │   ├── data/
│       │   │   │   ├── ThreadCpuProfilerService.kt # Medición de uso de CPU, frecuencia, memoria heap y mapeo de hilos en tiempo real
│       │   │   │   ├── ApkExtractorRepository.kt  # Gestión I/O, streaming ZIP, decodificación AXML, DEX, ELF, ARSC y guardado binario
│       │   │   │   ├── AppDispatchers.kt          # Despachadores de corrutinas optimizados (ComputeDispatcher y FastIODispatcher)
│       │   │   │   ├── ArscParser.kt              # Parser de resources.arsc mediante ARSCLib
│       │   │   │   ├── BinaryDataParser.kt        # Analizador binario, cálculo de Entropía y extractor de cadenas
│       │   │   │   ├── BouncyCastleCryptoService.kt # Análisis de certificados X.509 y firmas APK en META-INF/
│       │   │   │   ├── DecompressionService.kt    # Motor unificado de descompresión streaming (Zstandard, LZ4, Brotli, Bzip2, XZ, Deflate)
│       │   │   │   ├── DexDisassembler.kt         # Desensamblador Smali (baksmali 2.5.2)
│       │   │   │   ├── JadxDecompilerService.kt   # Descompilación Java estructural mediante jadx-core
│       │   │   │   ├── NativeElfDisassembler.kt   # Parseo ELF y desensamblado ASM con Capstone
│       │   │   │   └── ProguardRetraceService.kt  # Desofuscador con ProGuard ReTrace (mapping.txt)
│       │   │   ├── model/
│       │   │   │   ├── ThreadCpuProfilerModel.kt  # Modelos de métricas de CPU, niveles de estrés y estado de hilos
│       │   │   │   ├── ApkModel.kt                # Modelos de proyecto, categorías y archivos extraídos
│       │   │   │   ├── AppSettings.kt             # Modelo de configuración de tema (Cyber Dark, Material You)
│       │   │   │   └── InstalledAppItem.kt        # Metadatos de aplicaciones instaladas
│       │   │   ├── nativebridge/
│       │   │   │   └── NativeEngineBridge.kt      # Interfaz JNI que interactúa con libapk_native_engine.so
│       │   │   ├── ui/
│       │   │   │   ├── components/
│       │   │   │   │   ├── ArscViewerContent.kt       # Visor interactivo de recursos ARSC
│       │   │   │   │   ├── BinaryDataViewerContent.kt # Editor y visor táctil hexadecimal/texto
│       │   │   │   │   └── MediaPreviewComponents.kt  # Visor multimedia (ExoPlayer y Coil)
│       │   │   │   ├── navigation/
│       │   │   │   │   └── Screen.kt              # Destinos Compose (Home, Explorer, ThreadCpuProfiler, Settings, etc.)
│       │   │   │   ├── screens/
│       │   │   │   │   ├── ThreadCpuProfilerScreen.kt # Interfaz del monitor de hilos y estrés de CPU con acceso a Pluto
│       │   │   │   │   ├── FileExplorerScreen.kt      # Explorador de archivos con caché persistente y botón de recarga forzada
│       │   │   │   │   ├── HomeScreen.kt              # Panel de inicio con acceso rápido al monitor de rendimiento
│       │   │   │   │   ├── SettingsScreen.kt          # Configuración con sección técnica de depuración (Pluto, DoKit, Profiler)
│       │   │   │   │   ├── FileDetailScreen.kt        # Visor detallado (Smali, Java, ELF, ARSC, Hex)
│       │   │   │   │   ├── InstalledAppsScreen.kt     # Lista y extractor de aplicaciones instaladas
│       │   │   │   │   ├── ExtractionScreen.kt        # Progreso de extracción en streaming
│       │   │   │   │   └── CacheManagerScreen.kt      # Gestor de memoria y purga de caché
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt               # Paleta cromática Cyber Dark (Slate, Cyan, Mint, Amber, Coral)
│       │   │   │       ├── Theme.kt               # Soporte para temas Cyber Dark y Material You
│       │   │   │       └── Type.kt                # Tipografía del sistema
│       │   │   └── viewmodel/
│       │   │       └── ApkViewModel.kt            # Gestión de folderCache, projectCache, flujos StateFlow y profiler
│       │   └── res/                               # Recursos gráficos, iconos y strings
│       └── test/java/com/example/                 # Suites de pruebas unitarias
```
