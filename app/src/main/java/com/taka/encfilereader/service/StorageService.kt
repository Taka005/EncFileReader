package com.taka.encfilereader.service

import com.taka.encfilereader.model.Manifest
import com.taka.encfilereader.net.ApiClient

class StorageService(val baseUrl: String){
    private val manifests: MutableList<Manifest> = mutableListOf()

    private val apiClient = ApiClient(this.baseUrl)

    val manifestCount: Int
        get() = this.manifests.size

    fun getManifest(index: Int): Result<Manifest>{
        val manifest = this.manifests.getOrNull(index) ?: return Result.failure(
            IndexOutOfBoundsException("マニフェストの指定が範囲外です")
        )

        return Result.success(manifest)
    }

    suspend fun getContent(
        manifestIndex: Int,
        fileIndex: Int,
        contentIndex: Int
    ): Result<ByteArray>{
        val manifest = this.getManifest(manifestIndex).getOrElse { error ->
            return Result.failure(error)
        }

        val fileData = manifest.getFileMetaData(fileIndex).getOrElse { error ->
            return Result.failure(error)
        }

        val contentMetaData = fileData.getContentMetaData(contentIndex).getOrElse { error ->
            return Result.failure(error)
        }

        val path = "${manifest.dirName}/${fileData.fileName}"

        val data = this.apiClient.fetchFile(path,contentMetaData.start,contentMetaData.end).getOrElse { error ->
            return Result.failure(error)
        }

        return manifest.getContentData(data,contentMetaData)
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

    suspend fun downloadManifestData(
        password: String,
        index: Int
    ): Result<Unit>{
        val manifest = this.getManifest(index).getOrElse { error ->
            return Result.failure(error)
        }

        val data = this.apiClient.fetchManifest(manifest.dirName).getOrElse { error ->
            return Result.failure(error)
        }

        return manifest.setBuffer(data,password)
    }
}