# Roadmap del Proyecto APK Extractor

Este documento describe las fases de evolución técnica planificadas para convertir la aplicación en la herramienta definitiva de análisis e ingeniería inversa móvil en Android.

---

## Fase 1: Motor Base y Extracción Streaming (Completada)
- [x] Arquitectura MVVM reactiva con Kotlin y Jetpack Compose.
- [x] Flujo de extracción en streaming con búfer controlado de 32 KB directo a caché temporal.
- [x] Interfaz de usuario con tema oscuro ciberpunk / terminal y pantallas independientes (sin diálogos bloqueantes).
- [x] Explorador de archivos con breadcrumbs interactivos y visor hexadecimal con tabla ASCII.
- [x] Integración de dependencias nativas C (C11), C++ (C++17), Rust y Lua oficial (C 5.4.7).
- [x] Extracción directa de aplicaciones instaladas por el usuario y de sistema vía `QUERY_ALL_PACKAGES`.
- [x] Conexión y compilación del motor nativo NDK/CMake (`libapk_native_engine.so`) con NDK 26.1 y CMake 3.22.1.
- [x] Despachadores de corrutinas optimizados (`AppDispatchers.kt`) con `FastIODispatcher` y `ComputeDispatcher`.

---

## Fase 2: Decompilación, Recursos ARSC, Binarios y Material You (Completada)
- [x] **Decodificador de AXML (apk-parser)**: Conversión y desensamblado transparente de `AndroidManifest.xml` y layouts binarios a XML legible estructurado.
- [x] **Visor y Desensamblador DEX en Crudo (Smali)**: Integración de `org.smali:dexlib2` y `baksmali:2.5.2` para desensamblado exacto de bytecode Dalvik a Smali.
- [x] **Descompilador DEX a Java**: Traductor estructural `DexToJavaTranslator` que reconstruye clases, imports, campos, constructores, firmas de métodos y pseudocódigo de alto nivel en sintaxis Java legible.
- [x] **Selector Modal de Clases DEX**: Hoja inferior interactiva para buscar y filtrar clases dentro de cualquier archivo `.dex`.
- [x] **Editor de Código con Persistencia**: Soporte para modificar y guardar archivos DEX/Smali/Java y texto en caché con recálculo dinámico de integridad SHA-256 y tamaño en disco.
- [x] **Parser y Visor de `resources.arsc` (`ARSCLib`)**:
  - Integración de `io.github.reandroid:ARSCLib` para lectura de tablas `TableBlock`, `PackageBlock` y `ResourceEntry`.
  - Visor interactivo con búsqueda en tiempo real (ID, nombre, valor, tipo).
  - Filtro táctil horizontal por tipos de recursos (`string`, `color`, `drawable`, `layout`, etc.).
  - Modo resumen con análisis de paquetes, métricas del StringPool global y reporte técnico exportable.
- [x] **Visor Multimedia Integrado**:
  - Renderizado de imágenes (PNG, JPG, WebP, GIF, SVG) con Coil Compose y gestos multitáctiles de zoom y desplazamiento (`ImageViewerView.kt`).
  - Reproductor de audio nativo con AndroidX Media3 ExoPlayer para MP3, OGG, WAV, AAC, M4A, FLAC con visualización de progreso y control de tiempo (`AudioPlayerView.kt`).
- [x] **Suite de Dependencias para Inspección Binaria (.dat / .bin / streams)**:
  - Integración de `org.apache.tika:tika-core` para detección automática de magic bytes y tipos MIME.
  - Soporte de descompresión moderna: `com.github.luben:zstd-jni` (Zstandard), `org.lz4:lz4-java` (LZ4), `org.brotli:dec` (Brotli) y `org.apache.commons:commons-compress` (tar, bzip2, etc.).
  - Decodificación de formatos de serialización: `com.google.protobuf:protobuf-java` (Protocol Buffers wireformat), `org.msgpack:msgpack-core` (MessagePack) y `com.fasterxml.jackson.dataformat:jackson-dataformat-cbor` (CBOR).
  - Acceso e inspección de bases de datos embebidas SQLite vía `org.xerial:sqlite-jdbc`.
- [x] **Visor y Editor Especializado de Archivos .dat y .bin**:
  - Implementación de `BinaryDataViewerContent.kt` y `BinaryDataParser.kt` para visualización y edición en pantalla táctil sin depender de PC.
  - Editor hexadecimal interactivo con formateo automático, conteo dinámico de bytes y validación estricta de paridad.
  - Editor de texto plano UTF-8 / ISO-8859-1 con numeración de líneas y persistencia en caché.
  - Inspector de estructura con cálculo de Entropía de Shannon (0.0 - 8.0), detección de magic bytes y extracción de cadenas legibles (strings) con buscador reactivo y copiado rápido.
- [x] **Apartado de Configuración y Personalización Visual (`SettingsScreen.kt`)**:
  - Soporte para **Material You (Color Dinámico)**: Integración con Monet en Android 12+ (API 31+) extrayendo la paleta cromática del fondo de pantalla del usuario.
  - Soporte para **Cyber Dark**: Paleta clásica de ingeniería inversa de alto contraste (Azul Pizarra, Cian, Menta, Ámbar).
  - Persistencia de preferencias en SharedPreferences.
  - **Corrección de Modo Pantalla Completa (Edge-to-Edge)**: Implementación de márgenes de seguridad (`statusBarsPadding` y `navigationBarsPadding`) en todas las pantallas para evitar solapamientos con la barra de estado y la barra de navegación táctil del teléfono.
- [ ] **Análisis de Permisos y Componentes**: Extractor dedicado de Activities, Services, Receivers y Providers declarados en la app.

---

## Fase 3: Motor de Scripts en Lua (Automatización y Modding Móvil)
- [ ] **Consola Lua Embebida**: Editor de código en pantalla para ejecutar scripts `.lua` sobre el árbol de archivos extraído.
- [ ] **API de Modding en Lua**: Funciones nativas para buscar y reemplazar strings en archivos DEX y ARSC, parchear valores booleanos y renombrar recursos.
- [ ] **Gestor de Recetas**: Importación y exportación de scripts comunitarios para aplicar parches conocidos a APKs.

---

## Fase 4: Auditoría de Seguridad y Criptografía con Rust
- [x] **Inspector y Desensamblador de Librerías Nativas ELF (.so) con Goblin y Capstone**: Verificación de protecciones de compilación (PIE, stack canaries, RELRO, NX), extracción de símbolos dinámicos y funciones JNI, inspección de dependencias compartidas (DT_NEEDED) y desensamblado ASM de instrucciones nativas.
- [ ] **Verificación de Firmas APK**: Análisis de esquemas de firma v1 (JAR), v2 (APK Signature Scheme), v3 y v4.
- [ ] **Detección de Trackers y Vulnerabilidades**: Escaneo de librerías de terceros (Google Ads, Facebook SDK, Unity, etc.).
