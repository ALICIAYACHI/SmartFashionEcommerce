package com.ropa.smartfashionecommerce.carrito

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.pedidos.PedidoConfirmado
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class CardPaymentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val total = intent.getDoubleExtra("total", 0.0)
        val nombreFacturacion = intent.getStringExtra("nombreFacturacion") ?: ""
        val telefonoFacturacion = intent.getStringExtra("telefonoFacturacion") ?: ""
        val direccionFacturacion = intent.getStringExtra("direccionFacturacion") ?: ""
        val referenciasFacturacion = intent.getStringExtra("referenciasFacturacion") ?: ""
        val resumen = intent.getStringArrayListExtra("resumenPedido") ?: arrayListOf()

        // Asegurar que el carrito esté inicializado
        CartManager.initialize(this)

        setContent {
            SmartFashionEcommerceTheme {
                Surface(color = Color.White) {
                    CardPaymentScreen(
                        total = total,
                        nombreFacturacion = nombreFacturacion,
                        telefonoFacturacion = telefonoFacturacion,
                        direccionFacturacion = direccionFacturacion,
                        referenciasFacturacion = referenciasFacturacion,
                        resumenPedido = resumen,
                        onBack = { finish() },
                        onConfirm = {
                            val context = this@CardPaymentActivity
                            val cartItems = CartManager.cartItems

                            // Descontar stock de cada producto comprado
                            cartItems.forEach { item ->
                                StockManager.reduceStock(context, item.name, item.quantity)
                            }

                            // Registrar pedido
                            PedidosManager.agregarPedido(
                                context = context,
                                total = total,
                                productos = cartItems.map { "${it.name} x${it.quantity}" },
                                direccionTexto = direccionFacturacion
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
fun CardPaymentScreen(
    total: Double,
    nombreFacturacion: String,
    telefonoFacturacion: String,
    direccionFacturacion: String,
    referenciasFacturacion: String,
    resumenPedido: List<String>,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var cvvVisible by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                title = { Text("Pago", color = Color.White) },
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
            Text(
                text = "Número de Tarjeta",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = numeroTarjeta,
                onValueChange = { numeroTarjeta = it },
                placeholder = { Text("0000 0000 0000 0000") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Fecha de vencimiento", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fechaVencimiento,
                        onValueChange = { fechaVencimiento = it },
                        placeholder = { Text("MM/AAAA") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("CVV", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cvv,
                        onValueChange = { cvv = it },
                        placeholder = { Text("3-4 dígitos") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                        visualTransformation = if (cvvVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { cvvVisible = !cvvVisible }) {
                                Icon(
                                    imageVector = if (cvvVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (cvvVisible) "Ocultar CVV" else "Mostrar CVV"
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dirección de facturación
            Text("Dirección de facturación", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            if (nombreFacturacion.isNotBlank() || telefonoFacturacion.isNotBlank()) {
                Text(
                    text = listOf(nombreFacturacion, telefonoFacturacion).filter { it.isNotBlank() }.joinToString("    "),
                    fontSize = 13.sp,
                    color = Color.Black
                )
            }
            if (direccionFacturacion.isNotBlank()) {
                Text(direccionFacturacion, fontSize = 13.sp, color = Color.Black)
            }
            if (referenciasFacturacion.isNotBlank()) {
                Text(referenciasFacturacion, fontSize = 13.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Resumen del pedido (similar a FinalizarCompra)
            if (resumenPedido.isNotEmpty()) {
                Text("Resumen del pedido", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(4.dp))
                resumenPedido.forEach { linea ->
                    Text(linea, fontSize = 13.sp, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Total: S/ ${"%.2f".format(total)}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF007ACC),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // Validación básica de campos de tarjeta antes de confirmar
                    if (numeroTarjeta.isBlank() || fechaVencimiento.isBlank() || cvv.isBlank()) {
                        // En una app real podrías usar un Snackbar/AlertDialog;
                        // aquí simplemente no avanzamos si faltan datos.
                        return@Button
                    }

                    // 🔹 Punto de integración real con backend de tarjeta
                    // Aquí es donde, cuando tengas tu API, llamarías a tu servidor
                    // para procesar el pago con una pasarela (Niubiz, Culqi, Izipay, etc.).
                    // Ejemplo de flujo (no implementado todavía):
                    //
                    // 1. Parsear fecha de vencimiento "MM/AAAA" en mes y año:
                    //    val partes = fechaVencimiento.split("/")
                    //    val mes = partes.getOrNull(0)?.toIntOrNull() ?: 0
                    //    val anio = partes.getOrNull(1)?.toIntOrNull() ?: 0
                    //
                    // 2. Construir el request para tu API:
                    //    val request = CardCreatePaymentRequest(
                    //        amount = total,
                    //        cardNumber = numeroTarjeta,
                    //        cardHolder = nombreFacturacion,
                    //        expMonth = mes,
                    //        expYear = anio,
                    //        cvv = cvv,
                    //        orderId = "GENERAR_ID_PEDIDO"
                    //    )
                    //
                    // 3. Llamar a tu backend usando Retrofit (PaymentApiService / YapeApiService):
                    //    coroutineScope.launch {
                    //        val response = api.createCardPayment(request)
                    //        if (response.isSuccessful) {
                    //            val body = response.body()
                    //            when (body?.status) {
                    //                "APPROVED" -> onConfirm() // marcar como pagado
                    //                "PENDING"  -> mostrar pantalla de espera / 3DS
                    //                else        -> mostrar error de pago
                    //            }
                    //        } else {
                    //            // Mostrar error de integración
                    //        }
                    //    }
                    //
                    // Mientras no exista backend, seguimos usando la simulación actual:
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
        }
    }
}
