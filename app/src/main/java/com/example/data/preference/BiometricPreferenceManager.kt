package com.example.data.preference

import android.content.Context
import android.content.SharedPreferences

class BiometricPreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("hanif_biometric_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_BIOMETRIC_ENABLED_PREFIX = "biometric_enabled_"
        private const val KEY_SETUP_PROMPTED_PREFIX = "biometric_setup_prompted_"
        private const val KEY_LAST_LOGGED_IN_USER = "last_logged_in_user"
    }

    fun isBiometricEnabled(userId: String): Boolean {
        if (userId.isBlank()) return false
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED_PREFIX + userId, false)
    }

    fun setBiometricEnabled(userId: String, enabled: Boolean) {
        if (userId.isBlank()) return
        prefs.edit()
            .putBoolean(KEY_BIOMETRIC_ENABLED_PREFIX + userId, enabled)
            .apply()
    }

    fun hasPromptedSetup(userId: String): Boolean {
        if (userId.isBlank()) return false
        return prefs.getBoolean(KEY_SETUP_PROMPTED_PREFIX + userId, false)
    }

    fun setPromptedSetup(userId: String, prompted: Boolean) {
        if (userId.isBlank()) return
        prefs.edit()
            .putBoolean(KEY_SETUP_PROMPTED_PREFIX + userId, prompted)
            .apply()
    }

    fun getLastLoggedInUser(): String? {
        return prefs.getString(KEY_LAST_LOGGED_IN_USER, null)
    }

    fun setLastLoggedInUser(userId: String?) {
        prefs.edit()
            .putString(KEY_LAST_LOGGED_IN_USER, userId)
            .apply()
    }

    /**
     * Called when a user explicitly signs out from Google/Firebase.
     * Clears the setup prompted state and biometric enabled state for that user,
     * so that on next login, they are asked again and cannot bleed into another account.
     */
    fun clearForUser(userId: String) {
        if (userId.isBlank()) return
        prefs.edit()
            .remove(KEY_BIOMETRIC_ENABLED_PREFIX + userId)
            .remove(KEY_SETUP_PROMPTED_PREFIX + userId)
            .apply()
    }
}
