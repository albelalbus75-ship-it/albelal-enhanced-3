package com.example.data.model

enum class AccountType(val code: String, val titleAr: String, val defaultNature: AccountNature) {
    ASSET("1", "الأصول", AccountNature.DEBIT),
    LIABILITY("2", "الخصوم (الالتزامات)", AccountNature.CREDIT),
    EQUITY("3", "حقوق الملكية", AccountNature.CREDIT),
    REVENUE("4", "الإيرادات", AccountNature.CREDIT),
    EXPENSE("5", "المصروفات", AccountNature.DEBIT)
}

enum class AccountNature(val titleAr: String) {
    DEBIT("مدين"),
    CREDIT("دائن")
}

data class Account(
    val code: String, // e.g. "101", "10101"
    val name: String,
    val type: AccountType,
    val nature: AccountNature,
    val parentCode: String? = null,
    val isParent: Boolean = false,
    val balance: Double = 0.0,
    val description: String = "",
    val isSystem: Boolean = false
)

data class JournalEntryLine(
    val accountCode: String,
    val accountName: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val note: String = ""
)

enum class EntryReferenceType(val titleAr: String) {
    SALE("فاتورة مبيعات"),
    PURCHASE("فاتورة مشتريات"),
    SALE_RETURN("مرتجع مبيعات"),
    PURCHASE_RETURN("مرتجع مشتريات"),
    RECEIPT_VOUCHER("سند قبض"),
    PAYMENT_VOUCHER("سند صرف"),
    MANUAL("قيد يدوي")
}

data class JournalEntry(
    val id: String,
    val entryNumber: String,
    val date: Long = System.currentTimeMillis(),
    val description: String,
    val lines: List<JournalEntryLine>,
    val referenceType: EntryReferenceType,
    val referenceId: String = "",
    val isPosted: Boolean = true,
    val createdBy: String = "admin"
) {
    val totalDebit: Double get() = lines.sumOf { it.debit }
    val totalCredit: Double get() = lines.sumOf { it.credit }
    val isBalanced: Boolean get() = Math.abs(totalDebit - totalCredit) < 0.001
}

enum class VoucherType(val titleAr: String, val defaultAccountCode: String) {
    RECEIPT("سند قبض", "101"), // Debit Cash, Credit Party
    PAYMENT("سند صرف", "101")   // Debit Party, Credit Cash
}

enum class PaymentMode(val titleAr: String) {
    CASH("نقداً"),
    CREDIT("آجل"),
    BANK_TRANSFER("تحويل بنكي"),
    CHEQUE("شيك")
}

data class Voucher(
    val id: String,
    val voucherNumber: String,
    val type: VoucherType,
    val date: Long = System.currentTimeMillis(),
    val partyAccountCode: String,
    val partyName: String,
    val amount: Double,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val note: String = "",
    val journalEntryId: String = "",
    val isPosted: Boolean = true,
    val createdBy: String = "admin"
)

data class PurchaseItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitCost: Double,
    val total: Double = quantity * unitCost
)

data class PurchaseInvoice(
    val id: String,
    val invoiceNumber: String,
    val date: Long = System.currentTimeMillis(),
    val supplierId: String,
    val supplierName: String,
    val items: List<PurchaseItem>,
    val totalAmount: Double,
    val paymentMode: PaymentMode = PaymentMode.CASH,
    val note: String = "",
    val journalEntryId: String = "",
    val isPosted: Boolean = true
)

enum class ReturnType(val titleAr: String) {
    SALE_RETURN("مرتجع مبيعات"),
    PURCHASE_RETURN("مرتجع مشتريات")
}

data class ReturnItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val total: Double = quantity * unitPrice
)

data class ReturnInvoice(
    val id: String,
    val returnNumber: String,
    val originalInvoiceId: String = "",
    val date: Long = System.currentTimeMillis(),
    val type: ReturnType,
    val partyId: String = "",
    val partyName: String,
    val items: List<ReturnItem>,
    val totalAmount: Double,
    val reason: String = "",
    val journalEntryId: String = "",
    val isPosted: Boolean = true
)

data class StockMovement(
    val id: String,
    val date: Long = System.currentTimeMillis(),
    val productId: String,
    val productName: String,
    val changeQty: Int, // positive for in, negative for out
    val remainingQty: Int,
    val reason: String,
    val referenceType: String,
    val referenceId: String
)

data class CurrencyRate(
    val code: String,          // e.g. "SAR", "YER", "USD"
    val nameAr: String,        // "ريال سعودي", "ريال يمني", "دولار أمريكي"
    val symbol: String,        // "SAR", "YER", "$"
    val exchangeRate: Double,  // Relative to base (SAR = 1.0, YER = 70.0, USD = 0.2667)
    val isDefault: Boolean = false
)

