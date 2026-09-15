package com.example.presentation.screens.auth

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.R
import com.example.util.GoogleAuthHelper
import com.example.util.GoogleSignInResult
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val coroutineScope = rememberCoroutineScope()
    
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val user by viewModel.currentUser.collectAsState()

    val googleAuthHelper = remember(context) { GoogleAuthHelper(context) }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (val signInResult = googleAuthHelper.parseSignInResult(result.data)) {
            is GoogleSignInResult.Success -> {
                viewModel.signInWithGoogle(signInResult.idToken)
            }
            is GoogleSignInResult.Cancelled -> {
                // User dismissed account chooser
            }
            is GoogleSignInResult.Error -> {
                viewModel.setError(signInResult.message)
            }
        }
    }

    var showBiometricSetupDialog by remember { mutableStateOf(false) }

    val promptTitle = stringResource(R.string.biometric_prompt_title)
    val promptSubtitle = stringResource(R.string.biometric_prompt_subtitle)
    val promptCancel = stringResource(R.string.biometric_prompt_cancel)
    val enabledToast = stringResource(R.string.biometric_enabled_toast)

    LaunchedEffect(user) {
        val currentUser = user
        if (currentUser != null) {
            val isBioAvailable = viewModel.isBiometricAvailable(context)
            val hasPrompted = viewModel.hasPromptedSetup(currentUser.uid)
            if (isBioAvailable && !hasPrompted) {
                showBiometricSetupDialog = true
            } else {
                viewModel.unlockSession(currentUser.uid)
                onLoginSuccess()
            }
        }
    }

    if (showBiometricSetupDialog) {
        BiometricSetupDialog(
            onEnable = {
                val currentUser = user
                if (currentUser != null && activity != null) {
                    viewModel.enableBiometric(
                        activity = activity,
                        userId = currentUser.uid,
                        title = promptTitle,
                        subtitle = promptSubtitle,
                        cancelText = promptCancel,
                        onResult = { success, _ ->
                            if (success) {
                                showBiometricSetupDialog = false
                                Toast.makeText(context, enabledToast, Toast.LENGTH_SHORT).show()
                                onLoginSuccess()
                            }
                        }
                    )
                }
            },
            onNotNow = {
                val currentUser = user
                if (currentUser != null) {
                    viewModel.skipBiometricSetup(currentUser.uid)
                }
                showBiometricSetupDialog = false
                onLoginSuccess()
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome to Hanif Hisab Khata",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        val client = googleAuthHelper.getGoogleSignInClient()
                        client.signOut().addOnCompleteListener {
                            googleSignInLauncher.launch(client.signInIntent)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Google দিয়ে সাইন-ইন করুন", style = MaterialTheme.typography.titleMedium)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.signInAnonymously()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("সরাসরি শুরু করুন (গেস্ট মোড)", style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

