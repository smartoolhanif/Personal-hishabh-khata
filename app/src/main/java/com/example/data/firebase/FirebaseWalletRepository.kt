package com.example.data.firebase

import com.example.domain.model.Wallet
import com.example.domain.model.WalletTransfer
import com.example.domain.repository.WalletRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseWalletRepository : WalletRepository {
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

    private fun getWalletsCollection() = currentUserId?.let { uid ->
        firestore?.collection("users")?.document(uid)?.collection("wallets")
    }

    private fun getTransfersCollection() = currentUserId?.let { uid ->
        firestore?.collection("users")?.document(uid)?.collection("wallet_transfers")
    }

    override fun getWallets(): Flow<List<Wallet>> {
        val collection = getWalletsCollection() ?: return emptyFlow()
        return collection.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(Wallet::class.java) }
        }
    }

    override suspend fun addWallet(wallet: Wallet): Result<Unit> {
        val collection = getWalletsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docRef = collection.document()
            val newWallet = wallet.copy(
                id = docRef.id,
                currentBalance = if (wallet.currentBalance == 0.0) wallet.initialBalance else wallet.currentBalance
            )
            docRef.set(newWallet).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWallet(wallet: Wallet): Result<Unit> {
        val collection = getWalletsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(wallet.id).set(wallet).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBalance(walletId: String, delta: Double): Result<Unit> {
        val collection = getWalletsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docRef = collection.document(walletId)
            val doc = docRef.get().await()
            val wallet = doc.toObject(Wallet::class.java)
            if (wallet != null) {
                docRef.update("currentBalance", wallet.currentBalance + delta).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteWallet(walletId: String): Result<Unit> {
        val collection = getWalletsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(walletId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun transferBetweenWallets(transfer: WalletTransfer): Result<Unit> {
        val walletsCollection = getWalletsCollection() ?: return Result.failure(Exception("Not authenticated"))
        val transfersCollection = getTransfersCollection() ?: return Result.failure(Exception("Not authenticated"))
        val db = firestore ?: return Result.failure(Exception("Database unavailable"))

        return try {
            val transferDoc = transfersCollection.document()
            val finalTransfer = transfer.copy(id = transferDoc.id, timestamp = System.currentTimeMillis())

            db.runBatch { batch ->
                batch.set(transferDoc, finalTransfer)
            }.await()

            // Update source and target wallet balances
            val fromDoc = walletsCollection.document(transfer.fromWalletId).get().await()
            val toDoc = walletsCollection.document(transfer.toWalletId).get().await()

            val fromWallet = fromDoc.toObject(Wallet::class.java)
            val toWallet = toDoc.toObject(Wallet::class.java)

            if (fromWallet != null && toWallet != null) {
                walletsCollection.document(transfer.fromWalletId)
                    .update("currentBalance", fromWallet.currentBalance - transfer.amount).await()
                walletsCollection.document(transfer.toWalletId)
                    .update("currentBalance", toWallet.currentBalance + transfer.amount).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun seedDefaultWalletsIfEmpty() {
        val collection = getWalletsCollection() ?: return
        try {
            val count = collection.limit(1).get().await().size()
            if (count == 0) {
                val defaults = listOf(
                    Wallet("", "নগদ টাকা (Cash)", "cash", 0.0, 0.0, "", "#0B6E4F", true),
                    Wallet("", "ব্যাংক অ্যাকাউন্ট (Bank)", "bank", 0.0, 0.0, "", "#1E88E5", false),
                    Wallet("", "বিকাশ (bKash)", "bkash", 0.0, 0.0, "", "#D12053", false),
                    Wallet("", "নগদ (Nagad)", "nagad", 0.0, 0.0, "", "#F7941D", false),
                    Wallet("", "রকেট (Rocket)", "rocket", 0.0, 0.0, "", "#8C3494", false)
                )

                firestore?.runBatch { batch ->
                    for (wallet in defaults) {
                        val doc = collection.document()
                        batch.set(doc, wallet.copy(id = doc.id))
                    }
                }?.await()
            }
        } catch (e: Exception) {
            // Ignore failure on seed
        }
    }
}
