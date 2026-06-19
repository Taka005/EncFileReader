package com.taka.encfilereader.model

import kotlinx.serialization.Serializable

@Serializable
data class ContentMetaData(
    val name: String,
    val start: Int,
    val size: Int,
    val iv: String,
    val tag: String
){
    val end: Int
        get() = this.start + this.size - 1

}
