package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.ui.components.CameraBarcodeScannerDialog
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Product
import com.example.ui.components.dashedBorder
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel
import com.example.util.ImageHelper

@Composable
fun ManagementScreen(
    viewModel: StoreViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val categories by viewModel.categories.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val editingProduct by viewModel.editingProduct.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()

    var activeSubTab by remember { mutableStateOf(if (editingProduct != null) "تعديل صنف" else "إضافة صنف") }

    // Form fields
    var itemName by remember { mutableStateOf("") }
    var itemModel by remember { mutableStateOf("") }
    var itemCode by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf("حبة") }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "أدوات وتراثيات") }
    var barcode by remember { mutableStateOf("") }
    var costPriceStr by remember { mutableStateOf("") }
    var retailPriceStr by remember { mutableStateOf("") }
    var wholesalePriceStr by remember { mutableStateOf("") }
    var stockStr by remember { mutableStateOf("10") }
    var notes by remember { mutableStateOf("") }

    // Images
    var primaryImageUri by remember { mutableStateOf<String?>(null) }
    val additionalImages = remember { mutableStateListOf<String>() }

    // Dialog & Picker states
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var isSelectingAdditionalImage by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    var showBarcodeScannerCamera by remember { mutableStateOf(false) }
    var showSaveSuccessDialog by remember { mutableStateOf(false) }
    var savedProductName by remember { mutableStateOf("") }

    // Pre-populate when editing product changes
    LaunchedEffect(editingProduct) {
        editingProduct?.let { prod ->
            activeSubTab = "تعديل صنف"
            itemName = prod.name
            itemModel = prod.model
            itemCode = prod.id
            itemUnit = prod.unit
            selectedCategory = prod.category
            barcode = prod.barcode
            costPriceStr = if (prod.costPrice > 0) prod.costPrice.toString() else ""
            retailPriceStr = if (prod.price > 0) prod.price.toString() else ""
            wholesalePriceStr = if (prod.wholesalePrice > 0) prod.wholesalePrice.toString() else ""
            stockStr = prod.stock.toString()
            notes = prod.note
            primaryImageUri = prod.primaryImageUri
            additionalImages.clear()
            additionalImages.addAll(prod.additionalImages)
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = ImageHelper.compressAndSaveBitmap(context, bitmap)
            if (savedPath != null) {
                if (isSelectingAdditionalImage) {
                    if (additionalImages.size < 3) additionalImages.add(savedPath)
                } else {
                    primaryImageUri = savedPath
                }
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageHelper.compressAndSaveUri(context, uri)
            if (savedPath != null) {
                if (isSelectingAdditionalImage) {
                    if (additionalImages.size < 3) additionalImages.add(savedPath)
                } else {
                    primaryImageUri = savedPath
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("management_screen"),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Sticky Header / Subtabs
        item {
            Surface(
                color = DarkBackground,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = DarkCardBackground,
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGoldBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(modifier = Modifier.padding(3.dp)) {
                                val currentTabs = if (editingProduct != null)
                                    listOf("المخزون", "تعديل صنف")
                                else
                                    listOf("المخزون", "إضافة صنف")

                                currentTabs.forEach { tab ->
                                    val isSelected = activeSubTab == tab
                                    Surface(
                                        color = if (isSelected) LuxuryGold else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.clickable { activeSubTab = tab }
                                    ) {
                                        Text(
                                            text = tab,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) TextDark else TextWhite,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (editingProduct != null) "تعديل صنف" else "إدارة المنتجات والمخزون",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = LuxuryGold
                            )
                            Text(
                                text = if (editingProduct != null) "تحديث بيانات الصنف والصور" else "إضافة صنف وضبط الأسعار والصور",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Feedback / Notice
        notice?.let { msg ->
            item {
                Surface(
                    color = LuxuryGoldSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LuxuryGoldLight)
                    }
                }
            }
        }

        if (activeSubTab == "إضافة صنف" || activeSubTab == "تعديل صنف") {
            // Form Container
            item {
                Surface(
                    color = DarkCardBackground,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGoldBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {

                        // --- 1. حقل صورة الصنف الكبير بحدود ذهبية متقطعة ---
                        Text(
                            text = "صورة الصنف الرئيسية",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = LuxuryGold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF101010))
                                .dashedBorder(
                                    width = 1.5.dp,
                                    color = LuxuryGold,
                                    cornerRadius = 16.dp,
                                    dashLength = 9.dp,
                                    gapLength = 6.dp
                                )
                                .clickable {
                                    isSelectingAdditionalImage = false
                                    showImageSourceDialog = true
                                }
                                .testTag("primary_image_upload_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (primaryImageUri != null) {
                                // معاينة فورية للصورة
                                AsyncImage(
                                    model = primaryImageUri,
                                    contentDescription = "صورة الصنف الرئيسية",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // زر X لحذف أو تغيير الصورة
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp)
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .border(1.dp, LuxuryGold, CircleShape)
                                        .clickable {
                                            primaryImageUri = null
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "حذف الصورة",
                                        tint = LuxuryGoldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // علامة تغيير الصورة
                                Surface(
                                    color = Color.Black.copy(alpha = 0.75f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGold),
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 10.dp)
                                ) {
                                    Text(
                                        text = "اضغط لتغيير الصورة",
                                        color = LuxuryGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                // المظهر الأولي: أيقونة كاميرا ذهبية ونص "اضغط لإضافة صورة الصنف"
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(LuxuryGoldSurface)
                                            .border(1.dp, LuxuryGold.copy(alpha = 0.5f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "كاميرا",
                                            tint = LuxuryGold,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = "اضغط لإضافة صورة الصنف",
                                        color = LuxuryGold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "التقاط بالكاميرا أو اختيار من المعرض (يتم الضغط تلقائياً)",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // --- صور إضافية (حتى 3 صور) ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "صور إضافية (${additionalImages.size}/3)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGoldLight
                            )
                            if (additionalImages.size < 3) {
                                TextButton(
                                    onClick = {
                                        isSelectingAdditionalImage = true
                                        showImageSourceDialog = true
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "إضافة صورة", color = LuxuryGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            additionalImages.forEachIndexed { index, imgUri ->
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, LuxuryGold, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = imgUri,
                                        contentDescription = "صورة إضافية $index",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.8f))
                                            .clickable { additionalImages.removeAt(index) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "حذف",
                                            tint = LuxuryGoldLight,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            if (additionalImages.size < 3) {
                                Box(
                                    modifier = Modifier
                                        .size(76.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF141414))
                                        .dashedBorder(
                                            width = 1.dp,
                                            color = LuxuryGold.copy(alpha = 0.6f),
                                            cornerRadius = 12.dp,
                                            dashLength = 6.dp,
                                            gapLength = 4.dp
                                        )
                                        .clickable {
                                            isSelectingAdditionalImage = true
                                            showImageSourceDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = LuxuryGold, modifier = Modifier.size(20.dp))
                                        Text(text = "+ صورة", fontSize = 10.sp, color = TextMuted)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = Color(0xFF262626))
                        Spacer(modifier = Modifier.height(14.dp))

                        // --- البيانات الأساسية ---
                        Text(
                            text = "البيانات الأساسية",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = LuxuryGold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Item name
                        OutlinedTextField(
                            value = itemName,
                            onValueChange = {
                                itemName = it
                                formError = null
                            },
                            label = { Text("اسم الصنف *", color = LuxuryGoldLight) },
                            placeholder = { Text("مثال: طقم قدور جرانيت تركي") },
                            singleLine = true,
                            isError = formError != null && itemName.isBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF333333)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_item_name")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Code & Model Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = itemModel,
                                onValueChange = { itemModel = it },
                                label = { Text("موديل الصنف", color = TextMuted) },
                                placeholder = { Text("GR-9PCS") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_item_model")
                            )

                            OutlinedTextField(
                                value = itemCode,
                                onValueChange = { itemCode = it },
                                label = { Text("رقم الصنف / SKU", color = TextMuted) },
                                placeholder = { Text("1001") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_item_code")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Unit
                        OutlinedTextField(
                            value = itemUnit,
                            onValueChange = { itemUnit = it },
                            label = { Text("الوحدة", color = TextMuted) },
                            placeholder = { Text("مثال: حبة، كرتون، طقم، كجم") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF333333)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_item_unit")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Category Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddCategoryDialog = true }) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = LuxuryGold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "قسم جديد", color = LuxuryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(text = "القسم", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(categories) { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LuxuryGold,
                                        selectedLabelColor = TextDark,
                                        containerColor = DarkElevated,
                                        labelColor = TextWhite
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Barcode input with scan/generate button
                        Text(text = "باركود الصنف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Open Camera Scanner First (Requirement 5)
                            IconButton(
                                onClick = {
                                    showBarcodeScannerCamera = true
                                },
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(LuxuryGold)
                                    .testTag("scan_or_gen_barcode_btn")
                            ) {
                                Icon(imageVector = Icons.Outlined.QrCodeScanner, contentDescription = "مسح بالكاميرا أولاً", tint = TextDark)
                            }

                            OutlinedTextField(
                                value = barcode,
                                onValueChange = { barcode = it },
                                placeholder = { Text("أدخل الباركود يدوياً أو اضغط للتوليد") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_barcode_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFF262626))
                        Spacer(modifier = Modifier.height(14.dp))

                        // Pricing & Stock Section
                        Text(
                            text = "الأسعار والمخزون",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = LuxuryGold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = costPriceStr,
                                onValueChange = { costPriceStr = it },
                                label = { Text("سعر التكلفة", color = TextMuted) },
                                placeholder = { Text("0.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_cost_price")
                            )

                            OutlinedTextField(
                                value = retailPriceStr,
                                onValueChange = {
                                    retailPriceStr = it
                                    formError = null
                                },
                                label = { Text("سعر التجزئة *", color = LuxuryGoldLight) },
                                placeholder = { Text("0.00") },
                                singleLine = true,
                                isError = formError != null && retailPriceStr.isBlank(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_retail_price")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = wholesalePriceStr,
                                onValueChange = { wholesalePriceStr = it },
                                label = { Text("سعر الجملة", color = TextMuted) },
                                placeholder = { Text("0.00") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_wholesale_price")
                            )

                            OutlinedTextField(
                                value = stockStr,
                                onValueChange = { stockStr = it },
                                label = { Text("الكمية المتوفرة", color = TextMuted) },
                                placeholder = { Text("10") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = TextWhite,
                                    unfocusedTextColor = TextWhite,
                                    focusedBorderColor = LuxuryGold,
                                    unfocusedBorderColor = Color(0xFF333333)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_stock")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Notes
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("ملاحظات إضافية", color = TextMuted) },
                            placeholder = { Text("ملاحظات المنتج أو العروض...") },
                            minLines = 3,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedBorderColor = LuxuryGold,
                                unfocusedBorderColor = Color(0xFF333333)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_notes")
                        )

                        formError?.let { err ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = err, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearEditingProduct()
                                    onNavigateBack()
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextWhite),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(50.dp)
                            ) {
                                Text(text = "إلغاء")
                            }

                            Button(
                                onClick = {
                                    val price = retailPriceStr.toDoubleOrNull()
                                    if (itemName.isBlank() || price == null || price <= 0) {
                                        formError = "الرجاء إدخال اسم الصنف وسعر التجزئة على الأقل"
                                    } else {
                                        val currentEditing = editingProduct

                                        // Duplicate Barcode & Code Prevention (Requirement 6)
                                        val dupError = viewModel.validateProductUniqueness(
                                            barcode = barcode,
                                            model = itemModel,
                                            excludeId = currentEditing?.id
                                        )
                                        if (dupError != null) {
                                            formError = dupError
                                            android.widget.Toast.makeText(context, dupError, android.widget.Toast.LENGTH_LONG).show()
                                            return@Button
                                        }

                                        val cost = costPriceStr.toDoubleOrNull() ?: (price * 0.75)
                                        val wholesale = wholesalePriceStr.toDoubleOrNull() ?: (price * 0.9)
                                        val stock = stockStr.toIntOrNull() ?: 10

                                        if (currentEditing != null) {
                                            val err = viewModel.updateProduct(
                                                id = currentEditing.id,
                                                name = itemName,
                                                category = selectedCategory,
                                                price = price,
                                                costPrice = cost,
                                                wholesalePrice = wholesale,
                                                stock = stock,
                                                model = itemModel,
                                                unit = itemUnit,
                                                barcode = barcode,
                                                note = notes,
                                                primaryImageUri = primaryImageUri,
                                                additionalImages = additionalImages.toList(),
                                                imageResId = currentEditing.imageResId
                                            )
                                            if (err != null) {
                                                formError = err
                                                android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                                return@Button
                                            }
                                        } else {
                                            val err = viewModel.addNewProduct(
                                                name = itemName,
                                                category = selectedCategory,
                                                price = price,
                                                costPrice = cost,
                                                wholesalePrice = wholesale,
                                                stock = stock,
                                                model = itemModel,
                                                unit = itemUnit,
                                                barcode = barcode,
                                                note = notes,
                                                primaryImageUri = primaryImageUri,
                                                additionalImages = additionalImages.toList()
                                            )
                                            if (err != null) {
                                                formError = err
                                                android.widget.Toast.makeText(context, err, android.widget.Toast.LENGTH_LONG).show()
                                                return@Button
                                            }
                                        }

                                        // Do not close silently - show clear success alert (Requirement 6)
                                        savedProductName = itemName
                                        showSaveSuccessDialog = true
                                        formError = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LuxuryGold,
                                    contentColor = TextDark
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(50.dp)
                                    .testTag("save_product_btn")
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = TextDark)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (editingProduct != null) "تحديث الصنف" else "حفظ الصنف",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Inventory List with Edit and Delete options
            item {
                Text(
                    text = "الأصناف الحالية (${allProducts.size} صنف)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = LuxuryGold,
                    textAlign = TextAlign.Right,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }

            items(allProducts, key = { it.id }) { prod ->
                Surface(
                    color = DarkCardBackground,
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262626)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action buttons: Delete & Edit
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isAdmin) {
                                IconButton(
                                    onClick = { viewModel.deleteProduct(prod.id) },
                                    modifier = Modifier.testTag("delete_product_${prod.id}")
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف الصنف", tint = ErrorRed)
                                }
                            }
                            IconButton(
                                onClick = {
                                    viewModel.startEditingProduct(prod)
                                    activeSubTab = "تعديل صنف"
                                },
                                modifier = Modifier.testTag("edit_product_${prod.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل الصنف", tint = LuxuryGold)
                            }
                        }

                        // Product image thumbnail & Details
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = prod.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(
                                    text = "السعر: ${"%,.2f".format(prod.price)} ريال • المخزون: ${prod.stock} ${prod.unit} • ${prod.category}",
                                    fontSize = 11.sp,
                                    color = LuxuryGold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF141414))
                                    .border(1.dp, Color(0xFF2E2E2E), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (prod.primaryImageUri != null) {
                                    AsyncImage(
                                        model = prod.primaryImageUri,
                                        contentDescription = prod.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else if (prod.imageResId != null) {
                                    Image(
                                        painter = painterResource(id = prod.imageResId),
                                        contentDescription = prod.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.faisal_dallah_logo_1792029175878),
                                        contentDescription = prod.name,
                                        contentScale = ContentScale.Fit,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Choose between Camera and Gallery
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            containerColor = DarkCardBackground,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = if (isSelectingAdditionalImage) "إضافة صورة إضافية للصنف" else "اختيار صورة الصنف",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = LuxuryGold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "اختر مصدر الصورة لتحميلها وضغطها تلقائياً:",
                        color = TextWhite,
                        fontSize = 13.sp
                    )

                    // الخيار 1: التقاط صورة بالكاميرا
                    Surface(
                        color = Color(0xFF151515),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGoldBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImageSourceDialog = false
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                    cameraLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryGoldSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = LuxuryGold)
                            }
                            Column {
                                Text(text = "التقاط صورة بالكاميرا", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                                Text(text = "استخدام كاميرا الهاتف المباشرة", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }

                    // الخيار 2: اختيار من المعرض
                    Surface(
                        color = Color(0xFF151515),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGoldBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImageSourceDialog = false
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(LuxuryGoldSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = LuxuryGold)
                            }
                            Column {
                                Text(text = "اختيار من المعرض", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 13.sp)
                                Text(text = "تصفح الصور المحفوظة في الجهاز", color = TextMuted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }

    // Modal Dialog: Add new category
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = DarkCardBackground,
            title = { Text(text = "إضافة قسم جديد للمتجر", fontWeight = FontWeight.Black, fontSize = 16.sp, color = LuxuryGold) },
            text = {
                OutlinedTextField(
                    value = newCategoryInput,
                    onValueChange = { newCategoryInput = it },
                    placeholder = { Text("مثال: عطورات ومباخر ملكية") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryInput.isNotBlank()) {
                            viewModel.addNewCategory(newCategoryInput)
                            selectedCategory = newCategoryInput.trim()
                            newCategoryInput = ""
                            showAddCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = TextDark)
                ) {
                    Text("إضافة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }

    // Camera Barcode Scanner Dialog (Requirement 5)
    if (showBarcodeScannerCamera) {
        CameraBarcodeScannerDialog(
            primaryColor = LuxuryGold,
            cardBg = DarkCardBackground,
            textPrimary = TextWhite,
            textMuted = TextMuted,
            allowGenerateNew = true,
            sampleBarcodes = allProducts.mapNotNull { if (it.barcode.isNotBlank()) it.barcode else null }.take(6),
            onDismiss = { showBarcodeScannerCamera = false },
            onBarcodeScanned = { scannedCode ->
                barcode = scannedCode
                showBarcodeScannerCamera = false
            }
        )
    }

    // Save Product Success Alert Dialog (Requirement 6)
    if (showSaveSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSaveSuccessDialog = false
                viewModel.clearEditingProduct()
                activeSubTab = "المخزون"
            },
            containerColor = DarkCardBackground,
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = LuxuryGold)
                    Text(
                        text = "تم حفظ الصنف بنجاح",
                        color = LuxuryGold,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        fontFamily = CairoFont
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تم حفظ وإيداع الصنف \"$savedProductName\" في المخزون الفعلي بنجاح وتحديث كافة الأرصدة وحركات التوريد.",
                        color = TextWhite,
                        fontSize = 13.sp,
                        fontFamily = CairoFont
                    )
                    Text(
                        text = "الباركود المسجل: ${barcode.ifEmpty { "غير محدد" }}",
                        color = LuxuryGoldLight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFont
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSaveSuccessDialog = false
                        viewModel.clearEditingProduct()
                        activeSubTab = "المخزون"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LuxuryGold, contentColor = TextDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("متابعة وعرض المخزون", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                }
            }
        )
    }
}

