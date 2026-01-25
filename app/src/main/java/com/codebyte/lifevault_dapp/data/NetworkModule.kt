package com.codebyte.lifevault_dapp.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

// 1. Define the Shape of your Backend API
interface LifeVaultApiService {
    @POST("/api/v1/upload") // <-- Replace with your actual endpoint path
    suspend fun uploadFile(@Body request: UploadRequest): UploadResponse
}

// 2. The Real Network Object
object NetworkModule {
    // ⚠️ REPLACE WITH YOUR REAL SERVER URL (e.g., AWS, Heroku, or Ngrok for testing)
    // If you don't have a server yet, use a placeholder, but the app will log errors.
    private const val BACKEND_BASE_URL = "https://life-vault-backend-git-main-adityas-projects-e5e2af34.vercel.app/"

    // Real Aptos Devnet Node
    const val APTOS_NODE_URL = "https://fullnode.devnet.aptoslabs.com/v1"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Real timeouts
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    // The Retrofit Instance for your Backend
    val api: LifeVaultApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LifeVaultApiService::class.java)
    }

    // Shared Client for Blockchain Calls
    val httpClient = client
}