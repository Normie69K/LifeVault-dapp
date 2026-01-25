package com.codebyte.lifevault_dapp.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class CryptoManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_vault_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // REAL AUTH CHECK
    fun hasWallet(): Boolean {
        val key = sharedPreferences.getString("private_key", null)
        return !key.isNullOrEmpty()
    }

    fun getAddress(): String {
        return sharedPreferences.getString("address", "") ?: ""
    }

    // REAL STORAGE
// Using Ed25519 for Aptos-compatible key generation
    fun createNewWallet(): String {
        val keyPair = org.bouncycastle.crypto.generators.Ed25519KeyPairGenerator().apply {
            init(org.bouncycastle.crypto.params.Ed25519KeyGenerationParameters(java.security.SecureRandom()))
        }.generateKeyPair()

        val privateKey = (keyPair.private as org.bouncycastle.crypto.params.Ed25519PrivateKeyParameters).getEncoded()
        val publicKey = (keyPair.public as org.bouncycastle.crypto.params.Ed25519PublicKeyParameters).getEncoded()

        // Derive Aptos address from public key (SHA3-256 hash + 0x00 suffix)
        val address = deriveAptosAddress(publicKey)

        sharedPreferences.edit()
            .putString("private_key", android.util.Base64.encodeToString(privateKey, android.util.Base64.NO_WRAP))
            .putString("address", address)
            .apply()

        return address
    }

    fun logout() {
        sharedPreferences.edit().clear().apply()
    }
    fun importWalletFromMnemonic(mnemonic: String): String {
        // In production, derive KeyPair from Mnemonic.
        val demoAddress = "0x599c19cd1f5a85d4eb4f403337bee2c26a8259b43c6cd0c9b6cdfd63d3874cc6"

        sharedPreferences.edit()
            .putString("private_key", "0xIMPORTED_KEY")
            .putString("address", demoAddress)
            .apply()

        return demoAddress
    }

    private fun deriveAptosAddress(publicKey: ByteArray): String {
        // 1. Create a buffer containing the Public Key + the Scheme ID (0x00 for Ed25519)
        val buffer = publicKey + byteArrayOf(0x00)

        // 2. Run SHA3-256 Hash
        val digest = java.security.MessageDigest.getInstance("SHA3-256")
        val hash = digest.digest(buffer)

        // 3. Convert the resulting hash to a Hex String with 0x prefix
        return "0x" + hash.joinToString("") { "%02x".format(it) }
    }

    // REAL AES ENCRYPTION
    fun encryptData(data: ByteArray): Pair<String, String> {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data)

        return Pair(
            Base64.encodeToString(iv, Base64.NO_WRAP),
            Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        )
    }
}