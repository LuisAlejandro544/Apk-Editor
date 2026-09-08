# APK Extractor Mobile

Entorno móvil nativo desarrollado en Android (Kotlin + Jetpack Compose) con motor multilenguaje de alto rendimiento (C, C++, Rust y Lua original en C) y herramientas profesionales de ingeniería inversa para descompresión, auditoría, exploración, desensamblado e inspección de archivos APK directamente en teléfonos inteligentes.

## Problema que resuelve
Permite a desarrolladores, investigadores de seguridad y entusiastas de Android realizar ingeniería inversa, inspección de recursos, extracción de bytecode Dalvik (.dex), descompilación estructural a Java y análisis de librerías nativas directamente en un teléfono sin requerir ordenador, terminal ADB ni permisos de superusuario (root).

## Características Principales
1. **Extracción de APKs Externos**: Selección mediante el Storage Access Framework (SAF) de cualquier archivo `.apk` descargado en el dispositivo.
2. **Extracción de Aplicaciones Instaladas**: Detección y extracción de cualquier app actualmente en uso en el teléfono a través de `sourceDir` del sistema (`QUERY_ALL_PACKAGES`).
3. **Streaming a Caché Anti-Crash**: Descompresión en streaming con búfer estricto de 32 KB directo a la partición de caché temporal, garantizando consumo mínimo de RAM y evitando cierres por falta de memoria (*Out of Memory*).
4. **Visor y Desensamblador de Archivos DEX (`org.smali:dexlib2` & `baksmali`)**:
   - **Bytecode Crudo (Smali)**: Desensamblado exacto de instrucciones Dalvik a sintaxis Smali pura mediante Baksmali 2.5.2.
   - **Descompilador a Java Estructural**: Reconstrucción de clases, paquetes, firmas de métodos, tipos de datos y pseudocódigo Java comprensible para el usuario común.
   - **Explorador de Clases Integrado**: Selector modal en hoja inferior (*bottom sheet*) con buscador y filtrado interactivo entre todas las clases compiladas del archivo DEX.
5. **Visor y Desensamblador de Librerías Nativas ELF (.so) con Goblin y Capstone**:
   - **Análisis de Cabecera y Hardening (Goblin)**: Inspección de arquitectura (ARM64, ARMv7, x86_64, x86), endianness, tipo ELF, entry point y auditoría de mitigaciones de seguridad (PIE, NX/DEP, RELRO y Stack Canaries).
   - **Símbolos y Funciones JNI**: Detección y filtrado de funciones nativas Java (`Java_*`), símbolos exportados dinámicos y dependencias importadas.
   - **Explorador de Símbolos en Modal Bottom Sheet**: Selector con búsqueda interactiva y filtro "⭐ Solo JNI" para navegar entre funciones nativas al igual que con clases DEX.
   - **Dependencias Compartidas (DT_NEEDED)**: Lista de bibliotecas dinámicas vinculadas (libc.so, libm.so, liblog.so, etc.).
   - **Desensamblado ASM (Capstone)**: Desensamblado de código nativo a nivel de instrucciones con offsets y representación mnemónica.
   - **Extractor de Cadenas (Strings)**: Extracción y filtrado de cadenas ASCII embebidas en el binario.
6. **Editor de Código Móvil con Persistencia**: Modificación interactiva de código (Smali, Java, XML, JSON, texto) con numeración de líneas alineada verticalmente, alternancia entre modo lectura y edición, detección de cambios sin guardar y guardado persistente en disco con recálculo automático de hash SHA-256 y tamaño.
7. **Decodificación Automática de AXML (apk-parser)**: Conversión transparente de binarios compilados de Android (`AndroidManifest.xml` y recursos XML) a texto XML legible sin pérdida de etiquetas ni atributos.
8. **Explorador de Archivos Integrado**: Árbol de directorios con migas de pan (*breadcrumbs*), filtrado rápido y categorización visual por tipo de recurso (DEX, Manifiesto, ARSC, Recursos gráficos, Assets, Librerías ELF).
9. **Inspección Hexadecimal (Hex Dump)**: Visor hexadecimal con offsets y representación ASCII para auditoría de bajo nivel de cualquier archivo binario o librería `.so`.
10. **Gestor de Caché y Memoria**: Supervisión de espacio en disco en tiempo real y limpieza segura de proyectos temporales.
11. **Arquitectura Multilenguaje Vinculada (`libapk_native_engine.so`)**:
    - **C (C11)**: Operaciones de bajo nivel, JNI bridge, liberación de memoria (`rust_apk_free_string`) y compresión directa.
    - **C++ (C++17)**: Motor para parsing de bytecode DEX y tablas de recursos.
    - **Rust / Goblin**: ABI nativa con C, motor Goblin para análisis de estructuras binarias ELF y verificación criptográfica.
    - **Capstone**: Motor de desensamblado de instrucciones máquina para arquitecturas móviles.
    - **Lua (5.4.7 C original)**: Intérprete nativo sin capas wrapper intermedias para scripts y parches automatizados.
    - **Compilación Activa**: Vinculado al sistema de compilación de Gradle mediante CMake 3.22.1 y NDK 26.1 con soporte para arquitecturas arm64-v8a y armeabi-v7a.


## Requisitos Previos
- Dispositivo o emulador Android con Android 8.0 (API 26) o superior (recomendado Android 10+ / API 29+).
- Entorno de desarrollo: Android Studio Flamingo / Hedgehog / Iguana / Ladybug o Gradle 8.x.
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
