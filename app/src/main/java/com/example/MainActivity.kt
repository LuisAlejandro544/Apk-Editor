package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.AppThemeMode
import com.example.ui.navigation.Screen
import com.example.ui.screens.CacheManagerScreen
import com.example.ui.screens.ExtractionScreen
import com.example.ui.screens.FileDetailScreen
import com.example.ui.screens.FileExplorerScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InstalledAppsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.ApkViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: ApkViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeMode by viewModel.themeMode.collectAsState()
      MyApplicationTheme(dynamicColor = (themeMode == AppThemeMode.MATERIAL_YOU)) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          ApkExtractorApp(viewModel = viewModel)
        }
      }
    }
  }
}

@Composable
fun ApkExtractorApp(viewModel: ApkViewModel) {
  val navController = rememberNavController()

  NavHost(
    navController = navController,
    startDestination = Screen.Home.route
  ) {
    composable(Screen.Home.route) {
      HomeScreen(
        viewModel = viewModel,
        onNavigateToExtraction = {
          navController.navigate(Screen.Extraction.route)
        },
        onNavigateToInstalledApps = {
          navController.navigate(Screen.InstalledApps.route)
        },
        onNavigateToExplorer = { projectId ->
          navController.navigate(Screen.FileExplorer.createRoute(projectId, ""))
        },
        onNavigateToCacheManager = {
          navController.navigate(Screen.CacheManager.route)
        },
        onNavigateToSettings = {
          navController.navigate(Screen.Settings.route)
        }
      )
    }

    composable(Screen.InstalledApps.route) {
      InstalledAppsScreen(
        viewModel = viewModel,
        onNavigateToExtraction = {
          navController.navigate(Screen.Extraction.route)
        },
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }

    composable(Screen.Extraction.route) {
      ExtractionScreen(
        viewModel = viewModel,
        onNavigateToExplorer = { projectId ->
          navController.navigate(Screen.FileExplorer.createRoute(projectId, "")) {
            popUpTo(Screen.Home.route)
          }
        },
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }

    composable(
      route = Screen.FileExplorer.route,
      arguments = listOf(
        navArgument("projectId") { type = NavType.StringType },
        navArgument("subPath") {
          type = NavType.StringType
          defaultValue = ""
        }
      )
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
      val rawSubPath = backStackEntry.arguments?.getString("subPath") ?: ""
      val subPath = Screen.FileDetail.decodePath(rawSubPath)

      FileExplorerScreen(
        projectId = projectId,
        subPath = subPath,
        viewModel = viewModel,
        onNavigateToSubfolder = { newSubPath ->
          navController.navigate(Screen.FileExplorer.createRoute(projectId, newSubPath))
        },
        onNavigateToFileDetail = { relativePath ->
          navController.navigate(Screen.FileDetail.createRoute(projectId, relativePath))
        },
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }

    composable(
      route = Screen.FileDetail.route,
      arguments = listOf(
        navArgument("projectId") { type = NavType.StringType },
        navArgument("encodedPath") { type = NavType.StringType }
      )
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
      val encodedPath = backStackEntry.arguments?.getString("encodedPath") ?: ""
      val relativePath = Screen.FileDetail.decodePath(encodedPath)

      FileDetailScreen(
        projectId = projectId,
        relativePath = relativePath,
        viewModel = viewModel,
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }

    composable(Screen.CacheManager.route) {
      CacheManagerScreen(
        viewModel = viewModel,
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }

    composable(Screen.Settings.route) {
      SettingsScreen(
        viewModel = viewModel,
        onNavigateBack = {
          navController.popBackStack()
        }
      )
    }
  }
}
