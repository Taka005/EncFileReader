package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.FileUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class FileListViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _title: MutableStateFlow<String?> = MutableStateFlow(null)
    val title = _title.asStateFlow()

    private val _uiState = MutableStateFlow<List<FileUiState>>(emptyList())
    val uiState = combine(_uiState, _searchQuery) { items, query ->
        if (query.isBlank()) {
            items
        } else {
            items.filter { it.fileName.contains(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    suspend fun resetHistory(manifestIndex: Int, fileIndex: Int){
        manager.historyManager.savePosition(manifestIndex, fileIndex,0)
    }

    suspend fun getContentData(manifestIndex: Int, fileIndex: Int,contentIndex: Int): ByteArray?{
        return manager.getContentData(manifestIndex, fileIndex, contentIndex).getOrNull()
    }

    fun loadFileList(manifestIndex: Int) {
        _uiState.value = emptyList()

        val currentStorage = manager.storage ?: return

        val manifest = currentStorage.getManifest(manifestIndex).getOrNull() ?: return

        _title.value = manifest.originalDirName

        viewModelScope.launch {
            _uiState.value = (0 until manifest.fileCount).map { i ->
                val file = manifest.getFileMetaData(i).getOrNull()

                FileUiState(
                    manifestIndex = manifestIndex,
                    fileIndex = i,
                    fileName = file?.originalFileName ?: "不明",
                    contentCount = file?.contentCount ?: 0,
                    fileSize = file?.size ?: 0,
                    positionHistory = manager.historyManager.getPosition(manifestIndex, i),
                    imageData = null
                )
            }

            val deferredList = _uiState.value.map { fileUi ->
                async {
                    val data = manager.getContentData(manifestIndex, fileUi.fileIndex, 0).getOrNull()

                    fileUi.fileIndex to data
                }
            }

            val results = deferredList.awaitAll()

            _uiState.value = _uiState.value.map { fileUi ->
                val loadedData = results.find { it.first == fileUi.fileIndex }?.second
                fileUi.copy(imageData = loadedData)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}