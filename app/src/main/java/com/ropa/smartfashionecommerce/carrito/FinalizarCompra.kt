package com.ropa.smartfashionecommerce.carrito

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
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
import com.ropa.smartfashionecommerce.network.PaymentManager
import com.ropa.smartfashionecommerce.network.PayerInfo
import com.ropa.smartfashionecommerce.network.PaymentResult
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.network.CheckoutItemPayload
import com.ropa.smartfashionecommerce.network.CheckoutConfirmRequest
import com.ropa.smartfashionecommerce.network.AddressData
import com.ropa.smartfashionecommerce.network.AddressDto
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.ropa.smartfashionecommerce.chat.ChatFAB

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
    val direccionesPrefs = context.getSharedPreferences("direcciones_envio_$userEmail", Context.MODE_PRIVATE)

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

    // Direcciones guardadas desde el backend
    var direccionesGuardadas by remember { mutableStateOf<List<com.ropa.smartfashionecommerce.network.AddressDto>>(emptyList()) }
    var selectedAddressId by remember { mutableStateOf<Int?>(null) }
    var showDireccionesDialog by remember { mutableStateOf(false) }
    var loadingAddresses by remember { mutableStateOf(true) }
    var addressLoadError by remember { mutableStateOf<String?>(null) }

    // Cargar direcciones del backend al iniciar
    LaunchedEffect(userEmail) {
        if (userEmail.isNotBlank()) {
            loadingAddresses = true
            try {
                val api = ApiClient.apiService
                val res = api.getUserAddresses(email = userEmail)
                if (res.isSuccessful) {
                    val body = res.body()
                    direccionesGuardadas = body?.data ?: emptyList()
                    android.util.Log.d("FinalizarCompra", "Direcciones cargadas: ${direccionesGuardadas.size}")
                    // Seleccionar la primera o la que está marcada como default
                    selectedAddressId = direccionesGuardadas.firstOrNull { it.is_default }?.id 
                        ?: direccionesGuardadas.firstOrNull()?.id
                } else {
                    val errBody = try { res.errorBody()?.string() } catch (_: Exception) { null }
                    addressLoadError = "Error: ${res.code()} - $errBody"
                    android.util.Log.e("FinalizarCompra", "Error al cargar direcciones: ${res.code()}")
                }
            } catch (e: Exception) {
                addressLoadError = "Error de conexión: ${e.message}"
                android.util.Log.e("FinalizarCompra", "Excepción al cargar direcciones", e)
            } finally {
                loadingAddresses = false
            }
        } else {
            loadingAddresses = false
        }
    }

    // Método de pago seleccionado
    var metodoPago by remember { mutableStateOf("Mercado Pago") }
    var numeroTarjeta by remember { mutableStateOf("") }
    var fechaVencimiento by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var numeroYape by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            ChatFAB(modifier = Modifier.padding(bottom = 16.dp, end = 16.dp))
        },
        floatingActionButtonPosition = FabPosition.End,
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
                if (loadingAddresses) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (addressLoadError != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Text(
                            text = "Error al cargar direcciones: ${addressLoadError}",
                            color = Color(0xFFD32F2F),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else if (direccionesGuardadas.isNotEmpty()) {
                    Text("Mis direcciones guardadas:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Column {
                        direccionesGuardadas.forEach { addr ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedAddressId = addr.id }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedAddressId == addr.id,
                                    onClick = { selectedAddressId = addr.id }
                                )
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(
                                        "${addr.address}, ${addr.district}, ${addr.province}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                    if (addr.references?.isNotBlank() == true) {
                                        Text(
                                            "Ref: ${addr.references}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Text("No tienes direcciones guardadas. Ingresa una dirección aquí para esta compra.", fontSize = 13.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                // Mostrar siempre los campos para editar o crear nueva dirección
                Text("Dirección para esta compra:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
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

            // Sección: Método de pago (selector entre Mercado Pago y Stripe)
            SectionCard(title = "Forma de pago") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = metodoPago == "Mercado Pago",
                            onClick = { metodoPago = "Mercado Pago" }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Mercado Pago", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "✅ Acepta Yape, tarjetas, banca móvil y más",
                                fontSize = 13.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Pago 100% seguro y protegido",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = metodoPago == "Stripe",
                            onClick = { metodoPago = "Stripe" }
                        )
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Stripe", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💳 Pago con tarjeta de crédito o débito",
                                fontSize = 13.sp,
                                color = Color(0xFF1D4ED8),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Checkout seguro de Stripe (igual que en la web)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Info adicional para Mercado Pago
                    if (metodoPago == "Mercado Pago") {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "💳 Métodos disponibles:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("• Yape", fontSize = 12.sp, color = Color.Black)
                                Text("• Tarjetas de crédito/débito", fontSize = 12.sp, color = Color.Black)
                                Text("• Banca móvil", fontSize = 12.sp, color = Color.Black)
                                Text("• Efectivo (PagoEfectivo, Tambo+)", fontSize = 12.sp, color = Color.Black)
                            }
                        }
                    }
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

            // Estado de carga y mensaje de error
            var isProcessing by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            // Mostrar error si existe
            errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = error,
                        color = Color(0xFFD32F2F),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Botón de pago con Mercado Pago (solo visible si está seleccionado)
            if (metodoPago == "Mercado Pago") {
                Button(
                    onClick = {
                    val faltantes = mutableListOf<String>()
                    if (nombresCompletos.isBlank()) faltantes.add("Nombres Completos")
                    if (correo.isBlank()) faltantes.add("Correo electrónico")
                    if (telefono.isBlank()) faltantes.add("Teléfono")
                    if (numeroDocumento.isBlank()) faltantes.add(if (tipoDocumento == "RUC") "RUC" else "DNI")
                    // Si no hay dirección seleccionada, requieren campos de dirección
                    if (selectedAddressId == null) {
                        if (direccion.isBlank()) faltantes.add("Dirección")
                        if (departamento.isBlank()) faltantes.add("Departamento")
                        if (provincia.isBlank()) faltantes.add("Provincia")
                        if (distrito.isBlank()) faltantes.add("Distrito")
                        if (codigoPostal.isBlank()) faltantes.add("Código postal")
                    }

                    if (faltantes.isNotEmpty()) {
                        androidx.appcompat.app.AlertDialog.Builder(context)
                            .setTitle("Campos incompletos ⚠️")
                            .setMessage("Faltan: \n\n${faltantes.joinToString(", ")}")
                            .setPositiveButton("Aceptar", null)
                            .show()
                    } else {
                        // Procesar pago con Mercado Pago
                        isProcessing = true
                        errorMessage = null
                        
                        scope.launch {
                            // Separar nombre y apellido
                            val nombrePartes = nombresCompletos.split(" ", limit = 2)
                            val firstName = nombrePartes.getOrNull(0) ?: nombresCompletos
                            val lastName = nombrePartes.getOrNull(1) ?: ""
                            
                            // Construir información del pagador
                            val payerInfo = PayerInfo(
                                firstName = firstName,
                                lastName = lastName,
                                email = correo,
                                phone = telefono,
                                documentType = tipoDocumento,
                                documentNumber = numeroDocumento
                            )
                            
                            // Generar ID de referencia único
                            val externalReference = "SMART-${System.currentTimeMillis()}"
                            
                            // Crear preferencia de pago
                            when (val result = PaymentManager.createMercadoPagoPayment(
                                items = cartItems,
                                payerInfo = payerInfo,
                                externalReference = externalReference
                            )) {
                                is PaymentResult.Success -> {
                                    // Guardar referencia del pedido y total temporalmente
                                    context.getSharedPreferences("pedidos", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("ultimo_pedido", externalReference)
                                        .apply()
                                    
                                    context.getSharedPreferences("payment_temp", Context.MODE_PRIVATE)
                                        .edit()
                                        // Para Stripe usamos el subtotal (mismo monto que se muestra en Stripe)
                                        .putFloat("total", subtotal.value.toFloat())
                                        .apply()
                                    
                                    // Abrir navegador con el link de pago
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(result.paymentUrl))
                                    context.startActivity(intent)
                                    
                                    isProcessing = false
                                }
                                is PaymentResult.Error -> {
                                    errorMessage = result.message
                                    isProcessing = false
                                }
                            }
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (metodoPago == "Mercado Pago") Color(0xFF009EE3) else Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Pagar con Mercado Pago",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (metodoPago == "Stripe") {
                Spacer(modifier = Modifier.height(12.dp))

                // Segundo botón: pago con tarjeta (Stripe Checkout como en el web)
                Button(
                    onClick = {
                    val faltantes = mutableListOf<String>()
                    if (nombresCompletos.isBlank()) faltantes.add("Nombres Completos")
                    if (correo.isBlank()) faltantes.add("Correo electrónico")
                    if (telefono.isBlank()) faltantes.add("Teléfono")
                    if (numeroDocumento.isBlank()) faltantes.add(if (tipoDocumento == "RUC") "RUC" else "DNI")
                    // Si no hay dirección seleccionada, requieren campos de dirección
                    if (selectedAddressId == null) {
                        if (direccion.isBlank()) faltantes.add("Dirección")
                        if (departamento.isBlank()) faltantes.add("Departamento")
                        if (provincia.isBlank()) faltantes.add("Provincia")
                        if (distrito.isBlank()) faltantes.add("Distrito")
                        if (codigoPostal.isBlank()) faltantes.add("Código postal")
                    }

                    if (faltantes.isNotEmpty()) {
                        androidx.appcompat.app.AlertDialog.Builder(context)
                            .setTitle("Campos incompletos ⚠️")
                            .setMessage("Faltan: \n\n${faltantes.joinToString(", ")}")
                            .setPositiveButton("Aceptar", null)
                            .show()
                    } else if (cartItems.isEmpty()) {
                        androidx.appcompat.app.AlertDialog.Builder(context)
                            .setTitle("Carrito vacío")
                            .setMessage("No hay productos en el carrito")
                            .setPositiveButton("Aceptar", null)
                            .show()
                    } else {
                        isProcessing = true
                        errorMessage = null

                        scope.launch {
                            try {
                                val api = ApiClient.apiService

                                // Guardar un resumen de los productos y dirección para el historial de pedidos
                                val productosResumen = cartItems.map { item ->
            "${item.name} x${item.quantity}"
        }
        val imagenesResumen = cartItems.map { it.imageUrl ?: "" }
        val productIdsResumen = cartItems.map { it.productId.toString() }
        val direccionTexto = direccion
        val tempPrefs = context.getSharedPreferences("payment_temp", Context.MODE_PRIVATE)
        tempPrefs.edit()
            .putStringSet("productos_resumen", productosResumen.toSet())
            .putStringSet("imagenes_resumen", imagenesResumen.toSet())
            .putStringSet("product_ids_resumen", productIdsResumen.toSet())
                                    .putString("direccion_texto", direccionTexto)
                                    .putBoolean("order_saved", false)
                                    .apply()

                                val itemsPayload = cartItems.map { it ->
                                    CheckoutItemPayload(
                                        product_id = it.productId,
                                        size_id = it.sizeId,
                                        color_id = it.colorId,
                                        qty = it.quantity
                                    )
                                }

                                // Usar address_id si fue seleccionada, sino enviar datos de dirección
                                val basePayload = if (selectedAddressId != null) {
                                    CheckoutConfirmRequest(
                                        userEmail = correo,
                                        address_id = selectedAddressId,
                                        items = itemsPayload,
                                        pre_order = null,
                                        platform = "android"
                                    )
                                } else {
                                    val addressData = AddressData(
                                        address = direccion,
                                        department = departamento,
                                        province = provincia,
                                        district = distrito,
                                        postal_code = codigoPostal,
                                        references = referencias
                                    )
                                    CheckoutConfirmRequest(
                                        userEmail = correo,
                                        address_id = -1, // -1 = usar datos de address
                                        items = itemsPayload,
                                        pre_order = null,
                                        platform = "android",
                                        address = addressData,
                                        phone = telefono,
                                        document_type = tipoDocumento,
                                        document_number = numeroDocumento
                                    )
                                }

                                // 1) Confirmar checkout para generar order_number y reservar stock
                                val confirmRes = api.checkoutConfirm(basePayload)
                                if (!confirmRes.isSuccessful) {
                                    val errBody = try { confirmRes.errorBody()?.string() } catch (_: Exception) { null }
                                    val body = confirmRes.body()
                                    errorMessage = body?.message
                                        ?: errBody
                                        ?: "Error al confirmar el pedido (${confirmRes.code()})"
                                    return@launch
                                }
                                val confirmBody = confirmRes.body()
                                val orderNum = confirmBody?.order_number ?: "LOCAL-${System.currentTimeMillis()}"

                                // 2) Crear sesión de Stripe pasando el pre_order
                                val stripePayload = basePayload.copy(pre_order = orderNum)
                                val sessionRes = api.createStripeSession(stripePayload)
                                val sessionBody = sessionRes.body()

                                val url = sessionBody?.url
                                if (sessionRes.isSuccessful && !url.isNullOrBlank()) {
                                    // Guardar referencia del pedido y total temporalmente para la pantalla de confirmación
                                    context.getSharedPreferences("pedidos", Context.MODE_PRIVATE)
                                        .edit()
                                        .putString("ultimo_pedido", orderNum)
                                        .apply()

                                    context.getSharedPreferences("payment_temp", Context.MODE_PRIVATE)
                                        .edit()
                                        .putFloat("total", total.toFloat())
                                        .apply()

                                    // Abrir Stripe Checkout en el navegador (igual que en web)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                    
                                    // Esperar 2 segundos y luego ir a la pantalla de confirmación
                                    // (en realidad Stripe maneja el return_url, pero aquí nos preparamos)
                                    scope.launch {
                                        kotlinx.coroutines.delay(2000)
                                        val confirmIntent = Intent(context, com.ropa.smartfashionecommerce.pedidos.PedidoConfirmado::class.java)
                                        confirmIntent.putExtra("order_number", orderNum)
                                        confirmIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                        context.startActivity(confirmIntent)
                                        // Cerrar FinalizarCompra
                                        (context as? android.app.Activity)?.finish()
                                    }
                                } else {
                                    val errBody = try { sessionRes.errorBody()?.string() } catch (_: Exception) { null }
                                    errorMessage = sessionBody?.message
                                        ?: sessionBody?.status
                                        ?: errBody
                                        ?: "Error al crear sesión de pago (${sessionRes.code()})"
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error de conexión: ${e.message}"
                            } finally {
                                isProcessing = false
                            }
                        }
                    }
                },
                enabled = !isProcessing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF111827)
                ),
                shape = RoundedCornerShape(12.dp)
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Pagar con tarjeta (Stripe)",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Compra 100% segura",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
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
