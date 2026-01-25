package com.codebyte.lifevault_dapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.codebyte.lifevault_dapp.MainViewModel
import com.codebyte.lifevault_dapp.ui.screens.*
import com.codebyte.lifevault_dapp.ui.theme.*
import com.codebyte.lifevault_dapp.ui.components.EnhancedUploadModal

// src/main/java/com/codebyte/lifevault_dapp/ui/navigation/Navigation.kt

@Composable
fun Navigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    var showModal by remember { mutableStateOf(false) } // State for modal

    Scaffold(
        containerColor = BrandBlack,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            if (navBackStackEntry?.destination?.route != "onboarding") {
                BottomNavBar(navController) { showModal = true } // Open modal on click
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = "timeline", Modifier.padding(innerPadding)) {
            composable("timeline") { TimelineScreen(viewModel, navController) }
            composable("share") { SharedScreen(viewModel, navController) }
            composable("profile") { ProfileScreen(viewModel) }
            composable("send") { SendScreen(viewModel, navController) } // New Send Route
            composable("memory_detail/{id}") { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: 0
                MemoryDetailScreen(viewModel, id, navController)
            }
        }

        if (showModal) {
            EnhancedUploadModal(viewModel) { showModal = false }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, onAddClick: () -> Unit) {
    val items = listOf(
        Triple("timeline", "Vault", Icons.Rounded.Timeline),
        Triple("add", "Add", Icons.Rounded.AddCircle),
        Triple("profile", "Profile", Icons.Rounded.Person)
    )
    NavigationBar(containerColor = BrandCard) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { (route, label, icon) ->
            NavigationBarItem(
                icon = { Icon(icon, null) },
                label = { Text(label) },
                selected = currentRoute == route,
                onClick = {
                    if (route == "add") {
                        onAddClick()
                    } else {
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = BrandOrange,
                    unselectedIconColor = TextGrey,
                    selectedIconColor = BrandBlack
                )
            )
        }
    }
}