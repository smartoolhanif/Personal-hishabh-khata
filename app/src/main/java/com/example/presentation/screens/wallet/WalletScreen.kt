package com.example.presentation.screens.wallet

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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.domain.model.Wallet
import com.example.ui.theme.AccentGold
import com.example.ui.theme.HeroGradientEnd
import com.example.ui.theme.HeroGradientStart
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    viewModel: WalletViewModel,
    onNavigateBack: () -> Unit
) {
    val wallets by viewModel.wallets.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()

    var showAddWalletDialog by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }

    val df = remember { DecimalFormat("#,##0.00") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ওয়ালেট ও অ্যাকাউন্ট (Wallets)",
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
                onClick = { showAddWalletDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = MaterialTheme.shapes.large
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.AddCard, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("নতুন অ্যাকাউন্ট", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Balance Card
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
                                Brush.linearGradient(
                                    colors = listOf(HeroGradientStart, HeroGradientEnd)
                                )
                            )
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "সকল অ্যাকাউন্টের মোট স্থিতি",
                                    color = Color(0xFFC2EBD4),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color.White.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${wallets.size} টি ওয়ালেট",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "৳ ${df.format(totalBalance)}",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Transfer money button
                            Button(
                                onClick = { showTransferDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentGold,
                                    contentColor = Color(0xFF1B221E)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.SwapHoriz,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "টাকা স্থানান্তর (Transfer)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "আপনার অ্যাকাউন্টসমূহ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }

            // Wallet items
            items(wallets, key = { it.id }) { wallet ->
                val (cardIcon, cardColor) = when (wallet.type.lowercase()) {
                    "cash" -> Pair(Icons.Filled.Money, Color(0xFF0B6E4F))
                    "bank" -> Pair(Icons.Filled.AccountBalance, Color(0xFF1565C0))
                    "bkash" -> Pair(Icons.Filled.PhoneAndroid, Color(0xFFD12053))
                    "nagad" -> Pair(Icons.Filled.FlashOn, Color(0xFFE65100))
                    "rocket" -> Pair(Icons.Filled.RocketLaunch, Color(0xFF6A1B9A))
                    else -> Pair(Icons.Filled.Wallet, Color(0xFF37474F))
                }

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
                            color = cardColor.copy(alpha = 0.14f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = cardIcon,
                                    contentDescription = null,
                                    tint = cardColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = wallet.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (wallet.accountNumber.isNotBlank()) {
                                Text(
                                    text = "হিসাব নং: ${wallet.accountNumber}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "৳ ${df.format(wallet.currentBalance)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "ব্যালেন্স",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Wallet Dialog
    if (showAddWalletDialog) {
        var name by remember { mutableStateOf("") }
        var type by remember { mutableStateOf("bank") }
        var initialBalance by remember { mutableStateOf("") }
        var accNum by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showAddWalletDialog = false }) {
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
                        text = "নতুন অ্যাকাউন্ট যোগ করুন",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("অ্যাকাউন্টের নাম (যেমন: ব্র্যাক ব্যাংক, সিটি ব্যাংক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = accNum,
                        onValueChange = { accNum = it },
                        label = { Text("হিসাব / মোবাইল নম্বর (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("বর্তমান ব্যালেন্স (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddWalletDialog = false }) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val bal = initialBalance.toDoubleOrNull() ?: 0.0
                                    viewModel.addWallet(name, type, bal, accNum, "#1565C0")
                                    showAddWalletDialog = false
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

    // Transfer Money Dialog
    if (showTransferDialog && wallets.size >= 2) {
        var fromIndex by remember { mutableStateOf(0) }
        var toIndex by remember { mutableStateOf(1.coerceAtMost(wallets.size - 1)) }
        var amountStr by remember { mutableStateOf("") }
        var noteStr by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showTransferDialog = false }) {
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
                        text = "টাকা স্থানান্তর (Transfer)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    // From Wallet
                    Text("কোন অ্যাকাউন্ট থেকে পাঠাবেন:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        wallets.take(3).forEachIndexed { index, w ->
                            FilterChip(
                                selected = fromIndex == index,
                                onClick = { fromIndex = index },
                                label = { Text(w.name.take(8), fontSize = 11.sp) }
                            )
                        }
                    }

                    // To Wallet
                    Text("কোন অ্যাকাউন্টে জমা হবে:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        wallets.take(3).forEachIndexed { index, w ->
                            FilterChip(
                                selected = toIndex == index,
                                onClick = { toIndex = index },
                                label = { Text(w.name.take(8), fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("স্থানান্তরের পরিমাণ (৳)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = noteStr,
                        onValueChange = { noteStr = it },
                        label = { Text("বিবরণ / নোট (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTransferDialog = false }) {
                            Text("বাতিল")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amount = amountStr.toDoubleOrNull() ?: 0.0
                                if (amount > 0 && fromIndex != toIndex) {
                                    viewModel.transferMoney(wallets[fromIndex], wallets[toIndex], amount, noteStr)
                                    showTransferDialog = false
                                }
                            },
                            enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0 && fromIndex != toIndex
                        ) {
                            Text("ট্রান্সফার করুন")
                        }
                    }
                }
            }
        }
    }
}
