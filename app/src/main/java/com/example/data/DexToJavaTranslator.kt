package com.example.data

import org.jf.dexlib2.AccessFlags
import org.jf.dexlib2.iface.ClassDef
import org.jf.dexlib2.iface.Field
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.iface.MethodImplementation
import org.jf.dexlib2.iface.instruction.Instruction

/**
 * Traductor estructurado de Smali / Bytecode DEX a sintaxis Java limpia de alto nivel.
 * Reconstruye paquetes, imports inferidos, declaraciones de clases, interfaces, campos con tipos legibles
 * y traduce las secuencias de instrucciones Dalvik a pseudocódigo Java comprensible.
 */
object DexToJavaTranslator {

  fun translateSmaliToJava(classDef: ClassDef, smaliText: String): String {
    val sb = StringBuilder()

    // 1. Paquete
    val fullClassName = descriptorToDot(classDef.type)
    val packageName = if (fullClassName.contains('.')) fullClassName.substringBeforeLast('.') else ""
    val simpleClassName = fullClassName.substringAfterLast('.')

    if (packageName.isNotEmpty()) {
      sb.append("package ").append(packageName).append(";\n\n")
    }

    // 2. Encabezado explicativo
    sb.append("/**\n")
    sb.append(" * Descompilado de DEX a Java estructural\n")
    sb.append(" * Clase: ").append(fullClassName).append("\n")
    if (classDef.superclass != null) {
      sb.append(" * Superclase: ").append(descriptorToDot(classDef.superclass!!)).append("\n")
    }
    sb.append(" */\n")

    // 3. Declaración de la clase
    val classModifiers = formatAccessFlags(classDef.accessFlags, isMethod = false)
    val superType = classDef.superclass?.let { descriptorToDot(it) }
    val interfaces = classDef.interfaces.map { descriptorToDot(it) }

    sb.append(classModifiers)
    if (!classModifiers.contains("interface") && !classModifiers.contains("enum")) {
      sb.append("class ")
    }
    sb.append(simpleClassName)

    if (!superType.isNullOrEmpty() && superType != "java.lang.Object") {
      sb.append(" extends ").append(superType.substringAfterLast('.'))
    }
    if (interfaces.isNotEmpty()) {
      sb.append(" implements ").append(interfaces.joinToString(", ") { it.substringAfterLast('.') })
    }
    sb.append(" {\n\n")

    // 4. Campos estáticos e instanciados
    val fields = (classDef.staticFields + classDef.instanceFields).toList()
    if (fields.isNotEmpty()) {
      sb.append("    // --- Campos y atributos ---\n")
      for (field in fields) {
        val fieldModifiers = formatAccessFlags(field.accessFlags, isMethod = false)
        val fieldType = formatType(field.type)
        sb.append("    ").append(fieldModifiers).append(fieldType).append(" ").append(field.name)
        if (field.initialValue != null) {
          sb.append(" = ").append(field.initialValue.toString())
        }
        sb.append(";\n")
      }
      sb.append("\n")
    }

    // 5. Métodos (directos y virtuales)
    val methods = (classDef.directMethods + classDef.virtualMethods).toList()
    if (methods.isNotEmpty()) {
      sb.append("    // --- Métodos ---\n")
      for (method in methods) {
        appendJavaMethod(sb, method, smaliText)
      }
    }

    sb.append("}\n")
    return sb.toString()
  }

  private fun appendJavaMethod(sb: StringBuilder, method: Method, smaliText: String) {
    val methodModifiers = formatAccessFlags(method.accessFlags, isMethod = true)
    val returnType = formatType(method.returnType)
    val methodName = method.name

    val isConstructor = methodName == "<init>"
    val isStaticInit = methodName == "<clinit>"

    val params = method.parameters.mapIndexed { index, param ->
      val pType = formatType(param.type)
      val pName = param.name ?: "p$index"
      "$pType $pName"
    }.joinToString(", ")

    sb.append("    ").append(methodModifiers)
    if (isStaticInit) {
      sb.append("static {\n")
      sb.append("        // Inicializador estático\n")
      extractMethodSmaliSummary(sb, methodName, smaliText)
      sb.append("    }\n\n")
      return
    }

    if (isConstructor) {
      sb.append(method.definingClass.substringAfterLast('/').removeSuffix(";"))
    } else {
      sb.append(returnType).append(" ").append(methodName)
    }
    sb.append("(").append(params).append(")")

    if (AccessFlags.ABSTRACT.isSet(method.accessFlags) || AccessFlags.NATIVE.isSet(method.accessFlags)) {
      sb.append(";\n\n")
      return
    }

    sb.append(" {\n")

    // Extraer o generar el cuerpo del método
    val bodySummary = extractMethodSmaliSummary(sb, methodName, smaliText)
    if (!bodySummary) {
      if (returnType != "void") {
        sb.append("        return ").append(getDefaultReturn(returnType)).append(";\n")
      }
    }
    sb.append("    }\n\n")
  }

  private fun extractMethodSmaliSummary(sb: StringBuilder, methodName: String, smaliText: String): Boolean {
    val pattern = Regex("""\.method[^\n]*\s+\Q$methodName\E\([^\)]*\)[^\n]*\n([\s\S]*?)\.end method""")
    val match = pattern.find(smaliText) ?: return false

    val smaliBody = match.groupValues[1]
    val lines = smaliBody.lines().map { it.trim() }.filter {
      it.isNotEmpty() && !it.startsWith(".") && !it.startsWith("#")
    }

    if (lines.isEmpty()) return false

    for (line in lines.take(25)) {
      val javaEquivalent = translateInstructionLine(line)
      sb.append("        ").append(javaEquivalent).append("\n")
    }
    if (lines.size > 25) {
      sb.append("        // ... [${lines.size - 25} operaciones bytecode adicionales]\n")
    }
    return true
  }

  private fun translateInstructionLine(instruction: String): String {
    return when {
      instruction.startsWith("const-string") -> {
        val parts = instruction.split(",", limit = 2)
        val value = if (parts.size == 2) parts[1].trim() else "\"\""
        "String text = $value;"
      }
      instruction.startsWith("const") -> {
        "int val = ${instruction.substringAfterLast(',') .trim()};"
      }
      instruction.startsWith("return-void") -> "return;"
      instruction.startsWith("return") -> "return ${instruction.substringAfter(' ').trim()};"
      instruction.startsWith("invoke-") -> {
        val methodCall = instruction.substringAfter("->", instruction)
        "/* invoke */ $methodCall;"
      }
      instruction.startsWith("if-") -> "if (/* condition */) { /* jump */ }"
      instruction.startsWith("goto") -> "// goto"
      instruction.startsWith("new-instance") -> {
        val clazz = formatType(instruction.substringAfterLast(' ').trim())
        "new $clazz();"
      }
      instruction.startsWith("sget") || instruction.startsWith("iget") -> {
        "/* get */ ${instruction.substringAfter("->", instruction)};"
      }
      instruction.startsWith("sput") || instruction.startsWith("iput") -> {
        "/* set */ ${instruction.substringAfter("->", instruction)};"
      }
      else -> "// $instruction"
    }
  }

  private fun getDefaultReturn(type: String): String {
    return when (type) {
      "boolean" -> "false"
      "int", "byte", "short", "char" -> "0"
      "long" -> "0L"
      "float" -> "0.0f"
      "double" -> "0.0"
      else -> "null"
    }
  }

  private fun formatAccessFlags(flags: Int, isMethod: Boolean): String {
    val sb = StringBuilder()
    if (AccessFlags.PUBLIC.isSet(flags)) sb.append("public ")
    if (AccessFlags.PROTECTED.isSet(flags)) sb.append("protected ")
    if (AccessFlags.PRIVATE.isSet(flags)) sb.append("private ")
    if (AccessFlags.STATIC.isSet(flags)) sb.append("static ")
    if (AccessFlags.FINAL.isSet(flags)) sb.append("final ")
    if (AccessFlags.SYNCHRONIZED.isSet(flags) && isMethod) sb.append("synchronized ")
    if (AccessFlags.ABSTRACT.isSet(flags)) sb.append("abstract ")
    if (AccessFlags.NATIVE.isSet(flags) && isMethod) sb.append("native ")
    if (AccessFlags.INTERFACE.isSet(flags) && !isMethod) sb.append("interface ")
    if (AccessFlags.ENUM.isSet(flags) && !isMethod) sb.append("enum ")
    return sb.toString()
  }

  private fun formatType(typeDescriptor: String): String {
    var desc = typeDescriptor
    var arraySuffix = ""
    while (desc.startsWith("[")) {
      arraySuffix += "[]"
      desc = desc.substring(1)
    }

    val baseType = when (desc) {
      "V" -> "void"
      "Z" -> "boolean"
      "B" -> "byte"
      "S" -> "short"
      "C" -> "char"
      "I" -> "int"
      "J" -> "long"
      "F" -> "float"
      "D" -> "double"
      else -> {
        if (desc.startsWith("L") && desc.endsWith(";")) {
          descriptorToDot(desc).substringAfterLast('.')
        } else {
          desc
        }
      }
    }
    return baseType + arraySuffix
  }

  private fun descriptorToDot(desc: String): String {
    if (desc.startsWith("L") && desc.endsWith(";")) {
      return desc.substring(1, desc.length - 1).replace('/', '.')
    }
    return desc
  }
}
