package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppScreen
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel

enum class MoreSubScreen {
    MAIN,
    USERS,
    REPORTS,
    ACCOUNTS,
    SETTINGS,
    INVENTORY,
    ABOUT
}

@Composable
fun MoreScreen(
    viewModel: StoreViewModel,
    onNavigateToUsers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onOpenLoginDialog: () -> Unit,
    onOpenSyncDialog: () -> Unit
) {
    val branding by viewModel.brandingTheme.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()

    val primaryColor = parseHexColor(branding.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(branding.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(branding.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(branding.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(branding.textMutedColorHex, TextMuted)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("more_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Surface(
                color = bgColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(primaryColor.copy(alpha = 0.15f))
                                .border(1.dp, primaryColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Widgets, contentDescription = null, tint = primaryColor, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("المزيد من الخدمات المحاسبية", color = textPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                            Text(branding.appName, color = primaryColor, fontSize = 11.sp, fontFamily = CairoFont)
                        }
                    }

                    // Sync Status chip
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onOpenSyncDialog)
                            .testTag("more_sync_badge"),
                        shape = RoundedCornerShape(20.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("متزامن", color = primaryColor, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        }
                    }
                }
            }
        }

        // Current User Profile Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f))
                                    .border(1.5.dp, primaryColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (currentUser?.role) {
                                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                        UserRole.ACCOUNTANT -> Icons.Default.AccountBalance
                                        UserRole.CASHIER -> Icons.Default.PointOfSale
                                        UserRole.STOREKEEPER -> Icons.Default.Inventory
                                        else -> Icons.Default.Person
                                    },
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = currentUser?.fullName ?: "غير مسجل دخول",
                                    color = textPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFont
                                )
                                Text(
                                    text = "@${currentUser?.username ?: "guest"} • ${currentUser?.role?.titleAr ?: "زائر"}",
                                    color = primaryColor,
                                    fontSize = 11.sp,
                                    fontFamily = CairoFont
                                )
                            }
                        }

                        // Role badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor)
                        ) {
                            Text(
                                text = currentUser?.role?.titleAr ?: "مستخدم",
                                color = primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = CairoFont,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = primaryColor.copy(alpha = 0.15f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.testTag("logout_button")
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = LuxuryRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسجيل الخروج", color = LuxuryRed, fontSize = 11.sp, fontFamily = CairoFont)
                        }

                        TextButton(
                            onClick = onOpenLoginDialog,
                            modifier = Modifier.testTag("switch_user_button")
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تبديل الحساب", color = primaryColor, fontSize = 11.sp, fontFamily = CairoFont)
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            Text(
                text = "الأقسام المتاحة حسب الصلاحيات",
                color = textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFont,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Menu Item: Chart of Accounts & General Accounting (Accounts)
        if (viewModel.canAccessScreen(AppScreen.ACCOUNTS)) {
            item {
                MoreMenuCard(
                    title = "شجرة الحسابات والعمليات المحاسبية",
                    subtitle = "دليل الحسابات، سندات القبض والصرف، القيود اليومية، وكشف الحساب التفصيلي",
                    icon = Icons.Default.AccountBalance,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    onClick = onNavigateToAccounts
                )
            }
        }

        // Menu Item: Reports & Financial Statements
        if (viewModel.canAccessScreen(AppScreen.REPORTS)) {
            item {
                MoreMenuCard(
                    title = "التقارير المالية والأرباح",
                    subtitle = "ميزان المراجعة، قائمة الدخل، الميزانية العمومية، وحركة المخزون",
                    icon = Icons.Default.Analytics,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    onClick = onNavigateToReports
                )
            }
        }

        // Menu Item: Users & Permissions Management
        if (viewModel.canAccessScreen(AppScreen.USERS)) {
            item {
                MoreMenuCard(
                    title = "إدارة المستخدمين والصلاحيات المتقدمة",
                    subtitle = "تحديد الصلاحيات الدقيقة لكل مستخدم (الشاشات والعمليات: عرض، إضافة، تعديل، حذف)",
                    icon = Icons.Default.ManageAccounts,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    onClick = onNavigateToUsers
                )
            }
        }

        // Menu Item: Inventory Management
        if (viewModel.canAccessScreen(AppScreen.INVENTORY)) {
            item {
                MoreMenuCard(
                    title = "إدارة المستودع والجرد السلعي",
                    subtitle = "إضافة وتعديل الأصناف وتتبع حركة المخزون ورصيد التكلفة والجملة",
                    icon = Icons.Default.Inventory2,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    onClick = onNavigateToInventory
                )
            }
        }

        // Menu Item: Settings & Themes (Settings)
        if (viewModel.canAccessScreen(AppScreen.SETTINGS)) {
            item {
                MoreMenuCard(
                    title = "تخصيص الثيمات والهوية والإعدادات",
                    subtitle = "تغيير اسم التطبيق والشعار والألوان (3 ثيمات جاهزة)، النسخ الاحتياطي، وإعدادات Firebase",
                    icon = Icons.Default.Settings,
                    primaryColor = primaryColor,
                    cardBg = cardBg,
                    textPrimary = textPrimary,
                    textMuted = textMuted,
                    onClick = onNavigateToSettings
                )
            }
        }

        // Menu Item: About App & Designer
        item {
            MoreMenuCard(
                title = "عن التطبيق والمصمم",
                subtitle = "بيانات الإصدار، اسم المصمم: متجر الفيصل للتجارة العامة، ومعلومات الاتصال والدعم",
                icon = Icons.Default.Info,
                primaryColor = primaryColor,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted,
                onClick = onNavigateToAbout
            )
        }
    }
}

@Composable
private fun MoreMenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("more_item_${title}"),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.15f))
                    .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = CairoFont
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = textMuted,
                    fontSize = 11.sp,
                    fontFamily = CairoFont
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
