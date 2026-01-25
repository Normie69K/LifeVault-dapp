package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.theme.VaultPurple

@Composable
fun OnboardingScreen(viewModel: MainViewModel, onComplete: () -> Unit) {
    if (viewModel.hasWallet()) {
        LaunchedEffect(Unit) { onComplete() }
        return
    }

    var step by remember { mutableStateOf(0) } // 0=Splash, 1=Selection, 2=Create, 3=Import
    var importText by remember { mutableStateOf("") }
    val walletAddress by viewModel.walletAddress.collectAsState()
    var isImporting by remember { mutableStateOf(false) }

    // Fix for navigation: Observe state changes to trigger completion
    LaunchedEffect(walletAddress) {
        if (walletAddress != null && (step == 2 || isImporting)) {
            onComplete()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // --- STEP 0: SPLASH ---
        AnimatedVisibility(visible = step == 0, exit = fadeOut()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Rounded.Security, null, tint = VaultPurple, modifier = Modifier.size(100.dp))
                Spacer(modifier = Modifier.height(24.dp))
                Text("LifeVault", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(48.dp))
                Button(onClick = { step = 1 }, colors = ButtonDefaults.buttonColors(containerColor = VaultPurple)) {
                    Text("Get Started")
                }
            }
        }

        // --- STEP 1: SELECT ACTION ---
        AnimatedVisibility(visible = step == 1, enter = slideInHorizontally { it }, exit = slideOutHorizontally { -it }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                Text("Welcome", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Text("Choose how to access your vault.", color = Color.Gray)
                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { viewModel.createWallet(); step = 2 },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AddCircle, null, tint = VaultPurple)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Create New Vault", fontWeight = FontWeight.Bold)
                            Text("Generate a new secure key.", fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { step = 3 },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Download, null, tint = VaultPurple)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Import Vault", fontWeight = FontWeight.Bold)
                            Text("Restore from recovery phrase.", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- STEP 2: CREATE CONFIRMATION ---
        AnimatedVisibility(visible = step == 2, enter = slideInHorizontally { it }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("Vault Identity Created", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)).padding(16.dp)) {
                    Text(walletAddress ?: "Generating Address...", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                }
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth().height(50.dp), colors = ButtonDefaults.buttonColors(containerColor = VaultPurple)) {
                    Text("Enter My Vault")
                }
            }
        }

        // --- STEP 3: IMPORT PHRASE ---
        AnimatedVisibility(visible = step == 3, enter = slideInHorizontally { it }) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Text("Restore Vault", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    label = { Text("Enter 12-word phrase") },
                    modifier = Modifier.fillMaxWidth().height(150.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        isImporting = true
                        viewModel.importWallet(importText)
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VaultPurple),
                    enabled = importText.split(" ").size >= 12 && !isImporting
                ) {
                    if (isImporting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    else Text("Restore Vault")
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { step = 1 }, enabled = !isImporting) { Text("Back") }
            }
        }
    }
}