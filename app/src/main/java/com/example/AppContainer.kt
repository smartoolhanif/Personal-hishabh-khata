package com.example

import android.content.Context
import com.example.data.ai.GeminiService
import com.example.data.firebase.FirebaseAuthRepository
import com.example.data.firebase.FirebaseTransactionRepository
import com.example.data.preference.BiometricPreferenceManager
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.TransactionRepository
import com.example.util.biometric.BiometricHelper
import com.example.util.biometric.SessionLockManager

class AppContainer(private val context: Context) {
    val authRepository: AuthRepository by lazy {
        FirebaseAuthRepository()
    }
    
    val transactionRepository: TransactionRepository by lazy {
        FirebaseTransactionRepository()
    }
    
    val geminiService: GeminiService by lazy {
        GeminiService()
    }

    val biometricHelper: BiometricHelper by lazy {
        BiometricHelper()
    }

    val biometricPreferenceManager: BiometricPreferenceManager by lazy {
        BiometricPreferenceManager(context.applicationContext)
    }

    val sessionLockManager: SessionLockManager by lazy {
        SessionLockManager()
    }
}
