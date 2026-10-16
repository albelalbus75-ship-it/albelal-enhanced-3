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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Product
import com.example.ui.theme.CairoFont
import java.util.UUID

@Composable
fun QuickAddCartItemDialog(
    allProducts: List<Product>,
    primaryColor: Color,
    cardBg: Color,
    textPrimary: Color,
    textMuted: Color,
    currencySymbol: String = "ريال",
    onDismiss: () -> Unit,
    onProductAdded: (product: Product, quantity: Int) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("صنف مخصص جديد") } // "صنف مخصص جديد" or "من المخزون"

    // Custom new item fields
    var customName by remember { mutableStateOf("") }
    var customPriceStr by remember { mutableStateOf("") }
    var customQtyStr by remember { mutableStateOf("1") }
    var customCategory by remember { mutableStateOf("عام") }

    // Search inventory fields
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = remember(searchQuery, allProducts) {
        if (searchQuery.isBlank()) allProducts.take(8)
        else allProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.barcode.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, primaryColor, RoundedCornerShape(22.dp))
                .testTag("quick_add_cart_dialog"),
            color = cardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222222))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = textPrimary)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "إضافة صنف جديد للسلة",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            fontFamily = CairoFont
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("صنف مخصص جديد", "اختيار من المخزون").forEach { tab ->
                        val isSel = selectedTab == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedTab = tab },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) primaryColor else Color(0xFF1E1E1E)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSel) Color.Black else textPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                fontFamily = CairoFont
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == "صنف مخصص جديد") {
                    // Create New Custom Product Directly
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("اسم الصنف الجديد *", fontFamily = CairoFont) },
                        placeholder = { Text("مثال: قهوة خولاني ممتازة", fontFamily = CairoFont) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customPriceStr,
                            onValueChange = { customPriceStr = it },
                            label = { Text("السعر ($currencySymbol)", fontFamily = CairoFont) },
                            placeholder = { Text("0.00") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = customQtyStr,
                            onValueChange = { customQtyStr = it },
                            label = { Text("الكمية", fontFamily = CairoFont) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customCategory,
                        onValueChange = { customCategory = it },
                        label = { Text("القسم / التصنيف", fontFamily = CairoFont) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val price = customPriceStr.toDoubleOrNull()
                            val qty = customQtyStr.toIntOrNull() ?: 1
                            if (customName.isBlank() || price == null || price <= 0.0) {
                                Toast.makeText(context, "الرجاء إدخال اسم الصنف والسعر بشكل صحيح", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val newProd = Product(
                                id = "cust_" + UUID.randomUUID().toString().take(6),
                                name = customName.trim(),
                                category = customCategory.trim().ifEmpty { "عام" },
                                price = price,
                                costPrice = price * 0.75,
                                wholesalePrice = price * 0.9,
                                stock = 50,
                                unit = "حبة",
                                barcode = "628" + (10000000..99999999).random().toString()
                            )

                            onProductAdded(newProd, maxOf(1, qty))
                            Toast.makeText(context, "تمت إضافة \"${newProd.name}\" إلى السلة فوراً", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("إضافة مباشرة إلى السلة", fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                    }
                } else {
                    // Pick from Inventory
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("ابحث في المخزون...", fontFamily = CairoFont) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryColor) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filteredProducts) { prod ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF161616),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262626)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onProductAdded(prod, 1)
                                        Toast.makeText(context, "تمت إضافة ${prod.name} للسلة", Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            onProductAdded(prod, 1)
                                            Toast.makeText(context, "تمت إضافة ${prod.name} للسلة", Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("+ إضافة", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = CairoFont)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = textPrimary, fontFamily = CairoFont)
                                        Text("${"%,.2f".format(prod.price)} $currencySymbol • متاح: ${prod.stock}", fontSize = 11.sp, color = primaryColor, fontFamily = CairoFont)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
