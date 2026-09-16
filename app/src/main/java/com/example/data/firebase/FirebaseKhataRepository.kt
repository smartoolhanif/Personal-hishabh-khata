package com.example.data.firebase

import com.example.domain.model.KhataEntry
import com.example.domain.model.KhataParty
import com.example.domain.model.KhataType
import com.example.domain.repository.KhataRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseKhataRepository : KhataRepository {
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

    private fun getPartiesCollection() = currentUserId?.let { uid ->
        firestore?.collection("users")?.document(uid)?.collection("khata_parties")
    }

    private fun getEntriesCollection() = currentUserId?.let { uid ->
        firestore?.collection("users")?.document(uid)?.collection("khata_entries")
    }

    override fun getParties(): Flow<List<KhataParty>> {
        val collection = getPartiesCollection() ?: return emptyFlow()
        return collection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(KhataParty::class.java) }
                .sortedByDescending { it.lastUpdated }
        }
    }

    override suspend fun getPartyById(id: String): KhataParty? {
        val collection = getPartiesCollection() ?: return null
        return try {
            collection.document(id).get().await().toObject(KhataParty::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addParty(party: KhataParty): Result<Unit> {
        val collection = getPartiesCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docRef = collection.document()
            val newParty = party.copy(
                id = docRef.id,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
            docRef.set(newParty).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateParty(party: KhataParty): Result<Unit> {
        val collection = getPartiesCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(party.id).set(party.copy(lastUpdated = System.currentTimeMillis())).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteParty(partyId: String): Result<Unit> {
        val collection = getPartiesCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(partyId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getEntriesForParty(partyId: String): Flow<List<KhataEntry>> {
        val collection = getEntriesCollection() ?: return emptyFlow()
        return collection.whereEqualTo("partyId", partyId).snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(KhataEntry::class.java) }
                .sortedByDescending { it.timestamp }
        }
    }

    override suspend fun addEntry(entry: KhataEntry): Result<Unit> {
        val entriesCollection = getEntriesCollection() ?: return Result.failure(Exception("Not authenticated"))
        val partiesCollection = getPartiesCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docRef = entriesCollection.document()
            val newEntry = entry.copy(id = docRef.id, timestamp = System.currentTimeMillis())
            docRef.set(newEntry).await()

            // Update party total amount
            val partyDoc = partiesCollection.document(entry.partyId).get().await()
            val currentParty = partyDoc.toObject(KhataParty::class.java)
            if (currentParty != null) {
                val updatedAmount = if (entry.isPayment) {
                    (currentParty.totalAmount - entry.amount).coerceAtLeast(0.0)
                } else {
                    currentParty.totalAmount + entry.amount
                }
                partiesCollection.document(entry.partyId).update(
                    mapOf(
                        "totalAmount" to updatedAmount,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteEntry(entryId: String, partyId: String): Result<Unit> {
        val entriesCollection = getEntriesCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            entriesCollection.document(entryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
