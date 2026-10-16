package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun LoginDialog(
    viewModel: StoreViewModel,
    onDismiss: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, LuxuryGold, RoundedCornerShape(24.dp))
                .testTag("login_dialog"),
            color = DarkCardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, LuxuryGold, CircleShape)
                        .background(LuxuryGoldSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = LuxuryGold,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "تسجيل الدخول للنظام",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFont
                )

                Text(
                    text = "متجر الفيصل للتجارة العامة",
                    color = LuxuryGold,
                    fontSize = 13.sp,
                    fontFamily = CairoFont
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Error message
                if (errorMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF3B1515),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935))
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF8A80),
                            fontSize = 12.sp,
                            fontFamily = CairoFont,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Username field
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        errorMessage = null
                    },
                    label = { Text("اسم المستخدم", color = Color(0xFFAAAAAA), fontFamily = CairoFont) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = LuxuryGold)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("username_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = LuxuryGold
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Password field
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("كلمة المرور", color = Color(0xFFAAAAAA), fontFamily = CairoFont) },
                    leadingIcon = {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = LuxuryGold)
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = Color(0xFF888888)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = LuxuryGold
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Submit Button
                Button(
                    onClick = {
                        val ok = viewModel.login(username, password)
                        if (ok) {
                            onDismiss()
                        } else {
                            errorMessage = "اسم المستخدم أو كلمة المرور غير صحيحة، أو الحساب معطل"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_login_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuxuryGold,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "دخول للنظام",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = CairoFont
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider(color = Color(0xFF222222))

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "تسجيل دخول سريع للتجربة والتحقق:",
                    color = Color(0xFF888888),
                    fontSize = 11.sp,
                    fontFamily = CairoFont
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Admin
                    OutlinedButton(
                        onClick = {
                            username = "admin"
                            password = "1234"
                            val ok = viewModel.login("admin", "1234")
                            if (ok) onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_login_admin"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGold),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LuxuryGold)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("المدير (Admin)", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = CairoFont)
                            Text("admin / 1234", fontSize = 9.sp, color = Color(0xFFAAAAAA))
                        }
                    }

                    // Quick Cashier
                    OutlinedButton(
                        onClick = {
                            username = "cashier"
                            password = "1234"
                            val ok = viewModel.login("cashier", "1234")
                            if (ok) onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_login_cashier"),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF666666)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("كاشير (POS)", fontWeight = FontWeight.Bold, fontSize = 11.sp, fontFamily = CairoFont)
                            Text("cashier / 1234", fontSize = 9.sp, color = Color(0xFFAAAAAA))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_login_dialog")
                ) {
                    Text("إغلاق", color = Color(0xFF888888), fontFamily = CairoFont, fontSize = 12.sp)
                }
            }
        }
    }
}
