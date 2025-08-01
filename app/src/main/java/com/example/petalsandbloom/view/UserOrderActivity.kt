package com.example.petalsandbloom.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.petalsandbloom.model.OrderModel
import com.example.petalsandbloom.repository.OrderRepositoryImpl
import com.example.petalsandbloom.ui.theme.PetalsandbloomTheme
import com.example.petalsandbloom.viewmodel.OrderViewModel
import com.example.petalsandbloom.viewmodel.OrderViewModelFactory

class UserOrderActivity : ComponentActivity() {

    private lateinit var orderViewModel: OrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Get userId passed via Intent
        val userId = intent.getStringExtra("userId") ?: ""

        val orderRepo = OrderRepositoryImpl()
        val orderFactory = OrderViewModelFactory(orderRepo)
        orderViewModel = ViewModelProvider(this, orderFactory)[OrderViewModel::class.java]

        setContent {
            PetalsandbloomTheme {
                UserOrderScreen(userId, orderViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOrderScreen(userId: String, orderViewModel: OrderViewModel) {
    val orders by orderViewModel.userOrders.observeAsState(emptyList())
    val context = LocalContext.current

    LaunchedEffect(userId) {
        orderViewModel.loadOrdersByUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (orders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No orders found.")
                }
            } else {
                LazyColumn {
                    items(orders) { order ->
                        OrderCardWithCancel(
                            order = order,
                            onCancel = {
                                orderViewModel.cancelOrder(
                                    order.userId,
                                    userId = TODO()
                                )
                                Toast.makeText(context, "Order cancelled", Toast.LENGTH_SHORT).show()
                                orderViewModel.loadOrdersByUser(userId) // refresh list
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCardWithCancel(order: OrderModel, onCancel: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order ID: ${order.orderId}", style = MaterialTheme.typography.titleMedium)
            Text("Status: ${order.orderStatus}")
            Text("Total: Rs. ${order.totalAmount}")

            Spacer(modifier = Modifier.height(8.dp))

            if (order.orderStatus != "Cancelled") {
                Button(
                    onClick = onCancel,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Cancel Order")
                }
            }
        }
    }
}
