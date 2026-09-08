package com.example.data

import jadx.api.JadxArgs
import jadx.api.JadxDecompiler
import java.io.File

object JadxDecompilerService {

  fun decompileClass(file: File, classFullNameOrDescriptor: String): Result<String> {
    return try {
      val args = JadxArgs().apply {
        inputFiles.add(file)
        isSkipResources = true
        isDeobfuscationOn = true
        threadsCount = 2
        isFallbackMode = false
      }

      JadxDecompiler(args).use { decompiler ->
        decompiler.load()
        val targetName = normalizeClassName(classFullNameOrDescriptor)
        val targetClass = decompiler.classes.find { cls ->
          cls.fullName == targetName || cls.fullName.endsWith(".$targetName") || cls.classNode.classInfo.rawName == targetName
        }

        if (targetClass != null) {
          val code = targetClass.code
          Result.success(code)
        } else {
          // If specific class not found or summary requested, return first available or top classes
          val first = decompiler.classes.firstOrNull()
          if (first != null) {
            Result.success(first.code)
          } else {
            Result.failure(Exception("No se encontraron clases descompilables en el archivo."))
          }
        }
      }
    } catch (e: Throwable) {
      Result.failure(e)
    }
  }

  fun decompileFirstAvailable(file: File): Result<String> {
    return try {
      val args = JadxArgs().apply {
        inputFiles.add(file)
        isSkipResources = true
        isDeobfuscationOn = true
        threadsCount = 2
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
