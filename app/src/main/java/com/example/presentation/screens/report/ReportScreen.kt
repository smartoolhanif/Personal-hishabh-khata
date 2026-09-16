package com.example.presentation.screens.report

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import com.example.util.ReportExportHelper
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewModel: ReportViewModel
) {
    val context = LocalContext.current
    val currentMonth by viewModel.currentMonth.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val balance by viewModel.balance.collectAsState()
    val categoryBreakdown by viewModel.categoryBreakdown.collectAsState()
    val monthlyTrends by viewModel.monthlyTrends.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val displayMonth = monthYearFormat.format(currentMonth)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "আর্থিক প্রতিবেদন (Reports)",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        if (transactions.isEmpty()) {
                            Toast.makeText(context, "এক্সপোর্ট করার মতো লেনদেন নেই", Toast.LENGTH_SHORT).show()
                        } else {
                            val pdfFile = ReportExportHelper.exportToPdf(
                                context = context,
                                periodTitle = displayMonth,
                                transactions = transactions,
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                netBalance = balance
                            )
                            if (pdfFile != null) {
                                ReportExportHelper.shareFile(context, pdfFile, "application/pdf", "হিসাব-খাতা PDF রিপোর্ট ($displayMonth)")
                            } else {
                                Toast.makeText(context, "PDF তৈরিতে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Filled.PictureAsPdf, contentDescription = "Export PDF", tint = MaterialTheme.colorScheme.primary)
                    }

                    IconButton(onClick = {
                        if (transactions.isEmpty()) {
                            Toast.makeText(context, "এক্সপোর্ট করার মতো লেনদেন নেই", Toast.LENGTH_SHORT).show()
                        } else {
                            val csvFile = ReportExportHelper.exportToCsv(
                                context = context,
                                periodName = displayMonth,
                                transactions = transactions
                            )
                            if (csvFile != null) {
                                ReportExportHelper.shareFile(context, csvFile, "text/csv", "হিসাব-খাতা Excel রিপোর্ট ($displayMonth)")
                            } else {
                                Toast.makeText(context, "Excel ফাইলে সমস্যা হয়েছে", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Filled.TableChart, contentDescription = "Export Excel/CSV", tint = MaterialTheme.colorScheme.secondary)
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
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Selector Header
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { viewModel.previousMonth() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Text(
                            text = displayMonth,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = { viewModel.nextMonth() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Monthly Net Balance & Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "মাসের নিট সঞ্চয় ও হিসাব (Monthly Summary)",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFFD4AF37)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val balanceSign = if (balance >= 0) "৳ " else "- ৳ "
                            Text(
                                text = "$balanceSign${String.format("%,.2f", Math.abs(balance))}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
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
                                        Text("মোট আয় (Income)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                        Text(
                                            text = "৳ ${String.format("%,.2f", totalIncome)}",
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
                                        Text("মোট ব্যয় (Expense)", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                                        Text(
                                            text = "৳ ${String.format("%,.2f", totalExpense)}",
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
            }

            // Visual Chart 1: Income vs Expense Ratio Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.PieChart,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "আয় বনাম ব্যয় অনুপাত (Income vs Expense)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val totalFlow = totalIncome + totalExpense
                        val incomeRatio = if (totalFlow > 0) (totalIncome / totalFlow).toFloat() else 0.5f
                        val expenseRatio = if (totalFlow > 0) (totalExpense / totalFlow).toFloat() else 0.5f

                        // Split progress bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(14.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(if (incomeRatio > 0.01f) incomeRatio else 0.01f)
                                    .background(IncomeColor)
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .weight(if (expenseRatio > 0.01f) expenseRatio else 0.01f)
                                    .background(ExpenseColor)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeColor))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "আয়: ${String.format("%.1f", incomeRatio * 100)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = IncomeColor
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseColor))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ব্যয়: ${String.format("%.1f", expenseRatio * 100)}%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = ExpenseColor
                                )
                            }
                        }
                    }
                }
            }

            // Visual Chart 2: 6-Month Trend Bar Chart (Canvas)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "গত ৬ মাসের ট্রেন্ড (Monthly Trend)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Chart Legend
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("আয়", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseColor))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ব্যয়", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val maxTrendAmount = monthlyTrends.maxOfOrNull { Math.max(it.income, it.expense) } ?: 1.0
                        val effectiveMax = if (maxTrendAmount <= 0) 100.0 else maxTrendAmount

                        // Canvas-based dual bar chart
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val w = size.width
                                val h = size.height - 30f // Leave space for labels
                                val count = monthlyTrends.size
                                if (count == 0) return@Canvas

                                val groupWidth = w / count
                                val barWidth = (groupWidth * 0.3f).coerceIn(8f, 22f)
                                val spacing = 4f

                                monthlyTrends.forEachIndexed { index, item ->
                                    val centerX = index * groupWidth + groupWidth / 2

                                    // Income bar
                                    val incomeH = ((item.income / effectiveMax) * h).toFloat().coerceAtLeast(4f)
                                    val incX = centerX - barWidth - spacing / 2
                                    val incY = h - incomeH
                                    drawRoundRect(
                                        color = IncomeColor,
                                        topLeft = Offset(incX, incY),
                                        size = Size(barWidth, incomeH),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )

                                    // Expense bar
                                    val expH = ((item.expense / effectiveMax) * h).toFloat().coerceAtLeast(4f)
                                    val expX = centerX + spacing / 2
                                    val expY = h - expH
                                    drawRoundRect(
                                        color = ExpenseColor,
                                        topLeft = Offset(expX, expY),
                                        size = Size(barWidth, expH),
                                        cornerRadius = CornerRadius(4f, 4f)
                                    )
                                }
                            }

                            // Month Labels under each bar group
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                monthlyTrends.forEach { item ->
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Visual Chart 3: Category-wise Expense Breakdown
            item {
                Text(
                    text = "ক্যাটেগরি অনুযায়ী ব্যয় (Expense Breakdown)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (categoryBreakdown.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "এই মাসে কোনো ব্যয়ের এন্ট্রি নেই",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(categoryBreakdown) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Category,
                                            contentDescription = item.categoryName,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "৳ ${String.format("%,.2f", item.amount)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ExpenseColor
                                    )
                                    Text(
                                        text = "${String.format("%.1f", item.percentage)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = ExpenseColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
