package com.taka.encfilereader.service

import com.taka.encfilereader.model.Manifest
import com.taka.encfilereader.net.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StorageService(
    private val baseUrl: String,
    var maxRequests: Int
){
    private val manifests: MutableList<Manifest> = mutableListOf()

    private var apiClient = ApiClient(baseUrl,maxRequests)

    val manifestCount: Int
        get() = this.manifests.size

    fun getManifest(index: Int): Result<Manifest>{
        val manifest = this.manifests.getOrNull(index) ?: return Result.failure(
            IndexOutOfBoundsException("マニフェストの指定が範囲外です")
        )

        return Result.success(manifest)
    }

    fun resetApiClient(){
        apiClient = ApiClient(baseUrl,maxRequests)
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

        val path = "${manifest.dirName}/${fileData.fileName}"

        val data = apiClient.fetchFile(path, contentMetaData.start, contentMetaData.end).getOrElse {
            return@withContext Result.failure(it)
        }

        return@withContext withContext(Dispatchers.Default) {
            manifest.getContentData(data, contentMetaData)
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

    suspend fun fetchRawManifestData(dirName: String): Result<ByteArray> = withContext(Dispatchers.IO) {
        return@withContext apiClient.fetchManifest(dirName)
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
}