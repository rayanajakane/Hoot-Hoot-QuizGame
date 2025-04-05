package com.example.polyquiz.shop.domain

data class ShopItem(
    val id: String,
    val imageUrl: String,
    val price: Int,
    var owned: Boolean = false
)
