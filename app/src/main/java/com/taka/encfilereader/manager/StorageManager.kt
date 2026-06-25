package com.taka.encfilereader.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.taka.encfilereader.service.StorageService
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "settings")

class StorageManager(private val context: Context){
    private val BaseUrlKey = stringPreferencesKey("base_url")
    private val PasswordKey = stringPreferencesKey("password")

    private var _storage: StorageService? = null
    private var _password: String? = null

    val storage: StorageService?
        get() = _storage

    val password: String?
        get() = _password

    suspend fun setCredentials(baseUrl: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[BaseUrlKey] = baseUrl
            prefs[PasswordKey] = password
        }

        _storage = StorageService(baseUrl)
    }

    suspend fun loadCredentials(): Boolean {
        val prefs = context.dataStore.data.first()

        val baseUrl = prefs[BaseUrlKey]
        val password = prefs[PasswordKey]

        return if (baseUrl != null && password != null) {
            _storage = StorageService(baseUrl)

            true
        } else {
            false
        }
    }
}