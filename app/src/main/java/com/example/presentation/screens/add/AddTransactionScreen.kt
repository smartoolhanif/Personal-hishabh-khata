package com.example.presentation.screens.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.domain.model.Category
import com.example.domain.model.TransactionType
import com.example.domain.model.Wallet
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    transactionIdToEdit: String? = null,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isEditMode = transactionIdToEdit != null

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedWallet by remember { mutableStateOf<Wallet?>(null) }
    var selectedDate by remember { mutableStateOf(Date()) }
    var receiptUriString by remember { mutableStateOf<String?>(null) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringFrequency by remember { mutableStateOf("মাসিক (Monthly)") }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var newCategoryName by remember { mutableStateOf("") }
    var editCategoryName by remember { mutableStateOf("") }

    val type by viewModel.type.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    val loadedTransaction by viewModel.loadedTransaction.collectAsState()
    val isLoadingTransaction by viewModel.isLoadingTransaction.collectAsState()

    // Photo Picker launcher for Receipt
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                // Copy image to app private storage to persist access
                val dir = File(context.filesDir, "receipts").apply { mkdirs() }
                val destFile = File(dir, "receipt_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output ->
                        input.copyTo(output)
                    }
                }
                receiptUriString = destFile.absolutePath
            } catch (e: Exception) {
                receiptUriString = uri.toString()
            }
        }
    }

    // Trigger loading if in edit mode
    LaunchedEffect(transactionIdToEdit) {
        if (transactionIdToEdit != null) {
            viewModel.loadTransactionById(transactionIdToEdit)
        }
    }

    // Populate fields when transaction is loaded
    LaunchedEffect(loadedTransaction, categories, wallets) {
        val tx = loadedTransaction
        if (tx != null) {
            title = tx.title
            amount = if (tx.amount % 1.0 == 0.0) tx.amount.toLong().toString() else tx.amount.toString()
            note = tx.note
            receiptUriString = tx.receiptImageUri
            isRecurring = tx.isRecurring
            recurringFrequency = tx.recurringFrequency.ifBlank { "মাসিক (Monthly)" }
            if (tx.timestamp > 0) {
                selectedDate = Date(tx.timestamp)
            }
            if (selectedCategory == null && categories.isNotEmpty()) {
                selectedCategory = categories.firstOrNull { it.id == tx.categoryId }
                    ?: categories.firstOrNull { it.name.equals(tx.categoryName, ignoreCase = true) }
                    ?: Category(id = tx.categoryId, name = tx.categoryName, type = tx.type, iconName = "")
            }
            if (selectedWallet == null && wallets.isNotEmpty()) {
                selectedWallet = wallets.firstOrNull { it.id == tx.walletId }
                    ?: wallets.firstOrNull { it.name.equals(tx.walletName, ignoreCase = true) }
            }
        } else if (selectedWallet == null && wallets.isNotEmpty()) {
            selectedWallet = wallets.first()
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSuccess()
            onNavigateBack()
        }
    }

    val filteredCategories = categories.filter { it.type == type }
    val displayDateTimeFormat = remember { SimpleDateFormat("EEEE, dd MMMM yyyy • hh:mm a", Locale.getDefault()) }

    val showDatePicker = {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val newCal = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                }
                selectedDate = newCal.time
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    val showTimePicker = {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val newCal = Calendar.getInstance().apply {
                    time = selectedDate
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                }
                selectedDate = newCal.time
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "লেনদেন পরিবর্তন করুন (Edit)" else "নতুন লেনদেন যোগ (Add Transaction)",
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
        if (isLoadingTransaction) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Transaction Type Toggle (Expense / Income)
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setType(TransactionType.EXPENSE)
                                    if (selectedCategory?.type != TransactionType.EXPENSE) {
                                        selectedCategory = null
                                    }
                                },
                            color = if (type == TransactionType.EXPENSE) ExpenseColor else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🔴 ব্যয় (Expense)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setType(TransactionType.INCOME)
                                    if (selectedCategory?.type != TransactionType.INCOME) {
                                        selectedCategory = null
                                    }
                                },
                            color = if (type == TransactionType.INCOME) Color(0xFF006D44) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🟢 আয় (Income)",
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // 2. Amount Input Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "টাকার পরিমাণ (Amount)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "৳",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (type == TransactionType.INCOME) Color(0xFF006D44) else ExpenseColor,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { amount = it },
                                placeholder = { Text("0.00", fontSize = 28.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = LocalTextStyle.current.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (type == TransactionType.INCOME) Color(0xFF006D44) else ExpenseColor
                                ),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // 3. Wallet / Account Selection
                if (wallets.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "অ্যাকাউন্ট / ওয়ালেট (Wallet)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(wallets, key = { it.id }) { wallet ->
                                    val isSelected = selectedWallet?.id == wallet.id
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { selectedWallet = wallet },
                                        label = { Text("${wallet.name} (৳${wallet.currentBalance.toInt()})") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.AccountBalanceWallet,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Category Selector Chips & Management
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
                            Text(
                                text = "ক্যাটেগরি নির্বাচন করুন (Category)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            FilledTonalButton(
                                onClick = {
                                    newCategoryName = ""
                                    showAddCategoryDialog = true
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("নতুন ক্যাটেগরি", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick Add Chip
                            item {
                                AssistChip(
                                    onClick = {
                                        newCategoryName = ""
                                        showAddCategoryDialog = true
                                    },
                                    label = { Text("+ নতুন", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Filled.AddCircleOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            items(filteredCategories, key = { it.id }) { category ->
                                val isSelected = selectedCategory?.id == category.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category.name) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Category,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = {
                                                categoryToEdit = category
                                                editCategoryName = category.name
                                            },
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Edit,
                                                contentDescription = "Edit Category",
                                                tint = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (type == TransactionType.INCOME) Color(0xFF006D44) else ExpenseColor,
                                        selectedLabelColor = Color.White,
                                        selectedLeadingIconColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }
                    }
                }

                // 5. Date & Time Selection (Supports Past Dates, Today, Yesterday)
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
                            Text(
                                text = "তারিখ ও সময় (Date & Time)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            // Quick Shortcuts: Today / Yesterday
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                SuggestionChip(
                                    onClick = { selectedDate = Date() },
                                    label = { Text("আজকে", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                                SuggestionChip(
                                    onClick = {
                                        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                                        selectedDate = cal.time
                                    },
                                    label = { Text("গতকাল", fontSize = 11.sp) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Full Formatted Date Display
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = displayDateTimeFormat.format(selectedDate),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Row {
                                    IconButton(onClick = showDatePicker, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            imageVector = Icons.Filled.CalendarMonth,
                                            contentDescription = "Pick Date",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    IconButton(onClick = showTimePicker, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            imageVector = Icons.Filled.AccessTime,
                                            contentDescription = "Pick Time",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 6. Title & Note Fields
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("শিরোনাম বা বিবরণ (Title)") },
                            placeholder = { Text("যেমন: বাজার খরচ, ইন্টারনেট বিল, বেতন") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = { Text("অতিরিক্ত নোট বা মন্তব্য (Note — Optional)") },
                            placeholder = { Text("প্রয়োজনীয় কোনো তথ্য যোগ করতে পারেন") },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // 7. Receipt Photo Attachment Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "রসিদের ছবি (Receipt Attachment)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (receiptUriString != null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                AsyncImage(
                                    model = receiptUriString,
                                    contentDescription = "Receipt",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { receiptUriString = null },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                        .size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Remove Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ভাউচার বা রসিদের ছবি যোগ করুন")
                            }
                        }
                    }
                }

                // 8. Recurring Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "রিকারিং ট্রানজেকশন (Recurring)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "বাড়িভাড়া, বিল ইত্যাদি নিয়মিত খরচের জন্য",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isRecurring,
                                onCheckedChange = { isRecurring = it }
                            )
                        }

                        if (isRecurring) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("মাসিক (Monthly)", "সাপ্তাহিক (Weekly)", "বাৎসরিক (Yearly)").forEach { freq ->
                                    FilterChip(
                                        selected = recurringFrequency == freq,
                                        onClick = { recurringFrequency = freq },
                                        label = { Text(freq, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }
                    }
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Action Button
                Button(
                    onClick = {
                        viewModel.saveTransaction(
                            title = title,
                            amountStr = amount,
                            category = selectedCategory,
                            note = note,
                            date = selectedDate,
                            wallet = selectedWallet,
                            receiptImageUri = receiptUriString,
                            isRecurring = isRecurring,
                            recurringFrequency = if (isRecurring) recurringFrequency else ""
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    enabled = !isSaving,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == TransactionType.INCOME) Color(0xFF006D44) else ExpenseColor
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = if (isEditMode) "লেনদেন আপডেট করুন (Update)" else "লেনদেন সংরক্ষণ করুন (Save)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Dialog: Add New Category
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = {
                Text(
                    text = "নতুন ক্যাটেগরি যোগ করুন (New Category)",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "ধরন: ${if (type == TransactionType.EXPENSE) "🔴 ব্যয় (Expense)" else "🟢 আয় (Income)"}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (type == TransactionType.EXPENSE) ExpenseColor else Color(0xFF006D44)
                    )
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("ক্যাটেগরির নাম *") },
                        placeholder = { Text("উদা: মুদি বাজার, ওষুধ, বই...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.addCategory(newCategoryName.trim(), type)
                            showAddCategoryDialog = false
                            newCategoryName = ""
                        }
                    },
                    enabled = newCategoryName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (type == TransactionType.INCOME) Color(0xFF006D44) else ExpenseColor
                    )
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddCategoryDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Edit or Delete Selected Category
    categoryToEdit?.let { targetCat ->
        var showDeleteConfirm by remember { mutableStateOf(false) }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("ক্যাটেগরি মুছে ফেলবেন?") },
                text = { Text("\"${targetCat.name}\" ক্যাটেগরি কি সত্যি মুছে ফেলতে চান?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCategory(targetCat.id)
                            if (selectedCategory?.id == targetCat.id) {
                                selectedCategory = null
                            }
                            showDeleteConfirm = false
                            categoryToEdit = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseColor)
                    ) {
                        Text("মুছে ফেলুন (Delete)")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showDeleteConfirm = false }) {
                        Text("বাতিল")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { categoryToEdit = null },
                title = {
                    Text(
                        text = "ক্যাটেগরি এডিট বা মুছুন",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = editCategoryName,
                            onValueChange = { editCategoryName = it },
                            label = { Text("ক্যাটেগরির নাম") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = ExpenseColor),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("এই ক্যাটেগরি মুছে ফেলুন", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (editCategoryName.isNotBlank()) {
                                val updated = targetCat.copy(name = editCategoryName.trim())
                                viewModel.updateCategory(updated)
                                if (selectedCategory?.id == targetCat.id) {
                                    selectedCategory = updated
                                }
                                categoryToEdit = null
                            }
                        },
                        enabled = editCategoryName.isNotBlank()
                    ) {
                        Text("আপডেট করুন")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { categoryToEdit = null }) {
                        Text("বাতিল")
                    }
                }
            )
        }
    }
}
