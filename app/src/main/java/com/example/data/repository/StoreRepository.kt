package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.LocalStorageManager
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StoreRepository(context: Context? = null) {

    private val storage: LocalStorageManager? = context?.let { LocalStorageManager(it) }
    private val scope = CoroutineScope(Dispatchers.IO)

    // Predefined default accounts for standard ERP
    private val defaultAccounts = listOf(
        // Main categories
        Account("1", "الأصول", AccountType.ASSET, AccountNature.DEBIT, isParent = true, isSystem = true),
        Account("101", "الصندوق الرئيسي (النقدية)", AccountType.ASSET, AccountNature.DEBIT, parentCode = "1", balance = 15450.0, isSystem = true),
        Account("102", "البنك (الحساب الجاري)", AccountType.ASSET, AccountNature.DEBIT, parentCode = "1", balance = 48200.0, isSystem = true),
        Account("103", "العملاء (الذمم المدينة)", AccountType.ASSET, AccountNature.DEBIT, parentCode = "1", balance = 3850.0, isSystem = true),
        Account("104", "مخزون البضائع والأصناف", AccountType.ASSET, AccountNature.DEBIT, parentCode = "1", balance = 24600.0, isSystem = true),

        Account("2", "الخصوم (الالتزامات)", AccountType.LIABILITY, AccountNature.CREDIT, isParent = true, isSystem = true),
        Account("201", "الموردين (الذمم الدائنة)", AccountType.LIABILITY, AccountNature.CREDIT, parentCode = "2", balance = 8200.0, isSystem = true),

        Account("3", "حقوق الملكية", AccountType.EQUITY, AccountNature.CREDIT, isParent = true, isSystem = true),
        Account("301", "رأس المال", AccountType.EQUITY, AccountNature.CREDIT, parentCode = "3", balance = 75000.0, isSystem = true),

        Account("4", "الإيرادات", AccountType.REVENUE, AccountNature.CREDIT, isParent = true, isSystem = true),
        Account("401", "إيرادات المبيعات", AccountType.REVENUE, AccountNature.CREDIT, parentCode = "4", balance = 12480.0, isSystem = true),
        Account("402", "مردودات ومسموحات المبيعات", AccountType.REVENUE, AccountNature.DEBIT, parentCode = "4", balance = 350.0, isSystem = true),

        Account("5", "المصروفات", AccountType.EXPENSE, AccountNature.DEBIT, isParent = true, isSystem = true),
        Account("501", "تكلفة المشتريات", AccountType.EXPENSE, AccountNature.DEBIT, parentCode = "5", balance = 7800.0, isSystem = true),
        Account("502", "مردودات ومسموحات المشتريات", AccountType.EXPENSE, AccountNature.CREDIT, parentCode = "5", balance = 120.0, isSystem = true),
        Account("503", "المصروفات العمومية والإدارية", AccountType.EXPENSE, AccountNature.DEBIT, parentCode = "5", balance = 950.0, isSystem = true),
        Account("504", "مصروف الإيجار", AccountType.EXPENSE, AccountNature.DEBIT, parentCode = "5", balance = 3000.0, isSystem = true),
        Account("505", "مصروف الرواتب والأجور", AccountType.EXPENSE, AccountNature.DEBIT, parentCode = "5", balance = 4500.0, isSystem = true)
    )

    // Predefined default ERP roles and users
    private val defaultUsers = listOf(
        User(
            id = "usr_admin",
            username = "admin",
            password = "1234",
            fullName = "المدير العام (فيصل)",
            role = UserRole.ADMIN,
            isActive = true,
            phone = "0500000001"
        ),
        User(
            id = "usr_accountant",
            username = "accountant",
            password = "1234",
            fullName = "المحاسب المالي",
            role = UserRole.ACCOUNTANT,
            isActive = true,
            phone = "0500000002"
        ),
        User(
            id = "usr_cashier",
            username = "cashier",
            password = "1234",
            fullName = "كاشير المبيعات",
            role = UserRole.CASHIER,
            isActive = true,
            phone = "0500000003"
        ),
        User(
            id = "usr_storekeeper",
            username = "storekeeper",
            password = "1234",
            fullName = "أمين المستودع والمخزن",
            role = UserRole.STOREKEEPER,
            isActive = true,
            phone = "0500000004"
        ),
        User(
            id = "usr_viewer",
            username = "viewer",
            password = "1234",
            fullName = "مشاهد تقارير الإدارة",
            role = UserRole.REPORTS_VIEWER,
            isActive = true,
            phone = "0500000005"
        )
    )

    private val initialProducts = listOf(
        Product(
            id = "p1",
            name = "طقم دلة القهوة العربية والضيافة الملكية",
            category = "أدوات وتراثيات",
            price = 380.0,
            costPrice = 270.0,
            wholesalePrice = 330.0,
            stock = 18,
            iconName = "coffee",
            colorHex = "#1F1A10",
            note = "دلة فاخرة مع فناجيل منقوشة وصحن تقديم",
            barcode = "62810055001",
            model = "DAL-ROYAL",
            unit = "طقم",
            imageResId = R.drawable.prod_dallah_set_1792029190965
        ),
        Product(
            id = "p2",
            name = "طقم قدور جرانيت تركي فاخر 9 قطع",
            category = "أدوات وتراثيات",
            price = 295.0,
            costPrice = 210.0,
            wholesalePrice = 260.0,
            stock = 14,
            iconName = "restaurant",
            colorHex = "#181818",
            note = "مقابض مذهبة غير لاصقة • صناعة تركية ممتازة",
            barcode = "62810055002",
            model = "GR-GOLD-9",
            unit = "طقم",
            imageResId = R.drawable.prod_cookware_1792029207646
        ),
        Product(
            id = "p3",
            name = "بوكس هدية ملكي فاخر (ساعة وعود وقلم)",
            category = "هدايا راقية",
            price = 340.0,
            costPrice = 240.0,
            wholesalePrice = 295.0,
            stock = 12,
            iconName = "card_giftcard",
            colorHex = "#1C1914",
            note = "ساعة ذهبية + عطر عود ملكي + قلم فاخر ومحفظة",
            barcode = "62810055003",
            model = "BOX-ROYAL",
            unit = "طقم",
            imageResId = R.drawable.prod_royal_gift_1792029224376
        ),
        Product(
            id = "p4",
            name = "دلة السيف النحاسية نقش يدوي أصيل",
            category = "أدوات وتراثيات",
            price = 220.0,
            costPrice = 160.0,
            wholesalePrice = 190.0,
            stock = 25,
            iconName = "local_cafe",
            colorHex = "#211C12",
            note = "نحاس نقي مقاوم للحرارة ومحفور بالليزر",
            barcode = "62810055004",
            model = "DAL-SAIF-BRASS",
            unit = "حبة",
            imageResId = R.drawable.prod_dallah_set_1792029190965
        ),
        Product(
            id = "p5",
            name = "مبخرة كريستال فاخرة بقاعدة مذهبة",
            category = "هدايا راقية",
            price = 145.0,
            costPrice = 90.0,
            wholesalePrice = 120.0,
            stock = 30,
            iconName = "spa",
            colorHex = "#161513",
            note = "تصميم إسلامي كلاسيكي للمناسبات الرسمية",
            barcode = "62810055005",
            model = "INC-CRYSTAL",
            unit = "قطعة",
            imageResId = R.drawable.prod_royal_gift_1792029224376
        ),
        Product(
            id = "p6",
            name = "ترمس شاي وقهوة ألماني فاخر سعة 1 لتر",
            category = "أدوات وتراثيات",
            price = 185.0,
            costPrice = 130.0,
            wholesalePrice = 160.0,
            stock = 22,
            iconName = "emoji_food_beverage",
            colorHex = "#1A1916",
            note = "حفظ حرارة حتى 24 ساعة بلون لؤلؤي ذهبي",
            barcode = "62810055006",
            model = "THERM-DE-1L",
            unit = "حبة",
            imageResId = R.drawable.prod_cookware_1792029207646
        )
    )

    private val initialCategories = listOf("الكل", "أدوات وتراثيات", "هدايا راقية", "ضيافة ملكية")

    private val initialCustomers = listOf(
        Customer("cash", "عميل نقدي سريع", "سداد فوري عند الصندوق", 0.0, "-"),
        Customer("c1", "الشيخ خالد العتيبي", "عميل VIP - حد ائتمان 5,000 ريال", 750.0, "0501112233"),
        Customer("c2", "مؤسسة الرؤية للتجارة", "مبيعات جملة وآجل", 2100.0, "0548889900"),
        Customer("c3", "سلطان الشمري", "حساب آجل شخصي", 420.0, "0554433221")
    )

    // In-memory Flow State
    private val _users = MutableStateFlow<List<User>>(
        try {
            val loaded = storage?.loadUsers(defaultUsers) ?: defaultUsers
            val hasAdmin = loaded.any { it.username.equals("admin", ignoreCase = true) }
            if (!hasAdmin) loaded + defaultUsers.first() else loaded
        } catch (e: Throwable) {
            defaultUsers
        }
    )
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Startup begins at Login screen: currentUser is null until successful authentication
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _brandingTheme = MutableStateFlow<AppBrandingTheme>(
        try { storage?.loadBrandingTheme() ?: AppBrandingTheme.LUXURY_GOLD } catch (e: Throwable) { AppBrandingTheme.LUXURY_GOLD }
    )
    val brandingTheme: StateFlow<AppBrandingTheme> = _brandingTheme.asStateFlow()

    private val _products = MutableStateFlow<List<Product>>(
        try { storage?.loadProducts(initialProducts) ?: initialProducts } catch (e: Throwable) { initialProducts }
    )
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(
        try { storage?.loadCategories(initialCategories) ?: initialCategories } catch (e: Throwable) { initialCategories }
    )
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _customers = MutableStateFlow<List<Customer>>(
        try { storage?.loadCustomers(initialCustomers) ?: initialCustomers } catch (e: Throwable) { initialCustomers }
    )
    val customers: StateFlow<List<Customer>> = _customers.asStateFlow()

    private val _orders = MutableStateFlow<List<SaleOrder>>(
        try { storage?.loadOrders(createInitialOrders()) ?: createInitialOrders() } catch (e: Throwable) { createInitialOrders() }
    )
    val orders: StateFlow<List<SaleOrder>> = _orders.asStateFlow()

    // Accounting State
    private val _accounts = MutableStateFlow<List<Account>>(
        try { storage?.loadAccounts(defaultAccounts) ?: defaultAccounts } catch (e: Throwable) { defaultAccounts }
    )
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(
        try { storage?.loadJournalEntries(createInitialJournalEntries()) ?: createInitialJournalEntries() } catch (e: Throwable) { createInitialJournalEntries() }
    )
    val journalEntries: StateFlow<List<JournalEntry>> = _journalEntries.asStateFlow()

    private val _vouchers = MutableStateFlow<List<Voucher>>(
        try { storage?.loadVouchers(createInitialVouchers()) ?: createInitialVouchers() } catch (e: Throwable) { createInitialVouchers() }
    )
    val vouchers: StateFlow<List<Voucher>> = _vouchers.asStateFlow()

    private val _purchases = MutableStateFlow<List<PurchaseInvoice>>(
        try { storage?.loadPurchases() ?: emptyList() } catch (e: Throwable) { emptyList() }
    )
    val purchases: StateFlow<List<PurchaseInvoice>> = _purchases.asStateFlow()

    private val _returns = MutableStateFlow<List<ReturnInvoice>>(
        try { storage?.loadReturns() ?: emptyList() } catch (e: Throwable) { emptyList() }
    )
    val returns: StateFlow<List<ReturnInvoice>> = _returns.asStateFlow()

    private val _stockMovements = MutableStateFlow<List<StockMovement>>(
        try { storage?.loadStockMovements() ?: emptyList() } catch (e: Throwable) { emptyList() }
    )
    val stockMovements: StateFlow<List<StockMovement>> = _stockMovements.asStateFlow()

    private val _firebaseConfig = MutableStateFlow<FirebaseConfig>(
        try { storage?.loadFirebaseConfig() ?: FirebaseConfig() } catch (e: Throwable) { FirebaseConfig() }
    )
    val firebaseConfig: StateFlow<FirebaseConfig> = _firebaseConfig.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.SYNCED)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _lastSyncTime = MutableStateFlow<Long>(
        storage?.getLastSyncTime() ?: System.currentTimeMillis()
    )
    val lastSyncTime: StateFlow<Long> = _lastSyncTime.asStateFlow()

    // Currencies State
    private val defaultCurrencies = listOf(
        CurrencyRate(code = "SAR", nameAr = "ريال سعودي", symbol = "SAR", exchangeRate = 1.0, isDefault = true),
        CurrencyRate(code = "YER", nameAr = "ريال يمني", symbol = "YER", exchangeRate = 70.0, isDefault = false),
        CurrencyRate(code = "USD", nameAr = "دولار أمريكي", symbol = "$", exchangeRate = 0.2667, isDefault = false)
    )

    private val _currencies = MutableStateFlow<List<CurrencyRate>>(
        try { storage?.loadCurrencies(defaultCurrencies) ?: defaultCurrencies } catch (e: Throwable) { defaultCurrencies }
    )
    val currencies: StateFlow<List<CurrencyRate>> = _currencies.asStateFlow()

    fun updateCurrencyExchangeRate(code: String, newRate: Double) {
        if (newRate <= 0.0) return
        _currencies.update { list ->
            list.map { if (it.code.equals(code, ignoreCase = true)) it.copy(exchangeRate = newRate) else it }
        }
        storage?.saveCurrencies(_currencies.value)
        triggerAutoSync()
    }

    private fun createInitialOrders(): List<SaleOrder> {
        val now = System.currentTimeMillis()
        val p1 = initialProducts[0]
        val p2 = initialProducts[1]
        val p3 = initialProducts[2]

        return listOf(
            SaleOrder(
                id = "INV-1001",
                timestamp = now - 20 * 60 * 1000,
                items = listOf(CartItem(p1, 1), CartItem(p3, 1)),
                customer = initialCustomers[0],
                subtotal = 720.0,
                vat = 108.0,
                discount = 0.0,
                total = 828.0,
                paymentMethod = "نقدًا",
                journalEntryId = "JE-1001",
                invoiceNumber = "INV-1001"
            ),
            SaleOrder(
                id = "INV-1002",
                timestamp = now - 60 * 60 * 1000,
                items = listOf(CartItem(p2, 1)),
                customer = initialCustomers[1],
                subtotal = 295.0,
                vat = 44.25,
                discount = 0.0,
                total = 339.25,
                paymentMethod = "آجل",
                journalEntryId = "JE-1002",
                invoiceNumber = "INV-1002"
            )
        )
    }

    private fun createInitialJournalEntries(): List<JournalEntry> {
        val now = System.currentTimeMillis()
        return listOf(
            JournalEntry(
                id = "JE-1001",
                entryNumber = "JV-1001",
                date = now - 20 * 60 * 1000,
                description = "فاتورة مبيعات رقم INV-1001 - عميل نقدي",
                lines = listOf(
                    JournalEntryLine("101", "الصندوق الرئيسي (النقدية)", debit = 828.0, credit = 0.0),
                    JournalEntryLine("401", "إيرادات المبيعات", debit = 0.0, credit = 828.0)
                ),
                referenceType = EntryReferenceType.SALE,
                referenceId = "INV-1001"
            ),
            JournalEntry(
                id = "JE-1002",
                entryNumber = "JV-1002",
                date = now - 60 * 60 * 1000,
                description = "فاتورة مبيعات آجل رقم INV-1002 - الشيخ خالد العتيبي",
                lines = listOf(
                    JournalEntryLine("103", "العملاء (الذمم المدينة)", debit = 339.25, credit = 0.0),
                    JournalEntryLine("401", "إيرادات المبيعات", debit = 0.0, credit = 339.25)
                ),
                referenceType = EntryReferenceType.SALE,
                referenceId = "INV-1002"
            )
        )
    }

    private fun createInitialVouchers(): List<Voucher> {
        val now = System.currentTimeMillis()
        return listOf(
            Voucher(
                id = "VCH-REC-101",
                voucherNumber = "RV-101",
                type = VoucherType.RECEIPT,
                date = now - 2 * 3600 * 1000,
                partyAccountCode = "103",
                partyName = "سلطان الشمري",
                amount = 500.0,
                paymentMode = PaymentMode.CASH,
                note = "دفعة من الحساب الآجل",
                journalEntryId = "JE-VCH-101"
            )
        )
    }

    private fun triggerAutoSync() {
        scope.launch {
            _syncStatus.value = SyncStatus.SYNCING
            storage?.saveProducts(_products.value)
            storage?.saveOrders(_orders.value)
            storage?.saveCustomers(_customers.value)
            storage?.saveCategories(_categories.value)
            storage?.saveUsers(_users.value)
            storage?.saveBrandingTheme(_brandingTheme.value)
            storage?.saveAccounts(_accounts.value)
            storage?.saveJournalEntries(_journalEntries.value)
            storage?.saveVouchers(_vouchers.value)
            storage?.savePurchases(_purchases.value)
            storage?.saveReturns(_returns.value)
            storage?.saveStockMovements(_stockMovements.value)
            delay(400)
            _lastSyncTime.value = System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
        }
    }

    // --- Authentication & User Management ---
    fun login(username: String, password: String): Boolean {
        val trimmedUser = username.trim()
        val trimmedPass = password.trim()

        val foundUser = _users.value.find {
            it.username.equals(trimmedUser, ignoreCase = true) && it.password == trimmedPass
        }

        val user = foundUser ?: if (trimmedUser.equals("admin", ignoreCase = true) && trimmedPass == "1234") {
            // Safe fallback to default admin login (admin / 1234)
            val adminUser = defaultUsers.first()
            if (_users.value.none { it.id == adminUser.id }) {
                _users.update { it + adminUser }
                storage?.saveUsers(_users.value)
            }
            adminUser
        } else {
            null
        }

        return if (user != null && user.isActive) {
            _currentUser.value = user
            storage?.saveCurrentUserId(user.id)
            true
        } else {
            false
        }
    }

    fun logout() {
        _currentUser.value = null
        storage?.saveCurrentUserId(null)
    }

    fun switchUser(userId: String) {
        val target = _users.value.find { it.id == userId }
        if (target != null && target.isActive) {
            _currentUser.value = target
            storage?.saveCurrentUserId(target.id)
        }
    }

    fun addUser(user: User) {
        _users.update { it + user }
        triggerAutoSync()
    }

    fun updateUser(user: User) {
        _users.update { list ->
            list.map { if (it.id == user.id) user else it }
        }
        if (_currentUser.value?.id == user.id) {
            _currentUser.value = user
        }
        triggerAutoSync()
    }

    fun toggleUserActive(userId: String) {
        if (userId == "usr_admin") return
        _users.update { list ->
            list.map {
                if (it.id == userId) it.copy(isActive = !it.isActive) else it
            }
        }
        triggerAutoSync()
    }

    fun deleteUser(userId: String) {
        if (userId == "usr_admin") return
        _users.update { list -> list.filterNot { it.id == userId } }
        if (_currentUser.value?.id == userId) {
            _currentUser.value = _users.value.firstOrNull { it.role == UserRole.ADMIN }
        }
        triggerAutoSync()
    }

    fun updateAdminPassword(newPass: String): Boolean {
        val admin = _users.value.find { it.username == "admin" } ?: return false
        val updated = admin.copy(password = newPass.trim())
        updateUser(updated)
        return true
    }

    // --- Branding & Theme ---
    fun updateBrandingTheme(newTheme: AppBrandingTheme) {
        _brandingTheme.value = newTheme
        storage?.saveBrandingTheme(newTheme)
        triggerAutoSync()
    }

    // --- Products ---
    fun addProduct(product: Product) {
        _products.update { current ->
            listOf(product) + current
        }
        storage?.saveProducts(_products.value)
        _stockMovements.update { current ->
            listOf(
                StockMovement(
                    id = "SM-${System.currentTimeMillis()}",
                    productId = product.id,
                    productName = product.name,
                    changeQty = product.stock,
                    remainingQty = product.stock,
                    reason = "رصيد افتتاحي / إضافة صنف جديد",
                    referenceType = "PRODUCT_CREATION",
                    referenceId = product.id
                )
            ) + current
        }
        triggerAutoSync()
    }

    fun updateProduct(product: Product) {
        val oldProd = _products.value.find { it.id == product.id }
        if (oldProd != null && oldProd.stock != product.stock) {
            val diff = product.stock - oldProd.stock
            _stockMovements.update { current ->
                listOf(
                    StockMovement(
                        id = "SM-${System.currentTimeMillis()}",
                        productId = product.id,
                        productName = product.name,
                        changeQty = diff,
                        remainingQty = product.stock,
                        reason = "تعديل يدوي للمخزون / جرد مستودعي",
                        referenceType = "MANUAL_ADJUSTMENT",
                        referenceId = product.id
                    )
                ) + current
            }
        }

        _products.update { current ->
            current.map { if (it.id == product.id) product else it }
        }
        storage?.saveProducts(_products.value)
        triggerAutoSync()
    }

    fun deleteProduct(productId: String) {
        _products.update { current ->
            current.filterNot { it.id == productId }
        }
        storage?.saveProducts(_products.value)
        triggerAutoSync()
    }

    fun addCategory(category: String) {
        val trimmed = category.trim()
        if (trimmed.isNotEmpty() && !_categories.value.contains(trimmed)) {
            _categories.update { it + trimmed }
            triggerAutoSync()
        }
    }

    // --- Customers ---
    fun addCustomer(customer: Customer) {
        _customers.update { current ->
            current + customer
        }
        triggerAutoSync()
    }

    fun settleCustomerDebt(customerId: String, amount: Double) {
        val cust = _customers.value.find { it.id == customerId } ?: return
        val receiptVoucherId = "RV-${System.currentTimeMillis() % 10000}"
        
        // Auto Voucher + Auto Journal Entry
        addVoucher(
            Voucher(
                id = receiptVoucherId,
                voucherNumber = receiptVoucherId,
                type = VoucherType.RECEIPT,
                partyAccountCode = "103",
                partyName = cust.name,
                amount = amount,
                paymentMode = PaymentMode.CASH,
                note = "سداد ذمة العميل: ${cust.name}"
            )
        )

        _customers.update { list ->
            list.map { customer ->
                if (customer.id == customerId) {
                    val remaining = maxOf(0.0, customer.debt - amount)
                    val detail = if (remaining > 0) "دين: ${"%,.2f".format(remaining)} ريال" else "تم السداد بالكامل"
                    customer.copy(debt = remaining, detail = detail)
                } else {
                    customer
                }
            }
        }
        triggerAutoSync()
    }

    // --- Sales Orders & Automatic Accounting Entry ---
    fun recordSale(order: SaleOrder) {
        val journalId = "JE-SALE-${System.currentTimeMillis() % 100000}"
        val jvNumber = "JV-SALE-${1000 + _journalEntries.value.size + 1}"

        // Determine Debit Account
        val debitAccountCode = when (order.paymentMethod) {
            "شبكة" -> "102" // البنك
            "آجل" -> "103" // العملاء
            else -> "101" // الصندوق
        }
        val debitAccountName = when (order.paymentMethod) {
            "شبكة" -> "البنك (الحساب الجاري)"
            "آجل" -> "العملاء (الذمم المدينة)"
            else -> "الصندوق الرئيسي (النقدية)"
        }

        val saleJournalEntry = JournalEntry(
            id = journalId,
            entryNumber = jvNumber,
            date = order.timestamp,
            description = "فاتورة مبيعات ${order.id} - ${order.customer.name} (${order.paymentMethod})",
            lines = listOf(
                JournalEntryLine(debitAccountCode, debitAccountName, debit = order.total, credit = 0.0),
                JournalEntryLine("401", "إيرادات المبيعات", debit = 0.0, credit = order.total)
            ),
            referenceType = EntryReferenceType.SALE,
            referenceId = order.id
        )

        val updatedOrder = order.copy(
            journalEntryId = journalId,
            invoiceNumber = order.id
        )

        _orders.update { listOf(updatedOrder) + it }
        _journalEntries.update { listOf(saleJournalEntry) + it }

        // Deduct Inventory Stock & Record Stock Movement
        _products.update { productList ->
            productList.map { product ->
                val orderedItem = order.items.find { it.product.id == product.id }
                if (orderedItem != null) {
                    val newStock = maxOf(0, product.stock - orderedItem.quantity)
                    _stockMovements.update { movements ->
                        listOf(
                            StockMovement(
                                id = "SM-${System.currentTimeMillis()}-${product.id}",
                                productId = product.id,
                                productName = product.name,
                                changeQty = -orderedItem.quantity,
                                remainingQty = newStock,
                                reason = "صرف مبيعات فاتورة ${order.id}",
                                referenceType = "SALE",
                                referenceId = order.id
                            )
                        ) + movements
                    }
                    product.copy(stock = newStock)
                } else {
                    product
                }
            }
        }

        // Customer Debt if Credit
        if (order.paymentMethod == "آجل" && order.customer.id != "cash") {
            _customers.update { list ->
                list.map { customer ->
                    if (customer.id == order.customer.id) {
                        val newDebt = customer.debt + order.total
                        customer.copy(
                            debt = newDebt,
                            detail = "دين: ${"%,.2f".format(newDebt)} ريال"
                        )
                    } else {
                        customer
                    }
                }
            }
        }

        // Recalculate account balances
        refreshAccountBalances()
        triggerAutoSync()
    }

    fun deleteOrder(orderId: String) {
        val orderToDelete = _orders.value.find { it.id == orderId } ?: return

        // Restore stock
        _products.update { productList ->
            productList.map { product ->
                val itm = orderToDelete.items.find { it.product.id == product.id }
                if (itm != null) {
                    val restored = product.stock + itm.quantity
                    _stockMovements.update { movements ->
                        listOf(
                            StockMovement(
                                id = "SM-${System.currentTimeMillis()}-${product.id}",
                                productId = product.id,
                                productName = product.name,
                                changeQty = itm.quantity,
                                remainingQty = restored,
                                reason = "إلغاء فاتورة مبيعات ${orderToDelete.id}",
                                referenceType = "ORDER_DELETION",
                                referenceId = orderToDelete.id
                            )
                        ) + movements
                    }
                    product.copy(stock = restored)
                } else {
                    product
                }
            }
        }

        // Revert credit debt
        if (orderToDelete.paymentMethod == "آجل" && orderToDelete.customer.id != "cash") {
            _customers.update { list ->
                list.map { customer ->
                    if (customer.id == orderToDelete.customer.id) {
                        val newDebt = maxOf(0.0, customer.debt - orderToDelete.total)
                        val detail = if (newDebt > 0) "دين: ${"%,.2f".format(newDebt)} ريال" else "تم السداد بالكامل"
                        customer.copy(debt = newDebt, detail = detail)
                    } else {
                        customer
                    }
                }
            }
        }

        // Reverse Journal Entry
        if (orderToDelete.journalEntryId.isNotEmpty()) {
            _journalEntries.update { list ->
                list.filterNot { it.id == orderToDelete.journalEntryId }
            }
        }

        _orders.update { list -> list.filterNot { it.id == orderId } }
        refreshAccountBalances()
        triggerAutoSync()
    }

    fun updateOrderNote(orderId: String, newPaymentMethod: String, discount: Double) {
        _orders.update { list ->
            list.map { order ->
                if (order.id == orderId) {
                    val newTotal = maxOf(0.0, (order.subtotal + order.vat) - discount)
                    order.copy(paymentMethod = newPaymentMethod, discount = discount, total = newTotal)
                } else {
                    order
                }
            }
        }
        triggerAutoSync()
    }

    // --- Purchases Management & Auto Journal Entry ---
    fun addPurchase(purchase: PurchaseInvoice) {
        val journalId = "JE-PUR-${System.currentTimeMillis() % 100000}"
        val jvNumber = "JV-PUR-${2000 + _journalEntries.value.size + 1}"

        val creditCode = if (purchase.paymentMode == PaymentMode.CASH) "101" else "201"
        val creditName = if (purchase.paymentMode == PaymentMode.CASH) "الصندوق الرئيسي (النقدية)" else "الموردين (الذمم الدائنة)"

        val purJournalEntry = JournalEntry(
            id = journalId,
            entryNumber = jvNumber,
            date = purchase.date,
            description = "فاتورة مشتريات ${purchase.invoiceNumber} - مورد: ${purchase.supplierName}",
            lines = listOf(
                JournalEntryLine("501", "تكلفة المشتريات", debit = purchase.totalAmount, credit = 0.0),
                JournalEntryLine(creditCode, creditName, debit = 0.0, credit = purchase.totalAmount)
            ),
            referenceType = EntryReferenceType.PURCHASE,
            referenceId = purchase.id
        )

        val updatedPurchase = purchase.copy(journalEntryId = journalId)
        _purchases.update { listOf(updatedPurchase) + it }
        _journalEntries.update { listOf(purJournalEntry) + it }

        // Increase Stock
        purchase.items.forEach { item ->
            _products.update { list ->
                list.map { prod ->
                    if (prod.id == item.productId || prod.name.trim() == item.productName.trim()) {
                        val newStock = prod.stock + item.quantity
                        _stockMovements.update { movements ->
                            listOf(
                                StockMovement(
                                    id = "SM-${System.currentTimeMillis()}-${prod.id}",
                                    productId = prod.id,
                                    productName = prod.name,
                                    changeQty = item.quantity,
                                    remainingQty = newStock,
                                    reason = "توريد مشتريات فاتورة ${purchase.invoiceNumber}",
                                    referenceType = "PURCHASE",
                                    referenceId = purchase.id
                                )
                            ) + movements
                        }
                        prod.copy(stock = newStock, costPrice = item.unitCost)
                    } else {
                        prod
                    }
                }
            }
        }

        refreshAccountBalances()
        triggerAutoSync()
    }

    // --- Returns Management & Reversal Entries ---
    fun addReturn(returnInvoice: ReturnInvoice) {
        val journalId = "JE-RET-${System.currentTimeMillis() % 100000}"
        val jvNumber = "JV-RET-${3000 + _journalEntries.value.size + 1}"

        val lines = if (returnInvoice.type == ReturnType.SALE_RETURN) {
            // Sale Return: Debit Sales Returns (402), Credit Cash (101) or Customer (103)
            listOf(
                JournalEntryLine("402", "مردودات ومسموحات المبيعات", debit = returnInvoice.totalAmount, credit = 0.0),
                JournalEntryLine("101", "الصندوق الرئيسي (النقدية)", debit = 0.0, credit = returnInvoice.totalAmount)
            )
        } else {
            // Purchase Return: Debit Supplier (201) or Cash (101), Credit Purchase Returns (502)
            listOf(
                JournalEntryLine("101", "الصندوق الرئيسي (النقدية)", debit = returnInvoice.totalAmount, credit = 0.0),
                JournalEntryLine("502", "مردودات ومسموحات المشتريات", debit = 0.0, credit = returnInvoice.totalAmount)
            )
        }

        val retJournalEntry = JournalEntry(
            id = journalId,
            entryNumber = jvNumber,
            date = returnInvoice.date,
            description = "${returnInvoice.type.titleAr} رقم ${returnInvoice.returnNumber} - ${returnInvoice.partyName}",
            lines = lines,
            referenceType = if (returnInvoice.type == ReturnType.SALE_RETURN) EntryReferenceType.SALE_RETURN else EntryReferenceType.PURCHASE_RETURN,
            referenceId = returnInvoice.id
        )

        val updatedReturn = returnInvoice.copy(journalEntryId = journalId)
        _returns.update { listOf(updatedReturn) + it }
        _journalEntries.update { listOf(retJournalEntry) + it }

        // Adjust Stock
        val qtyMultiplier = if (returnInvoice.type == ReturnType.SALE_RETURN) 1 else -1
        returnInvoice.items.forEach { item ->
            _products.update { list ->
                list.map { prod ->
                    if (prod.id == item.productId) {
                        val newStock = maxOf(0, prod.stock + (item.quantity * qtyMultiplier))
                        _stockMovements.update { movements ->
                            listOf(
                                StockMovement(
                                    id = "SM-${System.currentTimeMillis()}-${prod.id}",
                                    productId = prod.id,
                                    productName = prod.name,
                                    changeQty = item.quantity * qtyMultiplier,
                                    remainingQty = newStock,
                                    reason = "${returnInvoice.type.titleAr} ${returnInvoice.returnNumber}",
                                    referenceType = returnInvoice.type.name,
                                    referenceId = returnInvoice.id
                                )
                            ) + movements
                        }
                        prod.copy(stock = newStock)
                    } else {
                        prod
                    }
                }
            }
        }

        refreshAccountBalances()
        triggerAutoSync()
    }

    // --- Vouchers (Receipts & Payments) ---
    fun addVoucher(voucher: Voucher) {
        val journalId = "JE-VCH-${System.currentTimeMillis() % 100000}"
        val jvNumber = "JV-VCH-${4000 + _journalEntries.value.size + 1}"

        val cashOrBankCode = if (voucher.paymentMode == PaymentMode.BANK_TRANSFER) "102" else "101"
        val cashOrBankName = if (voucher.paymentMode == PaymentMode.BANK_TRANSFER) "البنك (الحساب الجاري)" else "الصندوق الرئيسي (النقدية)"

        val lines = if (voucher.type == VoucherType.RECEIPT) {
            // Receipt: Debit Cash/Bank, Credit Party Account
            listOf(
                JournalEntryLine(cashOrBankCode, cashOrBankName, debit = voucher.amount, credit = 0.0),
                JournalEntryLine(voucher.partyAccountCode, voucher.partyName, debit = 0.0, credit = voucher.amount)
            )
        } else {
            // Payment: Debit Party Account, Credit Cash/Bank
            listOf(
                JournalEntryLine(voucher.partyAccountCode, voucher.partyName, debit = voucher.amount, credit = 0.0),
                JournalEntryLine(cashOrBankCode, cashOrBankName, debit = 0.0, credit = voucher.amount)
            )
        }

        val vchEntry = JournalEntry(
            id = journalId,
            entryNumber = jvNumber,
            date = voucher.date,
            description = "${voucher.type.titleAr} رقم ${voucher.voucherNumber} - ${voucher.partyName}",
            lines = lines,
            referenceType = if (voucher.type == VoucherType.RECEIPT) EntryReferenceType.RECEIPT_VOUCHER else EntryReferenceType.PAYMENT_VOUCHER,
            referenceId = voucher.id
        )

        val updatedVoucher = voucher.copy(journalEntryId = journalId)
        _vouchers.update { listOf(updatedVoucher) + it }
        _journalEntries.update { listOf(vchEntry) + it }

        refreshAccountBalances()
        triggerAutoSync()
    }

    // --- Chart of Accounts & Journal Entries CRUD ---
    fun addAccount(account: Account) {
        _accounts.update { it + account }
        triggerAutoSync()
    }

    fun updateAccount(account: Account) {
        _accounts.update { list ->
            list.map { if (it.code == account.code) account else it }
        }
        triggerAutoSync()
    }

    fun deleteAccount(accountCode: String): Boolean {
        val acc = _accounts.value.find { it.code == accountCode } ?: return false
        if (acc.isSystem) return false // Prevent deleting default critical accounts
        _accounts.update { list -> list.filterNot { it.code == accountCode } }
        triggerAutoSync()
        return true
    }

    fun addManualJournalEntry(entry: JournalEntry): Boolean {
        if (!entry.isBalanced) return false
        _journalEntries.update { listOf(entry) + it }
        refreshAccountBalances()
        triggerAutoSync()
        return true
    }

    private fun refreshAccountBalances() {
        val entries = _journalEntries.value
        _accounts.update { list ->
            list.map { account ->
                if (account.isParent) {
                    account
                } else {
                    var totalDebit = 0.0
                    var totalCredit = 0.0
                    entries.forEach { entry ->
                        entry.lines.forEach { line ->
                            if (line.accountCode == account.code) {
                                totalDebit += line.debit
                                totalCredit += line.credit
                            }
                        }
                    }
                    val net = if (account.nature == AccountNature.DEBIT) {
                        totalDebit - totalCredit
                    } else {
                        totalCredit - totalDebit
                    }
                    account.copy(balance = net)
                }
            }
        }
    }

    // --- Firebase & Backup ---
    fun updateFirebaseConfig(config: FirebaseConfig) {
        _firebaseConfig.value = config
        storage?.saveFirebaseConfig(config)
        triggerAutoSync()
    }

    fun exportBackupJson(): String {
        return storage?.exportBackupJson() ?: "{\"error\":\"Storage not ready\"}"
    }

    fun importBackupJson(jsonString: String): Boolean {
        val success = storage?.importBackupJson(jsonString) ?: false
        if (success) {
            _users.value = storage?.loadUsers(defaultUsers) ?: defaultUsers
            _products.value = storage?.loadProducts(initialProducts) ?: initialProducts
            _orders.value = storage?.loadOrders(emptyList()) ?: emptyList()
            _customers.value = storage?.loadCustomers(initialCustomers) ?: initialCustomers
            _categories.value = storage?.loadCategories(initialCategories) ?: initialCategories
            _brandingTheme.value = storage?.loadBrandingTheme() ?: AppBrandingTheme.LUXURY_GOLD
            _accounts.value = storage?.loadAccounts(defaultAccounts) ?: defaultAccounts
            _journalEntries.value = storage?.loadJournalEntries(emptyList()) ?: emptyList()
            _vouchers.value = storage?.loadVouchers(emptyList()) ?: emptyList()
            _purchases.value = storage?.loadPurchases() ?: emptyList()
            _returns.value = storage?.loadReturns() ?: emptyList()
            _stockMovements.value = storage?.loadStockMovements() ?: emptyList()
            _firebaseConfig.value = storage?.loadFirebaseConfig() ?: FirebaseConfig()
            _lastSyncTime.value = storage?.getLastSyncTime() ?: System.currentTimeMillis()
            _syncStatus.value = SyncStatus.SYNCED
        }
        return success
    }

    fun manualSyncNow() {
        triggerAutoSync()
    }
}
