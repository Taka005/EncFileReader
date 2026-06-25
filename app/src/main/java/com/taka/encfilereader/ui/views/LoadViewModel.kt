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
        val currentStorage = manager.storage ?: run {
            _uiState.value = UiState.Error("ストレージが初期化されていません")
            return
        }

        val currentPassword = manager.password ?: run {
            _uiState.value = UiState.Error("パスワードが設定されていません")
            return
        }

        viewModelScope.launch {
            currentStorage.downloadManifestList().getOrElse { error ->
                _uiState.value = UiState.Error(error.message ?: "マニフェストリストがダウンロードできませんでした")
                return@launch
            }

            currentStorage.checkValidPassword(currentPassword).getOrElse { error ->
                _uiState.value = UiState.Error(error.message ?: "パスワードが正しくありません")
                return@launch
            }

            val total = currentStorage.manifestCount

            _uiState.value = UiState.Progress(0, total)

            for (i in 0 until total) {
                val result = currentStorage.downloadManifestData(currentPassword, i)

                if (result.isFailure) {
                    _uiState.value = UiState.Error("ダウンロードに失敗しました")
                    return@launch
                }

                _uiState.value = UiState.Progress(i + 1, total)
            }

            _uiState.value = UiState.Success
        }
    }
}