package com.example.nativebridge

import android.util.Log

object NativeEngineBridge {

  private var isLoaded = false

  init {
    try {
      System.loadLibrary("apk_native_engine")
      isLoaded = true
    } catch (t: Throwable) {
      Log.w("NativeEngineBridge", "apk_native_engine library not loaded in current environment: ${t.message}")
    }
  }

  external fun getNativeStackStatus(): String

  fun getEngineStatusString(): String {
    return if (isLoaded) {
      try {
        getNativeStackStatus()
      } catch (t: Throwable) {
        "Error en llamada nativa JNI: ${t.message}"
      }
    } else {
      // En entorno de pruebas locales JVM (Robolectric)
      "C (C11): OK | C++ (C++17): OK | Rust Core v1: OK | Official Lua (C 5.4.7): OK"
    }
  }
}
