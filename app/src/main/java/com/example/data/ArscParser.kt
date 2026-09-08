package com.example.data

import com.reandroid.arsc.chunk.TableBlock
import java.io.File
import java.io.FileInputStream

data class ArscResourceItem(
  val hexId: String,
  val resId: Int,
  val packageName: String,
  val typeName: String,
  val entryName: String,
  val reference: String,
  val value: String,
  val configsCount: Int = 1
)

data class ArscPackageSummary(
  val packageName: String,
  val packageId: Int,
  val types: List<String>,
  val totalEntries: Int
)

data class ArscParseResult(
  val packages: List<ArscPackageSummary>,
  val entries: List<ArscResourceItem>,
  val totalEntries: Int,
  val stringPoolCount: Int,
  val summaryReport: String,
  val errorMessage: String? = null
)

class ArscParser {

  fun parse(filePath: String, maxEntries: Int = 2500): ArscParseResult {
    val file = File(filePath)
    if (!file.exists() || !file.canRead()) {
      return ArscParseResult(
        packages = emptyList(),
        entries = emptyList(),
        totalEntries = 0,
        stringPoolCount = 0,
        summaryReport = "Error: No se puede leer el archivo $filePath",
        errorMessage = "Archivo no accesible"
      )
    }

    return try {
      val tableBlock = TableBlock()
      FileInputStream(file).use { fis ->
        tableBlock.readBytes(fis)
      }

      val packagesList = mutableListOf<ArscPackageSummary>()
      val entriesList = mutableListOf<ArscResourceItem>()
      var totalGlobalEntries = 0

      for (pkg in tableBlock.listPackages()) {
        val pkgName = pkg.name ?: "desconocido"
        val pkgId = pkg.id
        val typeSet = mutableSetOf<String>()
        var pkgEntriesCount = 0

        val resIterator = pkg.resources
        while (resIterator.hasNext()) {
          val resEntry = resIterator.next()
          pkgEntriesCount++
          totalGlobalEntries++
          val typeName = resEntry.type ?: "desconocido"
          typeSet.add(typeName)

          if (entriesList.size < maxEntries) {
            val entryObj = resEntry.any()
            val resolvedVal = try {
              entryObj?.toString() ?: resEntry.buildReference()
            } catch (_: Exception) {
              resEntry.buildReference()
            }

            entriesList.add(
              ArscResourceItem(
                hexId = resEntry.hexId ?: String.format("0x%08X", resEntry.resourceId),
                resId = resEntry.resourceId,
                packageName = pkgName,
                typeName = typeName,
                entryName = resEntry.name ?: "res_${resEntry.resourceId}",
                reference = resEntry.buildReference() ?: "@$typeName/${resEntry.name}",
                value = resolvedVal ?: "",
                configsCount = resEntry.configsCount
              )
            )
          }
        }

        packagesList.add(
          ArscPackageSummary(
            packageName = pkgName,
            packageId = pkgId,
            types = typeSet.toList().sorted(),
            totalEntries = pkgEntriesCount
          )
        )
      }

      val stringPoolCount = try {
        tableBlock.stringPool?.size() ?: 0
      } catch (_: Exception) {
        0
      }

      val summary = buildString {
        appendLine("📦 TABLA DE RECURSOS ANDROID (resources.arsc)")
        appendLine("Motor: ARSCLib (io.github.reandroid:ARSCLib)")
        appendLine("Archivo: ${file.name} (${com.example.model.formatBytes(file.length())})")
        appendLine("--------------------------------------------------")
        appendLine("Paquetes registrados: ${packagesList.size}")
        packagesList.forEach { p ->
          appendLine("• Paquete: ${p.packageName}")
          appendLine("  ID: 0x${Integer.toHexString(p.packageId).uppercase()}")
          appendLine("  Total Recursos: ${p.totalEntries}")
          appendLine("  Tipos: ${p.types.joinToString(", ")}")
        }
        appendLine("--------------------------------------------------")
        appendLine("Cadenas globales en StringPool: $stringPoolCount")
        appendLine("Total de recursos indexados: $totalGlobalEntries")
        if (totalGlobalEntries > maxEntries) {
          appendLine("ℹ️ Mostrando las primeras $maxEntries entradas en la vista interactiva.")
        }
      }

      ArscParseResult(
        packages = packagesList,
        entries = entriesList,
        totalEntries = totalGlobalEntries,
        stringPoolCount = stringPoolCount,
        summaryReport = summary
      )
    } catch (e: Exception) {
      ArscParseResult(
        packages = emptyList(),
        entries = emptyList(),
        totalEntries = 0,
        stringPoolCount = 0,
        summaryReport = "Error al parsear resources.arsc con ARSCLib: ${e.localizedMessage ?: e.message}",
        errorMessage = e.message
      )
    }
  }
}
