package com.example.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.model.UserProfile
import com.example.domain.repository.AuthRepository
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SummaryPeriod(val labelBn: String, val labelEn: String) {
    TODAY("আজকে", "Today"),
    THIS_MONTH("এই মাস", "This Month"),
    PREVIOUS_MONTH("গত মাস", "Previous Month"),
    THIS_YEAR("এই বছর", "This Year")
}

data class PeriodSummary(
    val period: SummaryPeriod,
    val title: String,
    val subTitle: String,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,
    val transactionCount: Int = 0
)

class HomeViewModel(
    private val transactionRepository: TransactionRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Live Clock with seconds, detecting local device timezone and locale
    private val _liveDate = MutableStateFlow(formatCurrentDate())
    val liveDate: StateFlow<String> = _liveDate.asStateFlow()

    private val _liveTime = MutableStateFlow(formatCurrentTime())
    val liveTime: StateFlow<String> = _liveTime.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(SummaryPeriod.TODAY)
    val selectedPeriod: StateFlow<SummaryPeriod> = _selectedPeriod.asStateFlow()

    // All transactions from repository (chronologically descending)
    val allTransactions: StateFlow<List<Transaction>> = transactionRepository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Recent transactions for the dashboard preview (latest 5 across all dates)
    val recentTransactions: StateFlow<List<Transaction>> = allTransactions
        .combine(MutableStateFlow(Unit)) { txs, _ -> txs.take(5) }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Compute summaries reactively from allTransactions
    val todaySummary: StateFlow<PeriodSummary> = allTransactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val filtered = txs.filter { it.date == todayStr }
        computeSummary(SummaryPeriod.TODAY, "Today's Summary", "আজকের হিসাব", filtered)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PeriodSummary(SummaryPeriod.TODAY, "Today", "আজকের হিসাব"))

    val thisMonthSummary: StateFlow<PeriodSummary> = allTransactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        val monthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
        val filtered = txs.filter { it.date.startsWith(monthStr) }
        computeSummary(SummaryPeriod.THIS_MONTH, "This Month's Summary", "এই মাসের হিসাব", filtered)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PeriodSummary(SummaryPeriod.THIS_MONTH, "This Month", "এই মাসের হিসাব"))

    val previousMonthSummary: StateFlow<PeriodSummary> = allTransactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val prevMonthStr = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(cal.time)
        val filtered = txs.filter { it.date.startsWith(prevMonthStr) }
        computeSummary(SummaryPeriod.PREVIOUS_MONTH, "Previous Month's Summary", "গত মাসের হিসাব", filtered)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PeriodSummary(SummaryPeriod.PREVIOUS_MONTH, "Previous Month", "গত মাসের হিসাব"))

    val thisYearSummary: StateFlow<PeriodSummary> = allTransactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        val yearStr = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val filtered = txs.filter { it.date.startsWith(yearStr) }
        computeSummary(SummaryPeriod.THIS_YEAR, "This Year's Summary", "এই বছরের হিসাব", filtered)
    }.stateIn(viewModelScope, SharingStarted.Lazily, PeriodSummary(SummaryPeriod.THIS_YEAR, "This Year", "এই বছরের হিসাব"))

    // Active summary matching the selected period tab
    val activeSummary: StateFlow<PeriodSummary> = combine(
        _selectedPeriod,
        todaySummary,
        thisMonthSummary,
        previousMonthSummary,
        thisYearSummary
    ) { period, today, month, prevMonth, year ->
        when (period) {
            SummaryPeriod.TODAY -> today
            SummaryPeriod.THIS_MONTH -> month
            SummaryPeriod.PREVIOUS_MONTH -> prevMonth
            SummaryPeriod.THIS_YEAR -> year
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, PeriodSummary(SummaryPeriod.TODAY, "Today", "আজকের হিসাব"))

    init {
        // Seed initial categories if empty
        viewModelScope.launch {
            transactionRepository.seedDefaultCategoriesIfEmpty()
        }

        // Live clock ticker updating every second
        viewModelScope.launch {
            while (isActive) {
                _liveDate.value = formatCurrentDate()
                _liveTime.value = formatCurrentTime()
                delay(1000)
            }
        }
    }

    fun selectPeriod(period: SummaryPeriod) {
        _selectedPeriod.value = period
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }

    private fun computeSummary(
        period: SummaryPeriod,
        titleEn: String,
        subTitleBn: String,
        txs: List<Transaction>
    ): PeriodSummary {
        val income = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        return PeriodSummary(
            period = period,
            title = titleEn,
            subTitle = subTitleBn,
            income = income,
            expense = expense,
            balance = income - expense,
            transactionCount = txs.size
        )
    }

    private fun formatCurrentDate(): String {
        return SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
    }

    private fun formatCurrentTime(): String {
        return SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
    }
}
