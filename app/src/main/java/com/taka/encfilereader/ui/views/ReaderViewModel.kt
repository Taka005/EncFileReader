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

    fun loadContent(manifestIndex: Int,fileIndex: Int, newPosition: Int = 0){
        viewModelScope.launch {
            val nextAfter = manager.getContentData(manifestIndex, fileIndex, newPosition + 1).getOrNull()
            val nextNow = manager.getContentData(manifestIndex, fileIndex, newPosition).getOrNull()
            val nextBefore = manager.getContentData(manifestIndex, fileIndex, newPosition - 1).getOrNull()

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