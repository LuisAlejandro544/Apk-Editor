# AI Context (Contexto de Inteligencia Artificial)

Este documento condensa la información fundamental que cualquier modelo de IA o agente autónomo necesita para comprender el propósito, diseño, restricciones y estado técnico del proyecto.

---

## 1. Misión del Proyecto
Construir una suite completa de extracción, inspección, auditoría y análisis de paquetes APK que opere **100% de manera autónoma en un teléfono móvil Android**.

## 2. Perfil del Usuario
- El usuario opera exclusivamente desde un **dispositivo móvil (teléfono Android)**, sin acceso a una PC ni emulador de escritorio.
- El canal de distribución prioritario es **Uptodown** o distribución independiente de APKs, no Google Play Store.
- Prioridad total a la **funcionalidad completa de las dependencias** por encima de optimizaciones agresivas de tamaño final del APK. No usar soluciones recortadas sin dependencias si existen librerías consolidadas.

## 3. Principios Técnicos Innegociables
1. **Prevención de Caídas de Memoria (Anti-Crash OOM)**:
   - Todo archivo extraído de un APK se descomprime en **streaming** con un búfer estricto de 32 KB directo a la partición de almacenamiento en caché temporal (`context.cacheDir`).
   - Está terminantemente prohibido almacenar el contenido completo de un archivo APK o sus ficheros descomprimidos en arrays de bytes en memoria RAM.
2. **Navegación Móvil Óptima**:
   - Evitar modales o diálogos emergentes bloqueantes sobrecargados. Las pantallas principales deben ser destinos completos de navegación con soporte natural para el botón 'Atrás' del sistema Android.
3. **Pila Tecnológica Multilenguaje**:
   - **Kotlin + Jetpack Compose**: Toda la capa de presentación y orquestación.
   - **C (C11)**: Integrado en el NDK para rutinas de enlace JNI.
   - **C++ (C++17)**: Procesamiento de estructuras binarias.
   - **Lua (C puro 5.4.7 oficial)**: Compilado nativamente en C, sin wrappers innecesarios ni código simulado.
   - **Rust**: Crate ubicado en `app/src/main/cpp/rust_core` para módulos criptográficos y verificación de integridad.
4. **Almacenamiento y Limpieza**:
   - El usuario cuenta con un monitor de almacenamiento dedicado (`CacheManagerScreen`) que calcula el peso de cada proyecto y permite la purga individual o total de los archivos temporales.
5. **Decodificación y Edición de Recursos (AXML)**:
   - Uso de librerías consolidadas de la industria (`com.jaredrummler:apk-parser:1.0.2`) para transformar el formato binario `AndroidManifest.xml` en texto plano estructurado.
   - Entorno de edición táctil con numeración de líneas que permite alterar el código y persistir las modificaciones en caché, manteniendo actualizados los hashes SHA-256 y metadatos sin corromper el almacenamiento.
