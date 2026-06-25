package com.taka.encfilereader.ui.states

sealed class StartUiState {
    object Initial : StartUiState()
    object Success : StartUiState()
    object Error : StartUiState()
}