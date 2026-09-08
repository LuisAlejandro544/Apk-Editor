package com.example

import com.example.data.BinaryDataParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.charset.StandardCharsets

class BinaryDataViewerUnitTest {

  @get:Rule
  val tempFolder = TemporaryFolder()

  @Test
  fun testParseHexToBytes_validAndInvalid() {
    val hex = "48 65 6C 6C 6F"
    val result = BinaryDataParser.parseHexToBytes(hex)
    assertTrue(result.isSuccess)
    val bytes = result.getOrThrow()
    assertEquals("Hello", String(bytes, StandardCharsets.UTF_8))

    // Odd length hex should fail cleanly
    val oddHex = "48 65 6C 6C 6"
    val oddResult = BinaryDataParser.parseHexToBytes(oddHex)
    assertFalse(oddResult.isSuccess)
  }

  @Test
  fun testSaveHexAndReadBack() {
    val tempFile = tempFolder.newFile("sample_data.dat")
    val hexInput = "41 42 43 44 45 46 00 01 02"
    val saveResult = BinaryDataParser.saveHexToFile(tempFile, hexInput)
    assertTrue(saveResult.isSuccess)
    assertEquals(9, saveResult.getOrThrow())

    val parseResult = BinaryDataParser.parse(tempFile)
    assertEquals(9L, parseResult.fileSize)
    assertTrue(parseResult.hexFormatted.startsWith("41 42 43 44 45 46"))
  }

  @Test
  fun testSaveTextAndReadBack() {
    val tempFile = tempFolder.newFile("config.bin")
    val textInput = "version=2\napp_name=TestApp\nenabled=true\n"
    val saveResult = BinaryDataParser.saveTextToFile(tempFile, textInput)
    assertTrue(saveResult.isSuccess)

    val parseResult = BinaryDataParser.parse(tempFile)
    assertEquals(textInput, parseResult.textContent)
    assertTrue(parseResult.printableStrings.contains("version=2"))
    assertTrue(parseResult.printableStrings.contains("app_name=TestApp"))
  }

  @Test
  fun testDetectFormat_magicBytes() {
    val sqliteMagic = "SQLite format 3\u0000".toByteArray(StandardCharsets.US_ASCII)
    val detected = BinaryDataParser.detectFormat(sqliteMagic, "application/x-sqlite3", "cache.db")
    assertTrue(detected.contains("SQLite"))

    val zstdMagic = byteArrayOf(0x28.toByte(), 0xB5.toByte(), 0x2F.toByte(), 0xFD.toByte(), 0x00, 0x00)
    val detectedZstd = BinaryDataParser.detectFormat(zstdMagic, "application/octet-stream", "data.dat")
    assertTrue(detectedZstd.contains("Zstandard"))
  }

  @Test
  fun testCalculateShannonEntropy() {
    // All same bytes -> entropy 0.0
    val zeroEntropy = ByteArray(100) { 0x00 }
    val e0 = BinaryDataParser.calculateShannonEntropy(zeroEntropy)
    assertEquals(0.0, e0, 0.01)

    // Varied bytes -> higher entropy
    val varied = ByteArray(256) { it.toByte() }
    val eHigh = BinaryDataParser.calculateShannonEntropy(varied)
    assertTrue(eHigh > 7.9)
  }

  @Test
  fun testExtractPrintableStrings() {
    val bytes = "Header\u0000\u0001\u0002PayloadString\u0000Footer\u0000".toByteArray(StandardCharsets.ISO_8859_1)
    val strings = BinaryDataParser.extractPrintableStrings(bytes, minLength = 4)
    assertTrue(strings.contains("Header"))
    assertTrue(strings.contains("PayloadString"))
    assertTrue(strings.contains("Footer"))
  }

  @Test
  fun testFormatHexForDisplay() {
    val input = "48656c6c6f"
    val formatted = BinaryDataParser.formatHexForDisplay(input)
    assertEquals("48 65 6C 6C 6F", formatted)
  }
}
