package com.example.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.model.ApkProject
import com.example.model.ExtractedFileItem
import com.example.model.ExtractionProgress
import com.example.model.FileCategory
import com.example.model.StorageInfo
import com.example.model.formatBytes
import com.jaredrummler.apkparser.parser.BinaryXmlParser
import com.jaredrummler.apkparser.parser.XmlTranslator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class ApkExtractorRepository(private val context: Context) {

  private val workspacesDir: File
    get() = File(context.cacheDir, "apk_workspaces").apply { if (!exists()) mkdirs() }

  fun getFileName(uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
      val cursor = context.contentResolver.query(uri, null, null, null, null)
      cursor?.use {
        if (it.moveToFirst()) {
          val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            result = it.getString(nameIndex)
          }
        }
      }
    }
    if (result == null) {
      result = uri.path
      val cut = result?.lastIndexOf('/') ?: -1
      if (cut != -1) {
        result = result?.substring(cut + 1)
      }
    }
    return result ?: "app_package.apk"
  }

  /**
   * Extracts all files from the APK using a 32 KB buffered stream directly into cache storage.
   * This guarantees that memory consumption remains minimal (O(1) RAM usage regardless of APK size).
   */
  fun extractApk(apkUri: Uri, originalName: String): Flow<ExtractionProgress> = flow {
    emit(
      ExtractionProgress(
        isActive = true,
        currentFileName = "Iniciando búfer de caché seguro...",
        extractedFilesCount = 0,
        bytesExtracted = 0L,
        isCompleted = false
      )
    )

    val projectId = "apk_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().take(6)
    val projectDir = File(workspacesDir, projectId)
    val extractedDir = File(projectDir, "extracted")

    if (!extractedDir.exists()) {
      extractedDir.mkdirs()
    }

    var extractedCount = 0
    var totalBytesWritten = 0L
    val buffer = ByteArray(32 * 1024) // 32 KB chunk stream for RAM safety

    try {
      val inputStream: InputStream? = context.contentResolver.openInputStream(apkUri)
        ?: throw IllegalStateException("No se pudo abrir el archivo APK seleccionado.")

      BufferedInputStream(inputStream).use { bufferedIn ->
        ZipInputStream(bufferedIn).use { zipIn ->
          var entry: ZipEntry? = zipIn.nextEntry
          var lastProgressUpdate = System.currentTimeMillis()

          while (entry != null) {
            val entryName = entry.name

            // Path Traversal Security check
            val destinationFile = File(extractedDir, entryName)
            val destCanonical = destinationFile.canonicalPath
            val targetCanonical = extractedDir.canonicalPath

            if (!destCanonical.startsWith(targetCanonical)) {
              // Ignore malicious or invalid entry paths escaping root
              zipIn.closeEntry()
              entry = zipIn.nextEntry
              continue
            }

            if (entry.isDirectory) {
              destinationFile.mkdirs()
            } else {
              destinationFile.parentFile?.mkdirs()
              BufferedOutputStream(FileOutputStream(destinationFile)).use { fileOut ->
                var len: Int
                while (zipIn.read(buffer).also { len = it } > 0) {
                  fileOut.write(buffer, 0, len)
                  totalBytesWritten += len
                }
              }
              extractedCount++

              // Periodic progress report to avoid flooding Compose state
              val now = System.currentTimeMillis()
              if (now - lastProgressUpdate > 120 || extractedCount % 50 == 0) {
                lastProgressUpdate = now
                emit(
                  ExtractionProgress(
                    isActive = true,
                    currentFileName = entryName,
                    extractedFilesCount = extractedCount,
                    bytesExtracted = totalBytesWritten,
                    isCompleted = false,
                    projectId = projectId
                  )
                )
              }
            }

            zipIn.closeEntry()
            entry = zipIn.nextEntry
          }
        }
      }

      // Save metadata manifest file in project
      saveProjectMeta(
        projectDir = projectDir,
        id = projectId,
        originalName = originalName,
        extractedCount = extractedCount,
        totalBytes = totalBytesWritten
      )

      emit(
        ExtractionProgress(
          isActive = false,
          currentFileName = "Extracción completada con éxito",
          extractedFilesCount = extractedCount,
          bytesExtracted = totalBytesWritten,
          isCompleted = true,
          projectId = projectId
        )
      )
    } catch (e: Exception) {
      // Clean up failed partial extraction to keep phone storage pristine
      projectDir.deleteRecursively()
      emit(
        ExtractionProgress(
          isActive = false,
          currentFileName = "",
          extractedFilesCount = extractedCount,
          bytesExtracted = totalBytesWritten,
          isCompleted = false,
          error = "Error durante la extracción: ${e.localizedMessage ?: e.message}"
        )
      )
    }
  }.flowOn(Dispatchers.IO)

  private fun saveProjectMeta(
    projectDir: File,
    id: String,
    originalName: String,
    extractedCount: Int,
    totalBytes: Long
  ) {
    val metaFile = File(projectDir, "project_meta.txt")
    metaFile.writeText(
      """
      id=$id
      name=${originalName.removeSuffix(".apk")}
      originalFileName=$originalName
      fileCount=$extractedCount
      totalSizeBytes=$totalBytes
      extractedTimestamp=${System.currentTimeMillis()}
      """.trimIndent()
    )
  }

  suspend fun listProjects(): List<ApkProject> = withContext(Dispatchers.IO) {
    val projects = mutableListOf<ApkProject>()
    val dirs = workspacesDir.listFiles() ?: return@withContext emptyList()

    for (dir in dirs) {
      if (!dir.isDirectory) continue
      val metaFile = File(dir, "project_meta.txt")
      val extractedDir = File(dir, "extracted")
      if (metaFile.exists()) {
        try {
          val props = metaFile.readLines().associate { line ->
            val parts = line.split("=", limit = 2)
            if (parts.size == 2) parts[0].trim() to parts[1].trim() else "" to ""
          }
          projects.add(
            ApkProject(
              id = props["id"] ?: dir.name,
              name = props["name"] ?: dir.name,
              originalFileName = props["originalFileName"] ?: "${dir.name}.apk",
              extractedDirPath = extractedDir.absolutePath,
              fileCount = props["fileCount"]?.toIntOrNull() ?: 0,
              totalSizeBytes = props["totalSizeBytes"]?.toLongOrNull() ?: calculateFolderSize(extractedDir),
              extractedTimestamp = props["extractedTimestamp"]?.toLongOrNull() ?: dir.lastModified()
            )
          )
        } catch (_: Exception) {
          // Fallback if metadata is corrupted
        }
      }
    }
    projects.sortedByDescending { it.extractedTimestamp }
  }

  suspend fun getProjectById(projectId: String): ApkProject? = withContext(Dispatchers.IO) {
    listProjects().find { it.id == projectId }
  }

  suspend fun getDirectoryContents(
    projectId: String,
    relativeSubPath: String = ""
  ): List<ExtractedFileItem> = withContext(Dispatchers.IO) {
    val projectDir = File(workspacesDir, projectId)
    val extractedRoot = File(projectDir, "extracted")
    val targetDir = if (relativeSubPath.isEmpty() || relativeSubPath == "/") {
      extractedRoot
    } else {
      File(extractedRoot, relativeSubPath)
    }

    if (!targetDir.exists() || !targetDir.isDirectory) {
      return@withContext emptyList()
    }

    val files = targetDir.listFiles() ?: return@withContext emptyList()

    val items = files.map { file ->
      val relPath = file.relativeTo(extractedRoot).path.replace('\\', '/')
      val isDir = file.isDirectory
      val childCount = if (isDir) (file.listFiles()?.size ?: 0) else 0
      val size = if (isDir) 0L else file.length()
      val category = categorizeFile(file.name, isDir)

      ExtractedFileItem(
        name = file.name,
        relativePath = relPath,
        absolutePath = file.absolutePath,
        isDirectory = isDir,
        sizeBytes = size,
        category = category,
        childCount = childCount
      )
    }

    // Sort: directories first, then alphabetical
    items.sortedWith(
      compareByDescending<ExtractedFileItem> { it.isDirectory }
        .thenBy { it.name.lowercase() }
    )
  }

  private fun categorizeFile(name: String, isDir: Boolean): FileCategory {
    if (isDir) {
      return when (name.lowercase()) {
        "assets" -> FileCategory.ASSET
        "res" -> FileCategory.COMPILED_RES
        "lib" -> FileCategory.NATIVE_LIBRARY
        "meta-inf" -> FileCategory.SIGNATURE_META
        else -> FileCategory.OTHER
      }
    }

    val lower = name.lowercase()
    return when {
      lower == "androidmanifest.xml" -> FileCategory.MANIFEST
      lower.startsWith("classes") && lower.endsWith(".dex") -> FileCategory.DEX_BYTECODE
      lower == "resources.arsc" -> FileCategory.RESOURCES_ARSC
      lower.endsWith(".so") -> FileCategory.NATIVE_LIBRARY
      lower.endsWith(".xml") -> FileCategory.COMPILED_RES
      lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".webp") || lower.endsWith(".gif") -> FileCategory.IMAGE
      lower.endsWith(".json") || lower.endsWith(".txt") || lower.endsWith(".properties") || lower.endsWith(".js") -> FileCategory.CODE_OR_SCRIPT
      lower.startsWith("cert.") || lower.startsWith("manifest.mf") || lower.endsWith(".rsa") || lower.endsWith(".dsa") -> FileCategory.SIGNATURE_META
      else -> FileCategory.OTHER
    }
  }

  suspend fun deleteProject(projectId: String): Boolean = withContext(Dispatchers.IO) {
    val projectDir = File(workspacesDir, projectId)
    if (projectDir.exists()) {
      projectDir.deleteRecursively()
    } else {
      false
    }
  }

  suspend fun clearAllWorkspaces(): Boolean = withContext(Dispatchers.IO) {
    workspacesDir.deleteRecursively()
    workspacesDir.mkdirs()
  }

  suspend fun getStorageInfo(): StorageInfo = withContext(Dispatchers.IO) {
    val cacheUsed = calculateFolderSize(workspacesDir)
    val freeSpace = context.cacheDir.usableSpace
    val projects = listProjects().size
    StorageInfo(
      cacheUsedBytes = cacheUsed,
      availableDiskBytes = freeSpace,
      projectsCount = projects
    )
  }

  suspend fun readFileContent(filePath: String, maxBytes: Int = 256 * 1024): String = withContext(Dispatchers.IO) {
    val file = File(filePath)
    if (!file.exists() || file.isDirectory) return@withContext "El archivo no existe o es una carpeta."

    // 1. Si es un archivo XML (por extensión o contenido), decodificar AXML binario con apk-parser
    if (file.name.endsWith(".xml", ignoreCase = true)) {
      try {
        val bytes = file.readBytes()
        val decoded = decodeAxml(bytes)
        if (decoded != null) {
          return@withContext decoded
        }
      } catch (_: Exception) {
        // Continuar con lectura estándar si no era AXML binario
      }
    }

    try {
      FileInputStream(file).use { input ->
        val buffer = ByteArray(minOf(file.length(), maxBytes.toLong()).toInt())
        val read = input.read(buffer)
        if (read > 0) {
          // Detectar si los primeros bytes corresponden al header de Android Binary XML (0x00080003)
          if (read >= 4 && buffer[0] == 0x03.toByte() && buffer[1] == 0x00.toByte() && buffer[2] == 0x08.toByte() && buffer[3] == 0x00.toByte()) {
            val allBytes = file.readBytes()
            val decoded = decodeAxml(allBytes)
            if (decoded != null) return@withContext decoded
          }

          // Verificar si es texto legible
          val sampleSize = minOf(read, 256)
          val isPrintable = buffer.take(sampleSize).all {
            it in 32..126 || it == '\n'.code.toByte() || it == '\r'.code.toByte() || it == '\t'.code.toByte()
          }
          if (isPrintable) {
            String(buffer, 0, read, Charsets.UTF_8)
          } else {
            // Representación de formato binario
            "Este archivo contiene datos binarios compilados (${formatBytes(file.length())}).\nUsa la pestaña 'Vista Hexadecimal' para inspeccionar su estructura de bytes."
          }
        } else {
          "[Archivo vacío]"
        }
      }
    } catch (e: Exception) {
      "Error leyendo archivo: ${e.localizedMessage}"
    }
  }

  private fun decodeAxml(bytes: ByteArray): String? {
    return try {
      val byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
      val parser = BinaryXmlParser(byteBuffer, null)
      val translator = XmlTranslator()
      parser.setXmlStreamer(translator)
      parser.parse()
      val result = translator.xml
      if (result.isNullOrBlank()) null else result
    } catch (_: Exception) {
      null
    }
  }

  suspend fun saveFileContent(filePath: String, content: String): Boolean = withContext(Dispatchers.IO) {
    try {
      val file = File(filePath)
      if (!file.exists() || file.isDirectory) return@withContext false
      file.writeText(content, Charsets.UTF_8)
      true
    } catch (_: Exception) {
      false
    }
  }

  suspend fun readHexDump(filePath: String, maxLines: Int = 120): String = withContext(Dispatchers.IO) {
    val file = File(filePath)
    if (!file.exists() || file.isDirectory) return@withContext "Archivo no accesible."

    val sb = StringBuilder()
    try {
      FileInputStream(file).use { input ->
        val rowBuffer = ByteArray(16)
        var offset = 0L
        var linesRead = 0

        while (linesRead < maxLines) {
          val count = input.read(rowBuffer)
          if (count <= 0) break

          // Offset
          sb.append(String.format("%08X  ", offset))

          // Hex representation
          for (i in 0 until 16) {
            if (i < count) {
              sb.append(String.format("%02X ", rowBuffer[i]))
            } else {
              sb.append("   ")
            }
            if (i == 7) sb.append(" ")
          }

          // ASCII representation
          sb.append(" |")
          for (i in 0 until count) {
            val b = rowBuffer[i]
            if (b in 32..126) {
              sb.append(b.toInt().toChar())
            } else {
              sb.append('.')
            }
          }
          sb.append("|\n")

          offset += count
          linesRead++
        }

        if (input.available() > 0) {
          sb.append("\n... [${formatBytes(file.length() - offset)} restantes en disco]")
        }
      }
    } catch (e: Exception) {
      sb.append("Error generando volcado hexadecimal: ${e.localizedMessage}")
    }

    sb.toString()
  }

  suspend fun calculateSha256(filePath: String): String = withContext(Dispatchers.IO) {
    val file = File(filePath)
    if (!file.exists()) return@withContext "N/A"
    try {
      val digest = MessageDigest.getInstance("SHA-256")
      val buffer = ByteArray(32 * 1024)
      FileInputStream(file).use { input ->
        var read: Int
        while (input.read(buffer).also { read = it } > 0) {
          digest.update(buffer, 0, read)
        }
      }
      digest.digest().joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
      "Error hash: ${e.localizedMessage}"
    }
  }

  private fun calculateFolderSize(file: File): Long {
    if (!file.exists()) return 0L
    if (!file.isDirectory) return file.length()
    var size = 0L
    val children = file.listFiles() ?: return 0L
    for (child in children) {
      size += calculateFolderSize(child)
    }
    return size
  }

  /**
   * Retrieves all installed applications on the device that have valid APK files.
   */
  suspend fun getInstalledApps(): List<com.example.model.InstalledAppItem> = withContext(Dispatchers.IO) {
    val pm = context.packageManager
    val packages = try {
      pm.getInstalledPackages(0)
    } catch (e: Exception) {
      emptyList()
    }

    val list = mutableListOf<com.example.model.InstalledAppItem>()
    val currentPkg = context.packageName

    for (pkg in packages) {
      val appInfo = pkg.applicationInfo ?: continue
      val sourceDir = appInfo.sourceDir ?: continue
      val apkFile = File(sourceDir)
      if (!apkFile.exists() || !apkFile.canRead()) continue

      val appName = try {
        pm.getApplicationLabel(appInfo).toString()
      } catch (e: Exception) {
        pkg.packageName
      }

      val icon = try {
        pm.getApplicationIcon(appInfo)
      } catch (e: Exception) {
        null
      }

      val isSystem = (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
      val size = apkFile.length()

      val splits = appInfo.splitSourceDirs?.toList() ?: emptyList()

      list.add(
        com.example.model.InstalledAppItem(
          packageName = pkg.packageName,
          appName = appName,
          versionName = pkg.versionName ?: "1.0",
          versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            pkg.longVersionCode
          } else {
            @Suppress("DEPRECATION")
            pkg.versionCode.toLong()
          },
          apkPath = sourceDir,
          apkSizeBytes = size,
          isSystemApp = isSystem,
          splitApkPaths = splits,
          iconDrawable = icon
        )
      )
    }

    // Sort: User installed apps first, then alphabetically by appName
    list.sortedWith(
      compareBy<com.example.model.InstalledAppItem> { it.isSystemApp }
        .thenBy { it.appName.lowercase() }
    )
  }

  /**
   * Extracts an installed app's base.apk directly from its system sourceDir into the workspace cache.
   */
  fun extractInstalledApp(app: com.example.model.InstalledAppItem): Flow<ExtractionProgress> = flow {
    emit(
      ExtractionProgress(
        isActive = true,
        currentFileName = "Accediendo al APK de ${app.appName}...",
        extractedFilesCount = 0,
        bytesExtracted = 0L,
        isCompleted = false
      )
    )

    val projectId = UUID.randomUUID().toString()
    val projectDir = File(workspacesDir, projectId).apply { mkdirs() }
    val extractedRoot = File(projectDir, "extracted").apply { mkdirs() }

    var extractedCount = 0
    var totalBytesWritten = 0L
    val buffer = ByteArray(32 * 1024)

    try {
      val sourceFile = File(app.apkPath)
      if (!sourceFile.exists() || !sourceFile.canRead()) {
        throw IllegalStateException("No se puede leer el archivo APK en ${app.apkPath}")
      }

      FileInputStream(sourceFile).use { fileIn ->
        BufferedInputStream(fileIn, 32 * 1024).use { bufferedIn ->
          ZipInputStream(bufferedIn).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
              val entryName = entry.name

              // Prevent Zip Slip vulnerability
              val destinationFile = File(extractedRoot, entryName)
              val canonicalDest = destinationFile.canonicalPath
              val canonicalRoot = extractedRoot.canonicalPath
              if (!canonicalDest.startsWith(canonicalRoot + File.separator) && canonicalDest != canonicalRoot) {
                zis.closeEntry()
                entry = zis.nextEntry
                continue
              }

              if (entry.isDirectory) {
                destinationFile.mkdirs()
              } else {
                destinationFile.parentFile?.mkdirs()
                FileOutputStream(destinationFile).use { fos ->
                  BufferedOutputStream(fos, 32 * 1024).use { bos ->
                    var len: Int
                    while (zis.read(buffer).also { len = it } > 0) {
                      bos.write(buffer, 0, len)
                      totalBytesWritten += len
                    }
                    bos.flush()
                  }
                }
                extractedCount++

                if (extractedCount % 5 == 0 || entryName.endsWith(".dex") || entryName.endsWith(".xml")) {
                  emit(
                    ExtractionProgress(
                      isActive = true,
                      currentFileName = entryName,
                      extractedFilesCount = extractedCount,
                      bytesExtracted = totalBytesWritten,
                      isCompleted = false,
                      projectId = projectId
                    )
                  )
                }
              }
              zis.closeEntry()
              entry = zis.nextEntry
            }
          }
        }
      }

      // Save project metadata
      saveProjectMeta(
        projectDir = projectDir,
        id = projectId,
        originalName = "${app.appName}_${app.packageName}.apk",
        extractedCount = extractedCount,
        totalBytes = totalBytesWritten
      )

      emit(
        ExtractionProgress(
          isActive = false,
          currentFileName = "Extracción de ${app.appName} completada con éxito",
          extractedFilesCount = extractedCount,
          bytesExtracted = totalBytesWritten,
          isCompleted = true,
          projectId = projectId
        )
      )
    } catch (e: Exception) {
      projectDir.deleteRecursively()
      emit(
        ExtractionProgress(
          isActive = false,
          currentFileName = "",
          extractedFilesCount = extractedCount,
          bytesExtracted = totalBytesWritten,
          isCompleted = false,
          error = "Error al extraer ${app.appName}: ${e.localizedMessage ?: e.message}"
        )
      )
    }
  }.flowOn(Dispatchers.IO)
}
