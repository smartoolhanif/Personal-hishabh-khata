package com.example.presentation.screens.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.RecurringItem
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.RecurringRepository
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecurringViewModel(
    private val recurringRepository: RecurringRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val recurringItems: StateFlow<List<RecurringItem>> = recurringRepository.getRecurringItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecurringItem(
        title: String,
        amount: Double,
        type: TransactionType,
        categoryName: String,
        frequency: String,
        dayOfMonth: Int
    ) {
        viewModelScope.launch {
            val item = RecurringItem(
                title = title.trim(),
                amount = amount,
                type = type,
                categoryName = categoryName.trim(),
                frequency = frequency,
                dayOfMonth = dayOfMonth,
                isActive = true
            )
            recurringRepository.addRecurringItem(item)
        }
    }

    fun deleteRecurringItem(itemId: String) {
        viewModelScope.launch {
            recurringRepository.deleteRecurringItem(itemId)
        }
    }

    fun markAsPaid(item: RecurringItem) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val tx = Transaction(
                title = item.title,
                amount = item.amount,
                type = item.type,
                categoryName = item.categoryName,
                date = dateStr,
                note = "রিকারিং বিল পরিশোধ (${item.frequency})",
                isRecurring = true,
                recurringFrequency = item.frequency
            )
            transactionRepository.addTransaction(tx)
            recurringRepository.updateRecurringItem(item.copy(lastGeneratedDate = dateStr))
        }
    }
}
