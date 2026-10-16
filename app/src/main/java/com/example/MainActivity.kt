package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppScreen
import com.example.ui.components.LoginDialog
import com.example.ui.components.SyncStatusDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel

enum class AppTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    HOME("الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
    POS("نقطة البيع", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart, "tab_pos"),
    PRODUCTS("الأصناف", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, "tab_products"),
    INVOICES("الفواتير", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "tab_invoices"),
    MORE("المزيد", Icons.Filled.Widgets, Icons.Outlined.Widgets, "tab_more")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: StoreViewModel = viewModel()
            val brandingTheme by viewModel.brandingTheme.collectAsState()

            MyApplicationTheme(brandingTheme = brandingTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    val context = LocalContext.current
                    val currentUser by viewModel.currentUser.collectAsState()

                    val primaryColor = parseHexColor(brandingTheme.primaryColorHex, LuxuryGold)
                    val bgColor = parseHexColor(brandingTheme.backgroundColorHex, DarkBackground)
                    val cardBg = parseHexColor(brandingTheme.cardBackgroundColorHex, DarkCardBackground)
                    val textPrimary = parseHexColor(brandingTheme.textPrimaryColorHex, TextWhite)
                    val textMuted = parseHexColor(brandingTheme.textMutedColorHex, TextMuted)

                    var currentTab by remember { mutableStateOf(AppTab.HOME) }
                    var moreSubScreen by remember { mutableStateOf(MoreSubScreen.MAIN) }

                    val cart by viewModel.cart.collectAsState()
                    val cartCount = cart.sumOf { it.quantity }

                    var showAddProductModal by remember { mutableStateOf(false) }
                    var showLoginModal by remember { mutableStateOf(false) }
                    var showSyncModal by remember { mutableStateOf(false) }

                    val editingProduct by viewModel.editingProduct.collectAsState()
                    val notice by viewModel.notice.collectAsState()

                    // Toast/Snackbar notice when actions complete
                    LaunchedEffect(notice) {
                        if (notice != null) {
                            Toast.makeText(context, notice, Toast.LENGTH_SHORT).show()
                            viewModel.dismissNotice()
                        }
                    }

                    LaunchedEffect(editingProduct) {
                        if (editingProduct != null) {
                            showAddProductModal = true
                        }
                    }

                    // Dynamically filter bottom bar tabs according to RBAC permissions
                    val allowedTabs = remember(currentUser) {
                        try {
                            val filtered = AppTab.values().filter { tab ->
                                when (tab) {
                                    AppTab.HOME -> viewModel.canAccessScreen(AppScreen.HOME)
                                    AppTab.POS -> viewModel.canAccessScreen(AppScreen.POS)
                                    AppTab.PRODUCTS -> viewModel.canAccessScreen(AppScreen.PRODUCTS)
                                    AppTab.INVOICES -> viewModel.canAccessScreen(AppScreen.INVOICES)
                                    AppTab.MORE -> true
                                }
                            }
                            if (filtered.isEmpty()) AppTab.values().toList() else filtered
                        } catch (e: Throwable) {
                            AppTab.values().toList()
                        }
                    }

                    // Ensure current tab is within allowed tabs
                    LaunchedEffect(allowedTabs) {
                        if (!allowedTabs.contains(currentTab) && allowedTabs.isNotEmpty()) {
                            currentTab = allowedTabs.first()
                        }
                    }

                    if (currentUser == null) {
                        // First screen always renders the Login screen even if localStorage is empty or corrupted
                        LoginScreen(
                            viewModel = viewModel,
                            onLoginSuccess = {
                                currentTab = AppTab.HOME
                            }
                        )
                    } else {
                        // BackHandler returns from sub-screen to main MORE screen, or to HOME
                        BackHandler(
                            enabled = currentTab != AppTab.HOME ||
                                    showAddProductModal ||
                                    showLoginModal ||
                                    showSyncModal ||
                                    (currentTab == AppTab.MORE && moreSubScreen != MoreSubScreen.MAIN)
                        ) {
                            when {
                                showAddProductModal -> {
                                    viewModel.clearEditingProduct()
                                    showAddProductModal = false
                                }
                                showLoginModal -> showLoginModal = false
                                showSyncModal -> showSyncModal = false
                                currentTab == AppTab.MORE && moreSubScreen != MoreSubScreen.MAIN -> {
                                    moreSubScreen = MoreSubScreen.MAIN
                                }
                                else -> currentTab = AppTab.HOME
                            }
                        }

                        Scaffold(
                            modifier = Modifier
                                .fillMaxSize()
                            .testTag("main_scaffold"),
                        containerColor = bgColor,
                        contentWindowInsets = WindowInsets(0, 0, 0, 0),
                        bottomBar = {
                            // Fixed bottom navigation bar (always visible at bottom)
                            NavigationBar(
                                containerColor = cardBg,
                                tonalElevation = 14.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = 1.dp,
                                        color = primaryColor.copy(alpha = 0.25f),
                                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                                    )
                                    .testTag("bottom_nav_bar")
                            ) {
                                allowedTabs.forEach { tab ->
                                    val isSelected = currentTab == tab
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (currentTab != tab) {
                                                currentTab = tab
                                                if (tab == AppTab.MORE) {
                                                    moreSubScreen = MoreSubScreen.MAIN
                                                }
                                            }
                                        },
                                        icon = {
                                            if (tab == AppTab.POS && cartCount > 0) {
                                                BadgedBox(
                                                    badge = {
                                                        Badge(containerColor = primaryColor, contentColor = TextDark) {
                                                            Text(
                                                                text = "$cartCount",
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 10.sp
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                        contentDescription = tab.title,
                                                        tint = if (isSelected) primaryColor else textMuted,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            } else {
                                                Icon(
                                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                                    contentDescription = tab.title,
                                                    tint = if (isSelected) primaryColor else textMuted,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        },
                                        label = {
                                            Text(
                                                text = tab.title,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                                color = if (isSelected) primaryColor else textMuted,
                                                fontFamily = CairoFont
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = primaryColor,
                                            selectedTextColor = primaryColor,
                                            indicatorColor = primaryColor.copy(alpha = 0.15f),
                                            unselectedIconColor = textMuted,
                                            unselectedTextColor = textMuted
                                        ),
                                        modifier = Modifier.testTag(tab.tag)
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentTab, label = "TabSwitch") { tab ->
                                when (tab) {
                                    AppTab.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToCart = { currentTab = AppTab.POS },
                                        onOpenAddProductDialog = { showAddProductModal = true },
                                        onQuickCheckout = { currentTab = AppTab.POS }
                                    )
                                    AppTab.POS -> CartScreen(
                                        viewModel = viewModel,
                                        onNavigateToSales = { currentTab = AppTab.HOME }
                                    )
                                    AppTab.PRODUCTS -> SalesScreen(
                                        viewModel = viewModel,
                                        onNavigateToCart = { currentTab = AppTab.POS }
                                    )
                                    AppTab.INVOICES -> InvoicesScreen(
                                        viewModel = viewModel,
                                        onNavigateToPOS = { currentTab = AppTab.POS }
                                    )
                                    AppTab.MORE -> {
                                        when (moreSubScreen) {
                                            MoreSubScreen.MAIN -> MoreScreen(
                                                viewModel = viewModel,
                                                onNavigateToUsers = { moreSubScreen = MoreSubScreen.USERS },
                                                onNavigateToReports = { moreSubScreen = MoreSubScreen.REPORTS },
                                                onNavigateToAccounts = { moreSubScreen = MoreSubScreen.ACCOUNTS },
                                                onNavigateToSettings = { moreSubScreen = MoreSubScreen.SETTINGS },
                                                onNavigateToInventory = { showAddProductModal = true },
                                                onNavigateToAbout = { moreSubScreen = MoreSubScreen.ABOUT },
                                                onOpenLoginDialog = { showLoginModal = true },
                                                onOpenSyncDialog = { showSyncModal = true }
                                            )
                                            MoreSubScreen.USERS -> UsersManagementScreen(
                                                viewModel = viewModel,
                                                onNavigateBack = { moreSubScreen = MoreSubScreen.MAIN }
                                            )
                                            MoreSubScreen.REPORTS -> ReportsScreen(
                                                viewModel = viewModel
                                            )
                                            MoreSubScreen.ACCOUNTS -> AccountsScreen(
                                                viewModel = viewModel
                                            )
                                            MoreSubScreen.SETTINGS -> SettingsScreen(
                                                viewModel = viewModel,
                                                onNavigateToInventoryManagement = { showAddProductModal = true },
                                                onNavigateToUsersManagement = { moreSubScreen = MoreSubScreen.USERS },
                                                onNavigateToAbout = { moreSubScreen = MoreSubScreen.ABOUT }
                                            )
                                            MoreSubScreen.INVENTORY -> {
                                                ManagementScreen(
                                                    viewModel = viewModel,
                                                    onNavigateBack = { moreSubScreen = MoreSubScreen.MAIN }
                                                )
                                            }
                                            MoreSubScreen.ABOUT -> {
                                                AboutScreen(
                                                    viewModel = viewModel,
                                                    onNavigateBack = { moreSubScreen = MoreSubScreen.MAIN }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Add Product & Inventory Modal
                    if (showAddProductModal) {
                        ManagementScreen(
                            viewModel = viewModel,
                            onNavigateBack = {
                                viewModel.clearEditingProduct()
                                showAddProductModal = false
                            }
                        )
                    }

                    // Login Dialog
                    if (showLoginModal) {
                        LoginDialog(
                            viewModel = viewModel,
                            onDismiss = { showLoginModal = false }
                        )
                    }

                    // Auto-sync status dialog
                    if (showSyncModal) {
                        SyncStatusDialog(
                            viewModel = viewModel,
                            onDismiss = { showSyncModal = false }
                        )
                    }
                }
            }
            }
        }
    }
}
