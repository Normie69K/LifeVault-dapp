package com.codebyte.lifevault_dapp.data

// CHANGED: "class" instead of "interface" so we can instantiate it
class BackendApi {
    suspend fun uploadFile(request: UploadRequest): UploadResponse {
        // Simulating a backend response for the Hackathon
        kotlinx.coroutines.delay(1000)
        return UploadResponse(
            memoryId = "mem_${System.currentTimeMillis()}",
            ipfsPath = "QmHash${System.currentTimeMillis()}"
        )
    }
}

// Ensure these data classes exist in the same file or package
data class UploadRequest(val title: String, val encryptedData: String, val owner: String)
data class UploadResponse(val memoryId: String, val ipfsPath: String)