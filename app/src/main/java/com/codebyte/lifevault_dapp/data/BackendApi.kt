// src/main/java/com/codebyte/lifevault_dapp/data/BackendApi.kt
package com.codebyte.lifevault_dapp.data

data class UploadRequest(
    val title: String,
    val encryptedData: String,
    val owner: String
)

data class UploadResponse(
    val memoryId: String,
    val ipfsPath: String
)