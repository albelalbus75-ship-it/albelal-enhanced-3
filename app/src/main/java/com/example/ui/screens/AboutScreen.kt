package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun AboutScreen(
    viewModel: StoreViewModel,
    onNavigateBack: () -> Unit
) {
    val branding by viewModel.brandingTheme.collectAsState()
    val context = LocalContext.current
    val primaryColor = parseHexColor(branding.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(branding.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(branding.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(branding.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(branding.textMutedColorHex, TextMuted)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .testTag("about_screen")
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(cardBg)
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الرجوع",
                    tint = primaryColor
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "عن التطبيق والمصمم",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryColor,
                    fontFamily = CairoFont
                )
                Text(
                    text = "بيانات الهوية والاتصال والدعم الفني",
                    fontSize = 11.sp,
                    color = textMuted,
                    fontFamily = CairoFont
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App & Designer Logo Avatar
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(26.dp))
                    .background(primaryColor.copy(alpha = 0.15f))
                    .border(2.dp, primaryColor, RoundedCornerShape(26.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (branding.logoPreset) {
                        "diamond" -> Icons.Default.Diamond
                        "shield" -> Icons.Default.Shield
                        "store" -> Icons.Default.Storefront
                        else -> Icons.Default.WorkspacePremium
                    },
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Name & Version
            Text(
                text = branding.appName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = textPrimary,
                textAlign = TextAlign.Center,
                fontFamily = CairoFont
            )

            Surface(
                color = primaryColor.copy(alpha = 0.16f),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.4f)),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "نظام تخطيط موارد المؤسسات المحاسبي • ${branding.appVersion}",
                    color = primaryColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    fontFamily = CairoFont
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Designer Information Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "مصمم ومطور النظام",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = branding.designerName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth(),
                        fontFamily = CairoFont
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "تم بناء وتطوير هذا النظام المحاسبي المتكامل بأعلى معايير إدارة الأعمال، مع دعم شجرة الحسابات والقيود اليومية وسندات الصرف والقبض وتقارير الأرباح والميزانية والمزامنة الفورية للبيانات.",
                        fontSize = 12.sp,
                        color = textMuted,
                        textAlign = TextAlign.Right,
                        lineHeight = 18.sp,
                        fontFamily = CairoFont
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact Actions Grid
            Text(
                text = "بيانات التواصل والدعم المباشر",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Right,
                fontFamily = CairoFont
            )

            Spacer(modifier = Modifier.height(10.dp))

            ContactActionCard(
                title = "الاتصال الهاتفي",
                value = branding.phoneContact,
                icon = Icons.Default.Phone,
                primaryColor = primaryColor,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${branding.phoneContact}"))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContactActionCard(
                title = "واتساب الدعم الفني",
                value = branding.whatsappContact,
                icon = Icons.Default.Chat,
                primaryColor = primaryColor,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted,
                onClick = {
                    try {
                        val clean = branding.whatsappContact.replace("+", "").replace(" ", "")
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$clean"))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContactActionCard(
                title = "البريد الإلكتروني",
                value = branding.emailContact,
                icon = Icons.Default.Email,
                primaryColor = primaryColor,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted,
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${branding.emailContact}"))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContactActionCard(
                title = "الموقع الإلكتروني",
                value = branding.websiteContact,
                icon = Icons.Default.Language,
                primaryColor = primaryColor,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted,
                onClick = {
                    try {
                        val url = if (!branding.websiteContact.startsWith("http")) "https://${branding.websiteContact}" else branding.websiteContact
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    } catch (e: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "جميع الحقوق محفوظة © ${branding.designerName}",
                fontSize = 11.sp,
                color = textMuted,
                textAlign = TextAlign.Center,
                fontFamily = CairoFont
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ContactActionCard(
    title: String,
    value: String,
    icon: ImageVector,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.2f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(primaryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = textMuted,
                    fontFamily = CairoFont
                )
                Text(
                    text = value,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    fontFamily = CairoFont
                )
            }
        }
    }
}
