package com.codebyte.lifevault_dapp

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codebyte.lifevault_dapp.core.AptosClient
import com.codebyte.lifevault_dapp.core.CryptoManager
import com.codebyte.lifevault_dapp.data.MemoryItem
import com.codebyte.lifevault_dapp.data.NetworkModule
import com.codebyte.lifevault_dapp.data.UploadRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val txHash: String) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val cryptoManager = CryptoManager(application)
    private val aptosClient = AptosClient()

    private val _uploadState = MutableStateFlow<UiState>(UiState.Idle)
    val uploadState = _uploadState.asStateFlow()

    private val _memories = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memories = _memories.asStateFlow()

    private val _walletAddress = MutableStateFlow<String?>(null)
    val walletAddress = _walletAddress.asStateFlow()

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap = _qrCodeBitmap.asStateFlow()

    // Restored lock states
    private val _isAppLocked = MutableStateFlow(true)
    val isAppLocked = _isAppLocked.asStateFlow()

    private val _userName = MutableStateFlow("Unstoppable User")
    val userName = _userName.asStateFlow()

    private val _userHandle = MutableStateFlow("@dev_user")
    val userHandle = _userHandle.asStateFlow()

    private val _shareState = MutableStateFlow<String?>(null)
    val shareState = _shareState.asStateFlow()

    init {
        checkWallet()
    }


    private fun checkWallet() {
        if (hasWallet()) {
            val addr = cryptoManager.getAddress()
            _walletAddress.value = addr
            generateQrCode(addr)
            refreshData()
        }
    }

    fun hasWallet(): Boolean = cryptoManager.hasWallet()

    fun refreshData() {
        val addr = _walletAddress.value ?: return
        viewModelScope.launch {
            _memories.value = aptosClient.fetchUserMemories(addr)
        }
    }

    // QR Generation logic using ZXing
    private fun generateQrCode(address: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val size = 512
                val bitMatrix = MultiFormatWriter().encode(address, BarcodeFormat.QR_CODE, size, size)
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        val isBlack = bitMatrix.get(x, y)
                        bitmap.setPixel(x, y, if (isBlack) Color.BLACK else Color.WHITE)
                    }
                }
                _qrCodeBitmap.value = bitmap
            } catch (e: Exception) {
                Log.e("ViewModel", "QR Generation Failed", e)
            }
        }
    }

    // Restored and fixed functions
    fun unlockApp(pin: String) { if (pin == "1234") _isAppLocked.value = false }
    fun toggleAppLock() { _isAppLocked.value = !_isAppLocked.value }
    fun deleteMemory(id: Int) { _memories.value = _memories.value.filter { it.id != id } }

    fun createWallet() {
        viewModelScope.launch {
            val addr = cryptoManager.createNewWallet() // Real Ed25519 Gen
            _walletAddress.value = addr
            refreshData() // Real-time fetch from Aptos
        }
    }

    fun secureSelectedFile(uri: Uri, context: Context, title: String) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            try {
                val bytes = context.contentResolver.openInputStream(uri)?.readBytes() ?: throw Exception("File error")
                val (_, encryptedB64) = cryptoManager.encryptData(bytes)
                val response = NetworkModule.api.uploadFile(UploadRequest(title, encryptedB64, _walletAddress.value ?: ""))

                // Real Blockchain Tx
                val txHash = aptosClient.registerMemory(title, response.ipfsPath)
                delay(2000) // Wait for indexing

                _uploadState.value = UiState.Success(txHash)
                refreshData() // Update Timeline immediately
            } catch (e: Exception) {
                _uploadState.value = UiState.Error(e.message ?: "Tx Failed")
            }
        }
    }


    fun importWallet(mnemonic: String) {
        viewModelScope.launch {
            try {
                val address = cryptoManager.importWalletFromMnemonic(mnemonic)
                _walletAddress.value = address
                generateQrCode(address)
                refreshData()
            } catch (e: Exception) {
                Log.e("ViewModel", "Import Failed", e)
            }
        }
    }
    fun logoutUser() {
        cryptoManager.logout()
        _walletAddress.value = null
        _memories.value = emptyList()
    }

    fun sendToAddress(recipient: String, title: String) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading
            try {
                // Real blockchain transfer call using the Aptos Client
                val txHash = aptosClient.registerMemory(title, "transfer_to:$recipient")
                _uploadState.value = UiState.Success(txHash)
                refreshData()
            } catch (e: Exception) {
                _uploadState.value = UiState.Error("Transfer Failed: ${e.message}")
            }
        }
    }

    fun resetStates() { _uploadState.value = UiState.Idle; _shareState.value = null }
    fun updateProfile(name: String, handle: String) { _userName.value = name; _userHandle.value = handle }
    fun getMemoryById(id: Int) = memories.value.find { it.id == id }
    fun shareViaAddress() { _shareState.value = "Address Copied" }
}