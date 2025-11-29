package com.ropa.smartfashionecommerce.miperfil

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Pedido
import com.ropa.smartfashionecommerce.carrito.PedidosManager
import com.ropa.smartfashionecommerce.home.localProducts
import com.ropa.smartfashionecommerce.detalles.ProductDetailActivity
import androidx.compose.ui.platform.LocalContext
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.network.ReturnRequestBody
import kotlinx.coroutines.launch
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.*
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SeguimientoPedidoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val codigoPedido = intent.getStringExtra("codigo_pedido") ?: "N/A"

        lifecycleScope.launch {
            val context = this@SeguimientoPedidoActivity

            // 1) Intentar obtener el pedido desde el backend (historial real)
            var pedido: Pedido? = withContext(Dispatchers.IO) {
                val email = Firebase.auth.currentUser?.email
                if (email.isNullOrEmpty()) return@withContext null

                try {
                    val api = ApiClient.apiService
                    val resp = api.getUserOrders(email)
                    if (!resp.isSuccessful) return@withContext null

                    val orders = resp.body()?.data ?: emptyList()
                    val match = orders.firstOrNull { it.order_number == codigoPedido }
                    if (match != null) {
                        // Fecha en formato dd/MM/yyyy
                        val fechaIso = match.created_at ?: ""
                        val fechaLimpia = try {
                            if (fechaIso.length >= 10) {
                                val sdfIn = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val sdfOut = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                val date = sdfIn.parse(fechaIso.substring(0, 10))
                                if (date != null) sdfOut.format(date) else fechaIso
                            } else fechaIso
                        } catch (_: Exception) {
                            fechaIso
                        }

                        val productos = match.items.map { item ->
                            "${item.name} x${item.qty}"
                        }

                        val imagenes = match.items.map { it.image }
                        val productIds = match.items.map { it.product_id }

                        val direccionTexto = match.envio?.direccion ?: ""
                        val estado = match.envio?.status ?: "Procesando"

                        Pedido(
                            codigo = match.order_number,
                            fecha = fechaLimpia.ifBlank { "" },
                            total = match.total,
                            estado = estado,
                            productos = productos,
                            direccionTexto = direccionTexto,
                            productIds = productIds,
                            imagenes = imagenes
                        )
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                }
            }

            var esLocal = false

            // 2) Si backend no lo conoce (caso antiguo), usar PedidosManager local
            if (pedido == null) {
                PedidosManager.cargarPedidos(context)
                val pedidoLocal = PedidosManager.pedidos.find { it.codigo == codigoPedido }
                pedido = pedidoLocal
                esLocal = pedidoLocal != null
            }

            // 3) Para pedidos locales antiguos, seguir usando cálculo por fecha.
            //    Para pedidos nuevos del backend, respetar siempre el estado enviado por el servidor.
            val pedidoFinal = if (pedido != null && esLocal) {
                val nuevoEstado = calcularEstadoPedido(pedido!!.fecha)
                if (nuevoEstado != pedido!!.estado) {
                    PedidosManager.actualizarEstado(context, pedido!!.codigo, nuevoEstado)
                    pedido!!.copy(estado = nuevoEstado)
                } else {
                    pedido!!
                }
            } else {
                pedido
            }

            setContent {
                MaterialTheme {
                    if (pedidoFinal != null) {
                        SeguimientoPedidoScreen(pedido = pedidoFinal, onBack = { finish() })
                    } else {
                        PedidoNoEncontradoScreen(codigoPedido = codigoPedido, onBack = { finish() })
                    }
                }
            }
        }
    }
}

/**
 * Calcula el estado del pedido según los días transcurridos desde la fecha de compra.
 * 0-1 días: "Procesando"
 * 2-3 días: "En tránsito"
 * 4+ días: "Entregado"
 */
fun calcularEstadoPedido(fechaPedido: String): String {
    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = formato.parse(fechaPedido) ?: return "Procesando"

        val hoy = Calendar.getInstance()
        val calPedido = Calendar.getInstance().apply { time = fecha }

        val diffMillis = hoy.timeInMillis - calPedido.timeInMillis
        val dias = (diffMillis / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)

        when {
            dias <= 1 -> "Procesando"
            dias in 2..3 -> "En tránsito"
            else -> "Entregado"
        }
    } catch (_: Exception) {
        "Procesando"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeguimientoPedidoScreen(pedido: Pedido, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userEmail = Firebase.auth.currentUser?.email

    var showReturnDialog by remember { mutableStateOf(false) }
    var selectedMotivo by remember { mutableStateOf("no_satisfecho") }
    var selectedMetodo by remember { mutableStateOf("reembolso") }
    var descripcion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    // Estado de devolución existente (si la hay)
    var returnStatus by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(pedido.codigo) {
        try {
            val api = ApiClient.apiService
            val res = api.listReturnRequests()
            if (res.isSuccessful) {
                val body = res.body()
                val match = body?.data?.firstOrNull { it.order_number == pedido.codigo }
                returnStatus = match?.estado
            }
        } catch (_: Exception) {
            // ignorar errores de red
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Pedido", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF212121))
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Text("Pedido ${pedido.codigo}", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Seguimiento de tu compra", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))

            // Estado actual del pedido
            EstadoPedido(estado = pedido.estado, fecha = pedido.fecha)

            Spacer(modifier = Modifier.height(20.dp))

            // Resumen del pedido
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resumen del Pedido", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fecha: ${pedido.fecha}")
                    Text("Artículos: ${pedido.productos.size}")
                    Text("Estado: ${pedido.estado}", color = Color(0xFF3F51B5))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "Total: S/ %.2f", pedido.total),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Productos del pedido
            Text("Productos del pedido", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))

            pedido.productos.forEachIndexed { index, nombrePedido ->
                // Nombre limpio sin el "x1"
                val nombreSolo = nombrePedido.substringBefore(" x").trim()

                // Si el pedido tiene lista de imágenes, usarla
                val imageUrlFromPedido = pedido.imagenes?.getOrNull(index)
                val productId = pedido.productIds?.getOrNull(index)

                val onClick: () -> Unit = {
                    if (productId != null && productId > 0) {
                        val intent = Intent(context, ProductDetailActivity::class.java).apply {
                            putExtra("productId", productId)
                            putExtra("productName", nombreSolo)
                            if (!imageUrlFromPedido.isNullOrBlank()) {
                                putExtra("imageType", "url")
                                putExtra("productImageUrl", imageUrlFromPedido)
                            }
                        }
                        context.startActivity(intent)
                    }
                }

                if (imageUrlFromPedido != null) {
                    ProductoItem(
                        nombre = nombrePedido,
                        imageRes = null,
                        imageUrl = imageUrlFromPedido,
                        onClick = onClick
                    )
                } else {
                    // Fallback antiguo: buscar por nombre en localProducts
                    val producto = localProducts.find {
                        it.nombre.trim().equals(nombreSolo, ignoreCase = true)
                    }

                    ProductoItem(
                        nombre = nombrePedido,
                        imageRes = producto?.localImageRes,
                        imageUrl = producto?.image_preview,
                        onClick = onClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Información de envío
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Información de Envío", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Dirección: Se enviará a la dirección registrada en tu perfil.")
                    Text("Estado actual: ${pedido.estado}")
                    Text("Fecha estimada de entrega: ${calcularFechaEntrega(pedido.fecha)}")

                    returnStatus?.let { estadoDevRaw ->
                        Spacer(modifier = Modifier.height(8.dp))
                        val estadoLower = estadoDevRaw.lowercase()
                        val mensaje = when (estadoLower) {
                            "solicitado" -> "Tu solicitud de devolución ha sido enviada. Estamos revisando tu caso."
                            "aprobado", "reembolsado" -> "Tu reembolso ha sido aprobado. El monto se acreditará en tu tarjeta en 5–10 días hábiles."
                            "rechazado" -> "Tu solicitud de devolución ha sido rechazada. Revisa tu correo para más detalles."
                            else -> "Estado de devolución: ${estadoDevRaw}"
                        }
                        Text(
                            text = mensaje,
                            color = Color(0xFFEF6C00),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón para solicitar devolución / reembolso (solo si aún no hay solicitud)
            if (returnStatus == null) {
                Button(
                    onClick = { showReturnDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF6C00)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Solicitar devolución / reembolso", color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }

    if (showReturnDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSubmitting) showReturnDialog = false },
            confirmButton = {
                TextButton(
                    enabled = !isSubmitting,
                    onClick = {
                        isSubmitting = true
                        scope.launch {
                            try {
                                val api = ApiClient.apiService
                                val body = ReturnRequestBody(
                                    order_number = pedido.codigo,
                                    motivo = selectedMotivo,
                                    descripcion = descripcion.ifBlank { null },
                                    metodo = selectedMetodo,
                                    telefono = telefono.ifBlank { null },
                                    userEmail = userEmail
                                )
                                val res = api.createReturnRequest(body)
                                if (res.isSuccessful && res.body()?.status == "ok") {
                                    Toast.makeText(context, "Solicitud enviada", Toast.LENGTH_LONG).show()
                                    showReturnDialog = false
                                } else {
                                    Toast.makeText(context, "No se pudo enviar la solicitud", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSubmitting = false
                            }
                        }
                    }
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Text("Enviar")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isSubmitting,
                    onClick = { showReturnDialog = false }
                ) { Text("Cancelar") }
            },
            title = { Text("Solicitar devolución / reembolso") },
            text = {
                Column {
                    Text("Motivo", fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    val motivos = listOf(
                        "talla_incorrecta" to "Talla incorrecta",
                        "defectuoso" to "Producto defectuoso",
                        "no_satisfecho" to "No me gustó",
                        "otro" to "Otro"
                    )
                    motivos.forEach { (value, label) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedMotivo == value,
                                onClick = { selectedMotivo = value }
                            )
                            Text(label)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Qué deseas?", fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedMetodo == "reembolso",
                            onClick = { selectedMetodo = "reembolso" }
                        )
                        Text("Reembolso")
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedMetodo == "cambio",
                            onClick = { selectedMetodo = "cambio" }
                        )
                        Text("Cambio de producto")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Describe el motivo (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono de contacto (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidoNoEncontradoScreen(codigoPedido: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seguimiento de Pedido", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF212121))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No se encontró el pedido con código $codigoPedido", color = Color.Gray)
        }
    }
}

@Composable
fun EstadoPedido(estado: String, fecha: String) {
    val pasos = listOf("Pagado", "Procesando envío", "En tránsito", "Entregado")

    // Normalizar estado que viene del backend (ASIGNADO, EN_CAMINO, ENTREGADO, etc.)
    val estadoNorm = estado.trim().lowercase()

    val indiceActivo = when {
        // 0: Pagado (token ya creado y pago confirmado)
        //    Incluimos también estados iniciales como ASIGNADO/PROCESANDO/PENDIENTE
        //    para que, por ahora, solo se marque "Pagado".
        estadoNorm in listOf("pagado", "paid", "asignado", "procesando", "pendiente") -> 0
        // 1: Procesando envío (solo cuando el backend mande algo explícito tipo "preparando envío")
        estadoNorm.contains("prepar") -> 1
        // 2: En tránsito
        estadoNorm.contains("transito") || estadoNorm.contains("camino") -> 2
        // 3: Entregado / Completado
        estadoNorm.contains("entregado") || estadoNorm.contains("completado") -> 3
        else -> 0 // si no reconocemos, lo dejamos en el primer paso
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        pasos.forEachIndexed { index, tituloPaso ->
            val activo = index <= indiceActivo
            val fechaPaso = if (index == indiceActivo) fecha else "-"
            EstadoItem(
                titulo = tituloPaso,
                activo = activo,
                fecha = fechaPaso
            )
        }
    }
}

@Composable
fun EstadoItem(titulo: String, fecha: String, activo: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            titulo,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
            color = if (activo) Color(0xFF3F51B5) else Color.Gray
        )
        Text(fecha, color = Color.Gray, fontSize = 13.sp)
    }
}

@Composable
fun ProductoItem(nombre: String, imageRes: Int? = null, imageUrl: String? = null, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                imageUrl != null -> {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.LightGray)
                    )
                }
                imageRes != null -> {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.LightGray)
                    )
                }
                else -> {
                    Image(
                        painter = painterResource(id = R.drawable.modelo_ropa),
                        contentDescription = "Imagen genérica",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(70.dp)
                            .background(Color.LightGray)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))
            Text(nombre, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Calcula una fecha estimada de entrega (+3 días).
 */
fun calcularFechaEntrega(fechaPedido: String): String {
    return try {
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = formato.parse(fechaPedido)
        val cal = Calendar.getInstance()
        cal.time = fecha!!
        cal.add(Calendar.DAY_OF_MONTH, 3)
        formato.format(cal.time)
    } catch (_: Exception) {
        "-"
    }
}
