// src/main/java/com/codebyte/lifevault_dapp/ui/screens/SharedScreen.kt
package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.theme.*

@Composable
fun SharedScreen(viewModel: MainViewModel, navController: NavController) {
    val address by viewModel.walletAddress.collectAsState()
    val qrBitmap by viewModel.qrCodeBitmap.collectAsState()
    val shareState by viewModel.shareState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Receive Assets",
            color = TextWhite,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Share your address to receive secured assets",
            color = TextGrey,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        // QR Code Display
        Card(
            modifier = Modifier.size(280.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = TextWhite)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                qrBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Wallet QR",
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = BrandOrange)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Generating QR...",
                        color = BrandBlack.copy(0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Address Display Card
        Card(
            colors = CardDefaults.cardColors(containerColor = BrandCard),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Your Public Address",
                    color = TextGrey,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    address ?: "No wallet connected",
                    color = BrandOrange,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        Button(
            onClick = { viewModel.shareViaAddress() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                Icons.Rounded.ContentCopy,
                null,
                tint = BrandBlack
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Copy Address",
                color = BrandBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { /* Share functionality */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = BrandOrange
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(BrandOrange)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Rounded.Share, null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Share",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Success Message
        shareState?.let { message ->
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = BrandGreen.copy(0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.CheckCircle,
                        null,
                        tint = BrandGreen
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        message,
                        color = BrandGreen,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}