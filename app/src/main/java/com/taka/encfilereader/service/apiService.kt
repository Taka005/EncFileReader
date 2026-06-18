package com.taka.encfilereader.service

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Streaming

interface apiService {
    @Streaming
    @GET
    suspend fun getManifestData(): ResponseBody
}