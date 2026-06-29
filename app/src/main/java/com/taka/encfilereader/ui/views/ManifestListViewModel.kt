package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.ManifestUiState
import com.taka.encfilereader.ui.states.ProgressUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class ManifestListViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<List<ManifestUiState>>(emptyList())
    val uiState = _uiState.asStateFlow()

    private val _progressUiState = MutableStateFlow(ProgressUiState())
    val progressUiState = _progressUiState.asStateFlow()

    fun loadManifestList() {
        _uiState.value = emptyList()

        val currentStorage = manager.storage ?: return

        _progressUiState.value = ProgressUiState(0,currentStorage.manifestCount)

        viewModelScope.launch {
            val deferredList = (0 until currentStorage.manifestCount).map { i ->
                async {
                    val manifest = currentStorage.getManifest(i).getOrNull()
                    val manifestData = manager.getContentData(i, 0, 0).getOrNull()

                    _progressUiState.value = ProgressUiState(_progressUiState.value.current + 1, currentStorage.manifestCount)

                    ManifestUiState(
                        dirName = manifest?.originalDirName ?: "不明",
                        fileCount = manifest?.fileCount ?: 0,
                        imageData = manifestData
                    )
                }
            }

            _uiState.value = deferredList.awaitAll()
        }
    }
}