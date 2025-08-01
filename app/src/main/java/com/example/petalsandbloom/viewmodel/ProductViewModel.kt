package com.example.petalsandbloom.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petalsandbloom.model.ProductModel
import com.example.petalsandbloom.repository.ProductRepository


class ProductViewModel(private val repo:
                       ProductRepository) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _allProducts = MutableLiveData<List<ProductModel?>>()
    val allProducts: LiveData<List<ProductModel?>> get() = _allProducts

    private val _filteredProducts = MutableLiveData<List<ProductModel?>>()
    val filteredProducts: LiveData<List<ProductModel?>> get() = _filteredProducts

    private val _product = MutableLiveData<ProductModel?>()
    val product: LiveData<ProductModel?> get() = _product

    init {
        _filteredProducts.value = emptyList()
    }

    // Upload Image method
    fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        repo.uploadImage(context, imageUri, callback)
    }

    // Add new product
    fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        repo.addProduct(model, callback)
    }

    // Update existing product
    fun updateProduct(productId: String, data: MutableMap<String, Any?>, callback: (Boolean, String) -> Unit) {
        repo.updateProduct(productId, data, callback)
    }

    // Delete product
    fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit) {
        repo.deleteProduct(productId, callback)
    }

    // Get product by ID
    fun getProductById(productId: String) {
        repo.getProductById(productId) { data, success, _ ->
            if (success) {
                _product.postValue(data)
            } else {
                _product.postValue(null)
            }
        }
    }

    // Fetch all products from repo
    fun getAllProducts() {
        _loading.postValue(true)
        repo.getAllProducts { data, success, _ ->
            _loading.postValue(false)
            if (success) {
                _allProducts.postValue(data)
                _filteredProducts.postValue(data)  // Initialize filtered list
            } else {
                _allProducts.postValue(emptyList())
                _filteredProducts.postValue(emptyList())
            }
        }
    }

    // Filter by query only
    fun filterProducts(query: String) {
        val all = _allProducts.value ?: emptyList()
        if (query.isBlank()) {
            _filteredProducts.postValue(all)
        } else {
            val filtered = all.filter {
                it?.productName?.contains(query, ignoreCase = true) == true
            }
            _filteredProducts.postValue(filtered)
        }
    }

    // ✅ Filter by both category and search query
    fun filterByCategoryAndSearch(category: String, query: String) {
        val all = _allProducts.value ?: emptyList()
        val filtered = all.filter {
            (category == "All" || it?.category.equals(category, ignoreCase = true)) &&
                    it?.productName?.contains(query, ignoreCase = true) == true
        }
        _filteredProducts.postValue(filtered)
    }
}
