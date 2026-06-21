package com.taka.encfilereader.ui.states

data class ManifestUiState(
    val title: String,
    val fileCount: Int,
    val imageData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ManifestUiState

        if (fileCount != other.fileCount) return false
        if (title != other.title) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = fileCount
        result = 31 * result + title.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}