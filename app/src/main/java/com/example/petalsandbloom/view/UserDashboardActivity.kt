
package com.example.petalsandbloom.view


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.petalsandbloom.repository.*
import com.example.petalsandbloom.viewmodel.*

import com.example.petalsandbloom.R
import com.example.petalsandbloom.model.CartItemModel
import com.example.petalsandbloom.model.ProductModel
import com.example.petalsandbloom.model.WishlistItemModel
import com.example.petalsandbloom.repository.CartRepositoryImpl
import com.example.petalsandbloom.repository.OrderRepositoryImpl
import com.example.petalsandbloom.repository.ProductRepositoryImpl
import com.example.petalsandbloom.repository.UserRepositoryImplementation
import com.example.petalsandbloom.repository.WishlistRepositoryImpl
import com.example.petalsandbloom.viewmodel.CartViewModel
import com.example.petalsandbloom.viewmodel.CartViewModelFactory
import com.example.petalsandbloom.viewmodel.OrderViewModel
import com.example.petalsandbloom.viewmodel.OrderViewModelFactory
import com.example.petalsandbloom.viewmodel.ProductViewModel
import com.example.petalsandbloom.viewmodel.UserViewModel
import com.example.petalsandbloom.viewmodel.UserViewModelFactory
import com.example.petalsandbloom.viewmodel.WishlistViewModel
import com.example.petalsandbloom.viewmodel.WishlistViewModelFactory
import com.example.petalsandbloom.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.database.FirebaseDatabase

class UserDashboardActivity : ComponentActivity() {

    private lateinit var cartViewModel: CartViewModel
    private lateinit var wishlistViewModel: WishlistViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var orderViewModel: OrderViewModel
    private lateinit var imageUtils: ImageUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ImageUtils
        imageUtils = ImageUtils(this, this)

        // Initialize Cloudinary with your credentials
        val cloudinaryConfig = mapOf(
            "cloud_name" to "your_cloud_name",
            "api_key" to "your_api_key",
            "api_secret" to "your_api_secret",
            "secure" to true
        )
        MediaManager.init(this, cloudinaryConfig)


        cartViewModel = ViewModelProvider(this, CartViewModelFactory(CartRepositoryImpl()))[CartViewModel::class.java]
        wishlistViewModel = ViewModelProvider(this, WishlistViewModelFactory(WishlistRepositoryImpl))[WishlistViewModel::class.java]
        userViewModel = ViewModelProvider(this, UserViewModelFactory(UserRepositoryImplementation()))[UserViewModel::class.java]
        orderViewModel = ViewModelProvider(this, OrderViewModelFactory(OrderRepositoryImpl()))[OrderViewModel::class.java]

        setContent {
            UserDashboardBody(cartViewModel, wishlistViewModel, userViewModel, orderViewModel, imageUtils)
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUserId = userViewModel.getCurrentUser()?.uid
        currentUserId?.let {
            userViewModel.getUserById(it)
            orderViewModel.loadOrdersByUser(it)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDashboardBody(
    cartViewModel: CartViewModel,
    wishlistViewModel: WishlistViewModel,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    imageUtils: ImageUtils
) {
    val context = LocalContext.current
    val productRepo = remember { ProductRepositoryImpl() }
    val productViewModel = remember { ProductViewModel(productRepo) }

    val currentUser = userViewModel.getCurrentUser()
    val currentUserId = currentUser?.uid

    val user by userViewModel.users.observeAsState()
    val filteredProducts by productViewModel.filteredProducts.observeAsState(emptyList())
    val orders by orderViewModel.userOrders.observeAsState(emptyList())
    val loading by productViewModel.loading.observeAsState(true)

    var menuExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    LaunchedEffect(currentUserId) {
        currentUserId?.let {
            userViewModel.getUserById(it)
            orderViewModel.loadOrdersByUser(it)
        }
        productViewModel.getAllProducts()
    }

    LaunchedEffect(searchQuery, selectedCategory) {
        productViewModel.filterByCategoryAndSearch(selectedCategory, searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Dashboard", color = Color.Black) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White),
                actions = {
                    // Edit Profile Button
                    IconButton(onClick = {
                        context.startActivity(Intent(context, EditProfileActivity::class.java))
                    }) {
                        Icon(Icons.Default.Person, contentDescription = "Edit Profile", tint = Color.Black)
                    }
                    
                    // Logout Button
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        val intent = Intent(context, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = Color.Black)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFFFFE4E1)) {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.Black) },
                    label = { Text("Home", color = Color.Black) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { context.startActivity(Intent(context, CartActivity::class.java)) },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = Color.Black) },
                    label = { Text("Cart", color = Color.Black) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { context.startActivity(Intent(context, WishlistActivity::class.java)) },
                    icon = { Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist", tint = Color.Black) },
                    label = { Text("Wishlist", color = Color.Black) }
                )
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            UserHeader(
                user = user
            )

            Column {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search products...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )

                val categories = listOf("All", "Roses", "Tulips", "Lillies", "Sunflower", "Daisy")

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, bottom = 8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                selectedCategory = category
                                productViewModel.filterByCategoryAndSearch(category, searchQuery)
                            },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4CAF50),
                                selectedLabelColor = Color.White,
                                containerColor = Color.LightGray,
                                labelColor = Color.Black
                            ),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
            }

            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(filteredProducts) { product ->
                        product?.let {
                            ProductCard(it, cartViewModel, wishlistViewModel, imageUtils, context)
                        }
                    }
                }
            }

            if (orders.isNotEmpty()) {
                Text(
                    text = "Your Orders",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxHeight(0.4f)
                ) {
                    items(orders) { order ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Order ID: ${order.orderId}")
                                Text("Total: Rs. ${order.totalAmount}")
                                Text("Status: ${order.orderStatus}")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserHeader(
    user: com.example.petalsandbloom.model.UserModel?
) {
    Box(modifier = Modifier.padding(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = "Welcome, ${user?.firstName ?: "User"}!",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}


@Composable
fun ProductCard(
    product: ProductModel,
    cartViewModel: CartViewModel,
    wishlistViewModel: WishlistViewModel,
    imageUtils: ImageUtils,
    context: android.content.Context
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFC8E6C9)
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            AsyncImage(
                model = product.image,
                contentDescription = product.productName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.placeholder)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = product.productName ?: "Unnamed", style = MaterialTheme.typography.titleMedium)
            Text(text = "Rs. ${product.productPrice ?: 0}", style = MaterialTheme.typography.bodyLarge)

            product.category?.let {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .background(Color(0xFF4CAF50), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val cartItem = CartItemModel(
                            productId = product.productId ?: "",
                            productName = product.productName ?: "",
                            productPrice = product.productPrice ?: 0.0,
                            image = product.image ?: "",
                            quantity = 1
                        )
                        cartViewModel.addToCart(cartItem)
                        Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    )
                ) {
                    Text("Add to Cart")
                }

                Row {
                    IconButton(onClick = {
                        val wishlistItem = WishlistItemModel(
                            productName = product.productName ?: "",
                            productPrice = product.productPrice ?: 0.0,
                            image = product.image ?: ""
                        )
                        wishlistViewModel.addToWishlist(wishlistItem)
                        Toast.makeText(context, "Added to wishlist", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Wishlist", tint = Color.Red)
                    }
                    
                    IconButton(onClick = {
                        product.image?.let { imageUrl ->
                            imageUtils.saveImageToPhone(context, imageUrl) { success, message ->
                                if (success) {
                                    Toast.makeText(context, "Image saved successfully!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Failed to save image: $message", Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Save Image", tint = Color.Blue)
                    }
                }
            }
        }
    }
}
