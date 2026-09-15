package com.example.presentation.screens.auth

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preference.BiometricPreferenceManager
import com.example.domain.model.UserProfile
import com.example.domain.repository.AuthRepository
import com.example.util.biometric.BiometricHelper
import com.example.util.biometric.SessionLockManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    val biometricHelper: BiometricHelper,
    val biometricPreferenceManager: BiometricPreferenceManager,
    val sessionLockManager: SessionLockManager
) : ViewModel() {
    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
        .let { flow ->
            val stateFlow = MutableStateFlow<UserProfile?>(null)
            viewModelScope.launch {
                flow.collect { stateFlow.value = it }
            }
            stateFlow
        }

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun isBiometricAvailable(context: Context): Boolean {
        return biometricHelper.isBiometricAvailable(context)
    }

    fun isBiometricEnabledForUser(userId: String): Boolean {
        return biometricPreferenceManager.isBiometricEnabled(userId)
    }

    fun hasPromptedSetup(userId: String): Boolean {
        return biometricPreferenceManager.hasPromptedSetup(userId)
    }

    fun enableBiometric(
        activity: FragmentActivity,
        userId: String,
        title: String,
        subtitle: String,
        cancelText: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        biometricHelper.authenticate(
            activity = activity,
            title = title,
            subtitle = subtitle,
            negativeButtonText = cancelText,
            onSuccess = {
                biometricPreferenceManager.setBiometricEnabled(userId, true)
                biometricPreferenceManager.setPromptedSetup(userId, true)
                sessionLockManager.unlock(userId)
                onResult(true, null)
            },
            onError = { _, errString ->
                onResult(false, errString.toString())
            },
            onFailed = {
                onResult(false, null)
            }
        )
    }

    fun skipBiometricSetup(userId: String) {
        biometricPreferenceManager.setPromptedSetup(userId, true)
        sessionLockManager.unlock(userId)
    }

    fun unlockSession(userId: String) {
        sessionLockManager.unlock(userId)
    }

    fun signIn(idToken: String) {
        signInWithGoogle(idToken)
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signInWithGoogle(idToken)
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "গুগল সাইন-ইন ব্যর্থ হয়েছে"
            }
            _isLoading.value = false
        }
    }

    fun signInAnonymously() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.signInAnonymously()
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.message ?: "গেস্ট লগইন ব্যর্থ হয়েছে"
            }
            _isLoading.value = false
        }
    }

    fun setError(message: String) {
        _error.value = message
    }

    fun signOut() {
        viewModelScope.launch {
            val user = currentUser.value
            if (user != null) {
                biometricPreferenceManager.clearForUser(user.uid)
            }
            sessionLockManager.onUserSignedOut()
            authRepository.signOut()
        }
    }
}

