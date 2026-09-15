package com.example.presentation.screens.settings

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preference.BiometricPreferenceManager
import com.example.domain.model.UserProfile
import com.example.domain.repository.AuthRepository
import com.example.util.biometric.BiometricHelper
import com.example.util.biometric.BiometricStatus
import com.example.util.biometric.SessionLockManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val authRepository: AuthRepository,
    val biometricHelper: BiometricHelper,
    val biometricPreferenceManager: BiometricPreferenceManager,
    val sessionLockManager: SessionLockManager
) : ViewModel() {

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
        .let { flow ->
            val stateFlow = MutableStateFlow<UserProfile?>(null)
            viewModelScope.launch {
                flow.collect { user ->
                    stateFlow.value = user
                    if (user != null) {
                        _isBiometricEnabled.value = biometricPreferenceManager.isBiometricEnabled(user.uid)
                    }
                }
            }
            stateFlow
        }

    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    fun getBiometricStatus(context: Context): BiometricStatus {
        return biometricHelper.getBiometricStatus(context)
    }

    fun isBiometricAvailable(context: Context): Boolean {
        return biometricHelper.isBiometricAvailable(context)
    }

    fun enableBiometric(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        cancelText: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = currentUser.value ?: return onResult(false, "User not signed in")
        biometricHelper.authenticate(
            activity = activity,
            title = title,
            subtitle = subtitle,
            negativeButtonText = cancelText,
            onSuccess = {
                biometricPreferenceManager.setBiometricEnabled(user.uid, true)
                _isBiometricEnabled.value = true
                sessionLockManager.unlock(user.uid)
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

    fun disableBiometric() {
        val user = currentUser.value ?: return
        biometricPreferenceManager.setBiometricEnabled(user.uid, false)
        _isBiometricEnabled.value = false
    }

    fun updateDisplayName(newName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = authRepository.updateDisplayName(newName)
            onResult(res.isSuccess)
        }
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

