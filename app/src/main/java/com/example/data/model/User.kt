package com.example.data.model

enum class AppScreen(val id: String, val titleAr: String) {
    HOME("home", "الرئيسية"),
    POS("pos", "نقطة البيع"),
    PRODUCTS("products", "الأصناف"),
    INVENTORY("inventory", "المخزون"),
    INVOICES("invoices", "الفواتير"),
    ACCOUNTS("accounts", "الحسابات"),
    REPORTS("reports", "التقارير"),
    SETTINGS("settings", "الإعدادات"),
    USERS("users", "المستخدمين")
}

enum class ScreenAction(val id: String, val titleAr: String) {
    VIEW("view", "عرض"),
    ADD("add", "إضافة"),
    EDIT("edit", "تعديل"),
    DELETE("delete", "حذف"),
    PRINT("print", "طباعة"),
    APPROVE("approve", "اعتماد/ترحيل")
}

enum class UserRole(val code: String, val titleAr: String, val descriptionAr: String) {
    ADMIN("ADMIN", "مدير عام", "صلاحيات كاملة لكل الشاشات والعمليات والتهيئة"),
    ACCOUNTANT("ACCOUNTANT", "محاسب", "إدارة الحسابات، قيود اليومية، السندات، الفواتير، والتقارير المالية"),
    CASHIER("CASHIER", "كاشير", "نقاط البيع، استعراض الأصناف، إصدار الفواتير وطباعتها"),
    STOREKEEPER("STOREKEEPER", "أمين مخزن", "إدارة المخزون، الأصناف، الجرد، والتوريدات"),
    REPORTS_VIEWER("REPORTS_VIEWER", "مشاهد تقارير", "الاطلاع على التقارير وطباعتها بدون صلاحيات تعديل")
}

data class User(
    val id: String,
    val username: String,
    val password: String,
    val fullName: String,
    val role: UserRole,
    val isActive: Boolean = true,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // Custom fine-grained permissions per screen: map of AppScreen name to set of ScreenAction names
    val customPermissions: Map<String, List<String>> = emptyMap()
) {
    fun canAccessScreen(screen: AppScreen?): Boolean {
        if (screen == null) return true
        if (!isActive) return false
        val currentRole = role ?: UserRole.ADMIN
        if (currentRole == UserRole.ADMIN) return true
        
        // If user has custom permissions defined, use them
        if (customPermissions.isNotEmpty()) {
            val actions = customPermissions[screen.name] ?: emptyList()
            return actions.contains(ScreenAction.VIEW.name) || actions.isNotEmpty()
        }
        
        // Default role based permissions
        return when (currentRole) {
            UserRole.ADMIN -> true
            UserRole.ACCOUNTANT -> screen in setOf(
                AppScreen.HOME, AppScreen.POS, AppScreen.INVOICES,
                AppScreen.ACCOUNTS, AppScreen.REPORTS
            )
            UserRole.CASHIER -> screen in setOf(
                AppScreen.HOME, AppScreen.POS, AppScreen.PRODUCTS, AppScreen.INVOICES
            )
            UserRole.STOREKEEPER -> screen in setOf(
                AppScreen.HOME, AppScreen.PRODUCTS, AppScreen.INVENTORY
            )
            UserRole.REPORTS_VIEWER -> screen in setOf(
                AppScreen.HOME, AppScreen.REPORTS
            )
            else -> true
        }
    }

    fun hasPermission(screen: AppScreen?, action: ScreenAction?): Boolean {
        if (screen == null || action == null) return true
        if (!isActive) return false
        val currentRole = role ?: UserRole.ADMIN
        if (currentRole == UserRole.ADMIN) return true

        if (customPermissions.isNotEmpty()) {
            val actions = customPermissions[screen.name] ?: emptyList()
            return actions.contains(action.name)
        }

        // Default role action matrix
        return when (currentRole) {
            UserRole.ADMIN -> true
            UserRole.ACCOUNTANT -> {
                when (screen) {
                    AppScreen.ACCOUNTS, AppScreen.INVOICES, AppScreen.REPORTS -> true
                    AppScreen.POS -> action in setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.PRINT)
                    AppScreen.HOME -> action == ScreenAction.VIEW
                    else -> false
                }
            }
            UserRole.CASHIER -> {
                when (screen) {
                    AppScreen.POS -> action in setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.PRINT)
                    AppScreen.PRODUCTS -> action == ScreenAction.VIEW
                    AppScreen.INVOICES -> action in setOf(ScreenAction.VIEW, ScreenAction.PRINT)
                    AppScreen.HOME -> action == ScreenAction.VIEW
                    else -> false
                }
            }
            UserRole.STOREKEEPER -> {
                when (screen) {
                    AppScreen.PRODUCTS, AppScreen.INVENTORY -> action in setOf(ScreenAction.VIEW, ScreenAction.ADD, ScreenAction.EDIT, ScreenAction.PRINT)
                    AppScreen.HOME -> action == ScreenAction.VIEW
                    else -> false
                }
            }
            UserRole.REPORTS_VIEWER -> {
                when (screen) {
                    AppScreen.REPORTS, AppScreen.HOME -> action in setOf(ScreenAction.VIEW, ScreenAction.PRINT)
                    else -> false
                }
            }
            else -> true
        }
    }
}
