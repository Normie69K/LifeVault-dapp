package com.codebyte.lifevault_dapp

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codebyte.lifevault_dapp.core.CryptoManager
import com.codebyte.lifevault_dapp.core.Web3Client
import com.codebyte.lifevault_dapp.data.BackendApi
import com.codebyte.lifevault_dapp.data.MemoryItem
import com.codebyte.lifevault_dapp.data.UploadRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.web3j.utils.Numeric
import java.util.UUID

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val txHash: String) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val cryptoManager = CryptoManager(application)
    private val creds = cryptoManager.getOrCreateWeb3Credentials()
    private val web3Client = Web3Client(creds)
    private val backendApi = BackendApi.create()
    private val context = application.applicationContext

    var memories by mutableStateOf(listOf<MemoryItem>())
        private set
    var uploadState by mutableStateOf<UiState>(UiState.Idle)
        private set
    var shareState by mutableStateOf<UiState>(UiState.Idle)
        private set
    var walletAddress by mutableStateOf("Loading...")
        private set

    // New: For holding selected file
    var selectedFileUri by mutableStateOf<Uri?>(null)

    init {
        loadTimeline()
        walletAddress = creds.address
    }

    private fun loadTimeline() {
        viewModelScope.launch {
            // Simulated data for the polished UI feel
            memories = listOf(
                MemoryItem("1", "Tax Documents 2025", "Today, 10:30 AM", false),
                MemoryItem("2", "Hawaii Trip Photos", "Yesterday", true),
                MemoryItem("3", "House Deed Scan", "Jan 20, 2026", true)
            )
            // try { memories = backendApi.getTimeline() } catch (e: Exception) { }
        }
    }

    // --- NEW: Secure Real File ---
    fun secureSelectedFile(title: String) {
        val uri = selectedFileUri ?: return
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { uploadState = UiState.Loading }
            try {
                // 1. Read file bytes from URI
                val inputStream = context.contentResolver.openInputStream(uri)
                val fileBytes = inputStream?.readBytes() ?: throw Exception("Could not read file")
                inputStream.close()

                Log.d("ViewModel", "Read ${fileBytes.size} bytes from uri")

                // 2. Encrypt locally
                val (_, encryptedBytes) = cryptoManager.encryptData(fileBytes)
                val encryptedB64 = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.NO_WRAP)

                // 3. Upload & Mint
                val uploadResp = backendApi.uploadFile(UploadRequest(title, encryptedB64))
                val txHash = web3Client.registerMemory(uploadResp.memoryId)

                withContext(Dispatchers.Main) {
                    uploadState = UiState.Success(txHash)
                    memories = listOf(MemoryItem(uploadResp.memoryId, title, "Just now", false)) + memories
                    selectedFileUri = null // Reset selection
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Secure failed", e)
                withContext(Dispatchers.Main) {
                    uploadState = UiState.Error(e.message ?: "Security process failed.")
                }
            }
        }
    }

    fun shareMemory(memoryId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { shareState = UiState.Loading }
            try {
                val txHash = web3Client.registerMemory(memoryId)
                delay(1500) // Slower animation for effect
                val fakeLink = "https://lifevault.app/view/${memoryId.take(8)}#decKey=aB9f..."
                withContext(Dispatchers.Main) { shareState = UiState.Success(fakeLink) }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { shareState = UiState.Error("Verification failed on-chain.") }
            }
        }
    }

    // --- NEW: Security Center Logic ---
    fun getPrivateKeyForDisplay(): String {
        // WARNING: In a real app, require biometrics before showing this.
        return Numeric.toHexStringNoPrefix(creds.ecKeyPair.privateKey)
    }

    fun resetStates() {
        uploadState = UiState.Idle
        shareState = UiState.Idle
        selectedFileUri = null
    }
}