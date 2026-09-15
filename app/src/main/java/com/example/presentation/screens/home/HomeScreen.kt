package com.example.presentation.screens.home

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.presentation.components.TransactionItem
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToAi: () -> Unit,
    onNavigateToHistory: () -> Unit = {},
    onNavigateToEdit: (String) -> Unit = {}
) {
    val user by viewModel.currentUser.collectAsState()
    val liveDate by viewModel.liveDate.collectAsState()
    val liveTime by viewModel.liveTime.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val activeSummary by viewModel.activeSummary.collectAsState()

    val todaySummary by viewModel.todaySummary.collectAsState()
    val thisMonthSummary by viewModel.thisMonthSummary.collectAsState()
    val prevMonthSummary by viewModel.previousMonthSummary.collectAsState()
    val thisYearSummary by viewModel.thisYearSummary.collectAsState()

    val recentTransactions by viewModel.recentTransactions.collectAsState()

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Profile Image & User Greeting
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Circle Profile Image or Initial Avatar
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(
                                    width = 2.dp,
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF006D44), Color(0xFFD4AF37))
                                    ),
                                    shape = CircleShape
                                )
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!user?.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(user?.photoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                val initial = (user?.name?.firstOrNull() ?: 'U').uppercaseChar().toString()
                                Text(
                                    text = initial,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF006D44)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "স্বাগতম 👋",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = user?.name?.takeIf { it.isNotBlank() } ?: "Md Hanif",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // AI Assistant Quick Action
                    IconButton(
                        onClick = onNavigateToAi,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI Assistant",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Clock & Local Date Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Date column
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "Local Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = liveDate,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Live Time with Seconds
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccessTime,
                                    contentDescription = "Live Clock",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = liveTime,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // 2. Summary Period Tabs (Today | This Month | Previous Month | This Year)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SummaryPeriod.values().forEach { period ->
                        val isSelected = period == selectedPeriod
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectPeriod(period) },
                            label = {
                                Text(
                                    text = period.labelBn,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF006D44),
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Hero Balance Card (Active Summary)
            item {
                AnimatedContent(
                    targetState = activeSummary,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "ActiveSummaryAnimation"
                ) { summary ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(24.dp),
                                spotColor = Color(0xFF006D44).copy(alpha = 0.3f)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF0D422A),
                                            Color(0xFF072B1C)
                                        )
                                    )
                                )
                                .padding(20.dp)
                        ) {
                            Column {
                                // Period Label & Transaction Count Tag
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = summary.subTitle,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color(0xFFD4AF37),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "${summary.title} Balance",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                    }

                                    Surface(
                                        color = Color.White.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "${summary.transactionCount} টি লেনদেন",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Large Net Balance
                                val balancePrefix = if (summary.balance >= 0) "৳ " else "- ৳ "
                                val formattedBalance = String.format("%,.2f", Math.abs(summary.balance))

                                Text(
                                    text = "$balancePrefix$formattedBalance",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = if (summary.balance >= 0) Color(0xFF80E8B0) else Color(0xFFFF9E9E)
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Income vs Expense Metrics Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Income Mini Card
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White.copy(alpha = 0.08f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF2E7D32).copy(alpha = 0.3f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowDownward,
                                                    contentDescription = "Income",
                                                    tint = Color(0xFF69F0AE),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "মোট আয় (Income)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = "৳ ${String.format("%,.2f", summary.income)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF69F0AE)
                                                )
                                            }
                                        }
                                    }

                                    // Expense Mini Card
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White.copy(alpha = 0.08f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFC62828).copy(alpha = 0.3f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.ArrowUpward,
                                                    contentDescription = "Expense",
                                                    tint = Color(0xFFFF8A80),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "মোট ব্যয় (Expense)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White.copy(alpha = 0.7f),
                                                    fontSize = 10.sp
                                                )
                                                Text(
                                                    text = "৳ ${String.format("%,.2f", summary.expense)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFF8A80)
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

            // 4. Quick Summary Section (Today, This Month, Previous Month, This Year)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "সারসংক্ষেপ ও তুলনা (Overview & Comparison)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 1: Today & This Month
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickSummaryTile(
                            title = "আজকের হিসাব",
                            periodLabel = "Today",
                            icon = Icons.Filled.Today,
                            iconTint = Color(0xFF00897B),
                            income = todaySummary.income,
                            expense = todaySummary.expense,
                            balance = todaySummary.balance,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectPeriod(SummaryPeriod.TODAY) }
                        )

                        QuickSummaryTile(
                            title = "এই মাসের হিসাব",
                            periodLabel = "This Month",
                            icon = Icons.Filled.CalendarMonth,
                            iconTint = Color(0xFF1E88E5),
                            income = thisMonthSummary.income,
                            expense = thisMonthSummary.expense,
                            balance = thisMonthSummary.balance,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectPeriod(SummaryPeriod.THIS_MONTH) }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Previous Month & This Year
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickSummaryTile(
                            title = "গত মাসের হিসাব",
                            periodLabel = "Prev Month",
                            icon = Icons.Filled.History,
                            iconTint = Color(0xFF8E24AA),
                            income = prevMonthSummary.income,
                            expense = prevMonthSummary.expense,
                            balance = prevMonthSummary.balance,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectPeriod(SummaryPeriod.PREVIOUS_MONTH) }
                        )

                        QuickSummaryTile(
                            title = "এই বছরের হিসাব",
                            periodLabel = "This Year",
                            icon = Icons.Filled.ShowChart,
                            iconTint = Color(0xFFD84315),
                            income = thisYearSummary.income,
                            expense = thisYearSummary.expense,
                            balance = thisYearSummary.balance,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.selectPeriod(SummaryPeriod.THIS_YEAR) }
                        )
                    }
                }
            }

            // 5. Recent Transactions Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "সাম্প্রতিক হিসাব (Recent Records)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "সর্বশেষ ৫টি লেনদেন",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = onNavigateToHistory,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "সব দেখুন",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View All",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // List of Recent Transactions
            if (recentTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountBalanceWallet,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "এখনো কোনো হিসাব যোগ করা হয়নি",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "নিচের (+) বাটনে চাপ দিয়ে যেকোনো তারিখের আয় বা ব্যয় যোগ করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(recentTransactions, key = { it.id }) { tx ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
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

@Composable
fun QuickSummaryTile(
    title: String,
    periodLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    income: Double,
    expense: Double,
    balance: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = periodLabel,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = periodLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            val balancePrefix = if (balance >= 0) "৳ " else "- ৳ "
            Text(
                text = "$balancePrefix${String.format("%,.0f", Math.abs(balance))}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (balance >= 0) IncomeColor else ExpenseColor
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "আয়: ৳${String.format("%,.0f", income)}",
                    fontSize = 10.sp,
                    color = IncomeColor
                )
                Text(
                    text = "ব্যয়: ৳${String.format("%,.0f", expense)}",
                    fontSize = 10.sp,
                    color = ExpenseColor
                )
            }
        }
    }
}
