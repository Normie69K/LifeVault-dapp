// src/main/java/com/codebyte/lifevault_dapp/core/AptosClient.kt
package com.codebyte.lifevault_dapp.core

import android.util.Base64
import android.util.Log
import com.codebyte.lifevault_dapp.data.MemoryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
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

    // Get account info
    suspend fun getAccountInfo(address: String): AccountInfo? = withContext(Dispatchers.IO) {
        try {
            val url = "${AptosConfig.NODE_URL}/accounts/$address"
            val request = Request.Builder().url(url).get().build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null

                val json = JSONObject(response.body?.string() ?: "{}")
                AccountInfo(
                    sequenceNumber = json.optString("sequence_number", "0"),
                    authenticationKey = json.optString("authentication_key", "")
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get account info", e)
            null
        }
    }

    // Get account balance
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

    // Fund account from faucet (devnet only)
    suspend fun fundFromFaucet(address: String, amount: Long = 100_000_000): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "${AptosConfig.FAUCET_URL}/mint?amount=$amount&address=$address"
            val request = Request.Builder()
                .url(url)
                .post("".toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                Log.d(TAG, "Faucet response: ${response.code}")
                response.isSuccessful
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fund from faucet", e)
            false
        }
    }

    // Fetch user memories from blockchain
    suspend fun fetchUserMemories(address: String): List<MemoryItem> = withContext(Dispatchers.IO) {
        try {
            val resourceType = "${AptosConfig.MODULE_ADDRESS}::${AptosConfig.MODULE_NAME}::UserVault"
            val url = "${AptosConfig.NODE_URL}/accounts/$address/resource/$resourceType"

            val request = Request.Builder().url(url).get().build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.d(TAG, "No vault found for $address")
                    return@withContext emptyList()
                }

                val json = JSONObject(response.body?.string() ?: "{}")
                val data = json.optJSONObject("data") ?: return@withContext emptyList()
                val memoriesArray = data.optJSONArray("memories") ?: return@withContext emptyList()

                val list = mutableListOf<MemoryItem>()
                for (i in 0 until memoriesArray.length()) {
                    val item = memoriesArray.getJSONObject(i)
                    list.add(MemoryItem(
                        id = i + 1,
                        title = item.optString("title", "Secured Asset"),
                        date = "Block: ${item.optString("timestamp", "Unknown")}",
                        ipfsHash = item.optString("ipfs_hash", ""),
                        isSecured = true
                    ))
                }
                list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch memories", e)
            emptyList()
        }
    }

    // Register memory on blockchain
    suspend fun registerMemory(title: String, ipfsHash: String): String = withContext(Dispatchers.IO) {
        try {
            val address = cryptoManager.getAddress()
            val privateKeyBytes = cryptoManager.getPrivateKeyBytes()
                ?: throw IllegalStateException("No wallet found")
            val publicKeyBytes = cryptoManager.getPublicKeyBytes()
                ?: throw IllegalStateException("No wallet found")

            // Get account sequence number
            val accountInfo = getAccountInfo(address)
                ?: throw Exception("Account not found. Please fund your wallet first.")

            // Build transaction payload
            val payload = JSONObject().apply {
                put("type", "entry_function_payload")
                put("function", "${AptosConfig.MODULE_ADDRESS}::${AptosConfig.MODULE_NAME}::register_memory")
                put("type_arguments", JSONArray())
                put("arguments", JSONArray().apply {
                    put(title)
                    put(ipfsHash)
                })
            }

            // Create raw transaction
            val rawTxn = createRawTransaction(
                sender = address,
                sequenceNumber = accountInfo.sequenceNumber.toLong(),
                payload = payload
            )

            // Sign transaction
            val signedTxn = signTransaction(rawTxn, privateKeyBytes, publicKeyBytes)

            // Submit transaction
            submitTransaction(signedTxn)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register memory", e)
            throw e
        }
    }

    // Create raw transaction
    private suspend fun createRawTransaction(
        sender: String,
        sequenceNumber: Long,
        payload: JSONObject
    ): JSONObject = withContext(Dispatchers.IO) {
        // Get chain ID and gas estimate
        val gasEstimate = estimateGas(sender, payload)

        JSONObject().apply {
            put("sender", sender)
            put("sequence_number", sequenceNumber.toString())
            put("max_gas_amount", gasEstimate.maxGasAmount.toString())
            put("gas_unit_price", gasEstimate.gasUnitPrice.toString())
            put("expiration_timestamp_secs", ((System.currentTimeMillis() / 1000) + 600).toString())
            put("payload", payload)
        }
    }

    // Estimate gas
    private suspend fun estimateGas(sender: String, payload: JSONObject): GasEstimate = withContext(Dispatchers.IO) {
        try {
            val url = "${AptosConfig.NODE_URL}/transactions/simulate"

            val txn = JSONObject().apply {
                put("sender", sender)
                put("sequence_number", "0")
                put("max_gas_amount", "100000")
                put("gas_unit_price", "100")
                put("expiration_timestamp_secs", ((System.currentTimeMillis() / 1000) + 600).toString())
                put("payload", payload)
                put("signature", JSONObject().apply {
                    put("type", "ed25519_signature")
                    put("public_key", "0x" + "00".repeat(32))
                    put("signature", "0x" + "00".repeat(64))
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(txn.toString().toRequestBody("application/json".toMediaType()))
                .header("Content-Type", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val json = JSONArray(response.body?.string() ?: "[]")
                    if (json.length() > 0) {
                        val result = json.getJSONObject(0)
                        val gasUsed = result.optString("gas_used", "50000").toLong()
                        return@withContext GasEstimate(
                            maxGasAmount = (gasUsed * 2).coerceAtLeast(100000),
                            gasUnitPrice = 100
                        )
                    }
                }
                GasEstimate(100000, 100)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gas estimation failed, using defaults", e)
            GasEstimate(100000, 100)
        }
    }

    // Sign transaction
    private fun signTransaction(
        rawTxn: JSONObject,
        privateKeyBytes: ByteArray,
        publicKeyBytes: ByteArray
    ): JSONObject {
        // Create signing message (prefix + BCS serialized transaction)
        // For simplicity, we'll use the JSON representation
        val message = rawTxn.toString().toByteArray()

        // Sign with Ed25519
        val privateKey = Ed25519PrivateKeyParameters(privateKeyBytes, 0)
        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)
        val signature = signer.generateSignature()

        // Build signed transaction
        return JSONObject().apply {
            put("sender", rawTxn.getString("sender"))
            put("sequence_number", rawTxn.getString("sequence_number"))
            put("max_gas_amount", rawTxn.getString("max_gas_amount"))
            put("gas_unit_price", rawTxn.getString("gas_unit_price"))
            put("expiration_timestamp_secs", rawTxn.getString("expiration_timestamp_secs"))
            put("payload", rawTxn.getJSONObject("payload"))
            put("signature", JSONObject().apply {
                put("type", "ed25519_signature")
                put("public_key", "0x" + publicKeyBytes.joinToString("") { "%02x".format(it) })
                put("signature", "0x" + signature.joinToString("") { "%02x".format(it) })
            })
        }
    }

    // Submit transaction
    private suspend fun submitTransaction(signedTxn: JSONObject): String = withContext(Dispatchers.IO) {
        val url = "${AptosConfig.NODE_URL}/transactions"

        val request = Request.Builder()
            .url(url)
            .post(signedTxn.toString().toRequestBody("application/json".toMediaType()))
            .header("Content-Type", "application/json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "Transaction failed: $responseBody")
                throw Exception("Transaction failed: ${response.code}")
            }

            val json = JSONObject(responseBody)
            val txHash = json.optString("hash", "")

            Log.d(TAG, "Transaction submitted: $txHash")

            // Wait for transaction confirmation
            waitForTransaction(txHash)

            txHash
        }
    }

    // Wait for transaction to be confirmed
    private suspend fun waitForTransaction(txHash: String): Boolean = withContext(Dispatchers.IO) {
        repeat(30) { attempt ->
            try {
                val url = "${AptosConfig.NODE_URL}/transactions/by_hash/$txHash"
                val request = Request.Builder().url(url).get().build()

                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val json = JSONObject(response.body?.string() ?: "{}")
                        val success = json.optBoolean("success", false)
                        if (success) {
                            Log.d(TAG, "Transaction confirmed: $txHash")
                            return@withContext true
                        }
                        val vmStatus = json.optString("vm_status", "")
                        if (vmStatus.contains("error", ignoreCase = true)) {
                            throw Exception("Transaction failed: $vmStatus")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Waiting for transaction... attempt ${attempt + 1}")
            }
            kotlinx.coroutines.delay(1000)
        }
        throw Exception("Transaction confirmation timeout")
    }

    // Data classes
    data class AccountInfo(
        val sequenceNumber: String,
        val authenticationKey: String
    )

    data class GasEstimate(
        val maxGasAmount: Long,
        val gasUnitPrice: Long
    )
}