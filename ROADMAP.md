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

---

## Fase 2: Decompilación y Análisis DEX / XML Binario (En Progreso)
- [x] **Decodificador de AXML (apk-parser)**: Conversión y desensamblado transparente de `AndroidManifest.xml` y layouts binarios a XML legible estructurado.
- [x] **Editor de Líneas de Código en Móvil**: Entorno de edición y visualización con numeración de líneas, detección de cambios y persistencia en caché temporal con recálculo de SHA-256.
- [ ] **Parser de `resources.arsc`**: Explorar la tabla de strings, identificadores de recursos e internacionalización.
- [ ] **Desensamblador Smali / Dalvik**: Integración del motor de desensamblado para visualizar métodos e instrucciones de `classes*.dex`.
- [ ] **Análisis de Permisos y Componentes**: Extractor de Activities, Services, Receivers y Providers declarados en la app.

---

## Fase 3: Motor de Scripts en Lua (Automatización y Modding Móvil)
- [ ] **Consola Lua Embebida**: Editor de código en pantalla para ejecutar scripts `.lua` sobre el árbol de archivos extraído.
- [ ] **API de Modding en Lua**: Funciones nativas para buscar y reemplazar strings en archivos DEX, parchear valores booleanos y renombrar recursos.
- [ ] **Gestor de Recetas**: Importación y exportación de scripts comunitarios para aplicar parches conocidos a APKs.

---

## Fase 4: Auditoría de Seguridad y Criptografía con Rust
- [ ] **Verificación de Firmas APK**: Análisis de esquemas de firma v1 (JAR), v2 (APK Signature Scheme), v3 y v4.
- [ ] **Detección de Trackers y Vulnerabilidades**: Escaneo de librerías de terceros (Google Ads, Facebook SDK, Unity, etc.).
- [ ] **Inspector de Librerías Nativas ELF (.so)**: Verificación de protecciones de compilación (PIE, stack canaries, RELRO) mediante el motor Rust.

---

## Fase 5: Reempaquetado y Firma en el Dispositivo
- [ ] **Recompilación ZIP/APK**: Generar un nuevo archivo `.apk` a partir de la carpeta extraída y modificada.
- [ ] **Firma Automática con Llave de Pruebas**: Firma con zipalign y apksigner nativos integrados para permitir la reinstalación inmediata en el teléfono.
