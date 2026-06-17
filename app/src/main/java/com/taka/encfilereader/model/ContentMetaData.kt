package com.taka.encfilereader.model

import kotlinx.serialization.Serializable

@Serializable
data class ContentMetaData(
    val name: String,
    val start: Int,
    val end: Int,
    val iv: String,
    val tag: String
)
