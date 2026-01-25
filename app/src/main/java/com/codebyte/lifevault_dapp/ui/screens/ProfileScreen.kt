package com.codebyte.lifevault_dapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.components.MemoryCard
import com.codebyte.lifevault_dapp.ui.theme.VaultPurple

@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    val userName by viewModel.userName.collectAsState()
    val userHandle by viewModel.userHandle.collectAsState()
    val memories by viewModel.memories.collectAsState()

    var isEditing by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf(userName) }
    var editedHandle by remember { mutableStateOf(userHandle) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(VaultPurple.copy(0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.Person, null, modifier = Modifier.size(60.dp), tint = VaultPurple)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (isEditing) {
            OutlinedTextField(value = editedName, onValueChange = { editedName = it }, label = { Text("Name") })
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = editedHandle, onValueChange = { editedHandle = it }, label = { Text("Handle") })
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { viewModel.updateProfile(editedName, editedHandle); isEditing = false }) { Text("Save Profile") }
        } else {
            Text(userName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(userHandle, fontSize = 16.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { isEditing = true }) { Text("Edit Profile") }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text("Recent Activity", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Start))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(memories.take(3)) { memory ->
                MemoryCard(memory) { }
            }
        }
    }
}