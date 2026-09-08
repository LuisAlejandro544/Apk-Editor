package com.example.data

import com.example.nativebridge.NativeEngineBridge

data class ElfSymbolItem(
  val name: String,
  val isJni: Boolean,
  val isExport: Boolean,
  val address: String = "",
  val sizeBytes: Long = 0L
) {
  val displayName: String
    get() = if (isJni) "⭐ $name" else name
}

enum class SoViewMode(val label: String, val iconBadge: String) {
  HEADER("Cabecera", "🏛️"),
  SYMBOLS("Símbolos JNI", "🔣"),
  DEPENDENCIES("Dependencias", "📦"),
  DISASSEMBLY("ASM (Capstone)", "⚙️"),
  STRINGS("Strings", "🔤")
}

class NativeElfDisassembler {

  fun parseHeader(filePath: String): String {
    return NativeEngineBridge.analyzeElfHeader(filePath)
  }

  fun parseSymbols(filePath: String): Pair<List<ElfSymbolItem>, String> {
    val raw = NativeEngineBridge.analyzeElfSymbols(filePath)
    val symbols = mutableListOf<ElfSymbolItem>()
    val lines = raw.lines()
    for (line in lines) {
      val trimmed = line.trim()
      if (trimmed.startsWith("[JNI]")) {
        val name = trimmed.substringAfter("->").trim()
        if (name.isNotEmpty()) {
          symbols.add(ElfSymbolItem(name = name, isJni = true, isExport = true))
        }
      } else if (trimmed.startsWith("[EXP]")) {
        val name = trimmed.substringAfter("->").trim().ifEmpty { trimmed.substringAfter("[EXP]").trim() }
        if (name.isNotEmpty()) {
          symbols.add(ElfSymbolItem(name = name, isJni = false, isExport = true))
        }
      } else if (trimmed.startsWith("[IMP]")) {
        val name = trimmed.substringAfter("[IMP]").trim()
        if (name.isNotEmpty()) {
          symbols.add(ElfSymbolItem(name = name, isJni = false, isExport = false))
        }
      }
    }
    return Pair(symbols, raw)
  }

  fun parseDependencies(filePath: String): Pair<List<String>, String> {
    val raw = NativeEngineBridge.analyzeElfDependencies(filePath)
    val deps = mutableListOf<String>()
    for (line in raw.lines()) {
      val trimmed = line.trim()
      if (trimmed.matches(Regex("""\[\d+\]\s+.+"""))) {
        val dep = trimmed.substringAfter("]").trim()
        if (dep.isNotEmpty()) deps.add(dep)
      }
    }
    return Pair(deps, raw)
  }

  fun disassemble(filePath: String, maxInstructions: Int = 120): String {
    return NativeEngineBridge.disassembleElf(filePath, maxInstructions)
  }

  fun extractStrings(filePath: String, minLen: Int = 4): String {
    return NativeEngineBridge.extractStrings(filePath, minLen)
  }
}
