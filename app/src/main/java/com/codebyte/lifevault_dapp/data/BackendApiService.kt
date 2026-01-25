// src/main/java/com/codebyte/lifevault_dapp/data/BackendApiService.kt
package com.codebyte.lifevault_dapp.data

import com.codebyte.lifevault_dapp.core.AptosConfig
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

// API Response Models
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

data class UploadRequest(
    val title: String,
    val encryptedData: String,
    val owner: String,
    val category: String = "General",
    val description: String = ""
)

data class UploadResponse(
    val memoryId: String,
    val ipfsHash: String,
    val txHash: String = ""
)

data class UserProfile(
    val address: String,
    val name: String,
    val handle: String,
    val memoriesCount: Int
)

data class ShareRequest(
    val memoryId: String,
    val recipientAddress: String,
    val accessType: String = "view",
    val expiresAt: Long? = null
)

// API Interface
interface BackendApiService {

    @POST("api/v1/upload")
    suspend fun uploadMemory(@Body request: UploadRequest): Response<ApiResponse<UploadResponse>>

    @GET("api/v1/memories/{address}")
    suspend fun getMemories(@Path("address") address: String): Response<ApiResponse<List<MemoryItem>>>

    @GET("api/v1/memory/{id}")
    suspend fun getMemory(@Path("id") memoryId: String): Response<ApiResponse<MemoryItem>>

    @DELETE("api/v1/memory/{id}")
    suspend fun deleteMemory(@Path("id") memoryId: String): Response<ApiResponse<Unit>>

    @POST("api/v1/share")
    suspend fun shareMemory(@Body request: ShareRequest): Response<ApiResponse<Unit>>

    @POST("api/v1/revoke")
    suspend fun revokeShare(@Body request: ShareRequest): Response<ApiResponse<Unit>>

    @GET("api/v1/profile/{address}")
    suspend fun getProfile(@Path("address") address: String): Response<ApiResponse<UserProfile>>

    @PUT("api/v1/profile")
    suspend fun updateProfile(@Body profile: UserProfile): Response<ApiResponse<UserProfile>>

    @Multipart
    @POST("api/v1/upload-file")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("title") title: String,
        @Part("owner") owner: String
    ): Response<ApiResponse<UploadResponse>>
}

// Retrofit Instance
object BackendApi {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val service: BackendApiService by lazy {
        Retrofit.Builder()
            .baseUrl(AptosConfig.BACKEND_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BackendApiService::class.java)
    }
}