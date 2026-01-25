// src/main/java/com/codebyte/lifevault_dapp/core/AptosClient.kt
package com.codebyte.lifevault_dapp.core

import android.util.Log
import com.codebyte.lifevault_dapp.data.MemoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AptosClient(private val cryptoManager: CryptoManager) {

    companion object {
        private const val TAG = "AptosClient"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Custom RequestBody to avoid charset
    private class JsonRequestBody(private val json: String) : RequestBody() {
        override fun contentType() = "application/json".toMediaType()
        override fun writeTo(sink: BufferedSink) {
            sink.writeUtf8(json)
        }
    }

    // Get balance
    suspend fun getBalance(address: String): Long = withContext(Dispatchers.IO) {
        try {
            val resourceType = "0x1::coin::CoinStore<0x1::aptos_coin::AptosCoin>"
            val url = "${AptosConfig.NODE_URL}/accounts/$address/resource/$resourceType"
            val request = Request.Builder().url(url).get().build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext 0L

                val json = JSONObject(response.body?.string() ?: "{}")
                val data = json.optJSONObject("data") ?: return@withContext 0L
                val coin = data.optJSONObject("coin") ?: return@withContext 0L
                coin.optString("value", "0").toLongOrNull() ?: 0L
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get balance", e)
            0L
        }
    }

    // Fund from faucet
    suspend fun fundFromFaucet(address: String, amount: Long = 100_000_000): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${AptosConfig.FAUCET_URL}/mint?amount=$amount&address=$address"
            val request = Request.Builder()
                .url(url)
                .post(JsonRequestBody(""))
                .build()

            httpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "Faucet: ${response.code}")
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Faucet failed", e)
            false
        }
    }

    // Fetch memories
    suspend fun fetchUserMemories(address: String): List<MemoryItem> = withContext(Dispatchers.IO) {
        try {
            val url = "${AptosConfig.NODE_URL}/view"

            val payload = JSONObject().apply {
                put("function", "${AptosConfig.MODULE_ADDRESS}::${AptosConfig.MODULE_NAME}::get_memories")
                put("type_arguments", JSONArray())
                put("arguments", JSONArray().apply { put(address) })
            }

            val request = Request.Builder()
                .url(url)
                .post(JsonRequestBody(payload.toString()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()

                val jsonArray = JSONArray(response.body?.string() ?: "[]")
                if (jsonArray.length() == 0) return@withContext emptyList()

                val memoriesArray = jsonArray.getJSONArray(0)
                val list = mutableListOf<MemoryItem>()

                for (i in 0 until memoriesArray.length()) {
                    val mem = memoriesArray.getJSONObject(i)
                    list.add(MemoryItem(
                        id = i + 1,
                        title = mem.optString("title", "Secured Memory"),
                        date = "Secured on Aptos",
                        ipfsHash = mem.optString("ipfs_hash", ""),
                        isSecured = true
                    ))
                }
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch failed", e)
            emptyList()
        }
    }

    // Register memory - SIMPLIFIED FOR DEMO
    suspend fun registerMemory(title: String, ipfsHash: String): String = withContext(Dispatchers.IO) {
        try {
            // For hackathon demo: Generate a fake but realistic transaction hash
            val demoHash = "0x${System.currentTimeMillis().toString(16).padStart(64, '0')}"

            Log.d(TAG, "Demo Txn: $demoHash (Real blockchain integration pending)")

            // Simulate blockchain delay
            kotlinx.coroutines.delay(2000)

            demoHash
        } catch (e: Exception) {
            Log.e(TAG, "Register failed", e)
            throw e
        }
    }

    data class AccountInfo(
        val sequenceNumber: String,
        val authenticationKey: String
    )
}