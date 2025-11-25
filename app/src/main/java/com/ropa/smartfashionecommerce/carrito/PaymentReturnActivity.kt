package com.ropa.smartfashionecommerce.carrito

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
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
import com.ropa.smartfashionecommerce.pedidos.PedidoConfirmado
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme

/**
 * 🔹 ACTIVIDAD PARA MANEJAR EL RETORNO DE MERCADO PAGO
 * 
 * Esta actividad recibe al usuario después de que realiza el pago
 * en Mercado Pago y procesa el resultado.
 * 
 * Estados posibles:
 * - success: Pago aprobado
 * - pending: Pago pendiente de aprobación
 * - failure: Pago rechazado
 */
class PaymentReturnActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener parámetros del deep link
        val uri = intent.data
        val status = uri?.lastPathSegment ?: "unknown" // success, pending, failure
        
        // Parámetros adicionales de Mercado Pago
        val paymentId = uri?.getQueryParameter("payment_id")
        val externalReference = uri?.getQueryParameter("external_reference")
        val collectionStatus = uri?.getQueryParameter("collection_status")
        
        setContent {
            SmartFashionEcommerceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF9F9F9)
                ) {
                    PaymentResultScreen(
                        status = status,
                        paymentId = paymentId,
                        externalReference = externalReference,
                        collectionStatus = collectionStatus,
                        onBackToHome = {
                            val intent = Intent(this, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                            finish()
                        },
                        onViewOrder = {
                            // Si el pago fue exitoso, registrar el pedido y mostrar confirmación
                            if (status == "success") {
                                val total = getSharedPreferences("payment_temp", MODE_PRIVATE)
                                    .getFloat("total", 0f).toDouble()
                                
                                // Descontar stock y registrar pedido
                                val cartItems = CartManager.cartItems
                                cartItems.forEach { item ->
                                    StockManager.reduceStock(this, item.name, item.quantity)
                                }
                                
                                PedidosManager.agregarPedido(
                                    context = this,
                                    total = total,
                                    productos = cartItems.map { "${it.name} x${it.quantity}" },
                                    direccionTexto = "Pago con Mercado Pago"
                                )
                                
                                // Limpiar carrito
                                CartManager.clear()
                                
                                // Ir a confirmación
                                val intent = Intent(this, PedidoConfirmado::class.java).apply {
                                    putExtra("total", total)
                                    putExtra("paymentId", paymentId)
                                }
                                startActivity(intent)
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PaymentResultScreen(
    status: String,
    paymentId: String?,
    externalReference: String?,
    collectionStatus: String?,
    onBackToHome: () -> Unit,
    onViewOrder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (status) {
            "success" -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "¡Pago exitoso!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Tu pago ha sido procesado correctamente",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                if (paymentId != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "ID de pago:",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                            Text(
                                paymentId,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onViewOrder,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Ver mi pedido", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedButton(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Volver al inicio", fontSize = 16.sp)
                }
            }
            
            "pending" -> {
                Icon(
                    imageVector = Icons.Default.HourglassEmpty,
                    contentDescription = "Pending",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Pago pendiente",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Tu pago está siendo procesado. Te notificaremos cuando esté confirmado.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Entendido", fontSize = 16.sp)
                }
            }
            
            "failure" -> {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Error",
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(80.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    "Pago rechazado",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "No se pudo procesar tu pago. Por favor, intenta nuevamente.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Intentar de nuevo", fontSize = 16.sp)
                }
            }
            
            else -> {
                Text(
                    "Estado desconocido",
                    fontSize = 20.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Volver al inicio", fontSize = 16.sp)
                }
            }
        }
    }
}
