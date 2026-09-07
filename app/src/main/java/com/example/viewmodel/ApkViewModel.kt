package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ApkExtractorRepository
import com.example.model.ApkProject
import com.example.model.ExtractedFileItem
import com.example.model.ExtractionProgress
import com.example.model.StorageInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File

data class FileDetailState(
  val isLoading: Boolean = true,
  val fileName: String = "",
  val relativePath: String = "",
  val absolutePath: String = "",
  val sizeBytes: Long = 0L,
  val sha256: String = "",
  val textContent: String = "",
  val hexDump: String = "",
  val errorMessage: String? = null,
  val isSaving: Boolean = false,
  val isEditable: Boolean = false,
  val isAxmlDecoded: Boolean = false
)

class ApkViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ApkExtractorRepository(application)

  private val _projects = MutableStateFlow<List<ApkProject>>(emptyList())
  val projects: StateFlow<List<ApkProject>> = _projects.asStateFlow()

  private val _storageInfo = MutableStateFlow(StorageInfo(0L, 0L, 0))
  val storageInfo: StateFlow<StorageInfo> = _storageInfo.asStateFlow()

  private val _extractionProgress = MutableStateFlow(ExtractionProgress())
  val extractionProgress: StateFlow<ExtractionProgress> = _extractionProgress.asStateFlow()

  private val _currentFolderItems = MutableStateFlow<List<ExtractedFileItem>>(emptyList())
  val currentFolderItems: StateFlow<List<ExtractedFileItem>> = _currentFolderItems.asStateFlow()

  private val _currentProject = MutableStateFlow<ApkProject?>(null)
  val currentProject: StateFlow<ApkProject?> = _currentProject.asStateFlow()

  private val _fileDetail = MutableStateFlow(FileDetailState())
  val fileDetail: StateFlow<FileDetailState> = _fileDetail.asStateFlow()

  private val _isFolderLoading = MutableStateFlow(false)
  val isFolderLoading: StateFlow<Boolean> = _isFolderLoading.asStateFlow()

  private val _installedApps = MutableStateFlow<List<com.example.model.InstalledAppItem>>(emptyList())
  val installedApps: StateFlow<List<com.example.model.InstalledAppItem>> = _installedApps.asStateFlow()

  private val _isInstalledAppsLoading = MutableStateFlow(false)
  val isInstalledAppsLoading: StateFlow<Boolean> = _isInstalledAppsLoading.asStateFlow()

  private var extractionJob: Job? = null

  init {
    refreshProjectsAndStorage()
    loadInstalledApps()
  }

  fun loadInstalledApps() {
    viewModelScope.launch {
      _isInstalledAppsLoading.value = true
      _installedApps.value = repository.getInstalledApps()
      _isInstalledAppsLoading.value = false
    }
  }

  fun extractInstalledApp(app: com.example.model.InstalledAppItem, onNavigateToExtraction: () -> Unit) {
    extractionJob?.cancel()
    onNavigateToExtraction()

    extractionJob = viewModelScope.launch {
      repository.extractInstalledApp(app).collectLatest { progress ->
        _extractionProgress.value = progress
        if (progress.isCompleted) {
          refreshProjectsAndStorage()
        }
      }
    }
  }

  fun refreshProjectsAndStorage() {
    viewModelScope.launch {
      _projects.value = repository.listProjects()
      _storageInfo.value = repository.getStorageInfo()
    }
  }

  fun extractApk(uri: Uri, onNavigateToExtraction: () -> Unit) {
    extractionJob?.cancel()
    val fileName = repository.getFileName(uri)

    onNavigateToExtraction()

    extractionJob = viewModelScope.launch {
      repository.extractApk(uri, fileName).collectLatest { progress ->
        _extractionProgress.value = progress
        if (progress.isCompleted) {
          refreshProjectsAndStorage()
        }
      }
    }
  }

  fun cancelExtraction() {
    extractionJob?.cancel()
    _extractionProgress.value = ExtractionProgress(
      isActive = false,
      error = "Extracción cancelada por el usuario"
    )
    refreshProjectsAndStorage()
  }

  fun loadProjectFolder(projectId: String, subPath: String) {
    viewModelScope.launch {
      _isFolderLoading.value = true
      _currentProject.value = repository.getProjectById(projectId)
      _currentFolderItems.value = repository.getDirectoryContents(projectId, subPath)
      _isFolderLoading.value = false
    }
  }

  fun loadFileDetail(projectId: String, relativePath: String) {
    viewModelScope.launch {
      _fileDetail.value = FileDetailState(isLoading = true, relativePath = relativePath)

      val project = repository.getProjectById(projectId)
      if (project == null) {
        _fileDetail.value = FileDetailState(isLoading = false, errorMessage = "Proyecto no encontrado")
        return@launch
      }

      val targetFile = File(project.extractedDirPath, relativePath)
      if (!targetFile.exists()) {
        _fileDetail.value = FileDetailState(isLoading = false, errorMessage = "El archivo no existe en caché")
        return@launch
      }

      val size = targetFile.length()
      val sha256 = repository.calculateSha256(targetFile.absolutePath)
      val textPreview = repository.readFileContent(targetFile.absolutePath)
      val hexDump = repository.readHexDump(targetFile.absolutePath)

      val isBinaryPlaceholder = textPreview.startsWith("Este archivo contiene datos binarios") || textPreview == "[Archivo vacío]"
      val isXml = targetFile.name.endsWith(".xml", ignoreCase = true)
      val isAxml = isXml && (textPreview.contains("<manifest") || textPreview.contains("<?xml") || textPreview.contains("<"))
      val isEditable = !isBinaryPlaceholder && textPreview.isNotBlank() && !targetFile.name.endsWith(".dex") && !targetFile.name.endsWith(".so")

      _fileDetail.value = FileDetailState(
        isLoading = false,
        fileName = targetFile.name,
        relativePath = relativePath,
        absolutePath = targetFile.absolutePath,
        sizeBytes = size,
        sha256 = sha256,
        textContent = textPreview,
        hexDump = hexDump,
        isEditable = isEditable,
        isAxmlDecoded = isAxml
      )
    }
  }

  fun saveFileContent(projectId: String, relativePath: String, newContent: String, onComplete: (Boolean) -> Unit = {}) {
    viewModelScope.launch {
      val currentState = _fileDetail.value
      _fileDetail.value = currentState.copy(isSaving = true)

      val success = repository.saveFileContent(currentState.absolutePath, newContent)
      if (success) {
        val targetFile = File(currentState.absolutePath)
        val newSize = targetFile.length()
        val newSha256 = repository.calculateSha256(currentState.absolutePath)
        val newHex = repository.readHexDump(currentState.absolutePath)
        _fileDetail.value = currentState.copy(
          isSaving = false,
          textContent = newContent,
          sizeBytes = newSize,
          sha256 = newSha256,
          hexDump = newHex
        )
        refreshProjectsAndStorage()
      } else {
        _fileDetail.value = currentState.copy(isSaving = false)
      }
      onComplete(success)
    }
  }

  fun deleteProject(projectId: String, onDeleted: () -> Unit = {}) {
    viewModelScope.launch {
      repository.deleteProject(projectId)
      refreshProjectsAndStorage()
      onDeleted()
    }
  }

  fun clearAllCache(onCleared: () -> Unit = {}) {
    viewModelScope.launch {
      repository.clearAllWorkspaces()
      refreshProjectsAndStorage()
      onCleared()
    }
  }
}
