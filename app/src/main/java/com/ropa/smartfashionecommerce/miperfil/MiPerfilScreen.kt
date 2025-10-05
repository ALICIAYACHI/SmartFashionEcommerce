package com.ropa.smartfashionecommerce.miperfil

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.ropa.smartfashionecommerce.DarkLoginActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPerfilScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    var nombre by remember { mutableStateOf(user?.displayName ?: "Usuario") }
    val correo = user?.email ?: "correo@ejemplo.com"
    var editando by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 🔹 Datos del usuario
            Text(
                text = "Hola, $nombre 👋",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = correo,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Edición del nombre
            if (editando) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        guardarNombreFirebase(nombre)
                        editando = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "Guardar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar cambios")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Menú de opciones
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OpcionPerfil("Editar Perfil", Icons.Default.Edit) {
                    editando = true
                }
                OpcionPerfil("Historial de Compras", Icons.Default.History) {
                    // Aquí podrías abrir la actividad de historial
                }
                OpcionPerfil("Direcciones Guardadas", Icons.Default.LocationOn) {
                    // Abrir actividad de direcciones
                }
                OpcionPerfil("Métodos de Pago", Icons.Default.CreditCard) {
                    // Abrir actividad de métodos de pago
                }
                OpcionPerfil("Notificaciones", Icons.Default.Notifications) {
                    // Configurar notificaciones
                }
                OpcionPerfil("Privacidad", Icons.Default.Lock) {
                    // Configuración de privacidad
                }
                OpcionPerfil("Ayuda y Soporte", Icons.Default.HelpOutline) {
                    // Abrir centro de ayuda
                }

                // 🔴 Cerrar sesión
                OpcionPerfil(
                    titulo = "Cerrar Sesión",
                    icono = Icons.Default.Logout,
                    isLogout = true
                ) {
                    auth.signOut()
                    val intent = Intent(context, DarkLoginActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun OpcionPerfil(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    isLogout: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val colorTexto = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val colorIcono = if (isLogout) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick?.invoke() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icono, contentDescription = null, tint = colorIcono)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = titulo,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = colorTexto
            )
        }
    }
}

// 🔧 Guarda el nuevo nombre del usuario en Firebase
private fun guardarNombreFirebase(nuevoNombre: String) {
    val user = FirebaseAuth.getInstance().currentUser
    val profileUpdates = UserProfileChangeRequest.Builder()
        .setDisplayName(nuevoNombre)
        .build()

    user?.updateProfile(profileUpdates)
}
