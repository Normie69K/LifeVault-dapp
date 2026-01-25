package com.codebyte.lifevault_dapp.data

data class MemoryItem(
    val id: Int,
    val title: String,
    val date: String,
    val isSecured: Boolean = true // Added default for UI compatibility
)