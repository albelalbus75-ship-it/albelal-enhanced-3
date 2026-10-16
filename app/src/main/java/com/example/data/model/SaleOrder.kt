package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SaleOrder(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val items: List<CartItem>,
    val customer: Customer,
    val subtotal: Double,
    val vat: Double,
    val discount: Double = 0.0,
    val total: Double,
    val paymentMethod: String, // "نقدًا", "شبكة", "آجل"
    val journalEntryId: String = "",
    val invoiceNumber: String = id
) {
    val formattedDate: String
        get() = SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(Date(timestamp))

    val totalItemsCount: Int
        get() = items.sumOf { it.quantity }
}
