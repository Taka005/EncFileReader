package com.taka.encfilereader.ui.states

data class FileUiState(
    val fileName: String,
    val fileSize: Int,
    val contentCount: Int,
    val imageData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileUiState

        if (fileSize != other.fileSize) return false
        if (contentCount != other.contentCount) return false
        if (fileName != other.fileName) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileSize
        result = 31 * result + contentCount
        result = 31 * result + fileName.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}