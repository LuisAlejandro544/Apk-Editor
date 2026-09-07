# Directrices y Reglas para Agentes (AGENTS.md)

Este archivo define las reglas de comportamiento, restricciones de arquitectura y convenciones de código obligatorias para cualquier agente o desarrollador que modifique este repositorio.

---

## 1. Reglas Generales de Comportamiento
- **Razonar antes de actuar**: Analizar el impacto de cada herramienta antes de ejecutar comandos o ediciones. Justificar técnicamente las decisiones.
- **Respeto a la intención del usuario**: Construir exactamente lo solicitado, sin inventar características decorativas no pedidas.
- **Idioma de documentación y commits**: La información del proyecto, mensajes de commit (como `commit_message.txt` si existe) y comentarios explicativos deben mantenerse en **español**.
- **Seguridad jurídica y marcas**: Evitar el uso en nombres de archivos o paquetes de marcas protegidas por derechos de autor que puedan comprometer al usuario.

## 2. Reglas Específicas del Entorno Android y Móvil
- **Dispositivo Móvil Exclusivo**: Asumir siempre que el usuario final interactúa con la aplicación desde la pantalla táctil de un teléfono móvil, sin PC conectada.
- **Distribución de Terceros (Uptodown)**: La aplicación no está limitada por las políticas de Google Play; no eliminar permisos o capacidades necesarias (como `QUERY_ALL_PACKAGES`) por restricciones de Play Console.
- **No a persist.sys.\***: En caso de trabajar en características de aceleración o gestión de recursos, jamás utilizar propiedades de sistema restringidas como `persist.sys.*`.
- **Integración Obligatoria de C / C++ / Rust / Lua**: Si se integran o solicitan librerías o módulos en C, C++, Rust o Lua, deben estar debidamente incluidos y referenciados en los archivos de compilación (`CMakeLists.txt`, `build.gradle.kts`, `Cargo.toml`). No omitirlos ni sustituirlos con funciones fallback ficticias de Kotlin.
- **Dependencias 100% Funcionales**: No implementar soluciones improvisadas o "sin dependencias" cuando existan bibliotecas probadas de la industria. El peso del APK es secundario frente a la estabilidad.

## 3. Manejo de Archivos y Limpieza
- **Prohibido dejar binarios temporales**: Las carpetas intermedias de compilación local pesadas (como `target/`, carpetas `CMakeFiles/`, o archivos `.so` de prueba en directorios temporales de host) deben limpiarse para mantener el repositorio ordenado.
- **Aislamiento en `.gitignore`**: Mantener actualizadas las reglas que ignoran artefactos `.so`, `.a`, `.o`, `target/`, `.cxx`, y logs de Ninja/CMake.
