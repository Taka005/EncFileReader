package com.taka.encfilereader.service

import com.taka.encfilereader.model.Manifest
import com.taka.encfilereader.net.ApiClient

open class StorageService(val baseUrl: String){
    private val manifests: MutableList<Manifest> = mutableListOf()

    private val apiClient = ApiClient(this.baseUrl)

    open val manifestCount: Int
        get() = this.manifests.size

    open fun getManifest(index: Int): Result<Manifest>{
        val manifest = this.manifests.getOrNull(index) ?: return Result.failure(
            IndexOutOfBoundsException("ファイルの指定が範囲外です")
        )

        return Result.success(manifest)
    }

//    open fun getContent(): Result<ByteArray>{
//
//    }

    open suspend fun downloadManifestList(): Result<Unit>{
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

    open suspend fun downloadManifestData(
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