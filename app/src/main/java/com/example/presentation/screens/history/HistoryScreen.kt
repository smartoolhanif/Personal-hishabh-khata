package com.example.presentation.screens.history

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.TransactionType
import com.example.presentation.components.TransactionItem
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateToEdit: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val transactions by viewModel.filteredTransactions.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val filterCategory by viewModel.filterCategory.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val customStart by viewModel.customStartDate.collectAsState()
    val customEnd by viewModel.customEndDate.collectAsState()
    val filterSummary by viewModel.filterSummary.collectAsState()

    val displayDateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val yesterdayStr = remember {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.time)
    }

    // Group transactions by date
    val groupedTransactions = remember(transactions) {
        transactions.groupBy { it.date }
    }

    // Date picker dialog helpers
    val showStartDatePicker = {
        val cal = Calendar.getInstance()
        customStart?.let { cal.time = it }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val chosen = Calendar.getInstance().apply {
                    set(year, month, day)
                }.time
                viewModel.setCustomDateRange(chosen, customEnd)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val showEndDatePicker = {
        val cal = Calendar.getInstance()
        customEnd?.let { cal.time = it }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val chosen = Calendar.getInstance().apply {
                    set(year, month, day)
                }.time
                viewModel.setCustomDateRange(customStart, chosen)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "হিসাবের ইতিহাস (History)",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input Field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("নাম, ক্যাটেগরি বা বিবরণ দিয়ে খুঁজুন...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Horizontal Date Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateFilterOption.values().forEach { option ->
                    val isSelected = dateFilter == option
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.setDateFilter(option)
                            if (option == DateFilterOption.CUSTOM_RANGE && customStart == null) {
                                showStartDatePicker()
                            }
                        },
                        label = {
                            Text(
                                text = "${option.labelBn} (${option.labelEn})",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (option == DateFilterOption.CUSTOM_RANGE) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Custom Date Range Selector Row (visible when CUSTOM_RANGE is chosen)
            AnimatedVisibility(visible = dateFilter == DateFilterOption.CUSTOM_RANGE) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // From Date
                        OutlinedButton(
                            onClick = showStartDatePicker,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = customStart?.let { displayDateFormat.format(it) } ?: "হতে (From)",
                                fontSize = 12.sp
                            )
                        }

                        Text("→", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        // To Date
                        OutlinedButton(
                            onClick = showEndDatePicker,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = customEnd?.let { displayDateFormat.format(it) } ?: "পর্যন্ত (To)",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Category Filter Chips (horizontal scroll)
            if (availableCategories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = filterCategory == null,
                        onClick = { viewModel.setFilterCategory(null) },
                        label = { Text("সব ক্যাটেগরি", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                    availableCategories.forEach { cat ->
                        val isSelected = filterCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setFilterCategory(if (isSelected) null else cat)
                            },
                            label = { Text(cat, fontSize = 11.sp) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }

            // Transaction Type Filter (All, Income, Expense)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { viewModel.setFilterType(null) },
                    label = { Text("সব ধরন (All)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = filterType == TransactionType.INCOME,
                    onClick = { viewModel.setFilterType(TransactionType.INCOME) },
                    label = { Text("আয় (Income)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = IncomeColor.copy(alpha = 0.18f),
                        selectedLabelColor = IncomeColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = filterType == TransactionType.EXPENSE,
                    onClick = { viewModel.setFilterType(TransactionType.EXPENSE) },
                    label = { Text("ব্যয় (Expense)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ExpenseColor.copy(alpha = 0.18f),
                        selectedLabelColor = ExpenseColor
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                )
            }

            // Filter Summary Banner
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "মোট: ${filterSummary.count} টি",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "আয়: ৳${String.format("%,.0f", filterSummary.income)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeColor
                    )
                    Text(
                        text = "ব্যয়: ৳${String.format("%,.0f", filterSummary.expense)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseColor
                    )
                    Text(
                        text = "ব্যালেন্স: ৳${String.format("%,.0f", filterSummary.balance)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (filterSummary.balance >= 0) IncomeColor else ExpenseColor
                    )
                }
            }

            // Transaction List
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো হিসাব পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ফিল্টার পরিবর্তন করে অথবা সার্চ ক্লিয়ার করে চেষ্টা করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(onClick = { viewModel.clearFilters() }) {
                            Text("ফিল্টার রিসেট করুন (Reset Filters)")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    groupedTransactions.forEach { (dateStr, txList) ->
                        stickyHeader(key = "header_$dateStr") {
                            val headerLabel = when (dateStr) {
                                todayStr -> "আজ (Today) • $dateStr"
                                yesterdayStr -> "গতকাল (Yesterday) • $dateStr"
                                else -> dateStr
                            }
                            val dayTotalIncome = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                            val dayTotalExpense = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

                            Surface(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = headerLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "+৳${String.format("%,.0f", dayTotalIncome)} | -৳${String.format("%,.0f", dayTotalExpense)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        items(txList, key = { it.id }) { tx ->
                            TransactionItem(
                                transaction = tx,
                                onEdit = { onNavigateToEdit(tx.id) },
                                onDelete = {
                                    viewModel.deleteTransaction(tx)
                                    coroutineScope.launch {
                                        val res = snackbarHostState.showSnackbar(
                                            message = "\"${tx.title}\" মুছে ফেলা হয়েছে",
                                            actionLabel = "আনডু (Undo)",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (res == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
