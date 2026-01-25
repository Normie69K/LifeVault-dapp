package com.codebyte.lifevault_dapp.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// --- DATA MODELS ---
data class MemoryItem(val id: String, val title: String, val date: String, val isSecured: Boolean)
data class UploadRequest(val title: String, val encryptedDataB64: String)
data class UploadResponse(val memoryId: String, val ipfsPath: String)

// --- RETROFIT API INTERFACE ---
interface BackendApi {
    @GET("/timeline")
    suspend fun getTimeline(): List<MemoryItem>

    @POST("/upload")
    suspend fun uploadFile(@Body request: UploadRequest): UploadResponse

    companion object {
        // Emulator localhost address
        private const val BASE_URL = "http://10.0.2.2:3000/"

        fun create(): BackendApi {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(BackendApi::class.java)
        }
    }
}