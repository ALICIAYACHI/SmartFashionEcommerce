package com.ropa.smartfashionecommerce.miperfil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import java.text.SimpleDateFormat
import java.util.*

class SeguimientoPedidoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val codigoPedido = intent.getStringExtra("codigo_pedido") ?: "N/A"

        // Cargar pedidos del usuario
        PedidosManager.cargarPedidos(this)

        // Buscar el pedido seleccionado
        val pedido = PedidosManager.pedidos.find { it.codigo == codigoPedido }

        // Calcular y actualizar estado dinámicamente según la fecha
        val pedidoActualizado = pedido?.let { original ->
            val nuevoEstado = calcularEstadoPedido(original.fecha)
            if (nuevoEstado != original.estado) {
                PedidosManager.actualizarEstado(this, original.codigo, nuevoEstado)
                original.copy(estado = nuevoEstado)
            } else {
                original
            }
        }

        setContent {
            MaterialTheme {
                if (pedidoActualizado != null) {
                    SeguimientoPedidoScreen(pedido = pedidoActualizado, onBack = { finish() })
                } else {
                    PedidoNoEncontradoScreen(codigoPedido = codigoPedido, onBack = { finish() })
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

            pedido.productos.forEach { nombrePedido ->
                // Extraer solo el nombre del producto (antes de " xCantidad")
                val nombreSolo = nombrePedido.substringBefore(" x").trim()

                // 🔍 Coincidencia por nombre ignorando mayúsculas y espacios
                val producto = localProducts.find {
                    it.nombre.trim().equals(nombreSolo, ignoreCase = true)
                }

                ProductoItem(
                    nombre = nombrePedido,
                    imageRes = producto?.localImageRes,
                    imageUrl = producto?.image_preview
                )
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
                }
            }
        }
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
    val estados = listOf("Procesando", "En tránsito", "Entregado")

    Column(modifier = Modifier.fillMaxWidth()) {
        estados.forEach { estadoItem ->
            EstadoItem(
                titulo = estadoItem,
                activo = estadoItem == estado || estados.indexOf(estadoItem) < estados.indexOf(estado),
                fecha = if (estado == estadoItem) fecha else "-"
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
fun ProductoItem(nombre: String, imageRes: Int? = null, imageUrl: String? = null) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
