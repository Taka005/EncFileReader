package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.ManifestUiState
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

    fun loadManifestList() {
        _uiState.value = emptyList()

        val currentStorage = manager.storage ?: return

        viewModelScope.launch {
            _uiState.value = (0 until currentStorage.manifestCount).map { i ->
                val manifest = currentStorage.getManifest(i).getOrNull()

                ManifestUiState(
                    manifestIndex = i,
                    dirName = manifest?.originalDirName ?: "不明",
                    fileCount = manifest?.fileCount ?: 0,
                    imageData = null
                )
            }

            val deferredList = _uiState.value.map { manifestUi ->
                async {
                    val data = manager.getContentData(manifestUi.manifestIndex, 0, 0).getOrNull()

                    manifestUi.manifestIndex to data
                }
            }

            val results = deferredList.awaitAll()

            _uiState.value = _uiState.value.map { manifestUi ->
                val loadedData = results.find { it.first == manifestUi.manifestIndex }?.second
                manifestUi.copy(imageData = loadedData)
            }
        }
    }
}