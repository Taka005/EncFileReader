package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.LoadUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoadViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoadUiState>(LoadUiState.Initial)
    val uiState = _uiState.asStateFlow()

    fun loadData() {
        val currentStorage = manager.storage ?: run {
            _uiState.value = LoadUiState.Error("ストレージが初期化されていません")
            return
        }

        val currentPassword = manager.password ?: run {
            _uiState.value = LoadUiState.Error("パスワードが設定されていません")
            return
        }

        viewModelScope.launch {
            currentStorage.downloadManifestList().getOrElse { error ->
                _uiState.value = LoadUiState.Error(error.message ?: "マニフェストリストがダウンロードできませんでした")
                return@launch
            }

            currentStorage.checkValidPassword(currentPassword).getOrElse { error ->
                _uiState.value = LoadUiState.Error(error.message ?: "パスワードが正しくありません")
                return@launch
            }

            val total = currentStorage.manifestCount
            var completedCount = 0

            _uiState.value = LoadUiState.Progress(0, total)

            val jobs = (0 until total).map { i ->
                async {
                    val result = manager.loadManifest(i)

                    if (result.isSuccess) {
                        synchronized(this) {
                            completedCount++
                        }

                        _uiState.value = LoadUiState.Progress(completedCount, total)
                    }

                    result
                }
            }

            val results = jobs.awaitAll()

            if (results.any { it.isFailure }) {
                _uiState.value = LoadUiState.Error("ダウンロードに失敗しました")
            } else {
                _uiState.value = LoadUiState.Success
            }
        }
    }
}