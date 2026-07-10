package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import com.taka.encfilereader.manager.StorageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.ui.states.HistoryUiState
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val manager: StorageManager
): ViewModel(){
    private val _histories = MutableStateFlow<List<HistoryUiState>>(emptyList())
    val histories = _histories.asStateFlow()

    fun loadHistories() {
        viewModelScope.launch {
            _histories.value = manager.historyManager.getAllHistories().mapNotNull { history ->
                val currentStorage = manager.storage ?: return@mapNotNull null

                val manifest = currentStorage.getManifest(history.manifestIndex).getOrNull() ?: return@mapNotNull null

                val file = manifest.getFileMetaData(history.fileIndex).getOrNull() ?: return@mapNotNull null

                val imageData = manager.getContentData(history.manifestIndex, history.fileIndex, 0).getOrNull()

                HistoryUiState(
                    manifestIndex = history.manifestIndex,
                    fileIndex = history.fileIndex,
                    dirName = manifest.originalDirName ?: "",
                    position = history.position,
                    timestamp = history.timestamp,
                    fileName = file.originalFileName,
                    contentCount = file.contentCount,
                    imageData = imageData
                )
            }
        }
    }
}