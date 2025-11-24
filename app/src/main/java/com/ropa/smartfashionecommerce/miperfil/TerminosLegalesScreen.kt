package com.ropa.smartfashionecommerce.miperfil

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminosLegalesScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Términos y condiciones") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Conoce nuestras políticas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Política de privacidad ahora se muestra como texto interno
            SeccionLegalItem("Política de privacidad") {
                context.startActivity(
                    Intent(context, DetalleTerminoActivity::class.java).apply {
                        putExtra("titulo", "Política de privacidad")
                        putExtra("contenido", TEXTO_PRIVACIDAD)
                    }
                )
            }

            // Nuevo ítem: Decreto legislativo (abre WebView con el enlace proporcionado)
            SeccionLegalItem("Decreto legislativo") {
                context.startActivity(
                    Intent(context, WebViewActivity::class.java).apply {
                        putExtra("titulo", "Decreto legislativo")
                        putExtra("url", "https://busquedas.elperuano.pe/dispositivo/NL/2312442-1")
                    }
                )
            }

            SeccionLegalItem("Términos de uso") {
                context.startActivity(
                    Intent(context, DetalleTerminoActivity::class.java).apply {
                        putExtra("titulo", "Términos de uso")
                        putExtra("contenido", TEXTO_TERMINOS_USO)
                    }
                )
            }

            SeccionLegalItem("Política de devolución y reembolso") {
                context.startActivity(
                    Intent(context, DetalleTerminoActivity::class.java).apply {
                        putExtra("titulo", "Política de devolución y reembolso")
                        putExtra("contenido", TEXTO_DEVOLUCION)
                    }
                )
            }

            SeccionLegalItem("Política de propiedad intelectual") {
                context.startActivity(
                    Intent(context, DetalleTerminoActivity::class.java).apply {
                        putExtra("titulo", "Política de propiedad intelectual")
                        putExtra("contenido", TEXTO_PROPIEDAD_INTELECTUAL)
                    }
                )
            }

            SeccionLegalItem("Política de envíos") {
                context.startActivity(
                    Intent(context, DetalleTerminoActivity::class.java).apply {
                        putExtra("titulo", "Política de envíos")
                        putExtra("contenido", TEXTO_ENVIOS)
                    }
                )
            }

            SeccionLegalItem("Información de pago") {
                context.startActivity(
                    Intent(context, DetalleTerminoActivity::class.java).apply {
                        putExtra("titulo", "Información de pago")
                        putExtra("contenido", TEXTO_PAGOS)
                    }
                )
            }
        }
    }
}

@Composable
private fun SeccionLegalItem(titulo: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = titulo,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = Color(0xFFB0B0B0),
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer(rotationZ = 180f)
            )
        }
    }
}

const val TEXTO_PRIVACIDAD = "En SmartFashion, protegemos tu información personal y la utilizamos solo para procesar tus compras, enviarte tus pedidos y mejorar tu experiencia en la app. Nunca vendemos tus datos a terceros y solo los compartimos con proveedores necesarios para completar el servicio (por ejemplo, empresas de envío o procesadores de pago). Puedes solicitar la actualización o eliminación de tus datos personales escribiendo a nuestro soporte." 

const val TEXTO_TERMINOS_USO = "Al usar la aplicación SmartFashion aceptas que utilizarás la plataforma de forma responsable, sin realizar actividades fraudulentas ni intentar vulnerar la seguridad del sistema. Nos reservamos el derecho de suspender cuentas que incumplan estos términos o realicen un uso inadecuado del servicio." 

const val TEXTO_DEVOLUCION = "Las devoluciones y reembolsos se gestionan dentro de un plazo máximo de 7 días calendario después de recibir tu pedido, siempre que el producto se encuentre en buen estado, sin uso y con sus etiquetas originales. Algunos productos como ropa interior o accesorios personales pueden no aplicar para devolución por motivos de higiene." 

const val TEXTO_PROPIEDAD_INTELECTUAL = "Todas las imágenes, logotipos, descripciones y contenidos de la aplicación SmartFashion pertenecen a sus respectivos propietarios y están protegidos por las leyes de propiedad intelectual. No está permitido copiar, distribuir o utilizar estos contenidos con fines comerciales sin autorización previa." 

const val TEXTO_ENVIOS = "Realizamos envíos a las principales ciudades del país mediante empresas de mensajería aliadas. Los tiempos de entrega y costos de envío se mostrarán antes de confirmar tu compra y pueden variar según tu ubicación y promociones vigentes." 

const val TEXTO_PAGOS = "Aceptamos diferentes métodos de pago seguros como tarjetas de crédito y débito, así como otros medios disponibles en tu región. Los pagos son procesados por pasarelas certificadas que protegen tus datos bancarios. SmartFashion no almacena la información completa de tus tarjetas." 
