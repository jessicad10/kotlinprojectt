package com.example.petalsandbloom.repository



import com.example.petalsandbloom.model.WishlistItemModel
import kotlinx.coroutines.flow.Flow

interface WishlistRepository {
    suspend fun addToWishlist(item: WishlistItemModel)
    fun getWishlistItems(): Flow<List<WishlistItemModel>>
    suspend fun removeFromWishlist(item: WishlistItemModel)
}
