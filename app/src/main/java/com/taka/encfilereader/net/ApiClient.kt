package com.taka.encfilereader.net

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class ApiClient(
   private val baseUrl: String,
   maxRequests: Int
){
    private val dispatcher = Dispatcher().apply {
        this.maxRequests = maxRequests
        maxRequestsPerHost = maxRequests
    }

    private val okHttpClient = OkHttpClient.Builder()
        .dispatcher(dispatcher)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(parseUrl(baseUrl))
        .client(okHttpClient)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val apiService = retrofit.create(ApiService::class.java)

    fun parseUrl(url: String): String{
        val httpUrl = url.toHttpUrlOrNull() ?: throw IllegalArgumentException("不正なURL形式です")

        return httpUrl.newBuilder().query(null).build().toString()
    }

    suspend fun fetchManifestList(): Result<List<String>> {
        return try {
            val list = apiService.getManifestList()

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchManifest(dirName: String): Result<ByteArray> {
        return try {
            val response = apiService.getManifestData("${dirName}/manifest")

            Result.success(response.bytes())
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun fetchFile(path: String,start: Int,end: Int): Result<ByteArray> {
        return try {
            val response = apiService.getFileData(path,"bytes=${start}-${end}")

            Result.success(response.bytes())
        } catch (error: Exception) {
            Result.failure(error)
        }
    }
}