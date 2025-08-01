
package com.example.petalsandbloom.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.petalsandbloom.model.CartItemModel
import com.example.petalsandbloom.model.WishlistItemModel
import com.example.petalsandbloom.repository.CartRepositoryImpl
import com.example.petalsandbloom.repository.WishlistRepositoryImpl
import com.example.petalsandbloom.viewmodel.CartViewModel
import com.example.petalsandbloom.viewmodel.CartViewModelFactory
import com.example.petalsandbloom.viewmodel.WishlistViewModel
import com.example.petalsandbloom.viewmodel.WishlistViewModelFactory

class WishlistActivity : ComponentActivity() {
    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var cartViewModel: CartViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Initialize WishlistViewModel
        wishlistViewModel = ViewModelProvider(
            this,
            WishlistViewModelFactory(WishlistRepositoryImpl)
        )[WishlistViewModel::class.java]

        // Initialize CartViewModel
        cartViewModel = ViewModelProvider(
            this,
            CartViewModelFactory(CartRepositoryImpl())
        )[CartViewModel::class.java]

        setContent {
            WishlistScreen(wishlistViewModel, cartViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    wishlistViewModel: WishlistViewModel,
    cartViewModel: CartViewModel
) {
    val context = LocalContext.current
    val wishlistItems by wishlistViewModel.wishlistItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Wishlist", fontSize = 20.sp, color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            if (wishlistItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your wishlist is empty.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(wishlistItems) { item ->
                        WishlistItemCard(
                            item = item,
                            onRemove = {
                                wishlistViewModel.removeFromWishlist(item)
                                Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                            },
                            onAddToCart = {
                                val cartItem = item.toCartItem()
                                cartViewModel.addToCart(cartItem)
                                Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistItemCard(
    item: WishlistItemModel,
    onRemove: () -> Unit,
    onAddToCart: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = item.productName, fontSize = 18.sp, color = Color.Black)
            Text(text = "Price: Rs. ${item.productPrice}", color = Color.DarkGray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAddToCart) {
                    Text("Add to Cart")
                }
                Button(onClick = onRemove, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                    Text("Remove")
                }
            }
        }
    }
}


fun WishlistItemModel.toCartItem(): CartItemModel {
    return CartItemModel(
        id = "", // You can generate ID later or leave blank for auto generation
        productName = this.productName,
        productPrice = this.productPrice,
        image = this.image,
        quantity = 1
    )
}
