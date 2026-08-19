package com.taka.encfilereader.util

data class HistoryItem(
    val manifestIndex: Int,
    val fileIndex: Int,
    val position: Int,
    val timestamp: Long
)