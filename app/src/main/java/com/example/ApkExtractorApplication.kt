package com.example

import android.app.Application
import android.util.Log
import com.didichuxing.doraemonkit.DoKit
import com.pluto.Pluto
import com.pluto.plugins.exceptions.PlutoExceptionsPlugin
import com.pluto.plugins.logger.PlutoLoggerPlugin
import com.pluto.plugins.preferences.PlutoSharePreferencesPlugin

class ApkExtractorApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    // 1. Inicialización de Pluto (Suite de depuración móvil para logs, excepciones y SharedPreferences)
    try {
      Pluto.Installer(this)
        .addPlugin(PlutoExceptionsPlugin())
        .addPlugin(PlutoLoggerPlugin())
        .addPlugin(PlutoSharePreferencesPlugin())
        .install()
      Log.i("ApkExtractorApplication", "Pluto inicializado correctamente")
    } catch (e: Throwable) {
      Log.w("ApkExtractorApplication", "No se pudo inicializar Pluto: ${e.message}")
    }

    // 2. Inicialización de DoraemonKit (DoKit) para monitor en pantalla de FPS, CPU, RAM y caídas de frames
    try {
      DoKit.Builder(this)
        .build()
      Log.i("ApkExtractorApplication", "DoraemonKit inicializado correctamente")
    } catch (e: Throwable) {
      Log.w("ApkExtractorApplication", "No se pudo inicializar DoraemonKit: ${e.message}")
    }

    // 3. LeakCanary se autoinicializa mediante su propio ContentProvider (com.squareup.leakcanary:leakcanary-android)
    Log.i("ApkExtractorApplication", "LeakCanary activo para detección de fugas de memoria")
  }
}
