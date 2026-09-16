package com.example.domain.repository

import com.example.domain.model.BudgetGoal
import com.example.domain.model.Category
import com.example.domain.model.KhataEntry
import com.example.domain.model.KhataParty
import com.example.domain.model.RecurringItem
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.UserProfile
import com.example.domain.model.Wallet
import com.example.domain.model.WalletTransfer
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
    suspend fun restoreTransaction(transaction: Transaction): Result<Unit>
    
    fun getCategories(type: TransactionType? = null): Flow<List<Category>>
    suspend fun addCategory(category: Category): Result<Unit>
    suspend fun updateCategory(category: Category): Result<Unit>
    suspend fun deleteCategory(categoryId: String): Result<Unit>
    suspend fun seedDefaultCategoriesIfEmpty()
}

interface KhataRepository {
    fun getParties(): Flow<List<KhataParty>>
    suspend fun getPartyById(id: String): KhataParty?
    suspend fun addParty(party: KhataParty): Result<Unit>
    suspend fun updateParty(party: KhataParty): Result<Unit>
    suspend fun deleteParty(partyId: String): Result<Unit>
    fun getEntriesForParty(partyId: String): Flow<List<KhataEntry>>
    suspend fun addEntry(entry: KhataEntry): Result<Unit>
    suspend fun deleteEntry(entryId: String, partyId: String): Result<Unit>
}

interface WalletRepository {
    fun getWallets(): Flow<List<Wallet>>
    suspend fun addWallet(wallet: Wallet): Result<Unit>
    suspend fun updateWallet(wallet: Wallet): Result<Unit>
    suspend fun updateBalance(walletId: String, delta: Double): Result<Unit>
    suspend fun deleteWallet(walletId: String): Result<Unit>
    suspend fun transferBetweenWallets(transfer: WalletTransfer): Result<Unit>
    suspend fun seedDefaultWalletsIfEmpty()
}

interface BudgetRepository {
    fun getBudgets(monthYear: String): Flow<List<BudgetGoal>>
    suspend fun setBudget(budget: BudgetGoal): Result<Unit>
    suspend fun deleteBudget(budgetId: String): Result<Unit>
}

interface RecurringRepository {
    fun getRecurringItems(): Flow<List<RecurringItem>>
    suspend fun addRecurringItem(item: RecurringItem): Result<Unit>
    suspend fun updateRecurringItem(item: RecurringItem): Result<Unit>
    suspend fun deleteRecurringItem(itemId: String): Result<Unit>
}
