package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SyncStatus
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SyncStatusDialog(
    viewModel: StoreViewModel,
    onDismiss: () -> Unit
) {
    val syncStatus by viewModel.syncStatus.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val firebaseConfig by viewModel.firebaseConfig.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val customers by viewModel.customers.collectAsState()

    val formattedSyncTime = remember(lastSyncTime) {
        SimpleDateFormat("yyyy/MM/dd - hh:mm:ss a", Locale("ar")).format(Date(lastSyncTime))
    }

    val rotation = rememberInfiniteTransition(label = "sync_rotation")
    val angle by rotation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, LuxuryGold, RoundedCornerShape(22.dp))
                .testTag("sync_status_dialog"),
            color = DarkCardBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated Status Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, if (syncStatus == SyncStatus.SYNCING) LuxuryGold else Color(0xFF4CAF50), CircleShape)
                        .background(LuxuryGoldSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (syncStatus == SyncStatus.SYNCING) Icons.Default.Sync else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (syncStatus == SyncStatus.SYNCING) LuxuryGold else Color(0xFF4CAF50),
                        modifier = Modifier
                            .size(28.dp)
                            .then(if (syncStatus == SyncStatus.SYNCING) Modifier.rotate(angle) else Modifier)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "حالة المزامنة والحفظ التلقائي",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = CairoFont
                )

                Text(
                    text = syncStatus.labelAr,
                    color = if (syncStatus == SyncStatus.SYNCING) LuxuryGold else Color(0xFF81C784),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = CairoFont
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Surface
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF141414),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262626))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("آخر حفظ ومزامنة:", color = Color(0xFFAAAAAA), fontSize = 12.sp, fontFamily = CairoFont)
                            Text(formattedSyncTime, color = LuxuryGold, fontSize = 11.sp, fontFamily = CairoFont, fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(color = Color(0xFF222222))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("البيانات المحفوظة محلياً:", color = Color(0xFFAAAAAA), fontSize = 12.sp, fontFamily = CairoFont)
                            Text("${products.size} صنف • ${orders.size} فاتورة", color = Color.White, fontSize = 12.sp, fontFamily = CairoFont)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("العملاء والحسابات:", color = Color(0xFFAAAAAA), fontSize = 12.sp, fontFamily = CairoFont)
                            Text("${customers.size} عميل مسجل", color = Color.White, fontSize = 12.sp, fontFamily = CairoFont)
                        }

                        HorizontalDivider(color = Color(0xFF222222))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("جسر Firebase Firestore:", color = Color(0xFFAAAAAA), fontSize = 12.sp, fontFamily = CairoFont)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (firebaseConfig.isEnabled) Color(0xFF1B5E20) else Color(0xFF333333)
                            ) {
                                Text(
                                    text = if (firebaseConfig.isEnabled) "نشط (${firebaseConfig.projectId})" else "جاهز للربط",
                                    color = if (firebaseConfig.isEnabled) Color(0xFFC8E6C9) else Color(0xFFAAAAAA),
                                    fontSize = 10.sp,
                                    fontFamily = CairoFont,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.manualSync() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("manual_sync_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuxuryGold,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مزامنة فورية الآن", fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = CairoFont)
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(0.6f)
                            .height(44.dp)
                            .testTag("close_sync_dialog"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("إغلاق", fontSize = 12.sp, fontFamily = CairoFont)
                    }
                }
            }
        }
    }
}
