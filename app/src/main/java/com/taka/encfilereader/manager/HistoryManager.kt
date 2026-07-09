package com.taka.encfilereader.manager

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class HistoryItem(val manifestIndex: Int, val fileIndex: Int, val position: Int)

private val Context.dataStore by preferencesDataStore(name = "history")

class HistoryManager(private val context: Context) {
    private fun getHistoryKey(manifestIndex: Int, fileIndex: Int): Preferences.Key<Int> {
        return intPreferencesKey("history_${manifestIndex}_${fileIndex}")
    }

    suspend fun savePosition(manifestIndex: Int, fileIndex: Int, position: Int) {
        context.dataStore.edit { prefs ->
            prefs[getHistoryKey(manifestIndex, fileIndex)] = position
        }
    }

    suspend fun getPosition(manifestIndex: Int, fileIndex: Int): Int? {
        return context.dataStore.data.map { prefs ->
            prefs[getHistoryKey(manifestIndex, fileIndex)]
        }.first()
    }

    suspend fun getAllHistories(): List<HistoryItem> {
        return context.dataStore.data.map { prefs ->
            prefs.asMap().mapNotNull { (key, value) ->
                if (key.name.startsWith("history_")) {
                    val parts = key.name.removePrefix("history_").split("_")

                    if (parts.size == 2) {
                        val mIdx = parts[0].toIntOrNull()
                        val fIdx = parts[1].toIntOrNull()

                        if (mIdx != null && fIdx != null && value is Int) {
                            HistoryItem(mIdx, fIdx, value)
                        } else null
                    } else null
                } else null
            }
        }.first()
    }
}