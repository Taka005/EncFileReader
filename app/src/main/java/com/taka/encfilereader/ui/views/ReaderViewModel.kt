package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.ReaderUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

class ReaderViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState = _uiState.asStateFlow()

    private val loadingJobs = mutableMapOf<Int, Job>()

    fun initialize(manifestIndex: Int, fileIndex: Int) {
        val currentStorage = manager.storage ?: return
        val manifest = currentStorage.getManifest(manifestIndex).getOrNull() ?: return
        val file = manifest.getFileMetaData(fileIndex).getOrNull() ?: return

        _uiState.value = _uiState.value.copy(
            pageCount = file.contentCount,
            title = file.originalFileName
        )

        loadPage(manifestIndex, fileIndex, 0)
    }

    fun setPosition(newPosition: Int) {
        _uiState.value = _uiState.value.copy(position = newPosition)
    }

    fun loadPage(manifestIndex: Int, fileIndex: Int, contentIndex: Int) {
        val state = _uiState.value
        if (contentIndex < 0 || contentIndex >= state.pageCount) return
        if (state.loadedImages.containsKey(contentIndex) || loadingJobs.containsKey(contentIndex)) return

        loadingJobs[contentIndex] = viewModelScope.launch {
            manager.getContentData(manifestIndex, fileIndex, contentIndex, false).getOrNull()?.let { data ->
                val currentImages = _uiState.value.loadedImages.toMutableMap()
                currentImages[contentIndex] = data

                if (currentImages.size > 30) {
                    val keyToRemove = currentImages.keys.minByOrNull { abs(it - contentIndex) }
                    if (keyToRemove != null) currentImages.remove(keyToRemove)
                }

                _uiState.value = _uiState.value.copy(loadedImages = currentImages)
            }

            loadingJobs.remove(contentIndex)
        }
    }
}