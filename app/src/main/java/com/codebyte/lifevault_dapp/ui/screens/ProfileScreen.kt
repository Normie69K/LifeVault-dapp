// src/main/java/com/codebyte/lifevault_dapp/ui/screens/ProfileScreen.kt
package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.components.MemoryCard
import com.codebyte.lifevault_dapp.ui.components.WalletBalanceCard
import com.codebyte.lifevault_dapp.ui.components.FaucetButton
import com.codebyte.lifevault_dapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: MainViewModel, navController: NavController) {
    val userName by viewModel.userName.collectAsState()
    val userHandle by viewModel.userHandle.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val walletAddress by viewModel.walletAddress.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }
    var editedHandle by remember { mutableStateOf(userHandle) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // Update local state when userName/userHandle changes
    LaunchedEffect(userName, userHandle) {
        editedName = userName
        editedHandle = userHandle
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
            .padding(16.dp)
    ) {
        // Header with Settings
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Profile",
                color = TextWhite,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { navController.navigate("settings") }) {
                Icon(Icons.Rounded.Settings, null, tint = TextGrey)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(BrandOrange.copy(0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Person,
                        null,
                        modifier = Modifier.size(60.dp),
                        tint = BrandOrange
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isEditing) {
                    // Edit Mode
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandOrange,
                            unfocusedBorderColor = TextGrey,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = BrandOrange,
                            unfocusedLabelColor = TextGrey
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedHandle,
                        onValueChange = { editedHandle = it },
                        label = { Text("Handle") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandOrange,
                            unfocusedBorderColor = TextGrey,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite,
                            focusedLabelColor = BrandOrange,
                            unfocusedLabelColor = TextGrey
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                isEditing = false
                                editedName = userName
                                editedHandle = userHandle
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = TextGrey
                            )
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.updateProfile(editedName, editedHandle)
                                isEditing = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandOrange,
                                contentColor = BrandBlack
                            )
                        ) {
                            Text("Save")
                        }
                    }
                } else {
                    // Display Mode
                    Text(
                        userName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        userHandle,
                        fontSize = 16.sp,
                        color = TextGrey
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Wallet Address
                    Surface(
                        color = BrandBlack,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.AccountBalanceWallet,
                                null,
                                tint = BrandOrange,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = walletAddress?.let {
                                    "${it.take(8)}...${it.takeLast(6)}"
                                } ?: "No Wallet",
                                color = BrandOrange,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { isEditing = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandOrange,
                            contentColor = BrandBlack
                        )
                    ) {
                        Icon(Icons.Rounded.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Wallet Balance Section
        WalletBalanceCard(viewModel)

        Spacer(modifier = Modifier.height(12.dp))

        FaucetButton(viewModel)

        Spacer(modifier = Modifier.height(24.dp))

        // Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard("Assets", memories.size.toString())
            StatCard("Secured", memories.count { it.isSecured }.toString())
            StatCard("Shared", "0")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Activity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent Activity",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
            if (memories.isNotEmpty()) {
                TextButton(onClick = { navController.navigate("memories") }) {
                    Text("View All", color = BrandOrange)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (memories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.Inventory2,
                        null,
                        tint = TextGrey,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No memories yet", color = TextGrey)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(memories.take(3)) { memory ->
                    MemoryCard(memory) {
                        navController.navigate("memory_detail/${memory.id}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Logout Button
        OutlinedButton(
            onClick = { showLogoutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BrandRed
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(BrandRed)
            )
        ) {
            Icon(Icons.Rounded.Logout, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Logout")
        }
    }

    // Logout Confirmation Dialog
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
fun StatCard(label: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = BrandOrange
            )
            Text(
                label,
                fontSize = 12.sp,
                color = TextGrey
            )
        }
    }
}