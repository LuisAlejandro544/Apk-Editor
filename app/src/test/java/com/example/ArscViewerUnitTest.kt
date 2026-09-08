package com.example

import com.example.data.ArscParser
import com.example.data.ArscResourceItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ArscViewerUnitTest {

  @Test
  fun testArscParserHandlesMissingFileGracefully() {
    val parser = ArscParser()
    val result = parser.parse("/path/to/nonexistent/resources.arsc")

    assertNotNull(result)
    assertTrue(result.packages.isEmpty())
    assertTrue(result.entries.isEmpty())
    assertEquals(0, result.totalEntries)
    assertTrue(result.summaryReport.contains("Error"))
  }

  @Test
  fun testArscResourceItemProperties() {
    val item = ArscResourceItem(
      hexId = "0x7F040001",
      resId = 0x7F040001,
      packageName = "com.example.app",
      typeName = "string",
      entryName = "app_name",
      reference = "@string/app_name",
      value = "Mi Aplicación",
      configsCount = 1
    )

    assertEquals("0x7F040001", item.hexId)
    assertEquals("string", item.typeName)
    assertEquals("app_name", item.entryName)
    assertEquals("@string/app_name", item.reference)
    assertEquals("Mi Aplicación", item.value)
  }
}
