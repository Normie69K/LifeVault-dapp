package com.codebyte.lifevault_dapp.ui.screens

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.theme.*

@Composable
fun SettingsScreen(viewModel: MainViewModel, navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().background(BrandBlack).padding(20.dp)) {
        Text("Settings", color = TextWhite, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        // --- PREMIUM BANNER ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(Color(0xFF332F00), Color(0xFF1E1E1E))),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text("Premium", color = BrandOrange, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Text("Upgrade your experience with\nPremium features!", color = TextWhite, fontSize = 14.sp)
            }
            Icon(Icons.Rounded.Star, null, tint = BrandOrange, modifier = Modifier.size(60.dp).align(Alignment.CenterEnd))
        }

        Spacer(Modifier.height(24.dp))

        // --- SETTINGS LIST ---
        SettingsGroup {
            SettingsItem("Manage Wallets", Icons.Rounded.AccountBalanceWallet) {
                navController.navigate("wallet")
            }
            SettingsItem("Security", Icons.Rounded.Security) {
                viewModel.toggleAppLock()
            }
            // NEW: Logout Functionality
            SettingsItem("Logout / Reset Vault", Icons.Rounded.Logout, isError = true) {
                viewModel.logoutUser()
                navController.navigate("onboarding") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(content: @Composable () -> Unit) {
    Column(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(BrandCard)) {
        content()
    }
}

@Composable
fun SettingsItem(text: String, icon: ImageVector, isError: Boolean = false, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (isError) BrandRed else TextGrey, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(text, color = if (isError) BrandRed else TextWhite, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, null, tint = TextGrey)
    }
    Divider(color = BrandBlack, thickness = 1.dp)
}