package com.ropa.smartfashionecommerce.pedidos

import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.carrito.CartManager
import com.ropa.smartfashionecommerce.carrito.PedidosManager
import android.net.Uri
import androidx.core.content.edit
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
class PedidoConfirmado : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el carrito para poder limpiarlo si venimos desde un deep link
        CartManager.initialize(this)

        // Detectar si la Activity fue abierta mediante el deep link smartfashion://checkout/success
        val data: Uri? = intent?.data
        val orderFromLink: String? = if (Intent.ACTION_VIEW == intent?.action && data != null) {
            data.getQueryParameter("order")
        } else {
            null
        }

        // Cargar el total y productos almacenados temporalmente durante el flujo de pago
        val prefs = getSharedPreferences("payment_temp", MODE_PRIVATE)
        val total = prefs.getFloat("total", 0f).toDouble()
        val productosSet = prefs.getStringSet("productos_resumen", emptySet()) ?: emptySet()
        val productosLista = productosSet.toList().sorted()
        val imagenesLista = prefs.getStringSet("imagenes_resumen", emptySet())?.toList()
        val productIdsLista = prefs.getStringSet("product_ids_resumen", emptySet())
            ?.mapNotNull { it.toIntOrNull() }

        // Número de pedido: priorizar el que viene por deep link, luego el extra normal, luego generar uno
        val numPedido = orderFromLink
            ?: intent.getStringExtra("numeroPedido")
            ?: generarCodigoPedido()

        // Crear registro local en el historial de pedidos (solo una vez por pedido)
        val alreadySaved = prefs.getBoolean("order_saved", false)
        if (!alreadySaved) {
            val productos = productosLista
            val direccionTexto = prefs.getString("direccion_texto", "") ?: ""

            if (productos.isNotEmpty()) {
                PedidosManager.agregarPedido(
                    context = this,
                    total = total,
                    productos = productos,
                    direccionTexto = direccionTexto,
                    imagenes = imagenesLista,
                    productIds = productIdsLista
                )
            }

            prefs.edit {
                putBoolean("order_saved", true)
            }
        }

        // Vaciar el carrito local ahora que el pedido ha sido confirmado
        CartManager.clear()

        // 📅 Fecha actual (pedido)
        val fechaPedido = SimpleDateFormat("d/MM/yyyy", Locale.getDefault()).format(Date())

        // 📦 Fecha estimada: entre 8 y 20 días hábiles
        val rangoEntrega = calcularRangoEntrega(8, 20)
        val fechaEntrega = "${rangoEntrega.first} - ${rangoEntrega.second}"

        setContent {
            SmartFashionEcommerceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF9F9F9)
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Pedido confirmado", color = Color.Black) },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        val activity = this@PedidoConfirmado
                                        val intent = Intent(activity, HomeActivity::class.java)
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        activity.startActivity(intent)
                                        activity.finish()
                                    }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Volver",
                                            tint = Color.Black
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                            )
                        },
                        containerColor = Color(0xFFF9F9F9)
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            PedidoConfirmadoScreen(
                                numeroPedido = numPedido,
                                fechaPedido = fechaPedido,
                                fechaEntrega = fechaEntrega,
                                totalPagado = total,
                                productos = productosLista,
                                onSeguirComprando = {
                                    val activity = this@PedidoConfirmado
                                    val intent = Intent(activity, HomeActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    activity.startActivity(intent)
                                    activity.finish()
                                },
                                onVolverInicio = {
                                    val activity = this@PedidoConfirmado
                                    activity.finishAffinity()
                                    val intent = Intent(activity, HomeActivity::class.java)
                                    activity.startActivity(intent)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    /** 🔹 Genera un número de pedido aleatorio tipo “SFHUBT5RV5R” */
    private fun generarCodigoPedido(): String {
        val chars = ('A'..'Z') + ('0'..'9')
        val randomCode = (1..8).map { chars.random() }.joinToString("")
        return "SFH$randomCode"
    }

    /** 🔹 Calcula un rango de entrega entre [min] y [max] días hábiles */
    private fun calcularRangoEntrega(min: Int, max: Int): Pair<String, String> {
        val formato = SimpleDateFormat("d/MM/yyyy", Locale.getDefault())
        val calendario = Calendar.getInstance()

        val inicio = calendario.clone() as Calendar
        val fin = calendario.clone() as Calendar

        // Avanzar X días hábiles (saltando sábados y domingos)
        fun sumarDiasHabiles(cal: Calendar, dias: Int) {
            var agregados = 0
            while (agregados < dias) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
                val diaSemana = cal.get(Calendar.DAY_OF_WEEK)
                if (diaSemana != Calendar.SATURDAY && diaSemana != Calendar.SUNDAY) {
                    agregados++
                }
            }
        }

        sumarDiasHabiles(inicio, min)
        sumarDiasHabiles(fin, max)

        return Pair(formato.format(inicio.time), formato.format(fin.time))
    }
}

@Composable
fun PedidoConfirmadoScreen(
    numeroPedido: String,
    fechaPedido: String,
    fechaEntrega: String,
    totalPagado: Double,
    productos: List<String>,
    onSeguirComprando: () -> Unit,
    onVolverInicio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // ✅ Logo o nombre
        Text(
            "SmartFashion",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // ✅ Ícono verde check
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Pedido confirmado",
            tint = Color(0xFF22C55E),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "¡Pedido confirmado!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Gracias por tu compra. Tu pedido ha sido procesado exitosamente.",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // 🧾 Card de detalles del pedido
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Detalles del pedido", fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                DetalleFila("Número de pedido", numeroPedido)
                DetalleFila("Fecha de pedido", fechaPedido)
                DetalleFila("Entrega estimada", fechaEntrega)
                if (productos.isNotEmpty()) {
                    val productosTexto = productos.joinToString(separator = "\n")
                    Spacer(modifier = Modifier.height(4.dp))
                    DetalleFila("Productos", productosTexto)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                DetalleFila("Total pagado", "S/ ${"%.2f".format(totalPagado)}", negrita = true)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 📦 Qué sigue
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("¿Qué sigue?", fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                DetallePaso("Pago procesado", "Tu pago ha sido confirmado")
                DetallePaso("Preparando pedido", "Empaquetaremos tus productos")
                DetallePaso("Envío", "Te notificaremos cuando esté en camino")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 🔘 Botones
        Button(
            onClick = onSeguirComprando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Seguir comprando", color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolverInicio,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Volver al inicio", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "¿Tienes preguntas sobre tu pedido?\nContáctanos al +51 999 888 777 o soporte@smartfashion.com",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DetalleFila(label: String, valor: String, negrita: Boolean = false) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(
            valor,
            fontWeight = if (negrita) FontWeight.Bold else FontWeight.Normal,
            color = if (negrita) Color(0xFF2563EB) else Color.Black,
            fontSize = 14.sp
        )
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
fun DetallePaso(titulo: String, descripcion: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(titulo, fontWeight = FontWeight.Medium, color = Color.Black)
        Text(descripcion, color = Color.Gray, fontSize = 13.sp)
    }
}
