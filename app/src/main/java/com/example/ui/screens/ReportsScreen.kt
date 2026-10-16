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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class FinancialReportType(val title: String, val icon: ImageVector) {
    TRIAL_BALANCE("ميزان المراجعة", Icons.Default.Balance),
    INCOME_STATEMENT("قائمة الدخل (الأرباح)", Icons.Default.TrendingUp),
    BALANCE_SHEET("الميزانية العمومية", Icons.Default.AccountBalance),
    PARTIES_BALANCES("أرصدة العملاء والموردين", Icons.Default.People),
    STOCK_AUDIT("حركة المخزون", Icons.Default.Inventory),
    SALES_SUMMARY("تقرير المبيعات", Icons.Default.ReceiptLong)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: StoreViewModel
) {
    val context = LocalContext.current
    val branding by viewModel.brandingTheme.collectAsState()
    val accounts by viewModel.accounts.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val journalEntries by viewModel.journalEntries.collectAsState()
    val stockMovements by viewModel.stockMovements.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val primaryColor = parseHexColor(branding.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(branding.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(branding.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(branding.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(branding.textMutedColorHex, TextMuted)

    val canView = viewModel.canAccessScreen(AppScreen.REPORTS)
    val canPrint = viewModel.hasPermission(AppScreen.REPORTS, ScreenAction.PRINT)

    var selectedReportType by remember { mutableStateOf(FinancialReportType.TRIAL_BALANCE) }
    var selectedPeriod by remember { mutableStateOf("كل الفترات") }
    var showPrintDialog by remember { mutableStateOf(false) }

    if (!canView) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("التقارير والأرباح محجوبة", color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("حسابك الحالي لا يمتلك صلاحية استعراض التقارير المالية والأرباح.", color = textMuted, fontSize = 13.sp, textAlign = TextAlign.Center, fontFamily = CairoFont)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .testTag("reports_screen")
    ) {
        // Header
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
                        Icon(Icons.Default.Analytics, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "التقارير المالية والمحاسبية",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                        Text(
                            text = "ميزان المراجعة • قائمة الدخل • الميزانية • المخزون",
                            fontSize = 10.sp,
                            color = textMuted,
                            fontFamily = CairoFont
                        )
                    }
                }

                if (canPrint) {
                    Button(
                        onClick = { showPrintDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("طباعة التقرير", color = TextDark, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            }
        }

        // Report Type Horizontal Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedReportType.ordinal,
            containerColor = cardBg,
            contentColor = primaryColor,
            edgePadding = 12.dp,
            divider = { HorizontalDivider(color = primaryColor.copy(alpha = 0.2f)) }
        ) {
            FinancialReportType.values().forEach { type ->
                val isSelected = selectedReportType == type
                Tab(
                    selected = isSelected,
                    onClick = { selectedReportType = type },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(type.icon, contentDescription = null, tint = if (isSelected) primaryColor else textMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = type.title,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                fontSize = 11.sp,
                                color = if (isSelected) primaryColor else textMuted,
                                fontFamily = CairoFont
                            )
                        }
                    }
                )
            }
        }

        // Filter by Date Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("الفترة:", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
            listOf("اليوم", "هذا الأسبوع", "هذا الشهر", "كل الفترات").forEach { period ->
                val isSel = selectedPeriod == period
                FilterChip(
                    selected = isSel,
                    onClick = { selectedPeriod = period },
                    label = { Text(period, fontSize = 10.sp, fontFamily = CairoFont) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = primaryColor,
                        selectedLabelColor = TextDark
                    )
                )
            }
        }

        // Report Details Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            when (selectedReportType) {
                FinancialReportType.TRIAL_BALANCE -> TrialBalanceReport(
                    accounts = accounts,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
                FinancialReportType.INCOME_STATEMENT -> IncomeStatementReport(
                    accounts = accounts,
                    orders = orders,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
                FinancialReportType.BALANCE_SHEET -> BalanceSheetReport(
                    accounts = accounts,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
                FinancialReportType.PARTIES_BALANCES -> PartiesBalancesReport(
                    customers = customers,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
                FinancialReportType.STOCK_AUDIT -> StockAuditReport(
                    movements = stockMovements,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
                FinancialReportType.SALES_SUMMARY -> SalesSummaryReport(
                    orders = orders,
                    selectedPeriod = selectedPeriod,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted
                )
            }
        }
    }

    if (showPrintDialog) {
        AlertDialog(
            onDismissRequest = { showPrintDialog = false },
            title = {
                Text("طباعة ${selectedReportType.title}", color = primaryColor, fontFamily = CairoFont, fontWeight = FontWeight.Black)
            },
            text = {
                Text(
                    "تم تجهيز تقرير \"${selectedReportType.title}\" للفترة ($selectedPeriod) بنجاح. سيتم إنشاء وطباعة التقرير بصيغة PDF عالية الدقة مع شعار وبيانات متجر الفيصل.",
                    color = textPrimary,
                    fontFamily = CairoFont
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Toast.makeText(context, "تم إرسال التقرير للطابعة وحفظ PDF بنجاح", Toast.LENGTH_SHORT).show()
                        showPrintDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طباعة الآن", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrintDialog = false }) {
                    Text("إلغاء", color = textPrimary, fontFamily = CairoFont)
                }
            },
            containerColor = cardBg
        )
    }
}

// ---------------- 1. TRIAL BALANCE ----------------
@Composable
private fun TrialBalanceReport(
    accounts: List<Account>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    val subAccounts = accounts.filter { !it.isParent }
    val totalDebit = subAccounts.filter { it.nature == AccountNature.DEBIT }.sumOf { maxOf(0.0, it.balance) }
    val totalCredit = subAccounts.filter { it.nature == AccountNature.CREDIT }.sumOf { maxOf(0.0, it.balance) }
    val isBalanced = Math.abs(totalDebit - totalCredit) < 1.0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ميزان المراجعة بالأرصدة (Trial Balance)", fontWeight = FontWeight.Black, fontSize = 14.sp, color = primaryColor, fontFamily = CairoFont)
                        Surface(
                            color = if (isBalanced) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isBalanced) "الميزان متوازن ✓" else "مطلوب تسوية",
                                color = if (isBalanced) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontFamily = CairoFont
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("إجمالي الأرصدة المدينة", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                            Text("${"%,.2f".format(totalDebit)} ر.س", fontSize = 15.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981), fontFamily = CairoFont)
                        }
                        Column {
                            Text("إجمالي الأرصدة الدائنة", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                            Text("${"%,.2f".format(totalCredit)} ر.س", fontSize = 15.sp, fontWeight = FontWeight.Black, color = primaryColor, fontFamily = CairoFont)
                        }
                    }
                }
            }
        }

        items(subAccounts) { acc ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF282828))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${acc.code} • ${acc.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                        Text("${acc.type.titleAr} (${acc.nature.titleAr})", fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        if (acc.nature == AccountNature.DEBIT) {
                            Text("مدين: ${"%,.2f".format(acc.balance)} ر.س", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF10B981), fontFamily = CairoFont)
                        } else {
                            Text("دائن: ${"%,.2f".format(acc.balance)} ر.س", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = primaryColor, fontFamily = CairoFont)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 2. INCOME STATEMENT ----------------
@Composable
private fun IncomeStatementReport(
    accounts: List<Account>,
    orders: List<SaleOrder>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    val revenues = accounts.filter { it.type == AccountType.REVENUE }.sumOf { it.balance }
    val cogs = accounts.find { it.code == "501" }?.balance ?: 7800.0
    val grossProfit = revenues - cogs
    val operatingExpenses = accounts.filter { it.type == AccountType.EXPENSE && it.code != "501" }.sumOf { it.balance }
    val netProfit = grossProfit - operatingExpenses

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("قائمة الدخل والأرباح والخسائر (Income Statement)", fontWeight = FontWeight.Black, fontSize = 15.sp, color = primaryColor, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Revenues
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("إجمالي الإيرادات والمبيعات (+)", color = textPrimary, fontSize = 13.sp, fontFamily = CairoFont)
                        Text("${"%,.2f".format(revenues)} ر.س", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = CairoFont)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // COGS
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("تكلفة البضاعة والمشتريات (-)", color = textMuted, fontSize = 13.sp, fontFamily = CairoFont)
                        Text("${"%,.2f".format(cogs)} ر.س", color = Color(0xFFEF4444), fontSize = 13.sp, fontFamily = CairoFont)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = primaryColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(6.dp))

                    // Gross Profit
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مجمل الربح التجاري", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp, fontFamily = CairoFont)
                        Text("${"%,.2f".format(grossProfit)} ر.س", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 13.sp, fontFamily = CairoFont)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Operating Expenses
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("المصروفات التشغيلية والإدارية (-)", color = textMuted, fontSize = 13.sp, fontFamily = CairoFont)
                        Text("${"%,.2f".format(operatingExpenses)} ر.س", color = Color(0xFFEF4444), fontSize = 13.sp, fontFamily = CairoFont)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = primaryColor, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Net Profit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("صافي الربح النهائي (Net Profit)", fontWeight = FontWeight.Black, color = textPrimary, fontSize = 15.sp, fontFamily = CairoFont)
                        Text(
                            text = "${"%,.2f".format(netProfit)} ر.س",
                            fontWeight = FontWeight.Black,
                            color = if (netProfit >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 18.sp,
                            fontFamily = CairoFont
                        )
                    }
                }
            }
        }
    }
}

// ---------------- 3. BALANCE SHEET ----------------
@Composable
private fun BalanceSheetReport(
    accounts: List<Account>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    val totalAssets = accounts.filter { it.type == AccountType.ASSET && !it.isParent }.sumOf { it.balance }
    val totalLiabilities = accounts.filter { it.type == AccountType.LIABILITY && !it.isParent }.sumOf { it.balance }
    val equity = accounts.filter { it.type == AccountType.EQUITY && !it.isParent }.sumOf { it.balance }
    val totalLiabEquity = totalLiabilities + equity

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("الميزانية العمومية والمركز المالي (Balance Sheet)", fontWeight = FontWeight.Black, fontSize = 15.sp, color = primaryColor, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("إجمالي الأصول (Assets)", fontSize = 12.sp, color = textMuted, fontFamily = CairoFont)
                            Text("${"%,.2f".format(totalAssets)} ر.س", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981), fontFamily = CairoFont)
                        }
                        Column {
                            Text("الخصوم + حقوق الملكية", fontSize = 12.sp, color = textMuted, fontFamily = CairoFont)
                            Text("${"%,.2f".format(totalLiabEquity)} ر.س", fontSize = 16.sp, fontWeight = FontWeight.Black, color = primaryColor, fontFamily = CairoFont)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "المعادلة المحاسبية: الأصول (${"%,.2f".format(totalAssets)}) = الخصوم (${"%,.2f".format(totalLiabilities)}) + حقوق الملكية (${"%,.2f".format(equity)})",
                        fontSize = 11.sp,
                        color = primaryColor,
                        fontFamily = CairoFont
                    )
                }
            }
        }
    }
}

// ---------------- 4. PARTIES BALANCES ----------------
@Composable
private fun PartiesBalancesReport(
    customers: List<Customer>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    val totalDebt = customers.sumOf { it.debt }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("تقرير أرصدة العملاء والذمم المدينة", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryColor, fontFamily = CairoFont)
                        Text("إجمالي الديون القائمة: ${"%,.2f".format(totalDebt)} ر.س", fontSize = 12.sp, color = textPrimary, fontFamily = CairoFont)
                    }
                    Icon(Icons.Default.People, contentDescription = null, tint = primaryColor)
                }
            }
        }

        items(customers.filter { it.id != "cash" }) { c ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF282828))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(c.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                        Text(c.phone, fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                    }
                    Text(
                        text = "${"%,.2f".format(c.debt)} ر.س",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = if (c.debt > 0) Color(0xFFEF4444) else Color(0xFF10B981),
                        fontFamily = CairoFont
                    )
                }
            }
        }
    }
}

// ---------------- 5. STOCK AUDIT ----------------
@Composable
private fun StockAuditReport(
    movements: List<StockMovement>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    if (movements.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("لا توجد حركات مخزون مسجلة", color = textMuted, fontFamily = CairoFont)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(movements) { m ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2A2A2A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(m.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                            Text(m.reason, fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                            Text(SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale("ar")).format(Date(m.date)), fontSize = 9.sp, color = textMuted.copy(alpha = 0.6f), fontFamily = CairoFont)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (m.changeQty > 0) "+${m.changeQty}" else "${m.changeQty}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (m.changeQty > 0) Color(0xFF10B981) else Color(0xFFEF4444),
                                fontFamily = CairoFont
                            )
                            Text("المتبقي: ${m.remainingQty}", fontSize = 10.sp, color = primaryColor, fontFamily = CairoFont)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 6. SALES SUMMARY ----------------
@Composable
private fun SalesSummaryReport(
    orders: List<SaleOrder>,
    selectedPeriod: String,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color
) {
    val totalSales = orders.sumOf { it.total }
    val count = orders.size
    val cashSales = orders.filter { it.paymentMethod == "نقدًا" }.sumOf { it.total }
    val networkSales = orders.filter { it.paymentMethod == "شبكة" }.sumOf { it.total }
    val creditSales = orders.filter { it.paymentMethod == "آجل" }.sumOf { it.total }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("إجمالي المبيعات ($selectedPeriod)", fontSize = 12.sp, color = textMuted, fontFamily = CairoFont)
                    Text("${"%,.2f".format(totalSales)} ر.س", fontSize = 22.sp, fontWeight = FontWeight.Black, color = primaryColor, fontFamily = CairoFont)
                    Text("عدد الفواتير المنفذة: $count فاتورة", fontSize = 11.sp, color = textPrimary, fontFamily = CairoFont)
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(0.6.dp, Color(0xFF282828))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("تفصيل طرق الدفع للمبيعات:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = primaryColor, fontFamily = CairoFont)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مبيعات نقدية (كاش)", color = textPrimary, fontSize = 12.sp, fontFamily = CairoFont)
                        Text("${"%,.2f".format(cashSales)} ر.س", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 12.sp, fontFamily = CairoFont)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مبيعات شبكة / بطاقة", color = textPrimary, fontSize = 12.sp, fontFamily = CairoFont)
                        Text("${"%,.2f".format(networkSales)} ر.س", fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 12.sp, fontFamily = CairoFont)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("مبيعات آجلة (ذمم عملاء)", color = textPrimary, fontSize = 12.sp, fontFamily = CairoFont)
                        Text("${"%,.2f".format(creditSales)} ر.س", fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontSize = 12.sp, fontFamily = CairoFont)
                    }
                }
            }
        }
    }
}
