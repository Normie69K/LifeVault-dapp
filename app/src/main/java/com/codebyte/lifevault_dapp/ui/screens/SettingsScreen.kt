// src/main/java/com/codebyte/lifevault_dapp/ui/screens/SettingsScreen.kt
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, navController: NavController) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isLocked by viewModel.isAppLocked.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
            .padding(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Rounded.ArrowBack, null, tint = TextWhite)
                }
                Text(
                    "Settings",
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Premium Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = BrandCard)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(BrandOrange.copy(0.3f), BrandCard)
                        )
                    )
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Premium",
                            color = BrandOrange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Upgrade your experience\nwith Premium features!",
                            color = TextWhite,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }
                    Icon(
                        Icons.Rounded.Star,
                        null,
                        tint = BrandOrange,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Groups
        SettingsGroup {
            SettingsItem(
                "Manage Wallets",
                Icons.Rounded.AccountBalanceWallet
            ) {
                navController.navigate("wallet")
            }

            SettingsItemWithToggle(
                "App Lock",
                Icons.Rounded.Lock,
                isEnabled = isLocked,
                onToggle = { viewModel.toggleAppLock() }
            )

            SettingsItem(
                "Backup & Recovery",
                Icons.Rounded.Backup
            ) {
                // TODO: Implement backup screen
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsGroup {
            SettingsItem(
                "About LifeVault",
                Icons.Rounded.Info
            ) {
                // TODO: About screen
            }

            SettingsItem(
                "Help & Support",
                Icons.Rounded.Help
            ) {
                // TODO: Help screen
            }

            SettingsItem(
                "Privacy Policy",
                Icons.Rounded.Policy
            ) {
                // TODO: Privacy screen
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SettingsGroup {
            SettingsItem(
                "Logout / Reset Vault",
                Icons.Rounded.Logout,
                isError = true
            ) {
                showLogoutDialog = true
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Version Info
        Text(
            "LifeVault v1.0.0 (Hackathon Edition)",
            color = TextGrey,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }

    // Logout Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout", color = TextWhite) },
            text = {
                Text(
                    "Are you sure you want to logout? Make sure you have backed up your recovery phrase.",
                    color = TextGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logoutUser()
                        showLogoutDialog = false
                        navController.navigate("onboarding") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextGrey)
                }
            },
            containerColor = BrandCard
        )
    }
}

@Composable
fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BrandCard)
    ) {
        content()
    }
}

@Composable
fun SettingsItem(
    text: String,
    icon: ImageVector,
    isError: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = if (isError) BrandRed else TextGrey,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text,
            color = if (isError) BrandRed else TextWhite,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(
            Icons.Rounded.ChevronRight,
            null,
            tint = TextGrey
        )
    }
}

@Composable
fun SettingsItemWithToggle(
    text: String,
    icon: ImageVector,
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = TextGrey,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text,
            color = TextWhite,
            fontSize = 16.sp,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = isEnabled,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = BrandOrange,
                checkedTrackColor = BrandOrange.copy(0.5f),
                uncheckedThumbColor = TextGrey,
                uncheckedTrackColor = BrandBlack
            )
        )
    }
}