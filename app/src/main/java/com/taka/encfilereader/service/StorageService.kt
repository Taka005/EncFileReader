package com.taka.encfilereader.service

import android.util.LruCache
import com.taka.encfilereader.model.Manifest
import com.taka.encfilereader.net.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageService(val baseUrl: String){
    private val manifests: MutableList<Manifest> = mutableListOf()

    private val apiClient = ApiClient(this.baseUrl)

    private val memoryCache = object : LruCache<String, ByteArray>(50 * 1024 * 1024) {
        override fun sizeOf(key: String, value: ByteArray): Int {
            return value.size
        }
    }

    val manifestCount: Int
        get() = this.manifests.size

    fun getManifest(index: Int): Result<Manifest>{
        val manifest = this.manifests.getOrNull(index) ?: return Result.failure(
            IndexOutOfBoundsException("マニフェストの指定が範囲外です")
        )

        return Result.success(manifest)
    }

    suspend fun getContentData(
        manifestIndex: Int,
        fileIndex: Int,
        contentIndex: Int
    ): Result<ByteArray> = withContext(Dispatchers.IO) {
        val manifest = getManifest(manifestIndex).getOrElse {
            return@withContext Result.failure(it)
        }

        val fileData = manifest.getFileMetaData(fileIndex).getOrElse {
            return@withContext Result.failure(it)
        }

        val contentMetaData = fileData.getContentMetaData(contentIndex).getOrElse {
            return@withContext Result.failure(it)
        }

        val cacheKey = "${manifestIndex}_${fileIndex}_${contentIndex}"

        memoryCache.get(cacheKey)?.let {
            return@withContext Result.success(it)
        }

        val path = "${manifest.dirName}/${fileData.fileName}"

        val data = apiClient.fetchFile(path, contentMetaData.start, contentMetaData.end).getOrElse {
            return@withContext Result.failure(it)
        }

        val decryptedResult = withContext(Dispatchers.Default) {
            manifest.getContentData(data, contentMetaData)
        }

        return@withContext decryptedResult.onSuccess { decrypted ->
            memoryCache.put(cacheKey, decrypted)
        }
    }

    suspend fun downloadManifestList(): Result<Unit>{
        val list = this.apiClient.fetchManifestList().getOrElse { error ->
            return Result.failure(error)
        }

        this.manifests.clear()

        list.forEach { data ->
            val manifest = Manifest(data)

            this.manifests.add(manifest)
        }

        return Result.success(Unit)
    }

    suspend fun downloadManifestData(password: String, index: Int): Result<Unit> = withContext(Dispatchers.IO) {
        val manifest = this@StorageService.getManifest(index).getOrElse { error ->
            return@withContext Result.failure(error)
        }

        val data = apiClient.fetchManifest(manifest.dirName).getOrElse { error ->
            return@withContext Result.failure(error)
        }

        return@withContext withContext(Dispatchers.Default) {
            manifest.setBuffer(data, password)
        }
    }

    suspend fun checkValidPassword(password: String): Result<Unit> = withContext(Dispatchers.IO) {
        val manifest = this@StorageService.getManifest(0).getOrElse { error ->
            return@withContext Result.failure(error)
        }

        val copyManifest = Manifest(manifest.dirName)

        val data = this@StorageService.apiClient.fetchManifest(copyManifest.dirName).getOrElse { error ->
            return@withContext Result.failure(error)
        }

        return@withContext manifest.setBuffer(data,password)
    }

    fun clearCache() {
        memoryCache.evictAll()
    }
}