package com.taka.encfilereader.model

import kotlinx.serialization.Serializable

@Serializable
data class ManifestMetaData(
    val originalDirName: String,
    val files: Array<FileMetaData>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ManifestMetaData

        if (originalDirName != other.originalDirName) return false
        if (!files.contentEquals(other.files)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = originalDirName.hashCode()
        result = 31 * result + files.contentHashCode()
        return result
    }
}
