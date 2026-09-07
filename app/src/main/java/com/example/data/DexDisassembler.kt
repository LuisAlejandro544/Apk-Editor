package com.example.data

import org.jf.baksmali.Adaptors.ClassDefinition
import org.jf.baksmali.BaksmaliOptions
import org.jf.baksmali.formatter.BaksmaliWriter
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.DexFile
import java.io.File
import java.io.StringWriter

data class DexClassItem(
  val typeDescriptor: String,     // Ej: Lcom/example/MainActivity;
  val prettyClassName: String,    // Ej: com.example.MainActivity
  val simpleClassName: String,    // Ej: MainActivity
  val packageName: String,        // Ej: com.example
  val accessFlags: Int = 0
)

object DexDisassembler {

  /**
   * Carga el archivo DEX y lista todas las clases disponibles de manera indexada.
   */
  fun listClasses(dexFile: File): List<DexClassItem> {
    if (!dexFile.exists() || dexFile.length() == 0L) return emptyList()

    return try {
      val dex: DexFile = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
      dex.classes.map { classDef ->
        val desc = classDef.type
        val pretty = descriptorToDot(desc)
        val simple = pretty.substringAfterLast('.')
        val pkg = if (pretty.contains('.')) pretty.substringBeforeLast('.') else ""
        DexClassItem(
          typeDescriptor = desc,
          prettyClassName = pretty,
          simpleClassName = simple,
          packageName = pkg,
          accessFlags = classDef.accessFlags
        )
      }.sortedBy { it.prettyClassName }
    } catch (e: Exception) {
      emptyList()
    }
  }

  /**
   * Desensambla una clase específica a código Smali fiel y preciso.
   */
  fun disassembleToSmali(dexFile: File, classDescriptor: String): String {
    return try {
      val dex: DexFile = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
      val classDef: ClassDef? = dex.classes.find { it.type == classDescriptor }
        ?: dex.classes.find { descriptorToDot(it.type) == classDescriptor }

      if (classDef == null) {
        return "# Error: Clase no encontrada en el DEX: $classDescriptor"
      }

      val options = BaksmaliOptions()
      options.deodex = false
      options.parameterRegisters = true
      options.localsDirective = true

      val classDefinition = ClassDefinition(options, classDef)
      val stringWriter = StringWriter()
      val writer = BaksmaliWriter(stringWriter)
      classDefinition.writeTo(writer)
      writer.flush()
      stringWriter.toString()
    } catch (e: Exception) {
      "# Error desensamblando a Smali: ${e.localizedMessage}\n\n" +
        (e.stackTraceToString().lines().take(15).joinToString("\n"))
    }
  }

  /**
   * Desensambla la primera clase o genera un resumen Smali si no se especifica una.
   */
  fun disassembleSummary(dexFile: File, maxClasses: Int = 3): String {
    return try {
      val dex: DexFile = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
      val classes = dex.classes.take(maxClasses)
      if (classes.isEmpty()) return "# El archivo DEX no contiene clases definidas."

      val options = BaksmaliOptions()
      val sb = StringBuilder()
      sb.append("# Smali Bytecode Disassembly (Primeras ${classes.size} clases de ${dex.classes.size})\n")
      sb.append("# Selecciona una clase específica para ver y editar su código completo.\n\n")

      for (classDef in classes) {
        val classDefinition = ClassDefinition(options, classDef)
        val stringWriter = StringWriter()
        val writer = BaksmaliWriter(stringWriter)
        classDefinition.writeTo(writer)
        writer.flush()
        sb.append(stringWriter.toString())
        sb.append("\n# -----------------------------------------------------------------------\n\n")
      }
      sb.toString()
    } catch (e: Exception) {
      "# Error leyendo DEX: ${e.localizedMessage}"
    }
  }

  /**
   * Genera una representación descompilada a Java estructural limpio a partir de los metadatos de la clase.
   * Reconstruye firmas, campos, métodos con sus tipos, visibilidad y llamadas smali traducidas a pseudocódigo.
   */
  fun decompileToJava(dexFile: File, classDescriptor: String): String {
    return try {
      val dex: DexFile = DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
      val classDef: ClassDef? = dex.classes.find { it.type == classDescriptor }
        ?: dex.classes.find { descriptorToDot(it.type) == classDescriptor }

      if (classDef == null) {
        return "// Error: Clase no encontrada: $classDescriptor"
      }

      val smaliCode = disassembleToSmali(dexFile, classDef.type)
      DexToJavaTranslator.translateSmaliToJava(classDef, smaliCode)
    } catch (e: Exception) {
      "// Error descompilando a Java: ${e.localizedMessage}"
    }
  }

  private fun descriptorToDot(desc: String): String {
    if (desc.startsWith("L") && desc.endsWith(";")) {
      return desc.substring(1, desc.length - 1).replace('/', '.')
    }
    return desc
  }
}
