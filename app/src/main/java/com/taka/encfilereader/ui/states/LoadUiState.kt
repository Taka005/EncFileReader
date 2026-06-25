package com.taka.encfilereader.ui.states

sealed class LoadUiState {
    object Initial : LoadUiState()
    object Success : LoadUiState()
    data class Progress(val current: Int, val total: Int) : LoadUiState()
    data class Error(val message: String) : LoadUiState()
}