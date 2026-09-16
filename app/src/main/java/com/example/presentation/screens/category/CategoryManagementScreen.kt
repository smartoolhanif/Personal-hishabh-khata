package com.example.presentation.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Category
import com.example.domain.model.TransactionType
import com.example.ui.theme.ExpenseColor
import com.example.ui.theme.IncomeColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    onNavigateBack: () -> Unit
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categories by viewModel.filteredCategories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ক্যাটেগরি ব্যবস্থাপনা (Categories)",
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
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = if (selectedTab == TransactionType.EXPENSE) ExpenseColor else IncomeColor,
                contentColor = Color.White,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add") },
                text = { Text("নতুন ক্যাটেগরি", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Type Selector Tabs (ব্যয় / আয়)
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Expense Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setSelectedTab(TransactionType.EXPENSE) },
                        color = if (selectedTab == TransactionType.EXPENSE) ExpenseColor else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🔴 খরচের ক্যাটেগরি (Expense)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedTab == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Income Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setSelectedTab(TransactionType.INCOME) },
                        color = if (selectedTab == TransactionType.INCOME) IncomeColor else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🟢 আয়ের ক্যাটেগরি (Income)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedTab == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("ক্যাটেগরি খুঁজুন...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Category List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "মোট ক্যাটেগরি: ${categories.size} টি",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = { viewModel.seedDefaults() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ডিফল্ট রিস্টোর", fontSize = 12.sp)
                }
            }

            // Categories List
            if (categories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Category,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো ক্যাটেগরি পাওয়া যায়নি",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "নিচের বাটনে ক্লিক করে নতুন ক্যাটেগরি যোগ করুন।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("নতুন ক্যাটেগরি যোগ করুন")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        CategoryItemCard(
                            category = category,
                            onEdit = { categoryToEdit = category },
                            onDelete = { categoryToDelete = category }
                        )
                    }
                }
            }
        }
    }

    // Add Category Dialog
    if (showAddDialog) {
        AddOrEditCategoryDialog(
            initialCategory = null,
            initialType = selectedTab,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, type, iconName ->
                viewModel.addCategory(name, type, iconName)
                showAddDialog = false
            }
        )
    }

    // Edit Category Dialog
    categoryToEdit?.let { cat ->
        AddOrEditCategoryDialog(
            initialCategory = cat,
            initialType = cat.type,
            onDismiss = { categoryToEdit = null },
            onConfirm = { name, _, iconName ->
                viewModel.updateCategory(cat, name, iconName)
                categoryToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    categoryToDelete?.let { cat ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            icon = { Icon(Icons.Filled.DeleteOutline, contentDescription = null, tint = ExpenseColor) },
            title = { Text("ক্যাটেগরি মুছে ফেলবেন?") },
            text = { Text("\"${cat.name}\" ক্যাটেগরি মুছে ফেলতে চান? পূর্বে যুক্ত লেনদেনগুলি তাদের স্থানে বহাল থাকবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategory(cat.id)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseColor)
                ) {
                    Text("মুছে ফেলুন (Delete)")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { categoryToDelete = null }) {
                    Text("বাতিল (Cancel)")
                }
            }
        )
    }
}

@Composable
fun CategoryItemCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isExpense = category.type == TransactionType.EXPENSE
    val themeColor = if (isExpense) ExpenseColor else IncomeColor

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(themeColor.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCategoryVectorIcon(category.iconName),
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Category Name & Type
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (isExpense) "ব্যয় (Expense)" else "আয় (Income)",
                    style = MaterialTheme.typography.bodySmall,
                    color = themeColor
                )
            }

            // Edit Button
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Delete Button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = ExpenseColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AddOrEditCategoryDialog(
    initialCategory: Category? = null,
    initialType: TransactionType = TransactionType.EXPENSE,
    onDismiss: () -> Unit,
    onConfirm: (name: String, type: TransactionType, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCategory?.name ?: "") }
    var type by remember { mutableStateOf(initialCategory?.type ?: initialType) }
    var selectedIcon by remember { mutableStateOf(initialCategory?.iconName ?: "category") }
    val isEdit = initialCategory != null

    val iconOptions = listOf(
        "shopping_cart" to Icons.Filled.ShoppingCart,
        "restaurant" to Icons.Filled.Restaurant,
        "home" to Icons.Filled.Home,
        "directions_bus" to Icons.Filled.DirectionsBus,
        "medical_services" to Icons.Filled.LocalHospital,
        "shopping_bag" to Icons.Filled.ShoppingBag,
        "bolt" to Icons.Filled.ElectricBolt,
        "water_drop" to Icons.Filled.WaterDrop,
        "wifi" to Icons.Filled.Wifi,
        "smartphone" to Icons.Filled.Smartphone,
        "payments" to Icons.Filled.Payments,
        "store" to Icons.Filled.Store,
        "laptop_mac" to Icons.Filled.LaptopMac,
        "family_restroom" to Icons.Filled.FamilyRestroom,
        "category" to Icons.Filled.Category
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "ক্যাটেগরি এডিট করুন (Edit)" else "নতুন ক্যাটেগরি যোগ (New Category)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("ক্যাটেগরির নাম *") },
                    placeholder = { Text("উদা: মুদি বাজার, বিনোদন...") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Type Toggle (if adding)
                if (!isEdit) {
                    Text(
                        text = "ধরন নির্ধারণ করুন:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = type == TransactionType.EXPENSE,
                            onClick = { type = TransactionType.EXPENSE },
                            label = { Text("ব্যয় (Expense)") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = type == TransactionType.INCOME,
                            onClick = { type = TransactionType.INCOME },
                            label = { Text("আয় (Income)") },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Icon Picker
                Text(
                    text = "আইকন নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconOptions.take(5).forEach { (iconKey, vector) ->
                        val isSelected = selectedIcon == iconKey
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .clickable { selectedIcon = iconKey },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = CircleShape
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = vector,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name.trim(), type, selectedIcon)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(if (isEdit) "পরিবর্তন সংরক্ষণ" else "যোগ করুন")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

fun getCategoryVectorIcon(iconName: String): ImageVector {
    return when (iconName) {
        "restaurant" -> Icons.Filled.Restaurant
        "shopping_cart" -> Icons.Filled.ShoppingCart
        "home" -> Icons.Filled.Home
        "directions_bus" -> Icons.Filled.DirectionsBus
        "medical_services" -> Icons.Filled.LocalHospital
        "shopping_bag" -> Icons.Filled.ShoppingBag
        "bolt" -> Icons.Filled.ElectricBolt
        "water_drop" -> Icons.Filled.WaterDrop
        "wifi" -> Icons.Filled.Wifi
        "smartphone" -> Icons.Filled.Smartphone
        "payments" -> Icons.Filled.Payments
        "store" -> Icons.Filled.Store
        "laptop_mac" -> Icons.Filled.LaptopMac
        "family_restroom" -> Icons.Filled.FamilyRestroom
        else -> Icons.Filled.Category
    }
}
