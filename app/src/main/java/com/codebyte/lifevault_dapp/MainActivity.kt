package com.codebyte.lifevault_dapp

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.codebyte.lifevault_dapp.data.MemoryItem
import kotlinx.coroutines.delay

// Premium Dark Theme Colors
val VaultDark = Color(0xFF090E1A)
val VaultCardBg = Color(0xFF151C2F)
val VaultPurple = Color(0xFF6C5DD3)
val VaultBlue = Color(0xFF3B82F6)
val TextWhite = Color(0xFFFFFFFF)
val TextGrey = Color(0xFF9CA3AF)
val Success = Color(0xFF22C55E)
val WarningColor = Color(0xFFF59E0B)
val ErrorRed = Color(0xFFEF4444)

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(background = VaultDark, surface = VaultCardBg, primary = VaultPurple, onBackground = TextWhite),
                typography = Typography(bodyLarge = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Default))
            ) { Surface(color = VaultDark, modifier = Modifier.fillMaxSize()) { LifeVaultApp(viewModel) } }
        }
    }
}

@Composable
fun LifeVaultApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    var selectedMemoryId by remember { mutableStateOf("") }

    // Animated Navigation Host
    NavHost(
        navController = navController,
        startDestination = "onboarding",
        enterTransition = { fadeIn(animationSpec = tween(700)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(700)) },
        exitTransition = { fadeOut(animationSpec = tween(700)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(700)) },
        popEnterTransition = { fadeIn(animationSpec = tween(700)) + slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(700)) },
        popExitTransition = { fadeOut(animationSpec = tween(700)) + slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(700)) }
    ) {
        composable("onboarding") { OnboardingScreen(navController) }
        composable("dashboard") { DashboardScreen(navController, viewModel) { id -> selectedMemoryId = id; navController.navigate("share") } }
        composable("share") { ShareScreen(navController, viewModel, selectedMemoryId) }
        composable("settings") { SettingsScreen(navController, viewModel) }
    }
}

// --- 1. SLICKER ONBOARDING ---
@Composable
fun OnboardingScreen(navController: NavController) {
    var stage by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        delay(500); stage = 1 // Show Logo
        delay(1500); stage = 2 // Show Text/Bar
        delay(2500); stage = 3 // Complete
        delay(500); navController.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } }
    }

    Box(modifier = Modifier.fillMaxSize().background(VaultDark), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(visible = stage >= 1, enter = fadeIn() + scaleIn()) {
                Icon(Icons.Default.Security, null, tint = VaultPurple, modifier = Modifier.size(100.dp))
            }
            Spacer(height = 32.dp)
            AnimatedVisibility(visible = stage >= 2, enter = fadeIn() + slideInVertically { it / 2 }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LifeVault", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = TextWhite)
                    Text("Zero-Knowledge Personal Archive", fontSize = 14.sp, color = TextGrey)
                    Spacer(height = 48.dp)
                    val progress = if(stage==2) 0.6f else if(stage==3) 1.0f else 0f
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.width(200.dp).height(4.dp).clip(RoundedCornerShape(50)), color = VaultPurple, trackColor = VaultCardBg)
                    Spacer(height = 16.dp)
                    Text(if(stage < 3) "Creating secure enclave..." else "Vault Ready.", color = VaultPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// --- 2. POLISHED DASHBOARD ---
@Composable
fun DashboardScreen(navController: NavController, viewModel: MainViewModel, onMemorySelected: (String) -> Unit) {
    var showUploadSheet by remember { mutableStateOf(false) }
    Scaffold(
        containerColor = VaultDark,
        topBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(24.dp, 48.dp, 24.dp, 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("My Vault", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextWhite); Text("3 items secured", fontSize = 14.sp, color = TextGrey) }
                // Settings Button
                IconButton(onClick = { navController.navigate("settings") }, modifier = Modifier.background(VaultCardBg, CircleShape)) {
                    Icon(Icons.Default.Settings, null, tint = TextGrey)
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadSheet = true; viewModel.resetStates() }, containerColor = VaultPurple, contentColor = TextWhite, shape = RoundedCornerShape(16.dp), modifier = Modifier.size(64.dp)) { Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp)) }
        }
    ) { p ->
        Column(modifier = Modifier.padding(p)) {
            PremiumStatusCard(viewModel.walletAddress)
            Spacer(height = 24.dp)
            Text("Recent Activity", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(height = 16.dp)
            LazyColumn(contentPadding = PaddingValues(bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(viewModel.memories) { mem -> PremiumMemoryCard(mem, onClick = { onMemorySelected(mem.id) }) }
            }
        }
        if (showUploadSheet) {
            EnhancedUploadModal(viewModel) { showUploadSheet = false }
        }
    }
}

// --- 3. NEW SECURITY SETTINGS SCREEN ---
@Composable
fun SettingsScreen(navController: NavController, viewModel: MainViewModel) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var showWarningDialog by remember { mutableStateOf(false) }
    var revealedKey by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().background(VaultDark).padding(24.dp)) {
        Spacer(height = 24.dp)
        IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.background(VaultCardBg, CircleShape)) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) }
        Spacer(height = 24.dp)
        Text("Security Center", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Text("Manage your cryptographic keys", fontSize = 14.sp, color = TextGrey)
        Spacer(height = 48.dp)

        // Public Address Card
        Card(colors = CardDefaults.cardColors(containerColor = VaultCardBg), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Text("Public Vault Address", color = TextGrey, fontSize = 12.sp)
                Spacer(height = 8.dp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountBalanceWallet, null, tint = VaultBlue, modifier = Modifier.size(20.dp))
                    Spacer(width = 12.dp)
                    Text(viewModel.walletAddress.take(10) + "..." + viewModel.walletAddress.takeLast(6), color = TextWhite, fontSize = 16.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { clipboard.setText(AnnotatedString(viewModel.walletAddress)); Toast.makeText(context, "Address Copied", Toast.LENGTH_SHORT).show() }) { Icon(Icons.Default.ContentCopy, null, tint = VaultBlue) }
                }
            }
        }
        Spacer(height = 24.dp)

        // Private Key Section (Dangerous!)
        Text("Danger Zone", color = WarningColor, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start=4.dp))
        Spacer(height = 8.dp)
        Card(colors = CardDefaults.cardColors(containerColor = VaultCardBg), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Warning, null, tint = WarningColor)
                    Spacer(width = 12.dp)
                    Column {
                        Text("Backup Private Key", color = TextWhite, fontWeight = FontWeight.Bold)
                        Text("Your permanent access token. Never share.", color = TextGrey, fontSize = 12.sp)
                    }
                }
                Spacer(height = 16.dp)
                if (revealedKey.isEmpty()) {
                    Button(onClick = { showWarningDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = WarningColor.copy(alpha = 0.2f), contentColor = WarningColor), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text("Reveal Key (Requires Caution)")
                    }
                } else {
                    Box(Modifier.fillMaxWidth().background(VaultDark, RoundedCornerShape(8.dp)).padding(12.dp).clickable { clipboard.setText(AnnotatedString(revealedKey)); Toast.makeText(context, "PRIVATE KEY COPIED! BE CAREFUL!", Toast.LENGTH_LONG).show() }) {
                        Text(revealedKey.take(8) + "••••••••••••••••" + revealedKey.takeLast(4), color = TextWhite, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, overflow = TextOverflow.Ellipsis, maxLines = 1)
                    }
                    Text("Tap above to copy. Clear screen when done.", color = TextGrey, fontSize = 11.sp, modifier = Modifier.padding(top=8.dp))
                }
            }
        }
    }

    if (showWarningDialog) {
        AlertDialog(
            onDismissRequest = { showWarningDialog = false },
            icon = { Icon(Icons.Rounded.Warning, null, tint = WarningColor, modifier = Modifier.size(48.dp)) },
            title = { Text("Extreme Security Warning", textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
            text = { Text("If anyone sees your private key, they have full control of your vault. Are you absolutely sure you are in a safe, private location?") },
            confirmButton = { Button(onClick = { revealedKey = viewModel.getPrivateKeyForDisplay(); showWarningDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)) { Text("Yes, Reveal It") } },
            dismissButton = { TextButton(onClick = { showWarningDialog = false }) { Text("Cancel") } },
            containerColor = VaultCardBg, titleContentColor = TextWhite, textContentColor = TextGrey
        )
    }
}


// --- 4. ENHANCED UPLOAD MODAL (File Picking) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedUploadModal(viewModel: MainViewModel, onDismiss: () -> Unit) {
    var title by remember { mutableStateOf("") }
    val uiState = viewModel.uploadState
    // File Pickers
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? -> viewModel.selectedFileUri = uri }
    val docPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? -> viewModel.selectedFileUri = uri }

    ModalBottomSheet(onDismissRequest = { if(uiState !is UiState.Loading) onDismiss() }, containerColor = VaultCardBg, dragHandle = { BottomSheetDefaults.DragHandle(color = VaultPurple) }) {
        Column(Modifier.padding(24.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if(uiState is UiState.Success) "Secured Successfully!" else "Secure New Asset", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(height = 24.dp)

            when(uiState) {
                is UiState.Idle -> {
                    if (viewModel.selectedFileUri == null) {
                        // Step 1: Pick File Type
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            UploadOptionCard("Photo/Video", Icons.Outlined.Image, VaultBlue) { imagePicker.launch("image/*") }
                            UploadOptionCard("Document", Icons.Outlined.Description, VaultPurple) { docPicker.launch(arrayOf("application/pdf", "application/msword")) }
                        }
                    } else {
                        // Step 2: Add details & confirm
                        Text("Selected File Ready", color = VaultPurple, fontWeight = FontWeight.Bold)
                        Spacer(height = 16.dp)
                        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Asset Title") }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VaultPurple, unfocusedBorderColor = VaultCardBg, focusedContainerColor = VaultDark, unfocusedContainerColor = VaultDark, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
                        Spacer(height = 24.dp)
                        Button(onClick = { viewModel.secureSelectedFile(title) }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = VaultPurple), enabled = title.isNotEmpty()) {
                            Row { Icon(Icons.Default.Lock, null); Spacer(width = 8.dp); Text("Encrypt & Mint Now") }
                        }
                    }
                }
                is UiState.Loading -> { CircularProgressIndicator(color = VaultPurple); Spacer(height=16.dp); Text("Encrypting & syncing to blockchain...", color = TextGrey) }
                is UiState.Success -> { Icon(Icons.Outlined.Check, null, tint = Success, modifier = Modifier.size(64.dp)); Spacer(height=16.dp); Text("Ownership token minted.", color = TextGrey); Spacer(height=24.dp); Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = VaultCardBg)) { Text("Close") } }
                is UiState.Error -> { Icon(Icons.Rounded.Warning, null, tint = ErrorRed); Spacer(height=8.dp); Text(uiState.message, color = ErrorRed); Spacer(height=16.dp); Button(onClick = { viewModel.resetStates() }) { Text("Try Again") } }
            }
            Spacer(height = 36.dp)
        }
    }
}

// --- 5. PREMIUM UI COMPONENTS ---

@Composable
fun UploadOptionCard(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(140.dp).height(100.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = VaultDark)) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(height = 8.dp)
            Text(text, color = TextWhite, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable fun PremiumStatusCard(address: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(24.dp)) {
        Box(modifier = Modifier.background(Brush.linearGradient(listOf(VaultPurple, VaultBlue))).padding(24.dp).fillMaxWidth()) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Shield, null, tint = TextWhite.copy(0.8f), modifier=Modifier.size(16.dp)); Spacer(width=8.dp); Text("Vault Status: Active", color = TextWhite.copy(0.8f), fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                Spacer(height = 12.dp)
                Text("Protected by zero-knowledge cryptography.", color = TextWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                Spacer(height = 16.dp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(VaultDark.copy(0.3f), CircleShape).padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Default.Link, null, tint = VaultBlue, modifier = Modifier.size(14.dp)); Spacer(width = 6.dp)
                    Text(address.take(6) + "..." + address.takeLast(4), color = TextWhite, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable fun PremiumMemoryCard(item: MemoryItem, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = VaultCardBg), onClick = onClick) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).background(VaultDark, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(if(item.title.contains("Photo")) Icons.Outlined.Image else Icons.Outlined.Description, null, tint = VaultPurple)
            }
            Spacer(width = 16.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(height = 4.dp)
                Text(item.date, color = TextGrey, fontSize = 12.sp)
            }
            Spacer(width = 12.dp)
            // Status Indicator
            if(item.isSecured) { Icon(Icons.Outlined.Check, null, tint = Success, modifier = Modifier.size(20.dp)) }
            else { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = WarningColor) }
        }
    }
}

// (Reusing ShareScreen from previous code, just theme colors applied implicitly)
@Composable fun ShareScreen(navController: NavController, viewModel: MainViewModel, memoryId: String) { /* ... Keep previous implementation, colors will adapt ... */
    val uiState = viewModel.shareState; val cb = LocalClipboardManager.current; val ctx = LocalContext.current
    LaunchedEffect(Unit) { viewModel.resetStates() }
    Column(Modifier.fillMaxSize().background(VaultDark).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(height = 24.dp); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) { IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.background(VaultCardBg, CircleShape)) { Icon(Icons.Default.ArrowBack, null, tint = TextWhite) } }
        Spacer(height = 32.dp); Box(Modifier.size(100.dp).background(VaultCardBg, CircleShape), contentAlignment = Alignment.Center) { Icon(Icons.Default.Share, null, tint = VaultPurple, modifier=Modifier.size(40.dp)) }
        Spacer(height = 24.dp); Text("Share Access", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextWhite)
        Text("Generate a time-limited secure link.", fontSize = 14.sp, color = TextGrey)
        Spacer(height = 48.dp)
        // ... (Remainder of ShareScreen logic from previous iteration is fine) ...
        Button(onClick = { if (uiState is UiState.Success) { cb.setText(AnnotatedString(uiState.txHash)); Toast.makeText(ctx, "Link copied!", Toast.LENGTH_SHORT).show() } else { viewModel.shareMemory(memoryId) } }, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = VaultPurple), shape = RoundedCornerShape(16.dp), enabled = uiState !is UiState.Loading) {
            when (uiState) { is UiState.Idle -> Text("Generate 24h Link"); is UiState.Loading -> CircularProgressIndicator(color = TextWhite, modifier=Modifier.size(24.dp)); is UiState.Success -> Text("Copy Link"); is UiState.Error -> Text("Failed") }
        }
    }
}
// Helper for spacing
@Composable fun Spacer(height: androidx.compose.ui.unit.Dp = 0.dp, width: androidx.compose.ui.unit.Dp = 0.dp) { Spacer(Modifier.height(height).width(width)) }