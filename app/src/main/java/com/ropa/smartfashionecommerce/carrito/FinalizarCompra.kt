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
    var tipoDocumento by remember { mutableStateOf(sharedPrefs.getString("tipoDocumento", "DNI") ?: "DNI") }
    var numeroDocumento by remember { mutableStateOf(sharedPrefs.getString("numeroDocumento", "") ?: "") }

    // Dirección y referencias guardadas
    var direccion by remember { mutableStateOf(sharedPrefs.getString("direccionEnvio", "") ?: "") }
    var departamento by remember { mutableStateOf(sharedPrefs.getString("departamentoEnvio", "") ?: "") }
    var provincia by remember { mutableStateOf(sharedPrefs.getString("provinciaEnvio", "") ?: "") }
    var distrito by remember { mutableStateOf(sharedPrefs.getString("distritoEnvio", "") ?: "") }
    var codigoPostal by remember { mutableStateOf(sharedPrefs.getString("codigoPostalEnvio", "") ?: "") }
    var referencias by remember { mutableStateOf(sharedPrefs.getString("referenciasEnvio", "") ?: "") }

    // Copia base para detectar cambios y controlar el texto del botón Guardar/Actualizar
    var baseNombresCompletos by remember { mutableStateOf(nombresCompletos) }
    var baseCorreo by remember { mutableStateOf(correo) }
    var baseTelefono by remember { mutableStateOf(telefono) }
    var baseTipoDocumento by remember { mutableStateOf(tipoDocumento) }
    var baseNumeroDocumento by remember { mutableStateOf(numeroDocumento) }
    var baseDepartamento by remember { mutableStateOf(departamento) }
    var baseProvincia by remember { mutableStateOf(provincia) }
    var baseDistrito by remember { mutableStateOf(distrito) }
    var baseCodigoPostal by remember { mutableStateOf(codigoPostal) }
    var baseDireccion by remember { mutableStateOf(direccion) }
    var baseReferencias by remember { mutableStateOf(referencias) }

    var datosGuardados by remember {
        mutableStateOf(
            listOf(
                baseNombresCompletos,
                baseCorreo,
                baseTelefono,
                baseNumeroDocumento,
                baseDepartamento,
                baseProvincia,
                baseDistrito,
                baseCodigoPostal,
                baseDireccion
            ).any { it.isNotBlank() }
        )
    }

    val hayCambios = remember(
        nombresCompletos,
        correo,
        telefono,
        tipoDocumento,
        numeroDocumento,
        departamento,
        provincia,
        distrito,
        codigoPostal,
        direccion,
        referencias,
        baseNombresCompletos,
        baseCorreo,
        baseTelefono,
        baseTipoDocumento,
        baseNumeroDocumento,
        baseDepartamento,
        baseProvincia,
        baseDistrito,
        baseCodigoPostal,
        baseDireccion,
        baseReferencias
    ) {
        nombresCompletos != baseNombresCompletos ||
                correo != baseCorreo ||
                telefono != baseTelefono ||
                tipoDocumento != baseTipoDocumento ||
                numeroDocumento != baseNumeroDocumento ||
                departamento != baseDepartamento ||
                provincia != baseProvincia ||
                distrito != baseDistrito ||
                codigoPostal != baseCodigoPostal ||
                direccion != baseDireccion ||
                referencias != baseReferencias
    }

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

                Spacer(modifier = Modifier.height(8.dp))
                Text("Tipo de documento", fontWeight = FontWeight.SemiBold, color = Color.Black, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                        RadioButton(
                            selected = tipoDocumento == "DNI",
                            onClick = { tipoDocumento = "DNI" }
                        )
                        Text("DNI", color = Color.Black)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipoDocumento == "RUC",
                            onClick = { tipoDocumento = "RUC" }
                        )
                        Text("RUC", color = Color.Black)
                    }
                }

                val labelDocumento = if (tipoDocumento == "RUC") "RUC (11 dígitos)" else "DNI (8 dígitos)"
                CustomTextField(labelDocumento, numeroDocumento, KeyboardType.Number) { numeroDocumento = it }

                Spacer(modifier = Modifier.height(8.dp))
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
                // Orden: Departamento, Provincia, Distrito, Código postal, Dirección, Referencias
                CustomTextField("Departamento", departamento) { departamento = it }
                CustomTextField("Provincia", provincia) { provincia = it }
                CustomTextField("Distrito", distrito) { distrito = it }
                CustomTextField("Código postal", codigoPostal, KeyboardType.Number) { codigoPostal = it }
                CustomTextField("Dirección", direccion) { direccion = it }
                CustomTextField("Referencias", referencias) { referencias = it }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón para guardar / actualizar datos para futuras compras
            val textoBotonDatos = when {
                !datosGuardados -> "Guardar datos para próximas compras"
                hayCambios -> "Actualizar datos"
                else -> "Datos guardados"
            }

            Button(
                onClick = {
                    // Guardar/actualizar en SharedPreferences
                    sharedPrefs.edit().apply {
                        putString("nombre", nombresCompletos)
                        putString("email", correo)
                        putString("telefono", telefono)
                        putString("tipoDocumento", tipoDocumento)
                        putString("numeroDocumento", numeroDocumento)
                        putString("departamentoEnvio", departamento)
                        putString("provinciaEnvio", provincia)
                        putString("distritoEnvio", distrito)
                        putString("codigoPostalEnvio", codigoPostal)
                        putString("direccionEnvio", direccion)
                        putString("referenciasEnvio", referencias)
                    }.apply()

                    // Actualizar la copia base para que deje de marcar cambios
                    baseNombresCompletos = nombresCompletos
                    baseCorreo = correo
                    baseTelefono = telefono
                    baseTipoDocumento = tipoDocumento
                    baseNumeroDocumento = numeroDocumento
                    baseDepartamento = departamento
                    baseProvincia = provincia
                    baseDistrito = distrito
                    baseCodigoPostal = codigoPostal
                    baseDireccion = direccion
                    baseReferencias = referencias

                    datosGuardados = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(textoBotonDatos, color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección: Método de pago
            SectionCard(title = "Forma de pago") {
                // Opción Yape (primera)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = metodoPago == "Yape",
                        onClick = { metodoPago = "Yape" }
                    )
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text("Yape", fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(
                            text = "Yapea utilizando tu código de aprobación desde tu app Yape.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Opción Tarjeta de Crédito/Débito (segunda)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = metodoPago == "Tarjeta",
                        onClick = { metodoPago = "Tarjeta" }
                    )
                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text("Tarjeta de Crédito/Débito", fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(
                            text = "Paga con tu tarjeta de crédito o débito.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Los datos de tarjeta se ingresarán en la siguiente pantalla (CardPaymentActivity)
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

            // Botón inferior según método de pago
            if (metodoPago == "Tarjeta") {
                Button(
                    onClick = {
                        val faltantes = mutableListOf<String>()
                        if (nombresCompletos.isBlank()) faltantes.add("Nombres Completos")
                        if (correo.isBlank()) faltantes.add("Correo electrónico")
                        if (telefono.isBlank()) faltantes.add("Teléfono")
                        if (numeroDocumento.isBlank()) faltantes.add(if (tipoDocumento == "RUC") "RUC" else "DNI")
                        if (direccion.isBlank()) faltantes.add("Dirección")
                        if (departamento.isBlank()) faltantes.add("Departamento")
                        if (provincia.isBlank()) faltantes.add("Provincia")
                        if (distrito.isBlank()) faltantes.add("Distrito")
                        if (codigoPostal.isBlank()) faltantes.add("Código postal")

                        if (faltantes.isNotEmpty()) {
                            androidx.appcompat.app.AlertDialog.Builder(context)
                                .setTitle("Campos incompletos ⚠️")
                                .setMessage("Faltan: \n\n${faltantes.joinToString(", ")}")
                                .setPositiveButton("Aceptar", null)
                                .show()
                        } else {
                            // 👉 Ir a pantalla de pago con tarjeta, enviando datos de facturación y resumen
                            val resumenPedido = ArrayList(cartItems.map { "${it.name} x${it.quantity} - S/ ${"%.2f".format(it.price * it.quantity)}" })

                            val intent = Intent(context, CardPaymentActivity::class.java).apply {
                                putExtra("total", total)
                                putExtra("nombreFacturacion", nombresCompletos)
                                putExtra("telefonoFacturacion", telefono)
                                putExtra("direccionFacturacion", direccion)
                                putExtra("referenciasFacturacion", referencias)
                                putStringArrayListExtra("resumenPedido", resumenPedido)
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Proceder al pago", color = Color.White, fontSize = 16.sp)
                }
            } else if (metodoPago == "Yape") {
                Button(
                    onClick = {
                        val faltantes = mutableListOf<String>()
                        if (nombresCompletos.isBlank()) faltantes.add("Nombres Completos")
                        if (correo.isBlank()) faltantes.add("Correo electrónico")
                        if (telefono.isBlank()) faltantes.add("Teléfono")
                        if (numeroDocumento.isBlank()) faltantes.add(if (tipoDocumento == "RUC") "RUC" else "DNI")
                        if (direccion.isBlank()) faltantes.add("Dirección")
                        if (departamento.isBlank()) faltantes.add("Departamento")
                        if (provincia.isBlank()) faltantes.add("Provincia")
                        if (distrito.isBlank()) faltantes.add("Distrito")
                        if (codigoPostal.isBlank()) faltantes.add("Código postal")

                        if (faltantes.isNotEmpty()) {
                            androidx.appcompat.app.AlertDialog.Builder(context)
                                .setTitle("Campos incompletos ⚠️")
                                .setMessage("Faltan: \n\n${faltantes.joinToString(", ")}")
                                .setPositiveButton("Aceptar", null)
                                .show()
                        } else {
                            // 👉 Ir a pantalla tipo Yape (el número se ingresará allí)
                            val intent = Intent(context, YapePaymentActivity::class.java).apply {
                                putExtra("total", total)
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pagar ahora", color = Color.White, fontSize = 16.sp)
                }
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
