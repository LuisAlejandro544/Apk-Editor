package com.example.data

import proguard.retrace.ReTrace
import java.io.File
import java.io.LineNumberReader
import java.io.PrintWriter
import java.io.StringReader
import java.io.StringWriter

object ProguardRetraceService {

  fun retraceWithMappingFile(obfuscatedText: String, mappingFile: File): String {
    if (obfuscatedText.isBlank() || !mappingFile.exists()) return obfuscatedText

    return try {
      val reader = LineNumberReader(StringReader(obfuscatedText))
      val stringWriter = StringWriter()
      val writer = PrintWriter(stringWriter)

      val retrace = ReTrace(
        ReTrace.REGULAR_EXPRESSION,
        ReTrace.REGULAR_EXPRESSION2,
        false, // verbose
        false, // regex
        mappingFile
      )
      retrace.retrace(reader, writer)
      writer.flush()
      stringWriter.toString()
    } catch (e: Throwable) {
      "// Error en ProGuard Retrace: ${e.localizedMessage}\n$obfuscatedText"
    }
  }

  fun retrace(obfuscatedText: String, mappingContent: String, cacheDir: File): String {
    if (obfuscatedText.isBlank()) return ""
    if (mappingContent.isBlank()) return obfuscatedText

    return try {
      val tempMapping = File.createTempFile("mapping_", ".txt", cacheDir)
      tempMapping.writeText(mappingContent, Charsets.UTF_8)
      try {
        retraceWithMappingFile(obfuscatedText, tempMapping)
      } finally {
        tempMapping.delete()
      }
    } catch (e: Throwable) {
      "// Error en ProGuard Retrace: ${e.localizedMessage}\n$obfuscatedText"
    }
  }
}
