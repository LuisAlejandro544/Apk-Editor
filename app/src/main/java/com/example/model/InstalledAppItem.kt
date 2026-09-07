package com.example.model

import android.graphics.drawable.Drawable

/**
 * Represents an application currently installed on the user's Android device.
 */
data class InstalledAppItem(
  val packageName: String,
  val appName: String,
  val versionName: String,
  val versionCode: Long,
  val apkPath: String,
  val apkSizeBytes: Long,
  val isSystemApp: Boolean,
  val splitApkPaths: List<String> = emptyList(),
  val iconDrawable: Drawable? = null
) {
  val formattedSize: String get() = formatBytes(apkSizeBytes)
}
