package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.FileUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

@HiltViewModel
class FileListViewModel @Inject constructor(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<List<FileUiState>>(emptyList())
    val uiState = _uiState.asStateFlow()

    fun loadFileList(manifestIndex: Int) {
        _uiState.value = emptyList()

        val currentStorage = manager.storage ?: return

        val manifest = currentStorage.getManifest(manifestIndex).getOrNull() ?: return

        viewModelScope.launch {
            val list = (0 until manifest.fileCount).map { i ->
                val file = manifest.getFileMetaData(i).getOrNull()
                val imageData = currentStorage.getContentData(manifestIndex, i, 0).getOrNull()

                FileUiState(
                    fileName = file?.originalFileName ?: "不明",
                    contentCount = file?.contentCount ?: 0,
                    fileSize = file?.size ?: 0,
                    imageData = imageData
                )
            }

            _uiState.value = list
        }
    }
}