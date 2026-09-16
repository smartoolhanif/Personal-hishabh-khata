package com.example.presentation.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Wallet
import com.example.domain.model.WalletTransfer
import com.example.domain.repository.TransactionRepository
import com.example.domain.repository.WalletRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalletViewModel(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val wallets: StateFlow<List<Wallet>> = walletRepository.getWallets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBalance: StateFlow<Double> = wallets.map { list ->
        list.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    init {
        viewModelScope.launch {
            walletRepository.seedDefaultWalletsIfEmpty()
        }
    }

    fun addWallet(
        name: String,
        type: String,
        initialBalance: Double,
        accountNumber: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val wallet = Wallet(
                name = name.trim(),
                type = type,
                initialBalance = initialBalance,
                currentBalance = initialBalance,
                accountNumber = accountNumber.trim(),
                colorHex = colorHex
            )
            walletRepository.addWallet(wallet)
        }
    }

    fun updateWallet(wallet: Wallet) {
        viewModelScope.launch {
            walletRepository.updateWallet(wallet)
        }
    }

    fun deleteWallet(walletId: String) {
        viewModelScope.launch {
            walletRepository.deleteWallet(walletId)
        }
    }

    fun transferMoney(
        fromWallet: Wallet,
        toWallet: Wallet,
        amount: Double,
        note: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val transfer = WalletTransfer(
                fromWalletId = fromWallet.id,
                fromWalletName = fromWallet.name,
                toWalletId = toWallet.id,
                toWalletName = toWallet.name,
                amount = amount,
                note = note.trim(),
                date = dateStr
            )
            walletRepository.transferBetweenWallets(transfer)
        }
    }
}
