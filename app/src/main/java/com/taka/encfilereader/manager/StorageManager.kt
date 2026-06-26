package com.taka.encfilereader.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.taka.encfilereader.service.StorageService
import androidx.datastore.preferences.preferencesDataStore
import com.taka.encfilereader.service.ContentCacheService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "settings")

class StorageManager(private val context: Context){
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val passwordKey = stringPreferencesKey("password")
    private var _storage: StorageService? = null
    private var _password: String? = null
    private val cacheService = ContentCacheService(context.cacheDir)
    private val lock = Mutex()

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

    suspend fun getContentData(
        manifestIndex: Int,
        fileIndex: Int,
        contentIndex: Int
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        val currentStorage = storage ?: return@withContext Result.failure(Exception("ストレージが初期化されていません"))

        val cacheKey = "${manifestIndex}_${fileIndex}_${contentIndex}"

        val cachedData = cacheService.get(cacheKey)

        if (cachedData != null) {
            return@withContext Result.success(cachedData)
        }

        lock.withLock {
            val data = currentStorage.getContentData(manifestIndex, fileIndex, contentIndex).getOrElse { error ->
                return@withContext Result.failure(error)
            }

            cacheService.save(cacheKey, data)

            return@withContext Result.success(data)
        }
    }
}