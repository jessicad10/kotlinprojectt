package com.example.petalsandbloom.repository



import com.example.petalsandbloom.model.AddressModel
import com.google.firebase.firestore.FirebaseFirestore

class AddressRepositoryImpl : AddressRepository {
    private val db = FirebaseFirestore.getInstance()
    private val collection = db.collection("addresses")

    override fun addAddress(address: AddressModel, callback: (Boolean, String) -> Unit) {
        collection.document(address.id).set(address)
            .addOnSuccessListener { callback(true, "Address saved") }
            .addOnFailureListener { callback(false, "Error saving address") }
    }

    override fun getAddresses(userId: String, callback: (List<AddressModel>) -> Unit) {
        collection.whereEqualTo("userId", userId).get()
            .addOnSuccessListener { result ->
                val addresses = result.map { it.toObject(AddressModel::class.java) }
                callback(addresses)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    override fun updateAddress(address: AddressModel, callback: (Boolean, String) -> Unit) {
        collection.document(address.id).set(address)
            .addOnSuccessListener { callback(true, "Address updated") }
            .addOnFailureListener { callback(false, "Error updating address") }
    }

    override fun deleteAddress(addressId: String, callback: (Boolean, String) -> Unit) {
        collection.document(addressId).delete()
            .addOnSuccessListener { callback(true, "Address deleted") }
            .addOnFailureListener { callback(false, "Error deleting address") }
    }
}
