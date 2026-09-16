package com.example.presentation.screens.khata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.KhataEntry
import com.example.domain.model.KhataParty
import com.example.domain.model.KhataType
import com.example.domain.repository.KhataRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class KhataViewModel(
    private val khataRepository: KhataRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow<KhataType?>(null)
    val typeFilter = _typeFilter.asStateFlow()

    private val _selectedParty = MutableStateFlow<KhataParty?>(null)
    val selectedParty = _selectedParty.asStateFlow()

    val rawParties: StateFlow<List<KhataParty>> = khataRepository.getParties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredParties: StateFlow<List<KhataParty>> = combine(
        rawParties,
        _searchQuery,
        _typeFilter
    ) { list, query, type ->
        list.filter { party ->
            val matchesQuery = query.isBlank() ||
                    party.name.contains(query, ignoreCase = true) ||
                    party.phone.contains(query) ||
                    party.note.contains(query, ignoreCase = true)
            val matchesType = type == null || party.type == type
            matchesQuery && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalReceivable: StateFlow<Double> = rawParties.map { list ->
        list.filter { it.type == KhataType.RECEIVABLE }.sumOf { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalPayable: StateFlow<Double> = rawParties.map { list ->
        list.filter { it.type == KhataType.PAYABLE }.sumOf { it.totalAmount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val selectedPartyEntries: StateFlow<List<KhataEntry>> = _selectedParty
        .flatMapLatest { party ->
            if (party != null) khataRepository.getEntriesForParty(party.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: KhataType?) {
        _typeFilter.value = type
    }

    fun selectParty(party: KhataParty?) {
        _selectedParty.value = party
    }

    fun addParty(
        name: String,
        phone: String,
        address: String,
        type: KhataType,
        initialAmount: Double,
        note: String
    ) {
        viewModelScope.launch {
            val party = KhataParty(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                type = type,
                totalAmount = initialAmount,
                note = note.trim()
            )
            khataRepository.addParty(party)
        }
    }

    fun deleteParty(partyId: String) {
        viewModelScope.launch {
            khataRepository.deleteParty(partyId)
            if (_selectedParty.value?.id == partyId) {
                _selectedParty.value = null
            }
        }
    }

    fun addEntry(
        party: KhataParty,
        amount: Double,
        isPayment: Boolean,
        note: String
    ) {
        viewModelScope.launch {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val entry = KhataEntry(
                partyId = party.id,
                partyName = party.name,
                amount = amount,
                isPayment = isPayment,
                note = note.trim(),
                date = dateStr
            )
            khataRepository.addEntry(entry)
            // refresh selected party reference
            val updated = khataRepository.getPartyById(party.id)
            if (updated != null) {
                _selectedParty.value = updated
            }
        }
    }

    fun deleteEntry(entryId: String, partyId: String) {
        viewModelScope.launch {
            khataRepository.deleteEntry(entryId, partyId)
            val updated = khataRepository.getPartyById(partyId)
            if (updated != null) {
                _selectedParty.value = updated
            }
        }
    }
}
