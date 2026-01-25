package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.UiState
import com.codebyte.lifevault_dapp.ui.theme.*

// src/main/java/com/codebyte/lifevault_dapp/ui/screens/SendScreen.kt

@Composable
fun SendScreen(viewModel: MainViewModel, navController: NavController) {
    var recipientAddress by remember { mutableStateOf("") }
    var assetTitle by remember { mutableStateOf("") }
    val uiState by viewModel.uploadState.collectAsState()

    Column(Modifier.fillMaxSize().background(BrandBlack).padding(24.dp)) {
        Text("Send Asset", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = recipientAddress,
            onValueChange = { recipientAddress = it },
            label = { Text("Recipient Public Address") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = BrandOrange)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.sendToAddress(recipientAddress, assetTitle) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
        ) {
            if (uiState is UiState.Loading) CircularProgressIndicator(color = BrandBlack)
            else Text("Authorize Transfer", color = BrandBlack, fontWeight = FontWeight.Bold)
        }
    }
}