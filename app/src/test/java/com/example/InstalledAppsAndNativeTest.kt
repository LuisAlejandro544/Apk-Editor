package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.ApkExtractorRepository
import com.example.model.InstalledAppItem
import com.example.nativebridge.NativeEngineBridge
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class InstalledAppsAndNativeTest {

  @Test
  fun `read installed packages through repository`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = ApkExtractorRepository(context)

    val installedApps = repository.getInstalledApps()
    // In Robolectric environment, PackageManager provides test packages
    assertNotNull(installedApps)
  }

  @Test
  fun `native engine status reports multilanguage stack components`() {
    val status = NativeEngineBridge.getEngineStatusString()
    assertTrue("Should mention C", status.contains("C"))
    assertTrue("Should mention C++", status.contains("C++"))
    assertTrue("Should mention Rust", status.contains("Rust"))
    assertTrue("Should mention Lua", status.contains("Lua"))
  }

  @Test
  fun `installed app item format size`() {
    val item = InstalledAppItem(
      packageName = "com.test.target",
      appName = "Test Target",
      versionName = "1.0.0",
      versionCode = 100L,
      apkPath = "/data/app/test.apk",
      apkSizeBytes = 1024 * 1024 * 5L, // 5 MB
      isSystemApp = false
    )
    assertEquals("5.00 MB", item.formattedSize)
  }

  @Test
  fun `save and read file content allows editing lines of code`() {
    runBlocking {
      val context = ApplicationProvider.getApplicationContext<Context>()
      val repository = ApkExtractorRepository(context)

      val tempFile = java.io.File(context.cacheDir, "test_manifest.xml")
      val initialXml = "<manifest package=\"com.test.sample\">\n  <application android:label=\"Original\"/>\n</manifest>"
      tempFile.writeText(initialXml)

      val readInitial = repository.readFileContent(tempFile.absolutePath)
      assertEquals(initialXml, readInitial)

      // Simular edición de una línea de código
      val updatedXml = "<manifest package=\"com.test.sample\">\n  <application android:label=\"Modified\"/>\n</manifest>"
      val saveResult = repository.saveFileContent(tempFile.absolutePath, updatedXml)
      assertTrue("Debe guardar el archivo correctamente", saveResult)

      val readUpdated = repository.readFileContent(tempFile.absolutePath)
      assertEquals(updatedXml, readUpdated)
      assertTrue(readUpdated.contains("Modified"))

      tempFile.delete()
    }
  }
}
