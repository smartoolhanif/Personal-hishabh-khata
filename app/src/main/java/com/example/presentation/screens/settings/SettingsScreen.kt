package com.example.presentation.screens.settings

import androidx.compose.runtime.Composable

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onSignOut: () -> Unit
) {
    ProfileScreen(viewModel = viewModel, onSignOut = onSignOut)
}
