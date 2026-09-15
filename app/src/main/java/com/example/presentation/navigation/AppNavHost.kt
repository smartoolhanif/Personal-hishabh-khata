package com.example.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.AppContainer
import com.example.presentation.screens.add.AddTransactionScreen
import com.example.presentation.screens.ai.AiAssistantScreen
import com.example.presentation.screens.auth.AuthViewModel
import com.example.presentation.screens.auth.BiometricLockScreen
import com.example.presentation.screens.auth.LoginScreen
import com.example.presentation.screens.auth.SplashScreen
import com.example.presentation.screens.daily.DailyScreen
import com.example.presentation.screens.history.HistoryScreen
import com.example.presentation.screens.home.HomeScreen
import com.example.presentation.screens.report.ReportScreen
import com.example.presentation.screens.settings.SettingsScreen
import com.example.presentation.viewmodel.AppViewModelProvider

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModelFactory: AppViewModelProvider,
    appContainer: AppContainer
) {
    val sessionUnlocked by appContainer.sessionLockManager.isUnlocked.collectAsState()
    val authUser by appContainer.authRepository.currentUser.collectAsState(initial = null)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Enforce lock if session becomes locked while user is on protected screens
    LaunchedEffect(sessionUnlocked, authUser, currentRoute) {
        val user = authUser
        if (user != null && currentRoute != null &&
            currentRoute != Screen.Splash.route &&
            currentRoute != Screen.Login.route &&
            currentRoute != Screen.BiometricLock.route
        ) {
            val isBioEnabled = appContainer.biometricPreferenceManager.isBiometricEnabled(user.uid)
            if (isBioEnabled && !sessionUnlocked) {
                navController.navigate(Screen.BiometricLock.route) {
                    launchSingleTop = true
                }
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (BottomNavScreens.any { it.route == currentRoute }) {
                NavigationBar {
                    BottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (BottomNavScreens.any { it.route == currentRoute }) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Add.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, "Add Transaction")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    },
                    onNavigateToBiometricLock = {
                        navController.navigate(Screen.BiometricLock.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.BiometricLock.route) {
                val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
                BiometricLockScreen(
                    biometricHelper = authViewModel.biometricHelper,
                    onUnlockSuccess = {
                        val user = authViewModel.currentUser.value
                        if (user != null) {
                            authViewModel.unlockSession(user.uid)
                        }
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.BiometricLock.route) { inclusive = true }
                        }
                    },
                    onUseGoogleAccount = {
                        authViewModel.signOut()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.BiometricLock.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateToAi = { navController.navigate(Screen.AiAssistant.route) },
                    onNavigateToHistory = { navController.navigate(Screen.History.route) },
                    onNavigateToEdit = { transactionId ->
                        navController.navigate(Screen.Edit.createRoute(transactionId))
                    }
                )
            }
            composable(Screen.Daily.route) {
                DailyScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateToEdit = { transactionId ->
                        navController.navigate(Screen.Edit.createRoute(transactionId))
                    }
                )
            }
            composable(Screen.Add.route) {
                AddTransactionScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    transactionIdToEdit = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.Edit.route,
                arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
            ) { backStackEntry ->
                val transactionId = backStackEntry.arguments?.getString("transactionId")
                AddTransactionScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    transactionIdToEdit = transactionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Reports.route) {
                ReportScreen(
                    viewModel = viewModel(factory = viewModelFactory)
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateToEdit = { transactionId ->
                        navController.navigate(Screen.Edit.createRoute(transactionId))
                    }
                )
            }
            composable(Screen.Profile.route) {
                SettingsScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.AiAssistant.route) {
                AiAssistantScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
