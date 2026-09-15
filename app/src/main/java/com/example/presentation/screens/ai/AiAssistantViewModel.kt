package com.example.presentation.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiService
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AiAssistantViewModel(
    private val transactionRepository: TransactionRepository,
    private val geminiService: GeminiService
) : ViewModel() {

    private val _response = MutableStateFlow<String?>(null)
    val response = _response.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun askQuestion(question: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get some recent transactions to provide context
                val recentTxs = transactionRepository.getTransactions().first().take(50)
                val totalIncome = recentTxs.filter { it.type == com.example.domain.model.TransactionType.INCOME }.sumOf { it.amount }
                val totalExpense = recentTxs.filter { it.type == com.example.domain.model.TransactionType.EXPENSE }.sumOf { it.amount }
                
                val summary = """
                    Total Income: $totalIncome
                    Total Expense: $totalExpense
                    Balance: ${totalIncome - totalExpense}
                    Recent Transactions:
                    ${recentTxs.take(10).joinToString("\n") { "${it.date} - ${it.title}: ${it.amount} (${it.type.name})" }}
                """.trimIndent()
                
                val result = geminiService.analyzeFinances(summary, question)
                _response.value = result
            } catch (e: Exception) {
                _response.value = "Error: ${e.message}"
            }
            _isLoading.value = false
        }
    }
}
