package com.taka.encfilereader.manager

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class HistoryItem(
    val manifestIndex: Int,
    val fileIndex: Int,
    val position: Int,
    val timestamp: Long
)

private val Context.dataStore by preferencesDataStore(name = "history")

class HistoryManager(private val context: Context) {
    private fun getHistoryKey(manifestIndex: Int, fileIndex: Int): Preferences.Key<String> {
        return stringPreferencesKey("history_${manifestIndex}_${fileIndex}")
    }

    suspend fun savePosition(manifestIndex: Int, fileIndex: Int, position: Int) {
        context.dataStore.edit { prefs ->
            prefs[getHistoryKey(manifestIndex, fileIndex)] = "$position:${System.currentTimeMillis()}"
        }
    }

    suspend fun getPosition(manifestIndex: Int, fileIndex: Int): Int? {
        return context.dataStore.data.map { prefs ->
            val key = stringPreferencesKey("history_${manifestIndex}_${fileIndex}")
            val rawValue = prefs[key]

            rawValue?.split(":")?.getOrNull(0)?.toIntOrNull()
        }.first()
    }

    suspend fun getAllHistories(): List<HistoryItem> {
        return context.dataStore.data.map { prefs ->
            prefs.asMap().mapNotNull { (key, value) ->
                if (key.name.startsWith("history_") && value is String) {
                    val parts = key.name.removePrefix("history_").split("_")
                    val dataParts = value.split(":")

                    if (parts.size == 2 && dataParts.size == 2) {
                        val mIdx = parts[0].toIntOrNull()
                        val fIdx = parts[1].toIntOrNull()
                        val pos = dataParts[0].toIntOrNull()
                        val time = dataParts[1].toLongOrNull()

                        if (mIdx != null && fIdx != null && pos != null && time != null) {
                            HistoryItem(mIdx, fIdx, pos, time)
                        } else null
                    } else null
                } else null
            }.sortedByDescending { it.timestamp }
        }.first()
    }

    suspend fun reset() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}