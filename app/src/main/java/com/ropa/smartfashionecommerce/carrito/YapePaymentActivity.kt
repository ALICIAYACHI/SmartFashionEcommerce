package com.ropa.smartfashionecommerce.carrito

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.pedidos.PedidoConfirmado
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import androidx.compose.ui.res.painterResource
import com.ropa.smartfashionecommerce.R

class YapePaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val total = intent.getDoubleExtra("total", 0.0)
        val numeroYapeInicial = intent.getStringExtra("numeroYape") ?: ""

        // Asegurar que el carrito esté inicializado
        CartManager.initialize(this)

        setContent {
            SmartFashionEcommerceTheme {
                Surface(color = Color.White) {
                    YapePaymentScreen(
                        total = total,
                        numeroYapeInicial = numeroYapeInicial,
                        onBack = { finish() },
                        onConfirm = {
                            val context = this@YapePaymentActivity
                            val cartItems = CartManager.cartItems

                            // Descontar stock de cada producto comprado
                            cartItems.forEach { item ->
                                StockManager.reduceStock(context, item.name, item.quantity)
                            }

                            // Registrar pedido (usamos la dirección de envío que ya se guardó en el pedido desde FinalizarCompra)
                            PedidosManager.agregarPedido(
                                context = context,
                                total = total,
                                productos = cartItems.map { "${it.name} x${it.quantity}" },
                                direccionTexto = "Pago con Yape"
                            )

                            // Limpiar carrito y mostrar pantalla de confirmación
                            CartManager.clear()

                            val intentConfirm = Intent(context, PedidoConfirmado::class.java).apply {
                                putExtra("total", total)
                            }
                            startActivity(intentConfirm)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YapePaymentScreen(
    total: Double,
    numeroYapeInicial: String,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    var numeroYape by remember { mutableStateOf(numeroYapeInicial) }
    var codigoAprobacion by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                title = { Text("Pago con Yape", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start
        ) {
            // Encabezado SMARTFASHION + logo Yape centrado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SMARTFASHION",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.width(8.dp))

                Image(
                    painter = painterResource(id = R.drawable.yape),
                    contentDescription = "Yape",
                    modifier = Modifier.height(40.dp)
                )
            }

            Text(
                text = "Monto: S/ ${"%.2f".format(total)}",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color(0xFF007ACC),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Pagar con Yape", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Obtén el código de aprobación en la app de Yape para terminar tu compra.",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                label = "Ingresa tu celular Yape",
                value = numeroYape,
                keyboardType = KeyboardType.Phone,
                onValueChange = { numeroYape = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            CustomTextField(
                label = "Pega tu código de aprobación",
                value = codigoAprobacion,
                onValueChange = { codigoAprobacion = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Esta transacción es solo de prueba en la app. Aquí podrías mostrar un contador de tiempo como en Yape.",
                color = Color(0xFF1976D2),
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Instrucciones para realizar el pago",
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1. Ingresa tu número de celular asociado a tu cuenta de Yape.\n" +
                        "2. Abre la app Yape y obtén tu código de aprobación.\n" +
                        "3. Pega el código de aprobación en esta pantalla.\n" +
                        "4. Finalmente, toca en 'Confirmar pago' para terminar.",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Validación básica: se requiere celular y código de aprobación
                    if (numeroYape.isBlank() || codigoAprobacion.isBlank()) {
                        return@Button
                    }
                    onConfirm()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar pago", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Esta pantalla es una simulación del flujo de pago con Yape.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
