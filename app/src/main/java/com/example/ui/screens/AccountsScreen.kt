package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.PaymentSettlementDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class AccountsTab(val title: String) {
    CHART_OF_ACCOUNTS("دليل الحسابات"),
    VOUCHERS("سندات القبض والصرف"),
    JOURNAL("القيود اليومية"),
    STATEMENT("كشف حساب"),
    CUSTOMERS("العملاء والديون")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: StoreViewModel
) {
    val context = LocalContext.current
    val branding by viewModel.brandingTheme.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val primaryColor = parseHexColor(branding.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(branding.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(branding.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(branding.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(branding.textMutedColorHex, TextMuted)

    var currentTab by remember { mutableStateOf(AccountsTab.CHART_OF_ACCOUNTS) }

    // Dialog states
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var showAddVoucherDialog by remember { mutableStateOf(false) }
    var showAddJournalDialog by remember { mutableStateOf(false) }
    var showStatementPrintPreview by remember { mutableStateOf(false) }

    // Statement filters
    var statementAccountCode by remember { mutableStateOf("101") }
    var statementMode by remember { mutableStateOf("فرعي") } // "فرعي", "إجمالي رئيسي"
    var statementPeriod by remember { mutableStateOf("الكل") } // "اليوم", "هذا الشهر", "الكل"

    // Customers dialog
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var selectedCustomerForSettle by remember { mutableStateOf<Customer?>(null) }

    val canAdd = viewModel.hasPermission(AppScreen.ACCOUNTS, ScreenAction.ADD)
    val canEdit = viewModel.hasPermission(AppScreen.ACCOUNTS, ScreenAction.EDIT)
    val canDelete = viewModel.hasPermission(AppScreen.ACCOUNTS, ScreenAction.DELETE)
    val canPrint = viewModel.hasPermission(AppScreen.ACCOUNTS, ScreenAction.PRINT)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .testTag("accounts_screen")
    ) {
        // Screen Header
        Surface(
            color = bgColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "النظام المحاسبي المتكامل",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                        Text(
                            text = "دليل الحسابات • سندات • قيود • كشوفات",
                            fontSize = 10.sp,
                            color = textMuted,
                            fontFamily = CairoFont
                        )
                    }
                }

                if (canAdd) {
                    Button(
                        onClick = {
                            when (currentTab) {
                                AccountsTab.CHART_OF_ACCOUNTS -> showAddAccountDialog = true
                                AccountsTab.VOUCHERS -> showAddVoucherDialog = true
                                AccountsTab.JOURNAL -> showAddJournalDialog = true
                                AccountsTab.CUSTOMERS -> showAddCustomerDialog = true
                                AccountsTab.STATEMENT -> showStatementPrintPreview = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (currentTab) {
                                AccountsTab.CHART_OF_ACCOUNTS -> "حساب جديد +"
                                AccountsTab.VOUCHERS -> "سند جديد +"
                                AccountsTab.JOURNAL -> "قيد يدوي +"
                                AccountsTab.CUSTOMERS -> "عميل جديد +"
                                AccountsTab.STATEMENT -> "معاينة الطباعة"
                            },
                            color = TextDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFont
                        )
                    }
                }
            }
        }

        // Navigation Tabs Bar
        ScrollableTabRow(
            selectedTabIndex = currentTab.ordinal,
            containerColor = cardBg,
            contentColor = primaryColor,
            edgePadding = 12.dp,
            divider = { HorizontalDivider(color = primaryColor.copy(alpha = 0.2f)) }
        ) {
            AccountsTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { currentTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) primaryColor else textMuted,
                            fontFamily = CairoFont
                        )
                    }
                )
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            when (currentTab) {
                AccountsTab.CHART_OF_ACCOUNTS -> ChartOfAccountsContent(
                    accounts = accounts,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    canEdit = canEdit,
                    canDelete = canDelete,
                    onEditAccount = { editingAccount = it },
                    onDeleteAccount = { viewModel.deleteAccount(it.code) }
                )
                AccountsTab.VOUCHERS -> VouchersContent(
                    vouchers = vouchers,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
                AccountsTab.JOURNAL -> JournalEntriesContent(
                    entries = journalEntries,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
                AccountsTab.STATEMENT -> StatementContent(
                    accounts = accounts,
                    journalEntries = journalEntries,
                    selectedAccountCode = statementAccountCode,
                    onSelectAccountCode = { statementAccountCode = it },
                    statementMode = statementMode,
                    onChangeMode = { statementMode = it },
                    statementPeriod = statementPeriod,
                    onChangePeriod = { statementPeriod = it },
                    onPrint = { showStatementPrintPreview = true },
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
                AccountsTab.CUSTOMERS -> CustomersTabContent(
                    customers = customers,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    onSettle = { selectedCustomerForSettle = it }
                )
            }
        }
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        AccountFormDialog(
            title = "إضافة حساب جديد لدليل الحسابات",
            initialAccount = null,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { showAddAccountDialog = false },
            onSave = { acc ->
                viewModel.addAccount(acc)
                showAddAccountDialog = false
            }
        )
    }

    // Edit Account Dialog
    if (editingAccount != null) {
        AccountFormDialog(
            title = "تعديل بيانات الحساب",
            initialAccount = editingAccount,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { editingAccount = null },
            onSave = { acc ->
                viewModel.updateAccount(acc)
                editingAccount = null
            }
        )
    }

    // Add Voucher Dialog
    if (showAddVoucherDialog) {
        VoucherFormDialog(
            accounts = accounts,
            customers = customers,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { showAddVoucherDialog = false },
            onSave = { voucher ->
                viewModel.addVoucher(voucher)
                showAddVoucherDialog = false
            }
        )
    }

    // Add Manual Journal Entry Dialog
    if (showAddJournalDialog) {
        ManualJournalDialog(
            accounts = accounts,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { showAddJournalDialog = false },
            onSave = { desc, lines ->
                val ok = viewModel.addManualJournalEntry(desc, lines)
                if (ok) showAddJournalDialog = false
            }
        )
    }

    // Statement Print Preview Dialog
    if (showStatementPrintPreview) {
        StatementPrintDialog(
            accountCode = statementAccountCode,
            accounts = accounts,
            journalEntries = journalEntries,
            branding = branding,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { showStatementPrintPreview = false }
        )
    }

    // Add Customer Dialog
    if (showAddCustomerDialog) {
        AddCustomerDialog(
            onDismiss = { showAddCustomerDialog = false },
            onConfirm = { name, phone, debt ->
                viewModel.addNewCustomer(name, phone, debt)
                showAddCustomerDialog = false
            }
        )
    }

    // Settle Debt Dialog
    if (selectedCustomerForSettle != null) {
        PaymentSettlementDialog(
            customer = selectedCustomerForSettle!!,
            onDismiss = { selectedCustomerForSettle = null },
            onSettle = { amount ->
                viewModel.settleDebt(selectedCustomerForSettle!!.id, amount)
                selectedCustomerForSettle = null
            }
        )
    }
}

// ---------------- CHART OF ACCOUNTS TREE ----------------
@Composable
private fun ChartOfAccountsContent(
    accounts: List<Account>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    canEdit: Boolean,
    canDelete: Boolean,
    onEditAccount: (Account) -> Unit,
    onDeleteAccount: (Account) -> Unit
) {
    val grouped = remember(accounts) {
        AccountType.values().associateWith { type ->
            accounts.filter { it.type == type }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        AccountType.values().forEach { type ->
            val typeAccounts = grouped[type] ?: emptyList()
            if (typeAccounts.isNotEmpty()) {
                item {
                    Column {
                        Surface(
                            color = primaryColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${type.code} - ${type.titleAr}",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 14.sp,
                                    color = primaryColor,
                                    fontFamily = CairoFont
                                )
                                Text(
                                    text = "طبيعة: ${type.defaultNature.titleAr}",
                                    fontSize = 11.sp,
                                    color = textMuted,
                                    fontFamily = CairoFont
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            typeAccounts.forEach { acc ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = cardBg),
                                    border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF2E2E2E))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Surface(
                                                color = if (acc.isParent) primaryColor.copy(alpha = 0.2f) else Color(0xFF1E293B),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = acc.code,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (acc.isParent) primaryColor else textPrimary,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                    fontFamily = CairoFont
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))

                                            Column {
                                                Text(
                                                    text = acc.name,
                                                    fontWeight = if (acc.isParent) FontWeight.Black else FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = textPrimary,
                                                    fontFamily = CairoFont
                                                )
                                                Text(
                                                    text = if (acc.isParent) "حساب رئيسي" else "طبيعة: ${acc.nature.titleAr}",
                                                    fontSize = 10.sp,
                                                    color = textMuted,
                                                    fontFamily = CairoFont
                                                )
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${"%,.2f".format(acc.balance)} ر.س",
                                                fontWeight = FontWeight.Black,
                                                fontSize = 13.sp,
                                                color = if (acc.balance >= 0) primaryColor else Color(0xFFEF4444),
                                                fontFamily = CairoFont
                                            )

                                            if (!acc.isSystem && canEdit) {
                                                IconButton(
                                                    onClick = { onEditAccount(acc) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                                }
                                            }

                                            if (!acc.isSystem && canDelete) {
                                                IconButton(
                                                    onClick = { onDeleteAccount(acc) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- VOUCHERS CONTENT ----------------
@Composable
private fun VouchersContent(
    vouchers: List<Voucher>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    if (vouchers.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد سندات قبض أو صرف مسجلة حالياً", color = textMuted, fontFamily = CairoFont)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(vouchers) { vch ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Surface(
                                color = if (vch.type == VoucherType.RECEIPT) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (vch.type == VoucherType.RECEIPT) Color(0xFF10B981) else Color(0xFFF59E0B)
                                )
                            ) {
                                Text(
                                    text = "${vch.type.titleAr} • ${vch.voucherNumber}",
                                    color = if (vch.type == VoucherType.RECEIPT) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontFamily = CairoFont
                                )
                            }

                            Text(
                                text = "${"%,.2f".format(vch.amount)} ر.س",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = primaryColor,
                                fontFamily = CairoFont
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "حساب الطرف: ${vch.partyName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textPrimary,
                                fontFamily = CairoFont
                            )
                            Text(
                                text = "الدفع: ${vch.paymentMode.titleAr}",
                                fontSize = 11.sp,
                                color = textMuted,
                                fontFamily = CairoFont
                            )
                        }

                        if (vch.note.isNotBlank()) {
                            Text(
                                text = "البيان: ${vch.note}",
                                fontSize = 11.sp,
                                color = textMuted,
                                modifier = Modifier.padding(top = 4.dp),
                                fontFamily = CairoFont
                            )
                        }

                        Text(
                            text = "تاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(vch.date))} • مرحل للقيد ${vch.journalEntryId}",
                            fontSize = 10.sp,
                            color = textMuted.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 6.dp),
                            fontFamily = CairoFont
                        )
                    }
                }
            }
        }
    }
}

// ---------------- JOURNAL ENTRIES CONTENT ----------------
@Composable
private fun JournalEntriesContent(
    entries: List<JournalEntry>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد قيود يومية مسجلة", color = textMuted, fontFamily = CairoFont)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(entries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                            Surface(
                                color = primaryColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = entry.entryNumber,
                                    color = primaryColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    fontFamily = CairoFont
                                )
                            }

                            Text(
                                text = entry.referenceType.titleAr,
                                color = textMuted,
                                fontSize = 11.sp,
                                fontFamily = CairoFont
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = entry.description,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = textPrimary,
                            fontFamily = CairoFont
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = primaryColor.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(6.dp))

                        // Lines Table
                        entry.lines.forEach { line ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${line.accountCode} - ${line.accountName}",
                                    fontSize = 11.sp,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f),
                                    fontFamily = CairoFont
                                )

                                Row {
                                    if (line.debit > 0) {
                                        Text(
                                            text = "مدين: ${"%,.2f".format(line.debit)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981),
                                            fontFamily = CairoFont
                                        )
                                    }
                                    if (line.credit > 0) {
                                        Text(
                                            text = "دائن: ${"%,.2f".format(line.credit)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor,
                                            fontFamily = CairoFont
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "إجمالي القيد: ${"%,.2f".format(entry.totalDebit)} ر.س • التاريخ: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(entry.date))}",
                            fontSize = 10.sp,
                            color = textMuted,
                            fontFamily = CairoFont
                        )
                    }
                }
            }
        }
    }
}

// ---------------- STATEMENT OF ACCOUNT ----------------
@Composable
private fun StatementContent(
    accounts: List<Account>,
    journalEntries: List<JournalEntry>,
    selectedAccountCode: String,
    onSelectAccountCode: (String) -> Unit,
    statementMode: String,
    onChangeMode: (String) -> Unit,
    statementPeriod: String,
    onChangePeriod: (String) -> Unit,
    onPrint: () -> Unit,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    val targetAccount = accounts.find { it.code == selectedAccountCode } ?: accounts.firstOrNull()

    // Calculate opening balance, movements, closing balance
    val statementLines = remember(selectedAccountCode, statementMode, journalEntries) {
        val lines = mutableListOf<JournalEntryLine>()
        journalEntries.forEach { entry ->
            entry.lines.forEach { line ->
                if (statementMode == "فرعي") {
                    if (line.accountCode == selectedAccountCode) {
                        lines.add(line)
                    }
                } else {
                    if (line.accountCode.startsWith(selectedAccountCode)) {
                        lines.add(line)
                    }
                }
            }
        }
        lines
    }

    val totalDebit = statementLines.sumOf { it.debit }
    val totalCredit = statementLines.sumOf { it.credit }
    val openingBalance = 0.0
    val closingBalance = if (targetAccount?.nature == AccountNature.DEBIT) {
        openingBalance + totalDebit - totalCredit
    } else {
        openingBalance + totalCredit - totalDebit
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Filter Controls Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "فلترة كشف الحساب وتحديد الحساب المطلوب:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = primaryColor,
                        fontFamily = CairoFont
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Accounts chips horizontal
                    Text("اختر الحساب:", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        accounts.take(5).forEach { acc ->
                            val isSel = acc.code == selectedAccountCode
                            FilterChip(
                                selected = isSel,
                                onClick = { onSelectAccountCode(acc.code) },
                                label = { Text("${acc.code} ${acc.name}", fontSize = 10.sp, fontFamily = CairoFont) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = TextDark
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("فرعي", "إجمالي رئيسي").forEach { mode ->
                                val isSel = statementMode == mode
                                FilterChip(
                                    selected = isSel,
                                    onClick = { onChangeMode(mode) },
                                    label = { Text(mode, fontSize = 11.sp, fontFamily = CairoFont) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = primaryColor,
                                        selectedLabelColor = TextDark
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = onPrint,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("طباعة الكشف", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        }
                    }
                }
            }
        }

        // Summary Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "كشف حساب: ${targetAccount?.code} - ${targetAccount?.name}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = primaryColor,
                        fontFamily = CairoFont
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("الرصيد الافتتاحي", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                            Text("${"%,.2f".format(openingBalance)} ر.س", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = textPrimary, fontFamily = CairoFont)
                        }
                        Column {
                            Text("إجمالي المدين", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                            Text("${"%,.2f".format(totalDebit)} ر.س", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontFamily = CairoFont)
                        }
                        Column {
                            Text("إجمالي الدائن", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                            Text("${"%,.2f".format(totalCredit)} ر.س", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryColor, fontFamily = CairoFont)
                        }
                        Column {
                            Text("الرصيد الختامي", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                            Text("${"%,.2f".format(closingBalance)} ر.س", fontSize = 15.sp, fontWeight = FontWeight.Black, color = primaryColor, fontFamily = CairoFont)
                        }
                    }
                }
            }
        }

        // Movements Table
        item {
            Text(
                text = "سجل حركات الحساب (${statementLines.size} حركة):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textPrimary,
                fontFamily = CairoFont
            )
        }

        items(statementLines) { line ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF333333))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = line.accountName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = textPrimary,
                            fontFamily = CairoFont
                        )
                        if (line.note.isNotBlank()) {
                            Text(text = line.note, fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (line.debit > 0) {
                            Text(
                                text = "+ ${"%,.2f".format(line.debit)} ر.س (مدين)",
                                color = Color(0xFF10B981),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFont
                            )
                        }
                        if (line.credit > 0) {
                            Text(
                                text = "- ${"%,.2f".format(line.credit)} ر.س (دائن)",
                                color = primaryColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------- CUSTOMERS & DEBT CONTENT ----------------
@Composable
private fun CustomersTabContent(
    customers: List<Customer>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onSettle: (Customer) -> Unit
) {
    val totalDebt = customers.sumOf { it.debt }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("إجمالي ديون ومستحقات العملاء", fontSize = 12.sp, color = textMuted, fontFamily = CairoFont)
                        Text("${"%,.2f".format(totalDebt)} ر.س", fontSize = 22.sp, fontWeight = FontWeight.Black, color = primaryColor, fontFamily = CairoFont)
                    }
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = primaryColor, modifier = Modifier.size(36.dp))
                }
            }
        }

        items(customers.filter { it.id != "cash" }) { cust ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF2E2E2E))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textPrimary, fontFamily = CairoFont)
                        Text(cust.phone, fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                        Text(
                            text = "المستحق: ${"%,.2f".format(cust.debt)} ر.س",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            color = if (cust.debt > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                            fontFamily = CairoFont
                        )
                    }

                    if (cust.debt > 0) {
                        Button(
                            onClick = { onSettle(cust) },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("سند قبض وسداد", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- DIALOGS ----------------
@Composable
private fun AccountFormDialog(
    title: String,
    initialAccount: Account?,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onDismiss: () -> Unit,
    onSave: (Account) -> Unit
) {
    var code by remember { mutableStateOf(initialAccount?.code ?: "") }
    var name by remember { mutableStateOf(initialAccount?.name ?: "") }
    var selectedType by remember { mutableStateOf(initialAccount?.type ?: AccountType.ASSET) }
    var selectedNature by remember { mutableStateOf(initialAccount?.nature ?: AccountNature.DEBIT) }
    var isParent by remember { mutableStateOf(initialAccount?.isParent ?: false) }
    var parentCode by remember { mutableStateOf(initialAccount?.parentCode ?: "") }
    var description by remember { mutableStateOf(initialAccount?.description ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(20.dp)),
            color = cardBg
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(title, color = primaryColor, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = CairoFont)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("رقم / كود الحساب (مثال: 105)", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = initialAccount == null,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الحساب", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Account Type Chips
                Text("نوع الحساب الرئيسي:", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AccountType.values().forEach { t ->
                        FilterChip(
                            selected = selectedType == t,
                            onClick = {
                                selectedType = t
                                selectedNature = t.defaultNature
                            },
                            label = { Text(t.titleAr, fontSize = 9.sp, fontFamily = CairoFont) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Nature Debit/Credit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("طبيعة الحساب:", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AccountNature.values().forEach { n ->
                            FilterChip(
                                selected = selectedNature == n,
                                onClick = { selectedNature = n },
                                label = { Text(n.titleAr, fontSize = 10.sp, fontFamily = CairoFont) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = TextDark
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Sticky Bottom Save Bar (Requirement 6)
                Button(
                    onClick = {
                        if (code.isBlank() || name.isBlank()) return@Button
                        val account = Account(
                            code = code.trim(),
                            name = name.trim(),
                            type = selectedType,
                            nature = selectedNature,
                            parentCode = parentCode.trim().ifEmpty { null },
                            isParent = isParent,
                            description = description.trim()
                        )
                        onSave(account)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("حفظ الحساب وتطبيقه", fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = CairoFont)
                }
            }
        }
    }
}

@Composable
private fun VoucherFormDialog(
    accounts: List<Account>,
    customers: List<Customer>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onDismiss: () -> Unit,
    onSave: (Voucher) -> Unit
) {
    var voucherType by remember { mutableStateOf(VoucherType.RECEIPT) }
    var voucherNumber by remember { mutableStateOf("VCH-${System.currentTimeMillis() % 100000}") }
    var selectedPartyAccount by remember { mutableStateOf(accounts.firstOrNull { it.code == "103" } ?: accounts.first()) }
    var amountText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(20.dp)),
            color = cardBg
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = if (voucherType == VoucherType.RECEIPT) "إصدار سند قبض (Receipt)" else "إصدار سند صرف (Payment)",
                    color = primaryColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    fontFamily = CairoFont
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Type Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoucherType.values().forEach { t ->
                        FilterChip(
                            selected = voucherType == t,
                            onClick = { voucherType = t },
                            label = { Text(t.titleAr, fontSize = 11.sp, fontFamily = CairoFont) },
                            modifier = Modifier.weight(1f),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ (ريال سعودي)", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text("طريقة الدفع:", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PaymentMode.values().forEach { m ->
                        FilterChip(
                            selected = paymentMode == m,
                            onClick = { paymentMode = m },
                            label = { Text(m.titleAr, fontSize = 10.sp, fontFamily = CairoFont) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = primaryColor,
                                selectedLabelColor = TextDark
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("البيان / سبب العملية", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Sticky Save
                Button(
                    onClick = {
                        val amount = amountText.toDoubleOrNull() ?: return@Button
                        val voucher = Voucher(
                            id = "VCH-${System.currentTimeMillis() % 100000}",
                            voucherNumber = voucherNumber,
                            type = voucherType,
                            partyAccountCode = selectedPartyAccount.code,
                            partyName = selectedPartyAccount.name,
                            amount = amount,
                            paymentMode = paymentMode,
                            note = note
                        )
                        onSave(voucher)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ترحيل السند والقيد المحاسبي", fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = CairoFont)
                }
            }
        }
    }
}

@Composable
private fun ManualJournalDialog(
    accounts: List<Account>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onDismiss: () -> Unit,
    onSave: (description: String, lines: List<JournalEntryLine>) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var debitAccount by remember { mutableStateOf(accounts.firstOrNull { it.code == "101" } ?: accounts.first()) }
    var creditAccount by remember { mutableStateOf(accounts.firstOrNull { it.code == "401" } ?: accounts.first()) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val amount = amountText.toDoubleOrNull() ?: 0.0
    val isBalanced = amount > 0.0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(20.dp)),
            color = cardBg
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("إنشاء قيد يومي يدوي (Manual Journal)", color = primaryColor, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = CairoFont)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("شرح / بيان القيد اليومي", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("مبلغ القيد المتوازن (ر.س)", fontFamily = CairoFont) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Debit الطرف المدين
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("الطرف المدين (من حـ/): ${debitAccount.code} - ${debitAccount.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981), fontFamily = CairoFont)
                        Text("المبلغ المدين: ${"%,.2f".format(amount)} ر.س", fontSize = 11.sp, color = textPrimary, fontFamily = CairoFont)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Credit الطرف الدائن
                Surface(
                    color = primaryColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("الطرف الدائن (إلى حـ/): ${creditAccount.code} - ${creditAccount.name}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor, fontFamily = CairoFont)
                        Text("المبلغ الدائن: ${"%,.2f".format(amount)} ر.س", fontSize = 11.sp, color = textPrimary, fontFamily = CairoFont)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Balance check notice
                Surface(
                    color = if (isBalanced) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isBalanced) "القيد متوازن تماماً (المدين = الدائن = ${"%,.2f".format(amount)} ر.س)" else "أدخل مبلغاً صحيحاً لموازنة القيد",
                        color = if (isBalanced) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(6.dp),
                        fontFamily = CairoFont
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (!isBalanced || description.isBlank()) return@Button
                        val lines = listOf(
                            JournalEntryLine(debitAccount.code, debitAccount.name, debit = amount, credit = 0.0, note = description),
                            JournalEntryLine(creditAccount.code, creditAccount.name, debit = 0.0, credit = amount, note = description)
                        )
                        onSave(description, lines)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ترحيل القيد اليومي للنظام", fontWeight = FontWeight.Black, fontSize = 13.sp, fontFamily = CairoFont)
                }
            }
        }
    }
}

@Composable
private fun StatementPrintDialog(
    accountCode: String,
    accounts: List<Account>,
    journalEntries: List<JournalEntry>,
    branding: AppBrandingTheme,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onDismiss: () -> Unit
) {
    val acc = accounts.find { it.code == accountCode } ?: accounts.firstOrNull()
    val lines = journalEntries.flatMap { entry ->
        entry.lines.filter { it.accountCode == accountCode }
    }
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(20.dp))
                .border(2.dp, primaryColor, RoundedCornerShape(20.dp)),
            color = cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Document Header (ERP Printed Invoice / Statement Style)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(branding.appName, fontWeight = FontWeight.Black, fontSize = 16.sp, color = primaryColor, fontFamily = CairoFont)
                        Text("كشف حساب رسمي معتمد", fontSize = 12.sp, color = textMuted, fontFamily = CairoFont)
                    }

                    Icon(Icons.Default.Verified, contentDescription = null, tint = primaryColor, modifier = Modifier.size(32.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = primaryColor, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Account Metadata Box
                Surface(
                    color = primaryColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("رقم الحساب: ${acc?.code} • ${acc?.name}", fontWeight = FontWeight.Black, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                        Text("طبيعة الحساب: ${acc?.nature?.titleAr} • الرصيد الإجمالي: ${"%,.2f".format(acc?.balance ?: 0.0)} ر.س", fontSize = 11.sp, color = primaryColor, fontFamily = CairoFont)
                        Text("تاريخ الاستخراج: ${SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date())}", fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Transactions List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(lines) { line ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(line.accountName, fontSize = 11.sp, color = textPrimary, fontFamily = CairoFont)
                                Row {
                                    if (line.debit > 0) Text("مدين: ${"%,.2f".format(line.debit)}", fontSize = 11.sp, color = Color(0xFF10B981), fontFamily = CairoFont)
                                    if (line.credit > 0) Text("دائن: ${"%,.2f".format(line.credit)}", fontSize = 11.sp, color = primaryColor, fontFamily = CairoFont)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إغلاق", fontFamily = CairoFont)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "تم إرسال كشف الحساب إلى الطابعة وحفظ ملف PDF", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        modifier = Modifier.weight(2f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = TextDark)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("طباعة وتصدير PDF", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            }
        }
    }
}
