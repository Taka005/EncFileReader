package com.taka.encfilereader.manager

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context){
    val baseUrlKey = stringPreferencesKey("base_url")
    val passwordKey = stringPreferencesKey("password")
    val displayColumnsKey = stringPreferencesKey("displayColumnsKey")
    val maxRequestsKey = stringPreferencesKey("maxRequestsKey")

    suspend fun setValue(key: Preferences.Key<String>, value: String){
        context.dataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    suspend fun getValue(key: Preferences.Key<String>): String? {
        val prefs = context.dataStore.data.first()

        return prefs[key]
    }

    suspend fun reset(){
        context.dataStore.edit { prefs ->
            prefs.remove(baseUrlKey)
            prefs.remove(passwordKey)
            prefs.remove(displayColumnsKey)
            prefs.remove(maxRequestsKey)
        }
    }
}