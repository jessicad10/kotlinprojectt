package com.example.petalsandbloom.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petalsandbloom.model.OrderModel
import com.example.petalsandbloom.repository.OrderRepository


class OrderViewModel(private val orderRepository: OrderRepository) : ViewModel() {

    private val _userOrders = MutableLiveData<List<OrderModel>>()
    val userOrders: LiveData<List<OrderModel>> = _userOrders

    private val _allOrders = MutableLiveData<List<OrderModel>>()
    val allOrders: LiveData<List<OrderModel>> = _allOrders

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Load all orders (for admin or global views)
    fun loadAllOrders() {
        orderRepository.getAllOrders { list, success, message ->
            if (success) {
                _allOrders.postValue(list)
                _error.postValue(null)
            } else {
                _error.postValue(message)
            }
        }
    }

    // Load orders for a specific user (this is what you want for dashboard)
    fun loadOrdersByUser(userId: String) {
        orderRepository.getOrdersByUser(userId) { list, success, message ->
            if (success) {
                _userOrders.postValue(list)
                _error.postValue(null)
            } else {
                _error.postValue(message)
            }
        }
    }

    // Place order and reload user orders on success
    fun placeOrder(order: OrderModel) {
        orderRepository.placeOrder(order) { success, message ->
            if (success) {
                loadOrdersByUser(order.userId)  // Reload user's orders
                _error.postValue(null)
            } else {
                _error.postValue(message)
            }
        }
    }

    // Cancel order and reload user orders on success
    fun cancelOrder(orderId: String, userId: String) {
        orderRepository.cancelOrder(orderId) { success, message ->
            if (success) {
                loadOrdersByUser(userId) // Reload user's orders
                _error.postValue(null)
            } else {
                _error.postValue(message)
            }
        }
    }

    fun clearError() {
        _error.postValue(null)
    }
}
