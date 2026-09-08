# Estructura del Proyecto (Structure)

Mapa detallado de la arquitectura de directorios, capas de responsabilidad y componentes tecnológicos del proyecto.

```
apk-extractor/
├── app/
│   ├── build.gradle.kts                 # Configuración de compilación Android, NDK y dependencias (ARSCLib, smali, Media3, Coil, apk-parser, Tika, Zstd, LZ4, Brotli, Commons-Compress, Protobuf, MsgPack, CBOR, SQLite-JDBC)
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
│       │   │   └── rust_core/           # Crate de Rust y C ABI para análisis seguro de memoria y ELF
│       │   │       ├── Cargo.toml       # Definición del paquete Rust (cdylib/staticlib)
│       │   │       ├── src/lib.rs       # Rutinas Rust exportadas con #[no_mangle]
│       │   │       └── rust_core_abi.c  # Capa C ABI que implementa Goblin & Capstone para ELF y ASM
│       │   ├── java/com/example/
│       │   │   ├── MainActivity.kt      # Actividad principal con configuración de rutas Compose y Edge-to-Edge
│       │   │   ├── data/
│       │   │   │   ├── ApkExtractorRepository.kt  # Gestión I/O, streaming ZIP, decodificación AXML, DEX, ELF, ARSC y guardado binario
│       │   │   │   ├── AppDispatchers.kt          # Despachadores de corrutinas optimizados (ComputeDispatcher y FastIODispatcher)
│       │   │   │   ├── ArscParser.kt              # Parser de resources.arsc mediante io.github.reandroid:ARSCLib
│       │   │   │   ├── BinaryDataParser.kt        # Analizador binario (.dat/.bin), cálculo de Entropía de Shannon, formateo Hex y extractor de cadenas
│       │   │   │   ├── DexDisassembler.kt         # Desensamblador Smali con baksmali 2.5.2 e indexación de clases DEX
│       │   │   │   ├── DexToJavaTranslator.kt     # Traductor y reconstructor de bytecode Dalvik a código Java
│       │   │   │   └── NativeElfDisassembler.kt   # Parseo ELF con Goblin y desensamblado ASM con Capstone
│       │   │   ├── model/
│       │   │   │   ├── ApkModel.kt                # Modelos de datos: FileCategory (ARSC, DEX, ELF, AUDIO, IMAGE, BINARY_DATA), ApkProject, ExtractedFileItem, StorageInfo
│       │   │   │   └── InstalledAppItem.kt        # Modelo y metadatos de apps instaladas leídas del dispositivo
│       │   │   ├── nativebridge/
│       │   │   │   └── NativeEngineBridge.kt      # Interfaz JNI que interactúa con libapk_native_engine.so
│       │   │   ├── ui/
│       │   │   │   ├── components/
│       │   │   │   │   ├── ArscViewerContent.kt       # Visor táctil interactivo de recursos ARSC con filtros y buscador
│       │   │   │   │   ├── AudioPlayerView.kt         # Reproductor de audio nativo con AndroidX Media3 ExoPlayer
│       │   │   │   │   ├── BinaryDataViewerContent.kt # Visor y editor táctil de archivos .dat/.bin (Hex, Texto UTF-8, Inspector de Entropía y Strings)
│       │   │   │   │   └── ImageViewerView.kt         # Visor de imágenes con Coil Compose y zoom multitáctil
│       │   │   │   ├── navigation/
│       │   │   │   │   └── Screen.kt              # Definición de rutas y destinos de navegación Compose
│       │   │   │   ├── screens/
│       │   │   │   │   ├── HomeScreen.kt          # Panel de inicio, selección SAF y estado del sistema
│       │   │   │   │   ├── InstalledAppsScreen.kt # Explorador y extractor de apps instaladas del sistema/usuario
│       │   │   │   │   ├── ExtractionScreen.kt    # Vista en tiempo real del progreso de descompresión streaming
│       │   │   │   │   ├── FileExplorerScreen.kt  # Navegador de directorios internos del APK con categorización
│       │   │   │   │   ├── FileDetailScreen.kt    # Visor Hex Dump, Smali/Java DEX, ELF (.so), ARSC, multimedia y editor
│       │   │   │   │   └── CacheManagerScreen.kt  # Monitor de memoria y purga de caché temporal
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt               # Paleta ciberpunk/terminal (Slate, Cyan, Mint, Amber)
│       │   │   │       ├── Theme.kt               # Tema Material 3 oscuro
│       │   │   │       └── Type.kt                # Tipografía con soporte monoespaciado
│       │   │   └── viewmodel/
│       │   │       └── ApkViewModel.kt            # StateFlows reactivos para UI, DEX, ARSC, ELF, AXML y guardado en disco
│       │   └── res/                               # Recursos gráficos, iconos adaptativos y strings
│       └── test/java/com/example/                 # Pruebas unitarias locales (ArscViewerUnitTest, DexViewerUnitTest, etc.)
├── .gitignore                           # Exclusiones de Git para Android, C/C++, CMake, Rust y Lua
├── gradle/                              # Wrapper y catálogo de dependencias (libs.versions.toml)
├── build.gradle.kts                     # Configuración del proyecto raíz
├── settings.gradle.kts                  # Configuración de repositorios y módulos
├── README.md                            # Documentación principal actualizada
├── ROADMAP.md                           # Fases de desarrollo y estado de hitos
├── STRUCTURE.md                         # Mapa arquitectónico del sistema
├── AI_CONTEXT.md                        # Contexto técnico para modelos de lenguaje
├── commit_message.txt                   # Registro en español del último conjunto de cambios
└── AGENTS.md                            # Reglas operativas y directrices de desarrollo
```
