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
import com.example.model.AppThemeMode
import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.data.DexClassItem
import com.example.data.ElfSymbolItem
import com.example.data.SoViewMode
import com.example.data.ArscParseResult
import com.example.data.ArscResourceItem
import com.example.data.BinaryDataResult
import com.example.util.AppDispatchers
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
  val isAxmlDecoded: Boolean = false,
  // Campos especializados para DEX Bytecode & Decompiler
  val isDex: Boolean = false,
  val dexClasses: List<DexClassItem> = emptyList(),
  val selectedDexClass: DexClassItem? = null,
  val dexViewMode: DexViewMode = DexViewMode.SMALI,
  val isDexDecompiling: Boolean = false,
  // Campos especializados para ELF (.so) Goblin & Capstone
  val isSo: Boolean = false,
  val soViewMode: SoViewMode = SoViewMode.HEADER,
  val soSymbols: List<ElfSymbolItem> = emptyList(),
  val selectedSoSymbol: ElfSymbolItem? = null,
  val soDependencies: List<String> = emptyList(),
  val isSoAnalyzing: Boolean = false,
  // Campos especializados para Multimedia (Coil & Media3)
  val isImage: Boolean = false,
  val isAudio: Boolean = false,
  // Campos especializados para resources.arsc (ARSCLib)
  val isArsc: Boolean = false,
  val arscResult: ArscParseResult? = null,
  val arscViewMode: ArscViewMode = ArscViewMode.ENTRIES,
  val arscFilterType: String? = null,
  val arscSearchQuery: String = "",
  // Campos especializados para archivos binarios de datos (.dat / .bin)
  val isBinaryData: Boolean = false,
  val binaryDataResult: BinaryDataResult? = null,
  val binaryEditMode: BinaryEditMode = BinaryEditMode.HEX,
  val binaryHexEditable: String = "",
  val binaryTextEditable: String = ""
)

enum class BinaryEditMode(val label: String, val iconBadge: String) {
  HEX("Editor Hex", "🔢"),
  TEXT("Texto / UTF-8", "🔤"),
  INSPECTOR("Inspector & Strings", "🔍")
}

enum class DexViewMode {
  SMALI, JAVA
}

enum class ArscViewMode {
  ENTRIES, SUMMARY
}


class ApkViewModel(application: Application) : AndroidViewModel(application) {

  private val repository = ApkExtractorRepository(application)
  private val appPrefs = application.getSharedPreferences("apk_app_theme_prefs", Context.MODE_PRIVATE)

  private val _themeMode = MutableStateFlow(
    if (appPrefs.getString("theme_mode", AppThemeMode.CYBER_DARK.name) == AppThemeMode.MATERIAL_YOU.name) {
      AppThemeMode.MATERIAL_YOU
    } else {
      AppThemeMode.CYBER_DARK
    }
  )
  val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

  fun setThemeMode(mode: AppThemeMode) {
    _themeMode.value = mode
    appPrefs.edit().putString("theme_mode", mode.name).apply()
  }

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

      val project = withContext(AppDispatchers.FastIODispatcher) {
        repository.getProjectById(projectId)
      }
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
      val lowerName = targetFile.name.lowercase()
      val isDexFile = lowerName.endsWith(".dex")
      val isSoFile = lowerName.endsWith(".so")
      val isArscFile = lowerName.endsWith(".arsc") || targetFile.name.equals("resources.arsc", ignoreCase = true)
      val isImageFile = lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") ||
        lowerName.endsWith(".webp") || lowerName.endsWith(".gif") || lowerName.endsWith(".svg") ||
        lowerName.endsWith(".ico") || lowerName.endsWith(".bmp")
      val isAudioFile = lowerName.endsWith(".mp3") || lowerName.endsWith(".ogg") || lowerName.endsWith(".wav") ||
        lowerName.endsWith(".aac") || lowerName.endsWith(".m4a") || lowerName.endsWith(".flac") ||
        lowerName.endsWith(".opus") || lowerName.endsWith(".mid") || lowerName.endsWith(".midi")
      val isDatOrBinFile = lowerName.endsWith(".dat") || lowerName.endsWith(".bin")

      // Ejecutar operaciones en paralelo en hilos secundarios dinámicos para eliminar el lag al abrir archivos
      val sha256Deferred = async(AppDispatchers.ComputeDispatcher) {
        repository.calculateSha256(targetFile.absolutePath)
      }
      val hexDumpDeferred = async(AppDispatchers.FastIODispatcher) {
        repository.readHexDump(targetFile.absolutePath)
      }

      if (isDexFile) {
        // En archivos DEX: listar clases y desensamblar en hilo de cómputo de alta velocidad
        val dexContentDeferred = async(AppDispatchers.ComputeDispatcher) {
          val classes = repository.getDexClasses(targetFile.absolutePath)
          val initialClass = classes.firstOrNull()
          val initialText = if (initialClass != null) {
            repository.disassembleDexToSmali(targetFile.absolutePath, initialClass.typeDescriptor)
          } else {
            repository.disassembleDexToSmali(targetFile.absolutePath, null)
          }
          Triple(classes, initialClass, initialText)
        }

        val sha256 = sha256Deferred.await()
        val hexDump = hexDumpDeferred.await()
        val (classes, initialClass, initialText) = dexContentDeferred.await()

        _fileDetail.value = FileDetailState(
          isLoading = false,
          fileName = targetFile.name,
          relativePath = relativePath,
          absolutePath = targetFile.absolutePath,
          sizeBytes = size,
          sha256 = sha256,
          textContent = initialText,
          hexDump = hexDump,
          isEditable = true,
          isAxmlDecoded = false,
          isDex = true,
          dexClasses = classes,
          selectedDexClass = initialClass,
          dexViewMode = DexViewMode.SMALI,
          isSo = false
        )
      } else if (isSoFile) {
        // En archivos ELF (.so): parsear cabecera, símbolos y dependencias con Goblin en Rust Core
        val elfContentDeferred = async(AppDispatchers.ComputeDispatcher) {
          val headerText = repository.getElfHeader(targetFile.absolutePath)
          val (symbols, _) = repository.getElfSymbols(targetFile.absolutePath)
          val (deps, _) = repository.getElfDependencies(targetFile.absolutePath)
          Triple(headerText, symbols, deps)
        }

        val sha256 = sha256Deferred.await()
        val hexDump = hexDumpDeferred.await()
        val (headerText, symbols, deps) = elfContentDeferred.await()

        _fileDetail.value = FileDetailState(
          isLoading = false,
          fileName = targetFile.name,
          relativePath = relativePath,
          absolutePath = targetFile.absolutePath,
          sizeBytes = size,
          sha256 = sha256,
          textContent = headerText,
          hexDump = hexDump,
          isEditable = false,
          isAxmlDecoded = false,
          isDex = false,
          isSo = true,
          soViewMode = SoViewMode.HEADER,
          soSymbols = symbols,
          selectedSoSymbol = symbols.firstOrNull(),
          soDependencies = deps
        )
      } else if (isArscFile) {
        // En archivos resources.arsc: parsear tabla completa de recursos usando ARSCLib en ComputeDispatcher
        val arscDeferred = async(AppDispatchers.ComputeDispatcher) {
          repository.parseArsc(targetFile.absolutePath)
        }

        val sha256 = sha256Deferred.await()
        val hexDump = hexDumpDeferred.await()
        val arscResult = arscDeferred.await()

        _fileDetail.value = FileDetailState(
          isLoading = false,
          fileName = targetFile.name,
          relativePath = relativePath,
          absolutePath = targetFile.absolutePath,
          sizeBytes = size,
          sha256 = sha256,
          textContent = arscResult.summaryReport,
          hexDump = hexDump,
          isEditable = false,
          isAxmlDecoded = false,
          isDex = false,
          isSo = false,
          isImage = false,
          isAudio = false,
          isArsc = true,
          arscResult = arscResult,
          arscViewMode = ArscViewMode.ENTRIES
        )
      } else if (isDatOrBinFile) {
        // En archivos de datos binarios (.dat / .bin): analizar estructura, extraer hex editable y cadenas legibles
        val binaryDeferred = async(AppDispatchers.ComputeDispatcher) {
          repository.parseBinaryData(targetFile.absolutePath)
        }

        val sha256 = sha256Deferred.await()
        val hexDump = hexDumpDeferred.await()
        val binaryResult = binaryDeferred.await()

        _fileDetail.value = FileDetailState(
          isLoading = false,
          fileName = targetFile.name,
          relativePath = relativePath,
          absolutePath = targetFile.absolutePath,
          sizeBytes = size,
          sha256 = sha256,
          textContent = binaryResult.hexFormatted,
          hexDump = hexDump,
          isEditable = true,
          isAxmlDecoded = false,
          isDex = false,
          isSo = false,
          isImage = false,
          isAudio = false,
          isArsc = false,
          isBinaryData = true,
          binaryDataResult = binaryResult,
          binaryEditMode = BinaryEditMode.HEX,
          binaryHexEditable = binaryResult.hexFormatted,
          binaryTextEditable = binaryResult.textContent
        )
      } else {

        val textPreviewDeferred = async(AppDispatchers.FastIODispatcher) {
          repository.readFileContent(targetFile.absolutePath)
        }

        val sha256 = sha256Deferred.await()
        val hexDump = hexDumpDeferred.await()
        val textPreview = textPreviewDeferred.await()

        val isBinaryPlaceholder = textPreview.startsWith("Este archivo contiene datos binarios") || textPreview == "[Archivo vacío]"
        val isXml = targetFile.name.endsWith(".xml", ignoreCase = true)
        val isAxml = isXml && (textPreview.contains("<manifest") || textPreview.contains("<?xml") || textPreview.contains("<"))
        val isEditable = !isBinaryPlaceholder && textPreview.isNotBlank() && !targetFile.name.endsWith(".so") && !isImageFile && !isAudioFile

        val displayContent = when {
          isImageFile -> "Activo de imagen (${com.example.model.formatBytes(size)})\nRenderizado con Coil (soporta PNG, JPG, WebP, GIF, SVG e ICO)"
          isAudioFile -> "Activo de audio (${com.example.model.formatBytes(size)})\nReproductor multimedia AndroidX Media3 ExoPlayer"
          else -> textPreview
        }

        _fileDetail.value = FileDetailState(
          isLoading = false,
          fileName = targetFile.name,
          relativePath = relativePath,
          absolutePath = targetFile.absolutePath,
          sizeBytes = size,
          sha256 = sha256,
          textContent = displayContent,
          hexDump = hexDump,
          isEditable = isEditable,
          isAxmlDecoded = isAxml,
          isDex = false,
          isSo = false,
          isImage = isImageFile,
          isAudio = isAudioFile
        )
      }
    }
  }

  fun switchDexClass(clazz: DexClassItem) {
    viewModelScope.launch {
      val current = _fileDetail.value
      if (!current.isDex) return@launch

      _fileDetail.value = current.copy(isDexDecompiling = true, selectedDexClass = clazz)
      val content = withContext(AppDispatchers.ComputeDispatcher) {
        when (current.dexViewMode) {
          DexViewMode.SMALI -> repository.disassembleDexToSmali(current.absolutePath, clazz.typeDescriptor)
          DexViewMode.JAVA -> repository.decompileDexToJava(current.absolutePath, clazz.typeDescriptor)
        }
      }
      _fileDetail.value = _fileDetail.value.copy(
        isDexDecompiling = false,
        textContent = content
      )
    }
  }

  fun switchDexViewMode(mode: DexViewMode) {
    viewModelScope.launch {
      val current = _fileDetail.value
      if (!current.isDex || current.dexViewMode == mode) return@launch

      _fileDetail.value = current.copy(isDexDecompiling = true, dexViewMode = mode)
      val descriptor = current.selectedDexClass?.typeDescriptor
      val content = withContext(AppDispatchers.ComputeDispatcher) {
        when (mode) {
          DexViewMode.SMALI -> repository.disassembleDexToSmali(current.absolutePath, descriptor)
          DexViewMode.JAVA -> if (descriptor != null) {
            repository.decompileDexToJava(current.absolutePath, descriptor)
          } else {
            "// Selecciona una clase para ver el código Java descompilado"
          }
        }
      }
      _fileDetail.value = _fileDetail.value.copy(
        isDexDecompiling = false,
        textContent = content
      )
    }
  }

  fun switchSoViewMode(mode: SoViewMode) {
    viewModelScope.launch {
      val current = _fileDetail.value
      if (!current.isSo || current.soViewMode == mode) return@launch

      _fileDetail.value = current.copy(isSoAnalyzing = true, soViewMode = mode)
      val content = withContext(AppDispatchers.ComputeDispatcher) {
        when (mode) {
          SoViewMode.HEADER -> repository.getElfHeader(current.absolutePath)
          SoViewMode.SYMBOLS -> repository.getElfSymbols(current.absolutePath).second
          SoViewMode.DEPENDENCIES -> repository.getElfDependencies(current.absolutePath).second
          SoViewMode.DISASSEMBLY -> repository.disassembleElf(current.absolutePath)
          SoViewMode.STRINGS -> repository.extractElfStrings(current.absolutePath)
        }
      }
      _fileDetail.value = _fileDetail.value.copy(
        isSoAnalyzing = false,
        textContent = content
      )
    }
  }

  fun switchSoSymbol(symbol: ElfSymbolItem) {
    viewModelScope.launch {
      val current = _fileDetail.value
      if (!current.isSo) return@launch

      _fileDetail.value = current.copy(
        isSoAnalyzing = true,
        selectedSoSymbol = symbol,
        soViewMode = SoViewMode.DISASSEMBLY
      )
      val content = withContext(AppDispatchers.ComputeDispatcher) {
        val disasm = repository.disassembleElf(current.absolutePath)
        """
          ====================================================
             SÍMBOLO SELECCIONADO: ${symbol.name}
             Tipo: ${if (symbol.isJni) "Función JNI Nativa" else if (symbol.isExport) "Exportado" else "Importado"}
             Motor: Capstone Disassembler & Goblin
          ====================================================

          $disasm
        """.trimIndent()
      }
      _fileDetail.value = _fileDetail.value.copy(
        isSoAnalyzing = false,
        textContent = content
      )
    }
  }

  fun switchArscViewMode(mode: ArscViewMode) {
    _fileDetail.value = _fileDetail.value.copy(arscViewMode = mode)
  }

  fun filterArscByType(typeName: String?) {
    _fileDetail.value = _fileDetail.value.copy(arscFilterType = typeName)
  }

  fun setArscSearchQuery(query: String) {
    _fileDetail.value = _fileDetail.value.copy(arscSearchQuery = query)
  }

  fun switchBinaryEditMode(mode: BinaryEditMode) {
    val current = _fileDetail.value
    if (!current.isBinaryData) return
    val newContent = when (mode) {
      BinaryEditMode.HEX -> current.binaryHexEditable
      BinaryEditMode.TEXT -> current.binaryTextEditable
      BinaryEditMode.INSPECTOR -> current.textContent
    }
    _fileDetail.value = current.copy(
      binaryEditMode = mode,
      textContent = newContent
    )
  }

  fun updateBinaryHexContent(hex: String) {
    _fileDetail.value = _fileDetail.value.copy(binaryHexEditable = hex)
  }

  fun updateBinaryTextContent(text: String) {
    _fileDetail.value = _fileDetail.value.copy(binaryTextEditable = text)
  }

  fun saveBinaryContent(
    projectId: String,
    relativePath: String,
    content: String,
    mode: BinaryEditMode,
    onComplete: (Boolean, String?) -> Unit = { _, _ -> }
  ) {
    viewModelScope.launch {
      val currentState = _fileDetail.value
      _fileDetail.value = currentState.copy(isSaving = true)

      val saveResult = when (mode) {
        BinaryEditMode.HEX -> repository.saveBinaryHex(currentState.absolutePath, content)
        BinaryEditMode.TEXT -> repository.saveBinaryText(currentState.absolutePath, content)
        BinaryEditMode.INSPECTOR -> Result.failure(IllegalStateException("El inspector es de solo lectura"))
      }

      if (saveResult.isSuccess) {
        val newBinaryResult = repository.parseBinaryData(currentState.absolutePath)
        val newSha256 = repository.calculateSha256(currentState.absolutePath)
        val newHex = repository.readHexDump(currentState.absolutePath)
        _fileDetail.value = currentState.copy(
          isSaving = false,
          sizeBytes = newBinaryResult.fileSize,
          sha256 = newSha256,
          hexDump = newHex,
          binaryDataResult = newBinaryResult,
          binaryHexEditable = if (mode == BinaryEditMode.HEX) content else newBinaryResult.hexFormatted,
          binaryTextEditable = if (mode == BinaryEditMode.TEXT) content else newBinaryResult.textContent,
          textContent = if (mode == BinaryEditMode.HEX) content else newBinaryResult.textContent
        )
        refreshProjectsAndStorage()
        onComplete(true, null)
      } else {
        _fileDetail.value = currentState.copy(isSaving = false)
        onComplete(false, saveResult.exceptionOrNull()?.message)
      }
    }
  }

  fun saveFileContent(projectId: String, relativePath: String, newContent: String, onComplete: (Boolean) -> Unit = {}) {
    val currentState = _fileDetail.value
    if (currentState.isBinaryData) {
      saveBinaryContent(projectId, relativePath, newContent, currentState.binaryEditMode) { success, _ ->
        onComplete(success)
      }
      return
    }

    viewModelScope.launch {
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
