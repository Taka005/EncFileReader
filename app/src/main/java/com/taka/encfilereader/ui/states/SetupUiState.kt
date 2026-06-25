package com.taka.encfilereader.ui.states

sealed class SetupUiState {
    object Initial : SetupUiState()
    object Loading : SetupUiState()
    object Success : SetupUiState()
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.NONE
    ) : SetupUiState()
}