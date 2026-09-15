package com.example.util.biometric

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SessionLockManager {
    companion object {
        const val BACKGROUND_LOCK_TIMEOUT_MS = 30_000L // 30 seconds background timeout
    }

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private var unlockedUserId: String? = null
    private var backgroundTimestamp: Long = 0L

    fun isUserSessionUnlocked(userId: String): Boolean {
        return _isUnlocked.value && unlockedUserId == userId
    }

    fun unlock(userId: String) {
        unlockedUserId = userId
        _isUnlocked.value = true
        backgroundTimestamp = 0L
    }

    fun lock() {
        _isUnlocked.value = false
        backgroundTimestamp = 0L
    }

    fun onUserSignedOut() {
        unlockedUserId = null
        _isUnlocked.value = false
        backgroundTimestamp = 0L
    }

    fun onAppBackgrounded() {
        backgroundTimestamp = System.currentTimeMillis()
    }

    fun onAppForegrounded(isBiometricEnabledForCurrentUser: Boolean) {
        if (backgroundTimestamp > 0L) {
            val backgroundDuration = System.currentTimeMillis() - backgroundTimestamp
            if (isBiometricEnabledForCurrentUser && backgroundDuration >= BACKGROUND_LOCK_TIMEOUT_MS) {
                // Background timeout elapsed, require unlock again
                lock()
            }
            backgroundTimestamp = 0L
        }
    }
}
