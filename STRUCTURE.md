# Estructura del Proyecto (Structure)

Mapa detallado de la arquitectura de directorios, capas de responsabilidad y componentes tecnológicos del proyecto.

```
apk-extractor/
├── app/
│   ├── build.gradle.kts                 # Configuración de compilación Android, NDK y dependencias (JADX Core, ProGuard ReTrace, BouncyCastle, ARSCLib, smali, Media3, Coil, apk-parser, Tika, Zstd, LZ4, Brotli, Commons-Compress, Protobuf, MsgPack, CBOR, SQLite-JDBC)
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
│       │   │   ├── MainActivity.kt      # Actividad principal con configuración Edge-to-Edge, observador de tema y rutas Compose
│       │   │   ├── data/
│       │   │   │   ├── ApkExtractorRepository.kt  # Gestión I/O, streaming ZIP, decodificación AXML, DEX, ELF, ARSC y guardado binario
│       │   │   │   ├── AppDispatchers.kt          # Despachadores de corrutinas optimizados (ComputeDispatcher y FastIODispatcher)
│       │   │   │   ├── ArscParser.kt              # Parser de resources.arsc mediante io.github.reandroid:ARSCLib
│       │   │   │   ├── BinaryDataParser.kt        # Analizador binario (.dat/.bin), cálculo de Entropía de Shannon, formateo Hex y extractor de cadenas
│       │   │   │   ├── BouncyCastleCryptoService.kt # Suite criptográfica: análisis de certificados X.509, firmas APK (META-INF/*.RSA), huellas SHA-256/SHA-1/MD5 y decodificación ASN.1/PKCS7
│       │   │   │   ├── DecompressionService.kt    # Motor unificado de descompresión streaming (Zstandard, LZ4, Brotli, Bzip2, XZ/LZMA, GZIP, Deflate)
│       │   │   │   ├── DexDisassembler.kt         # Desensamblador Smali (baksmali 2.5.2) e indexador de clases Dalvik
│       │   │   │   ├── DexToJavaTranslator.kt     # Traductor estructural auxiliar de bytecode a pseudocódigo Java
│       │   │   │   ├── JadxDecompilerService.kt   # Servicio de descompilación Java estructural de alto nivel mediante jadx-core
│       │   │   │   ├── NativeElfDisassembler.kt   # Parseo ELF con Goblin y desensamblado ASM con Capstone
│       │   │   │   └── ProguardRetraceService.kt  # Desofuscador de stack traces y símbolos mediante ProGuard ReTrace (mapping.txt)
│       │   │   ├── model/
│       │   │   │   ├── ApkModel.kt                # Modelos de datos: FileCategory (ARSC, DEX, ELF, AUDIO, IMAGE, BINARY_DATA), ApkProject, ExtractedFileItem, StorageInfo
│       │   │   │   ├── AppSettings.kt             # Modelo de configuración: enum AppThemeMode (CYBER_DARK, MATERIAL_YOU)
│       │   │   │   └── InstalledAppItem.kt        # Modelo y metadatos de apps instaladas leídas del dispositivo
│       │   │   ├── nativebridge/
│       │   │   │   └── NativeEngineBridge.kt      # Interfaz JNI que interactúa con libapk_native_engine.so
│       │   │   ├── ui/
│       │   │   │   ├── components/
│       │   │   │   │   ├── ArscViewerContent.kt       # Visor táctil interactivo de recursos ARSC con filtros y buscador
│       │   │   │   │   ├── BinaryDataViewerContent.kt # Visor y editor táctil de archivos .dat/.bin (Hex, Texto UTF-8, Inspector de Entropía y Strings)
│       │   │   │   │   └── MediaPreviewComponents.kt  # Visores multimedia: reproductor de audio nativo ExoPlayer y visor de imágenes Coil con zoom
│       │   │   │   ├── navigation/
│       │   │   │   │   └── Screen.kt              # Definición de rutas y destinos de navegación Compose (Home, InstalledApps, Extraction, Explorer, Detail, Cache, Settings)
│       │   │   │   ├── screens/
│       │   │   │   │   ├── HomeScreen.kt          # Panel de inicio, selección SAF, estado y acceso a Configuración (con statusBarsPadding)
│       │   │   │   │   ├── InstalledAppsScreen.kt # Explorador y extractor de apps instaladas del sistema/usuario (con insets seguros)
│       │   │   │   │   ├── ExtractionScreen.kt    # Vista en tiempo real del progreso de descompresión streaming
│       │   │   │   │   ├── FileExplorerScreen.kt  # Navegador de directorios internos del APK con categorización (con insets seguros)
│       │   │   │   │   ├── FileDetailScreen.kt    # Visor integral: Smali, Java (JADX Core), ELF (.so), ARSC, Certificados BouncyCastle, editor y Hex Dump
│       │   │   │   │   ├── CacheManagerScreen.kt  # Monitor de memoria y purga de caché temporal (con insets seguros)
│       │   │   │   │   └── SettingsScreen.kt      # Pantalla de Configuración con selector de tema (Material You vs Cyber Dark) e información Edge-to-Edge
│       │   │   │   └── theme/
│       │   │   │       ├── Color.kt               # Paleta ciberpunk/terminal (Slate, Cyan, Mint, Amber)
│       │   │   │       ├── Theme.kt               # Tema Material 3 adaptativo con soporte para dynamicDarkColorScheme (Material You)
│       │   │   │       └── Type.kt                # Tipografía con soporte monoespaciado
│       │   │   └── viewmodel/
│       │   │       └── ApkViewModel.kt            # StateFlows reactivos para UI, DEX (Smali/JADX), ARSC, ELF, AXML, BinaryData, Criptografía y persistencia de tema
│       │   └── res/                               # Recursos gráficos, iconos adaptativos y strings
│       └── test/java/com/example/                 # Suites de pruebas unitarias
```
