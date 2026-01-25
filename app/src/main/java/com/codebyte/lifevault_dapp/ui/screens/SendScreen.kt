// src/main/java/com/codebyte/lifevault_dapp/ui/screens/SendScreen.kt
package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.UiState
import com.codebyte.lifevault_dapp.ui.components.QRScannerScreen
import com.codebyte.lifevault_dapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SendScreen(viewModel: MainViewModel, navController: NavController) {
    var recipientAddress by remember { mutableStateOf("") }
    var assetNote by remember { mutableStateOf("") }
    var showScanner by remember { mutableStateOf(false) }

    val uiState by viewModel.uploadState.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.resetStates() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (showScanner) {
            QRScannerScreen(
                onQRCodeScanned = { code ->
                    if (viewModel.handleScannedQRCode(code)) {
                        recipientAddress = code
                    }
                    showScanner = false
                },
                onDismiss = { showScanner = false }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BrandBlack)
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Rounded.ArrowBack, null, tint = TextWhite)
                    }
                    Text(
                        "Send Asset",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showScanner = true }) {
                        Icon(Icons.Rounded.QrCodeScanner, null, tint = BrandOrange)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Transfer ownership of your secured assets",
                    color = TextGrey,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                when (uiState) {
                    is UiState.Idle, is UiState.Error -> {
                        // Recipient Input
                        Text(
                            "Recipient Address",
                            color = TextWhite,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = recipientAddress,
                            onValueChange = { recipientAddress = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("0x...", color = TextGrey) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Person, null, tint = TextGrey)
                            },
                            trailingIcon = {
                                Row {
                                    if (recipientAddress.isNotEmpty()) {
                                        IconButton(onClick = { recipientAddress = "" }) {
                                            Icon(Icons.Rounded.Clear, null, tint = TextGrey)
                                        }
                                    }
                                    IconButton(onClick = { showScanner = true }) {
                                        Icon(Icons.Rounded.QrCode, null, tint = BrandOrange)
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandOrange,
                                unfocusedBorderColor = BrandCard,
                                focusedContainerColor = BrandCard,
                                unfocusedContainerColor = BrandCard,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        // Validation hint
                        if (recipientAddress.isNotEmpty()) {
                            val isValid = recipientAddress.startsWith("0x") &&
                                    recipientAddress.length == 66
                            Text(
                                if (isValid) "✓ Valid Aptos address" else "Invalid address format",
                                color = if (isValid) BrandGreen else BrandRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Note Input
                        Text(
                            "Note (Optional)",
                            color = TextWhite,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = assetNote,
                            onValueChange = { assetNote = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Add a note for this transfer", color = TextGrey) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Notes, null, tint = TextGrey)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BrandOrange,
                                unfocusedBorderColor = BrandCard,
                                focusedContainerColor = BrandCard,
                                unfocusedContainerColor = BrandCard,
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )

                        // Error message
                        if (uiState is UiState.Error || errorMessage != null) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = BrandRed.copy(0.2f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Error, null, tint = BrandRed)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        (uiState as? UiState.Error)?.message
                                            ?: errorMessage ?: "An error occurred",
                                        color = BrandRed,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Send Button
                        val isValidAddress = recipientAddress.startsWith("0x") &&
                                recipientAddress.length == 66

                        Button(
                            onClick = { viewModel.sendToAddress(recipientAddress, assetNote) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandOrange,
                                disabledContainerColor = BrandOrange.copy(0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            enabled = isValidAddress
                        ) {
                            Icon(Icons.Rounded.Send, null, tint = BrandBlack)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Authorize Transfer",
                                color = BrandBlack,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    is UiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = BrandOrange,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    "Processing Transfer...",
                                    color = TextWhite,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Confirming on Aptos blockchain",
                                    color = TextGrey,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    is UiState.Success -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Rounded.CheckCircle,
                                    null,
                                    tint = BrandGreen,
                                    modifier = Modifier.size(80.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    "Transfer Successful!",
                                    color = TextWhite,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Transaction Hash:", color = TextGrey)
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = BrandCard),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text(
                                        (uiState as UiState.Success).txHash.take(24) + "...",
                                        modifier = Modifier.padding(12.dp),
                                        color = BrandOrange,
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(32.dp))
                                Button(
                                    onClick = {
                                        viewModel.resetStates()
                                        navController.popBackStack()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = BrandCard),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Done", color = TextWhite)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}