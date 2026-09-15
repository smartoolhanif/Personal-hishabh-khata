package com.example.data.firebase

import com.example.domain.model.UserProfile
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository : AuthRepository {
    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    private val customDisplayName = MutableStateFlow<String?>(null)

    override val currentUser: Flow<UserProfile?> = callbackFlow {
        val currentAuth = auth
        if (currentAuth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                val name = customDisplayName.value ?: user.displayName ?: ""
                trySend(UserProfile(user.uid, name, user.email ?: "", user.photoUrl?.toString()))
            } else {
                trySend(null)
            }
        }
        currentAuth.addAuthStateListener(listener)
        awaitClose { currentAuth.removeAuthStateListener(listener) }
    }.combine(customDisplayName) { user, customName ->
        if (user != null && !customName.isNullOrBlank()) {
            user.copy(name = customName)
        } else {
            user
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<UserProfile> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase is not initialized. Please ensure google-services.json is configured."))
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = currentAuth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                customDisplayName.value = null
                Result.success(UserProfile(user.uid, user.displayName ?: "", user.email ?: "", user.photoUrl?.toString()))
            } else {
                Result.failure(Exception("Sign in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<UserProfile> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase is not initialized."))
        return try {
            val authResult = currentAuth.signInAnonymously().await()
            val user = authResult.user
            if (user != null) {
                customDisplayName.value = null
                Result.success(UserProfile(user.uid, "অতিথি", "", null))
            } else {
                Result.failure(Exception("Guest sign-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateDisplayName(name: String): Result<Unit> {
        val currentAuth = auth ?: return Result.failure(Exception("Firebase is not initialized."))
        val user = currentAuth.currentUser ?: return Result.failure(Exception("User not authenticated."))
        return try {
            val request = UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .build()
            user.updateProfile(request).await()
            customDisplayName.value = name

            // Also persist to firestore user document
            try {
                firestore?.collection("users")?.document(user.uid)?.set(
                    mapOf("name" to name, "email" to (user.email ?: ""), "updatedAt" to System.currentTimeMillis()),
                    SetOptions.merge()
                )?.await()
            } catch (_: Exception) {
                // Ignore firestore write error if rules/offline
            }

            Result.success(Unit)
        } catch (e: Exception) {
            // Still update local name if network is offline
            customDisplayName.value = name
            Result.success(Unit)
        }
    }

    override suspend fun signOut() {
        customDisplayName.value = null
        auth?.signOut()
    }
}
