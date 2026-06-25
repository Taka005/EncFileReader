package com.taka.encfilereader.ui.views

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.ErrorType
import com.taka.encfilereader.ui.states.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SetupViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun setSettings(baseUrl: String, password: String) {
        viewModelScope.launch {
            manager.resetCredentials()
        }

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

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            manager.setCredentials(baseUrl, password)

            _uiState.value = UiState.Success
        }
    }
}