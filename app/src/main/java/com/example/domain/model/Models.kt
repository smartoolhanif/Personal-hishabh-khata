package com.example.domain.model

import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE
}

data class Category(
    val id: String = "",
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val iconName: String = "" // E.g. material icon name
)

data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: String = "",
    val categoryName: String = "",
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val date: String = "", // E.g. yyyy-MM-dd for easy grouping
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null
)
