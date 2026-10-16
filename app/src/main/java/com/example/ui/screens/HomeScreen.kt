package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.ui.draw.rotate
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Product
import com.example.data.model.SyncStatus
import com.example.data.model.UserRole
import com.example.ui.components.LoginDialog
import com.example.ui.components.ProductDetailDialog
import com.example.ui.components.SyncStatusDialog
import com.example.ui.components.CameraBarcodeScannerDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StoreViewModel,
    onNavigateToCart: () -> Unit,
    onOpenAddProductDialog: () -> Unit,
    onQuickCheckout: () -> Unit
) {
    val context = LocalContext.current
    val paginatedProducts by viewModel.paginatedProducts.collectAsState()
    val allFilteredProducts by viewModel.products.collectAsState()
    val hasMoreProducts by viewModel.hasMoreProducts.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val cart by viewModel.cart.collectAsState()
    val inspectingProduct by viewModel.inspectingProduct.collectAsState()
    val syncStatus by viewModel.syncStatus.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val branding by viewModel.brandingTheme.collectAsState()

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showSyncStatusDialog by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showCameraScannerDialog by remember { mutableStateOf(false) }

    val rotation = rememberInfiniteTransition(label = "sync_spin")
    val syncSpinAngle by rotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "syncSpinAngle"
    )

    val cartTotalCount = cart.sumOf { it.quantity }
    val cartTotalAmount = cart.sumOf { it.totalPrice }

    // Speech recognition launcher for voice search
    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenTextMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            if (!spokenTextMatches.isNullOrEmpty()) {
                val query = spokenTextMatches[0]
                viewModel.setSearchQuery(query)
                Toast.makeText(context, "تم البحث صوتياً: $query", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Grid state for infinite scrolling
    val gridState = rememberLazyGridState()

    // Trigger load more when user scrolls to bottom
    val shouldLoadMore by remember {
        derivedStateOf {
            val totalItems = gridState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && hasMoreProducts && !isLoadingMore) {
            viewModel.loadMoreProducts()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ================= 1. الهيدر العلوي الثابت (Sticky Header) =================
            // شعار المتجر (أيقونة دلة ذهبية) + اسم "متجر الفيصل للتجارة العامة" + على اليسار إشعارات وسلة وسحب التحديث
            Surface(
                color = DarkBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right side (in RTL): Store logo (Golden Dallah) + Store Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.5.dp, LuxuryGold, RoundedCornerShape(14.dp))
                                .background(Color(0xFF141414)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.faisal_dallah_logo_1792029175878),
                                contentDescription = "شعار دلة الفيصل",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = branding.appName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = LuxuryGold
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "نظام تخطيط الموارد المحاسبي",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = TextWhite.copy(alpha = 0.85f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }

                    // Left side (in RTL): Cart pill, Refresh, Notifications
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Auto-Sync Status Icon in header (Feature 2)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showSyncStatusDialog = true }
                                .testTag("header_sync_status"),
                            shape = RoundedCornerShape(10.dp),
                            color = if (syncStatus == SyncStatus.SYNCING) LuxuryGoldSurface else Color(0xFF141414),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (syncStatus == SyncStatus.SYNCING) LuxuryGold else Color(0xFF262626)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (syncStatus == SyncStatus.SYNCING) Icons.Default.Sync else Icons.Default.CheckCircle,
                                    contentDescription = "حالة المزامنة",
                                    tint = if (syncStatus == SyncStatus.SYNCING) LuxuryGold else Color(0xFF81C784),
                                    modifier = Modifier
                                        .size(15.dp)
                                        .then(if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(syncSpinAngle) else Modifier)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (syncStatus == SyncStatus.SYNCING) "حفظ..." else "متزامن",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (syncStatus == SyncStatus.SYNCING) LuxuryGold else Color(0xFF81C784),
                                    fontFamily = CairoFont
                                )
                            }
                        }

                        // User Role / Login Badge (Feature 1)
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showLoginDialog = true }
                                .testTag("header_user_badge"),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF141414),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262626))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (currentUser?.role == UserRole.ADMIN) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = "المستخدم الحالي",
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (currentUser?.role == UserRole.ADMIN) "المدير" else "كاشير",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = CairoFont
                                )
                            }
                        }

                        // Quick Cart Count Pill if items exist
                        if (cartTotalCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, LuxuryGold, RoundedCornerShape(10.dp))
                                    .background(LuxuryGoldSurface)
                                    .clickable { onNavigateToCart() }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            ) {
                                Text(
                                    text = "$cartTotalCount سلة",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LuxuryGold
                                )
                            }
                        }

                        // Notification Icon with Gold Dot Badge
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, Color(0xFF262626), RoundedCornerShape(10.dp))
                                .background(DarkCardBackground)
                                .clickable { showNotificationsDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "الإشعارات",
                                tint = LuxuryGold,
                                modifier = Modifier.size(17.dp)
                            )
                            // Unread notification badge
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-4).dp, y = 4.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryGold)
                            )
                        }
                    }
                }
            }

            // ================= 2. شريط البحث الثابت مع خاصية البحث الصوتي =================
            // حقل بحث كبير بحدود ذهبية وحاشية دائرية، فيه placeholder "ابحث عن منتج..." وأيقونة بحث ذهبية وميكروفون
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "ابحث عن منتج أو اضغط الميكروفون...",
                            color = TextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = LuxuryGold,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            // Barcode / Camera Scan Button (Requirement 7: Always visible, never disappears)
                            IconButton(
                                onClick = { showCameraScannerDialog = true },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryGoldSurface)
                                    .testTag("search_barcode_camera_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "مسح الباركود بالكاميرا",
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Voice Search Microphone Button
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(
                                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                            )
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
                                            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث للبحث عن المنتجات...")
                                        }
                                        voiceSearchLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "التعرف على الصوت غير متاح على هذا الجهاز",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryGoldSurface)
                                    .testTag("voice_search_mic_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "البحث بالصوت",
                                    tint = LuxuryGold,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح",
                                        tint = LuxuryGoldLight,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF141414),
                        unfocusedContainerColor = Color(0xFF141414),
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = LuxuryGold.copy(alpha = 0.55f),
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        cursorColor = LuxuryGold
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("luxury_search_field")
                )
            }

            // شريط الفئات السريعة الثابت
            val allCats = remember(categories) { listOf("الكل") + categories }
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allCats.forEach { cat ->
                    item(key = cat) {
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectCategory(cat) },
                            label = {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LuxuryGold,
                                selectedLabelColor = TextDark,
                                containerColor = DarkElevated,
                                labelColor = TextWhite
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF2E2E2E),
                                selectedBorderColor = LuxuryGold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // ================= 3. شبكة المنتجات القابلة للتمرير مع السحب للتحديث =================
            // (Pull to Refresh + Infinite Scroll / Pagination)
            val pullToRefreshState = rememberPullToRefreshState()

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshProducts() },
                state = pullToRefreshState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("product_grid_section"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // شريط حالة المنتجات المتاحة
                    item(span = { GridItemSpan(2) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 2.dp, vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "المنتجات المتاحة (${allFilteredProducts.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGoldLight
                            )
                            if (searchQuery.isNotBlank()) {
                                Text(
                                    text = "نتائج البحث عن \"$searchQuery\"",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }

                    if (paginatedProducts.isEmpty()) {
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Outlined.Inventory2,
                                        contentDescription = null,
                                        tint = LuxuryGold.copy(alpha = 0.4f),
                                        modifier = Modifier.size(50.dp)
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = "لا توجد منتجات مطابقة",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "جرّب كتابة كلمة بحث أخرى أو استخدم البحث الصوتي",
                                        fontSize = 12.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                        }
                    } else {
                        items(paginatedProducts, key = { it.id }) { product ->
                            LuxuryProductGridCard(
                                product = product,
                                onCardClick = { viewModel.inspectProduct(product) },
                                onAddToCart = { viewModel.addToCart(product) }
                            )
                        }

                        // Infinite Scroll: مؤشر تحميل المزيد تلقائياً عند نهاية القائمة
                        if (hasMoreProducts || isLoadingMore) {
                            item(span = { GridItemSpan(2) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = LuxuryGold,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "جاري تحميل المزيد من المنتجات تلقائياً...",
                                            fontSize = 12.sp,
                                            color = LuxuryGoldLight,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ================= 4. أزرار الإجراءات الرئيسية الثابتة في الأسفل =================
            // - زر ذهبي ممتلئ: "إضافة منتج جديد +"
            // - زر بحدود ذهبية وخلفية شفافة: "إنهاء عملية البيع"
            Surface(
                color = DarkBackground,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF262626), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // الزر الذهبي الممتلئ: "إضافة منتج جديد +"
                    Button(
                        onClick = onOpenAddProductDialog,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuxuryGold,
                            contentColor = TextDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("action_btn_add_product")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = TextDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "إضافة منتج جديد +",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = TextDark
                        )
                    }

                    // الزر بحدود ذهبية وخلفية شفافة: "إنهاء عملية البيع"
                    OutlinedButton(
                        onClick = onQuickCheckout,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = LuxuryGold
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, LuxuryGold),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("action_btn_checkout")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (cartTotalAmount > 0)
                                "إنهاء عملية البيع • ${"%,.2f".format(cartTotalAmount * 1.15)} ريال"
                            else
                                "إنهاء عملية البيع",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGold
                        )
                    }
                }
            }
        }

        // Product Details Modal
        inspectingProduct?.let { prod ->
            ProductDetailDialog(
                product = prod,
                onDismiss = { viewModel.inspectProduct(null) },
                onAddToCart = { qty ->
                    repeat(qty) {
                        viewModel.addToCart(prod)
                    }
                }
            )
        // Camera Barcode Scanner Dialog for Main Search (Requirement 7)
        if (showCameraScannerDialog) {
            CameraBarcodeScannerDialog(
                primaryColor = LuxuryGold,
                cardBg = DarkCardBackground,
                textPrimary = TextWhite,
                textMuted = TextMuted,
                allowGenerateNew = true,
                sampleBarcodes = allFilteredProducts.mapNotNull { if (it.barcode.isNotBlank()) it.barcode else null }.take(6),
                onDismiss = { showCameraScannerDialog = false },
                onBarcodeScanned = { scannedCode ->
                    viewModel.setSearchQuery(scannedCode)
                    showCameraScannerDialog = false
                    Toast.makeText(context, "تم البحث الفوري بالباركود: $scannedCode", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Notifications Dialog
        if (showNotificationsDialog) {
            AlertDialog(
                onDismissRequest = { showNotificationsDialog = false },
                containerColor = DarkCardBackground,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, tint = LuxuryGold)
                        Text(text = "إشعارات المتجر", color = LuxuryGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "• مرحباً بك في متجر الفيصل للتجارة العامة.",
                            color = TextWhite,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "• تم تفعيل خاصية البحث الصوتي بالميكروفون ورفع صور المنتجات بنجاح.",
                            color = LuxuryGoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "• السحب للأسفل للتحديث (Pull to Refresh) والتمرير اللانهائي مفعلان.",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showNotificationsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold)
                    ) {
                        Text("حسناً", color = TextDark, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            )
        }

        // Auto Sync Status Dialog (Feature 2)
        if (showSyncStatusDialog) {
            SyncStatusDialog(
                viewModel = viewModel,
                onDismiss = { showSyncStatusDialog = false }
            )
        }

        // Login / User Switch Dialog (Feature 1)
        if (showLoginDialog) {
            LoginDialog(
                viewModel = viewModel,
                onDismiss = { showLoginDialog = false }
            )
        }

        // Camera & Barcode Scanner Dialog (Requirement 7: Search by scanning barcode)
        if (showCameraScannerDialog) {
            val themePrimary = parseHexColor(branding.primaryColorHex, LuxuryGold)
            val sampleCodes = allFilteredProducts.mapNotNull { it.barcode.takeIf { b -> b.isNotBlank() } }.take(5)
            CameraBarcodeScannerDialog(
                primaryColor = themePrimary,
                cardBg = DarkCardBackground,
                textPrimary = TextWhite,
                textMuted = TextMuted,
                allowGenerateNew = false,
                sampleBarcodes = sampleCodes,
                onDismiss = { showCameraScannerDialog = false },
                onBarcodeScanned = { barcode ->
                    showCameraScannerDialog = false
                    viewModel.setSearchQuery(barcode)
                    Toast.makeText(context, "تم مسح الباركود والبحث: $barcode", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}
}

/**
 * بطاقة المنتج الفاخرة بنظام الشبكة Grid (2 Columns)
 * - الحواف دائرية 16px (16.dp)
 * - الخلفية سوداء أفتح قليلاً (#161616)
 * - حدود ذهبية رفيعة (#D4AF37)
 * - صورة المنتج (صورة مرفوعة، أو مورد، أو صورة المتجر الافتراضية الذهبية)
 * - اسم المنتج، السعر بالريال، زر صغير ذهبي "إضافة +"
 */
@Composable
fun LuxuryProductGridCard(
    product: Product,
    onCardClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardBackground),
        border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGoldBorder.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("luxury_product_card_${product.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // صورة المنتج مع زوايا دائرية ودعم الصورة المرفوعة والصورة الافتراضية بشعار المتجر الذهبي
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F0F0F))
                    .border(1.dp, Color(0xFF262626), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (product.primaryImageUri != null) {
                    // صورة الصنف المرفوعة مباشرة من الكاميرا أو المعرض
                    AsyncImage(
                        model = product.primaryImageUri,
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (product.imageResId != null) {
                    Image(
                        painter = painterResource(id = product.imageResId),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // إذا لم يتم رفع صورة، عرض صورة افتراضية أنيقة بشعار المتجر الذهبي
                    Image(
                        painter = painterResource(id = R.drawable.faisal_dallah_logo_1792029175878),
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(68.dp)
                    )
                }

                // شارة القسم الصغيرة في الزاوية
                Surface(
                    color = Color.Black.copy(alpha = 0.75f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, LuxuryGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = product.category,
                        color = LuxuryGoldLight,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // اسم المنتج بخط أبيض
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // السعر بالريال وزر الإضافة الصغير الذهبي "إضافة +"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // السعر بالريال (ذهبي فاخر)
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "${"%,.0f".format(product.price)} ريال",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = LuxuryGold
                    )
                }

                // زر صغير ذهبي "إضافة +"
                Button(
                    onClick = onAddToCart,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuxuryGold,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("add_btn_${product.id}")
                ) {
                    Text(
                        text = "إضافة +",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = TextDark
                    )
                }
            }
        }
    }
}
