package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AddCustomerDialog
import com.example.ui.components.QuickAddCartItemDialog
import com.example.ui.components.ReceiptDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun CartScreen(
    viewModel: StoreViewModel,
    onNavigateToSales: () -> Unit
) {
    val cart by viewModel.cart.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val selectedPayment by viewModel.selectedPayment.collectAsState()
    val discount by viewModel.discount.collectAsState()
    val completedOrder by viewModel.completedOrder.collectAsState()
    val notice by viewModel.notice.collectAsState()
    val branding by viewModel.brandingTheme.collectAsState()
    val currencies by viewModel.currencies.collectAsState()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()

    val primaryColor = parseHexColor(branding.primaryColorHex, LuxuryGold)
    val bgColor = parseHexColor(branding.backgroundColorHex, DarkBackground)
    val cardBg = parseHexColor(branding.cardBackgroundColorHex, DarkCardBackground)
    val textPrimary = parseHexColor(branding.textPrimaryColorHex, TextWhite)
    val textMuted = parseHexColor(branding.textMutedColorHex, TextMuted)

    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var showQuickAddDialog by remember { mutableStateOf(false) }

    val currencyRate = selectedCurrency.exchangeRate
    val currencySymbol = selectedCurrency.symbol

    val totalItems = cart.sumOf { it.quantity }
    val subtotal = cart.sumOf { it.totalPrice }
    val vat = subtotal * 0.15
    val grandTotal = maxOf(0.0, subtotal + vat - discount)

    val convertedGrandTotal = grandTotal * currencyRate
    val convertedSubtotal = subtotal * currencyRate
    val convertedVat = vat * currencyRate

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .testTag("cart_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Screen Header
            Surface(
                color = bgColor,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (cart.isNotEmpty()) {
                        Surface(
                            color = primaryColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${"%,.2f".format(convertedGrandTotal)} $currencySymbol",
                                color = primaryColor,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                fontFamily = CairoFont,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "سلة المبيعات",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                        Text(
                            text = if (cart.isEmpty()) "السلة فارغة" else "$totalItems عناصر في الفاتورة",
                            fontSize = 11.sp,
                            color = textMuted,
                            fontFamily = CairoFont
                        )
                    }
                }
            }

            // Notice Banner
            notice?.let { msg ->
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
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = LuxuryGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = msg,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LuxuryGoldLight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (cart.isEmpty()) {
                // Empty Cart State
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 90.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, LuxuryGold.copy(alpha = 0.5f), CircleShape)
                                .background(DarkCardBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = LuxuryGold,
                                modifier = Modifier.size(42.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "السلة فارغة حالياً",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تصفح شبكة المنتجات وأضف الأصناف التي ترغب في بيعها.",
                            fontSize = 13.sp,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showQuickAddDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor),
                                border = androidx.compose.foundation.BorderStroke(1.2.dp, primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("quick_add_empty_cart_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = primaryColor)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة صنف جديد +", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                            }

                            Button(
                                onClick = onNavigateToSales,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("browse_products_btn")
                            ) {
                                Text(text = "تصفح المنتجات", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Lines Card
                    item {
                        Surface(
                            color = cardBg,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        TextButton(
                                            onClick = { showQuickAddDialog = true },
                                            modifier = Modifier.testTag("quick_add_active_cart_btn")
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = primaryColor)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "إضافة صنف +", color = primaryColor, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                        }

                                        TextButton(onClick = { viewModel.clearCart() }) {
                                            Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = ErrorRed)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "تفريغ السلة", color = ErrorRed, fontSize = 11.sp, fontFamily = CairoFont)
                                        }
                                    }

                                    Text(
                                        text = "الأصناف المختارة",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = primaryColor,
                                        fontFamily = CairoFont
                                    )
                                }

                                HorizontalDivider(color = Color(0xFF262626))
                                Spacer(modifier = Modifier.height(8.dp))

                                cart.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Quantity Stepper
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .border(1.dp, LuxuryGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                                                .background(DarkElevated)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            IconButton(
                                                onClick = { viewModel.updateQuantity(item.product.id, 1) },
                                                modifier = Modifier.size(28.dp).testTag("plus_item_${item.product.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = "زيادة", tint = LuxuryGold, modifier = Modifier.size(16.dp))
                                            }
                                            Text(
                                                text = "${item.quantity}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = LuxuryGoldLight,
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                            IconButton(
                                                onClick = { viewModel.updateQuantity(item.product.id, -1) },
                                                modifier = Modifier.size(28.dp).testTag("minus_item_${item.product.id}")
                                            ) {
                                                Icon(imageVector = Icons.Default.Remove, contentDescription = "تقليل", tint = LuxuryGold, modifier = Modifier.size(16.dp))
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        // Item details
                                        Column(
                                            modifier = Modifier.weight(1f),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = item.product.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Right
                                            )
                                            Text(
                                                text = "${"%,.2f".format(item.product.price)} ريال للوحدة",
                                                fontSize = 11.sp,
                                                color = LuxuryGold,
                                                textAlign = TextAlign.Right
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(DarkElevated)
                                                .border(0.5.dp, LuxuryGold.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (item.product.primaryImageUri != null) {
                                                coil.compose.AsyncImage(
                                                    model = item.product.primaryImageUri,
                                                    contentDescription = item.product.name,
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else if (item.product.imageResId != null) {
                                                androidx.compose.foundation.Image(
                                                    painter = androidx.compose.ui.res.painterResource(id = item.product.imageResId),
                                                    contentDescription = item.product.name,
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                androidx.compose.foundation.Image(
                                                    painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.faisal_dallah_logo_1792029175878),
                                                    contentDescription = item.product.name,
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Customer Selection
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(onClick = { showAddCustomerDialog = true }) {
                                    Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = LuxuryGold)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "عميل جديد", color = LuxuryGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Text(
                                    text = "العميل",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = LuxuryGold
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(customers) { cust ->
                                    val isSelected = selectedCustomer?.id == cust.id
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) LuxuryGoldSurface else DarkCardBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) LuxuryGold else Color(0xFF262626)),
                                        modifier = Modifier
                                            .clickable { viewModel.setCustomer(cust) }
                                            .testTag("customer_chip_${cust.id}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Text(
                                                text = cust.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) LuxuryGold else TextWhite
                                            )
                                            Text(
                                                text = cust.detail,
                                                fontSize = 10.sp,
                                                color = if (isSelected) LuxuryGoldLight else TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Payment Method Selection
                    item {
                        Column {
                            Text(
                                text = "طريقة الدفع",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = LuxuryGold,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val methods = listOf(
                                    Triple("نقدًا", Icons.Default.Payments, "نقدًا"),
                                    Triple("شبكة", Icons.Default.CreditCard, "شبكة"),
                                    Triple("آجل", Icons.Default.Schedule, "آجل")
                                )

                                methods.forEach { (name, icon, key) ->
                                    val isSelected = selectedPayment == key
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) LuxuryGold else DarkCardBackground,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) LuxuryGold else Color(0xFF262626)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(46.dp)
                                            .clickable { viewModel.setPayment(key) }
                                            .testTag("payment_method_$key")
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (isSelected) TextDark else LuxuryGold,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (isSelected) TextDark else TextWhite
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Order Summary Card
                    item {
                        Surface(
                            color = DarkCardBackground,
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGoldBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "ملخص الفاتورة",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    color = LuxuryGold,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${"%,.2f".format(subtotal)} ريال", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text(text = "المجموع قبل الضريبة", fontSize = 12.sp, color = TextMuted)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "${"%,.2f".format(vat)} ريال", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                    Text(text = "ضريبة القيمة المضافة (15%)", fontSize = 12.sp, color = TextMuted)
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color(0xFF262626))
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${"%,.2f".format(convertedGrandTotal)} $currencySymbol",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = primaryColor,
                                        fontFamily = CairoFont
                                    )
                                    Text(
                                        text = "المبلغ المطلوب إجمالاً",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textPrimary,
                                        fontFamily = CairoFont
                                    )
                                }
                            }
                        }
                    }

                    // Complete Sale Button
                    item {
                        Button(
                            onClick = { viewModel.finishSale() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = primaryColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("checkout_button")
                        ) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إتمام البيع • ${"%,.2f".format(convertedGrandTotal)} $currencySymbol",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }
            }
        }

        if (showAddCustomerDialog) {
            AddCustomerDialog(
                onDismiss = { showAddCustomerDialog = false },
                onConfirm = { name, phone, debt ->
                    viewModel.addNewCustomer(name, phone, debt)
                }
            )
        }

        // Quick Add Cart Item Dialog (Requirement 8)
        if (showQuickAddDialog) {
            QuickAddCartItemDialog(
                allProducts = allProducts,
                primaryColor = primaryColor,
                cardBg = cardBg,
                textPrimary = textPrimary,
                textMuted = textMuted,
                currencySymbol = currencySymbol,
                onDismiss = { showQuickAddDialog = false },
                onProductAdded = { prod, qty ->
                    repeat(qty) {
                        viewModel.addToCart(prod)
                    }
                }
            )
        }

        completedOrder?.let { ord ->
            ReceiptDialog(
                order = ord,
                branding = branding,
                currencies = currencies,
                currentCurrency = selectedCurrency,
                onDismiss = { viewModel.dismissCompletedOrder() },
                onUpdateCurrencyRate = { code, rate ->
                    viewModel.updateCurrencyExchangeRate(code, rate)
                }
            )
        }
    }
}
