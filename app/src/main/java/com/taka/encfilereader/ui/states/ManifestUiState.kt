package com.taka.encfilereader.ui.states

data class ManifestUiState(
    val manifestIndex: Int,
    val dirName: String,
    val fileCount: Int,
    val imageData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ManifestUiState

        if (manifestIndex != other.manifestIndex) return false
        if (fileCount != other.fileCount) return false
        if (dirName != other.dirName) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = manifestIndex
        result = 31 * result + fileCount
        result = 31 * result + dirName.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}