package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.codebyte.lifevault_dapp.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryDetailScreen(viewModel: MainViewModel, memoryId: Int, navController: NavController) {
    val memory = viewModel.getMemoryById(memoryId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Memory Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteMemory(memoryId)
                        navController.popBackStack()
                    }) {
                        Icon(Icons.Default.Delete, "Delete")
                    }
                }
            )
        }
    ) { padding ->
        if (memory != null) {
            Column(
                modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(memory.title, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(memory.date, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(32.dp))
                Text("Secured on Aptos Blockchain", color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Memory not found")
            }
        }
    }
}