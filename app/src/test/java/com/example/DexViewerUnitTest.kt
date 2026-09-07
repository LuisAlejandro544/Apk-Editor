package com.example

import com.example.data.DexDisassembler
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DexViewerUnitTest {

  @Test
  fun testDexClassesLoadingOrFallback() {
    val dummyFile = File("non_existent.dex")
    val classes = DexDisassembler.listClasses(dummyFile)
    assertTrue("Debe retornar lista vacía para archivo inexistente", classes.isEmpty())
  }

  @Test
  fun testSmaliDisassemblyGracefulError() {
    val dummyFile = File("non_existent.dex")
    val result = DexDisassembler.disassembleToSmali(dummyFile, "Lcom/example/Test;")
    assertNotNull(result)
    assertTrue(result.contains("Error"))
  }

  @Test
  fun testJavaDecompilerGracefulError() {
    val dummyFile = File("non_existent.dex")
    val result = DexDisassembler.decompileToJava(dummyFile, "Lcom/example/Test;")
    assertNotNull(result)
    assertTrue(result.contains("Error"))
  }
}
