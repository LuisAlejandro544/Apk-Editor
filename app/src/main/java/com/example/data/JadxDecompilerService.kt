package com.example.data

import jadx.api.JadxArgs
import jadx.api.JadxDecompiler
import java.io.File

object JadxDecompilerService {

  fun decompileClass(
    file: File,
    classFullNameOrDescriptor: String,
    deobfuscate: Boolean = false,
    fallbackMode: Boolean = false
  ): Result<String> {
    // Intento primario con los parámetros solicitados
    val primaryResult = executeDecompile(file, classFullNameOrDescriptor, deobfuscate, fallbackMode)
    if (primaryResult.isSuccess || fallbackMode) {
      return primaryResult
    }
    // Si falló y no estaba en modo fallback, intentar con modo fallback para tolerar código ofuscado o CFG dañado
    return executeDecompile(file, classFullNameOrDescriptor, deobfuscate, fallbackMode = true)
  }

  private fun executeDecompile(
    file: File,
    classFullNameOrDescriptor: String,
    deobfuscate: Boolean,
    fallbackMode: Boolean
  ): Result<String> {
    return try {
      val args = JadxArgs().apply {
        inputFiles.add(file)
        isSkipResources = true
        isDeobfuscationOn = deobfuscate
        threadsCount = 2
        isFallbackMode = fallbackMode
      }

      JadxDecompiler(args).use { decompiler ->
        decompiler.load()
        val targetName = normalizeClassName(classFullNameOrDescriptor)
        val targetClass = decompiler.classes.find { cls ->
          cls.fullName == targetName ||
            cls.fullName.endsWith(".$targetName") ||
            cls.classNode.classInfo.rawName == targetName ||
            cls.classNode.classInfo.shortName == targetName ||
            cls.name == targetName
        }

        if (targetClass != null) {
          val header = if (fallbackMode) "// [Modo Fallback Tolerante Activo: descompilación recuperada de ofuscación agresiva]\n" else ""
          val deobfNote = if (deobfuscate) "// [Auto-Renombrado de JADX Activo: alias sintéticos asignados a identificadores ofuscados]\n" else "// [Nombres Ofuscados Preservados: visualizando identificadores reales del DEX]\n"
          Result.success(header + deobfNote + targetClass.code)
        } else {
          val matching = decompiler.classes.find { cls ->
            cls.name.equals(targetName, ignoreCase = true) || cls.fullName.endsWith(targetName, ignoreCase = true)
          }
          if (matching != null) {
            val note = "// [Coincidencia aproximada encontrada para: $targetName]\n"
            Result.success(note + matching.code)
          } else {
            val first = decompiler.classes.firstOrNull()
            if (first != null) {
              Result.success("// [Clase '$targetName' no hallada en AST principal, mostrando primera clase disponible]\n" + first.code)
            } else {
              Result.failure(Exception("No se encontraron clases descompilables en el archivo DEX."))
            }
          }
        }
      }
    } catch (e: Throwable) {
      Result.failure(e)
    }
  }

  fun decompileFirstAvailable(file: File, deobfuscate: Boolean = false, fallbackMode: Boolean = false): Result<String> {
    return try {
      val args = JadxArgs().apply {
        inputFiles.add(file)
        isSkipResources = true
        isDeobfuscationOn = deobfuscate
        threadsCount = 2
        isFallbackMode = fallbackMode
      }
      JadxDecompiler(args).use { decompiler ->
        decompiler.load()
        val first = decompiler.classes.firstOrNull()
        if (first != null) {
          Result.success(first.code)
        } else {
          Result.failure(Exception("No se encontraron clases."))
        }
      }
    } catch (e: Throwable) {
      Result.failure(e)
    }
  }

  private fun normalizeClassName(name: String): String {
    var clean = name.trim()
    if (clean.startsWith("L") && clean.endsWith(";")) {
      clean = clean.substring(1, clean.length - 1)
    }
    return clean.replace('/', '.')
  }
}
