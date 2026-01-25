package com.codebyte.lifevault_dapp.core

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager(context: Context) {

    // Master key to encrypt the shared preferences
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // Encrypted storage for the private key
    private val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_vault_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // 1. The "Invisible Wallet" Get or Create Logic
    fun getOrCreateWeb3Credentials(): Credentials {
        var privateKeyHex = sharedPreferences.getString("user_private_key", null)
        if (privateKeyHex == null) {
            // Generate new Ethereum keypair if none exists
            val ecKeyPair = Keys.createEcKeyPair()
            privateKeyHex = ecKeyPair.privateKey.toString(16)
            sharedPreferences.edit().putString("user_private_key", privateKeyHex).apply()
        }
        // Load credentials from the stored private key
        return Credentials.create(privateKeyHex)
    }

    // 2. Local File Encryption (AES-GCM) before upload
    private val androidKeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val AES_KEY_ALIAS = "FileUploadKey"

    private fun getUploadKey(): SecretKey {
        return androidKeyStore.getKey(AES_KEY_ALIAS, null) as? SecretKey ?: run {
            val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGen.init(
                KeyGenParameterSpec.Builder(AES_KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGen.generateKey()
        }
    }

    fun encryptData(data: ByteArray): Pair<ByteArray, ByteArray> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getUploadKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data)
        // Returns Initialization Vector (IV) and Encrypted Data
        return Pair(iv, encryptedBytes)
    }
}