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

        this.password = password
        this.storage = StorageService(baseUrl)

        loadManifestList()
    }

    fun loadManifestList(){
        val currentStorage = this.storage ?: return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            val result = currentStorage.downloadManifestList()

            result.onSuccess {
                _uiState.value = UiState.Success
            }.onFailure { e ->
                _uiState.value = UiState.Error(e.message ?: "不明なエラーが発生しました",ErrorType.BASE_URL)
            }
        }
    }
}

enum class ErrorType { NONE, BASE_URL, PASSWORD }

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.NONE
    ) : UiState()
}

val localContext = staticCompositionLocalOf<MainViewModel> {
    error("MainViewModelが提供されていません")
}