package com.example.presentation.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.BudgetGoal
import com.example.domain.model.Category
import com.example.domain.model.TransactionType
import com.example.domain.repository.BudgetRepository
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CategoryBudgetDisplay(
    val categoryId: String,
    val categoryName: String,
    val targetAmount: Double,
    val spentAmount: Double,
    val budgetId: String = ""
) {
    val progress: Float
        get() = if (targetAmount > 0) (spentAmount / targetAmount).toFloat().coerceIn(0f, 1f) else 0f

    val isOverBudget: Boolean
        get() = spentAmount > targetAmount && targetAmount > 0

    val percent: Int
        get() = if (targetAmount > 0) ((spentAmount / targetAmount) * 100).toInt() else 0
}

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val currentCalendar = Calendar.getInstance()
    private val monthYearFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private val _currentMonthYear = MutableStateFlow(monthYearFormat.format(currentCalendar.time))
    val currentMonthYear = _currentMonthYear.asStateFlow()

    val categories: StateFlow<List<Category>> = transactionRepository.getCategories(TransactionType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rawBudgets: StateFlow<List<BudgetGoal>> = _currentMonthYear
        .flatMapLatest { ym -> budgetRepository.getBudgets(ym) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val monthlyTransactions = _currentMonthYear
        .flatMapLatest { ym -> transactionRepository.getTransactionsByMonth(ym) }

    val budgetDisplays: StateFlow<List<CategoryBudgetDisplay>> = combine(
        categories,
        rawBudgets,
        monthlyTransactions
    ) { cats, budgets, txs ->
        val expenseTxs = txs.filter { it.type == TransactionType.EXPENSE }
        val spentMap = expenseTxs.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val spentByNameMap = expenseTxs.groupBy { it.categoryName.lowercase() }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        cats.map { cat ->
            val budget = budgets.firstOrNull { it.categoryId == cat.id || it.categoryName.equals(cat.name, ignoreCase = true) }
            val spent = spentMap[cat.id] ?: spentByNameMap[cat.name.lowercase()] ?: 0.0
            CategoryBudgetDisplay(
                categoryId = cat.id,
                categoryName = cat.name,
                targetAmount = budget?.targetAmount ?: 0.0,
                spentAmount = spent,
                budgetId = budget?.id ?: ""
            )
        }.sortedByDescending { it.targetAmount > 0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBudget: StateFlow<Double> = budgetDisplays.map { list ->
        list.sumOf { it.targetAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalSpent: StateFlow<Double> = budgetDisplays.map { list ->
        list.sumOf { it.spentAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun setBudget(categoryId: String, categoryName: String, targetAmount: Double) {
        viewModelScope.launch {
            val goal = BudgetGoal(
                categoryId = categoryId,
                categoryName = categoryName,
                monthYear = _currentMonthYear.value,
                targetAmount = targetAmount
            )
            budgetRepository.setBudget(goal)
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            if (budgetId.isNotBlank()) {
                budgetRepository.deleteBudget(budgetId)
            }
        }
    }
}
