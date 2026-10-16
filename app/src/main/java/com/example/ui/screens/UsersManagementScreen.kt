package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersManagementScreen(
    viewModel: StoreViewModel,
    onNavigateBack: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    val branding by viewModel.brandingTheme.collectAsState()

    val primaryColor = parseHexColor(branding.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(branding.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(branding.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(branding.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(branding.textMutedColorHex, TextMuted)

    var showAddUserDialog by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var userToDelete by remember { mutableStateOf<User?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "إدارة المستخدمين والصلاحيات",
                            color = textPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFont
                        )
                        Text(
                            text = "${users.size} حسابات مسجلة ومفعلة",
                            color = primaryColor,
                            fontSize = 11.sp,
                            fontFamily = CairoFont
                        )
                    }

                    if (isAdmin) {
                        Button(
                            onClick = { showAddUserDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_user_top_button")
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = TextDark, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مستخدم جديد +", color = TextDark, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        }
                    }
                }
            },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("users_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = primaryColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        containerColor = bgColor
    ) { paddingValues ->
        if (!isAdmin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(24.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("عفواً: هذه الشاشة مخصصة للمدير العام فقط", color = textPrimary, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("يتطلب الدخول صلاحية إدارة النظام بالكامل لتعديل الصلاحيات والحسابات.", color = textMuted, fontSize = 12.sp, textAlign = TextAlign.Center, fontFamily = CairoFont)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .testTag("users_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Banner
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = cardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "نظام الصلاحيات المتقدم (RBAC Matrix)",
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    fontFamily = CairoFont
                                )
                                Text(
                                    text = "حدد لكل مستخدم الشاشات المسموحة (تختفي إذا ممنوعة) والعمليات الدقيقة (عرض، إضافة، تعديل، حذف، طباعة، اعتماد).",
                                    color = textMuted,
                                    fontSize = 11.sp,
                                    lineHeight = 16.sp,
                                    fontFamily = CairoFont
                                )
                            }
                        }
                    }
                }

                // User Cards
                items(users, key = { it.id }) { user ->
                    UserCardItem(
                        user = user,
                        isCurrentUser = user.id == currentUser?.id,
                        primaryColor = primaryColor,
                        cardBg = cardBg,
                        textPrimary = textPrimary,
                        textMuted = textMuted,
                        onEdit = { editingUser = user },
                        onToggleActive = { viewModel.toggleUserActive(user.id) },
                        onDelete = { userToDelete = user }
                    )
                }
            }
        }
    }

    // Add User Dialog
    if (showAddUserDialog) {
        UserFormDialog(
            title = "إنشاء مستخدم جديد وتحديد الصلاحيات",
            initialUser = null,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { showAddUserDialog = false },
            onSave = { username, pass, name, role, phone, perms ->
                val ok = viewModel.addUser(username, pass, name, role, phone, perms)
                if (ok) showAddUserDialog = false
            }
        )
    }

    // Edit User Dialog
    if (editingUser != null) {
        val u = editingUser!!
        UserFormDialog(
            title = "تعديل المستخدم وصلاحياته",
            initialUser = u,
            primaryColor = primaryColor,
            cardBg = cardBg,
            textPrimary = textPrimary,
            textMuted = textMuted,
            onDismiss = { editingUser = null },
            onSave = { username, pass, name, role, phone, perms ->
                val updated = u.copy(
                    username = username,
                    password = if (pass.isNotBlank()) pass else u.password,
                    fullName = name,
                    role = role,
                    phone = phone,
                    customPermissions = perms
                )
                viewModel.updateUser(updated)
                editingUser = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Text("تأكيد حذف المستخدم", color = textPrimary, fontFamily = CairoFont, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "هل أنت متأكد من حذف الحساب \"${userToDelete?.fullName}\"؟ لن يتمكن من تسجيل الدخول للنظام.",
                    color = textMuted,
                    fontFamily = CairoFont
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.let { viewModel.deleteUser(it.id) }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("نعم، حذف الحساب", fontFamily = CairoFont)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("إلغاء", color = textPrimary, fontFamily = CairoFont)
                }
            },
            containerColor = cardBg
        )
    }
}

@Composable
private fun UserCardItem(
    user: User,
    isCurrentUser: Boolean,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("user_card_${user.username}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCurrentUser) primaryColor else primaryColor.copy(alpha = 0.2f)
        )
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
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(primaryColor.copy(alpha = 0.15f))
                            .border(1.dp, primaryColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (user.role) {
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                UserRole.ACCOUNTANT -> Icons.Default.AccountBalance
                                UserRole.CASHIER -> Icons.Default.PointOfSale
                                UserRole.STOREKEEPER -> Icons.Default.Inventory
                                UserRole.REPORTS_VIEWER -> Icons.Default.Analytics
                            },
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                color = textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                fontFamily = CairoFont
                            )
                            if (isCurrentUser) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = primaryColor.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "حسابك الحالي",
                                        color = primaryColor,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontFamily = CairoFont
                                    )
                                }
                            }
                        }

                        Text(
                            text = "@${user.username} • ${user.role.titleAr}",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontFamily = CairoFont
                        )
                    }
                }

                // Active / Inactive Badge
                Surface(
                    color = if (user.isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (user.isActive) Color(0xFF10B981) else Color(0xFFEF4444)
                    )
                ) {
                    Text(
                        text = if (user.isActive) "نشط ومفعل" else "معطل",
                        color = if (user.isActive) Color(0xFF10B981) else Color(0xFFEF4444),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontFamily = CairoFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Allowed Screens Chips Preview
            Text(
                text = "الشاشات المسموحة:",
                color = textMuted,
                fontSize = 11.sp,
                fontFamily = CairoFont
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AppScreen.values().take(5).forEach { screen ->
                    val canAccess = user.canAccessScreen(screen)
                    Surface(
                        color = if (canAccess) primaryColor.copy(alpha = 0.12f) else Color.Transparent,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (canAccess) primaryColor else Color(0xFF333333)
                        )
                    ) {
                        Text(
                            text = screen.titleAr,
                            color = if (canAccess) textPrimary else textMuted.copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            fontFamily = CairoFont
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = primaryColor.copy(alpha = 0.15f), thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (user.id != "usr_admin") {
                    TextButton(onClick = onToggleActive) {
                        Text(
                            text = if (user.isActive) "تعطيل الحساب" else "تفعيل الحساب",
                            color = if (user.isActive) Color(0xFFF59E0B) else Color(0xFF10B981),
                            fontSize = 11.sp,
                            fontFamily = CairoFont
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل الصلاحيات", tint = primaryColor, modifier = Modifier.size(18.dp))
                    }
                    if (user.id != "usr_admin") {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserFormDialog(
    title: String,
    initialUser: User?,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onDismiss: () -> Unit,
    onSave: (username: String, pass: String, name: String, role: UserRole, phone: String, perms: Map<String, List<String>>) -> Unit
) {
    var username by remember { mutableStateOf(initialUser?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf(initialUser?.fullName ?: "") }
    var phone by remember { mutableStateOf(initialUser?.phone ?: "") }
    var selectedRole by remember { mutableStateOf(initialUser?.role ?: UserRole.CASHIER) }

    // Map of Screen -> mutable set of actions
    var permissionsMap by remember {
        mutableStateOf<Map<AppScreen, Set<ScreenAction>>>(
            if (initialUser?.customPermissions?.isNotEmpty() == true) {
                initialUser.customPermissions.mapNotNull { (k, v) ->
                    try {
                        val screen = AppScreen.valueOf(k)
                        val actions = v.mapNotNull { act ->
                            try { ScreenAction.valueOf(act) } catch (e: Exception) { null }
                        }.toSet()
                        screen to actions
                    } catch (e: Exception) { null }
                }.toMap()
            } else {
                getDefaultRolePermissions(initialUser?.role ?: UserRole.CASHIER)
            }
        )
    }

    var showSavedNotice by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.98f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(22.dp)),
            color = cardBg
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    item {
                        Text(
                            text = title,
                            color = primaryColor,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CairoFont
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = { Text("الاسم الكامل للمستخدم", fontFamily = CairoFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("اسم المستخدم (Username)", fontFamily = CairoFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = {
                                Text(
                                    if (initialUser == null) "كلمة المرور (مطلوبة)" else "كلمة المرور الجديدة (اتركه فارغاً للإبقاء)",
                                    fontFamily = CairoFont
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الجوال", fontFamily = CairoFont) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Ready Roles Selector
                    item {
                        Text(
                            text = "اختر الدور الجاهز (يتم ضبط الصلاحيات تلقائياً):",
                            color = primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFont
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            UserRole.values().forEach { role ->
                                val isSelected = selectedRole == role
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRole = role
                                            permissionsMap = getDefaultRolePermissions(role)
                                        },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) primaryColor.copy(alpha = 0.15f) else Color.Transparent,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) primaryColor else Color(0xFF333333)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = {
                                                selectedRole = role
                                                permissionsMap = getDefaultRolePermissions(role)
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = primaryColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(role.titleAr, color = textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, fontFamily = CairoFont)
                                            Text(role.descriptionAr, color = textMuted, fontSize = 10.sp, fontFamily = CairoFont)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Advanced Permissions Matrix
                    item {
                        Text(
                            text = "مصفوفة الصلاحيات والعمليات لكل شاشة:",
                            color = primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = CairoFont
                        )
                        Text(
                            text = "يمكنك تخصيص العمليات المسموحة (عرض، إضافة، تعديل، حذف، طباعة، اعتماد).",
                            color = textMuted,
                            fontSize = 10.sp,
                            fontFamily = CairoFont
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    items(AppScreen.values()) { screen ->
                        val currentActions = permissionsMap[screen] ?: emptySet()
                        val hasAccess = currentActions.isNotEmpty()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF141414)),
                            border = androidx.compose.foundation.BorderStroke(
                                0.8.dp,
                                if (hasAccess) primaryColor.copy(alpha = 0.5f) else Color(0xFF282828)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = hasAccess,
                                            onCheckedChange = { checked ->
                                                val newMap = permissionsMap.toMutableMap()
                                                if (checked) {
                                                    newMap[screen] = setOf(ScreenAction.VIEW)
                                                } else {
                                                    newMap.remove(screen)
                                                }
                                                permissionsMap = newMap
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = primaryColor)
                                        )
                                        Text(
                                            text = screen.titleAr,
                                            color = if (hasAccess) primaryColor else textMuted,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            fontFamily = CairoFont
                                        )
                                    }

                                    Text(
                                        text = if (hasAccess) "مسموحة (${currentActions.size} عمليات)" else "مخفية تماماً",
                                        color = if (hasAccess) Color(0xFF10B981) else Color(0xFFEF4444),
                                        fontSize = 10.sp,
                                        fontFamily = CairoFont
                                    )
                                }

                                if (hasAccess) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        ScreenAction.values().forEach { action ->
                                            val isActionAllowed = currentActions.contains(action)
                                            FilterChip(
                                                selected = isActionAllowed,
                                                onClick = {
                                                    val newActions = if (isActionAllowed) {
                                                        currentActions - action
                                                    } else {
                                                        currentActions + action
                                                    }
                                                    val newMap = permissionsMap.toMutableMap()
                                                    if (newActions.isEmpty()) {
                                                        newMap.remove(screen)
                                                    } else {
                                                        newMap[screen] = newActions
                                                    }
                                                    permissionsMap = newMap
                                                },
                                                label = {
                                                    Text(action.titleAr, fontSize = 9.sp, fontFamily = CairoFont)
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = primaryColor,
                                                    selectedLabelColor = TextDark
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sticky Bottom Save Bar (Requirement 6)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    color = cardBg,
                    tonalElevation = 16.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = textPrimary)
                        ) {
                            Text("إلغاء", fontFamily = CairoFont)
                        }

                        Button(
                            onClick = {
                                if (fullName.isBlank() || username.isBlank() || (initialUser == null && password.isBlank())) {
                                    return@Button
                                }
                                val permsMapString = permissionsMap.mapKeys { it.key.name }
                                    .mapValues { entry -> entry.value.map { it.name } }

                                onSave(username, password, fullName, selectedRole, phone, permsMapString)
                                showSavedNotice = true
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(48.dp)
                                .testTag("save_user_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = TextDark)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = TextDark)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "حفظ المستخدم والصلاحيات",
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getDefaultRolePermissions(role: UserRole): Map<AppScreen, Set<ScreenAction>> {
    return when (role) {
        UserRole.ADMIN -> AppScreen.values().associateWith { ScreenAction.values().toSet() }
        UserRole.ACCOUNTANT -> mapOf(
            AppScreen.HOME to setOf(ScreenAction.VIEW, ScreenAction.PRINT),
            AppScreen.POS to setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.PRINT),
            AppScreen.INVOICES to setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.EDIT, ScreenAction.PRINT, ScreenAction.APPROVE),
            AppScreen.ACCOUNTS to setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.EDIT, ScreenAction.PRINT, ScreenAction.APPROVE),
            AppScreen.REPORTS to setOf(ScreenAction.VIEW, ScreenAction.PRINT)
        )
        UserRole.CASHIER -> mapOf(
            AppScreen.HOME to setOf(ScreenAction.VIEW),
            AppScreen.POS to setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.PRINT),
            AppScreen.PRODUCTS to setOf(ScreenAction.VIEW),
            AppScreen.INVOICES to setOf(ScreenAction.VIEW, ScreenAction.PRINT)
        )
        UserRole.STOREKEEPER -> mapOf(
            AppScreen.HOME to setOf(ScreenAction.VIEW),
            AppScreen.PRODUCTS to setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.EDIT, ScreenAction.PRINT),
            AppScreen.INVENTORY to setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.EDIT, ScreenAction.PRINT)
        )
        UserRole.REPORTS_VIEWER -> mapOf(
            AppScreen.HOME to setOf(ScreenAction.VIEW),
            AppScreen.REPORTS to setOf(ScreenAction.VIEW, ScreenAction.PRINT)
        )
    }
}
