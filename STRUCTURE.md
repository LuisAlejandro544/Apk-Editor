# Estructura del Proyecto (Structure)

Mapa detallado de la arquitectura de directorios, capas de responsabilidad y componentes tecnológicos del proyecto.

```
apk-extractor/
├── app/
│   ├── build.gradle.kts                 # Configuración de compilación Android, NDK y dependencias (smali, apk-parser, etc.)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml      # Permisos del sistema (SAF, QUERY_ALL_PACKAGES) y Activities
│       │   ├── cpp/                     # Módulos de código nativo (C, C++, Rust y Lua)
│       │   │   ├── CMakeLists.txt       # Script de compilación CMake (C11 + C++17 + Lua C)
│       │   │   ├── native_bridge.c      # Conector JNI en C que enlaza el runtime de Android con Lua y Rust
│       │   │   ├── cpp_engine.cpp       # Motor C++ para parsing binario y análisis estructural
│       │   │   ├── lua/                 # Código fuente oficial en C puro de Lua 5.4.7
│       │   │   │   ├── lua.h / lauxlib.h / lualib.h
│       │   │   │   ├── lapi.c, lvm.c, ldo.c, lgc.c...
│       │   │   │   └── (todos los módulos C del intérprete)
│       │   │   └── rust_core/           # Crate de Rust para análisis seguro de memoria
│       │   │       ├── Cargo.toml       # Definición del paquete Rust (cdylib/staticlib)
│       │   │       └── src/lib.rs       # Rutinas Rust exportadas con #[no_mangle]
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt      # Actividad principal con configuración de rutas Compose
│       │   │   ├── data/
│       │   │   │   ├── ApkExtractorRepository.kt  # Gestión I/O, streaming ZIP, decodificación AXML y métodos DEX
│       │   │   │   ├── DexDisassembler.kt         # Desensamblador Smali con baksmali 2.5.2 e indexación de clases DEX
│       │   │   │   └── DexToJavaTranslator.kt     # Traductor y reconstructor de bytecode Dalvik a código Java
│       │   │   ├── model/
│       │   │   │   ├── ApkFileItem.kt        # Entidad de archivo extraído (tamaño, tipo, extensiones)
│       │   │   │   ├── ExtractedProject.kt   # Metadata de sesiones y proyectos guardados en caché
│       │   │   │   ├── ExtractionProgress.kt # Estado reactivo del progreso de extracción
│       │   │   │   └── InstalledAppItem.kt   # Información de apps instaladas leídas del dispositivo
│       │   │   ├── nativebridge/
│       │   │   │   └── NativeEngineBridge.kt # Interfaz JNI que interactúa con la librería libapk_native_engine.so
│       │   │   ├── ui/
│       │   │   │   ├── navigation/
│       │   │   │   │   └── Screen.kt         # Definición de pantallas y parámetros de navegación
│       │   │   │   ├── screens/
│       │   │   │   │   ├── HomeScreen.kt          # Panel de inicio, selección SAF y estado del sistema
│       │   │   │   │   ├── InstalledAppsScreen.kt # Explorador y extractor de apps instaladas
│       │   │   │   │   ├── ExtractionScreen.kt    # Vista en tiempo real del progreso de descompresión
│       │   │   │   │   ├── FileExplorerScreen.kt  # Navegador de directorios internos del APK
│       │   │   │   │   ├── FileDetailScreen.kt    # Visor Hex Dump, desensamblador Smali/Java para DEX y editor con persistencia
│       │   │   │   │   └── CacheManagerScreen.kt  # Monitor de memoria y purga de caché temporal
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt               # Paleta ciberpunk/terminal (Slate, Cyan, Mint, Amber)
│       │   │   │       ├── Theme.kt               # Tema Material 3 oscuro
│       │   │   │       └── Type.kt                # Tipografía con soporte monoespaciado
│       │   │   └── viewmodel/
│       │   │       └── ApkViewModel.kt            # StateFlows reactivos para UI, DEX, AXML y guardado en disco
│       │   └── res/                               # Recursos gráficos, iconos adaptativos y strings
│       └── test/java/com/example/                 # Pruebas unitarias locales (DexViewerUnitTest, ExampleUnitTest)
├── .gitignore                           # Exclusiones de Git para Android, C/C++, CMake, Rust y Lua
├── gradle/                              # Wrapper y catálogo de dependencias
├── build.gradle.kts                     # Configuración del proyecto raíz
├── settings.gradle.kts                  # Configuración de repositorios y módulos
├── README.md                            # Documentación principal actualizada
├── ROADMAP.md                           # Fases de desarrollo planificadas
├── STRUCTURE.md                         # Mapa arquitectónico del sistema
├── AI_CONTEXT.md                        # Contexto técnico para modelos de lenguaje
├── commit_message.txt                   # Registro en español del último conjunto de cambios
└── AGENTS.md                            # Reglas operativas y directrices de desarrollo
```
