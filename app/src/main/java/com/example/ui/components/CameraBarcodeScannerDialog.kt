package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CairoFont

@Composable
fun CameraBarcodeScannerDialog(
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    allowGenerateNew: Boolean = true,
    sampleBarcodes: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    var manualBarcode by remember { mutableStateOf("") }
    var hasCameraPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "تم تفعيل الكاميرا لمسح الباركود", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(android.Manifest.permission.CAMERA)
    }

    // Laser scanning animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_anim"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(24.dp))
                .testTag("camera_barcode_dialog"),
            color = cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = textPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "كاميرا ومسح الباركود",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated Viewfinder Box with Laser Beam
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0A0A0A))
                        .border(1.5.dp, primaryColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Scanning Reticle corners
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(2.dp, primaryColor, RoundedCornerShape(12.dp))
                    )

                    // Laser line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(2.5.dp)
                            .offset(y = (laserOffsetY - 90).dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color.Transparent, primaryColor, Color(0xFFEF4444), primaryColor, Color.Transparent)
                                )
                            )
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.QrCodeScanner,
                            contentDescription = null,
                            tint = primaryColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "وجّه الكاميرا نحو الباركود ليتم التقاطه فوراً",
                            color = textMuted,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = CairoFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Secondary Button: Generate New Barcode (Requirement 5)
                if (allowGenerateNew) {
                    OutlinedButton(
                        onClick = {
                            val newBarcode = "628" + (10000000..99999999).random().toString()
                            Toast.makeText(context, "تم توليد باركود جديد: $newBarcode", Toast.LENGTH_SHORT).show()
                            onBarcodeScanned(newBarcode)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("generate_new_barcode_secondary_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, primaryColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "توليد باركود جديد لمن لا يملك باركود",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Sample Quick Pick Chips (Useful for quick selection in preview / tests)
                if (sampleBarcodes.isNotEmpty()) {
                    Text(
                        text = "أو اختر باركود سريع من الأصناف:",
                        fontSize = 11.sp,
                        color = textMuted,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right,
                        fontFamily = CairoFont
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(sampleBarcodes) { code ->
                            SuggestionChip(
                                onClick = {
                                    manualBarcode = code
                                    onBarcodeScanned(code)
                                },
                                label = { Text(code, fontSize = 10.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = Color(0xFF1E1E1E),
                                    labelColor = primaryColor
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Manual barcode input & confirm
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualBarcode,
                        onValueChange = { manualBarcode = it },
                        placeholder = { Text("أو اكتب الباركود يدوياً...", fontSize = 11.sp, fontFamily = CairoFont) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            if (manualBarcode.isNotBlank()) {
                                onBarcodeScanned(manualBarcode.trim())
                            } else {
                                Toast.makeText(context, "الرجاء إدخال الباركود", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text("تأكيد", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            }
        }
    }
}
