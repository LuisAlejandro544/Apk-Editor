package com.example

import com.example.util.AppDispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDispatchersUnitTest {

  @Test
  fun testAvailableCoresIsPositive() {
    assertTrue("Los núcleos disponibles deben ser al menos 2", AppDispatchers.availableCores >= 2)
  }

  @Test
  fun testScannerDispatcherRunsOnCustomPool() = runBlocking {
    withContext(AppDispatchers.ScannerDispatcher) {
      val currentThreadName = Thread.currentThread().name
      assertNotNull(currentThreadName)
      assertTrue(
        "El hilo debe ser gestionado por APK-ScannerPool",
        currentThreadName.contains("APK-ScannerPool")
      )
    }
  }

  @Test
  fun testFastIODispatcherRunsOnCustomPool() = runBlocking {
    withContext(AppDispatchers.FastIODispatcher) {
      val currentThreadName = Thread.currentThread().name
      assertNotNull(currentThreadName)
      assertTrue(
        "El hilo debe ser gestionado por APK-FastIO",
        currentThreadName.contains("APK-FastIO")
      )
    }
  }
}
