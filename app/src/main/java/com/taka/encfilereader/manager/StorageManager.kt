package com.taka.encfilereader.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.taka.encfilereader.service.StorageService
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "settings")

class StorageManager(private val context: Context){
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val passwordKey = stringPreferencesKey("password")

    private var _storage: StorageService? = null
    private var _password: String? = null

    val storage: StorageService?
        get() = _storage

    val password: String?
        get() = _password

    suspend fun setCredentials(baseUrl: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[baseUrlKey] = baseUrl
            prefs[passwordKey] = password
        }

        _storage = StorageService(baseUrl)
        _password = password
    }

    suspend fun loadCredentials(): Boolean {
        val prefs = context.dataStore.data.first()

        val baseUrl = prefs[baseUrlKey]
        val password = prefs[passwordKey]

        return if (baseUrl != null && password != null) {
            _storage = StorageService(baseUrl)
            _password = password

            true
        } else {
            false
        }
    }

    suspend fun resetCredentials(){
        context.dataStore.edit { prefs ->
            prefs.remove(baseUrlKey)
            prefs.remove(passwordKey)
        }

        _storage = null
        _password = null
    }
}