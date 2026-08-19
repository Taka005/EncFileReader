package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.ManifestUiState
import com.taka.encfilereader.util.SortType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class ManifestListViewModel(
    private val manager: StorageManager
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()
    private val _sortType = MutableStateFlow(SortType.NONE)
    val sortType = _sortType.asStateFlow()

    private val _uiState = MutableStateFlow<List<ManifestUiState>>(emptyList())
    val uiState = combine(_uiState, _searchQuery, _sortType) { items, query, sort ->
        val filtered = if (query.isBlank()) {
            items
        } else {
            items.filter { it.dirName.contains(query, ignoreCase = true) }
        }

        when (sort) {
            SortType.NONE -> filtered
            SortType.NAME_ASC -> filtered.sortedBy { it.dirName }
            SortType.NAME_DESC -> filtered.sortedByDescending { it.dirName }
        }
    }

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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSortType(order: SortType) {
        _sortType.value = order
    }
}