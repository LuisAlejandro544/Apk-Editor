# Roadmap del Proyecto APK Extractor

Este documento describe las fases de evolución técnica planificadas para consolidar la aplicación como la herramienta definitiva de análisis e ingeniería inversa móvil en Android.

---

## Fase 1: Motor Base y Extracción Streaming (Completada)
- [x] Arquitectura MVVM reactiva con Kotlin y Jetpack Compose.
- [x] Flujo de extracción en streaming con búfer controlado de 32 KB directo a caché temporal (`context.cacheDir`).
- [x] Interfaz de usuario con tema oscuro ciberpunk / terminal y pantallas independientes sin diálogos bloqueantes.
- [x] Explorador de archivos con breadcrumbs interactivos y visor hexadecimal con tabla ASCII.
- [x] Integración de dependencias nativas C (C11), C++ (C++17), Rust y Lua oficial (C 5.4.7).
- [x] Extracción directa de aplicaciones instaladas por el usuario y de sistema vía `QUERY_ALL_PACKAGES`.
- [x] Conexión y compilación del motor nativo NDK/CMake (`libapk_native_engine.so`) con NDK 26.1 y CMake 3.22.1.
- [x] Despachadores de corrutinas optimizados (`AppDispatchers.kt`) con `FastIODispatcher` y `ComputeDispatcher`.

---

## Fase 2: Decompilación Dalvik, Recursos ARSC, Multimedia y Material You (Completada)
- [x] **Decodificador de AXML (apk-parser)**: Conversión y desensamblado transparente de `AndroidManifest.xml` y layouts binarios a XML legible estructurado.
- [x] **Visor y Desensamblador DEX en Crudo (Smali)**: Integración de `org.smali:dexlib2` y `baksmali:2.5.2` para desensamblado exacto de bytecode Dalvik a Smali.
- [x] **Selector Modal de Clases DEX**: Hoja inferior interactiva para buscar y filtrar clases dentro de cualquier archivo `.dex`.
- [x] **Editor de Código con Persistencia**: Soporte para modificar y guardar archivos DEX/Smali/Java y texto en caché con recálculo dinámico de integridad SHA-256 y tamaño en disco.
- [x] **Parser y Visor de `resources.arsc` (`ARSCLib`)**:
  - Lectura de tablas `TableBlock`, `PackageBlock` y `ResourceEntry` con `io.github.reandroid:ARSCLib`.
  - Visor interactivo con búsqueda en tiempo real (ID, nombre, valor, tipo).
  - Filtro táctil horizontal por tipos de recursos (`string`, `color`, `drawable`, `layout`, etc.).
  - Modo resumen con análisis de paquetes, métricas del StringPool global y reporte técnico exportable.
- [x] **Visor Multimedia Integrado**:
  - Renderizado de imágenes (PNG, JPG, WebP, GIF, SVG) con Coil Compose y gestos multitáctiles de zoom y desplazamiento (`ImageViewerView.kt`).
  - Reproductor de audio nativo con AndroidX Media3 ExoPlayer para MP3, OGG, WAV, AAC, M4A, FLAC con visualización de progreso y control de tiempo (`AudioPlayerView.kt`).
- [x] **Apartado de Configuración y Personalización Visual (`SettingsScreen.kt`)**:
  - Soporte para **Material You (Color Dinámico)** con integración Monet en Android 12+ (API 31+).
  - Soporte para **Cyber Dark** (paleta clásica de ingeniería inversa de alto contraste).
  - Persistencia de preferencias en SharedPreferences.
  - **Corrección de Modo Pantalla Completa (Edge-to-Edge)**: Implementación de márgenes de seguridad (`statusBarsPadding` y `navigationBarsPadding`) en todas las pantallas.

---

## Fase 3: Descompilación Industrial JADX Core, Criptografía BouncyCastle y ProGuard ReTrace (Completada)
- [x] **Descompilador Java Estructural con JADX Core (`JadxDecompilerService.kt`)**:
  - Integración de `io.github.skylot:jadx-core` para transformar bytecode Dalvik (.dex) en código fuente Java real, fiel y de grado de producción.
  - Opciones de desofuscación heurística, saneamiento de identificadores no válidos y resolución de referencias entre paquetes.
  - Alternancia dinámica en `FileDetailScreen` entre vista Smali cruda y vista de código Java generado por JADX.
- [x] **Auditoría Forense de Firmas APK con BouncyCastle (`BouncyCastleCryptoService.kt`)**:
  - Integración de `org.bouncycastle:bcprov-jdk18on` y `bcpkix-jdk18on` para análisis de certificados en `META-INF/` (`.RSA`, `.DSA`, `.EC`, `CERT.SF`, `MANIFEST.MF`).
  - Extracción de Sujeto, Emisor, Período de Validez, Algoritmo de firma y Clave pública.
  - Cálculo de Fingerprints en SHA-256, SHA-1 y MD5 con botón de copiado rápido al portapapeles.
  - Soporte criptográfico para hashes avanzados (SHA3-256, Keccak, RIPEMD-160) y estructuras ASN.1 / PKCS#7 SignedData.
- [x] **Desofuscación y Mapeo con ProGuard ReTrace (`ProguardRetraceService.kt`)**:
  - Integración de `com.guardsquare:proguard-retrace` para cargar archivos `mapping.txt`.
  - Reconstrucción de nombres ofuscados en stack traces y código descompilado.
- [x] **Inspector y Desensamblador ELF (.so) con Goblin y Capstone**:
  - Análisis de arquitectura, mitigaciones (PIE, Stack Canaries, RELRO, NX) y funciones JNI (`Java_*`).
  - Desensamblado de código máquina nativo a nivel mnemónico ASM.

---

## Fase 4: Motor de Descompresión Multi-Algoritmo y Suite Binaria Avanzada (Completada)
- [x] **Motor Unificado de Descompresión en Streaming (`DecompressionService.kt`)**:
  - Soporte para Zstandard (`zstd-jni`), LZ4 (`lz4-java`), Brotli (`brotli-dec`), Bzip2 (`commons-compress`), XZ/LZMA, GZIP y Deflate.
  - Detección automática de algoritmos por magic bytes y cabeceras binarias.
  - Extracción directa a `context.cacheDir` con búfer de 32 KB para blindar el consumo de memoria RAM.
- [x] **Visor y Editor Especializado de Archivos .dat y .bin (`BinaryDataViewerContent.kt` & `BinaryDataParser.kt`)**:
  - Editor hexadecimal interactivo con validación de paridad y bloques de 16 bytes.
  - Editor de texto plano UTF-8 / ISO-8859-1 con numeración vertical de líneas.
  - Inspector de estructura con cálculo de Entropía de Shannon (0.0 a 8.0) y extractor de cadenas legibles (>= 4 caracteres).
  - Acceso e inspección de bases de datos SQLite embebidas (`sqlite-jdbc`).
  - Decodificación de formatos de serialización: Protocol Buffers (`protobuf-java`), MessagePack (`msgpack-core`) y CBOR (`jackson-dataformat-cbor`).

---

## Fase 5: Motor de Scripts en Lua (Automatización y Modding Móvil)
- [ ] **Consola Lua Embebida**: Editor de código en pantalla para ejecutar scripts `.lua` sobre el árbol de archivos extraído.
- [ ] **API de Modding en Lua**: Funciones nativas para buscar y reemplazar strings en archivos DEX y ARSC, parchear valores booleanos y renombrar recursos.
- [ ] **Gestor de Recetas**: Importación y exportación de scripts comunitarios para aplicar parches conocidos a APKs.

---

## Fase 6: Auditoría de Seguridad Extendida y Detección de Trackers
- [ ] **Verificación de Esquemas de Firma APK**: Análisis de versiones v1 (JAR), v2, v3 y v4.
- [ ] **Detección de Trackers y Vulnerabilidades**: Escaneo de librerías de analítica y publicidad de terceros.
