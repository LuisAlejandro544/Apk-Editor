# APK Extractor Mobile

Entorno móvil nativo desarrollado en Android (Kotlin + Jetpack Compose) con motor multilenguaje de alto rendimiento (C, C++, Rust y Lua original en C) y herramientas profesionales de ingeniería inversa para descompresión, auditoría, exploración, desensamblado, visualización de recursos compilados e inspección de archivos APK directamente en teléfonos inteligentes.

## Problema que resuelve
Permite a desarrolladores, auditores de seguridad, analistas de malware y entusiastas de Android realizar ingeniería inversa completa, inspección de tablas de recursos binarios (`resources.arsc`), extracción de bytecode Dalvik (.dex), descompilación estructural a Java, análisis de librerías nativas (.so), visualización de activos multimedia y edición de archivos directamente en un teléfono sin requerir ordenador personal, terminal ADB ni permisos de superusuario (root).

## Características Principales
1. **Extracción de APKs Externos**: Selección mediante el Storage Access Framework (SAF) de cualquier archivo `.apk` descargado en el dispositivo móvil.
2. **Extracción de Aplicaciones Instaladas**: Detección y extracción directa de cualquier app en uso en el teléfono a través de `sourceDir` del sistema (`QUERY_ALL_PACKAGES`), diferenciando apps de usuario y apps de sistema.
3. **Streaming a Caché Anti-Crash**: Descompresión en streaming con búfer estricto de 32 KB directo a la partición de almacenamiento en caché temporal (`cacheDir`), garantizando consumo mínimo de RAM y blindando la aplicación contra cierres por falta de memoria (*Out of Memory*).
4. **Visor e Inspector de Recursos ARSC (`io.github.reandroid:ARSCLib`)**:
   - **Parsing Nativo de `resources.arsc`**: Procesamiento de la tabla completa de recursos binarios mediante la librería especializada `ARSCLib`.
   - **Explorador Interactivo de Recursos**: Búsqueda en tiempo real por identificador hexadecimal (`0x7F...`), nombre de recurso, valor o referencia.
   - **Filtrado Dinámico por Tipo**: Chips táctiles para filtrar instantáneamente por `string`, `drawable`, `color`, `layout`, `id`, `dimen`, `bool`, `style`, `attr`, `array`, etc., con conteo en vivo de entradas.
   - **Detalle de Recursos y Copia Rápida**: Tarjetas con badges de color según el tipo de recurso, identificador hexadecimal, referencia oficial (`@string/...`) con botón de copiado al portapapeles y valor resuelto.
   - **Auditoría de Paquetes y StringPool**: Modo de resumen estructural que desglosa paquetes registrados, IDs asignados, tipos de recursos contenidos, cantidad de cadenas globales en el StringPool y reporte técnico exportable.
5. **Visor y Desensamblador de Archivos DEX (`org.smali:dexlib2` & `baksmali`)**:
   - **Bytecode Crudo (Smali)**: Desensamblado exacto de instrucciones Dalvik a sintaxis Smali pura mediante Baksmali 2.5.2.
   - **Descompilador a Java Estructural**: Reconstrucción de clases, paquetes, firmas de métodos, tipos de datos y pseudocódigo Java comprensible.
   - **Explorador de Clases Integrado**: Selector modal en hoja inferior (*bottom sheet*) con buscador y filtrado interactivo entre todas las clases compiladas del archivo DEX.
6. **Visor y Desensamblador de Librerías Nativas ELF (.so) con Goblin y Capstone**:
   - **Análisis de Cabecera y Mitigaciones (Goblin)**: Inspección de arquitectura (ARM64, ARMv7, x86_64, x86), endianness, tipo ELF, entry point y auditoría de mitigaciones de seguridad (PIE, NX/DEP, RELRO y Stack Canaries).
   - **Símbolos y Funciones JNI**: Detección y filtrado de funciones nativas Java (`Java_*`), símbolos exportados dinámicos y dependencias importadas.
   - **Explorador de Símbolos en Modal Bottom Sheet**: Selector con búsqueda interactiva y filtro "⭐ Solo JNI" para navegar entre funciones nativas.
   - **Dependencias Compartidas (DT_NEEDED)**: Lista de bibliotecas dinámicas vinculadas (libc.so, libm.so, liblog.so, etc.).
   - **Desensamblado ASM (Capstone)**: Desensamblado de código nativo a nivel de instrucciones con offsets y representación mnemónica.
   - **Extractor de Cadenas (Strings)**: Extracción y filtrado de cadenas ASCII embebidas en el binario.
7. **Visor Multimedia de Activos (Coil Compose & AndroidX Media3 ExoPlayer)**:
   - **Visor de Imágenes Interactivo**: Renderizado de PNG, JPG, WebP, GIF animado y SVG con soporte para gestos multitáctiles de zoom y desplazamiento (*pinch-to-zoom / pan*).
   - **Reproductor de Audio Nativo**: Reproducción directa de formatos de audio (MP3, OGG, WAV, AAC, M4A, FLAC) con barra de progreso (*scrubber*), controles Play/Pause e indicador de tiempo.
8. **Decodificación Automática de AXML (apk-parser)**: Conversión transparente de binarios compilados de Android (`AndroidManifest.xml` y recursos XML) a texto XML legible sin pérdida de etiquetas ni atributos.
9. **Editor de Código Móvil con Persistencia**: Modificación interactiva de código (Smali, Java, XML, JSON, texto) con numeración de líneas alineada verticalmente, alternancia entre modo lectura y edición, detección de cambios sin guardar y guardado persistente en disco con recálculo automático de hash SHA-256 y tamaño.
10. **Explorador de Archivos Integrado**: Árbol de directorios con migas de pan (*breadcrumbs*), filtrado rápido y categorización visual por tipo de recurso (DEX, Manifiesto, ARSC, Recursos gráficos, Assets, Librerías ELF, Audio, Imágenes).
11. **Inspección Hexadecimal (Hex Dump)**: Visor hexadecimal con offsets y representación ASCII para auditoría de bajo nivel de cualquier archivo binario o librería `.so`.
12. **Suite Especializada de Inspección, Edición y Descompresión Binaria (.dat, .bin, streams y bases de datos)**:
    - **Visor y Editor Nativo de Archivos .dat y .bin (`BinaryDataViewerContent.kt`)**: Soporte integral para inspeccionar y editar directamente archivos de datos binarios desde la pantalla táctil móvil sin necesidad de PC.
    - **Editor Hexadecimal Interactivo**: Edición directa de bytes en hexadecimal (`XX XX XX...`), cálculo dinámico del tamaño en bytes, formateador automático de bloques de 16 bytes por línea, validación estricta de paridad y botón de copiado rápido.
    - **Editor de Texto y Cadenas (UTF-8 / ISO-8859-1)**: Modo de edición de cadenas de texto plano preservando el 100% de la fidelidad de bytes, con numeración de líneas y guardado persistente.
    - **Inspector de Estructura Binaria y Extractor de Strings**: Diagnóstico automático con cálculo de Entropía de Shannon (0.0 a 8.0) para detectar cifrado o compresión, detección de números mágicos (Zstandard, GZIP, SQLite, ELF, DEX, Brotli, LZ4, Protobuf, CBOR, MsgPack), extracción de cadenas de texto legibles (>= 4 caracteres) y buscador instantáneo.
    - **Identificación de Tipos MIME y Números Mágicos**: Detección de firmas binarias mediante `org.apache.tika:tika-core`.
    - **Descompresión Multi-Algoritmo en Streaming**: Soporte para Zstandard (`com.github.luben:zstd-jni`), LZ4 (`org.lz4:lz4-java`), Brotli (`org.brotli:dec`) y suites de compresión avanzadas (`org.apache.commons:commons-compress`).
    - **Decodificación de Serialización Binaria y Protobuf**: Decodificación de Protocol Buffers (`com.google.protobuf:protobuf-java`), MessagePack (`org.msgpack:msgpack-core`) y CBOR (`com.fasterxml.jackson.dataformat:jackson-dataformat-cbor`).
    - **Soporte de Bases de Datos Incrustadas**: Lectura e inspección de bases de datos relacionales SQLite mediante `org.xerial:sqlite-jdbc`.
13. **Gestor de Caché y Memoria**: Supervisión de espacio en disco en tiempo real y limpieza segura de proyectos temporales.
14. **Arquitectura Multilenguaje Vinculada (`libapk_native_engine.so`)**:
    - **C (C11)**: Operaciones de bajo nivel, JNI bridge, liberación de memoria (`rust_apk_free_string`) y compresión directa.
    - **C++ (C++17)**: Motor para parsing de bytecode DEX y estructuras binarias.
    - **Rust / Goblin**: ABI nativa con C, motor Goblin para análisis de estructuras binarias ELF y verificación criptográfica.
    - **Capstone**: Motor de desensamblado de instrucciones máquina para arquitecturas móviles.
    - **Lua (5.4.7 C original)**: Intérprete nativo sin capas wrapper intermedias para scripts y parches automatizados.
    - **Compilación Activa**: Vinculado al sistema de compilación de Gradle mediante CMake 3.22.1 y NDK 26.1 con soporte para arquitecturas arm64-v8a y armeabi-v7a.

## Requisitos Previos
- Dispositivo o emulador Android con Android 8.0 (API 26) o superior (recomendado Android 10+ / API 29+).
- Entorno de desarrollo: Android Studio Flamingo / Hedgehog / Iguana / Ladybug o Gradle 8.x / 9.x.
- Android NDK (r25+ / r26+) y CMake 3.22.1+.
- JDK 17 o 21.

## Instalación y Compilación
Para compilar y generar el archivo APK instalable:

```bash
# Compilación del APK de depuración
gradle :app:assembleDebug

# Ejecución de la suite de pruebas locales
gradle :app:testDebugUnitTest
```

El archivo APK generado se ubicará en:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Distribución
La app está configurada y adaptada para distribución libre mediante plataformas alternativas como **Uptodown** o descarga directa de APK, sin limitaciones arbitrarias de catálogo ni recortes de permisos.
