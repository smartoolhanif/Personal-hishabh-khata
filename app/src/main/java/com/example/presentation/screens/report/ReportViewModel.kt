package com.example.presentation.screens.report

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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategoryExpenseItem(
    val categoryName: String,
    val amount: Double,
    val percentage: Float
)

data class MonthTrendItem(
    val monthKey: String, // yyyy-MM
    val label: String,    // e.g. "Sep"
    val income: Double,
    val expense: Double
)

class ReportViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private val _currentMonth = MutableStateFlow(Date())
    val currentMonth: StateFlow<Date> = _currentMonth.asStateFlow()

    val allTransactions: StateFlow<List<Transaction>> = transactionRepository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: StateFlow<List<Transaction>> = _currentMonth
        .flatMapLatest { date ->
            val monthStr = monthFormat.format(date)
            transactionRepository.getTransactionsByMonth(monthStr)
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

    val categoryBreakdown: StateFlow<List<CategoryExpenseItem>> = combine(
        transactions,
        totalExpense
    ) { txs, totalExp ->
        if (totalExp <= 0.0) {
            emptyList()
        } else {
            txs.filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.categoryName }
                .map { (cat, list) ->
                    val sum = list.sumOf { it.amount }
                    val pct = ((sum / totalExp) * 100).toFloat()
                    CategoryExpenseItem(cat, sum, pct)
                }
                .sortedByDescending { it.amount }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 6-Month Trend comparison
    val monthlyTrends: StateFlow<List<MonthTrendItem>> = allTransactions.combine(MutableStateFlow(Unit)) { allTxs, _ ->
        val list = mutableListOf<MonthTrendItem>()
        val cal = Calendar.getInstance()
        val labelFormat = SimpleDateFormat("MMM", Locale.getDefault())

        for (i in 5 downTo 0) {
            val monthCal = Calendar.getInstance().apply {
                time = cal.time
                add(Calendar.MONTH, -i)
            }
            val key = monthFormat.format(monthCal.time)
            val label = labelFormat.format(monthCal.time)
            val monthTxs = allTxs.filter { it.date.startsWith(key) }
            val inc = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val exp = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            list.add(MonthTrendItem(key, label, inc, exp))
        }
        list
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun previousMonth() {
        val cal = Calendar.getInstance().apply {
            time = _currentMonth.value
            add(Calendar.MONTH, -1)
        }
        _currentMonth.value = cal.time
    }

    fun nextMonth() {
        val cal = Calendar.getInstance().apply {
            time = _currentMonth.value
            add(Calendar.MONTH, 1)
        }
        _currentMonth.value = cal.time
    }

    fun setMonth(date: Date) {
        _currentMonth.value = date
    }

    fun goToCurrentMonth() {
        _currentMonth.value = Date()
    }
}
