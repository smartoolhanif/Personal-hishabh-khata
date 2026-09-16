package com.example.presentation.screens.recurring

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.TransactionType
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    viewModel: RecurringViewModel,
    onNavigateBack: () -> Unit
) {
    val items by viewModel.recurringItems.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val df = remember { DecimalFormat("#,##0.00") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "রিকারিং বিল ও রিমাইন্ডার",
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
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AddAlert, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নতুন রিমাইন্ডার", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
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
                                imageVector = Icons.Filled.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "কোনো রিকারিং বিল সেট করা নেই",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "বাড়িভাড়া, বিদ্যুৎ বিল, ইন্টারনেট বিল ইত্যাদির জন্য রিমাইন্ডার সেট করুন",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    val isExpense = item.type == TransactionType.EXPENSE
                    val color = if (isExpense) ExpenseColor else IncomeColor

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = color.copy(alpha = 0.14f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.Repeat,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${item.frequency} • প্রতি মাসের ${item.dayOfMonth} তারিখ",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (item.lastGeneratedDate.isNotBlank()) {
                                    Text(
                                        text = "সর্বশেষ পরিশোধ: ${item.lastGeneratedDate}",
                                        fontSize = 10.5.sp,
                                        color = IncomeColor
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "৳ ${df.format(item.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = color
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Button(
                                        onClick = { viewModel.markAsPaid(item) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("পরিশোধ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteRecurringItem(item.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
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
        }
    }

    // Add Dialog
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var dayOfMonthStr by remember { mutableStateOf("5") }
        var frequency by remember { mutableStateOf("মাসিক (Monthly)") }

        Dialog(onDismissRequest = { showAddDialog = false }) {
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
                        text = "নতুন রিমাইন্ডার বিল যোগ করুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("বিলের নাম (যেমন: বাড়িভাড়া, ওয়াইফাই)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
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
                        value = dayOfMonthStr,
                        onValueChange = { dayOfMonthStr = it },
                        label = { Text("মাসের কত তারিখে (১ - ৩১)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = amountStr.toDoubleOrNull() ?: 0.0
                                val day = dayOfMonthStr.toIntOrNull() ?: 1
                                if (title.isNotBlank() && amount > 0) {
                                    viewModel.addRecurringItem(
                                        title = title,
                                        amount = amount,
                                        type = TransactionType.EXPENSE,
                                        categoryName = "বিল",
                                        frequency = frequency,
                                        dayOfMonth = day
                                    )
                                    showAddDialog = false
                                }
                            },
                            enabled = title.isNotBlank() && (amountStr.toDoubleOrNull() ?: 0.0) > 0
                        ) {
                            Text("সংরক্ষণ করুন")
                        }
                    }
                }
            }
        }
    }
}
