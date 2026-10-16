package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Customer
import com.example.ui.theme.*

@Composable
fun PaymentSettlementDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onSettle: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkCardBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, LuxuryGoldBorder),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .testTag("payment_settlement_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "سداد دفعة حساب",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = LuxuryGold
                )
                Text(
                    text = "تسجيل سداد نقدي أو بنكي لتخفيض رصيد العميل",
                    fontSize = 12.sp,
                    color = TextMuted
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    color = DarkElevated,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF2E2E2E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextWhite)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "إجمالي الدين الحالي:", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = "${"%,.2f".format(customer.debt)} ريال",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = ErrorRed
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = {
                        amountText = it
                        isError = false
                    },
                    label = { Text("المبلغ المدفوع (ريال) *", color = LuxuryGoldLight) },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    isError = isError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedBorderColor = LuxuryGold,
                        unfocusedBorderColor = Color(0xFF333333)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settle_amount_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { amountText = customer.debt.toString() },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(
                        text = "سداد كامل المبلغ (${"%,.2f".format(customer.debt)} ريال)",
                        fontSize = 12.sp,
                        color = LuxuryGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF444444)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("إلغاء", color = TextWhite)
                    }

                    Button(
                        onClick = {
                            val parsed = amountText.toDoubleOrNull()
                            if (parsed == null || parsed <= 0) {
                                isError = true
                            } else {
                                onSettle(parsed)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LuxuryGold,
                            contentColor = TextDark
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_settlement_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("تأكيد السداد", fontWeight = FontWeight.Bold, color = TextDark)
                    }
                }
            }
        }
    }
}
