package com.ropa.smartfashionecommerce.miperfil

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.DarkLoginActivity
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.PedidosManager
import com.ropa.smartfashionecommerce.home.FavoritesManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPerfilScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val user = Firebase.auth.currentUser

    // 🔑 Usar el email del usuario como clave única para SharedPreferences
    val userEmail = user?.email ?: ""
    val sharedPrefs = context.getSharedPreferences("user_profile_$userEmail", android.content.Context.MODE_PRIVATE)

    // 🟢 Estados para datos del usuario
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var fotoUri by remember { mutableStateOf<Uri?>(null) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    // ✅ Cargar pedidos
    val pedidos = remember { derivedStateOf { PedidosManager.pedidos } }

    // 🔄 Cargar/recargar datos cuando la pantalla se muestra
    LaunchedEffect(refreshTrigger, userEmail) {
        nombre = sharedPrefs.getString("nombre", user?.displayName ?: "Usuario") ?: user?.displayName ?: "Usuario"
        email = userEmail.ifEmpty { "Sin email" }
        telefono = sharedPrefs.getString("telefono", "") ?: ""
        direccion = sharedPrefs.getString("direccion", "") ?: ""

        // Cargar foto de perfil específica del usuario
        val savedImageUri = sharedPrefs.getString("foto_perfil_uri", null)
        fotoUri = savedImageUri?.toUri()

        // ✅ Cargar historial de pedidos
        PedidosManager.cargarPedidos(context)
    }

    // 🔄 Recargar datos al volver de editar perfil
    DisposableEffect(Unit) {
        onDispose {
            refreshTrigger++
        }
    }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showNotificacionesDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFEAEAEA))))
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🟣 FOTO DE PERFIL
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (fotoUri != null) {
                    val bitmap = ProfileImageManager.getBitmapFromUri(context, fotoUri!!)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(R.drawable.ic_person),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )
                    }
                } else {
                    Image(
                        painter = rememberAsyncImagePainter(user?.photoUrl ?: R.drawable.ic_person),
                        contentDescription = "Foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 🟣 Nombre + ícono editar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(nombre, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF212121))
                IconButton(
                    onClick = {
                        val intent = Intent(context, EditarPerfilActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar perfil",
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(email, fontSize = 14.sp, color = Color(0xFF616161))

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 🟣 INFORMACIÓN PERSONAL
            Text("Información Personal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))

            InfoRow("Email", email)
            InfoRow("Teléfono", telefono.ifEmpty { "No registrado" })
            InfoRow("Dirección", direccion.ifEmpty { "No registrada" })

            Spacer(modifier = Modifier.height(25.dp))

            // 🟣 HISTORIAL DE PEDIDOS (REAL)
            Text("Historial de Pedidos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))

            if (pedidos.value.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No tienes pedidos aún",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                pedidos.value.forEach { pedido ->
                    val colorEstado = when (pedido.estado) {
                        "Entregado" -> Color(0xFF4CAF50)
                        "En tránsito" -> Color(0xFF3F51B5)
                        else -> Color(0xFFFFC107) // Procesando
                    }

                    PedidoItem(
                        codigo = pedido.codigo,
                        estado = pedido.estado,
                        colorEstado = colorEstado,
                        precio = "S/ ${"%.2f".format(pedido.total)}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(25.dp))

            // 🟣 CONFIGURACIÓN DE CUENTA
            Text("Configuración de Cuenta", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF212121))
            Spacer(modifier = Modifier.height(10.dp))

            ProfileOptionCard(Icons.Default.Edit, "Editar Perfil", "Actualiza tu información") {
                val intent = Intent(context, EditarPerfilActivity::class.java)
                context.startActivity(intent)
            }

            ProfileOptionCard(Icons.Default.Lock, "Cambiar Contraseña", "Actualiza tu contraseña") {
                showChangePasswordDialog = true
            }

            ProfileOptionCard(Icons.Default.Notifications, "Notificaciones", "Alertas y promociones") {
                showNotificacionesDialog = true
            }

            ProfileOptionCard(Icons.Default.Home, "Dirección de Envío", "Gestiona tus direcciones") {
                val intent = Intent(context, DireccionesEnvioActivity::class.java)
                context.startActivity(intent)
            }

            // 🟣 CERRAR SESIÓN
            ProfileOptionCard(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar Sesión", "Salir de tu cuenta") {
                // ⚠️ NO eliminar los datos del perfil al cerrar sesión
                // Solo limpiar datos temporales como favoritos
                FavoritesManager.clearFavorites()

                Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()

                // Cerrar sesión de Firebase
                Firebase.auth.signOut()

                val intent = Intent(context, DarkLoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
                (context as? Activity)?.finish()
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Versión 1.0.0", color = Color.Gray, fontSize = 12.sp, fontStyle = FontStyle.Italic)
        }
    }

    if (showChangePasswordDialog) {
        CambiarContrasenaDialog(onDismiss = { showChangePasswordDialog = false })
    }

    if (showNotificacionesDialog) {
        PreferenciasNotificacionesDialog(onDismiss = { showNotificacionesDialog = false })
    }
}

// ✅ COMPONENTES REUTILIZABLES
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF616161))
        Text(value, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
    }
}

@Composable
fun PedidoItem(codigo: String, estado: String, colorEstado: Color, precio: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Pedido $codigo", fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Box(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(colorEstado, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(estado, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
            Text(precio, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
        }
    }
}

@Composable
fun ProfileOptionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = Color.Black, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF212121))
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF616161))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF616161))
        }
    }
}