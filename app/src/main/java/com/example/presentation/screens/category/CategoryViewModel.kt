package com.example.presentation.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Category
import com.example.domain.model.TransactionType
import com.example.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val allCategories: StateFlow<List<Category>> = transactionRepository.getCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(TransactionType.EXPENSE)
    val selectedTab: StateFlow<TransactionType> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCategories: StateFlow<List<Category>> = combine(
        allCategories,
        _selectedTab,
        _searchQuery
    ) { categories, tab, query ->
        categories
            .filter { it.type == tab }
            .filter { if (query.isBlank()) true else it.name.contains(query, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(type: TransactionType) {
        _selectedTab.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addCategory(name: String, type: TransactionType, iconName: String = "category") {
        if (name.isBlank()) return
        viewModelScope.launch {
            transactionRepository.addCategory(
                Category(
                    name = name.trim(),
                    type = type,
                    iconName = iconName
                )
            )
        }
    }

    fun updateCategory(category: Category, newName: String, iconName: String = category.iconName) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            transactionRepository.updateCategory(
                category.copy(name = newName.trim(), iconName = iconName)
            )
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            transactionRepository.deleteCategory(categoryId)
        }
    }

    fun seedDefaults() {
        viewModelScope.launch {
            transactionRepository.seedDefaultCategoriesIfEmpty()
        }
    }
}
