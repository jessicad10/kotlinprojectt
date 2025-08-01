
package com.example.petalsandbloom.view

//import OrderViewModel
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.petalsandbloom.model.OrderModel
import com.example.petalsandbloom.repository.OrderRepositoryImpl
import com.example.petalsandbloom.viewmodel.OrderViewModelFactory
import com.example.petalsandbloom.ui.theme.PetalsandbloomTheme
import com.example.petalsandbloom.viewmodel.OrderViewModel

class OrderActivity : ComponentActivity() {

    private lateinit var orderViewModel: OrderViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderRepo = OrderRepositoryImpl()
        val orderFactory = OrderViewModelFactory(orderRepo)
        orderViewModel = ViewModelProvider(this, orderFactory)[OrderViewModel::class.java]

        orderViewModel.loadAllOrders()

        setContent {
            PetalsandbloomTheme {
                OrderScreen(orderViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(orderViewModel: OrderViewModel) {
    val orders by orderViewModel.allOrders.observeAsState(emptyList())
    val error by orderViewModel.error.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Orders", fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (!error.isNullOrEmpty()) {
                Text(text = error ?: "", color = MaterialTheme.colorScheme.error)
            }

            if (orders.isEmpty()) {
                Text("No orders found.", style = MaterialTheme.typography.bodyLarge)
            } else {
                LazyColumn {
                    items(orders) { order ->
                        OrderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(order: OrderModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order ID: ${order.orderId}", style = MaterialTheme.typography.titleMedium)
            Text("Status: ${order.orderStatus}")
            Text("Total: Rs. ${order.totalAmount}")
        }
    }
}
