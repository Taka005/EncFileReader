package com.taka.encfilereader.ui.views

import android.util.Patterns
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.service.StorageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState
    private var password: String? = null
    private var storage: StorageService? = null

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
}

enum class ErrorType { NONE, BASE_URL, PASSWORD }

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Progress(val current: Int, val total: Int) : UiState()
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.NONE
    ) : UiState()
}

val localContext = staticCompositionLocalOf<MainViewModel> {
    error("MainViewModelが提供されていません")
}