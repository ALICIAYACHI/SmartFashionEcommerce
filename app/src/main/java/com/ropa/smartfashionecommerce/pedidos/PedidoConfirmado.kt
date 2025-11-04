package com.ropa.smartfashionecommerce.pedidos

import android.content.Intent
import androidx.activity.ComponentActivity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import java.text.SimpleDateFormat
import java.util.*

class PedidoConfirmado : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val total = intent.getDoubleExtra("total", 0.0)
        val numPedido = intent.getStringExtra("numeroPedido") ?: generarCodigoPedido()

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
                    PedidoConfirmadoScreen(
                        numeroPedido = numPedido,
                        fechaPedido = fechaPedido,
                        fechaEntrega = fechaEntrega,
                        totalPagado = total,
                        onSeguirComprando = {
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        },
                        onVolverInicio = {
                            finishAffinity()
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                        }
                    )
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
                Divider(modifier = Modifier.padding(vertical = 8.dp))
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
