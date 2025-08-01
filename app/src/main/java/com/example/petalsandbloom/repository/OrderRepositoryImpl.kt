package com.example.petalsandbloom.repository



import com.example.petalsandbloom.model.OrderModel
import com.google.firebase.database.*

class OrderRepositoryImpl : OrderRepository {

    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("orders")

    override fun placeOrder(order: OrderModel, callback: (Boolean, String) -> Unit) {
        val key = dbRef.push().key
        if (key != null) {
            // Firebase Database keys are immutable, so create a copy with the key
            val orderWithId = order.copy(orderId = key)
            dbRef.child(key).setValue(orderWithId)
                .addOnSuccessListener { callback(true, "Order Placed") }
                .addOnFailureListener { callback(false, "Failed: ${it.message}") }
        } else {
            callback(false, "Failed to generate order ID")
        }
    }

    override fun getOrdersByUser(userId: String, callback: (List<OrderModel>, Boolean, String) -> Unit) {
        dbRef.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders = mutableListOf<OrderModel>()
                    for (orderSnap in snapshot.children) {
                        val order = orderSnap.getValue(OrderModel::class.java)
                        order?.let { orders.add(it) }
                    }
                    callback(orders, true, "Success")
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList(), false, error.message)
                }
            })
    }

    override fun cancelOrder(orderId: String, callback: (Boolean, String) -> Unit) {
        dbRef.child(orderId).removeValue()
            .addOnSuccessListener { callback(true, "Order cancelled") }
            .addOnFailureListener { callback(false, "Failed: ${it.message}") }
    }

    override fun getAllOrders(callback: (List<OrderModel>, Boolean, String) -> Unit) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val orders = mutableListOf<OrderModel>()
                for (orderSnap in snapshot.children) {
                    val order = orderSnap.getValue(OrderModel::class.java)
                    order?.let { orders.add(it) }
                }
                callback(orders, true, "Success")
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), false, error.message)
            }
        })
    }
}
