package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.ReaderUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReaderViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState())
    val uiState = _uiState.asStateFlow()

    private val _title: MutableStateFlow<String?> = MutableStateFlow(null)
    val title = _title.asStateFlow()

    fun loadContent(manifestIndex: Int,fileIndex: Int, newPosition: Int = 0){
        val currentStorage = manager.storage ?: run {
            return
        }

        val manifest = currentStorage.getManifest(manifestIndex).getOrNull() ?: return

        val file = manifest.getFileMetaData(fileIndex).getOrNull() ?: return

        _title.value = file.originalFileName

        viewModelScope.launch {
            val nextAfter = manager.getContentData(manifestIndex, fileIndex, newPosition + 1,false).getOrNull()
            val nextNow = manager.getContentData(manifestIndex, fileIndex, newPosition,false).getOrNull()
            val nextBefore = manager.getContentData(manifestIndex, fileIndex, newPosition - 1,false).getOrNull()

            val newState = ReaderUiState(
                position = newPosition,
                after = nextAfter ?: _uiState.value.after,
                now = nextNow ?: _uiState.value.now,
                before = nextBefore ?: _uiState.value.before
            )

            if (newState != _uiState.value) {
                _uiState.value = newState
            }
        }
    }
}