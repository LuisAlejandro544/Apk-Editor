package com.example.model

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class FileCategory(val label: String, val badgeColorHex: Long) {
  MANIFEST("Manifest", 0xFFFFB703),
  DEX_BYTECODE("DEX Bytecode", 0xFF00B4D8),
  RESOURCES_ARSC("ARSC Tables", 0xFF9D4EDD),
  COMPILED_RES("Recurso / Res", 0xFF48CAE4),
  ASSET("Asset", 0xFF06D6A0),
  NATIVE_LIBRARY("ELF Nativo (.so)", 0xFFEF476F),
  SIGNATURE_META("Firma / Meta-Inf", 0xFF8338EC),
  CODE_OR_SCRIPT("Código / Config", 0xFF3A86FF),
  IMAGE("Imagen", 0xFF2EC4B6),
  AUDIO("Audio", 0xFFFF006E),
  OTHER("Archivo", 0xFF94A3B8)
}

data class ApkProject(
  val id: String,
  val name: String,
  val originalFileName: String,
  val extractedDirPath: String,
  val fileCount: Int,
  val totalSizeBytes: Long,
  val extractedTimestamp: Long
) {
  val formattedDate: String
    get() = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(extractedTimestamp))

  val formattedSize: String
    get() = formatBytes(totalSizeBytes)
}

data class ExtractedFileItem(
  val name: String,
  val relativePath: String,
  val absolutePath: String,
  val isDirectory: Boolean,
  val sizeBytes: Long,
  val category: FileCategory,
  val childCount: Int = 0
) {
  val formattedSize: String
    get() = if (isDirectory) "$childCount elementos" else formatBytes(sizeBytes)
}

data class ExtractionProgress(
  val isActive: Boolean = false,
  val currentFileName: String = "",
  val extractedFilesCount: Int = 0,
  val bytesExtracted: Long = 0L,
  val isCompleted: Boolean = false,
  val error: String? = null,
  val projectId: String? = null
) {
  val formattedBytes: String
    get() = formatBytes(bytesExtracted)
}

data class StorageInfo(
  val cacheUsedBytes: Long,
  val availableDiskBytes: Long,
  val projectsCount: Int
) {
  val formattedCacheUsed: String
    get() = formatBytes(cacheUsedBytes)

  val formattedAvailable: String
    get() = formatBytes(availableDiskBytes)
}

fun formatBytes(bytes: Long): String {
  if (bytes <= 0) return "0 B"
  val units = arrayOf("B", "KB", "MB", "GB", "TB")
  val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt().coerceIn(0, units.size - 1)
  return String.format(Locale.US, "%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}
