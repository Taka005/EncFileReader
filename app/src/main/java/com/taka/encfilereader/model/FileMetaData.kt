package com.taka.encfilereader.model

import kotlinx.serialization.Serializable

@Serializable
data class FileMetaData(
    val fileName: String,
    val originalFileName: String,
    val contents: Array<ContentMetaData>
) {
    val contentCount: Int
        get() = contents.size

    val size: Int
        get() = contents.fold(0) { sum, element -> sum + element.size }

    fun getContentMetaData(
        index: Int
    ): Result<ContentMetaData>{
        val contentMetaData = this.contents.getOrNull(index) ?: return Result.failure(
            IndexOutOfBoundsException("コンテンツの指定が範囲外です")
        )

        return Result.success(contentMetaData)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileMetaData

        if (fileName != other.fileName) return false
        if (originalFileName != other.originalFileName) return false
        if (!contents.contentEquals(other.contents)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + originalFileName.hashCode()
        result = 31 * result + contents.contentHashCode()
        return result
    }
}
