package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppBrandingTheme
import com.example.data.model.FirebaseConfig
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun SettingsScreen(
    viewModel: StoreViewModel,
    onNavigateToInventoryManagement: () -> Unit = {},
    onNavigateToUsersManagement: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val firebaseConfig by viewModel.firebaseConfig.collectAsState()
    val brandingTheme by viewModel.brandingTheme.collectAsState()

    val primaryColor = parseHexColor(brandingTheme.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(brandingTheme.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(brandingTheme.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(brandingTheme.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(brandingTheme.textMutedColorHex, TextMuted)

    // Form editing state for Branding & Theme
    var editedAppName by remember(brandingTheme) { mutableStateOf(brandingTheme.appName) }
    var editedLogoPreset by remember(brandingTheme) { mutableStateOf(brandingTheme.logoPreset) }
    var editedPrimaryHex by remember(brandingTheme) { mutableStateOf(brandingTheme.primaryColorHex) }
    var editedSecondaryHex by remember(brandingTheme) { mutableStateOf(brandingTheme.secondaryColorHex) }
    var editedBgHex by remember(brandingTheme) { mutableStateOf(brandingTheme.backgroundColorHex) }
    var editedTextHex by remember(brandingTheme) { mutableStateOf(brandingTheme.textPrimaryColorHex) }

    var editedPhone by remember(brandingTheme) { mutableStateOf(brandingTheme.phoneContact) }
    var editedWhatsapp by remember(brandingTheme) { mutableStateOf(brandingTheme.whatsappContact) }
    var editedEmail by remember(brandingTheme) { mutableStateOf(brandingTheme.emailContact) }
    var editedWebsite by remember(brandingTheme) { mutableStateOf(brandingTheme.websiteContact) }

    var showChangeAdminPassDialog by remember { mutableStateOf(false) }
    var showBackupExportDialog by remember { mutableStateOf(false) }
    var showRestoreImportDialog by remember { mutableStateOf(false) }
    var showFirebaseConfigDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("settings_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 140.dp)
        ) {
            // Screen Header
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, primaryColor, RoundedCornerShape(12.dp))
                                .background(primaryColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "إعدادات النظام والهوية",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = primaryColor,
                                fontFamily = CairoFont
                            )
                            Text(
                                text = "تخصيص الثيمات • الهوية • الأمان • المزامنة",
                                fontSize = 11.sp,
                                color = textMuted,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }
            }

            // Quick Link to "About App & Designer"
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { onNavigateToAbout() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("عن التطبيق والمصمم", fontWeight = FontWeight.Black, fontSize = 14.sp, color = textPrimary, fontFamily = CairoFont)
                                Text("${brandingTheme.appName} • ${brandingTheme.appVersion}", fontSize = 11.sp, color = primaryColor, fontFamily = CairoFont)
                            }
                        }

                        Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = primaryColor)
                    }
                }
            }

            // SECTION 1: THEMES & BRANDING CUSTOMIZATION (ADMIN ONLY)
            if (isAdmin) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(
                            text = "الهوية والثيمات المخصصة (للمدير فقط):",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                        Text(
                            text = "اختر من الثيمات الجاهزة أو خصص الألوان والشعار واسم التطبيق فوراً.",
                            fontSize = 11.sp,
                            color = textMuted,
                            fontFamily = CairoFont
                        )
                    }
                }

                // 3 Ready Themes Switcher
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("الثيمات الجاهزة للاختيار السريع:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Theme 1: Luxury Gold
                                ThemePresetOption(
                                    name = "الفخم الأسود والذهبي",
                                    badgeColor = Color(0xFFD4AF37),
                                    bgSample = Color(0xFF0A0A0A),
                                    isSelected = editedPrimaryHex == "#D4AF37" && editedBgHex == "#0A0A0A",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val t = AppBrandingTheme.LUXURY_GOLD
                                        editedPrimaryHex = t.primaryColorHex
                                        editedSecondaryHex = t.secondaryColorHex
                                        editedBgHex = t.backgroundColorHex
                                        editedTextHex = t.textPrimaryColorHex
                                    }
                                )

                                // Theme 2: Modern Light
                                ThemePresetOption(
                                    name = "الأبيض العصري",
                                    badgeColor = Color(0xFF1E40AF),
                                    bgSample = Color(0xFFF8FAFC),
                                    isSelected = editedBgHex == "#F8FAFC",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val t = AppBrandingTheme.MODERN_LIGHT
                                        editedPrimaryHex = t.primaryColorHex
                                        editedSecondaryHex = t.secondaryColorHex
                                        editedBgHex = t.backgroundColorHex
                                        editedTextHex = t.textPrimaryColorHex
                                    }
                                )

                                // Theme 3: Royal Navy
                                ThemePresetOption(
                                    name = "الأزرق الداكن",
                                    badgeColor = Color(0xFF38BDF8),
                                    bgSample = Color(0xFF0B132B),
                                    isSelected = editedBgHex == "#0B132B",
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        val t = AppBrandingTheme.ROYAL_NAVY
                                        editedPrimaryHex = t.primaryColorHex
                                        editedSecondaryHex = t.secondaryColorHex
                                        editedBgHex = t.backgroundColorHex
                                        editedTextHex = t.textPrimaryColorHex
                                    }
                                )
                            }
                        }
                    }
                }

                // Custom Branding & Identity Fields
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("تخصيص الهوية والشعار والألوان:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = editedAppName,
                                onValueChange = { editedAppName = it },
                                label = { Text("اسم التطبيق التجاري", fontFamily = CairoFont) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Logo Preset Selector
                            Text("رمز وشعار التطبيق (Logo):", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "gold_crown" to "تاج ملكي",
                                    "shield" to "درع أمان",
                                    "diamond" to "ماسة فاخرة",
                                    "store" to "متجر تجاري"
                                ).forEach { (id, label) ->
                                    FilterChip(
                                        selected = editedLogoPreset == id,
                                        onClick = { editedLogoPreset = id },
                                        label = { Text(label, fontSize = 10.sp, fontFamily = CairoFont) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = primaryColor,
                                            selectedLabelColor = TextDark
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Hex Colors Row
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = editedPrimaryHex,
                                    onValueChange = { editedPrimaryHex = it },
                                    label = { Text("اللون الأساسي", fontSize = 10.sp, fontFamily = CairoFont) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = editedBgHex,
                                    onValueChange = { editedBgHex = it },
                                    label = { Text("لون الخلفية", fontSize = 10.sp, fontFamily = CairoFont) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = editedTextHex,
                                    onValueChange = { editedTextHex = it },
                                    label = { Text("لون النصوص", fontSize = 10.sp, fontFamily = CairoFont) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Contact Information Editable Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("بيانات التواصل الرسمية:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = editedPhone,
                                onValueChange = { editedPhone = it },
                                label = { Text("رقم الهاتف", fontFamily = CairoFont) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = editedWhatsapp,
                                onValueChange = { editedWhatsapp = it },
                                label = { Text("واتساب الدعم", fontFamily = CairoFont) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = editedEmail,
                                onValueChange = { editedEmail = it },
                                label = { Text("البريد الإلكتروني", fontFamily = CairoFont) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // SECTION 2: USERS & SECURITY MANAGEMENT (ADMIN ONLY)
            if (isAdmin) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(
                            text = "الأمان والمستخدمين:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { onNavigateToUsersManagement() }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("شاشة إدارة المستخدمين والصلاحيات", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                                    Text("تحديد صلاحيات الشاشات والعمليات لكل مستخدم", fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                                }
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = primaryColor)
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { showChangeAdminPassDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("تغيير كلمة مرور المدير (Admin)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                                    Text("تعديل كلمة مرور الحساب الافتراضي (admin/1234)", fontSize = 10.sp, color = textMuted, fontFamily = CairoFont)
                                }
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = primaryColor)
                        }
                    }
                }
            }

            // SECTION 3: BACKUP, RESTORE & FIREBASE (ADMIN ONLY)
            if (isAdmin) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                        Text(
                            text = "النسخ الاحتياطي والمزامنة السحابية:",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showBackupExportDialog = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصدير النسخة (JSON)", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        }

                        Button(
                            onClick = { showRestoreImportDialog = true },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = cardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("استعادة بيانات (JSON)", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        }
                    }
                }

                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { showFirebaseConfigDialog = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("إعدادات جسر Firebase Firestore", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textPrimary, fontFamily = CairoFont)
                                    Text(
                                        text = if (firebaseConfig.isEnabled) "الربط مفعّل (${firebaseConfig.projectId})" else "جاهز للتفعيل دون تعديل كود",
                                        fontSize = 10.sp,
                                        color = if (firebaseConfig.isEnabled) Color(0xFF10B981) else textMuted,
                                        fontFamily = CairoFont
                                    )
                                }
                            }
                            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = primaryColor)
                        }
                    }
                }
            }
        }

        // STICKY BOTTOM SAVE & APPLY BAR (Requirement 1 & 6)
        if (isAdmin) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = cardBg,
                tonalElevation = 18.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val updatedTheme = brandingTheme.copy(
                                appName = editedAppName.trim(),
                                logoPreset = editedLogoPreset,
                                primaryColorHex = editedPrimaryHex.trim(),
                                secondaryColorHex = editedSecondaryHex.trim(),
                                backgroundColorHex = editedBgHex.trim(),
                                textPrimaryColorHex = editedTextHex.trim(),
                                phoneContact = editedPhone.trim(),
                                whatsappContact = editedWhatsapp.trim(),
                                emailContact = editedEmail.trim(),
                                websiteContact = editedWebsite.trim()
                            )
                            viewModel.updateBrandingTheme(updatedTheme)
                            Toast.makeText(context, "تم الحفظ وتطبيق الهوية بنجاح على كل الواجهة", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("sticky_save_theme_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TextDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "حفظ وتطبيق الهوية فوراً",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            fontFamily = CairoFont
                        )
                    }
                }
            }
        }
    }

    // Change Admin Password Dialog
    if (showChangeAdminPassDialog) {
        var newPass by remember { mutableStateOf("") }
        var confirmPass by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showChangeAdminPassDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor),
                modifier = Modifier.fillMaxWidth(0.95f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("تغيير كلمة مرور المدير (admin)", fontWeight = FontWeight.Black, fontSize = 16.sp, color = primaryColor, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("كلمة المرور الجديدة", fontFamily = CairoFont) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = confirmPass,
                        onValueChange = { confirmPass = it },
                        label = { Text("تأكيد كلمة المرور", fontFamily = CairoFont) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newPass.length < 4) {
                                Toast.makeText(context, "كلمة المرور يجب ألا تقل عن 4 خانات", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (newPass != confirmPass) {
                                Toast.makeText(context, "كلمتا المرور غير متطابقتين", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            val ok = viewModel.updateAdminPassword(newPass)
                            if (ok) showChangeAdminPassDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حفظ كلمة المرور الجديدة", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            }
        }
    }

    // Backup Export Dialog
    if (showBackupExportDialog) {
        val backupJson = remember { viewModel.exportBackupJson() }
        Dialog(onDismissRequest = { showBackupExportDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("تصدير النسخة الاحتياطية للنظام المحاسبي", fontWeight = FontWeight.Black, fontSize = 15.sp, color = primaryColor, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("تشمل هذه النسخة كافة الأصناف، الفواتير، الحسابات، القيود اليومية، السندات، والمستخدمين.", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = backupJson.take(500) + if (backupJson.length > 500) "\n\n... باقي بيانات النظام متوفرة بالملف" else "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("ERP Backup", backupJson))
                                Toast.makeText(context, "تم نسخ ملف النسخة الاحتياطية للحافظة", Toast.LENGTH_SHORT).show()
                                showBackupExportDialog = false
                            },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = TextDark)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("نسخ JSON", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        }

                        OutlinedButton(
                            onClick = { showBackupExportDialog = false },
                            modifier = Modifier.weight(1f).height(46.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إغلاق", fontFamily = CairoFont)
                        }
                    }
                }
            }
        }
    }

    // Restore Import Dialog
    if (showRestoreImportDialog) {
        var importJsonText by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { showRestoreImportDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("استعادة بيانات النظام المحاسبي (Restore JSON)", fontWeight = FontWeight.Black, fontSize = 15.sp, color = primaryColor, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("الصق بيانات ملف النسخة الاحتياطية لاستعادة كافة السجلات والحسابات.", fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = importJsonText,
                        onValueChange = { importJsonText = it },
                        placeholder = { Text("الصق كود JSON هنا...", fontFamily = CairoFont) },
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (importJsonText.isBlank()) return@Button
                            val success = viewModel.importBackupJson(importJsonText.trim())
                            if (success) showRestoreImportDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("بدء الاستعادة والتحديث", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            }
        }
    }

    // Firebase Config Dialog
    if (showFirebaseConfigDialog) {
        var projectId by remember { mutableStateOf(firebaseConfig.projectId) }
        var apiKey by remember { mutableStateOf(firebaseConfig.apiKey) }
        var appId by remember { mutableStateOf(firebaseConfig.appId) }
        var isEnabled by remember { mutableStateOf(firebaseConfig.isEnabled) }

        Dialog(onDismissRequest = { showFirebaseConfigDialog = false }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardBg,
                border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor),
                modifier = Modifier.fillMaxWidth(0.96f)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("إعدادات جسر Firebase Firestore", fontWeight = FontWeight.Black, fontSize = 16.sp, color = primaryColor, fontFamily = CairoFont)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = projectId,
                        onValueChange = { projectId = it },
                        label = { Text("Project ID", fontFamily = CairoFont) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { apiKey = it },
                        label = { Text("API Key", fontFamily = CairoFont) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = appId,
                        onValueChange = { appId = it },
                        label = { Text("App ID", fontFamily = CairoFont) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("تفعيل الربط السحابي التلقائي", fontWeight = FontWeight.Bold, color = textPrimary, fontSize = 12.sp, fontFamily = CairoFont)
                        Switch(
                            checked = isEnabled,
                            onCheckedChange = { isEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val newConfig = firebaseConfig.copy(
                                projectId = projectId.trim(),
                                apiKey = apiKey.trim(),
                                appId = appId.trim(),
                                isEnabled = isEnabled
                            )
                            viewModel.updateFirebaseConfig(newConfig)
                            showFirebaseConfigDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حفظ إعدادات Firebase", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemePresetOption(
    name: String,
    badgeColor: Color,
    bgSample: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .border(
                1.5.dp,
                if (isSelected) badgeColor else Color(0xFF333333),
                RoundedCornerShape(12.dp)
            ),
        color = bgSample
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (bgSample == Color(0xFFF8FAFC)) Color(0xFF0F172A) else Color.White,
                textAlign = TextAlign.Center,
                fontFamily = CairoFont
            )
        }
    }
}
