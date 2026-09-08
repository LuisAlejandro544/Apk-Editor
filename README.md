# APK Extractor Mobile

Entorno móvil nativo desarrollado en Android (Kotlin + Jetpack Compose) con motor multilenguaje de alto rendimiento (C, C++, Rust y Lua original en C) y una suite profesional de ingeniería inversa para descompilación estructural con JADX, auditoría criptográfica con BouncyCastle, desofuscación ProGuard ReTrace, descompresión multi-algoritmo, inspección de recursos ARSC, desensamblado Smali/ELF y edición de binarios directamente desde el teléfono móvil.

## Problema que resuelve
Permite a desarrolladores, auditores de seguridad, analistas de malware y entusiastas de Android realizar ingeniería inversa exhaustiva, auditoría de certificados de firma APK, descompilación completa de bytecode Dalvik (.dex) a código fuente Java fiel, desofuscación de símbolos con mapeos ProGuard/R8, descompresión de streams binarios de última generación (Zstandard, LZ4, Brotli, Bzip2, XZ), inspección de tablas de recursos binarios (`resources.arsc`), análisis de librerías nativas (.so) con Goblin/Capstone y edición de archivos directamente en la pantalla táctil de un teléfono inteligente, sin requerir ordenador personal, terminal ADB ni permisos de superusuario (root).

## Características Principales

1. **Descompilación Estructural a Java con JADX Core (`JadxDecompilerService.kt`)**:
   - **Motor Industrial JADX**: Integración directa de `io.github.skylot:jadx-core` ejecutado nativamente en el entorno móvil.
   - **Código Java Fiel y Comprensible**: Reconstrucción de clases completas, imports, interfaces, métodos, estructuras de control, tipos genéricos y expresiones complejas a partir de bytecode Dalvik (.dex).
   - **Desofuscación Heurística**: Saneamiento automático de identificadores no imprimibles o colisionados en el bytecode.
   - **Selector Dinámico**: Alternancia fluida en la barra de herramientas de `FileDetailScreen` entre código desensamblado en Smali puro y código fuente Java generado por JADX.

2. **Auditoría Forense de Certificados y Criptografía con BouncyCastle (`BouncyCastleCryptoService.kt`)**:
   - **Análisis de Bloques de Firma APK**: Inspección completa de archivos de firma en `META-INF/` (`.RSA`, `.DSA`, `.EC`, `CERT.SF` y `MANIFEST.MF`).
   - **Metadatos de Certificados X.509**: Extracción detallada de Sujeto (Subject), Emisor (Issuer), Número de serie, Período de validez (Not Before / Not After), Algoritmo de firma y Clave pública.
   - **Huellas Digitales Criptográficas (Fingerprints)**: Cálculo instantáneo de hashes SHA-256, SHA-1 y MD5 con botón de copiado rápido al portapapeles.
   - **Criptografía Avanzada y ASN.1**: Decodificación de estructuras PKCS#7 SignedData y soporte para algoritmos de resumen modernos (SHA3-256, Keccak, RIPEMD-160).

3. **Desofuscación y Mapeo con ProGuard ReTrace (`ProguardRetraceService.kt`)**:
   - **Procesamiento de Mapeos**: Integración de `com.guardsquare:proguard-retrace` para cargar archivos `mapping.txt` generados por ProGuard o R8.
   - **Reconstrucción de Símbolos**: Desofuscación en tiempo real de nombres de paquetes, clases, métodos y atributos ofuscados tanto en stack traces como en código descompilado.

4. **Motor Unificado de Descompresión Multi-Algoritmo en Streaming (`DecompressionService.kt`)**:
   - **Formatos Modernos y Tradicionales**: Descompresión de flujos de datos en Zstandard (`zstd-jni`), LZ4 (`lz4-java`), Brotli (`brotli-dec`), Bzip2 (`commons-compress`), XZ / LZMA, GZIP y Deflate.
   - **Detección Automática por Magic Bytes**: Identificación inteligente del algoritmo mediante la lectura de firmas y cabeceras binarias.
   - **Streaming Anti-Crash**: Descompresión continua hacia el almacenamiento en caché temporal (`context.cacheDir`) con búfer de 32 KB, protegiendo al dispositivo móvil contra errores por falta de memoria (Out Of Memory).

5. **Suite de Inspección y Edición de Archivos Binarios (.dat / .bin / streams)**:
   - **Editor Hexadecimal Táctil (`BinaryDataViewerContent.kt`)**: Edición directa de bytes en hexadecimal con validación estricta de paridad, formateo automático en bloques de 16 bytes y cálculo dinámico de longitud.
   - **Editor de Texto Plano**: Edición y visualización en UTF-8 e ISO-8859-1 con numeración vertical de líneas y persistencia directa en caché.
   - **Inspector de Entropía y Strings (`BinaryDataParser.kt`)**: Cálculo de Entropía de Shannon (0.0 a 8.0) para detectar compresión o cifrado, detección de tipos MIME con `org.apache.tika:tika-core` y extracción de cadenas de texto legibles (>= 4 caracteres) con buscador interactivo.
   - **Soporte de Bases de Datos y Serialización**: Acceso e inspección de bases de datos SQLite embebidas (`sqlite-jdbc`), Protocol Buffers (`protobuf-java`), MessagePack (`msgpack-core`) y CBOR (`jackson-dataformat-cbor`).

6. **Visor y Desensamblador DEX con Smali (`org.smali:dexlib2` & `baksmali`)**:
   - **Bytecode Crudo Dalvik**: Desensamblado exacto de instrucciones a sintaxis Smali con Baksmali 2.5.2.
   - **Explorador de Clases**: Selector modal en hoja inferior (*bottom sheet*) con buscador en vivo entre todas las clases compiladas del archivo DEX.

7. **Visor e Inspector de Recursos ARSC (`io.github.reandroid:ARSCLib`)**:
   - **Parsing Nativo de `resources.arsc`**: Procesamiento integral de `TableBlock`, `PackageBlock` y `ResourceEntry` con `ARSCLib`.
   - **Filtrado Dinámico**: Chips horizontales para filtrar por tipos (`string`, `drawable`, `color`, `layout`, `id`, `dimen`, `bool`, etc.) y búsqueda en tiempo real por ID hexadecimal o nombre.
   - **Resumen Estructural**: Análisis de paquetes registrados, métricas globales del StringPool y reporte técnico exportable.

8. **Análisis de Librerías Nativas ELF (.so) con Goblin y Capstone**:
   - **Cabeceras y Mitigaciones**: Inspección de arquitectura (ARM64, ARMv7, x86_64, x86), endianness y mitigaciones de seguridad (PIE, Stack Canaries, RELRO, NX/DEP).
   - **Símbolos y Funciones JNI**: Detección y filtrado de funciones nativas Java (`Java_*`), símbolos exportados dinámicamente y dependencias compartidas (`DT_NEEDED`).
   - **Desensamblado ASM**: Mnemónicos e instrucciones de máquina mediante Capstone.

9. **Visor Multimedia de Activos y Decodificación AXML**:
   - **Visor de Imágenes Interactivo**: Renderizado de PNG, JPG, WebP, GIF animado y SVG con Coil Compose y soporte táctil para zoom gestual y paneo.
   - **Reproductor de Audio Nativo**: Reproducción de audio (MP3, OGG, WAV, AAC, M4A, FLAC) con AndroidX Media3 ExoPlayer y barra de progreso.
   - **Decodificación AXML**: Conversión transparente de binarios XML (`AndroidManifest.xml`) a texto XML legible con `apk-parser`.

10. **Personalización Visual (Material You & Cyber Dark) y Modo Pantalla Completa**:
    - **Soporte Material You**: Integración con el motor Monet en Android 12+ (API 31+) sincronizando la paleta cromática con el fondo de pantalla del teléfono.
    - **Tema Cyber Dark**: Paleta de alto contraste optimizada para ingeniería inversa (Azul Pizarra, Cian, Menta, Ámbar).
    - **Ajuste Edge-to-Edge**: Márgenes seguros con `Modifier.statusBarsPadding()` y `Modifier.navigationBarsPadding()` en todas las pantallas para evitar solapamientos con la barra de estado y la barra de navegación táctil.

11. **Arquitectura Multilenguaje Vinculada (`libapk_native_engine.so`)**:
    - **C (C11)**: JNI bridge, liberación segura de memoria (`rust_apk_free_string`) y rutinas de enlace.
    - **C++ (C++17)**: Parsing binario y análisis estructural.
    - **Rust / Goblin**: Análisis seguro de estructuras ELF y verificación criptográfica.
    - **Lua (5.4.7 C original)**: Intérprete nativo en C compilado directamente sin wrappers para automatización.
    - **Compilación Activa**: Enlazado mediante CMake 3.22.1 y NDK 26.1 con soporte para arquitecturas arm64-v8a y armeabi-v7a.

## Requisitos del Sistema
- Dispositivo móvil Android con Android 8.0 (API 26) o superior (recomendado Android 12+ / API 31+ para soporte de Material You).
- Entorno de desarrollo: Android Studio o Gradle 8.x / 9.x.
- Android NDK (r25+ / r26+) y CMake 3.22.1+.
- JDK 17 o 21.

## Compilación y Empaquetado

```bash
# Compilación del APK de depuración
gradle :app:assembleDebug

# Ejecución de la suite de pruebas unitarias
gradle :app:testDebugUnitTest
```

El binario APK generado se localiza en:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Distribución
La aplicación está preparada para su distribución libre a través de canales alternativos como **Uptodown** o descarga directa de APK, sin restricciones de catálogo ni eliminación de permisos necesarios como `QUERY_ALL_PACKAGES`.
