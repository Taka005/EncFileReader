package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.FileUiState
import com.taka.encfilereader.ui.states.ProgressUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class FileListViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<List<FileUiState>>(emptyList())
    val uiState = _uiState.asStateFlow()

    private val _progressUiState = MutableStateFlow(ProgressUiState())
    val progressUiState = _progressUiState.asStateFlow()

    private val _title: MutableStateFlow<String?> = MutableStateFlow(null)
    val title = _title.asStateFlow()

    suspend fun resetHistory(manifestIndex: Int, fileIndex: Int){
        manager.historyManager.savePosition(manifestIndex, fileIndex,0)
    }

    fun loadFileList(manifestIndex: Int) {
        _uiState.value = emptyList()

        val currentStorage = manager.storage ?: return

        val manifest = currentStorage.getManifest(manifestIndex).getOrNull() ?: return

        _progressUiState.value = ProgressUiState(0,manifest.fileCount)

        _title.value = manifest.originalDirName

        viewModelScope.launch {
            val deferredList = (0 until manifest.fileCount).map { i ->
                async {
                    val file = manifest.getFileMetaData(i).getOrNull()
                    val imageData = manager.getContentData(manifestIndex, i, 0).getOrNull()

                    _progressUiState.value = ProgressUiState(_progressUiState.value.current + 1,manifest.fileCount)

                    FileUiState(
                        fileIndex = i,
                        fileName = file?.originalFileName ?: "不明",
                        contentCount = file?.contentCount ?: 0,
                        fileSize = file?.size ?: 0,
                        positionHistory = manager.historyManager.getPosition(manifestIndex,i),
                        imageData = imageData
                    )
                }
            }

            _uiState.value = deferredList.awaitAll()
        }
    }
}