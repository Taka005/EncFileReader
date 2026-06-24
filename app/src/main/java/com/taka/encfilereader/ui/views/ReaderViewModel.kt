package com.taka.encfilereader.ui.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taka.encfilereader.manager.StorageManager
import com.taka.encfilereader.ui.states.ReaderUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val manager: StorageManager
) : ViewModel() {
    private val _uiState = MutableStateFlow<ReaderUiState>(ReaderUiState())
    val uiState = _uiState.asStateFlow()

    fun loadContent(manifestIndex: Int,fileIndex: Int, newPosition: Int = 0){
        val currentStorage = manager.storage ?: return

        viewModelScope.launch {
            val after = currentStorage.getContentData(manifestIndex, fileIndex, newPosition + 1).getOrNull()
            val now = currentStorage.getContentData(manifestIndex, fileIndex, newPosition).getOrNull() ?: _uiState.value.now
            val before = currentStorage.getContentData(manifestIndex, fileIndex, newPosition - 1).getOrNull()

            _uiState.value = ReaderUiState(newPosition, after, now, before)
        }
    }
}