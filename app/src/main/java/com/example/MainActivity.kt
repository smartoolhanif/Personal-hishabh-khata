package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.example.presentation.navigation.AppNavHost
import com.example.presentation.viewmodel.AppViewModelProvider
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

    private lateinit var app: HisabKhataApp

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        var isAppReady = false
        splashScreen.setKeepOnScreenCondition { !isAppReady }
        
        app = application as HisabKhataApp
        val viewModelFactory = AppViewModelProvider(
            authRepository = app.container.authRepository,
            transactionRepository = app.container.transactionRepository,
            khataRepository = app.container.khataRepository,
            walletRepository = app.container.walletRepository,
            budgetRepository = app.container.budgetRepository,
            recurringRepository = app.container.recurringRepository,
            geminiService = app.container.geminiService,
            biometricHelper = app.container.biometricHelper,
            biometricPreferenceManager = app.container.biometricPreferenceManager,
            sessionLockManager = app.container.sessionLockManager
        )
        
        setContent {
            SideEffect {
                isAppReady = true
            }
            MyApplicationTheme {
                val navController = rememberNavController()
                AppNavHost(
                    navController = navController,
                    viewModelFactory = viewModelFactory,
                    appContainer = app.container
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        app.container.sessionLockManager.onAppBackgrounded()
    }

    override fun onStart() {
        super.onStart()
        val currentUserId = try {
            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
        } catch (e: Exception) {
            null
        }
        val isBioEnabled = currentUserId?.let { app.container.biometricPreferenceManager.isBiometricEnabled(it) } ?: false
        app.container.sessionLockManager.onAppForegrounded(isBioEnabled)
    }
}

