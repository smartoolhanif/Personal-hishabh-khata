package com.example.domain.repository

import com.example.domain.model.Category
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<UserProfile?>
    suspend fun signInWithGoogle(idToken: String): Result<UserProfile>
    suspend fun signInAnonymously(): Result<UserProfile>
    suspend fun updateDisplayName(name: String): Result<Unit>
    suspend fun signOut()
}

interface TransactionRepository {
    fun getTransactions(): Flow<List<Transaction>>
    fun getTransactionsByDate(date: String): Flow<List<Transaction>>
    fun getTransactionsByMonth(monthYear: String): Flow<List<Transaction>> // Format: yyyy-MM
    suspend fun getTransactionById(transactionId: String): Transaction?
    suspend fun addTransaction(transaction: Transaction): Result<Unit>
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    suspend fun deleteTransaction(transactionId: String): Result<Unit>
    
    fun getCategories(type: TransactionType? = null): Flow<List<Category>>
    suspend fun addCategory(category: Category): Result<Unit>
    suspend fun seedDefaultCategoriesIfEmpty()
}
