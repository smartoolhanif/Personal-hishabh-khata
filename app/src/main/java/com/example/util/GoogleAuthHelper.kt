package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes

sealed class GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult()
    object Cancelled : GoogleSignInResult()
    data class Error(val message: String) : GoogleSignInResult()
}

class GoogleAuthHelper(private val context: Context) {
    
    // Web Client ID from Firebase hisab-93d73
    private val defaultWebClientId = "422990485958-2brv7g49rhrjvcdfdctagkoc4ho3fg78.apps.googleusercontent.com"
    
    fun getServerClientId(): String {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        return if (resId != 0) {
            val resStr = context.getString(resId)
            if (resStr.isNotBlank() && !resStr.startsWith("YOUR_")) resStr else defaultWebClientId
        } else {
            defaultWebClientId
        }
    }

    fun getGoogleSignInClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getServerClientId())
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    fun parseSignInResult(data: Intent?): GoogleSignInResult {
        if (data == null) return GoogleSignInResult.Cancelled
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (!idToken.isNullOrBlank()) {
                GoogleSignInResult.Success(idToken)
            } else {
                GoogleSignInResult.Error("গুগল আইডি টোকেন পাওয়া যায়নি")
            }
        } catch (e: ApiException) {
            if (e.statusCode == CommonStatusCodes.CANCELED || e.statusCode == 12501 || e.statusCode == 12502) {
                Log.d("GoogleAuthHelper", "User cancelled sign-in flow")
                GoogleSignInResult.Cancelled
            } else {
                Log.e("GoogleAuthHelper", "Google Sign-In failed: code ${e.statusCode}", e)
                GoogleSignInResult.Error("গুগল সাইন-ইন ব্যর্থ হয়েছে (${e.statusCode})")
            }
        } catch (e: Exception) {
            Log.e("GoogleAuthHelper", "Sign-in exception", e)
            GoogleSignInResult.Error(e.message ?: "লগইন ব্যর্থ হয়েছে")
        }
    }
}


