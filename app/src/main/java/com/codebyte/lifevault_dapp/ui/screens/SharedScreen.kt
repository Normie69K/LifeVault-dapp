package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        modifier = Modifier.fillMaxSize().background(BrandBlack).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Receive Assets", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("Scan QR to send files to this vault", color = TextGrey)

        Spacer(modifier = Modifier.height(48.dp))

        // QR Code Display
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(TextWhite)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Wallet QR",
                    modifier = Modifier.fillMaxSize()
                )
            } ?: CircularProgressIndicator(color = BrandOrange)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Address Display
        Surface(
            color = BrandCard,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Public Address", color = TextGrey, fontSize = 12.sp)
                Text(
                    address ?: "Not Available",
                    color = BrandOrange,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.shareViaAddress() },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BrandOrange)
        ) {
            Text("Copy Address", color = BrandBlack, fontWeight = FontWeight.Bold)
        }

        if (shareState != null) {
            Text(shareState!!, color = BrandGreen, modifier = Modifier.padding(top = 16.dp))
        }
    }
}