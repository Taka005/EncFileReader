package com.taka.encfilereader.ui.states

enum class ErrorType { NONE, BASE_URL, PASSWORD }

sealed class UiState {
    object Initial : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Progress(val current: Int, val total: Int) : UiState()
    data class Error(
        val message: String,
        val type: ErrorType = ErrorType.NONE
    ) : UiState()
}