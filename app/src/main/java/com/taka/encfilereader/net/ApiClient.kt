package com.taka.encfilereader.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

open class ApiClient(val baseUrl: String){
    private val retrofit = Retrofit.Builder()
        .baseUrl(if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/")
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    open suspend fun fetchManifestList(): Result<List<String>> {
        return try {
            val list = apiService.getManifestList()

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun fetchManifest(dirName: String): Result<ByteArray> {
        return try {
            val response = apiService.getManifestData("${dirName}/manifest")

            Result.success(response.bytes())
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    open suspend fun fetchFile(path: String,start: Int,end: Int): Result<ByteArray> {
        return try {
            val response = apiService.getFileData(path,"bytes=${start}-${end}")

            Result.success(response.bytes())
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}