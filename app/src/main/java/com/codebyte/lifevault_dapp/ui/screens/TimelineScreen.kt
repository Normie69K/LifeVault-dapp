package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.components.MemoryCard
import com.codebyte.lifevault_dapp.ui.theme.BrandBlack
import com.codebyte.lifevault_dapp.ui.theme.TextWhite
import com.codebyte.lifevault_dapp.ui.theme.TextGrey

@Composable
fun TimelineScreen(viewModel: MainViewModel, navController: NavController) {
    val memories by viewModel.memories.collectAsState()

    Column(modifier = Modifier.fillMaxSize().background(BrandBlack).padding(16.dp)) {
        Text("My Vault", color = TextWhite, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("${memories.size} Secured Memories", color = TextGrey)
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(memories) { memory ->
                // Ensure MemoryCard is updated to handle new theme colors implicitly via Theme.kt
                MemoryCard(memory) { navController.navigate("memory_detail/${memory.id}") }
            }
        }
    }
}