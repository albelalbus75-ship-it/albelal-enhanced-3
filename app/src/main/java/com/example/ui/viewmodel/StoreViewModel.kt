package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class StoreViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = StoreRepository(application.applicationContext)

    // --- Authentication & User Management ---
    val currentUser: StateFlow<User?> = repository.currentUser
    val users: StateFlow<List<User>> = repository.users

    val isAdmin: StateFlow<Boolean> = currentUser.map { user ->
        user?.role == UserRole.ADMIN
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    // Permission checks helper (No screen or action is hidden before login)
    fun canAccessScreen(screen: AppScreen?): Boolean {
        if (screen == null) return true
        val user = currentUser.value ?: return true
        return try {
            user.canAccessScreen(screen)
        } catch (e: Throwable) {
            true
        }
    }

    fun hasPermission(screen: AppScreen?, action: ScreenAction?): Boolean {
        if (screen == null || action == null) return true
        val user = currentUser.value ?: return true
        return try {
            user.hasPermission(screen, action)
        } catch (e: Throwable) {
            true
        }
    }

    // --- Branding & Identity Theme ---
    val brandingTheme: StateFlow<AppBrandingTheme> = repository.brandingTheme

    fun updateBrandingTheme(newTheme: AppBrandingTheme) {
        if (!isAdmin.value) {
            showNotice("تغيير ثيم وهوية التطبيق متاح للمدير العام فقط")
            return
        }
        repository.updateBrandingTheme(newTheme)
        showNotice("تم الحفظ وتطبيق الهوية بنجاح")
    }

    // --- Sync & Backup ---
    val syncStatus: StateFlow<SyncStatus> = repository.syncStatus
    val lastSyncTime: StateFlow<Long> = repository.lastSyncTime
    val firebaseConfig: StateFlow<FirebaseConfig> = repository.firebaseConfig

    // Currencies & Exchange Rate
    val currencies: StateFlow<List<CurrencyRate>> = repository.currencies
    private val _selectedCurrency = MutableStateFlow(
        CurrencyRate(code = "SAR", nameAr = "ريال سعودي", symbol = "SAR", exchangeRate = 1.0, isDefault = true)
    )
    val selectedCurrency: StateFlow<CurrencyRate> = _selectedCurrency.asStateFlow()

    fun selectCurrency(currency: CurrencyRate) {
        val current = currencies.value.find { it.code.equals(currency.code, ignoreCase = true) } ?: currency
        _selectedCurrency.value = current
    }

    fun updateCurrencyExchangeRate(code: String, newRate: Double) {
        if (newRate <= 0.0) return
        repository.updateCurrencyExchangeRate(code, newRate)
        if (_selectedCurrency.value.code.equals(code, ignoreCase = true)) {
            _selectedCurrency.value = _selectedCurrency.value.copy(exchangeRate = newRate)
        }
        showNotice("تم تحديث سعر صرف $code إلى $newRate فوراً")
    }

    fun convertAmount(amountInSAR: Double, currency: CurrencyRate = selectedCurrency.value): Double {
        return amountInSAR * currency.exchangeRate
    }

    // Filter states
    val categories: StateFlow<List<String>> = repository.categories
    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Inspecting product dialog state
    private val _inspectingProduct = MutableStateFlow<Product?>(null)
    val inspectingProduct: StateFlow<Product?> = _inspectingProduct.asStateFlow()
    fun inspectProduct(product: Product?) {
        _inspectingProduct.value = product
    }

    // POS discount state
    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()
    fun setDiscount(value: Double) {
        _discount.value = maxOf(0.0, value)
    }

    val selectedPayment: StateFlow<String> get() = selectedPaymentMethod

    // Filtered products
    val products: StateFlow<List<Product>> = combine(
        repository.products,
        _selectedCategory,
        _searchQuery
    ) { allProducts, cat, query ->
        allProducts.filter { product ->
            val matchesCategory = cat == "الكل" || product.category == cat
            val matchesQuery = query.isBlank() ||
                    product.name.contains(query, ignoreCase = true) ||
                    product.barcode.contains(query, ignoreCase = true) ||
                    product.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<Product>> = repository.products

    // Pull-to-refresh & Infinite Scroll / Pagination state
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _itemsLimit = MutableStateFlow(6)
    val itemsLimit: StateFlow<Int> = _itemsLimit.asStateFlow()

    val paginatedProducts: StateFlow<List<Product>> = combine(
        products,
        _itemsLimit
    ) { list, limit ->
        list.take(limit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasMoreProducts: StateFlow<Boolean> = combine(
        products,
        _itemsLimit
    ) { list, limit ->
        list.size > limit
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Editing product state
    private val _editingProduct = MutableStateFlow<Product?>(null)
    val editingProduct: StateFlow<Product?> = _editingProduct.asStateFlow()

    // Cart state
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    val cart: StateFlow<List<CartItem>> = _cart.asStateFlow()

    val customers: StateFlow<List<Customer>> = repository.customers
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _selectedPaymentMethod = MutableStateFlow("نقدًا")
    val selectedPaymentMethod: StateFlow<String> = _selectedPaymentMethod.asStateFlow()

    // Sales Orders History
    val orders: StateFlow<List<SaleOrder>> = repository.orders

    // Completed order for receipt dialog
    private val _completedOrder = MutableStateFlow<SaleOrder?>(null)
    val completedOrder: StateFlow<SaleOrder?> = _completedOrder.asStateFlow()

    // Notice message banner
    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    // Selected customer for detail
    private val _selectedCustomerForDetail = MutableStateFlow<Customer?>(null)
    val selectedCustomerForDetail: StateFlow<Customer?> = _selectedCustomerForDetail.asStateFlow()

    // --- Accounting Flows ---
    val accounts: StateFlow<List<Account>> = repository.accounts
    val journalEntries: StateFlow<List<JournalEntry>> = repository.journalEntries
    val vouchers: StateFlow<List<Voucher>> = repository.vouchers
    val purchases: StateFlow<List<PurchaseInvoice>> = repository.purchases
    val returns: StateFlow<List<ReturnInvoice>> = repository.returns
    val stockMovements: StateFlow<List<StockMovement>> = repository.stockMovements

    init {
        _selectedCustomer.value = repository.customers.value.firstOrNull { it.id == "cash" }
    }

    // --- Authentication Actions ---
    fun login(username: String, pass: String): Boolean {
        val success = repository.login(username, pass)
        if (success) {
            val user = repository.currentUser.value
            showNotice("أهلاً بك يا ${user?.fullName} (${user?.role?.titleAr})")
        } else {
            showNotice("اسم المستخدم أو كلمة المرور غير صحيحة")
        }
        return success
    }

    fun logout() {
        val name = currentUser.value?.fullName ?: "المستخدم"
        repository.logout()
        showNotice("تم تسجيل خروج $name")
    }

    fun switchUser(userId: String) {
        repository.switchUser(userId)
        val user = repository.currentUser.value
        showNotice("تم التبديل إلى: ${user?.fullName}")
    }

    fun addUser(
        username: String,
        pass: String,
        fullName: String,
        role: UserRole,
        phone: String = "",
        customPermissions: Map<String, List<String>> = emptyMap()
    ): Boolean {
        if (!isAdmin.value) {
            showNotice("عفواً: إدارة المستخدمين متاحة للمدير العام فقط")
            return false
        }
        if (users.value.any { it.username.equals(username.trim(), ignoreCase = true) }) {
            showNotice("اسم المستخدم \"$username\" مستخدم بالفعل")
            return false
        }
        val user = User(
            id = "usr_" + UUID.randomUUID().toString().take(6),
            username = username.trim(),
            password = pass.trim(),
            fullName = fullName.trim(),
            role = role,
            phone = phone.trim(),
            customPermissions = customPermissions
        )
        repository.addUser(user)
        showNotice("تم إنشاء حساب \"${user.fullName}\" بنجاح")
        return true
    }

    fun updateUser(user: User) {
        if (!isAdmin.value) {
            showNotice("عفواً: تعديل المستخدمين متاح للمدير العام فقط")
            return
        }
        repository.updateUser(user)
        showNotice("تم تحديث حساب \"${user.fullName}\" بنجاح")
    }

    fun toggleUserActive(userId: String) {
        if (!isAdmin.value) {
            showNotice("عفواً: تعديل حالة المستخدم متاح للمدير فقط")
            return
        }
        repository.toggleUserActive(userId)
        showNotice("تم تحديث حالة تفعيل الحساب")
    }

    fun deleteUser(userId: String) {
        if (!isAdmin.value) {
            showNotice("عفواً: حذف المستخدمين متاح للمدير فقط")
            return
        }
        repository.deleteUser(userId)
        showNotice("تم حذف المستخدم بنجاح")
    }

    fun createUser(
        username: String,
        pass: String,
        fullName: String,
        role: UserRole,
        phone: String = ""
    ): Boolean = addUser(username, pass, fullName, role, phone)

    fun updateAdminPassword(newPass: String): Boolean {
        if (!isAdmin.value) {
            showNotice("عفواً: تغيير كلمة مرور المدير يتطلب صلاحية المدير العام")
            return false
        }
        if (newPass.trim().length < 4) {
            showNotice("كلمة المرور يجب ألا تقل عن 4 خانات")
            return false
        }
        val res = repository.updateAdminPassword(newPass)
        if (res) {
            showNotice("تم تحديث كلمة مرور المدير بنجاح")
        }
        return res
    }

    fun manualSync() = manualSyncNow()

    // --- Filter methods ---
    fun setCategory(category: String) {
        _selectedCategory.value = category
        _itemsLimit.value = 6
    }

    fun selectCategory(category: String) = setCategory(category)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        _itemsLimit.value = 6
    }

    // --- Cart Actions ---
    fun addToCart(product: Product) {
        if (product.stock <= 0) {
            showNotice("عذراً، المنتج نفد من المستودع")
            return
        }

        _cart.update { currentCart ->
            val existingIndex = currentCart.indexOfFirst { it.product.id == product.id }
            if (existingIndex >= 0) {
                val existingItem = currentCart[existingIndex]
                if (existingItem.quantity < product.stock) {
                    currentCart.toMutableList().apply {
                        this[existingIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
                    }
                } else {
                    showNotice("وصلت للحد الأقصى المتوفر بالمخزون (${product.stock})")
                    currentCart
                }
            } else {
                currentCart + CartItem(product, 1)
            }
        }
    }

    fun removeFromCart(productId: String) {
        _cart.update { current ->
            current.filterNot { it.product.id == productId }
        }
    }

    fun updateCartQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        _cart.update { current ->
            current.map { item ->
                if (item.product.id == productId) {
                    val maxQty = item.product.stock
                    val clamped = if (newQuantity > maxQty) {
                        showNotice("الكمية المطلوبة تتجاوز المخزون ($maxQty)")
                        maxQty
                    } else newQuantity
                    item.copy(quantity = clamped)
                } else item
            }
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) = updateCartQuantity(productId, newQuantity)

    fun clearCart() {
        _cart.value = emptyList()
    }

    fun selectCustomer(customer: Customer) {
        _selectedCustomer.value = customer
    }

    fun setCustomer(customer: Customer) = selectCustomer(customer)

    fun selectCustomerForDetail(customer: Customer?) {
        _selectedCustomerForDetail.value = customer
    }

    fun setPaymentMethod(method: String) {
        _selectedPaymentMethod.value = method
    }

    fun setPayment(method: String) = setPaymentMethod(method)

    // --- POS Checkout & Sale Record with Journal Entry ---
    fun checkout(discount: Double = 0.0): SaleOrder? {
        val currentCart = _cart.value
        if (currentCart.isEmpty()) return null

        val customer = _selectedCustomer.value ?: repository.customers.value.first { it.id == "cash" }
        val paymentMethod = _selectedPaymentMethod.value

        val subtotal = currentCart.sumOf { it.product.price * it.quantity }
        val vat = subtotal * 0.15
        val discountedTotal = maxOf(0.0, (subtotal + vat) - discount)

        val nextOrderNumber = 1000 + repository.orders.value.size + 1
        val order = SaleOrder(
            id = "INV-$nextOrderNumber",
            items = currentCart,
            customer = customer,
            subtotal = subtotal,
            vat = vat,
            discount = discount,
            total = discountedTotal,
            paymentMethod = paymentMethod
        )

        repository.recordSale(order)
        _completedOrder.value = order
        clearCart()
        showNotice("تم إصدار الفاتورة ${order.id} وترحيل القيد المحاسبي بنجاح")
        return order
    }

    fun finishSale(discount: Double = 0.0): SaleOrder? = checkout(discount)

    fun dismissReceipt() {
        _completedOrder.value = null
    }

    fun dismissCompletedOrder() = dismissReceipt()

    // --- Invoices Management ---
    fun deleteOrder(orderId: String) {
        if (!hasPermission(AppScreen.INVOICES, ScreenAction.DELETE)) {
            showNotice("عفواً: حذف الفواتير يتطلب صلاحية مدير النظام")
            return
        }
        repository.deleteOrder(orderId)
        showNotice("تم حذف الفاتورة وإلغاء القيد المحاسبي وإعادة الكميات")
    }

    fun updateOrder(orderId: String, paymentMethod: String, discount: Double) {
        if (!hasPermission(AppScreen.INVOICES, ScreenAction.EDIT)) {
            showNotice("عفواً: تعديل الفواتير يتطلب صلاحية المدير")
            return
        }
        repository.updateOrderNote(orderId, paymentMethod, discount)
        showNotice("تم تعديل بيانات الفاتورة بنجاح")
    }

    // --- Accounting Operations ---
    fun addAccount(account: Account) {
        if (!hasPermission(AppScreen.ACCOUNTS, ScreenAction.ADD)) {
            showNotice("عفواً: إضافة حساب تتطلب صلاحية إدارة الحسابات")
            return
        }
        if (accounts.value.any { it.code == account.code }) {
            showNotice("رقم الحساب ${account.code} مستخدم مسبقاً")
            return
        }
        repository.addAccount(account)
        showNotice("تمت إضافة الحساب \"${account.name}\" بنجاح")
    }

    fun updateAccount(account: Account) {
        if (!hasPermission(AppScreen.ACCOUNTS, ScreenAction.EDIT)) {
            showNotice("عفواً: تعديل الحساب يتطلب صلاحية إدارة الحسابات")
            return
        }
        repository.updateAccount(account)
        showNotice("تم تعديل بيانات الحساب بنجاح")
    }

    fun deleteAccount(accountCode: String) {
        if (!hasPermission(AppScreen.ACCOUNTS, ScreenAction.DELETE)) {
            showNotice("عفواً: حذف الحساب يتطلب صلاحية إدارة الحسابات")
            return
        }
        val success = repository.deleteAccount(accountCode)
        if (success) {
            showNotice("تم حذف الحساب بنجاح")
        } else {
            showNotice("لا يمكن حذف هذا الحساب (حساب نظام أساسي)")
        }
    }

    fun addManualJournalEntry(description: String, lines: List<JournalEntryLine>): Boolean {
        if (!hasPermission(AppScreen.ACCOUNTS, ScreenAction.ADD)) {
            showNotice("عفواً: إنشاء القيود يتطلب صلاحية محاسبية")
            return false
        }
        val entryId = "JE-MANUAL-${System.currentTimeMillis() % 100000}"
        val entryNumber = "JV-MAN-${5000 + journalEntries.value.size + 1}"
        val entry = JournalEntry(
            id = entryId,
            entryNumber = entryNumber,
            description = description.trim(),
            lines = lines,
            referenceType = EntryReferenceType.MANUAL,
            createdBy = currentUser.value?.username ?: "admin"
        )
        val success = repository.addManualJournalEntry(entry)
        if (success) {
            showNotice("تم ترحيل القيد اليومي $entryNumber بنجاح")
        } else {
            showNotice("القيد غير متوازن: إجمالي المدين يجب أن يساوي إجمالي الدائن")
        }
        return success
    }

    fun addVoucher(voucher: Voucher) {
        if (!hasPermission(AppScreen.ACCOUNTS, ScreenAction.ADD)) {
            showNotice("عفواً: إصدار السندات يتطلب صلاحية محاسبية")
            return
        }
        repository.addVoucher(voucher)
        showNotice("تم ترحيل ${voucher.type.titleAr} رقم ${voucher.voucherNumber} بنجاح")
    }

    fun addPurchase(purchase: PurchaseInvoice) {
        if (!hasPermission(AppScreen.INVOICES, ScreenAction.ADD)) {
            showNotice("عفواً: تسجيل المشتريات يتطلب صلاحية إضافة الفواتير")
            return
        }
        repository.addPurchase(purchase)
        showNotice("تم حفظ فاتورة المشتريات رقم ${purchase.invoiceNumber} وتحديث المخزون والقيد")
    }

    fun addReturn(returnInvoice: ReturnInvoice) {
        if (!hasPermission(AppScreen.INVOICES, ScreenAction.ADD)) {
            showNotice("عفواً: تسجيل المرتجعات يتطلب صلاحية الفواتير")
            return
        }
        repository.addReturn(returnInvoice)
        showNotice("تم حفظ ${returnInvoice.type.titleAr} رقم ${returnInvoice.returnNumber} وترحيل القيد العكسي")
    }

    // --- Pull to Refresh & Load More ---
    fun refreshProducts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500)
            _itemsLimit.value = 6
            _isRefreshing.value = false
            repository.manualSyncNow()
        }
    }

    fun loadMoreProducts() {
        if (_isLoadingMore.value || !hasMoreProducts.value) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            delay(400)
            _itemsLimit.update { it + 6 }
            _isLoadingMore.value = false
        }
    }

    // --- Product Management ---
    fun startEditingProduct(product: Product) {
        _editingProduct.value = product
    }

    fun clearEditingProduct() {
        _editingProduct.value = null
    }

    fun validateProductUniqueness(barcode: String, model: String, excludeId: String? = null): String? {
        val trimmedBarcode = barcode.trim()
        val trimmedModel = model.trim()
        if (trimmedBarcode.isNotEmpty()) {
            val duplicateBarcode = allProducts.value.any { it.id != excludeId && it.barcode.isNotBlank() && it.barcode.equals(trimmedBarcode, ignoreCase = true) }
            if (duplicateBarcode) {
                return "عفواً: الباركود \"$trimmedBarcode\" مسجل بالفعل لصنف آخر! يرجى إدخال أو توليد باركود فريد لمنع التكرار."
            }
        }
        if (trimmedModel.isNotEmpty()) {
            val duplicateModel = allProducts.value.any { it.id != excludeId && it.model.isNotBlank() && (it.model.equals(trimmedModel, ignoreCase = true) || it.id.equals(trimmedModel, ignoreCase = true)) }
            if (duplicateModel) {
                return "عفواً: كود الصنف / الموديل \"$trimmedModel\" مكرر ومسجل لصنف آخر بالفعل!"
            }
        }
        return null
    }

    fun addNewProduct(
        name: String,
        category: String,
        price: Double,
        costPrice: Double,
        wholesalePrice: Double,
        stock: Int,
        model: String,
        unit: String,
        barcode: String,
        note: String,
        primaryImageUri: String?,
        additionalImages: List<String> = emptyList()
    ): String? {
        val error = validateProductUniqueness(barcode, model, excludeId = null)
        if (error != null) {
            return error
        }

        val newProduct = Product(
            id = "p_" + UUID.randomUUID().toString().take(6),
            name = name.trim(),
            category = category.trim().ifEmpty { "أدوات وتراثيات" },
            price = price,
            costPrice = costPrice,
            wholesalePrice = wholesalePrice,
            stock = stock,
            model = model.trim(),
            unit = unit.trim().ifEmpty { "حبة" },
            barcode = barcode.trim().ifEmpty { "628${System.currentTimeMillis().toString().takeLast(8)}" },
            note = note.trim(),
            colorHex = "#181818",
            primaryImageUri = primaryImageUri,
            additionalImages = additionalImages
        )
        repository.addProduct(newProduct)
        showNotice("تم حفظ الصنف \"${newProduct.name}\" وإيداعه المخزون بنجاح")
        return null
    }

    fun updateProduct(
        id: String,
        name: String,
        category: String,
        price: Double,
        costPrice: Double,
        wholesalePrice: Double,
        stock: Int,
        model: String,
        unit: String,
        barcode: String,
        note: String,
        primaryImageUri: String?,
        additionalImages: List<String> = emptyList(),
        imageResId: Int? = null
    ): String? {
        val error = validateProductUniqueness(barcode, model, excludeId = id)
        if (error != null) {
            return error
        }

        val updated = Product(
            id = id,
            name = name.trim(),
            category = category.trim().ifEmpty { "أدوات وتراثيات" },
            price = price,
            costPrice = costPrice,
            wholesalePrice = wholesalePrice,
            stock = stock,
            model = model.trim(),
            unit = unit.trim().ifEmpty { "حبة" },
            barcode = barcode.trim(),
            note = note.trim(),
            colorHex = "#181818",
            primaryImageUri = primaryImageUri,
            additionalImages = additionalImages,
            imageResId = imageResId
        )
        repository.updateProduct(updated)
        _editingProduct.value = null
        showNotice("تم تحديث بيانات الصنف \"${updated.name}\" بنجاح")
        return null
    }

    fun deleteProduct(productId: String) {
        if (!hasPermission(AppScreen.PRODUCTS, ScreenAction.DELETE)) {
            showNotice("عفواً: حذف الأصناف متاح لمدير النظام فقط")
            return
        }
        repository.deleteProduct(productId)
        if (_editingProduct.value?.id == productId) {
            _editingProduct.value = null
        }
        showNotice("تم حذف الصنف بنجاح")
    }

    fun addNewCategory(name: String) {
        if (name.isNotBlank()) {
            repository.addCategory(name.trim())
            showNotice("تمت إضافة قسم \"$name\"")
        }
    }

    fun addNewCustomer(name: String, phone: String, initialDebt: Double) {
        if (name.isNotBlank()) {
            val customer = Customer(
                id = "cust_" + UUID.randomUUID().toString().take(6),
                name = name.trim(),
                detail = if (initialDebt > 0) "دين: ${"%,.2f".format(initialDebt)} ريال" else "حساب جديد",
                debt = initialDebt,
                phone = phone.trim().ifEmpty { "-" }
            )
            repository.addCustomer(customer)
            showNotice("تمت إضافة العميل \"${customer.name}\"")
        }
    }

    fun settleDebt(customerId: String, amount: Double) {
        if (amount > 0) {
            repository.settleCustomerDebt(customerId, amount)
            showNotice("تم تسجيل سند قبض وسداد مبلغ ${"%,.2f".format(amount)} ريال")
            _selectedCustomerForDetail.value = null
        }
    }

    // --- Firebase and Backup ---
    fun updateFirebaseConfig(config: FirebaseConfig) {
        repository.updateFirebaseConfig(config)
        showNotice("تم حفظ إعدادات الربط السحابي Firebase")
    }

    fun exportBackupJson(): String = repository.exportBackupJson()

    fun importBackupJson(json: String): Boolean {
        val success = repository.importBackupJson(json)
        if (success) {
            showNotice("تمت استعادة كافة البيانات المحاسبية والنظام بنجاح")
        } else {
            showNotice("فشل استيراد البيانات: ملف JSON غير صالح")
        }
        return success
    }

    fun manualSyncNow() {
        repository.manualSyncNow()
    }

    fun showNotice(message: String) {
        _notice.value = message
    }

    fun dismissNotice() {
        _notice.value = null
    }
}
