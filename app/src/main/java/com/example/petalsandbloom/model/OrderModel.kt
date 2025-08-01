package com.example.petalsandbloom.model

//import com.example.sportsequipmentstore.model.CartItemModel

data class OrderModel(
    var orderId: String = "",      // Changed from val to var
    val userId: String = "",
    val items: List<CartItemModel> = emptyList(),
    val totalAmount: Double = 0.0,
    val orderStatus: String = "Pending",
    val orderDate: Long = System.currentTimeMillis()
)
