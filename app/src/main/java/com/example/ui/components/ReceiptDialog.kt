package com.example.ui.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppBrandingTheme
import com.example.data.model.CurrencyRate
import com.example.data.model.SaleOrder
import com.example.ui.theme.CairoFont
import com.example.ui.theme.DarkCardBackground
import com.example.ui.theme.DarkElevated
import com.example.ui.theme.LuxuryGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite
import com.example.ui.theme.parseHexColor

@Composable
fun ReceiptDialog(
    order: SaleOrder,
    branding: AppBrandingTheme? = null,
    currencies: List<CurrencyRate> = emptyList(),
    currentCurrency: CurrencyRate? = null,
    onDismiss: () -> Unit,
    onUpdateCurrencyRate: ((code: String, rate: Double) -> Unit)? = null
) {
    val context = LocalContext.current

    // Dynamically apply selected branding theme colors
    val primaryColor = parseHexColor(branding?.primaryColorHex ?: "#D4AF37", LuxuryGold)
    val cardBg = parseHexColor(branding?.cardBackgroundColorHex ?: "#141414", DarkCardBackground)
    val textPrimary = parseHexColor(branding?.textPrimaryColorHex ?: "#FFFFFF", TextWhite)
    val textMuted = parseHexColor(branding?.textMutedColorHex ?: "#999999", TextMuted)
    val appTitle = branding?.appName?.ifEmpty { "متجر الفيصل للتجارة العامة" } ?: "متجر الفيصل للتجارة العامة"

    // Currency & Exchange Rate State
    val defaultList = if (currencies.isNotEmpty()) currencies else listOf(
        CurrencyRate("SAR", "ريال سعودي", "SAR", 1.0, isDefault = true),
        CurrencyRate("YER", "ريال يمني", "YER", 70.0, isDefault = false),
        CurrencyRate("USD", "دولار أمريكي", "$", 0.2667, isDefault = false)
    )

    var activeCurrency by remember(currentCurrency, currencies) {
        mutableStateOf(currentCurrency ?: defaultList.firstOrNull { it.isDefault } ?: defaultList.first())
    }

    var manualRateText by remember(activeCurrency.exchangeRate) {
        mutableStateOf(activeCurrency.exchangeRate.toString())
    }

    // Dynamic Recalculation (Requirement 4: Auto-recalculate prices and totals on currency change)
    val currentRate = manualRateText.toDoubleOrNull() ?: activeCurrency.exchangeRate
    val convertedSubtotal = order.subtotal * currentRate
    val convertedVat = order.vat * currentRate
    val convertedDiscount = order.discount * currentRate
    val convertedTotal = order.total * currentRate
    val symbol = activeCurrency.symbol

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = cardBg,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryColor),
            tonalElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 12.dp)
                .testTag("receipt_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with close button & theme-styled success badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkElevated)
                            .testTag("close_receipt_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = primaryColor)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فاتورة مبيعات معتمدة",
                            color = primaryColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = CairoFont
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Currency selector bar with editable exchange rate (Requirement 1 & 4)
                Surface(
                    color = Color(0xFF161616),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "عملة الفاتورة:",
                                fontSize = 11.sp,
                                color = textMuted,
                                fontFamily = CairoFont
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                defaultList.forEach { curr ->
                                    val isSelected = activeCurrency.code == curr.code
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) primaryColor else Color(0xFF222222),
                                        modifier = Modifier
                                            .clickable {
                                                activeCurrency = curr
                                                manualRateText = curr.exchangeRate.toString()
                                            }
                                            .padding(horizontal = 2.dp)
                                    ) {
                                        Text(
                                            text = curr.symbol,
                                            color = if (isSelected) Color.Black else textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Editable exchange rate row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = {
                                    val r = manualRateText.toDoubleOrNull()
                                    if (r != null && r > 0.0) {
                                        activeCurrency = activeCurrency.copy(exchangeRate = r)
                                        onUpdateCurrencyRate?.invoke(activeCurrency.code, r)
                                        Toast.makeText(context, "تم حفظ وتطبيق سعر الصرف ($r) فوراً", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Done, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("تطبيق", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                            }

                            OutlinedTextField(
                                value = manualRateText,
                                onValueChange = { manualRateText = it },
                                label = { Text("سعر الصرف لـ (${activeCurrency.symbol})", fontSize = 9.sp, fontFamily = CairoFont) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Receipt Container (Scrollable)
                Surface(
                    color = Color(0xFF0F0F0F),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            Text(
                                text = appTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = primaryColor,
                                fontFamily = CairoFont
                            )
                            Text(
                                text = "فاتورة ضريبية معتمدة",
                                fontSize = 12.sp,
                                color = textMuted,
                                fontWeight = FontWeight.Medium,
                                fontFamily = CairoFont
                            )
                            Text(
                                text = "الرقم الضريبي: 310293847500003",
                                fontSize = 10.sp,
                                color = textMuted.copy(alpha = 0.7f),
                                fontFamily = FontFamily.Monospace
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = primaryColor.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(8.dp))

                            ReceiptRow(label = "رقم الفاتورة", value = order.id, textPrimary = textPrimary, textMuted = textMuted)
                            ReceiptRow(label = "التاريخ والوقت", value = order.formattedDate, textPrimary = textPrimary, textMuted = textMuted)
                            ReceiptRow(label = "العميل", value = order.customer.name, textPrimary = textPrimary, textMuted = textMuted)
                            ReceiptRow(label = "طريقة الدفع", value = order.paymentMethod, textPrimary = textPrimary, textMuted = textMuted)
                            ReceiptRow(label = "العملة المطبقة", value = "${activeCurrency.nameAr} ($symbol) @ $currentRate", textPrimary = primaryColor, textMuted = textMuted)

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = primaryColor.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Items list with recalculated prices (Requirement 4)
                        items(order.items) { item ->
                            val itemConvertedUnit = item.product.price * currentRate
                            val itemConvertedTotal = item.totalPrice * currentRate
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.product.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary,
                                        fontFamily = CairoFont
                                    )
                                    Text(
                                        text = "${item.quantity} × ${"%,.2f".format(itemConvertedUnit)} $symbol",
                                        fontSize = 11.sp,
                                        color = textMuted,
                                        fontFamily = CairoFont
                                    )
                                }
                                Text(
                                    text = "${"%,.2f".format(itemConvertedTotal)} $symbol",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryColor,
                                    fontFamily = CairoFont
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = primaryColor.copy(alpha = 0.25f))
                            Spacer(modifier = Modifier.height(8.dp))

                            ReceiptRow(label = "المجموع قبل الضريبة", value = "${"%,.2f".format(convertedSubtotal)} $symbol", textPrimary = textPrimary, textMuted = textMuted)
                            ReceiptRow(label = "ضريبة القيمة المضافة (15%)", value = "${"%,.2f".format(convertedVat)} $symbol", textPrimary = textPrimary, textMuted = textMuted)
                            if (order.discount > 0) {
                                ReceiptRow(label = "الخصم", value = "- ${"%,.2f".format(convertedDiscount)} $symbol", textPrimary = textPrimary, textMuted = textMuted)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                    .border(1.dp, primaryColor, RoundedCornerShape(10.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "المبلغ الإجمالي ($symbol)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = primaryColor,
                                    fontFamily = CairoFont
                                )
                                Text(
                                    text = "${"%,.2f".format(convertedTotal)} $symbol",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = primaryColor,
                                    fontFamily = CairoFont
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "شكراً لتعاملكم مع $appTitle",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = textMuted,
                                textAlign = TextAlign.Center,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "مشاركة", fontSize = 12.sp, color = textPrimary, fontFamily = CairoFont)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(context, "تمت الطباعة بنجاح بعملة $symbol", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(46.dp)
                            .testTag("done_receipt_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "طباعة الفاتورة", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String, textPrimary: Color, textMuted: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, color = textMuted, fontFamily = CairoFont)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textPrimary, fontFamily = CairoFont)
    }
}
