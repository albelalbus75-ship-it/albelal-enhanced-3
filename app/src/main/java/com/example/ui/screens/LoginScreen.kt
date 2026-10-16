package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun LoginScreen(
    viewModel: StoreViewModel,
    onLoginSuccess: () -> Unit = {}
) {
    val brandingTheme by viewModel.brandingTheme.collectAsState()
    val primaryColor = parseHexColor(brandingTheme.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(brandingTheme.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(brandingTheme.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(brandingTheme.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(brandingTheme.textMutedColorHex, TextMuted)

    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("1234") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    fun doLogin(u: String, p: String) {
        if (u.isBlank() || p.isBlank()) {
            errorMessage = "الرجاء إدخال اسم المستخدم وكلمة المرور"
            return
        }
        isLoading = true
        errorMessage = null
        try {
            val success = viewModel.login(u, p)
            if (success) {
                onLoginSuccess()
            } else {
                errorMessage = "اسم المستخدم أو كلمة المرور غير صحيحة"
            }
        } catch (e: Throwable) {
            errorMessage = "تعذر تسجيل الدخول: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("login_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Icon & Logo
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                primaryColor.copy(alpha = 0.35f),
                                cardBg
                            )
                        )
                    )
                    .border(2.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "ERP Logo",
                    tint = primaryColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = brandingTheme.appName.ifBlank { "متجر الفيصل للتجارة العامة" },
                color = textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFont,
                textAlign = TextAlign.Center
            )

            Text(
                text = "نظام ERP المحاسبي وإدارة نقاط البيع والمستودعات",
                color = primaryColor,
                fontSize = 13.sp,
                fontFamily = CairoFont,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Login Card Form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل الدخول إلى النظام",
                        color = textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = CairoFont
                    )

                    Text(
                        text = "أدخل بيانات حسابك للمتابعة",
                        color = textMuted,
                        fontSize = 12.sp,
                        fontFamily = CairoFont
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (errorMessage != null) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp)),
                            color = LuxuryRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryRed)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Error, contentDescription = null, tint = LuxuryRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = LuxuryRed,
                                    fontSize = 12.sp,
                                    fontFamily = CairoFont
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Username Input
                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("اسم المستخدم", fontFamily = CairoFont) },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = textMuted.copy(alpha = 0.4f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = textMuted,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("كلمة المرور", fontFamily = CairoFont) },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = primaryColor)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = textMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = textMuted.copy(alpha = 0.4f),
                            focusedLabelColor = primaryColor,
                            unfocusedLabelColor = textMuted,
                            focusedTextColor = textPrimary,
                            unfocusedTextColor = textPrimary
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { doLogin(username, password) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Login Button
                    Button(
                        onClick = { doLogin(username, password) },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = DarkBackground,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, tint = DarkBackground)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "تسجيل الدخول",
                                    color = DarkBackground,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = CairoFont
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Enter as Admin (Instant Preview Access)
                    OutlinedButton(
                        onClick = {
                            username = "admin"
                            password = "1234"
                            doLogin("admin", "1234")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("quick_admin_login")
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "الدخول السريع كمدير النظام (admin / 1234)",
                            color = primaryColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = CairoFont
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Role Choosers
            Text(
                text = "حسابات تجريبية سريعة بنقرة واحدة (كلمة المرور: 1234):",
                color = textMuted,
                fontSize = 12.sp,
                fontFamily = CairoFont,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val demoUsers = listOf(
                    Triple("المدير العام", "admin", Icons.Default.AdminPanelSettings),
                    Triple("محاسب مالي", "accountant", Icons.Default.Calculate),
                    Triple("كاشير مبيعات", "cashier", Icons.Default.PointOfSale),
                    Triple("أمين مستودع", "storekeeper", Icons.Default.Inventory2),
                    Triple("مشاهد تقارير", "viewer", Icons.Default.Analytics)
                )

                demoUsers.forEach { (roleTitle, uName, icon) ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                username = uName
                                password = "1234"
                                doLogin(uName, "1234")
                            },
                        color = cardBg.copy(alpha = 0.7f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = roleTitle,
                                    color = textPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    fontFamily = CairoFont
                                )
                            }
                            Text(
                                text = "تسجيل دخول ($uName)",
                                color = primaryColor,
                                fontSize = 11.sp,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Footer
            Text(
                text = "${brandingTheme.appName} • ${brandingTheme.appVersion}",
                color = textMuted.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = CairoFont
            )
        }
    }
}
