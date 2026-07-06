package com.taka.encfilereader.manager

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.taka.encfilereader.service.StorageService
import androidx.datastore.preferences.preferencesDataStore
import com.taka.encfilereader.model.Manifest
import com.taka.encfilereader.service.ContentCacheService
import com.taka.encfilereader.service.ManifestCacheService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "settings")

class StorageManager(private val context: Context){
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val passwordKey = stringPreferencesKey("password")
    private val displayColumnsKey = stringPreferencesKey("displayColumnsKey")
    private val maxRequestsKey = stringPreferencesKey("maxRequestsKey")

    private var _storage: StorageService? = null
    private var _password: String? = null
    private var _displayColumns: Int = 2
    private val lock = Mutex()
    val manifestCacheService = ManifestCacheService(context.cacheDir)
    val contentCacheService = ContentCacheService(context.cacheDir)

    val storage: StorageService?
        get() = _storage

    val password: String?
        get() = _password

    val displayColumns: Int
        get() = _displayColumns

    val maxRequests: Int
        get() = storage?.maxRequests ?: 50

    suspend fun updateDisplayColumns(value: Int) {
        if (value <= 0) return

        _displayColumns = value

        context.dataStore.edit { prefs ->
            prefs[displayColumnsKey] = value.toString()
        }
    }

    suspend fun updateMaxRequests(value: Int) {
        if (value <= 0) return

        context.dataStore.edit { prefs ->
            prefs[maxRequestsKey] = value.toString()
        }

        storage?.maxRequests = value
        storage?.resetApiClient()
    }

    suspend fun setCredentials(baseUrl: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[baseUrlKey] = baseUrl
            prefs[passwordKey] = password
        }

        _storage = StorageService(baseUrl,maxRequests)
        _password = password
    }

    suspend fun loadCredentials(): Boolean {
        val prefs = context.dataStore.data.first()

        val baseUrl = prefs[baseUrlKey]
        val password = prefs[passwordKey]

        updateDisplayColumns(prefs[displayColumnsKey]?.toInt() ?: displayColumns)
        updateMaxRequests(prefs[maxRequestsKey]?.toInt() ?: maxRequests)

        return if (
            baseUrl != null &&
            password != null
        ) {
            _storage = StorageService(baseUrl,maxRequests)
            _password = password

            true
        } else {
            false
        }
    }

    suspend fun loadManifest(index: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val currentStorage = storage ?: return@withContext Result.failure(Exception("ストレージが初期化されていません"))
        val password = _password ?: return@withContext Result.failure(Exception("パスワードが設定されていません"))

        val manifest = currentStorage.getManifest(index).getOrElse { return@withContext Result.failure(it) }

        val cachedData = manifestCacheService.get(manifest.dirName)

        val data = if (cachedData != null) {
            cachedData
        } else {
            val downloaded = currentStorage.fetchRawManifestData(manifest.dirName).getOrElse {
                return@withContext Result.failure(it)
            }

            manifestCacheService.save(manifest.dirName, downloaded)

            downloaded
        }

        return@withContext manifest.setBuffer(data, password)
    }

    suspend fun checkValidPassword(password: String): Result<Unit> {
        val storage = storage ?: return Result.failure(Exception("ストレージが初期化されていません"))

        val manifest = storage.getManifest(0).getOrElse {
            return Result.failure(it)
        }

        val copyManifest = Manifest(manifest.dirName)

        val cachedData = manifestCacheService.get(copyManifest.dirName)

        val data = if (cachedData != null) {
            cachedData
        } else {
            val downloaded = storage.fetchRawManifestData(copyManifest.dirName).getOrElse {
                return Result.failure(it)
            }

            downloaded
        }

        return copyManifest.setBuffer(data, password)
    }

    suspend fun resetCredentials(){
        context.dataStore.edit { prefs ->
            prefs.remove(baseUrlKey)
            prefs.remove(passwordKey)
            prefs.remove(displayColumnsKey)
            prefs.remove(maxRequestsKey)
        }

        _storage = null
        _password = null
        updateDisplayColumns(2)
        updateMaxRequests(50)
    }

    suspend fun getContentData(
        manifestIndex: Int,
        fileIndex: Int,
        contentIndex: Int,
        isDiskCache: Boolean = true
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        val currentStorage = storage ?: return@withContext Result.failure(Exception("ストレージが初期化されていません"))

        val cacheKey = "${manifestIndex}_${fileIndex}_${contentIndex}"

        val cachedData = contentCacheService.get(cacheKey)

        if (cachedData != null) {
            return@withContext Result.success(cachedData)
        }

        lock.withLock {
            val data = currentStorage.getContentData(manifestIndex, fileIndex, contentIndex).getOrElse { error ->
                return@withContext Result.failure(error)
            }

            contentCacheService.save(cacheKey, data, isDiskCache)

            return@withContext Result.success(data)
        }
    }

    fun close(){
        contentCacheService.close()
        manifestCacheService.close()
    }
}