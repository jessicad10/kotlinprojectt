package com.example.petalsandbloom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petalsandbloom.model.CartItemModel
import com.example.petalsandbloom.repository.CartRepository

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItemModel>>()
    val cartItems: LiveData<List<CartItemModel>> get() = _cartItems

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    fun loadCartItems() {
        cartRepository.getCartItems { items, success, message ->
            if (success) {
                _cartItems.postValue(items)
            } else {
                _error.postValue(message)
            }
        }
    }

    fun addToCart(item: CartItemModel) {
        cartRepository.addToCart(item) { success, message ->
            if (!success) {
                _error.postValue(message)
            } else {
                loadCartItems() // refresh after adding
            }
        }
    }

    fun updateQuantity(cartItemId: String, newQuantity: Int) {
        cartRepository.updateCartItemQuantity(cartItemId, newQuantity) { success, message ->
            if (!success) {
                _error.postValue(message)
            } else {
                loadCartItems() // refresh after updating
            }
        }
    }

    fun removeCartItem(cartItemId: String) {
        cartRepository.removeCartItem(cartItemId) { success, message ->
            if (!success) {
                _error.postValue(message)
            } else {
                loadCartItems() // refresh after removing
            }
        }
    }

    fun clearError() {
        _error.postValue(null)
    }
}