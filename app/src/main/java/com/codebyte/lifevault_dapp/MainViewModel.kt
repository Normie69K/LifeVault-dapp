// src/main/java/com/codebyte/lifevault_dapp/MainViewModel.kt
package com.codebyte.lifevault_dapp

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codebyte.lifevault_dapp.core.AptosClient
import com.codebyte.lifevault_dapp.core.CryptoManager
import com.codebyte.lifevault_dapp.core.IPFSClient
import com.codebyte.lifevault_dapp.data.BackendApi
import com.codebyte.lifevault_dapp.data.MemoryItem
import com.codebyte.lifevault_dapp.data.UploadRequest
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    data class Success(val txHash: String) : UiState()
    data class Error(val message: String) : UiState()
}

sealed class WalletState {
    object NoWallet : WalletState()
    object Creating : WalletState()
    object Importing : WalletState()
    data class Ready(val address: String) : WalletState()
    data class Error(val message: String) : WalletState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val cryptoManager = CryptoManager(application)
    private val aptosClient = AptosClient(cryptoManager)
    private val ipfsClient = IPFSClient()

    // UI States
    private val _uploadState = MutableStateFlow<UiState>(UiState.Idle)
    val uploadState = _uploadState.asStateFlow()

    private val _walletState = MutableStateFlow<WalletState>(WalletState.NoWallet)
    val walletState = _walletState.asStateFlow()

    // Data States
    private val _memories = MutableStateFlow<List<MemoryItem>>(emptyList())
    val memories = _memories.asStateFlow()

    private val _walletAddress = MutableStateFlow<String?>(null)
    val walletAddress = _walletAddress.asStateFlow()

    private val _walletBalance = MutableStateFlow(0L)
    val walletBalance = _walletBalance.asStateFlow()

    private val _mnemonic = MutableStateFlow<String?>(null)
    val mnemonic = _mnemonic.asStateFlow()

    private val _qrCodeBitmap = MutableStateFlow<Bitmap?>(null)
    val qrCodeBitmap = _qrCodeBitmap.asStateFlow()

    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked = _isAppLocked.asStateFlow()

    private val _userName = MutableStateFlow("LifeVault User")
    val userName = _userName.asStateFlow()

    private val _userHandle = MutableStateFlow("@user")
    val userHandle = _userHandle.asStateFlow()

    private val _shareState = MutableStateFlow<String?>(null)
    val shareState = _shareState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init {
        checkExistingWallet()
    }

    // Check for existing wallet on app start
    private fun checkExistingWallet() {
        if (cryptoManager.hasWallet()) {
            val address = cryptoManager.getAddress()
            _walletAddress.value = address
            _walletState.value = WalletState.Ready(address)
            generateQrCode(address)
            loadWalletData()
        }
    }

    fun hasWallet(): Boolean = cryptoManager.hasWallet()

    // Create new wallet
    fun createWallet() {
        viewModelScope.launch {
            _walletState.value = WalletState.Creating
            _isLoading.value = true

            try {
                val walletData = withContext(Dispatchers.Default) {
                    cryptoManager.createNewWallet()
                }

                _walletAddress.value = walletData.address
                _mnemonic.value = walletData.mnemonic
                _walletState.value = WalletState.Ready(walletData.address)

                generateQrCode(walletData.address)

                // Fund from faucet on devnet
                fundWalletFromFaucet()

                Log.d(TAG, "Wallet created: ${walletData.address}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to create wallet", e)
                _walletState.value = WalletState.Error(e.message ?: "Failed to create wallet")
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Import wallet from mnemonic
    fun importWallet(mnemonic: String) {
        viewModelScope.launch {
            _walletState.value = WalletState.Importing
            _isLoading.value = true

            try {
                val address = withContext(Dispatchers.Default) {
                    cryptoManager.importWalletFromMnemonic(mnemonic)
                }

                _walletAddress.value = address
                _mnemonic.value = mnemonic
                _walletState.value = WalletState.Ready(address)

                generateQrCode(address)
                loadWalletData()

                Log.d(TAG, "Wallet imported: $address")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to import wallet", e)
                _walletState.value = WalletState.Error(e.message ?: "Invalid recovery phrase")
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Fund wallet from faucet (devnet only)
    private fun fundWalletFromFaucet() {
        viewModelScope.launch {
            val address = _walletAddress.value ?: return@launch

            try {
                val success = aptosClient.fundFromFaucet(address)
                if (success) {
                    delay(2000) // Wait for transaction
                    updateBalance()
                    Log.d(TAG, "Wallet funded from faucet")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fund from faucet", e)
            }
        }
    }

    // Load wallet data (balance, memories)
    private fun loadWalletData() {
        viewModelScope.launch {
            updateBalance()
            refreshMemories()
        }
    }

    // Update wallet balance
    private suspend fun updateBalance() {
        val address = _walletAddress.value ?: return
        try {
            val balance = aptosClient.getBalance(address)
            _walletBalance.value = balance
            Log.d(TAG, "Balance: $balance")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get balance", e)
        }
    }

    // Refresh memories from blockchain
    fun refreshMemories() {
        viewModelScope.launch {
            _isLoading.value = true
            val address = _walletAddress.value ?: return@launch

            try {
                // Try to fetch from blockchain first
                val blockchainMemories = aptosClient.fetchUserMemories(address)

                if (blockchainMemories.isNotEmpty()) {
                    _memories.value = blockchainMemories
                } else {
                    // Try backend as fallback
                    try {
                        val response = BackendApi.service.getMemories(address)
                        if (response.isSuccessful && response.body()?.success == true) {
                            _memories.value = response.body()?.data ?: emptyList()
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Backend fetch failed", e)
                    }
                }

                updateBalance()

            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh memories", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Secure and upload a file
    fun secureSelectedFile(uri: Uri, context: Context, title: String, category: String = "General") {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading

            try {
                val address = _walletAddress.value
                    ?: throw Exception("No wallet connected")

                // Read file bytes
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.readBytes()
                        ?: throw Exception("Could not read file")
                }

                Log.d(TAG, "File read: ${bytes.size} bytes")

                // Encrypt data
                val encryptedData = cryptoManager.encryptData(bytes)
                Log.d(TAG, "Data encrypted")

                // Upload to IPFS
                val ipfsResult = ipfsClient.uploadToPinata(
                    encryptedData = encryptedData.data,
                    fileName = title,
                    metadata = mapOf(
                        "owner" to address,
                        "iv" to encryptedData.iv,
                        "category" to category
                    )
                )
                Log.d(TAG, "Uploaded to IPFS: ${ipfsResult.hash}")

                // Register on blockchain
                val txHash = try {
                    aptosClient.registerMemory(title, ipfsResult.hash)
                } catch (e: Exception) {
                    Log.w(TAG, "Blockchain registration failed, continuing with IPFS only", e)
                    "pending_${System.currentTimeMillis()}"
                }

                Log.d(TAG, "Registered on blockchain: $txHash")

                // Also save to backend
                try {
                    BackendApi.service.uploadMemory(
                        UploadRequest(
                            title = title,
                            encryptedData = encryptedData.data,
                            owner = address,
                            category = category
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Backend save failed", e)
                }

                // Add to local list
                val newMemory = MemoryItem(
                    id = (_memories.value.maxOfOrNull { it.id } ?: 0) + 1,
                    title = title,
                    date = java.text.SimpleDateFormat(
                        "MMM dd, yyyy HH:mm",
                        java.util.Locale.getDefault()
                    ).format(java.util.Date()),
                    ipfsHash = ipfsResult.hash,
                    category = category,
                    isSecured = true,
                    txHash = txHash
                )
                _memories.value = listOf(newMemory) + _memories.value

                _uploadState.value = UiState.Success(txHash)

            } catch (e: Exception) {
                Log.e(TAG, "Upload failed", e)
                _uploadState.value = UiState.Error(e.message ?: "Upload failed")
            }
        }
    }

    // Send/Transfer to address
    fun sendToAddress(recipientAddress: String, note: String) {
        viewModelScope.launch {
            _uploadState.value = UiState.Loading

            try {
                if (recipientAddress.isBlank()) {
                    throw Exception("Recipient address is required")
                }

                if (!recipientAddress.startsWith("0x") || recipientAddress.length != 66) {
                    throw Exception("Invalid Aptos address format")
                }

                // Register transfer on blockchain
                val txHash = aptosClient.registerMemory(
                    "Transfer: ${note.ifBlank { "Asset" }}",
                    "transfer:$recipientAddress"
                )

                Log.d(TAG, "Transfer recorded: $txHash")

                _uploadState.value = UiState.Success(txHash)

            } catch (e: Exception) {
                Log.e(TAG, "Transfer failed", e)
                _uploadState.value = UiState.Error(e.message ?: "Transfer failed")
            }
        }
    }

    // Generate QR code for wallet address
    private fun generateQrCode(address: String) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val size = 512
                val bitMatrix = MultiFormatWriter().encode(
                    address,
                    BarcodeFormat.QR_CODE,
                    size,
                    size
                )
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
                for (x in 0 until size) {
                    for (y in 0 until size) {
                        bitmap.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                    }
                }
                _qrCodeBitmap.value = bitmap
            } catch (e: Exception) {
                Log.e(TAG, "QR generation failed", e)
            }
        }
    }

    // Copy address to clipboard
    fun copyAddressToClipboard() {
        val context = getApplication<Application>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Wallet Address", _walletAddress.value ?: "")
        clipboard.setPrimaryClip(clip)
        _shareState.value = "Address copied to clipboard!"

        viewModelScope.launch {
            delay(3000)
            _shareState.value = null
        }
    }

    // Handle scanned QR code
    fun handleScannedQRCode(code: String): Boolean {
        return if (code.startsWith("0x") && code.length == 66) {
            // Valid Aptos address
            true
        } else {
            _errorMessage.value = "Invalid QR code. Expected an Aptos address."
            false
        }
    }

    // Delete memory
    fun deleteMemory(id: Int) {
        viewModelScope.launch {
            try {
                val memory = _memories.value.find { it.id == id }

                // Try to delete from backend
                memory?.let {
                    try {
                        BackendApi.service.deleteMemory(it.id.toString())
                    } catch (e: Exception) {
                        Log.w(TAG, "Backend delete failed", e)
                    }
                }

                // Remove from local list
                _memories.value = _memories.value.filter { it.id != id }

            } catch (e: Exception) {
                Log.e(TAG, "Delete failed", e)
                _errorMessage.value = e.message
            }
        }
    }

    // Get memory by ID
    fun getMemoryById(id: Int): MemoryItem? = _memories.value.find { it.id == id }

    // Update user profile
    fun updateProfile(name: String, handle: String) {
        _userName.value = name.ifBlank { "LifeVault User" }
        _userHandle.value = if (handle.startsWith("@")) handle else "@${handle.ifBlank { "user" }}"
    }

    // App lock
    fun unlockApp(pin: String): Boolean {
        return if (pin == "1234") {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun toggleAppLock() {
        _isAppLocked.value = !_isAppLocked.value
    }

    // Logout
    fun logoutUser() {
        cryptoManager.logout()
        _walletAddress.value = null
        _mnemonic.value = null
        _walletBalance.value = 0
        _memories.value = emptyList()
        _qrCodeBitmap.value = null
        _walletState.value = WalletState.NoWallet
        _isAppLocked.value = false
    }

    // Reset states
    fun resetStates() {
        _uploadState.value = UiState.Idle
        _shareState.value = null
        _errorMessage.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    // Get balance formatted
    fun getFormattedBalance(): String {
        val balance = _walletBalance.value
        val apt = balance / 100_000_000.0
        return String.format("%.4f APT", apt)
    }

    // Share memory
    fun shareMemory(memoryId: Int, recipientAddress: String) {
        viewModelScope.launch {
            try {
                BackendApi.service.shareMemory(
                    com.codebyte.lifevault_dapp.data.ShareRequest(
                        memoryId = memoryId.toString(),
                        recipientAddress = recipientAddress
                    )
                )
                _shareState.value = "Memory shared successfully!"
            } catch (e: Exception) {
                Log.e(TAG, "Share failed", e)
                _errorMessage.value = e.message
            }
        }
    }
}