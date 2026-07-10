package com.taka.encfilereader.ui.states

data class HistoryUiState (
    val manifestIndex: Int,
    val fileIndex: Int,
    val dirName: String,
    val fileName: String,
    val contentCount: Int,
    val position: Int,
    val timestamp: Long,
    val imageData: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HistoryUiState

        if (manifestIndex != other.manifestIndex) return false
        if (fileIndex != other.fileIndex) return false
        if (contentCount != other.contentCount) return false
        if (position != other.position) return false
        if (timestamp != other.timestamp) return false
        if (dirName != other.dirName) return false
        if (fileName != other.fileName) return false
        if (!imageData.contentEquals(other.imageData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = manifestIndex
        result = 31 * result + fileIndex
        result = 31 * result + contentCount
        result = 31 * result + position
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + dirName.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + (imageData?.contentHashCode() ?: 0)
        return result
    }
}