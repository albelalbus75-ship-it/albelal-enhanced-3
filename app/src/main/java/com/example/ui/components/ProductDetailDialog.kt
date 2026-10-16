package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.theme.*

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGoldBorder),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag("product_detail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header row with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkElevated)
                            .testTag("close_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = LuxuryGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Surface(
                        color = LuxuryGoldSurface,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, LuxuryGold),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = product.category,
                            color = LuxuryGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product image or box
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, LuxuryGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F0F0F)),
                    contentAlignment = Alignment.Center
                ) {
                    if (product.primaryImageUri != null) {
                        coil.compose.AsyncImage(
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
                        Image(
                            painter = painterResource(id = com.example.R.drawable.faisal_dallah_logo_1792029175878),
                            contentDescription = product.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(76.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Product Name
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    ),
                    color = TextWhite
                )

                if (product.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = product.note,
                        fontSize = 12.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price display
                Text(
                    text = "${"%,.2f".format(product.price)} ريال",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = LuxuryGold
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Specs Container
                Surface(
                    color = DarkElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2E2E2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "المخزون المتوفر", fontSize = 10.sp, color = TextMuted)
                            Text(
                                text = "${product.stock} ${product.unit}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = LuxuryGoldLight
                            )
                        }

                        if (product.barcode.isNotBlank()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "الباركود", fontSize = 10.sp, color = TextMuted)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Outlined.QrCode,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = LuxuryGold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = product.barcode,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextWhite
                                    )
                                }
                            }
                        }

                        if (product.model.isNotBlank()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "الموديل", fontSize = 10.sp, color = TextMuted)
                                Text(
                                    text = product.model,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextWhite
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Quantity selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = { if (quantity > 1) quantity-- },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkElevated)
                            .border(1.dp, LuxuryGoldBorder.copy(alpha = 0.5f), CircleShape)
                            .testTag("quantity_decrease")
                    ) {
                        Icon(imageVector = Icons.Default.Remove, contentDescription = "تقليل", tint = LuxuryGold)
                    }

                    Text(
                        text = "$quantity",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = LuxuryGold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    IconButton(
                        onClick = { if (quantity < product.stock) quantity++ },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DarkElevated)
                            .border(1.dp, LuxuryGoldBorder.copy(alpha = 0.5f), CircleShape)
                            .testTag("quantity_increase")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة", tint = LuxuryGold)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Add to cart button
                Button(
                    onClick = {
                        onAddToCart(quantity)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LuxuryGold,
                        contentColor = TextDark
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("add_to_cart_dialog_btn")
                ) {
                    Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, tint = TextDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة إلى السلة • ${"%,.2f".format(product.price * quantity)} ريال",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = TextDark
                    )
                }
            }
        }
    }
}
