package com.taka.encfilereader.ui.states

data class ReaderUiState(
    val position: Int = 0,
    val after: ByteArray? = null,
    val now: ByteArray? = null,
    val before: ByteArray? = null
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReaderUiState

        if (position != other.position) return false
        if (!after.contentEquals(other.after)) return false
        if (!now.contentEquals(other.now)) return false
        if (!before.contentEquals(other.before)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = position
        result = 31 * result + (after?.contentHashCode() ?: 0)
        result = 31 * result + (now?.contentHashCode() ?: 0)
        result = 31 * result + (before?.contentHashCode() ?: 0)
        return result
    }
}