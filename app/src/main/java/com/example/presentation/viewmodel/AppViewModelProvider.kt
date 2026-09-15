package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.ai.GeminiService
import com.example.data.preference.BiometricPreferenceManager
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.TransactionRepository
import com.example.presentation.screens.add.AddTransactionViewModel
import com.example.presentation.screens.ai.AiAssistantViewModel
import com.example.presentation.screens.auth.AuthViewModel
import com.example.presentation.screens.daily.DailyViewModel
import com.example.presentation.screens.history.HistoryViewModel
import com.example.presentation.screens.home.HomeViewModel
import com.example.presentation.screens.report.ReportViewModel
import com.example.presentation.screens.settings.SettingsViewModel
import com.example.util.biometric.BiometricHelper
import com.example.util.biometric.SessionLockManager

class AppViewModelProvider(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val geminiService: GeminiService,
    private val biometricHelper: BiometricHelper,
    private val biometricPreferenceManager: BiometricPreferenceManager,
    private val sessionLockManager: SessionLockManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(
                    authRepository,
                    biometricHelper,
                    biometricPreferenceManager,
                    sessionLockManager
                ) as T
            }
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(transactionRepository, authRepository) as T
            }
            modelClass.isAssignableFrom(AddTransactionViewModel::class.java) -> {
                AddTransactionViewModel(transactionRepository) as T
            }
            modelClass.isAssignableFrom(DailyViewModel::class.java) -> {
                DailyViewModel(transactionRepository) as T
            }
            modelClass.isAssignableFrom(ReportViewModel::class.java) -> {
                ReportViewModel(transactionRepository) as T
            }
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> {
                HistoryViewModel(transactionRepository) as T
            }
            modelClass.isAssignableFrom(AiAssistantViewModel::class.java) -> {
                AiAssistantViewModel(transactionRepository, geminiService) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    authRepository,
                    biometricHelper,
                    biometricPreferenceManager,
                    sessionLockManager
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
