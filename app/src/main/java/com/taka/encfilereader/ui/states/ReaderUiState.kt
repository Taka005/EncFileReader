package com.taka.encfilereader.ui.states

data class ReaderUiState(
    val title: String? = null,
    val position: Int = 0,
    val pageCount: Int = 0,
    val loadedImages: Map<Int, ByteArray> = emptyMap()
)