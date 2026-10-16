package com.example.data.model

data class Product(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val costPrice: Double = price * 0.75,
    val wholesalePrice: Double = price * 0.9,
    val stock: Int = 10,
    val iconName: String = "store",
    val colorHex: String = "#1A1A1A",
    val note: String = "",
    val barcode: String = "",
    val model: String = "",
    val unit: String = "حبة",
    val imageResId: Int? = null,
    val primaryImageUri: String? = null,
    val additionalImages: List<String> = emptyList()
)
