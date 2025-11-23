package com.ropa.smartfashionecommerce.carrito

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.pedidos.PedidoConfirmado
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

class FinalizarCompra : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CartManager.initialize(this)

        setContent {
            SmartFashionEcommerceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF9F9F9)
                ) {
                    FinalizarCompraScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinalizarCompraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    val userEmail = user?.email ?: ""
    val sharedPrefs = context.getSharedPreferences("user_profile_$userEmail", android.content.Context.MODE_PRIVATE)
    val direccionesPrefs = context.getSharedPreferences("SmartFashionPrefs", Context.MODE_PRIVATE)

    val cartItems by remember { derivedStateOf { CartManager.cartItems } }
    val subtotal = remember { derivedStateOf { CartManager.getTotal() } }
    val igv = subtotal.value * 0.18
    val total = subtotal.value + igv

    // Datos personales
    var nombresCompletos by remember { mutableStateOf(sharedPrefs.getString("nombre", user?.displayName ?: "") ?: "") }
    var correo by remember { mutableStateOf(sharedPrefs.getString("email", user?.email ?: "") ?: "") }
    var telefono by remember { mutableStateOf(sharedPrefs.getString("telefono", "") ?: "") }
    var direccion by remember { mutableStateOf(sharedPrefs.getString("direccion", "") ?: "") }
    var ciudad by remember { mutableStateOf("") }
    var departamento by remember { mutableStateOf("") }
    var codigoPostal by remember { mutableStateOf("") }

    // Direcciones guardadas
    var direccionesGuardadas by remember {
        mutableStateOf(direccionesPrefs.getStringSet("direcciones_envio", emptySet())?.toList() ?: emptyList())
    }
    var showDireccionesDialog by remember { mutableStateOf(false) }

    // Método de pago
    var metodoPago by remember { mutableStateOf("Tarjeta") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var nombreTarjeta by remember { mutableStateOf("") }
    var numeroYape by remember { mutableStateOf("") }

    Scaffold(
        containerColor = Color(0xFFF9F9F9),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.Black
                                )
                            }
                            Text("Volver", color = Color.Black, fontSize = 16.sp)
                        }
                        Text(
                            "SMARTFASHION",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.padding(end = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Finalizar Compra",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Datos personales
            SectionCard(title = "Datos personales") {
                CustomTextField("Nombres Completos", nombresCompletos) { nombresCompletos = it }
                CustomTextField("Correo electrónico", correo, KeyboardType.Email) { correo = it }
                CustomTextField("Teléfono", telefono, KeyboardType.Phone) { telefono = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Dirección de envío
            SectionCard(title = "Dirección de envío") {
                if (direccionesGuardadas.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { showDireccionesDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Elegir de mis direcciones", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                CustomTextField("Dirección completa", direccion) { direccion = it }
                CustomTextField("Ciudad", ciudad) { ciudad = it }
                CustomTextField("Departamento", departamento) { departamento = it }
                CustomTextField("Código postal", codigoPostal, KeyboardType.Number) { codigoPostal = it }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Método de pago
            SectionCard(title = "Método de pago") {
                Text("Selecciona un método:", fontWeight = FontWeight.Medium, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PaymentOptionButton(Modifier.weight(1f), "Tarjeta", metodoPago == "Tarjeta") { metodoPago = "Tarjeta" }
                    PaymentOptionButton(Modifier.weight(1f), "Yape", metodoPago == "Yape") { metodoPago = "Yape" }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (metodoPago == "Tarjeta") {
                    CustomTextField("Número de tarjeta", numeroTarjeta, KeyboardType.Number) { numeroTarjeta = it }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        CustomTextField("Vencimiento", fechaVencimiento, modifier = Modifier.weight(1f)) { fechaVencimiento = it }
                        CustomTextField("CVV", cvv, KeyboardType.Number, modifier = Modifier.weight(1f)) { cvv = it }
                    }
                    CustomTextField("Nombre en la tarjeta", nombreTarjeta) { nombreTarjeta = it }
                } else {
                    CustomTextField("Número de celular Yape", numeroYape, KeyboardType.Phone) { numeroYape = it }
                    Text("Se enviará una solicitud de pago a tu Yape.", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Resumen del pedido
            SectionCard(title = "Resumen del pedido") {
                if (cartItems.isEmpty()) {
                    Text("No hay productos en el carrito", color = Color.Gray)
                } else {
                    cartItems.forEach {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${it.name} x${it.quantity}", color = Color.Black)
                            Text("S/ ${"%.2f".format(it.price * it.quantity)}", color = Color.Black)
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Color.Gray)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total a pagar", fontWeight = FontWeight.Bold, color = Color(0xFF007ACC))
                        Text("S/ ${"%.2f".format(total)}", fontWeight = FontWeight.Bold, color = Color(0xFF007ACC))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón confirmar pedido
            Button(
                onClick = {
                    val faltantes = mutableListOf<String>()
                    if (nombresCompletos.isBlank()) faltantes.add("Nombres Completos")
                    if (correo.isBlank()) faltantes.add("Correo electrónico")
                    if (telefono.isBlank()) faltantes.add("Teléfono")
                    if (direccion.isBlank()) faltantes.add("Dirección")
                    if (ciudad.isBlank()) faltantes.add("Ciudad")
                    if (departamento.isBlank()) faltantes.add("Departamento")
                    if (codigoPostal.isBlank()) faltantes.add("Código postal")

                    if (metodoPago == "Tarjeta") {
                        if (numeroTarjeta.isBlank()) faltantes.add("Número de tarjeta")
                        if (fechaVencimiento.isBlank()) faltantes.add("Fecha de vencimiento")
                        if (cvv.isBlank()) faltantes.add("CVV")
                        if (nombreTarjeta.isBlank()) faltantes.add("Nombre en la tarjeta")
                    } else if (numeroYape.isBlank()) faltantes.add("Número de Yape")

                    if (faltantes.isNotEmpty()) {
                        androidx.appcompat.app.AlertDialog.Builder(context)
                            .setTitle("Campos incompletos ⚠️")
                            .setMessage("Faltan: \n\n${faltantes.joinToString(", ")}")
                            .setPositiveButton("Aceptar", null)
                            .show()
                    } else {
                        // 🧮 Descontar stock de cada producto comprado (demo en local)
                        cartItems.forEach { item ->
                            StockManager.reduceStock(context, item.name, item.quantity)
                        }

                        PedidosManager.agregarPedido(
                            context = context,
                            total = total,
                            productos = cartItems.map { "${it.name} x${it.quantity}" },
                            direccionTexto = direccion
                        )
                        CartManager.clear()

                        val intent = Intent(context, PedidoConfirmado::class.java)
                        intent.putExtra("total", total)
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Confirmar pedido", color = Color.White, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Compra 100% segura",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // Diálogo para elegir dirección guardada
            if (showDireccionesDialog && direccionesGuardadas.isNotEmpty()) {
                AlertDialog(
                    onDismissRequest = { showDireccionesDialog = false },
                    confirmButton = {},
                    title = { Text("Mis direcciones", fontWeight = FontWeight.Bold, color = Color.Black) },
                    text = {
                        Column {
                            direccionesGuardadas.forEach { dir ->
                                TextButton(onClick = {
                                    direccion = dir
                                    showDireccionesDialog = false
                                }) {
                                    Text(dir, color = Color.Black)
                                }
                            }
                        }
                    },
                    containerColor = Color.White
                )
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
fun CustomTextField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = Color.Black) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Gray,
            cursorColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        )
    )
}

@Composable
fun PaymentOptionButton(
    modifier: Modifier = Modifier,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.Black else Color.LightGray
        ),
        modifier = modifier.height(45.dp),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, color = Color.White)
    }
}
