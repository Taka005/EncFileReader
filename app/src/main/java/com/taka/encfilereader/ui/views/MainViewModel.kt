package com.taka.encfilereader.ui.views

import android.util.Patterns
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.service.StorageService
import com.taka.encfilereader.ui.states.ErrorType
import com.taka.encfilereader.ui.states.FileUiState
import com.taka.encfilereader.ui.states.ManifestUiState
import com.taka.encfilereader.ui.states.ReaderUiState
import com.taka.encfilereader.ui.states.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState

    private val _manifestUiState = MutableStateFlow<List<ManifestUiState>>(emptyList())
    val manifestUiState: MutableStateFlow<List<ManifestUiState>> = _manifestUiState

    private val _fileUiState = MutableStateFlow<List<FileUiState>>(emptyList())
    val fileUiState: MutableStateFlow<List<FileUiState>> = _fileUiState

    private val _readerUiState = MutableStateFlow(ReaderUiState())
    val readerUiState: MutableStateFlow<ReaderUiState> = _readerUiState

    private var password: String? = null
    var storage: StorageService? = null

    fun setInitSettings(baseUrl: String, password: String){
        if (baseUrl.isBlank()) {
            _uiState.value = UiState.Error("ストレージサーバーのURLを入力してください", ErrorType.BASE_URL)
            return
        }

        if (!Patterns.WEB_URL.matcher(baseUrl).matches()) {
            _uiState.value = UiState.Error("無効なURLです", ErrorType.BASE_URL)
            return
        }

        if (password.isBlank()) {
            _uiState.value = UiState.Error("パスワードを入力してください", ErrorType.PASSWORD)
            return
        }

        val currentStorage = StorageService(baseUrl)

        this.password = password
        this.storage = currentStorage

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            _uiState.value = loadInitData(currentStorage, password)
        }
    }

    private suspend fun loadInitData(storage: StorageService, pass: String): UiState {
        storage.downloadManifestList().getOrElse {
            return UiState.Error(it.message ?: "", ErrorType.BASE_URL)
        }

        storage.checkValidPassword(pass).getOrElse {
            return UiState.Error(it.message ?: "", ErrorType.PASSWORD)
        }

        return UiState.Success
    }

    fun loadManifestData(){
        val currentStorage = storage ?: return
        val currentPassword = password ?: return
        val total = currentStorage.manifestCount

        viewModelScope.launch {
            _uiState.value = UiState.Progress(0, total)

            for (i in 0 until total) {
                val result = currentStorage.downloadManifestData(currentPassword, i)

                if (result.isFailure) {
                    _uiState.value = UiState.Error("ダウンロードに失敗しました: ${i+1}番目")
                    return@launch
                }

                _uiState.value = UiState.Progress(i + 1, total)
            }

            _uiState.value = UiState.Success
        }
    }

    fun loadManifestList() {
        _manifestUiState.value = emptyList()

        val currentStorage = storage ?: return

        viewModelScope.launch {
            val list = (0 until currentStorage.manifestCount).map { i ->
                val manifest = currentStorage.getManifest(i).getOrNull()
                val manifestData = currentStorage.getContentData(i, 0, 0).getOrNull()

                ManifestUiState(
                    dirName = manifest?.originalDirName ?: "不明",
                    fileCount = manifest?.fileCount ?: 0,
                    imageData = manifestData
                )
            }

            _manifestUiState.value = list
        }
    }

    fun loadFileList(manifestIndex: Int) {
        _fileUiState.value = emptyList()

        val currentStorage = storage ?: return

        val manifest = currentStorage.getManifest(manifestIndex).getOrNull() ?: return

        viewModelScope.launch {
            val list = (0 until manifest.fileCount).map { i ->
                val file = manifest.getFileMetaData(i).getOrNull()
                val imageData = currentStorage.getContentData(manifestIndex, i, 0).getOrNull()

                FileUiState(
                    fileName = file?.originalFileName ?: "不明",
                    contentCount = file?.contentCount ?: 0,
                    fileSize = file?.size ?: 0,
                    imageData = imageData
                )
            }

            _fileUiState.value = list
        }
    }

    fun loadReaderContent(manifestIndex: Int,fileIndex: Int, newPosition: Int = 0){
        val currentStorage = storage ?: return

        viewModelScope.launch {
            val after = currentStorage.getContentData(manifestIndex, fileIndex, newPosition + 1).getOrNull()
            val now = currentStorage.getContentData(manifestIndex, fileIndex, newPosition).getOrNull() ?: _readerUiState.value.now
            val before = currentStorage.getContentData(manifestIndex, fileIndex, newPosition - 1).getOrNull()

            _readerUiState.value = ReaderUiState(newPosition, after, now, before)
        }
    }
}

val localContext = staticCompositionLocalOf<MainViewModel> {
    error("MainViewModelが提供されていません")
}