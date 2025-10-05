package com.ropa.smartfashionecommerce.carrito

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.HomeActivity
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class Carrito : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFashionEcommerceTheme {
                ShoppingCartScreen(this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingCartScreen(activity: ComponentActivity? = null) {
    val context = LocalContext.current
    var cartItems by remember {
        mutableStateOf(
            mutableListOf(
                CartItem("Blusa Elegante Negra", "M", "Negro", 2, 89.90, R.drawable.modelo_ropa),
                CartItem("Vestido Dorado Noche", "S", "Dorado", 1, 159.90, R.drawable.fondo2)
            )
        )
    }

    // Calculamos totales
    val subtotal = cartItems.sumOf { it.quantity * it.price }
    val igv = subtotal * 0.18
    val total = subtotal + igv

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SmartFashion",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        // Volver al HomeActivity
                        val intent = Intent(context, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        context.startActivity(intent)
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE3E2E2))
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Carrito de compras",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(cartItems, key = { it.name }) { item ->
                        CartItemCard(
                            item = item,
                            onIncrease = {
                                item.quantity++
                                cartItems = cartItems.toMutableList()
                            },
                            onDecrease = {
                                if (item.quantity > 1) {
                                    item.quantity--
                                    cartItems = cartItems.toMutableList()
                                }
                            },
                            onDelete = {
                                cartItems.remove(item)
                                cartItems = cartItems.toMutableList()
                            }
                        )
                    }
                }

                OrderSummary(
                    productCount = cartItems.size,
                    subtotal = subtotal,
                    igv = igv,
                    total = total,
                    onFinish = {
                        if (activity != null) {
                            AlertDialog.Builder(activity)
                                .setTitle("Compra realizada")
                                .setMessage("Tu pedido ha sido procesado exitosamente 🎉")
                                .setPositiveButton("Aceptar") { dialog, _ ->
                                    dialog.dismiss()
                                }
                                .show()
                        }
                        cartItems = mutableListOf()
                    }
                )
            }
        }
    )
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Talla: ${item.size} | Color: ${item.color}", fontSize = 13.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botón disminuir
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .clickable { onDecrease() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("-", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        Text(
                            "${item.quantity}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .widthIn(min = 24.dp)
                                .padding(horizontal = 8.dp),
                            textAlign = TextAlign.Center
                        )

                        // Botón aumentar
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black)
                                .clickable { onIncrease() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("+", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }

                    // Precio y eliminar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "S/ ${String.format("%.2f", item.quantity * item.price)}",
                                color = Color(0xFF0099CC),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "S/ ${String.format("%.2f", item.price)}/u",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        IconButton(onClick = { onDelete() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummary(
    productCount: Int,
    subtotal: Double,
    igv: Double,
    total: Double,
    onFinish: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Resumen del pedido", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Subtotal ($productCount productos)")
                Text("S/ ${String.format("%.2f", subtotal)}")
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("IGV (18%)")
                Text("S/ ${String.format("%.2f", igv)}")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", fontWeight = FontWeight.Bold)
                Text(
                    "S/ ${String.format("%.2f", total)}",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0099CC),
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = { onFinish() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Finalizar compra", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }
}

data class CartItem(
    val name: String,
    val size: String,
    val color: String,
    var quantity: Int,
    val price: Double,
    val imageRes: Int
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewShoppingCartScreen() {
    SmartFashionEcommerceTheme {
        ShoppingCartScreen()
    }
}
