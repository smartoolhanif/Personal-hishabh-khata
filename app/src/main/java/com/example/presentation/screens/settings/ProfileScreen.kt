package com.example.presentation.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.util.biometric.BiometricStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val biometricStatus = remember(context) { viewModel.getBiometricStatus(context) }
    val isBiometricAvailable = biometricStatus == BiometricStatus.Available

    var showDisableConfirmDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember(currentUser?.name) { mutableStateOf(currentUser?.name ?: "") }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    val promptTitle = stringResource(R.string.biometric_prompt_title)
    val promptSubtitle = stringResource(R.string.biometric_prompt_subtitle)
    val promptCancel = stringResource(R.string.biometric_prompt_cancel)
    val enabledToast = stringResource(R.string.biometric_enabled_toast)
    val disabledToast = stringResource(R.string.biometric_disabled_toast)

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = {
                Text(
                    text = "নাম পরিবর্তন করুন (Edit Name)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column {
                    Text(
                        text = "আপনার প্রদর্শন নাম লিখুন:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            viewModel.updateDisplayName(editedName.trim()) { success ->
                                if (success) {
                                    Toast.makeText(context, "নাম সফলভাবে পরিবর্তিত হয়েছে", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showEditNameDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006D44))
                ) {
                    Text("সংরক্ষণ (Save)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("বাতিল (Cancel)")
                }
            }
        )
    }

    // Biometric Disable Confirmation Dialog
    if (showDisableConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDisableConfirmDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.dialog_disable_biometric_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_disable_biometric_desc),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.disableBiometric()
                        showDisableConfirmDialog = false
                        Toast.makeText(context, disabledToast, Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.btn_disable))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableConfirmDialog = false }) {
                    Text(stringResource(R.string.btn_cancel))
                }
            }
        )
    }

    // Sign Out Confirmation Dialog
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            title = { Text("সাইন আউট নিশ্চিতকরণ", fontWeight = FontWeight.Bold) },
            text = { Text("আপনি কি নিশ্চিতভাবে এই অ্যাকাউন্ট থেকে সাইন আউট করতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        viewModel.signOut()
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("সাইন আউট (Sign Out)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmDialog = false }) {
                    Text("বাতিল (Cancel)")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "প্রোফাইল ও সেটিংস (Profile)",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Circular Profile Avatar with decorative border
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF006D44), Color(0xFFD4AF37))
                                ),
                                shape = CircleShape
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentUser?.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(currentUser?.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initial = (currentUser?.name?.firstOrNull() ?: 'U').uppercaseChar().toString()
                            Text(
                                text = initial,
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp,
                                color = Color(0xFF006D44)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Name with Edit Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentUser?.name?.takeIf { it.isNotBlank() } ?: "Md Hanif",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = {
                                editedName = currentUser?.name ?: ""
                                showEditNameDialog = true
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit Name",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Email Address
                    if (!currentUser?.email.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Verified",
                                tint = Color(0xFF00897B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentUser?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            editedName = currentUser?.name ?: ""
                            showEditNameDialog = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("নাম পরিবর্তন করুন (Edit Display Name)")
                    }
                }
            }

            // 2. Security Section: Biometric Fingerprint / Face Unlock
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "অ্যাপের নিরাপত্তা (App Security)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Fingerprint,
                                    contentDescription = null,
                                    tint = if (isBiometricAvailable) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ফিঙ্গারপ্রিন্ট আনলক (Biometric)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = when {
                                        !isBiometricAvailable -> "ডিভাইসে সমর্থিত নয়"
                                        isBiometricEnabled -> "● চালু রয়েছে (Enabled)"
                                        else -> "○ বন্ধ রয়েছে (Disabled)"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isBiometricEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isBiometricAvailable) {
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        if (activity != null) {
                                            viewModel.enableBiometric(
                                                activity = activity,
                                                title = promptTitle,
                                                subtitle = promptSubtitle,
                                                cancelText = promptCancel,
                                                onResult = { success, _ ->
                                                    if (success) {
                                                        Toast.makeText(context, enabledToast, Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            )
                                        }
                                    } else {
                                        showDisableConfirmDialog = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 3. App Settings & Sync Preferences
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Currency
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Paid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("মুদ্রা ফরম্যাট (Currency)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("বাংলাদেশী টাকা (৳ BDT)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Cloud Sync
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ডাটা ক্লাউড সিঙ্ক (Data Sync)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("Firebase ক্লাউডে সংরক্ষিত ও সুরক্ষিত", style = MaterialTheme.typography.bodySmall, color = Color(0xFF00897B))
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Category Management
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToCategoryManagement() }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("ক্যাটেগরি ব্যবস্থাপনা (Categories)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("আয় ও ব্যয়ের ক্যাটেগরি যোগ ও এডিট করুন", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Manage",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // App Version & Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("অ্যাপ সংস্করণ (App Version)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text("HANIF HISAB KHATA v2.0 (Official)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 4. Sign Out Button
            Button(
                onClick = { showSignOutConfirmDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = "Sign Out")
                Spacer(modifier = Modifier.width(8.dp))
                Text("অ্যাকাউন্ট থেকে সাইন আউট (Sign Out)", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}
