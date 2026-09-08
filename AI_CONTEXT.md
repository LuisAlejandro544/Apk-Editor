# AI Context (Contexto de Inteligencia Artificial)

Este documento condensa la información fundamental que cualquier modelo de IA o agente autónomo necesita para comprender el propósito, diseño, restricciones y estado técnico del proyecto.

---

## 1. Misión del Proyecto
Construir una suite completa de extracción, inspección, auditoría, desensamblado, análisis de recursos e ingeniería inversa de paquetes APK que opere **100% de manera autónoma en un teléfono móvil Android**, sin necesidad de ordenador personal ni terminal ADB.

## 2. Perfil del Usuario
- El usuario opera exclusivamente desde un **dispositivo móvil (teléfono Android)** con pantalla táctil, sin acceso a una PC ni emulador de escritorio.
- El canal de distribución prioritario es **Uptodown** o distribución independiente de APKs, no Google Play Store. No deben recortarse permisos legítimos como `QUERY_ALL_PACKAGES`.
- Prioridad total a la **funcionalidad completa de las dependencias** por encima del peso final del APK. No implementar soluciones improvisadas o "sin dependencias" cuando existan bibliotecas probadas de la industria (como `ARSCLib`, `baksmali`, `dexlib2`, `Media3 ExoPlayer`, `Coil` y `apk-parser`).

## 3. Principios Técnicos Innegociables
1. **Prevención de Caídas de Memoria (Anti-Crash OOM)**:
   - Todo archivo extraído de un APK se descomprime en **streaming** con un búfer estricto de 32 KB directo a la partición de almacenamiento en caché temporal (`context.cacheDir`).
   - Está terminantemente prohibido almacenar el contenido completo de un archivo APK o sus ficheros descomprimidos en arrays de bytes en memoria RAM.
2. **Navegación Móvil Óptima**:
   - Evitar modales o diálogos emergentes bloqueantes sobrecargados. Las pantallas principales son destinos completos de navegación con soporte natural para el botón 'Atrás' del sistema Android.
3. **Pila Tecnológica Multilenguaje**:
   - **Kotlin + Jetpack Compose**: Capa de presentación y orquestación UI reactiva en arquitectura MVVM.
   - **C (C11)**: Integrado en el NDK para rutinas de enlace JNI y liberación segura de memoria (`rust_apk_free_string`).
   - **C++ (C++17)**: Procesamiento de estructuras binarias.
   - **Lua (C puro 5.4.7 oficial)**: Compilado nativamente en C en `app/src/main/cpp/lua/`, sin wrappers innecesarios ni código simulado.
   - **Rust / Goblin & Capstone**: Crate y ABI nativa en `app/src/main/cpp/rust_core` para módulos criptográficos, análisis de estructuras binarias ELF con Goblin y desensamblado de instrucciones ASM móviles con Capstone.
4. **Inspección de Recursos Compilados (`resources.arsc` con ARSCLib)**:
   - Integración de `io.github.reandroid:ARSCLib` para parsear `TableBlock`, iterar sobre `PackageBlock`, `ResourceEntry` y el `StringPool` global.
   - Componente visual `ArscViewerContent.kt` con selector de modos (Recursos vs Resumen), barra de búsqueda reactiva y chips horizontales para filtrar por tipos de recursos (`string`, `color`, `drawable`, `layout`, etc.).
   - Tarjetas informativas con identificador hexadecimal, referencia oficial (`@string/...`) con botón de copiado rápido y visualización de valores y configuraciones.
   - Modo de resumen estructural con métricas de paquetes registrados, StringPool global y reporte técnico exportable.
5. **Desensamblado y Decompilación DEX (Smali & Java)**:
   - Integración oficial de `org.smali:dexlib2` y `org.smali:baksmali` (2.5.2) para desensamblado exacto a nivel de registros, instrucciones y etiquetas Dalvik.
   - Reconstrucción a Java estructural de alto nivel mediante `DexToJavaTranslator` para lectura amigable de clases, interfaces, campos, métodos y llamadas.
   - Hoja modal interactiva para explorar y filtrar clases compiladas en el DEX por paquete y nombre.
6. **Inspección y Desensamblado de Librerías Nativas ELF (.so)**:
   - Análisis de arquitectura, entry point, endianness y protecciones de compilación (PIE, Stack Canaries, NX/DEP, RELRO) mediante el motor Goblin.
   - Extracción de símbolos exportados y detección automática de funciones JNI (`Java_*`).
   - Hoja modal interactiva para explorar símbolos con buscador instantáneo y filtro "⭐ Solo JNI".
   - Desensamblado de código máquina nativo a nivel de mnemónicos ASM mediante Capstone.
   - Identificación de dependencias compartidas (`DT_NEEDED`) y extracción de cadenas ASCII.
7. **Visor de Medios (Coil & Media3 ExoPlayer)**:
   - Visor de imágenes táctil con soporte para zoom gestual y paneo para formatos gráficos (PNG, JPG, WebP, GIF animado y SVG).
   - Reproductor de audio integrado para inspeccionar archivos de sonido empaquetados en el APK (MP3, OGG, WAV, AAC, M4A, FLAC).
8. **Decodificación y Edición de Recursos (AXML & Código)**:
   - Uso de `com.jaredrummler:apk-parser:1.0.2` para transformar `AndroidManifest.xml` binario en texto plano estructurado.
   - Entorno de edición táctil con numeración de líneas que permite modificar código (Smali, Java, XML, texto) y persistir las modificaciones en caché, manteniendo actualizados los hashes SHA-256 y metadatos sin corromper el almacenamiento.
9. **Despacho Concurrente y Gestión de Memoria**:
   - Despachadores dedicados en `AppDispatchers.kt` (`ComputeDispatcher` para parsing de ARSC, decompilación DEX y desensamblado ASM; `FastIODispatcher` para lecturas en disco).
   - Monitor de almacenamiento dedicado (`CacheManagerScreen`) que calcula el peso de cada proyecto y permite la purga individual o total de los archivos temporales.
10. **Suite de Análisis, Edición y Descompresión Binaria (.dat, .bin y Streams)**:
    - **Visor y Editor Nativo (`BinaryDataViewerContent.kt` & `BinaryDataParser.kt`)**: Arquitectura especializada para visualizar y editar archivos `.dat` y `.bin` directamente desde el dispositivo móvil.
    - **Modo Editor Hexadecimal**: Modificación en tiempo real de pares de bytes hexadecimales con validación estricta de paridad, formateador automático por bloques de 16 bytes y guardado con conversión binaria directa.
    - **Modo Editor de Texto**: Visualización y edición en texto UTF-8/ISO-8859-1 con numeración de líneas para streams de configuración y texto binario.
    - **Modo Inspector y Extracción de Cadenas**: Detección de magic bytes y firmas, análisis de Entropía de Shannon (0.0 - 8.0) para evaluar compresión/cifrado y buscador con extracción de strings legibles (>= 4 caracteres) con función de copiado rápido.
    - **Identificación Mágica**: Detección de tipos MIME y estructuras binarias desconocidas mediante `org.apache.tika:tika-core`.
    - **Compresión / Descompresión Avanzada**: Descompresión streaming con `com.github.luben:zstd-jni` (Zstandard), `org.lz4:lz4-java` (LZ4), `org.brotli:dec` (Brotli) y `org.apache.commons:commons-compress`.
    - **Protocol Buffers y Serialización**: Inspección y decodificación de `com.google.protobuf:protobuf-java`, `org.msgpack:msgpack-core` y `com.fasterxml.jackson.dataformat:jackson-dataformat-cbor`.
    - **Bases de Datos Embebidas**: Conector para bases de datos SQLite incrustadas vía `org.xerial:sqlite-jdbc`.
