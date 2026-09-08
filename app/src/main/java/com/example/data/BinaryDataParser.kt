package com.example.data

import org.apache.tika.Tika
import java.io.File
import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import kotlin.math.log2

data class BinaryDataResult(
  val fileSize: Long,
  val detectedFormat: String,
  val mimeType: String,
  val entropy: Double,
  val entropyDescription: String,
  val hexFormatted: String,
  val hexLines: String,
  val textContent: String,
  val printableStrings: List<String>,
  val isTruncated: Boolean,
  val loadedBytesCount: Int
)

object BinaryDataParser {
  private val tika by lazy { Tika() }

  fun parse(file: File, maxBytes: Int = 128 * 1024): BinaryDataResult {
    if (!file.exists() || file.isDirectory) {
      return BinaryDataResult(
        fileSize = 0L,
        detectedFormat = "No encontrado",
        mimeType = "unknown",
        entropy = 0.0,
        entropyDescription = "Vacío",
        hexFormatted = "",
        hexLines = "",
        textContent = "",
        printableStrings = emptyList(),
        isTruncated = false,
        loadedBytesCount = 0
      )
    }

    val totalSize = file.length()
    val bytesToRead = minOf(totalSize, maxBytes.toLong()).toInt()
    val buffer = ByteArray(bytesToRead)

    var bytesRead = 0
    if (bytesToRead > 0) {
      FileInputStream(file).use { input ->
        bytesRead = input.read(buffer)
      }
    }

    val actualBytes = if (bytesRead > 0) buffer.copyOf(bytesRead) else ByteArray(0)
    val isTruncated = totalSize > actualBytes.size

    // 1. Detección MIME con Tika
    val mimeType = try {
      tika.detect(file)
    } catch (_: Throwable) {
      "application/octet-stream"
    }

    // 2. Detección heurística de formato binario (Magic bytes)
    val detectedFormat = detectFormat(actualBytes, mimeType, file.name)

    // 3. Cálculo de Entropía de Shannon (0.0 a 8.0)
    val entropy = calculateShannonEntropy(actualBytes)
    val entropyDesc = when {
      entropy > 7.5 -> "Alta entropía (Datos Comprimidos o Cifrados)"
      entropy > 6.0 -> "Entropía media-alta (Estructura Binaria densa)"
      entropy > 3.5 -> "Entropía moderada (Texto / Estructuras empaquetadas)"
      entropy > 0.0 -> "Baja entropía (Datos estructurados / Rellenos)"
      else -> "Sin entropía"
    }

    // 4. Hex Formatted (cadena editable de pares hex "XX XX XX...")
    val hexFormatted = actualBytes.joinToString(" ") { String.format("%02X", it) }

    // 5. Hex estructurado con offset y ascii
    val hexLines = formatHexDump(actualBytes)

    // 6. Texto decodificado seguro (UTF-8 con fallback a ISO-8859-1 para preservar 100% de bytes)
    val textContent = safeDecodeText(actualBytes)

    // 7. Extracción de cadenas legibles (min 4 chars imprimibles)
    val printableStrings = extractPrintableStrings(actualBytes, 4)

    return BinaryDataResult(
      fileSize = totalSize,
      detectedFormat = detectedFormat,
      mimeType = mimeType,
      entropy = entropy,
      entropyDescription = entropyDesc,
      hexFormatted = hexFormatted,
      hexLines = hexLines,
      textContent = textContent,
      printableStrings = printableStrings,
      isTruncated = isTruncated,
      loadedBytesCount = actualBytes.size
    )
  }

  fun detectFormat(bytes: ByteArray, mimeType: String, fileName: String): String {
    if (bytes.size >= 16 && bytes.take(15).toByteArray().toString(StandardCharsets.US_ASCII).startsWith("SQLite format 3")) {
      return "Base de Datos SQLite (.db / .sqlite)"
    }
    if (bytes.size >= 4) {
      // Zstandard: 0x28 0xB5 0x2F 0xFD
      if (bytes[0] == 0x28.toByte() && bytes[1] == 0xB5.toByte() && bytes[2] == 0x2F.toByte() && bytes[3] == 0xFD.toByte()) {
        return "Flujo Comprimido Zstandard (Zstd)"
      }
      // GZIP: 0x1F 0x8B
      if (bytes[0] == 0x1F.toByte() && bytes[1] == 0x8B.toByte()) {
        return "Flujo Comprimido GZIP"
      }
      // ZIP / APK / JAR: PK\x03\x04
      if (bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() && bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()) {
        return "Archivo ZIP / Paquete Comprimido"
      }
      // ELF: 0x7F 'E' 'L' 'F'
      if (bytes[0] == 0x7F.toByte() && bytes[1] == 'E'.code.toByte() && bytes[2] == 'L'.code.toByte() && bytes[3] == 'F'.code.toByte()) {
        return "Binario ELF Nativo"
      }
      // DEX: 'd' 'e' 'x' '\n'
      if (bytes[0] == 'd'.code.toByte() && bytes[1] == 'e'.code.toByte() && bytes[2] == 'x'.code.toByte() && bytes[3] == '\n'.code.toByte()) {
        return "Dalvik Executable (DEX)"
      }
      // AXML: 0x03 0x00 0x08 0x00
      if (bytes[0] == 0x03.toByte() && bytes[1] == 0x00.toByte() && bytes[2] == 0x08.toByte() && bytes[3] == 0x00.toByte()) {
        return "Android Binary XML (AXML)"
      }
      // LZ4: 0x04 0x22 0x4D 0x18
      if (bytes[0] == 0x04.toByte() && bytes[1] == 0x22.toByte() && bytes[2] == 0x4D.toByte() && bytes[3] == 0x18.toByte()) {
        return "Flujo Comprimido LZ4 Frame"
      }
      // Java Class: 0xCA 0xFE 0xBA 0xBE
      if (bytes[0] == 0xCA.toByte() && bytes[1] == 0xFE.toByte() && bytes[2] == 0xBA.toByte() && bytes[3] == 0xBE.toByte()) {
        return "Java Bytecode (.class)"
      }
    }

    if (mimeType.contains("brotli")) {
      return "Flujo Comprimido Brotli"
    }
    if (mimeType.contains("protobuf") || fileName.contains("pb", ignoreCase = true)) {
      return "Protocol Buffers (Protobuf)"
    }
    if (mimeType.contains("cbor")) {
      return "Concise Binary Object Representation (CBOR)"
    }
    if (mimeType.contains("msgpack")) {
      return "MessagePack Binary Format"
    }

    if (mimeType != "application/octet-stream" && mimeType.isNotBlank()) {
      return "Formato: $mimeType"
    }

    return "Datos Binarios Crudos (.dat / .bin)"
  }

  fun calculateShannonEntropy(bytes: ByteArray): Double {
    if (bytes.isEmpty()) return 0.0
    val freq = IntArray(256)
    for (b in bytes) {
      freq[b.toInt() and 0xFF]++
    }
    val len = bytes.size.toDouble()
    var entropy = 0.0
    for (count in freq) {
      if (count > 0) {
        val p = count / len
        entropy -= p * log2(p)
      }
    }
    return Math.round(entropy * 100.0) / 100.0
  }

  fun formatHexDump(bytes: ByteArray, maxLines: Int = 120): String {
    val sb = StringBuilder()
    var offset = 0
    var lines = 0
    while (offset < bytes.size && lines < maxLines) {
      val chunkLen = minOf(16, bytes.size - offset)
      sb.append(String.format("%08X  ", offset))
      for (i in 0 until 16) {
        if (i < chunkLen) {
          sb.append(String.format("%02X ", bytes[offset + i]))
        } else {
          sb.append("   ")
        }
        if (i == 7) sb.append(" ")
      }
      sb.append(" |")
      for (i in 0 until chunkLen) {
        val b = bytes[offset + i].toInt() and 0xFF
        if (b in 32..126) {
          sb.append(b.toChar())
        } else {
          sb.append('.')
        }
      }
      sb.append("|\n")
      offset += chunkLen
      lines++
    }
    if (offset < bytes.size) {
      sb.append(String.format("... [%d bytes restantes]", bytes.size - offset))
    }
    return sb.toString()
  }

  fun extractPrintableStrings(bytes: ByteArray, minLength: Int = 4, maxStrings: Int = 200): List<String> {
    val list = mutableListOf<String>()
    val current = StringBuilder()
    for (b in bytes) {
      val ch = b.toInt() and 0xFF
      if (ch in 32..126) {
        current.append(ch.toChar())
      } else {
        if (current.length >= minLength) {
          list.add(current.toString())
          if (list.size >= maxStrings) break
        }
        current.setLength(0)
      }
    }
    if (current.length >= minLength && list.size < maxStrings) {
      list.add(current.toString())
    }
    return list
  }

  fun safeDecodeText(bytes: ByteArray): String {
    return try {
      val utf8 = String(bytes, StandardCharsets.UTF_8)
      if (utf8.count { it == '\uFFFD' } > bytes.size * 0.05) {
        String(bytes, StandardCharsets.ISO_8859_1)
      } else {
        utf8
      }
    } catch (_: Throwable) {
      String(bytes, StandardCharsets.ISO_8859_1)
    }
  }

  fun parseHexToBytes(hexString: String): Result<ByteArray> {
    return try {
      val clean = hexString.replace(Regex("[^0-9A-Fa-f]"), "")
      if (clean.length % 2 != 0) {
        return Result.failure(IllegalArgumentException("Longitud de hex debe ser par (cada byte requiere 2 dígitos hexadecimales). Longitud: ${clean.length}"))
      }
      val byteArray = ByteArray(clean.length / 2)
      for (i in 0 until clean.length step 2) {
        val byteStr = clean.substring(i, i + 2)
        byteArray[i / 2] = byteStr.toInt(16).toByte()
      }
      Result.success(byteArray)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun saveHexToFile(file: File, hexString: String): Result<Int> {
    return try {
      val parseResult = parseHexToBytes(hexString)
      if (parseResult.isFailure) {
        return Result.failure(parseResult.exceptionOrNull() ?: Exception("Error convirtiendo hexadecimal"))
      }
      val bytes = parseResult.getOrThrow()
      file.writeBytes(bytes)
      Result.success(bytes.size)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun saveTextToFile(file: File, text: String): Result<Int> {
    return try {
      val bytes = text.toByteArray(StandardCharsets.UTF_8)
      file.writeBytes(bytes)
      Result.success(bytes.size)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  fun formatHexForDisplay(hexString: String): String {
    val clean = hexString.replace(Regex("[^0-9A-Fa-f]"), "").uppercase()
    val sb = StringBuilder()
    for (i in 0 until clean.length step 2) {
      if (i + 2 <= clean.length) {
        sb.append(clean.substring(i, i + 2)).append(" ")
      } else {
        sb.append(clean.substring(i)).append(" ")
      }
      if ((i / 2 + 1) % 16 == 0) {
        sb.append("\n")
      }
    }
    return sb.toString().trim()
  }
}
