package com.example.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.presentation.screens.budget.BudgetScreen
import com.example.presentation.screens.category.CategoryManagementScreen
import com.example.presentation.screens.daily.DailyScreen
import com.example.presentation.screens.history.HistoryScreen
import com.example.presentation.screens.home.HomeScreen
import com.example.presentation.screens.khata.KhataScreen
import com.example.presentation.screens.onboarding.OnboardingScreen
import com.example.presentation.screens.recurring.RecurringScreen
import com.example.presentation.screens.report.ReportScreen
import com.example.presentation.screens.settings.SettingsScreen
import com.example.presentation.screens.wallet.WalletScreen
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
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                ) {
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .height(68.dp)
                    ) {
                        BottomNavScreens.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = {
                                    Box(
                                        modifier = if (selected) {
                                            Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
                                                .padding(horizontal = 14.dp, vertical = 4.dp)
                                        } else {
                                            Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                                        },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = screen.icon!!,
                                            contentDescription = screen.title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = screen.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = if (selected) 11.5.sp else 11.sp
                                    )
                                },
                                selected = selected,
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = Color.Transparent,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                ),
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
            }
        },
        floatingActionButton = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (BottomNavScreens.any { it.route == currentRoute }) {
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.Add.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 2.dp
                    ),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(Icons.Filled.Add, "নতুন হিসাব", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "নতুন হিসাব",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
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
                    },
                    onNavigateToKhata = { navController.navigate(Screen.Khata.route) },
                    onNavigateToWallets = { navController.navigate(Screen.Wallets.route) },
                    onNavigateToBudgets = { navController.navigate(Screen.Budgets.route) },
                    onNavigateToRecurring = { navController.navigate(Screen.Recurring.route) },
                    onNavigateToOnboarding = { navController.navigate(Screen.Onboarding.route) },
                    onNavigateToCategoryManagement = { navController.navigate(Screen.CategoryManagement.route) }
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
            composable(Screen.Khata.route) {
                KhataScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Wallets.route) {
                WalletScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Budgets.route) {
                BudgetScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Recurring.route) {
                RecurringScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    onFinished = { navController.popBackStack() }
                )
            }
            composable(Screen.Profile.route) {
                SettingsScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onSignOut = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToCategoryManagement = {
                        navController.navigate(Screen.CategoryManagement.route)
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
                    },
                    onNavigateToCategoryManagement = {
                        navController.navigate(Screen.CategoryManagement.route)
                    }
                )
            }
            composable(Screen.CategoryManagement.route) {
                CategoryManagementScreen(
                    viewModel = viewModel(factory = viewModelFactory),
                    onNavigateBack = { navController.popBackStack() }
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
