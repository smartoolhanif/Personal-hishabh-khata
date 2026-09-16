package com.example.data.firebase

import com.example.domain.model.RecurringItem
import com.example.domain.repository.RecurringRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseRecurringRepository : RecurringRepository {
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }

    private val currentUserId: String?
        get() = auth?.currentUser?.uid

    private fun getRecurringCollection() = currentUserId?.let { uid ->
        firestore?.collection("users")?.document(uid)?.collection("recurring")
    }

    override fun getRecurringItems(): Flow<List<RecurringItem>> {
        val collection = getRecurringCollection() ?: return emptyFlow()
        return collection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(RecurringItem::class.java) }
        }
    }

    override suspend fun addRecurringItem(item: RecurringItem): Result<Unit> {
        val collection = getRecurringCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docRef = collection.document()
            val newItem = item.copy(id = docRef.id)
            docRef.set(newItem).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRecurringItem(item: RecurringItem): Result<Unit> {
        val collection = getRecurringCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(item.id).set(item).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecurringItem(itemId: String): Result<Unit> {
        val collection = getRecurringCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(itemId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
