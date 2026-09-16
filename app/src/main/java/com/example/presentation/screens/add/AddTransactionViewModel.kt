package com.example.presentation.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Category
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.Wallet
import com.example.domain.repository.TransactionRepository
import com.example.domain.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddTransactionViewModel(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository? = null
) : ViewModel() {

    private val _type = MutableStateFlow(TransactionType.EXPENSE)
    val type = _type.asStateFlow()
    
    val categories: StateFlow<List<Category>> = transactionRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val wallets: StateFlow<List<Wallet>> = (walletRepository?.getWallets() ?: kotlinx.coroutines.flow.emptyFlow())
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess = _saveSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _loadedTransaction = MutableStateFlow<Transaction?>(null)
    val loadedTransaction = _loadedTransaction.asStateFlow()

    private val _isLoadingTransaction = MutableStateFlow(false)
    val isLoadingTransaction = _isLoadingTransaction.asStateFlow()
    
    private var editingTransactionId: String? = null
    private var createdAt: Long = System.currentTimeMillis()

    fun setType(newType: TransactionType) {
        _type.value = newType
    }
    
    fun loadTransaction(transaction: Transaction) {
        editingTransactionId = transaction.id
        _type.value = transaction.type
        createdAt = transaction.createdAt
        _loadedTransaction.value = transaction
    }

    fun loadTransactionById(transactionId: String) {
        viewModelScope.launch {
            _isLoadingTransaction.value = true
            val transaction = transactionRepository.getTransactionById(transactionId)
            if (transaction != null) {
                editingTransactionId = transaction.id
                _type.value = transaction.type
                createdAt = transaction.createdAt
                _loadedTransaction.value = transaction
            } else {
                _error.value = "Transaction not found"
            }
            _isLoadingTransaction.value = false
        }
    }

    fun saveTransaction(
        title: String,
        amountStr: String,
        category: Category?,
        note: String,
        date: Date,
        wallet: Wallet? = null,
        receiptImageUri: String? = null,
        isRecurring: Boolean = false,
        recurringFrequency: String = ""
    ) {
        val amount = amountStr.toDoubleOrNull()
        if (title.isBlank()) {
            _error.value = "Title cannot be empty"
            return
        }
        if (amount == null || amount <= 0) {
            _error.value = "Invalid amount"
            return
        }
        if (category == null) {
            _error.value = "Please select a category"
            return
        }

        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        val transaction = Transaction(
            id = editingTransactionId ?: "",
            title = title,
            amount = amount,
            type = _type.value,
            categoryId = category.id,
            categoryName = category.name,
            walletId = wallet?.id ?: "",
            walletName = wallet?.name ?: "",
            receiptImageUri = receiptImageUri,
            isRecurring = isRecurring,
            recurringFrequency = recurringFrequency,
            note = note,
            timestamp = date.time,
            date = dateStr,
            createdAt = createdAt,
            updatedAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            
            val result = if (editingTransactionId != null) {
                transactionRepository.updateTransaction(transaction)
            } else {
                val addRes = transactionRepository.addTransaction(transaction)
                if (addRes.isSuccess && wallet != null && walletRepository != null) {
                    val balanceDelta = if (_type.value == TransactionType.INCOME) amount else -amount
                    walletRepository.updateBalance(wallet.id, balanceDelta)
                }
                addRes
            }
            
            if (result.isSuccess) {
                _saveSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to save"
            }
            _isSaving.value = false
        }
    }
    
    fun resetSuccess() {
        _saveSuccess.value = false
    }

    fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch {
            transactionRepository.addCategory(
                Category(name = name.trim(), type = type, iconName = "category")
            )
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            transactionRepository.updateCategory(category)
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            transactionRepository.deleteCategory(categoryId)
        }
    }
}
