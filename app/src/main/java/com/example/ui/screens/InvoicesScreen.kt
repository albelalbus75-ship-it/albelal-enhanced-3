package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.*
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class InvoiceTypeTab(val title: String) {
    SALES("فواتير المبيعات"),
    PURCHASES("فواتير المشتريات"),
    RETURNS("المرتجعات")
}

@Composable
fun InvoicesScreen(
    viewModel: StoreViewModel,
    onNavigateToPOS: () -> Unit
) {
    val context = LocalContext.current
    val branding by viewModel.brandingTheme.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val returns by viewModel.returns.collectAsState()
    val products by viewModel.products.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val currencies by viewModel.currencies.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    val primaryColor = parseHexColor(branding.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(branding.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(branding.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(branding.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(branding.textMutedColorHex, TextMuted)

    val canEdit = viewModel.hasPermission(AppScreen.INVOICES, ScreenAction.EDIT)
    val canDelete = viewModel.hasPermission(AppScreen.INVOICES, ScreenAction.DELETE)
    val canAdd = viewModel.hasPermission(AppScreen.INVOICES, ScreenAction.ADD)

    var currentTab by remember { mutableStateOf(InvoiceTypeTab.SALES) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedPaymentFilter by remember { mutableStateOf("الكل") }
    var viewingOrder by remember { mutableStateOf<SaleOrder?>(null) }
    var orderToDelete by remember { mutableStateOf<SaleOrder?>(null) }
    var orderToEdit by remember { mutableStateOf<SaleOrder?>(null) }

    var showAddPurchaseDialog by remember { mutableStateOf(false) }
    var showAddReturnDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("الكل", "نقدًا", "شبكة", "آجل")

    val filteredOrders = remember(orders, searchQuery, selectedPaymentFilter) {
        orders.filter { order ->
            val matchPayment = selectedPaymentFilter == "الكل" || order.paymentMethod == selectedPaymentFilter
            val matchQuery = searchQuery.isBlank() ||
                    order.id.contains(searchQuery, ignoreCase = true) ||
                    order.customer.name.contains(searchQuery, ignoreCase = true)
            matchPayment && matchQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("invoices_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Sticky Header / Top App Bar
        item {
            Surface(
                color = bgColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(primaryColor.copy(alpha = 0.15f))
                                    .border(1.dp, primaryColor, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "إدارة وسجل الفواتير",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textPrimary,
                                    fontFamily = CairoFont
                                )
                                Text(
                                    text = "مرتبطة آلياً بالقيود اليومية والمستودع",
                                    fontSize = 11.sp,
                                    color = primaryColor,
                                    fontFamily = CairoFont
                                )
                            }
                        }

                        if (canAdd) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { showAddPurchaseDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = TextDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مشتريات +", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                }

                                Button(
                                    onClick = { showAddReturnDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("مرتجع +", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Selector: Sales / Purchases / Returns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        InvoiceTypeTab.values().forEach { tab ->
                            val isSel = currentTab == tab
                            FilterChip(
                                selected = isSel,
                                onClick = { currentTab = tab },
                                label = { Text(tab.title, fontSize = 11.sp, fontFamily = CairoFont) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = TextDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search and Filter
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_invoices_field"),
                        placeholder = {
                            Text("البحث برقم الفاتورة أو اسم العميل...", color = textMuted, fontSize = 12.sp, fontFamily = CairoFont)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = null, tint = textMuted)
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        singleLine = true
                    )
                }
            }
        }

        when (currentTab) {
            InvoiceTypeTab.SALES -> {
                if (filteredOrders.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد فواتير مبيعات مطابقة", color = textMuted, fontFamily = CairoFont)
                        }
                    }
                } else {
                    items(filteredOrders, key = { it.id }) { order ->
                        InvoiceCardItem(
                            order = order,
                            primaryColor = primaryColor,
                            cardBg = cardBg,
                            textPrimary = textPrimary,
                            textMuted = textMuted,
                            canEdit = canEdit,
                            canDelete = canDelete,
                            onViewReceipt = { viewingOrder = order },
                            onEdit = { orderToEdit = order },
                            onDelete = { orderToDelete = order }
                        )
                    }
                }
            }
            InvoiceTypeTab.PURCHASES -> {
                if (purchases.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد فواتير مشتريات مسجلة حالياً", color = textMuted, fontFamily = CairoFont)
                        }
                    }
                } else {
                    items(purchases, key = { it.id }) { pur ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pur.invoiceNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary, fontFamily = CairoFont)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(color = Color(0xFF1E293B), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = "قيد: ${pur.journalEntryId}",
                                                color = primaryColor,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontFamily = CairoFont
                                            )
                                        }
                                    }
                                    Text("${"%,.2f".format(pur.totalAmount)} ر.س", fontWeight = FontWeight.Black, color = primaryColor, fontSize = 15.sp, fontFamily = CairoFont)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("المورد: ${pur.supplierName} • طريقة الدفع: ${pur.paymentMode.titleAr}", fontSize = 12.sp, color = textMuted, fontFamily = CairoFont)
                                Text("عدد الأصناف الموردة: ${pur.items.size} صنف", fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                            }
                        }
                    }
                }
            }
            InvoiceTypeTab.RETURNS -> {
                if (returns.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد فواتير مرتجعات مسجلة", color = textMuted, fontFamily = CairoFont)
                        }
                    }
                } else {
                    items(returns, key = { it.id }) { ret ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(ret.returnNumber, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary, fontFamily = CairoFont)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(color = Color(0xFF2E1212), shape = RoundedCornerShape(4.dp)) {
                                            Text(
                                                text = "قيد عكسي: ${ret.journalEntryId}",
                                                color = Color(0xFFEF4444),
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontFamily = CairoFont
                                            )
                                        }
                                    }
                                    Text("${"%,.2f".format(ret.totalAmount)} ر.س", fontWeight = FontWeight.Black, color = Color(0xFFEF4444), fontSize = 15.sp, fontFamily = CairoFont)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("${ret.type.titleAr} • الطرف: ${ret.partyName}", fontSize = 12.sp, color = textMuted, fontFamily = CairoFont)
                                if (ret.reason.isNotBlank()) {
                                    Text("السبب: ${ret.reason}", fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Receipt Dialog Preview
    if (viewingOrder != null) {
        ReceiptDialog(
            order = viewingOrder!!,
            onDismiss = { viewingOrder = null }
        )
    }

    // Delete Confirmation Dialog (Admin Only)
    if (orderToDelete != null) {
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = {
                Text("تأكيد حذف الفاتورة", color = textPrimary, fontFamily = CairoFont, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "هل أنت متأكد من حذف الفاتورة \"${orderToDelete?.id}\"؟ سيتم إلغاء القيد المحاسبي المرتبط (${orderToDelete?.journalEntryId}) واسترجاع الأصناف للمستودع وتعديل مديونية العميل.",
                    color = textMuted,
                    fontFamily = CairoFont
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        orderToDelete?.let { viewModel.deleteOrder(it.id) }
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("نعم، حذف الفاتورة وإلغاء القيد", fontFamily = CairoFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text("إلغاء", color = textPrimary, fontFamily = CairoFont)
                }
            },
            containerColor = cardBg
        )
    }

    // Edit Invoice Dialog (Admin Only)
    if (orderToEdit != null) {
        EditInvoiceDialog(
            order = orderToEdit!!,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { orderToEdit = null },
            onSave = { newPayment, discount ->
                viewModel.updateOrder(orderToEdit!!.id, newPayment, discount)
                orderToEdit = null
            }
        )
    }

    // Add Purchase Dialog
    if (showAddPurchaseDialog) {
        AddPurchaseDialog(
            products = products,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { showAddPurchaseDialog = false },
            onSave = { purchase ->
                viewModel.addPurchase(purchase)
                showAddPurchaseDialog = false
            }
        )
    }

    // Add Return Dialog
    if (showAddReturnDialog) {
        AddReturnDialog(
            products = products,
            orders = orders,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { showAddReturnDialog = false },
            onSave = { returnInv ->
                viewModel.addReturn(returnInv)
                showAddReturnDialog = false
            }
        )
    }
}

@Composable
private fun InvoiceCardItem(
    order: SaleOrder,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    canEdit: Boolean,
    canDelete: Boolean,
    onViewReceipt: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onViewReceipt)
            .testTag("invoice_card_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f))
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "#", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = order.id, color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, fontFamily = CairoFont)
                            Spacer(modifier = Modifier.width(6.dp))
                            // Accounting Journal Entry ID link (Requirement 4)
                            if (order.journalEntryId.isNotEmpty()) {
                                Surface(color = primaryColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                    Text(
                                        text = "قيد: ${order.journalEntryId}",
                                        color = primaryColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontFamily = CairoFont
                                    )
                                }
                            }
                        }
                        Text(text = order.formattedDate, color = textMuted, fontSize = 11.sp, fontFamily = CairoFont)
                    }
                }

                // Payment method badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (order.paymentMethod) {
                        "نقدًا" -> Color(0xFF10B981).copy(alpha = 0.18f)
                        "شبكة" -> primaryColor.copy(alpha = 0.18f)
                        else -> Color(0xFFEF4444).copy(alpha = 0.18f)
                    },
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when (order.paymentMethod) {
                            "نقدًا" -> Color(0xFF10B981)
                            "شبكة" -> primaryColor
                            else -> Color(0xFFEF4444)
                        }
                    )
                ) {
                    Text(
                        text = order.paymentMethod,
                        color = when (order.paymentMethod) {
                            "نقدًا" -> Color(0xFF10B981)
                            "شبكة" -> primaryColor
                            else -> Color(0xFFEF4444)
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFont,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "العميل: ${order.customer.name}", color = textPrimary, fontSize = 12.sp, fontFamily = CairoFont)
                    Text(text = "${order.totalItemsCount} أصناف مباعة", color = textMuted, fontSize = 11.sp, fontFamily = CairoFont)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${"%,.2f".format(order.total)} ر.س", color = primaryColor, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = CairoFont)
                    Text(text = "شامل ضريبة 15%", color = textMuted, fontSize = 10.sp, fontFamily = CairoFont)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = primaryColor.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(6.dp))

            // Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onViewReceipt,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض وتصدير الفاتورة", color = primaryColor, fontSize = 11.sp, fontFamily = CairoFont)
                }

                Row {
                    if (canEdit) {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = textMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                    if (canDelete) {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف الفاتورة", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditInvoiceDialog(
    order: SaleOrder,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onDismiss: () -> Unit,
    onSave: (paymentMethod: String, discount: Double) -> Unit
) {
    var paymentMethod by remember { mutableStateOf(order.paymentMethod) }
    var discountText by remember { mutableStateOf(order.discount.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(20.dp)),
            color = cardBg
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(text = "تعديل الفاتورة ${order.id}", color = primaryColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                Spacer(modifier = Modifier.height(14.dp))

                Text("طريقة الدفع:", color = textPrimary, fontSize = 12.sp, fontFamily = CairoFont)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("نقدًا", "شبكة", "آجل").forEach { method ->
                        val isSel = paymentMethod == method
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { paymentMethod = method },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) primaryColor else Color(0xFF333333))
                        ) {
                            Text(
                                text = method,
                                color = if (isSel) primaryColor else textPrimary,
                                modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = discountText,
                    onValueChange = { discountText = it },
                    label = { Text("مبلغ الخصم (ريال)", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val disc = discountText.toDoubleOrNull() ?: 0.0
                            onSave(paymentMethod, disc)
                        },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حفظ التعديلات", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(0.6f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء", fontFamily = CairoFont)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddPurchaseDialog(
    products: List<Product>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onDismiss: () -> Unit,
    onSave: (PurchaseInvoice) -> Unit
) {
    var supplierName by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantityText by remember { mutableStateOf("10") }
    var unitCostText by remember { mutableStateOf(selectedProduct?.costPrice?.toString() ?: "150.0") }
    var paymentMode by remember { mutableStateOf(PaymentMode.CASH) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(20.dp)),
            color = cardBg
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("تسجيل فاتورة مشتريات وتوريد مخزون", color = primaryColor, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = CairoFont)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = supplierName,
                    onValueChange = { supplierName = it },
                    label = { Text("اسم المورد / الشركة الموردة", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Product Selection
                Text("اختر الصنف المراد توريده للمخزن:", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    products.take(3).forEach { prod ->
                        val isSel = selectedProduct?.id == prod.id
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                selectedProduct = prod
                                unitCostText = prod.costPrice.toString()
                            },
                            label = { Text(prod.name.take(12), fontSize = 10.sp, fontFamily = CairoFont) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("الكمية الموردة", fontFamily = CairoFont) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = unitCostText,
                        onValueChange = { unitCostText = it },
                        label = { Text("سعر التكلفة للوحدة", fontFamily = CairoFont) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sticky Save
                Button(
                    onClick = {
                        val qty = quantityText.toIntOrNull() ?: return@Button
                        val cost = unitCostText.toDoubleOrNull() ?: return@Button
                        val prod = selectedProduct ?: return@Button
                        val total = qty * cost

                        val invoiceNumber = "PUR-${System.currentTimeMillis() % 100000}"
                        val purchase = PurchaseInvoice(
                            id = invoiceNumber,
                            invoiceNumber = invoiceNumber,
                            supplierId = "sup_1",
                            supplierName = supplierName.trim().ifEmpty { "مورد عام" },
                            items = listOf(PurchaseItem(prod.id, prod.name, qty, cost, total)),
                            totalAmount = total,
                            paymentMode = paymentMode
                        )
                        onSave(purchase)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ فاتورة المشتريات وترحيل القيد", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                }
            }
        }
    }
}

@Composable
private fun AddReturnDialog(
    products: List<Product>,
    orders: List<SaleOrder>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onDismiss: () -> Unit,
    onSave: (ReturnInvoice) -> Unit
) {
    var returnType by remember { mutableStateOf(ReturnType.SALE_RETURN) }
    var partyName by remember { mutableStateOf("") }
    var selectedProduct by remember { mutableStateOf(products.firstOrNull()) }
    var quantityText by remember { mutableStateOf("1") }
    var priceText by remember { mutableStateOf(selectedProduct?.price?.toString() ?: "100.0") }
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(20.dp)),
            color = cardBg
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("تسجيل فاتورة مرتجع (مبيعات / مشتريات)", color = primaryColor, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = CairoFont)
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReturnType.values().forEach { t ->
                        FilterChip(
                            selected = returnType == t,
                            onClick = { returnType = t },
                            label = { Text(t.titleAr, fontSize = 11.sp, fontFamily = CairoFont) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    label = { Text("اسم العميل أو المورد", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("الكمية المرتجعة", fontFamily = CairoFont) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("المبلغ المرتجع", fontFamily = CairoFont) },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("سبب الإرجاع", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val qty = quantityText.toIntOrNull() ?: return@Button
                        val price = priceText.toDoubleOrNull() ?: return@Button
                        val prod = selectedProduct ?: return@Button
                        val total = qty * price

                        val retNumber = "RET-${System.currentTimeMillis() % 100000}"
                        val returnInv = ReturnInvoice(
                            id = retNumber,
                            returnNumber = retNumber,
                            type = returnType,
                            partyName = partyName.trim().ifEmpty { "عميل عام" },
                            items = listOf(ReturnItem(prod.id, prod.name, qty, price, total)),
                            totalAmount = total,
                            reason = reason.trim()
                        )
                        onSave(returnInv)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ المرتجع وترحيل القيد العكسي", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                }
            }
        }
    }
}
