package com.ropa.smartfashionecommerce.miperfil


import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.R

@Composable
fun MiPerfilScreen( ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 👈 Esto hace que sea scrolleable
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Imagen circular del perfil
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground), // cambia por tu imagen real
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Juan Pérez", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("juanperez@gmail.com", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

        Spacer(modifier = Modifier.height(24.dp))

        // Opciones de menú
        OpcionPerfil("Editar Perfil")
        OpcionPerfil("Historial de Compras")
        OpcionPerfil("Direcciones Guardadas")
        OpcionPerfil("Métodos de Pago")
        OpcionPerfil("Notificaciones")
        OpcionPerfil("Privacidad")
        OpcionPerfil("Ayuda y Soporte")
        OpcionPerfil("Cerrar Sesión")

        Spacer(modifier = Modifier.height(50.dp)) // para que no se corte al final
    }
}

@Composable
fun OpcionPerfil(texto: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Text(
            texto,
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp
        )
    }
}