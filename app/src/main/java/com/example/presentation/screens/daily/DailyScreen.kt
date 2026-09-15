package com.example.presentation.screens.daily

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.components.TransactionItem
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyScreen(
    viewModel: DailyViewModel,
    onNavigateToEdit: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val currentDate by viewModel.currentDate.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val balance by viewModel.balance.collectAsState()

    val fullDateFormatter = remember { SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()) }
    val dayMonthFormatter = remember { SimpleDateFormat("dd MMMM", Locale.getDefault()) }

    val showDatePicker = {
        val cal = Calendar.getInstance().apply { time = currentDate }
        DatePickerDialog(
            context,
            { _, year, month, day ->
                val newDate = Calendar.getInstance().apply {
                    set(year, month, day)
                }.time
                viewModel.setDate(newDate)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "দৈনিক হিসাব (Daily Ledger)",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.goToToday() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Filled.Today, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("আজকে (Today)", fontWeight = FontWeight.Bold)
                    }
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
            // Date Navigation Header
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { viewModel.previousDay() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Day",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Clickable Date Title to Open Calendar
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePicker() },
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Pick Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = fullDateFormatter.format(currentDate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.nextDay() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Day",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Daily Balance & Metrics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF0D422A), Color(0xFF006D44))
                            )
                        )
                        .padding(18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${dayMonthFormatter.format(currentDate)}-এর নিট হিসাব",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFD4AF37),
                                fontWeight = FontWeight.SemiBold
                            )
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "${transactions.size} টি এন্ট্রি",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val balanceSign = if (balance >= 0) "৳ " else "- ৳ "
                        Text(
                            text = "$balanceSign${String.format("%,.2f", Math.abs(balance))}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (balance >= 0) Color(0xFF80E8B0) else Color(0xFFFF9E9E)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("দিনের আয় (Income)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        text = "+ ৳ ${String.format("%,.2f", totalIncome)}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF69F0AE),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("দিনের ব্যয় (Expense)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                    Text(
                                        text = "- ৳ ${String.format("%,.2f", totalExpense)}",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFF8A80),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Transactions list for the selected day
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EventNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "এই তারিখে কোনো লেনদেন নেই",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${fullDateFormatter.format(currentDate)}-এ কোনো আয় বা ব্যয় যোগ করা হয়নি।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionItem(
                            transaction = tx,
                            onEdit = { onNavigateToEdit(tx.id) },
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                }
            }
        }
    }
}
