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

        viewModelScope.launch {
            val position = manager.historyManager.getPosition(manifestIndex, fileIndex) ?: 0

            _uiState.value = _uiState.value.copy(
                pageCount = file.contentCount,
                title = file.originalFileName,
                position = position
            )

            loadPage(manifestIndex, fileIndex, position)
        }
    }

    fun setPosition(manifestIndex: Int, fileIndex: Int, newPosition: Int) {
        _uiState.value = _uiState.value.copy(position = newPosition)

        unLoadPage(newPosition)

        viewModelScope.launch {
            manager.historyManager.savePosition(manifestIndex, fileIndex, newPosition)
        }
    }

    fun loadPage(manifestIndex: Int, fileIndex: Int, contentIndex: Int) {
        if (contentIndex < 0 || contentIndex >= _uiState.value.pageCount) return
        if (_uiState.value.loadedImages.containsKey(contentIndex) || loadingJobs.containsKey(contentIndex)) return

        loadingJobs[contentIndex] = viewModelScope.launch {
            manager.getContentData(manifestIndex, fileIndex, contentIndex, false).getOrNull()?.let { data ->
                val currentImages = _uiState.value.loadedImages.toMutableMap()
                currentImages[contentIndex] = data

                _uiState.value = _uiState.value.copy(loadedImages = currentImages)
            }

            loadingJobs.remove(contentIndex)
        }
    }

    private fun unLoadPage(currentPosition: Int) {
        val currentImages = _uiState.value.loadedImages

        val toRemove = currentImages.keys.filter { key ->
            abs(key - currentPosition) > 3
        }

        if (toRemove.isNotEmpty()) {
            val newImages = currentImages.toMutableMap()
            toRemove.forEach { newImages.remove(it) }
            _uiState.value = _uiState.value.copy(loadedImages = newImages)
        }
    }
}