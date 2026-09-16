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
    val updatedAt: Long = System.currentTimeMillis(),
    val walletId: String = "cash",
    val walletName: String = "নগদ (Cash)",
    val receiptImageUri: String? = null,
    val isRecurring: Boolean = false,
    val recurringFrequency: String = ""
)

enum class KhataType {
    RECEIVABLE, // পাওনা (আমি পাবো - Customer owes me)
    PAYABLE     // দেনা (আমি দেবো - I owe Supplier/Person)
}

data class KhataParty(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val type: KhataType = KhataType.RECEIVABLE,
    val totalAmount: Double = 0.0,
    val note: String = "",
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

data class KhataEntry(
    val id: String = "",
    val partyId: String = "",
    val partyName: String = "",
    val amount: Double = 0.0,
    val isPayment: Boolean = false, // True if payment received/made; False if new debt/credit given/taken
    val note: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Wallet(
    val id: String = "",
    val name: String = "",
    val type: String = "cash", // cash, bank, bkash, nagad, rocket, other
    val initialBalance: Double = 0.0,
    val currentBalance: Double = 0.0,
    val accountNumber: String = "",
    val colorHex: String = "#0B6E4F",
    val isDefault: Boolean = false
)

data class WalletTransfer(
    val id: String = "",
    val fromWalletId: String = "",
    val fromWalletName: String = "",
    val toWalletId: String = "",
    val toWalletName: String = "",
    val amount: Double = 0.0,
    val note: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class BudgetGoal(
    val id: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val monthYear: String = "", // Format: yyyy-MM
    val targetAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val notifyThreshold: Double = 0.8
)

data class RecurringItem(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryName: String = "",
    val frequency: String = "MONTHLY", // DAILY, WEEKLY, MONTHLY
    val dueDayOfMonth: Int = 1,
    val dayOfMonth: Int = 1,
    val nextDueDate: String = "",
    val lastGeneratedDate: String = "",
    val isAutoRecord: Boolean = false,
    val isPaidThisCycle: Boolean = false,
    val isActive: Boolean = true,
    val note: String = "",
    val walletName: String = "নগদ (Cash)"
)

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null
)
