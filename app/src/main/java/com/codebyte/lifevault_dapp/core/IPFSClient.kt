// src/main/java/com/codebyte/lifevault_dapp/core/IPFSClient.kt
package com.codebyte.lifevault_dapp.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class IPFSClient {

    companion object {
        private const val TAG = "IPFSClient"

        // Pinata API (Free tier available)
        private const val PINATA_API_URL = "https://api.pinata.cloud"

        // Replace with your Pinata API keys
        private const val PINATA_API_KEY = "cff9b41913c8991e63bb"
        private const val PINATA_SECRET_KEY = "8620c8fcbb4c9b4a2e034fac3d8ecfb9016a0440f75d6f58ccd0e4884e81630b\n" +
                "PINATA_JWT=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySW5mb3JtYXRpb24iOnsiaWQiOiI5MWJmMTM5OS00NDc5LTQyZmMtYmU2Yy0xZDFiN2E3YWE5NjEiLCJlbWFpbCI6ImFkaXR5YW5pc2hhZDMwMDVAZ21haWwuY29tIiwiZW1haWxfdmVyaWZpZWQiOnRydWUsInBpbl9wb2xpY3kiOnsicmVnaW9ucyI6W3siZGVzaXJlZFJlcGxpY2F0aW9uQ291bnQiOjEsImlkIjoiRlJBMSJ9LHsiZGVzaXJlZFJlcGxpY2F0aW9uQ291bnQiOjEsImlkIjoiTllDMSJ9XSwidmVyc2lvbiI6MX0sIm1mYV9lbmFibGVkIjpmYWxzZSwic3RhdHVzIjoiQUNUSVZFIn0sImF1dGhlbnRpY2F0aW9uVHlwZSI6InNjb3BlZEtleSIsInNjb3BlZEtleUtleSI6ImNmZjliNDE5MTNjODk5MWU2M2JiIiwic2NvcGVkS2V5U2VjcmV0IjoiODYyMGM4ZmNiYjRjOWI0YTJlMDM0ZmFjM2Q4ZWNmYjkwMTZhMDQ0MGY3NWQ2ZjU4Y2NkMGU0ODg0ZTgxNjMwYiIsImV4cCI6MTgwMDg3NjQ0NX0.r9CH_aIhsBhcBpVBVViItPyuz2zP43rCx2WOJzgOVWI"

        // Alternative: Use Web3.Storage or NFT.Storage (also free)
        private const val WEB3_STORAGE_URL = "https://api.web3.storage"
        private const val WEB3_STORAGE_TOKEN = "YOUR_WEB3_STORAGE_TOKEN"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    // Upload encrypted data to IPFS via Pinata
    suspend fun uploadToPinata(
        encryptedData: String,
        fileName: String,
        metadata: Map<String, String> = emptyMap()
    ): IPFSUploadResult = withContext(Dispatchers.IO) {
        try {
            val url = "$PINATA_API_URL/pinning/pinJSONToIPFS"

            // Create JSON payload with encrypted data
            val jsonPayload = JSONObject().apply {
                put("pinataContent", JSONObject().apply {
                    put("encrypted_data", encryptedData)
                    put("filename", fileName)
                    put("timestamp", System.currentTimeMillis())
                    metadata.forEach { (key, value) ->
                        put(key, value)
                    }
                })
                put("pinataMetadata", JSONObject().apply {
                    put("name", fileName)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(jsonPayload.toString().toRequestBody("application/json".toMediaType()))
                .header("pinata_api_key", PINATA_API_KEY)
                .header("pinata_secret_api_key", PINATA_SECRET_KEY)
                .header("Content-Type", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.e(TAG, "Pinata upload failed: $responseBody")
                    throw Exception("Upload failed: ${response.code}")
                }

                val json = JSONObject(responseBody)
                val ipfsHash = json.getString("IpfsHash")

                Log.d(TAG, "Uploaded to IPFS: $ipfsHash")

                IPFSUploadResult(
                    hash = ipfsHash,
                    url = "${AptosConfig.IPFS_GATEWAY}$ipfsHash",
                    size = json.optLong("PinSize", 0)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "IPFS upload failed", e)
            throw e
        }
    }

    // Upload file bytes to IPFS
    suspend fun uploadFileToPinata(
        fileBytes: ByteArray,
        fileName: String,
        mimeType: String = "application/octet-stream"
    ): IPFSUploadResult = withContext(Dispatchers.IO) {
        try {
            val url = "$PINATA_API_URL/pinning/pinFileToIPFS"

            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file",
                    fileName,
                    fileBytes.toRequestBody(mimeType.toMediaType())
                )
                .addFormDataPart(
                    "pinataMetadata",
                    JSONObject().apply {
                        put("name", fileName)
                    }.toString()
                )
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .header("pinata_api_key", PINATA_API_KEY)
                .header("pinata_secret_api_key", PINATA_SECRET_KEY)
                .build()

            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    Log.e(TAG, "Pinata file upload failed: $responseBody")
                    throw Exception("Upload failed: ${response.code}")
                }

                val json = JSONObject(responseBody)
                val ipfsHash = json.getString("IpfsHash")

                Log.d(TAG, "File uploaded to IPFS: $ipfsHash")

                IPFSUploadResult(
                    hash = ipfsHash,
                    url = "${AptosConfig.IPFS_GATEWAY}$ipfsHash",
                    size = json.optLong("PinSize", 0)
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "IPFS file upload failed", e)
            throw e
        }
    }

    // Download from IPFS
    suspend fun downloadFromIPFS(ipfsHash: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            val url = "${AptosConfig.IPFS_GATEWAY}$ipfsHash"
            val request = Request.Builder().url(url).get().build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw Exception("Download failed: ${response.code}")
                }

                response.body?.bytes() ?: throw Exception("Empty response")
            }
        } catch (e: Exception) {
            Log.e(TAG, "IPFS download failed", e)
            throw e
        }
    }

    // Get IPFS content as JSON
    suspend fun getIPFSContent(ipfsHash: String): JSONObject = withContext(Dispatchers.IO) {
        try {
            val bytes = downloadFromIPFS(ipfsHash)
            JSONObject(String(bytes))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get IPFS content", e)
            throw e
        }
    }

    data class IPFSUploadResult(
        val hash: String,
        val url: String,
        val size: Long
    )
}