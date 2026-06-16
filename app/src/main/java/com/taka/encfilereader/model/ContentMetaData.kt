package com.taka.encfilereader.model

data class ContentMetaData(
    val name: String,
    val start: Int,
    val end: Int,
    val iv: String,
    val tag: String
)
