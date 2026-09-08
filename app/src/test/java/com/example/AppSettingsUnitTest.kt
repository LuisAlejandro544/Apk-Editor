package com.example

import com.example.model.AppThemeMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsUnitTest {

  @Test
  fun appThemeMode_containsRequiredValues() {
    val modes = AppThemeMode.values()
    assertEquals(2, modes.size)
    assertTrue(modes.contains(AppThemeMode.CYBER_DARK))
    assertTrue(modes.contains(AppThemeMode.MATERIAL_YOU))
  }

  @Test
  fun appThemeMode_cyberDark_metadataValid() {
    val cyberDark = AppThemeMode.CYBER_DARK
    assertEquals("Color Predeterminado (Cyber Dark)", cyberDark.title)
    assertTrue(cyberDark.subtitle.isNotEmpty())
    assertTrue(cyberDark.description.contains("ingeniería inversa", ignoreCase = true))
  }

  @Test
  fun appThemeMode_materialYou_metadataValid() {
    val materialYou = AppThemeMode.MATERIAL_YOU
    assertEquals("Material You (Color Dinámico)", materialYou.title)
    assertTrue(materialYou.subtitle.contains("Monet", ignoreCase = true))
    assertTrue(materialYou.description.contains("fondo de pantalla", ignoreCase = true))
  }

  @Test
  fun appThemeMode_valueOfMatchesName() {
    assertEquals(AppThemeMode.CYBER_DARK, AppThemeMode.valueOf("CYBER_DARK"))
    assertEquals(AppThemeMode.MATERIAL_YOU, AppThemeMode.valueOf("MATERIAL_YOU"))
  }
}
