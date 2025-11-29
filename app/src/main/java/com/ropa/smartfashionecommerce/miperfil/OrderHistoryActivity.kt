package com.ropa.smartfashionecommerce.miperfil

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.model.UserOrderDto
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OrderHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    OrderHistoryScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var orders by remember { mutableStateOf<List<UserOrderDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        val email = Firebase.auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            isLoading = false
            return@LaunchedEffect
        }
        scope.launch(Dispatchers.IO) {
            try {
                val resp = ApiClient.apiService.getUserOrders(email)
                if (resp.isSuccessful) {
                    val body = resp.body()
                    val data = body?.data ?: emptyList()
                    orders = data
                }
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de pedidos", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                orders.isEmpty() -> {
                    Text(
                        text = "No tienes pedidos aún",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color(0xFF616161)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(orders) { order ->
                            OrderCard(order)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(order: UserOrderDto) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val intent = Intent(context, SeguimientoPedidoActivity::class.java)
                intent.putExtra("codigo_pedido", order.order_number)
                context.startActivity(intent)
            },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Pedido #${order.order_number}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFF212121)
            )
            if (!order.created_at.isNullOrEmpty()) {
                Text(
                    text = order.created_at.replace("T", " "),
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Total: S/ %.2f".format(order.total),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD32F2F)
            )
            order.envio?.let { e ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Envío: ${e.status ?: ""}",
                    fontSize = 12.sp,
                    color = Color(0xFF424242)
                )
                if (!e.direccion.isNullOrEmpty()) {
                    Text(
                        text = e.direccion,
                        fontSize = 12.sp,
                        color = Color(0xFF616161)
                    )
                }
            }
        }
    }
}
