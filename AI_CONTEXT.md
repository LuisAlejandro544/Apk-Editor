# AI Context (Contexto de Inteligencia Artificial)

Este documento condensa la información fundamental que cualquier modelo de IA o agente autónomo necesita para comprender el propósito, arquitectura, restricciones y estado técnico del proyecto.

---

## 1. Misión del Proyecto
Proporcionar un entorno integral y autónomo de ingeniería inversa, descompilación estructural de código, auditoría forense de firmas criptográficas, análisis de recursos compilados y edición binaria que opere **100% de manera local en un teléfono inteligente Android**, sin depender de ordenador personal ni terminal ADB.

## 2. Perfil del Usuario y Restricciones de Plataforma
- **Dispositivo Móvil Exclusivo**: El usuario final interactúa con la aplicación únicamente mediante la pantalla táctil de su teléfono móvil.
- **Distribución Externa (Uptodown)**: La aplicación se distribuye fuera de Google Play Store; los permisos legítimos como `QUERY_ALL_PACKAGES` deben preservarse íntegramente.
- **Dependencias 100% Funcionales de Grado Industrial**: Prioridad absoluta a la estabilidad y potencia de las dependencias oficiales sobre el peso final del APK. Queda prohibido implementar soluciones improvisadas o "sin dependencias" cuando existan bibliotecas probadas de la industria (`jadx-core`, `bouncy-castle`, `proguard-retrace`, `ARSCLib`, `baksmali`, `commons-compress`, `tika-core`, etc.).
- **Prohibido el uso de `persist.sys.*`**: No utilizar propiedades restringidas del sistema para aceleración o ajustes.

## 3. Principios de Arquitectura Técnica

1. **Descompilación Estructural a Java con JADX Core (`JadxDecompilerService.kt`)**:
   - Ejecución integrada de `io.github.skylot:jadx-core` sobre el dispositivo móvil para traducir clases Dalvik (.dex) a código Java limpio y legible.
   - Manejo de opciones de `JadxArgs` orientadas a desofuscación heurística, control de excepciones y resolución de referencias cruzadas.
   - Ejecución asíncrona sobre `ComputeDispatcher` en `ApkViewModel.kt` para mantener la fluidez de la interfaz táctil.

2. **Auditoría Forense Criptográfica con BouncyCastle (`BouncyCastleCryptoService.kt`)**:
   - Integración de `org.bouncycastle:bcprov-jdk18on` y `bcpkix-jdk18on` para inspección de firmas APK en `META-INF/` (`.RSA`, `.DSA`, `.EC`, `CERT.SF`, `MANIFEST.MF`).
   - Decodificación de certificados X.509 y estructuras PKCS#7 SignedData.
   - Extracción de Sujeto, Emisor, Fechas de vigencia, Algoritmo de firma y cálculo instantáneo de huellas digitales (SHA-256, SHA-1, MD5).

3. **Desofuscación y Mapeo con ProGuard ReTrace (`ProguardRetraceService.kt`)**:
   - Integración de `com.guardsquare:proguard-retrace` para procesar archivos `mapping.txt` generados por ProGuard o R8.
   - Reconstrucción de stack traces ofuscados y resolución de nombres reales de clases, métodos y campos.

4. **Motor de Descompresión Multi-Algoritmo en Streaming (`DecompressionService.kt`)**:
   - Soporte para Zstandard (`zstd-jni`), LZ4 (`lz4-java`), Brotli (`brotli-dec`), Bzip2 (`commons-compress`), XZ/LZMA, GZIP y Deflate.
   - Detección automática de firmas binarias y descompresión en streaming con búfer estricto de 32 KB directo a `context.cacheDir`, blindando la app contra errores de falta de memoria (OOM).

5. **Suite de Inspección, Edición y Extracción Binaria (.dat / .bin / streams)**:
   - Componentes `BinaryDataViewerContent.kt` y `BinaryDataParser.kt` adaptados a la interacción táctil.
   - Modos: Editor Hexadecimal (con validación de paridad y bloques de 16 bytes), Editor de Texto plano (UTF-8 / ISO-8859-1 con numeración de líneas) e Inspector de Entropía de Shannon (0.0 a 8.0) con extractor de cadenas legibles (>= 4 caracteres).
   - Acceso a bases de datos SQLite incrustadas (`sqlite-jdbc`) y decodificación de Protocol Buffers (`protobuf-java`), MessagePack (`msgpack-core`) y CBOR (`jackson-dataformat-cbor`).

6. **Desensamblado Dalvik y Recursos Compilados**:
   - Smali puro con `baksmali:2.5.2` y `dexlib2` con selector interactivo de clases compiladas.
   - Parsing de `resources.arsc` mediante `io.github.reandroid:ARSCLib` con visualización por tarjetas, copia de referencias `@string/...` y resumen del StringPool global.
   - Decodificación transparente de binarios XML con `apk-parser`.

7. **Inspección de Binarios Nativos ELF (.so)**:
   - Análisis de arquitectura, mitigaciones de seguridad (PIE, Stack Canaries, RELRO, NX) y funciones JNI con Goblin.
   - Desensamblado de código máquina a nivel mnemónico ASM mediante Capstone.

8. **Pila Multilenguaje Nativa (`libapk_native_engine.so`)**:
   - C (C11) para enlace JNI y gestión de memoria; C++ (C++17) para estructuras binarias; Rust para análisis ELF; Lua 5.4.7 oficial en C puro para automatización. Compilación mediante CMake 3.22.1 y NDK 26.1.

9. **Diseño Visual y Pantalla Completa (Edge-to-Edge)**:
   - Personalización reactiva entre tema clásico "Cyber Dark" y tema adaptativo "Material You" (Monet en Android 12+ / API 31+) con persistencia en SharedPreferences.
   - Insets seguros (`statusBarsPadding` y `navigationBarsPadding`) en todas las pantallas para evitar solapamientos con la barra de estado y la barra de navegación del teléfono.
