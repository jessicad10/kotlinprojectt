package com.example.petalsandbloom.repository



import com.example.petalsandbloom.model.CartItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartRepositoryImpl : CartRepository {

    private val auth = FirebaseAuth.getInstance()


    private val db = FirebaseDatabase.getInstance()
    private val userId: String
        get() = auth.currentUser?.uid ?: "guest_user"
    private val cartRef: DatabaseReference
        get() = db.reference.child("cart").child(userId)

    override fun addToCart(item: CartItemModel, callback: (Boolean, String) -> Unit) {
        val cartItemId = cartRef.push().key
        if (cartItemId == null) {
            callback(false, "Failed to generate cart item ID.")
            return
        }
        item.id = cartItemId
        cartRef.child(cartItemId).setValue(item).addOnCompleteListener {
            callback(it.isSuccessful, if (it.isSuccessful) "Item added to cart" else it.exception?.message ?: "Unknown error")
        }
    }

    override fun getCartItems(callback: (List<CartItemModel>, Boolean, String) -> Unit) {
        cartRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<CartItemModel>()
                for (itemSnapshot in snapshot.children) {
                    val item = itemSnapshot.getValue(CartItemModel::class.java)
                    if (item != null) {
                        cartItems.add(item)
                    }
                }
                callback(cartItems, true, "Cart items fetched")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), false, error.message)
            }
        })
    }

    override fun updateCartItemQuantity(cartItemId: String, quantity: Int, callback: (Boolean, String) -> Unit) {
        cartRef.child(cartItemId).child("quantity").setValue(quantity).addOnCompleteListener {
            callback(it.isSuccessful, if (it.isSuccessful) "Quantity updated" else it.exception?.message ?: "Unknown error")
        }
    }

    override fun removeCartItem(cartItemId: String, callback: (Boolean, String) -> Unit) {
        cartRef.child(cartItemId).removeValue().addOnCompleteListener {
            callback(it.isSuccessful, if (it.isSuccessful) "Item removed from cart" else it.exception?.message ?: "Unknown error")
        }
    }
}