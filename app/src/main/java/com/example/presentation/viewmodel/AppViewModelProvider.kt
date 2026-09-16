package com.example.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.data.ai.GeminiService
import com.example.data.preference.BiometricPreferenceManager
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.KhataRepository
import com.example.domain.repository.RecurringRepository
import com.example.domain.repository.TransactionRepository
import com.example.domain.repository.WalletRepository
import com.example.presentation.screens.add.AddTransactionViewModel
import com.example.presentation.screens.ai.AiAssistantViewModel
import com.example.presentation.screens.auth.AuthViewModel
import com.example.presentation.screens.budget.BudgetViewModel
import com.example.presentation.screens.daily.DailyViewModel
import com.example.presentation.screens.history.HistoryViewModel
import com.example.presentation.screens.home.HomeViewModel
import com.example.presentation.screens.khata.KhataViewModel
import com.example.presentation.screens.recurring.RecurringViewModel
import com.example.presentation.screens.report.ReportViewModel
import com.example.presentation.screens.settings.SettingsViewModel
import com.example.presentation.screens.wallet.WalletViewModel
import com.example.util.biometric.BiometricHelper
import com.example.util.biometric.SessionLockManager

class AppViewModelProvider(
    private val authRepository: AuthRepository,
    private val transactionRepository: TransactionRepository,
    private val khataRepository: KhataRepository? = null,
    private val walletRepository: WalletRepository? = null,
    private val budgetRepository: BudgetRepository? = null,
    private val recurringRepository: RecurringRepository? = null,
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
                AddTransactionViewModel(transactionRepository, walletRepository) as T
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
            modelClass.isAssignableFrom(KhataViewModel::class.java) -> {
                KhataViewModel(khataRepository ?: throw IllegalStateException("KhataRepository not provided")) as T
            }
            modelClass.isAssignableFrom(WalletViewModel::class.java) -> {
                WalletViewModel(
                    walletRepository = walletRepository ?: throw IllegalStateException("WalletRepository not provided"),
                    transactionRepository = transactionRepository
                ) as T
            }
            modelClass.isAssignableFrom(BudgetViewModel::class.java) -> {
                BudgetViewModel(
                    budgetRepository = budgetRepository ?: throw IllegalStateException("BudgetRepository not provided"),
                    transactionRepository = transactionRepository
                ) as T
            }
            modelClass.isAssignableFrom(RecurringViewModel::class.java) -> {
                RecurringViewModel(
                    recurringRepository = recurringRepository ?: throw IllegalStateException("RecurringRepository not provided"),
                    transactionRepository = transactionRepository
                ) as T
            }
            modelClass.isAssignableFrom(com.example.presentation.screens.category.CategoryViewModel::class.java) -> {
                com.example.presentation.screens.category.CategoryViewModel(
                    transactionRepository = transactionRepository
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    authRepository,
                    biometricHelper,
                    biometricPreferenceManager,
                    sessionLockManager
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
