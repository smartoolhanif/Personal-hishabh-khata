package com.example.presentation.screens.khata

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.KhataParty
import com.example.domain.model.KhataType
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataScreen(
    viewModel: KhataViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val parties by viewModel.filteredParties.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val totalReceivable by viewModel.totalReceivable.collectAsState()
    val totalPayable by viewModel.totalPayable.collectAsState()
    val selectedParty by viewModel.selectedParty.collectAsState()
    val partyEntries by viewModel.selectedPartyEntries.collectAsState()

    var showAddPartyDialog by remember { mutableStateOf(false) }
    var showAddEntryDialog by remember { mutableStateOf(false) }
    var entryIsPayment by remember { mutableStateOf(false) }

    val df = remember { DecimalFormat("#,##0.00") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "বাকি ও ধার খাতা (Khata)",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddPartyDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নতুন খাতা", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Summary Cards Header (পাওনা vs দেনা)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total Receivable (পাওনা - Customer owes me)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = IncomeColor.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CallReceived,
                                contentDescription = null,
                                tint = IncomeColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "মোট পাওনা",
                                style = MaterialTheme.typography.labelMedium,
                                color = IncomeColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "৳ ${df.format(totalReceivable)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeColor
                        )
                        Text(
                            text = "আমি পাবো",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total Payable (দেনা - I owe supplier/person)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = ExpenseColor.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.CallMade,
                                contentDescription = null,
                                tint = ExpenseColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "মোট দেনা",
                                style = MaterialTheme.typography.labelMedium,
                                color = ExpenseColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "৳ ${df.format(totalPayable)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseColor
                        )
                        Text(
                            text = "আমি দেবো",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("নাম বা ফোন নম্বর দিয়ে খুঁজুন...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Filter Chips (সকল, পাওনা, দেনা)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = typeFilter == null,
                    onClick = { viewModel.setTypeFilter(null) },
                    label = { Text("সব খাতা") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = typeFilter == KhataType.RECEIVABLE,
                    onClick = { viewModel.setTypeFilter(KhataType.RECEIVABLE) },
                    label = { Text("পাওনা (পাবো)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = typeFilter == KhataType.PAYABLE,
                    onClick = { viewModel.setTypeFilter(KhataType.PAYABLE) },
                    label = { Text("দেনা (দেবো)") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Parties List
            if (parties.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "কোনো খাতা বা বাকি নেই",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "নিচের বাটনে চাপ দিয়ে কাস্টমার বা পার্টির নাম যোগ করুন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(parties, key = { it.id }) { party ->
                        val isReceivable = party.type == KhataType.RECEIVABLE
                        val tagColor = if (isReceivable) IncomeColor else ExpenseColor

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectParty(party) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar Circle
                                Surface(
                                    shape = CircleShape,
                                    color = tagColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = party.name.take(1).uppercase(Locale.getDefault()),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = tagColor
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = party.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (party.phone.isNotBlank()) {
                                        Text(
                                            text = party.phone,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = if (isReceivable) "পাওনা খাতা (গ্রাহক)" else "দেনা খাতা (মহাজন)",
                                        fontSize = 11.sp,
                                        color = tagColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳ ${df.format(party.totalAmount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = tagColor
                                    )
                                    Text(
                                        text = if (isReceivable) "পাবো" else "দেবো",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Party Dialog
    if (showAddPartyDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(KhataType.RECEIVABLE) }
        var initialAmount by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddPartyDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "নতুন খাতা খুলুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // Type Toggle (পাওনা vs দেনা)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = type == KhataType.RECEIVABLE,
                            onClick = { type = KhataType.RECEIVABLE },
                            label = { Text("পাওনা (আমি পাবো)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == KhataType.PAYABLE,
                            onClick = { type = KhataType.PAYABLE },
                            label = { Text("দেনা (আমি দেবো)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("নাম (Customer/Party Name)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = initialAmount,
                        onValueChange = { initialAmount = it },
                        label = { Text("প্রারম্ভিক বাকি (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("বিবরণ / নোট (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddPartyDialog = false }) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val amount = initialAmount.toDoubleOrNull() ?: 0.0
                                    viewModel.addParty(name, phone, address, type, amount, note)
                                    showAddPartyDialog = false
                                }
                            },
                            enabled = name.isNotBlank()
                        ) {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }

    // Party Detail / Ledger Dialog
    selectedParty?.let { party ->
        val isReceivable = party.type == KhataType.RECEIVABLE
        val tagColor = if (isReceivable) IncomeColor else ExpenseColor

        Dialog(onDismissRequest = { viewModel.selectParty(null) }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = party.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (party.phone.isNotBlank()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = party.phone,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                                data = Uri.parse("tel:${party.phone}")
                                            }
                                            context.startActivity(intent)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Filled.Call, contentDescription = "Call", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                        IconButton(onClick = { viewModel.selectParty(null) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    // Balance Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = tagColor.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isReceivable) "বর্তমান মোট পাওনা" else "বর্তমান মোট দেনা",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "৳ ${df.format(party.totalAmount)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = tagColor
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteParty(party.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    Text(
                        text = "লেনদেন ইতিহাস (Entries)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    // Entries List
                    if (partyEntries.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "কোনো এন্ট্রি নেই। নিচের বাটন দিয়ে যোগ করুন।",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(partyEntries, key = { it.id }) { entry ->
                                val isPayment = entry.isPayment
                                val entryColor = if (isPayment) IncomeColor else ExpenseColor

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isPayment) "পরিশোধ / জমা (Payment)" else "নতুন বাকি (Added Debt)",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = entryColor
                                            )
                                            if (entry.note.isNotBlank()) {
                                                Text(
                                                    text = entry.note,
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = entry.date,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${if (isPayment) "-" else "+"}৳ ${df.format(entry.amount)}",
                                                fontWeight = FontWeight.Bold,
                                                color = entryColor,
                                                fontSize = 14.sp
                                            )
                                            IconButton(
                                                onClick = { viewModel.deleteEntry(entry.id, party.id) },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    Icons.Filled.Close,
                                                    contentDescription = "Delete",
                                                    tint = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Bottom Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Payment Received / Given
                        Button(
                            onClick = {
                                entryIsPayment = true
                                showAddEntryDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IncomeColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("টাকা জমা / পরিশোধ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // Add Debt / Credit
                        Button(
                            onClick = {
                                entryIsPayment = false
                                showAddEntryDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ExpenseColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("বাকি যোগ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Add Entry Dialog
    if (showAddEntryDialog && selectedParty != null) {
        val party = selectedParty!!
        var amountStr by remember { mutableStateOf("") }
        var noteStr by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddEntryDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (entryIsPayment) "টাকা জমা বা পরিশোধ" else "নতুন বাকি যোগ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "পার্টি: ${party.name}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("পরিমাণ (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = noteStr,
                        onValueChange = { noteStr = it },
                        label = { Text("বিবরণ / নোট") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddEntryDialog = false }) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = amountStr.toDoubleOrNull() ?: 0.0
                                if (amount > 0) {
                                    viewModel.addEntry(party, amount, entryIsPayment, noteStr)
                                    showAddEntryDialog = false
                                }
                            },
                            enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0
                        ) {
                            Text("যোগ করুন")
                        }
                    }
                }
            }
        }
    }
}
