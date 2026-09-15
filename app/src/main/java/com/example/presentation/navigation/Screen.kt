package com.example.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    object Splash : Screen("splash", "Splash")
    object Login : Screen("login", "Login")
    object BiometricLock : Screen("biometric_lock", "Unlock")
    object Home : Screen("home", "Home", Icons.Filled.Home)
    object Daily : Screen("daily", "Daily", Icons.Filled.CalendarToday)
    object Add : Screen("add", "Add Transaction")
    object Edit : Screen("edit/{transactionId}", "Edit Transaction") {
        fun createRoute(transactionId: String) = "edit/$transactionId"
    }
    object Reports : Screen("reports", "Reports", Icons.Filled.BarChart)
    object History : Screen("history", "History", Icons.Filled.History)
    object Profile : Screen("profile", "Profile", Icons.Filled.Person)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    object AiAssistant : Screen("ai_assistant", "AI Assistant")
}

val BottomNavScreens = listOf(
    Screen.Home,
    Screen.Daily,
    Screen.Reports,
    Screen.History,
    Screen.Profile
)
