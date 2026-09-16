package com.example.presentation.screens.settings

import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit = {}
) {
    ProfileScreen(
        viewModel = viewModel,
        onSignOut = onSignOut,
        onNavigateToCategoryManagement = onNavigateToCategoryManagement
    )
}
