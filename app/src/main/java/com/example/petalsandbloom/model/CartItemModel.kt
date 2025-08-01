package com.example.petalsandbloom.model


data class CartItemModel(
    var id: String = "",
    var productId: String = "",
    var productName: String = "",
    var productPrice: Double = 0.0, // ✅ Fix here
    var quantity: Int = 1,
    var image: String = ""
)
