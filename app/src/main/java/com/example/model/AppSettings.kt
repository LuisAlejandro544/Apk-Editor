package com.example.model

enum class AppThemeMode(
  val title: String,
  val subtitle: String,
  val description: String
) {
  CYBER_DARK(
    title = "Color Predeterminado (Cyber Dark)",
    subtitle = "Paleta técnica de alta fidelidad",
    description = "Esquema oscuro optimizado para ingeniería inversa, visores hexadecimales y análisis de código (Azul Pizarra, Cian, Menta y Ámbar)."
  ),
  MATERIAL_YOU(
    title = "Material You (Color Dinámico)",
    subtitle = "Integración con Monet de Android 12+",
    description = "Extrae y adapta la paleta cromática a partir del fondo de pantalla de tu dispositivo móvil, armonizando barras, tarjetas y controles."
  )
}
