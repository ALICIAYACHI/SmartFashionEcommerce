package com.ropa.smartfashionecommerce.miperfil

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.DarkLoginActivity
import com.ropa.smartfashionecommerce.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPerfilScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val user = Firebase.auth.currentUser
    var displayName by remember { mutableStateOf(user?.displayName ?: "Usuario") }
    val email = user?.email ?: "correo@ejemplo.com"
    val photoUrl = user?.photoUrl

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = Color.White)
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
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFEAEAEA))))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de perfil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        brush = Brush.linearGradient(
                            listOf(Color(0xFF000000), Color(0xFF757575))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(photoUrl ?: R.drawable.ic_person),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo editable de nombre
            var nuevoNombre by remember { mutableStateOf(displayName) }

            OutlinedTextField(
                value = nuevoNombre,
                onValueChange = { nuevoNombre = it },
                label = { Text("Nombre") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    guardarNombreFirebase(nuevoNombre)
                    displayName = nuevoNombre
                    Toast.makeText(context, "Nombre actualizado", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Guardar Cambios", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = email,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Opciones del perfil
            ProfileOptionCard(
                icon = Icons.Default.ShoppingBag,
                title = "Mis pedidos",
                subtitle = "Historial y seguimiento de compras",
                onClick = { Toast.makeText(context, "Abrir mis pedidos", Toast.LENGTH_SHORT).show() }
            )

            ProfileOptionCard(
                icon = Icons.Default.Settings,
                title = "Configuración",
                subtitle = "Preferencias de cuenta y notificaciones",
                onClick = { Toast.makeText(context, "Abrir configuración", Toast.LENGTH_SHORT).show() }
            )

            ProfileOptionCard(
                icon = Icons.Default.ExitToApp,
                title = "Cerrar sesión",
                subtitle = "Salir de tu cuenta",
                onClick = {
                    Firebase.auth.signOut()
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, DarkLoginActivity::class.java)
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Versión 1.0.0",
                color = Color.Gray,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
fun ProfileOptionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF0F0F0), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
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