// src/main/java/com/codebyte/lifevault_dapp/ui/screens/MemoryDetailScreen.kt
package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetailScreen(viewModel: MainViewModel, memoryId: Int, navController: NavController) {
    val memory = viewModel.getMemoryById(memoryId)
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Asset Details", color = TextWhite) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextWhite)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, "Delete", tint = BrandRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BrandBlack)
            )
        },
        containerColor = BrandBlack
    ) { padding ->
        if (memory != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Asset Icon
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(BrandOrange.copy(0.3f), BrandCard)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        null,
                        tint = BrandOrange,
                        modifier = Modifier.size(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    memory.title,
                    color = TextWhite,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date
                Text(
                    memory.date,
                    color = TextGrey,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        DetailRow(
                            icon = Icons.Rounded.Shield,
                            label = "Status",
                            value = if (memory.isSecured) "Secured" else "Pending",
                            valueColor = if (memory.isSecured) BrandGreen else BrandOrange
                        )

                        Divider(
                            color = BrandBlack,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        DetailRow(
                            icon = Icons.Rounded.Link,
                            label = "Blockchain",
                            value = "Aptos Devnet",
                            valueColor = TextWhite
                        )

                        Divider(
                            color = BrandBlack,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )

                        DetailRow(
                            icon = Icons.Rounded.Fingerprint,
                            label = "Asset ID",
                            value = "#${memory.id}",
                            valueColor = BrandOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { /* Download */ },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BrandOrange
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(BrandOrange)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Download, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download")
                    }

                    Button(
                        onClick = { navController.navigate("send") },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandOrange
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.Send, null, tint = BrandBlack)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transfer", color = BrandBlack)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Rounded.SearchOff,
                        null,
                        tint = TextGrey,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Memory not found", color = TextGrey)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandCard)
                    ) {
                        Text("Go Back", color = TextWhite)
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Asset", color = TextWhite) },
            text = {
                Text(
                    "Are you sure you want to delete this asset? This action cannot be undone.",
                    color = TextGrey
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteMemory(memoryId)
                        showDeleteDialog = false
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = TextGrey)
                }
            },
            containerColor = BrandCard
        )
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                null,
                tint = TextGrey,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, color = TextGrey, fontSize = 14.sp)
        }
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}