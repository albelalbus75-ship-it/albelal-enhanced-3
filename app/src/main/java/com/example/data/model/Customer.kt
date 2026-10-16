package com.example.data.model

data class Customer(
    val id: String,
    val name: String,
    val detail: String,
    val debt: Double = 0.0,
    val phone: String = "0500000000"
)
