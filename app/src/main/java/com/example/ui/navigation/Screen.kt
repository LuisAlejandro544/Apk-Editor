package com.example.ui.navigation

import java.net.URLEncoder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

sealed class Screen(val route: String) {
  object Home : Screen("home")
  object InstalledApps : Screen("installed_apps")
  object Extraction : Screen("extraction")
  object CacheManager : Screen("cache_manager")
  object Settings : Screen("settings")
  object ThreadCpuProfiler : Screen("thread_cpu_profiler")

  object FileExplorer : Screen("explorer/{projectId}?subPath={subPath}") {
    fun createRoute(projectId: String, subPath: String = ""): String {
      val encodedPath = if (subPath.isEmpty()) "" else URLEncoder.encode(subPath, StandardCharsets.UTF_8.name())
      return "explorer/$projectId?subPath=$encodedPath"
    }
  }

  object FileDetail : Screen("file_detail/{projectId}/{encodedPath}") {
    fun createRoute(projectId: String, relativePath: String): String {
      val encoded = URLEncoder.encode(relativePath, StandardCharsets.UTF_8.name())
      return "file_detail/$projectId/$encoded"
    }

    fun decodePath(encodedPath: String): String {
      return try {
        URLDecoder.decode(encodedPath, StandardCharsets.UTF_8.name())
      } catch (_: Exception) {
        encodedPath
      }
    }
  }
}
