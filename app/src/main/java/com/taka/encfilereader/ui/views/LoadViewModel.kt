package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoadViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun loadData() {
        val currentStorage = manager.storage ?: return
        val currentPassword = manager.password ?: return
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