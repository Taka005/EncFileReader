package com.taka.encfilereader.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Streaming

interface ApiService {
    @GET("api/manifest")
    suspend fun getManifestList(): ResponseBody

    @GET("api/download")
    suspend fun getManifestData(
        @Query("path") path: String
    ): ResponseBody

    @Streaming
    @GET("api/download")
    suspend fun getFileData(
        @Header("Range") range: String,
        @Query("path") path: String
    ): ResponseBody
}