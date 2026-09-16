package com.example.presentation.screens.budget

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentGold
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel,
    onNavigateBack: () -> Unit
) {
    val budgetDisplays by viewModel.budgetDisplays.collectAsState()
    val totalBudget by viewModel.totalBudget.collectAsState()
    val totalSpent by viewModel.totalSpent.collectAsState()
    val monthYear by viewModel.currentMonthYear.collectAsState()

    var selectedForBudget by remember { mutableStateOf<CategoryBudgetDisplay?>(null) }
    var budgetInput by remember { mutableStateOf("") }

    val df = remember { DecimalFormat("#,##0") }
    val overallProgress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f) else 0f

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "মাসিক বাজেট গোল (Budgets)",
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Overall Monthly Budget Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "চলতি মাসের সামগ্রিক বাজেট ($monthYear)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${(overallProgress * 100).toInt()}% খরচ",
                                fontWeight = FontWeight.Bold,
                                color = if (totalSpent > totalBudget && totalBudget > 0) ExpenseColor else MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Linear Progress Indicator
                        LinearProgressIndicator(
                            progress = { overallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (totalSpent > totalBudget && totalBudget > 0) ExpenseColor else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("মোট ব্যয়", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "৳ ${df.format(totalSpent)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = ExpenseColor
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("মোট বাজেট", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "৳ ${df.format(totalBudget)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val remaining = (totalBudget - totalSpent).coerceAtLeast(0.0)
                                Text("অবশিষ্ট", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = "৳ ${df.format(remaining)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (totalSpent > totalBudget && totalBudget > 0) ExpenseColor else IncomeColor
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ক্যাটাগরি অনুযায়ী বাজেট",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Categories with Budgets
            items(budgetDisplays, key = { it.categoryId }) { item ->
                val hasBudget = item.targetAmount > 0
                val barColor = when {
                    item.isOverBudget -> ExpenseColor
                    item.progress >= 0.8f -> AccentGold
                    else -> IncomeColor
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedForBudget = item
                            budgetInput = if (hasBudget) item.targetAmount.toLong().toString() else ""
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = barColor.copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Filled.Category,
                                            contentDescription = null,
                                            tint = barColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        text = item.categoryName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (hasBudget) "বাজেট: ৳ ${df.format(item.targetAmount)}" else "কোনো বাজেট সেট নেই",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "৳ ${df.format(item.spentAmount)}",
                                    fontWeight = FontWeight.Bold,
                                    color = barColor,
                                    fontSize = 14.sp
                                )
                                if (hasBudget) {
                                    Text(
                                        text = "${item.percent}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (item.isOverBudget) ExpenseColor else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (hasBudget) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { item.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            if (item.isOverBudget) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = ExpenseColor,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "বাজেট সীমা অতিক্রম করেছে! (৳ ${df.format(item.spentAmount - item.targetAmount)} বেশি)",
                                        fontSize = 10.5.sp,
                                        color = ExpenseColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Set Budget Dialog
    selectedForBudget?.let { item ->
        Dialog(onDismissRequest = { selectedForBudget = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "${item.categoryName} বাজেট সেট করুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "এই মাসে ${item.categoryName} বাবদ কত টাকা পর্যন্ত খরচ করতে চান?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = budgetInput,
                        onValueChange = { budgetInput = it },
                        label = { Text("মাসিক বাজেট পরিমাণ (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedForBudget = null }) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val target = budgetInput.toDoubleOrNull() ?: 0.0
                                if (target > 0) {
                                    viewModel.setBudget(item.categoryId, item.categoryName, target)
                                    selectedForBudget = null
                                }
                            },
                            enabled = (budgetInput.toDoubleOrNull() ?: 0.0) > 0
                        ) {
                            Text("সেভ করুন")
                        }
                    }
                }
            }
        }
    }
}
