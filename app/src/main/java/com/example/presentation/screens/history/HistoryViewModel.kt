package com.example.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class DateFilterOption(val labelBn: String, val labelEn: String) {
    ALL("সব", "All"),
    TODAY("আজ", "Today"),
    YESTERDAY("গতকাল", "Yesterday"),
    THIS_WEEK("এই সপ্তাহ", "This Week"),
    THIS_MONTH("এই মাস", "This Month"),
    PREVIOUS_MONTH("গত মাস", "Prev Month"),
    THIS_YEAR("এই বছর", "This Year"),
    CUSTOM_RANGE("কাস্টম", "Custom Range")
}

data class FilterSummary(
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val balance: Double = 0.0,
    val count: Int = 0
)

data class HistoryFilter(
    val query: String = "",
    val type: TransactionType? = null,
    val dateOption: DateFilterOption = DateFilterOption.ALL,
    val customStart: Date? = null,
    val customEnd: Date? = null
)

class HistoryViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(HistoryFilter())
    val filter: StateFlow<HistoryFilter> = _filter.asStateFlow()

    val searchQuery: StateFlow<String> = _filter.map { it.query }
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val filterType: StateFlow<TransactionType?> = _filter.map { it.type }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val dateFilter: StateFlow<DateFilterOption> = _filter.map { it.dateOption }
        .stateIn(viewModelScope, SharingStarted.Lazily, DateFilterOption.ALL)

    val customStartDate: StateFlow<Date?> = _filter.map { it.customStart }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val customEndDate: StateFlow<Date?> = _filter.map { it.customEnd }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allTransactions: StateFlow<List<Transaction>> = transactionRepository.getTransactions()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        allTransactions,
        _filter
    ) { txList: List<Transaction>, currentFilter: HistoryFilter ->
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Date())

        val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val yesterdayStr = dateFormat.format(yesterdayCal.time)

        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val thisMonthStr = monthFormat.format(Date())

        val prevMonthCal = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }
        val prevMonthStr = monthFormat.format(prevMonthCal.time)

        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val thisYearStr = yearFormat.format(Date())

        // Calculate start of current week
        val weekCal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfWeekTimestamp = weekCal.timeInMillis

        txList.filter { tx ->
            // 1. Text Search Filter
            val matchesQuery = if (currentFilter.query.isBlank()) true else {
                tx.title.contains(currentFilter.query, ignoreCase = true) ||
                tx.note.contains(currentFilter.query, ignoreCase = true) ||
                tx.categoryName.contains(currentFilter.query, ignoreCase = true)
            }

            // 2. Type Filter
            val matchesType = if (currentFilter.type == null) true else tx.type == currentFilter.type

            // 3. Date Filter
            val matchesDate = when (currentFilter.dateOption) {
                DateFilterOption.ALL -> true
                DateFilterOption.TODAY -> tx.date == todayStr
                DateFilterOption.YESTERDAY -> tx.date == yesterdayStr
                DateFilterOption.THIS_WEEK -> {
                    val txTime = if (tx.timestamp > 0L) tx.timestamp else tx.createdAt
                    txTime >= startOfWeekTimestamp
                }
                DateFilterOption.THIS_MONTH -> tx.date.startsWith(thisMonthStr)
                DateFilterOption.PREVIOUS_MONTH -> tx.date.startsWith(prevMonthStr)
                DateFilterOption.THIS_YEAR -> tx.date.startsWith(thisYearStr)
                DateFilterOption.CUSTOM_RANGE -> {
                    val startStr = currentFilter.customStart?.let { dateFormat.format(it) }
                    val endStr = currentFilter.customEnd?.let { dateFormat.format(it) }
                    if (startStr != null && endStr != null) {
                        tx.date in startStr..endStr
                    } else if (startStr != null) {
                        tx.date >= startStr
                    } else if (endStr != null) {
                        tx.date <= endStr
                    } else {
                        true
                    }
                }
            }

            matchesQuery && matchesType && matchesDate
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filterSummary: StateFlow<FilterSummary> = filteredTransactions.combine(MutableStateFlow(Unit)) { txs, _ ->
        val income = txs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = txs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        FilterSummary(
            income = income,
            expense = expense,
            balance = income - expense,
            count = txs.size
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, FilterSummary())

    fun setSearchQuery(query: String) {
        _filter.value = _filter.value.copy(query = query)
    }

    fun setFilterType(type: TransactionType?) {
        _filter.value = _filter.value.copy(type = type)
    }

    fun setDateFilter(option: DateFilterOption) {
        _filter.value = _filter.value.copy(dateOption = option)
    }

    fun setCustomDateRange(start: Date?, end: Date?) {
        _filter.value = _filter.value.copy(
            customStart = start,
            customEnd = end,
            dateOption = DateFilterOption.CUSTOM_RANGE
        )
    }

    fun clearFilters() {
        _filter.value = HistoryFilter()
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
        }
    }
}
