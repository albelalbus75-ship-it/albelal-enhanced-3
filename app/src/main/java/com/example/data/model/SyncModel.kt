package com.example.data.model

enum class SyncStatus(val labelAr: String) {
    SYNCED("متزامن محلياً وسحابياً"),
    SYNCING("جارٍ الحفظ والمزامنة..."),
    OFFLINE("وضع غير متصل"),
    ERROR("تعذر الاتصال بالسحابة")
}

data class FirebaseConfig(
    val projectId: String = "al-faisal-store",
    val apiKey: String = "",
    val appId: String = "1:123456789:android:abcdef",
    val isEnabled: Boolean = false,
    val autoSyncIntervalMinutes: Int = 5,
    val collectionPrefix: String = "store_v1"
)
