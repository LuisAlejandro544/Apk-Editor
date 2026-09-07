# APK Extractor Mobile

Entorno móvil nativo desarrollado en Android (Kotlin + Jetpack Compose) con motor multilenguaje de alto rendimiento (C, C++, Rust y Lua original en C) para descompresión, auditoría, exploración y análisis completo de archivos APK en teléfonos inteligentes.

## Problema que resuelve
Permite a desarrolladores, investigadores de seguridad y entusiastas de Android realizar ingeniería inversa, inspección de recursos, extracción de bytecode DEX y análisis de librerías nativas directamente en un teléfono sin requerir ordenador, terminal ADB ni permisos de superusuario (root).

## Características Principales
1. **Extracción de APKs Externos**: Selección mediante el Storage Access Framework (SAF) de cualquier archivo `.apk` descargado en el dispositivo.
2. **Extracción de Aplicaciones Instaladas**: Detección y extracción de cualquier app actualmente en uso en el teléfono a través de `sourceDir` del sistema.
3. **Streaming a Caché Anti-Crash**: Descompresión en streaming con búfer estricto de 32 KB directo a la partición de caché temporal, garantizando consumo mínimo de RAM y evitando cierres por falta de memoria (*Out of Memory*).
4. **Explorador de Archivos Integrado**: Árbol de directorios con migas de pan (*breadcrumbs*), filtrado rápido y categorización visual por tipo de recurso (DEX, Manifiesto, ARSC, Recursos gráficos, Assets, Librerías ELF).
5. **Inspección de Archivos**: Visor hexadecimal (*Hex Dump*) con offsets y representación ASCII, visor de texto plano y cálculo de hash de integridad criptográfica SHA-256.
6. **Gestor de Caché y Memoria**: Supervisión de espacio en disco en tiempo real y limpieza segura de proyectos temporales.
7. **Decodificación Automática de AXML (apk-parser)**: Conversión transparente de binarios compilados de Android (`AndroidManifest.xml` y recursos XML) a texto XML legible sin pérdida de etiquetas ni atributos.
8. **Editor de Líneas de Código en Móvil**: Entorno de edición y visualización con numeración de líneas alineada verticalmente, modo lectura y edición, botón para revertir y guardado persistente en disco con actualización de SHA-256 y tamaño en tiempo real.
9. **Arquitectura Multilenguaje Vinculada (`libapk_native_engine.so`)**:
   - **C (C11)**: Operaciones de bajo nivel, JNI bridge y compresión directa.
   - **C++ (C++17)**: Motor para parsing de bytecode DEX y tablas de recursos.
   - **Rust**: ABI nativa con C y análisis de integridad criptográfica.
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
La app está configurada y adaptada para distribución libre mediante plataformas alternativas como **Uptodown** o descarga directa de APK, sin restricciones de políticas restrictivas de permisos de catálogo.
