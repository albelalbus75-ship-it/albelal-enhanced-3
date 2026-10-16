package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.*
import org.json.JSONArray
import org.json.JSONObject

class LocalStorageManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("al_faisal_store_storage", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_CURRENT_USER_ID = "current_user_id"
        private const val KEY_USERS_JSON = "users_json"
        private const val KEY_PRODUCTS_JSON = "products_json"
        private const val KEY_ORDERS_JSON = "orders_json"
        private const val KEY_CUSTOMERS_JSON = "customers_json"
        private const val KEY_CATEGORIES_JSON = "categories_json"
        private const val KEY_FIREBASE_CONFIG = "firebase_config_json"
        private const val KEY_LAST_SYNC_TIME = "last_sync_timestamp"
        private const val KEY_BRANDING_THEME = "branding_theme_json"
        private const val KEY_ACCOUNTS_JSON = "accounts_json"
        private const val KEY_JOURNAL_ENTRIES_JSON = "journal_entries_json"
        private const val KEY_VOUCHERS_JSON = "vouchers_json"
        private const val KEY_PURCHASES_JSON = "purchases_json"
        private const val KEY_RETURNS_JSON = "returns_json"
        private const val KEY_STOCK_MOVEMENTS_JSON = "stock_movements_json"
        private const val KEY_CURRENCIES_JSON = "currencies_json"
    }

    // --- Users & Session ---
    fun loadUsers(defaultUsers: List<User>): List<User> {
        val raw = prefs.getString(KEY_USERS_JSON, null) ?: return defaultUsers
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<User>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val roleStr = obj.optString("role", "ADMIN")
                val role = try {
                    UserRole.valueOf(roleStr)
                } catch (e: Exception) {
                    if (roleStr == "CASHIER") UserRole.CASHIER else UserRole.ADMIN
                }

                val permsMap = mutableMapOf<String, List<String>>()
                val permsObj = obj.optJSONObject("customPermissions")
                if (permsObj != null) {
                    val keys = permsObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val actArr = permsObj.getJSONArray(key)
                        val acts = mutableListOf<String>()
                        for (a in 0 until actArr.length()) {
                            acts.add(actArr.getString(a))
                        }
                        permsMap[key] = acts
                    }
                }

                list.add(
                    User(
                        id = obj.getString("id"),
                        username = obj.getString("username"),
                        password = obj.getString("password"),
                        fullName = obj.getString("fullName"),
                        role = role,
                        isActive = obj.optBoolean("isActive", true),
                        phone = obj.optString("phone", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                        customPermissions = permsMap
                    )
                )
            }
            if (list.isEmpty()) defaultUsers else list
        } catch (e: Exception) {
            defaultUsers
        }
    }

    fun saveUsers(users: List<User>) {
        val array = JSONArray()
        users.forEach { u ->
            val obj = JSONObject().apply {
                put("id", u.id)
                put("username", u.username)
                put("password", u.password)
                put("fullName", u.fullName)
                put("role", u.role.name)
                put("isActive", u.isActive)
                put("phone", u.phone)
                put("createdAt", u.createdAt)

                if (u.customPermissions.isNotEmpty()) {
                    val permsObj = JSONObject()
                    u.customPermissions.forEach { (screen, actions) ->
                        val actArr = JSONArray()
                        actions.forEach { actArr.put(it) }
                        permsObj.put(screen, actArr)
                    }
                    put("customPermissions", permsObj)
                }
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_USERS_JSON, array.toString()).apply()
        markSyncTime()
    }

    fun loadCurrentUserId(): String? {
        return prefs.getString(KEY_CURRENT_USER_ID, "usr_admin")
    }

    fun saveCurrentUserId(userId: String?) {
        prefs.edit().putString(KEY_CURRENT_USER_ID, userId).apply()
    }

    // --- Branding & Identity Theme ---
    fun loadBrandingTheme(): AppBrandingTheme {
        val raw = prefs.getString(KEY_BRANDING_THEME, null) ?: return AppBrandingTheme.LUXURY_GOLD
        return try {
            val obj = JSONObject(raw)
            AppBrandingTheme(
                appName = obj.optString("appName", "متجر الفيصل للتجارة العامة"),
                logoPreset = obj.optString("logoPreset", "gold_crown"),
                customLogoBase64 = obj.optString("customLogoBase64", ""),
                themePresetId = obj.optString("themePresetId", "luxury_gold"),
                primaryColorHex = obj.optString("primaryColorHex", "#D4AF37"),
                secondaryColorHex = obj.optString("secondaryColorHex", "#C9A227"),
                backgroundColorHex = obj.optString("backgroundColorHex", "#0A0A0A"),
                cardBackgroundColorHex = obj.optString("cardBackgroundColorHex", "#161616"),
                textPrimaryColorHex = obj.optString("textPrimaryColorHex", "#FFFFFF"),
                textMutedColorHex = obj.optString("textMutedColorHex", "#A0A0A0"),
                surfaceColorHex = obj.optString("surfaceColorHex", "#241E0E"),
                borderColorHex = obj.optString("borderColorHex", "#332A15"),
                phoneContact = obj.optString("phoneContact", "0555555555"),
                whatsappContact = obj.optString("whatsappContact", "+966555555555"),
                emailContact = obj.optString("emailContact", "info@alfaisal-store.com"),
                websiteContact = obj.optString("websiteContact", "www.alfaisal-store.com"),
                designerName = obj.optString("designerName", "متجر الفيصل للتجارة العامة"),
                appVersion = obj.optString("appVersion", "v2.5 ERP Pro")
            )
        } catch (e: Exception) {
            AppBrandingTheme.LUXURY_GOLD
        }
    }

    fun saveBrandingTheme(theme: AppBrandingTheme) {
        val obj = JSONObject().apply {
            put("appName", theme.appName)
            put("logoPreset", theme.logoPreset)
            put("customLogoBase64", theme.customLogoBase64)
            put("themePresetId", theme.themePresetId)
            put("primaryColorHex", theme.primaryColorHex)
            put("secondaryColorHex", theme.secondaryColorHex)
            put("backgroundColorHex", theme.backgroundColorHex)
            put("cardBackgroundColorHex", theme.cardBackgroundColorHex)
            put("textPrimaryColorHex", theme.textPrimaryColorHex)
            put("textMutedColorHex", theme.textMutedColorHex)
            put("surfaceColorHex", theme.surfaceColorHex)
            put("borderColorHex", theme.borderColorHex)
            put("phoneContact", theme.phoneContact)
            put("whatsappContact", theme.whatsappContact)
            put("emailContact", theme.emailContact)
            put("websiteContact", theme.websiteContact)
            put("designerName", theme.designerName)
            put("appVersion", theme.appVersion)
        }
        prefs.edit().putString(KEY_BRANDING_THEME, obj.toString()).apply()
        markSyncTime()
    }

    // --- Products ---
    fun loadProducts(defaultProducts: List<Product>): List<Product> {
        val raw = prefs.getString(KEY_PRODUCTS_JSON, null) ?: return defaultProducts
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<Product>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val addImages = mutableListOf<String>()
                val imgArr = obj.optJSONArray("additionalImages")
                if (imgArr != null) {
                    for (j in 0 until imgArr.length()) {
                        addImages.add(imgArr.getString(j))
                    }
                }

                list.add(
                    Product(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        category = obj.getString("category"),
                        price = obj.getDouble("price"),
                        costPrice = obj.optDouble("costPrice", obj.getDouble("price") * 0.7),
                        wholesalePrice = obj.optDouble("wholesalePrice", obj.getDouble("price") * 0.85),
                        stock = obj.getInt("stock"),
                        iconName = obj.optString("iconName", "inventory_2"),
                        colorHex = obj.optString("colorHex", "#181818"),
                        note = obj.optString("note", ""),
                        barcode = obj.optString("barcode", ""),
                        model = obj.optString("model", ""),
                        unit = obj.optString("unit", "حبة"),
                        primaryImageUri = if (obj.has("primaryImageUri") && !obj.isNull("primaryImageUri")) obj.getString("primaryImageUri") else null,
                        additionalImages = addImages,
                        imageResId = if (obj.has("imageResId")) obj.getInt("imageResId") else null
                    )
                )
            }
            if (list.isEmpty()) defaultProducts else list
        } catch (e: Exception) {
            defaultProducts
        }
    }

    fun saveProducts(products: List<Product>) {
        val array = JSONArray()
        products.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("category", p.category)
                put("price", p.price)
                put("costPrice", p.costPrice)
                put("wholesalePrice", p.wholesalePrice)
                put("stock", p.stock)
                put("iconName", p.iconName)
                put("colorHex", p.colorHex)
                put("note", p.note)
                put("barcode", p.barcode)
                put("model", p.model)
                put("unit", p.unit)
                p.primaryImageUri?.let { put("primaryImageUri", it) }
                if (p.additionalImages.isNotEmpty()) {
                    val imgArr = JSONArray()
                    p.additionalImages.forEach { imgArr.put(it) }
                    put("additionalImages", imgArr)
                }
                p.imageResId?.let { put("imageResId", it) }
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PRODUCTS_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Orders / Sales Invoices ---
    fun loadOrders(defaultOrders: List<SaleOrder>): List<SaleOrder> {
        val raw = prefs.getString(KEY_ORDERS_JSON, null) ?: return defaultOrders
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<SaleOrder>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val itemsArr = obj.getJSONArray("items")
                val items = mutableListOf<CartItem>()
                for (k in 0 until itemsArr.length()) {
                    val itemObj = itemsArr.getJSONObject(k)
                    val prodObj = itemObj.getJSONObject("product")
                    val prod = Product(
                        id = prodObj.getString("id"),
                        name = prodObj.getString("name"),
                        category = prodObj.optString("category", "عام"),
                        price = prodObj.getDouble("price"),
                        stock = prodObj.optInt("stock", 0)
                    )
                    items.add(CartItem(prod, itemObj.getInt("quantity")))
                }

                val custObj = obj.getJSONObject("customer")
                val cust = Customer(
                    id = custObj.getString("id"),
                    name = custObj.getString("name"),
                    detail = custObj.optString("detail", ""),
                    debt = custObj.optDouble("debt", 0.0),
                    phone = custObj.optString("phone", "-")
                )

                list.add(
                    SaleOrder(
                        id = obj.getString("id"),
                        timestamp = obj.getLong("timestamp"),
                        items = items,
                        customer = cust,
                        subtotal = obj.getDouble("subtotal"),
                        vat = obj.getDouble("vat"),
                        discount = obj.optDouble("discount", 0.0),
                        total = obj.getDouble("total"),
                        paymentMethod = obj.getString("paymentMethod"),
                        journalEntryId = obj.optString("journalEntryId", ""),
                        invoiceNumber = obj.optString("invoiceNumber", obj.getString("id"))
                    )
                )
            }
            if (list.isEmpty()) defaultOrders else list
        } catch (e: Exception) {
            defaultOrders
        }
    }

    fun saveOrders(orders: List<SaleOrder>) {
        val array = JSONArray()
        orders.forEach { o ->
            val obj = JSONObject().apply {
                put("id", o.id)
                put("timestamp", o.timestamp)
                put("subtotal", o.subtotal)
                put("vat", o.vat)
                put("discount", o.discount)
                put("total", o.total)
                put("paymentMethod", o.paymentMethod)
                put("journalEntryId", o.journalEntryId)
                put("invoiceNumber", o.invoiceNumber)

                val custObj = JSONObject().apply {
                    put("id", o.customer.id)
                    put("name", o.customer.name)
                    put("detail", o.customer.detail)
                    put("debt", o.customer.debt)
                    put("phone", o.customer.phone)
                }
                put("customer", custObj)

                val itemsArr = JSONArray()
                o.items.forEach { itm ->
                    val itmObj = JSONObject().apply {
                        put("quantity", itm.quantity)
                        val pObj = JSONObject().apply {
                            put("id", itm.product.id)
                            put("name", itm.product.name)
                            put("category", itm.product.category)
                            put("price", itm.product.price)
                            put("stock", itm.product.stock)
                        }
                        put("product", pObj)
                    }
                    itemsArr.put(itmObj)
                }
                put("items", itemsArr)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_ORDERS_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Customers ---
    fun loadCustomers(defaultCustomers: List<Customer>): List<Customer> {
        val raw = prefs.getString(KEY_CUSTOMERS_JSON, null) ?: return defaultCustomers
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<Customer>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    Customer(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        detail = obj.optString("detail", ""),
                        debt = obj.optDouble("debt", 0.0),
                        phone = obj.optString("phone", "-")
                    )
                )
            }
            if (list.isEmpty()) defaultCustomers else list
        } catch (e: Exception) {
            defaultCustomers
        }
    }

    fun saveCustomers(customers: List<Customer>) {
        val array = JSONArray()
        customers.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("detail", c.detail)
                put("debt", c.debt)
                put("phone", c.phone)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CUSTOMERS_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Categories ---
    fun loadCategories(defaultCategories: List<String>): List<String> {
        val raw = prefs.getString(KEY_CATEGORIES_JSON, null) ?: return defaultCategories
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            if (list.isEmpty()) defaultCategories else list
        } catch (e: Exception) {
            defaultCategories
        }
    }

    fun saveCategories(categories: List<String>) {
        val array = JSONArray()
        categories.forEach { array.put(it) }
        prefs.edit().putString(KEY_CATEGORIES_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Chart of Accounts ---
    fun loadAccounts(defaultAccounts: List<Account>): List<Account> {
        val raw = prefs.getString(KEY_ACCOUNTS_JSON, null) ?: return defaultAccounts
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<Account>()
            for (i in 0 until array.length()) {
                try {
                    val obj = array.getJSONObject(i)
                    val type = try { AccountType.valueOf(obj.getString("type")) } catch (e: Exception) { AccountType.ASSET }
                    val nature = try { AccountNature.valueOf(obj.getString("nature")) } catch (e: Exception) { AccountNature.DEBIT }
                    list.add(
                        Account(
                            code = obj.getString("code"),
                            name = obj.getString("name"),
                            type = type,
                            nature = nature,
                            parentCode = if (obj.has("parentCode") && !obj.isNull("parentCode")) obj.getString("parentCode") else null,
                            isParent = obj.optBoolean("isParent", false),
                            balance = obj.optDouble("balance", 0.0),
                            description = obj.optString("description", ""),
                            isSystem = obj.optBoolean("isSystem", false)
                        )
                    )
                } catch (e: Exception) {
                    // Safe recovery from malformed account item
                }
            }
            if (list.isEmpty()) defaultAccounts else list
        } catch (e: Exception) {
            defaultAccounts
        }
    }

    fun saveAccounts(accounts: List<Account>) {
        val array = JSONArray()
        accounts.forEach { a ->
            val obj = JSONObject().apply {
                put("code", a.code)
                put("name", a.name)
                put("type", a.type.name)
                put("nature", a.nature.name)
                a.parentCode?.let { put("parentCode", it) }
                put("isParent", a.isParent)
                put("balance", a.balance)
                put("description", a.description)
                put("isSystem", a.isSystem)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_ACCOUNTS_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Journal Entries ---
    fun loadJournalEntries(defaultEntries: List<JournalEntry>): List<JournalEntry> {
        val raw = prefs.getString(KEY_JOURNAL_ENTRIES_JSON, null) ?: return defaultEntries
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<JournalEntry>()
            for (i in 0 until array.length()) {
                try {
                    val obj = array.getJSONObject(i)
                    val linesArr = obj.getJSONArray("lines")
                    val lines = mutableListOf<JournalEntryLine>()
                    for (j in 0 until linesArr.length()) {
                        val lObj = linesArr.getJSONObject(j)
                        lines.add(
                            JournalEntryLine(
                                accountCode = lObj.getString("accountCode"),
                                accountName = lObj.getString("accountName"),
                                debit = lObj.optDouble("debit", 0.0),
                                credit = lObj.optDouble("credit", 0.0),
                                note = lObj.optString("note", "")
                            )
                        )
                    }

                    val refType = try {
                        EntryReferenceType.valueOf(obj.optString("referenceType", EntryReferenceType.MANUAL.name))
                    } catch (e: Exception) {
                        EntryReferenceType.MANUAL
                    }

                    list.add(
                        JournalEntry(
                            id = obj.getString("id"),
                            entryNumber = obj.getString("entryNumber"),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            description = obj.getString("description"),
                            lines = lines,
                            referenceType = refType,
                            referenceId = obj.optString("referenceId", ""),
                            isPosted = obj.optBoolean("isPosted", true),
                            createdBy = obj.optString("createdBy", "admin")
                        )
                    )
                } catch (e: Exception) {
                    // Safe recovery from malformed journal entry
                }
            }
            if (list.isEmpty()) defaultEntries else list
        } catch (e: Exception) {
            defaultEntries
        }
    }

    fun saveJournalEntries(entries: List<JournalEntry>) {
        val array = JSONArray()
        entries.forEach { entry ->
            val obj = JSONObject().apply {
                put("id", entry.id)
                put("entryNumber", entry.entryNumber)
                put("date", entry.date)
                put("description", entry.description)
                put("referenceType", entry.referenceType.name)
                put("referenceId", entry.referenceId)
                put("isPosted", entry.isPosted)
                put("createdBy", entry.createdBy)

                val linesArr = JSONArray()
                entry.lines.forEach { line ->
                    val lObj = JSONObject().apply {
                        put("accountCode", line.accountCode)
                        put("accountName", line.accountName)
                        put("debit", line.debit)
                        put("credit", line.credit)
                        put("note", line.note)
                    }
                    linesArr.put(lObj)
                }
                put("lines", linesArr)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_JOURNAL_ENTRIES_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Vouchers (Receipts & Payments) ---
    fun loadVouchers(defaultVouchers: List<Voucher>): List<Voucher> {
        val raw = prefs.getString(KEY_VOUCHERS_JSON, null) ?: return defaultVouchers
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<Voucher>()
            for (i in 0 until array.length()) {
                try {
                    val obj = array.getJSONObject(i)
                    val vType = try {
                        VoucherType.valueOf(obj.getString("type"))
                    } catch (e: Exception) {
                        VoucherType.RECEIPT
                    }
                    val pMode = try {
                        PaymentMode.valueOf(obj.optString("paymentMode", PaymentMode.CASH.name))
                    } catch (e: Exception) {
                        PaymentMode.CASH
                    }
                    list.add(
                        Voucher(
                            id = obj.getString("id"),
                            voucherNumber = obj.getString("voucherNumber"),
                            type = vType,
                            date = obj.optLong("date", System.currentTimeMillis()),
                            partyAccountCode = obj.getString("partyAccountCode"),
                            partyName = obj.getString("partyName"),
                            amount = obj.getDouble("amount"),
                            paymentMode = pMode,
                            note = obj.optString("note", ""),
                            journalEntryId = obj.optString("journalEntryId", ""),
                            isPosted = obj.optBoolean("isPosted", true),
                            createdBy = obj.optString("createdBy", "admin")
                        )
                    )
                } catch (e: Exception) {
                    // Safe recovery from malformed voucher
                }
            }
            if (list.isEmpty()) defaultVouchers else list
        } catch (e: Exception) {
            defaultVouchers
        }
    }

    fun saveVouchers(vouchers: List<Voucher>) {
        val array = JSONArray()
        vouchers.forEach { v ->
            val obj = JSONObject().apply {
                put("id", v.id)
                put("voucherNumber", v.voucherNumber)
                put("type", v.type.name)
                put("date", v.date)
                put("partyAccountCode", v.partyAccountCode)
                put("partyName", v.partyName)
                put("amount", v.amount)
                put("paymentMode", v.paymentMode.name)
                put("note", v.note)
                put("journalEntryId", v.journalEntryId)
                put("isPosted", v.isPosted)
                put("createdBy", v.createdBy)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_VOUCHERS_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Purchases ---
    fun loadPurchases(): List<PurchaseInvoice> {
        val raw = prefs.getString(KEY_PURCHASES_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<PurchaseInvoice>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val itmArr = obj.getJSONArray("items")
                val items = mutableListOf<PurchaseItem>()
                for (j in 0 until itmArr.length()) {
                    val itm = itmArr.getJSONObject(j)
                    items.add(
                        PurchaseItem(
                            productId = itm.getString("productId"),
                            productName = itm.getString("productName"),
                            quantity = itm.getInt("quantity"),
                            unitCost = itm.getDouble("unitCost"),
                            total = itm.optDouble("total", itm.getInt("quantity") * itm.getDouble("unitCost"))
                        )
                    )
                }
                list.add(
                    PurchaseInvoice(
                        id = obj.getString("id"),
                        invoiceNumber = obj.getString("invoiceNumber"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        supplierId = obj.getString("supplierId"),
                        supplierName = obj.getString("supplierName"),
                        items = items,
                        totalAmount = obj.getDouble("totalAmount"),
                        paymentMode = PaymentMode.valueOf(obj.optString("paymentMode", PaymentMode.CASH.name)),
                        note = obj.optString("note", ""),
                        journalEntryId = obj.optString("journalEntryId", ""),
                        isPosted = obj.optBoolean("isPosted", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun savePurchases(purchases: List<PurchaseInvoice>) {
        val array = JSONArray()
        purchases.forEach { p ->
            val obj = JSONObject().apply {
                put("id", p.id)
                put("invoiceNumber", p.invoiceNumber)
                put("date", p.date)
                put("supplierId", p.supplierId)
                put("supplierName", p.supplierName)
                put("totalAmount", p.totalAmount)
                put("paymentMode", p.paymentMode.name)
                put("note", p.note)
                put("journalEntryId", p.journalEntryId)
                put("isPosted", p.isPosted)

                val itmArr = JSONArray()
                p.items.forEach { itm ->
                    val itmObj = JSONObject().apply {
                        put("productId", itm.productId)
                        put("productName", itm.productName)
                        put("quantity", itm.quantity)
                        put("unitCost", itm.unitCost)
                        put("total", itm.total)
                    }
                    itmArr.put(itmObj)
                }
                put("items", itmArr)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_PURCHASES_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Returns (Sale & Purchase Returns) ---
    fun loadReturns(): List<ReturnInvoice> {
        val raw = prefs.getString(KEY_RETURNS_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<ReturnInvoice>()
            for (i in 0 until array.length()) {
                try {
                    val obj = array.getJSONObject(i)
                    val itmArr = obj.getJSONArray("items")
                    val items = mutableListOf<ReturnItem>()
                    for (j in 0 until itmArr.length()) {
                        val itm = itmArr.getJSONObject(j)
                        items.add(
                            ReturnItem(
                                productId = itm.getString("productId"),
                                productName = itm.getString("productName"),
                                quantity = itm.getInt("quantity"),
                                unitPrice = itm.getDouble("unitPrice"),
                                total = itm.optDouble("total", itm.getInt("quantity") * itm.getDouble("unitPrice"))
                            )
                        )
                    }
                    val retType = try {
                        ReturnType.valueOf(obj.getString("type"))
                    } catch (e: Exception) {
                        ReturnType.SALE_RETURN
                    }
                    list.add(
                        ReturnInvoice(
                            id = obj.getString("id"),
                            returnNumber = obj.getString("returnNumber"),
                            originalInvoiceId = obj.optString("originalInvoiceId", ""),
                            date = obj.optLong("date", System.currentTimeMillis()),
                            type = retType,
                            partyId = obj.optString("partyId", ""),
                            partyName = obj.getString("partyName"),
                            items = items,
                            totalAmount = obj.getDouble("totalAmount"),
                            reason = obj.optString("reason", ""),
                            journalEntryId = obj.optString("journalEntryId", ""),
                            isPosted = obj.optBoolean("isPosted", true)
                        )
                    )
                } catch (e: Exception) {
                    // Safe recovery from malformed return invoice
                }
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveReturns(returns: List<ReturnInvoice>) {
        val array = JSONArray()
        returns.forEach { r ->
            val obj = JSONObject().apply {
                put("id", r.id)
                put("returnNumber", r.returnNumber)
                put("originalInvoiceId", r.originalInvoiceId)
                put("date", r.date)
                put("type", r.type.name)
                put("partyId", r.partyId)
                put("partyName", r.partyName)
                put("totalAmount", r.totalAmount)
                put("reason", r.reason)
                put("journalEntryId", r.journalEntryId)
                put("isPosted", r.isPosted)

                val itmArr = JSONArray()
                r.items.forEach { itm ->
                    val itmObj = JSONObject().apply {
                        put("productId", itm.productId)
                        put("productName", itm.productName)
                        put("quantity", itm.quantity)
                        put("unitPrice", itm.unitPrice)
                        put("total", itm.total)
                    }
                    itmArr.put(itmObj)
                }
                put("items", itmArr)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_RETURNS_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Stock Movements ---
    fun loadStockMovements(): List<StockMovement> {
        val raw = prefs.getString(KEY_STOCK_MOVEMENTS_JSON, null) ?: return emptyList()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<StockMovement>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    StockMovement(
                        id = obj.getString("id"),
                        date = obj.optLong("date", System.currentTimeMillis()),
                        productId = obj.getString("productId"),
                        productName = obj.getString("productName"),
                        changeQty = obj.getInt("changeQty"),
                        remainingQty = obj.getInt("remainingQty"),
                        reason = obj.optString("reason", ""),
                        referenceType = obj.optString("referenceType", "SALE"),
                        referenceId = obj.optString("referenceId", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveStockMovements(movements: List<StockMovement>) {
        val array = JSONArray()
        movements.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("date", m.date)
                put("productId", m.productId)
                put("productName", m.productName)
                put("changeQty", m.changeQty)
                put("remainingQty", m.remainingQty)
                put("reason", m.reason)
                put("referenceType", m.referenceType)
                put("referenceId", m.referenceId)
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_STOCK_MOVEMENTS_JSON, array.toString()).apply()
        markSyncTime()
    }

    // --- Firebase Config ---
    fun loadFirebaseConfig(): FirebaseConfig {
        val raw = prefs.getString(KEY_FIREBASE_CONFIG, null) ?: return FirebaseConfig()
        return try {
            val obj = JSONObject(raw)
            FirebaseConfig(
                projectId = obj.optString("projectId", "al-faisal-store"),
                apiKey = obj.optString("apiKey", ""),
                appId = obj.optString("appId", ""),
                isEnabled = obj.optBoolean("isEnabled", false),
                autoSyncIntervalMinutes = obj.optInt("autoSyncIntervalMinutes", 5),
                collectionPrefix = obj.optString("collectionPrefix", "store_v1")
            )
        } catch (e: Exception) {
            FirebaseConfig()
        }
    }

    fun saveFirebaseConfig(config: FirebaseConfig) {
        val obj = JSONObject().apply {
            put("projectId", config.projectId)
            put("apiKey", config.apiKey)
            put("appId", config.appId)
            put("isEnabled", config.isEnabled)
            put("autoSyncIntervalMinutes", config.autoSyncIntervalMinutes)
            put("collectionPrefix", config.collectionPrefix)
        }
        prefs.edit().putString(KEY_FIREBASE_CONFIG, obj.toString()).apply()
        markSyncTime()
    }

    // --- Backup & Restore JSON ---
    fun exportBackupJson(): String {
        val root = JSONObject()
        root.put("version", "2.5 ERP Pro")
        root.put("storeName", "متجر الفيصل للتجارة العامة")
        root.put("exportedAt", System.currentTimeMillis())

        val usersRaw = prefs.getString(KEY_USERS_JSON, "[]")
        val productsRaw = prefs.getString(KEY_PRODUCTS_JSON, "[]")
        val ordersRaw = prefs.getString(KEY_ORDERS_JSON, "[]")
        val customersRaw = prefs.getString(KEY_CUSTOMERS_JSON, "[]")
        val categoriesRaw = prefs.getString(KEY_CATEGORIES_JSON, "[]")
        val firebaseConfigRaw = prefs.getString(KEY_FIREBASE_CONFIG, "{}")
        val brandingRaw = prefs.getString(KEY_BRANDING_THEME, "{}")
        val accountsRaw = prefs.getString(KEY_ACCOUNTS_JSON, "[]")
        val journalRaw = prefs.getString(KEY_JOURNAL_ENTRIES_JSON, "[]")
        val vouchersRaw = prefs.getString(KEY_VOUCHERS_JSON, "[]")
        val purchasesRaw = prefs.getString(KEY_PURCHASES_JSON, "[]")
        val returnsRaw = prefs.getString(KEY_RETURNS_JSON, "[]")
        val movementsRaw = prefs.getString(KEY_STOCK_MOVEMENTS_JSON, "[]")

        root.put("users", JSONArray(usersRaw ?: "[]"))
        root.put("products", JSONArray(productsRaw ?: "[]"))
        root.put("orders", JSONArray(ordersRaw ?: "[]"))
        root.put("customers", JSONArray(customersRaw ?: "[]"))
        root.put("categories", JSONArray(categoriesRaw ?: "[]"))
        root.put("firebaseConfig", JSONObject(firebaseConfigRaw ?: "{}"))
        root.put("brandingTheme", JSONObject(brandingRaw ?: "{}"))
        root.put("accounts", JSONArray(accountsRaw ?: "[]"))
        root.put("journalEntries", JSONArray(journalRaw ?: "[]"))
        root.put("vouchers", JSONArray(vouchersRaw ?: "[]"))
        root.put("purchases", JSONArray(purchasesRaw ?: "[]"))
        root.put("returns", JSONArray(returnsRaw ?: "[]"))
        root.put("stockMovements", JSONArray(movementsRaw ?: "[]"))
        val currenciesRaw = prefs.getString(KEY_CURRENCIES_JSON, "[]")
        root.put("currencies", JSONArray(currenciesRaw ?: "[]"))

        return root.toString(2)
    }

    // --- Currencies & Exchange Rates ---
    fun loadCurrencies(defaultCurrencies: List<CurrencyRate>): List<CurrencyRate> {
        val raw = prefs.getString(KEY_CURRENCIES_JSON, null) ?: return defaultCurrencies
        return try {
            val array = JSONArray(raw)
            if (array.length() == 0) return defaultCurrencies
            val list = mutableListOf<CurrencyRate>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    CurrencyRate(
                        code = obj.getString("code"),
                        nameAr = obj.getString("nameAr"),
                        symbol = obj.getString("symbol"),
                        exchangeRate = obj.getDouble("exchangeRate"),
                        isDefault = obj.optBoolean("isDefault", false)
                    )
                )
            }
            if (list.isEmpty()) defaultCurrencies else list
        } catch (e: Exception) {
            defaultCurrencies
        }
    }

    fun saveCurrencies(currencies: List<CurrencyRate>) {
        val array = JSONArray()
        for (c in currencies) {
            val obj = JSONObject()
            obj.put("code", c.code)
            obj.put("nameAr", c.nameAr)
            obj.put("symbol", c.symbol)
            obj.put("exchangeRate", c.exchangeRate)
            obj.put("isDefault", c.isDefault)
            array.put(obj)
        }
        prefs.edit().putString(KEY_CURRENCIES_JSON, array.toString()).apply()
        markSyncTime()
    }

    fun importBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)
            val editor = prefs.edit()
            if (root.has("users")) {
                editor.putString(KEY_USERS_JSON, root.getJSONArray("users").toString())
            }
            if (root.has("products")) {
                editor.putString(KEY_PRODUCTS_JSON, root.getJSONArray("products").toString())
            }
            if (root.has("orders")) {
                editor.putString(KEY_ORDERS_JSON, root.getJSONArray("orders").toString())
            }
            if (root.has("customers")) {
                editor.putString(KEY_CUSTOMERS_JSON, root.getJSONArray("customers").toString())
            }
            if (root.has("categories")) {
                editor.putString(KEY_CATEGORIES_JSON, root.getJSONArray("categories").toString())
            }
            if (root.has("firebaseConfig")) {
                editor.putString(KEY_FIREBASE_CONFIG, root.getJSONObject("firebaseConfig").toString())
            }
            if (root.has("brandingTheme")) {
                editor.putString(KEY_BRANDING_THEME, root.getJSONObject("brandingTheme").toString())
            }
            if (root.has("accounts")) {
                editor.putString(KEY_ACCOUNTS_JSON, root.getJSONArray("accounts").toString())
            }
            if (root.has("journalEntries")) {
                editor.putString(KEY_JOURNAL_ENTRIES_JSON, root.getJSONArray("journalEntries").toString())
            }
            if (root.has("vouchers")) {
                editor.putString(KEY_VOUCHERS_JSON, root.getJSONArray("vouchers").toString())
            }
            if (root.has("purchases")) {
                editor.putString(KEY_PURCHASES_JSON, root.getJSONArray("purchases").toString())
            }
            if (root.has("returns")) {
                editor.putString(KEY_RETURNS_JSON, root.getJSONArray("returns").toString())
            }
            if (root.has("stockMovements")) {
                editor.putString(KEY_STOCK_MOVEMENTS_JSON, root.getJSONArray("stockMovements").toString())
            }
            if (root.has("currencies")) {
                editor.putString(KEY_CURRENCIES_JSON, root.getJSONArray("currencies").toString())
            }
            editor.apply()
            markSyncTime()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun markSyncTime() {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis()).apply()
    }

    fun getLastSyncTime(): Long {
        return prefs.getLong(KEY_LAST_SYNC_TIME, System.currentTimeMillis())
    }
}
