package com.example.data.firebase

import com.example.domain.model.Category
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.TransactionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirebaseTransactionRepository : TransactionRepository {
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

    private fun getTransactionsCollection() = currentUserId?.let { uid ->
        firestore?.collection("users")?.document(uid)?.collection("transactions")
    }

    private fun getCategoriesCollection() = currentUserId?.let { uid ->
        firestore?.collection("users")?.document(uid)?.collection("categories")
    }

    override fun getTransactions(): Flow<List<Transaction>> {
        val collection = getTransactionsCollection() ?: return emptyFlow()
        return collection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .sortedByDescending { it.timestamp.takeIf { t -> t > 0L } ?: it.createdAt }
            }
    }

    override fun getTransactionsByDate(date: String): Flow<List<Transaction>> {
        val collection = getTransactionsCollection() ?: return emptyFlow()
        return collection.whereEqualTo("date", date)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .sortedByDescending { it.timestamp.takeIf { t -> t > 0L } ?: it.createdAt }
            }
    }

    override fun getTransactionsByMonth(monthYear: String): Flow<List<Transaction>> {
        val collection = getTransactionsCollection() ?: return emptyFlow()
        return collection
            .snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .filter { it.date.startsWith(monthYear) }
                    .sortedByDescending { it.timestamp.takeIf { t -> t > 0L } ?: it.createdAt }
            }
    }

    override suspend fun getTransactionById(transactionId: String): Transaction? {
        val collection = getTransactionsCollection() ?: return null
        return try {
            collection.document(transactionId).get().await().toObject(Transaction::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        val collection = getTransactionsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docRef = collection.document()
            val newTransaction = transaction.copy(id = docRef.id)
            docRef.set(newTransaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        val collection = getTransactionsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(transaction.id).set(transaction).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        val collection = getTransactionsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(transactionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreTransaction(transaction: Transaction): Result<Unit> {
        val collection = getTransactionsCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docId = if (transaction.id.isNotBlank()) transaction.id else collection.document().id
            collection.document(docId).set(transaction.copy(id = docId)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getCategories(type: TransactionType?): Flow<List<Category>> {
        val collection = getCategoriesCollection() ?: return emptyFlow()
        val query = if (type != null) {
            collection.whereEqualTo("type", type.name)
        } else {
            collection
        }
        return query.snapshots().map { snapshot ->
            snapshot.documents.mapNotNull { it.toObject(Category::class.java) }
        }
    }

    override suspend fun addCategory(category: Category): Result<Unit> {
        val collection = getCategoriesCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val docRef = collection.document()
            val newCategory = category.copy(id = docRef.id)
            docRef.set(newCategory).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCategory(category: Category): Result<Unit> {
        val collection = getCategoriesCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(category.id).set(category).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCategory(categoryId: String): Result<Unit> {
        val collection = getCategoriesCollection() ?: return Result.failure(Exception("Not authenticated"))
        return try {
            collection.document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun seedDefaultCategoriesIfEmpty() {
        val collection = getCategoriesCollection() ?: return
        try {
            val count = collection.limit(1).get().await().size()
            if (count == 0) {
                val defaults = listOf(
                    Category("", "খাবার", TransactionType.EXPENSE, "restaurant"),
                    Category("", "বাজার", TransactionType.EXPENSE, "shopping_cart"),
                    Category("", "বাসা", TransactionType.EXPENSE, "home"),
                    Category("", "বিদ্যুৎ", TransactionType.EXPENSE, "bolt"),
                    Category("", "পানি", TransactionType.EXPENSE, "water_drop"),
                    Category("", "WiFi/Internet", TransactionType.EXPENSE, "wifi"),
                    Category("", "মোবাইল", TransactionType.EXPENSE, "smartphone"),
                    Category("", "যাতায়াত", TransactionType.EXPENSE, "directions_bus"),
                    Category("", "চিকিৎসা", TransactionType.EXPENSE, "medical_services"),
                    Category("", "কেনাকাটা", TransactionType.EXPENSE, "shopping_bag"),
                    Category("", "পরিবার", TransactionType.EXPENSE, "family_restroom"),
                    Category("", "অন্যান্য", TransactionType.EXPENSE, "category"),
                    
                    Category("", "বেতন", TransactionType.INCOME, "payments"),
                    Category("", "ব্যবসা", TransactionType.INCOME, "store"),
                    Category("", "ফ্রিল্যান্স", TransactionType.INCOME, "laptop_mac"),
                    Category("", "উপহার", TransactionType.INCOME, "redeem"),
                    Category("", "অন্যান্য", TransactionType.INCOME, "category")
                )
                
                firestore?.runBatch { batch ->
                    for (cat in defaults) {
                        val doc = collection.document()
                        batch.set(doc, cat.copy(id = doc.id))
                    }
                }?.await()
            }
        } catch (e: Exception) {
            // Ignore failure on seed
        }
    }
}
