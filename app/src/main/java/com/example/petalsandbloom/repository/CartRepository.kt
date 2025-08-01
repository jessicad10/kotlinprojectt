package com.example.petalsandbloom.repository



import com.example.petalsandbloom.model.CartItemModel

interface CartRepository {
    fun addToCart(item: CartItemModel, callback: (Boolean, String) -> Unit)
    fun getCartItems(callback: (List<CartItemModel>, Boolean, String) -> Unit)
    fun updateCartItemQuantity(cartItemId: String, quantity: Int, callback: (Boolean, String) -> Unit)
    fun removeCartItem(cartItemId: String, callback: (Boolean, String) -> Unit)
}