package com.example.presentation.screens.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DailyViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private val _currentDate = MutableStateFlow(Date())
    val currentDate: StateFlow<Date> = _currentDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _currentDate
        .flatMapLatest { date ->
            val dateStr = dateFormat.format(date)
            transactionRepository.getTransactionsByDate(dateStr)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalExpense: StateFlow<Double> = transactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val totalIncome: StateFlow<Double> = transactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    val balance: StateFlow<Double> = totalIncome.combine(totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0.0)

    fun previousDay() {
        val cal = Calendar.getInstance().apply {
            time = _currentDate.value
            add(Calendar.DAY_OF_YEAR, -1)
        }
        _currentDate.value = cal.time
    }

    fun nextDay() {
        val cal = Calendar.getInstance().apply {
            time = _currentDate.value
            add(Calendar.DAY_OF_YEAR, 1)
        }
        _currentDate.value = cal.time
    }

    fun setDate(date: Date) {
        _currentDate.value = date
    }

    fun goToToday() {
        _currentDate.value = Date()
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }
}
