package com.example.data

import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.util.zip.InflaterInputStream

object DecompressionService {

  fun tryDecompressStream(bytes: ByteArray, maxOutputBytes: Int = 10 * 1024 * 1024): Result<Pair<String, ByteArray>> {
    if (bytes.isEmpty()) return Result.failure(IllegalArgumentException("Bytes vacíos"))

    // 1. Detección por Commons Compress
    try {
      ByteArrayInputStream(bytes).use { bais ->
        BufferedInputStream(bais).use { bis ->
          val compressorName = CompressorStreamFactory.detect(bis)
          val uncompressedStream = CompressorStreamFactory().createCompressorInputStream(compressorName, bis)
          val out = ByteArrayOutputStream()
          val buf = ByteArray(16 * 1024)
          var read: Int
          var total = 0
          while (uncompressedStream.read(buf).also { read = it } != -1) {
            out.write(buf, 0, read)
            total += read
            if (total > maxOutputBytes) break
          }
          return Result.success(Pair(compressorName.uppercase(), out.toByteArray()))
        }
      }
    } catch (_: Throwable) {
      // Continuar con otros algoritmos si detect falla
    }

    // 2. GZIP directo
    if (bytes.size >= 2 && bytes[0] == 0x1F.toByte() && bytes[1] == 0x8B.toByte()) {
      try {
        GZIPInputStream(ByteArrayInputStream(bytes)).use { gis ->
          val out = ByteArrayOutputStream()
          val buf = ByteArray(16 * 1024)
          var read: Int
          var total = 0
          while (gis.read(buf).also { read = it } != -1) {
            out.write(buf, 0, read)
            total += read
            if (total > maxOutputBytes) break
          }
          return Result.success(Pair("GZIP", out.toByteArray()))
        }
      } catch (_: Throwable) {}
    }

    // 3. Zlib / Deflate (común en AppsFlyer y SDKs ofuscados)
    // Cabeceras típicas zlib: 0x78 0x01, 0x78 0x9C, 0x78 0xDA, 0x78 0x5E
    if (bytes.size >= 2 && bytes[0] == 0x78.toByte() &&
      (bytes[1] == 0x9C.toByte() || bytes[1] == 0x01.toByte() || bytes[1] == 0xDA.toByte() || bytes[1] == 0x5E.toByte())
    ) {
      try {
        InflaterInputStream(ByteArrayInputStream(bytes)).use { iis ->
          val out = ByteArrayOutputStream()
          val buf = ByteArray(16 * 1024)
          var read: Int
          var total = 0
          while (iis.read(buf).also { read = it } != -1) {
            out.write(buf, 0, read)
            total += read
            if (total > maxOutputBytes) break
          }
          return Result.success(Pair("ZLIB / DEFLATE", out.toByteArray()))
        }
      } catch (_: Throwable) {}
    }

    // 4. Raw Deflate (sin header zlib)
    try {
      val inflater = java.util.zip.Inflater(true)
      inflater.setInput(bytes)
      val out = ByteArrayOutputStream()
      val buf = ByteArray(16 * 1024)
      while (!inflater.finished()) {
        val count = inflater.inflate(buf)
        if (count == 0 && inflater.needsInput()) break
        if (count == 0 && inflater.needsDictionary()) break
        out.write(buf, 0, count)
        if (out.size() > maxOutputBytes) break
      }
      inflater.end()
      val res = out.toByteArray()
      if (res.isNotEmpty() && res.size != bytes.size) {
        return Result.success(Pair("RAW DEFLATE", res))
      }
    } catch (_: Throwable) {}

    return Result.failure(Exception("No se reconoció un flujo comprimido estándar."))
  }

  fun decompressFile(file: File): Result<Pair<String, ByteArray>> {
    if (!file.exists()) return Result.failure(Exception("Archivo no existe"))
    val bytes = file.readBytes()
    return tryDecompressStream(bytes)
  }
}
