// src/main/java/com/codebyte/lifevault_dapp/core/CryptoManager.kt
package com.codebyte.lifevault_dapp.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import cash.z.ecc.android.bip39.Mnemonics
import cash.z.ecc.android.bip39.toSeed
import org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator
import org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters
import org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters
import org.bouncycastle.crypto.params.Ed25519PublicKeyParameters
import org.bouncycastle.crypto.signers.Ed25519Signer
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

class CryptoManager(context: Context) {

    companion object {
        private const val TAG = "CryptoManager"
        private const val PREFS_NAME = "lifevault_secure_prefs"
        private const val KEY_PRIVATE_KEY = "private_key"
        private const val KEY_PUBLIC_KEY = "public_key"
        private const val KEY_ADDRESS = "wallet_address"
        private const val KEY_MNEMONIC = "mnemonic"
        private const val KEY_ENCRYPTION_KEY = "encryption_key"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Check if wallet exists
    fun hasWallet(): Boolean {
        val privateKey = sharedPreferences.getString(KEY_PRIVATE_KEY, null)
        return !privateKey.isNullOrEmpty()
    }

    // Get wallet address
    fun getAddress(): String {
        return sharedPreferences.getString(KEY_ADDRESS, "") ?: ""
    }

    // Get mnemonic for backup
    fun getMnemonic(): String {
        return sharedPreferences.getString(KEY_MNEMONIC, "") ?: ""
    }

    // Get private key bytes
    fun getPrivateKeyBytes(): ByteArray? {
        val encoded = sharedPreferences.getString(KEY_PRIVATE_KEY, null) ?: return null
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    // Get public key bytes
    fun getPublicKeyBytes(): ByteArray? {
        val encoded = sharedPreferences.getString(KEY_PUBLIC_KEY, null) ?: return null
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    // Create new wallet with real Ed25519 keys
    fun createNewWallet(): WalletData {
        try {
            // Generate BIP39 mnemonic (24 words for better security)
            val mnemonicCode = Mnemonics.MnemonicCode(Mnemonics.WordCount.COUNT_24)
            val mnemonic = mnemonicCode.words.joinToString(" ")

            // Derive seed from mnemonic
            val seed = mnemonicCode.toSeed()

            // Generate Ed25519 key pair from seed
            val keyPair = generateKeyPairFromSeed(seed)

            val privateKey = (keyPair.private as Ed25519PrivateKeyParameters).encoded
            val publicKey = (keyPair.public as Ed25519PublicKeyParameters).encoded

            // Derive Aptos address from public key
            val address = deriveAptosAddress(publicKey)

            // Generate encryption key for file encryption
            val encryptionKey = generateEncryptionKey()

            // Store securely
            sharedPreferences.edit()
                .putString(KEY_PRIVATE_KEY, Base64.encodeToString(privateKey, Base64.NO_WRAP))
                .putString(KEY_PUBLIC_KEY, Base64.encodeToString(publicKey, Base64.NO_WRAP))
                .putString(KEY_ADDRESS, address)
                .putString(KEY_MNEMONIC, mnemonic)
                .putString(KEY_ENCRYPTION_KEY, Base64.encodeToString(encryptionKey.encoded, Base64.NO_WRAP))
                .apply()

            Log.d(TAG, "Wallet created: $address")

            return WalletData(
                address = address,
                mnemonic = mnemonic,
                publicKey = publicKey
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create wallet", e)
            throw e
        }
    }

    // Import wallet from mnemonic
    fun importWalletFromMnemonic(mnemonic: String): String {
        try {
            // Validate mnemonic
            val words = mnemonic.trim().split("\\s+".toRegex())
            if (words.size !in listOf(12, 24)) {
                throw IllegalArgumentException("Invalid mnemonic: must be 12 or 24 words")
            }

            // Create mnemonic code and derive seed
            val mnemonicCode = Mnemonics.MnemonicCode(mnemonic.toCharArray())
            val seed = mnemonicCode.toSeed()

            // Generate key pair from seed
            val keyPair = generateKeyPairFromSeed(seed)

            val privateKey = (keyPair.private as Ed25519PrivateKeyParameters).encoded
            val publicKey = (keyPair.public as Ed25519PublicKeyParameters).encoded

            // Derive address
            val address = deriveAptosAddress(publicKey)

            // Generate encryption key
            val encryptionKey = generateEncryptionKey()

            // Store securely
            sharedPreferences.edit()
                .putString(KEY_PRIVATE_KEY, Base64.encodeToString(privateKey, Base64.NO_WRAP))
                .putString(KEY_PUBLIC_KEY, Base64.encodeToString(publicKey, Base64.NO_WRAP))
                .putString(KEY_ADDRESS, address)
                .putString(KEY_MNEMONIC, mnemonic)
                .putString(KEY_ENCRYPTION_KEY, Base64.encodeToString(encryptionKey.encoded, Base64.NO_WRAP))
                .apply()

            Log.d(TAG, "Wallet imported: $address")

            return address
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import wallet", e)
            throw e
        }
    }

    // Generate Ed25519 key pair from seed
    private fun generateKeyPairFromSeed(seed: ByteArray): org.bouncycastle.crypto.AsymmetricCipherKeyPair {
        // Use first 32 bytes of seed for Ed25519 private key
        val privateKeySeed = seed.copyOf(32)

        val privateKeyParams = Ed25519PrivateKeyParameters(privateKeySeed, 0)
        val publicKeyParams = privateKeyParams.generatePublicKey()

        return org.bouncycastle.crypto.AsymmetricCipherKeyPair(publicKeyParams, privateKeyParams)
    }

    // Derive Aptos address from public key (SHA3-256 hash with scheme suffix)
    private fun deriveAptosAddress(publicKey: ByteArray): String {
        // Aptos uses: SHA3-256(public_key || 0x00) for Ed25519 single-key accounts
        val buffer = publicKey + byteArrayOf(0x00)

        val digest = MessageDigest.getInstance("SHA3-256")
        val hash = digest.digest(buffer)

        return "0x" + hash.joinToString("") { "%02x".format(it) }
    }

    // Sign message with Ed25519
    fun signMessage(message: ByteArray): ByteArray {
        val privateKeyBytes = getPrivateKeyBytes()
            ?: throw IllegalStateException("No wallet found")

        val privateKey = Ed25519PrivateKeyParameters(privateKeyBytes, 0)

        val signer = Ed25519Signer()
        signer.init(true, privateKey)
        signer.update(message, 0, message.size)

        return signer.generateSignature()
    }

    // Generate AES encryption key
    private fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256, SecureRandom())
        return keyGenerator.generateKey()
    }

    // Get encryption key
    private fun getEncryptionKey(): SecretKey {
        val encoded = sharedPreferences.getString(KEY_ENCRYPTION_KEY, null)
        return if (encoded != null) {
            val keyBytes = Base64.decode(encoded, Base64.NO_WRAP)
            SecretKeySpec(keyBytes, "AES")
        } else {
            val key = generateEncryptionKey()
            sharedPreferences.edit()
                .putString(KEY_ENCRYPTION_KEY, Base64.encodeToString(key.encoded, Base64.NO_WRAP))
                .apply()
            key
        }
    }

    // Encrypt data with AES-GCM
    fun encryptData(data: ByteArray): EncryptedData {
        val secretKey = getEncryptionKey()

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data)

        return EncryptedData(
            iv = Base64.encodeToString(iv, Base64.NO_WRAP),
            data = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        )
    }

    // Decrypt data
    fun decryptData(encryptedData: EncryptedData): ByteArray {
        val secretKey = getEncryptionKey()

        val iv = Base64.decode(encryptedData.iv, Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(encryptedData.data, Base64.NO_WRAP)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        return cipher.doFinal(encryptedBytes)
    }

    // Logout / Clear wallet
    fun logout() {
        sharedPreferences.edit().clear().apply()
        Log.d(TAG, "Wallet cleared")
    }

    // Data classes
    data class WalletData(
        val address: String,
        val mnemonic: String,
        val publicKey: ByteArray
    )

    data class EncryptedData(
        val iv: String,
        val data: String
    )
}