package com.codebyte.lifevault_dapp.core

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import java.math.BigInteger

class Web3Client(private val credentials: Credentials) {

    // Use 10.0.2.2 for Android Emulator to reach localhost.
    // If using a physical device, replace with your computer's local IP.
    private val web3j = Web3j.build(HttpService("http://10.0.2.2:8545"))

    // REPLACE WITH YOUR DEPLOYED CONTRACT ADDRESS FROM HARDHAT
    // Dummy address for demo purposes.
    private val contractAddress = "0x5FbDB2315678afecb367f032d93F642f64180aa3"

    // Hardhat/Ganache Chain ID (Must be a Long 'L')
    private val chainId = 31337L

    // Transaction manager handles nonces and signing
    private val txManager = RawTransactionManager(web3j, credentials, chainId)

    // Hardcoded gas values for hackathon demo reliability
    private val gasPrice = BigInteger.valueOf(20_000_000_000) // 20 Gwei
    private val gasLimit = BigInteger.valueOf(500_000)

    suspend fun registerMemory(memoryId: String): String = withContext(Dispatchers.IO) {
        Log.d("Web3", "Attempting to mint memoryId: $memoryId with address: ${credentials.address}")

        try {
            // Hackathon Shortcut: We are sending a 0 ETH transaction to the contract address
            // to prove signing and network connectivity. In a real app, 'data' would hold
            // the encoded function call (e.g., register(memoryId)).
            val data = ""

            val receipt = txManager.sendTransaction(
                gasPrice,
                gasLimit,
                contractAddress,
                data,
                BigInteger.ZERO
            )
            Log.d("Web3", "Success! Tx Hash: ${receipt.transactionHash}")
            receipt.transactionHash

        } catch (e: Exception) {
            Log.e("Web3", "Transaction Failed: ${e.message}", e)
            throw e
        }
    }

    // Optional: Helper to check balance for debugging
    suspend fun getBalance(): String = withContext(Dispatchers.IO) {
        try {
            val balance = web3j.ethGetBalance(credentials.address, DefaultBlockParameterName.LATEST).send()
            balance.balance.toString()
        } catch (e: Exception) {
            Log.e("Web3", "Failed to get balance", e)
            "0"
        }
    }
}