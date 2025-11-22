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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
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
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.home.FavActivity
import com.ropa.smartfashionecommerce.catalog.CatalogActivity
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.carrito.CartManager
import com.ropa.smartfashionecommerce.miperfil.MisResenasActivity

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

    // ✅ Cargar pedidos reales
    val pedidos = remember { derivedStateOf { PedidosManager.pedidos } }

    // 🔄 Cargar/recargar datos cuando la pantalla se muestra
    LaunchedEffect(refreshTrigger, userEmail) {
        nombre = sharedPrefs.getString("nombre", user?.displayName ?: "Usuario") ?: user?.displayName ?: "Usuario"
        email = userEmail.ifEmpty { "Sin email" }
        telefono = sharedPrefs.getString("telefono", "") ?: ""
        direccion = sharedPrefs.getString("direccion", "") ?: ""

        // Cargar foto de perfil específica del usuario
        // 1) Desde ProfileImageManager (archivo interno por UID)
        ProfileImageManager.loadProfileImage(context)
        val managerUri = ProfileImageManager.profileImageUri.value

        // 2) Fallback: desde SharedPreferences por email (compatibilidad)
        val savedImageUri = sharedPrefs.getString("foto_perfil_uri", null)

        fotoUri = managerUri ?: savedImageUri?.toUri()

        // ✅ Cargar historial de pedidos reales
        PedidosManager.cargarPedidos(context)
    }

    // 🔄 Recargar datos al volver de editar perfil
    DisposableEffect(Unit) {
        onDispose { refreshTrigger++ }
    }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showNotificacionesDialog by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableStateOf("Perfil") }

    val cartCount by remember { derivedStateOf { CartManager.cartItems.sumOf { it.quantity } } }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
                val selectedColor = Color(0xFFE53935)
                NavigationBarItem(
                    selected = selectedTab == "Inicio",
                    onClick = {
                        selectedTab = "Inicio"
                        val activity = context as? Activity
                        activity?.finish()
                        context.startActivity(Intent(context, HomeActivity::class.java))
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = Color(0xFF212121),
                        unselectedTextColor = Color(0xFF212121)
                    ),
                    icon = {
                        val icon = if (selectedTab == "Inicio") Icons.Filled.Home else Icons.Outlined.Home
                        Icon(icon, contentDescription = "Inicio")
                    },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Categorias",
                    onClick = {
                        selectedTab = "Categorias"
                        context.startActivity(Intent(context, CatalogActivity::class.java))
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = Color(0xFF212121),
                        unselectedTextColor = Color(0xFF212121)
                    ),
                    icon = {
                        val icon = if (selectedTab == "Categorias") Icons.Filled.Category else Icons.Outlined.Category
                        Icon(icon, contentDescription = "Categorías")
                    },
                    label = { Text("Categorías") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Carrito",
                    onClick = {
                        selectedTab = "Carrito"
                        context.startActivity(Intent(context, Carrito::class.java))
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = Color(0xFF212121),
                        unselectedTextColor = Color(0xFF212121)
                    ),
                    icon = {
                        val icon = if (selectedTab == "Carrito") Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge {
                                        Text(cartCount.toString(), fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(icon, contentDescription = "Carrito")
                        }
                    },
                    label = { Text("Carrito") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Favoritos",
                    onClick = {
                        selectedTab = "Favoritos"
                        context.startActivity(Intent(context, FavActivity::class.java))
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = Color(0xFF212121),
                        unselectedTextColor = Color(0xFF212121)
                    ),
                    icon = {
                        val icon = if (selectedTab == "Favoritos") Icons.Filled.Favorite else Icons.Outlined.Favorite
                        Icon(icon, contentDescription = "Favoritos")
                    },
                    label = { Text("Favoritos") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Perfil",
                    onClick = {
                        selectedTab = "Perfil"
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = Color(0xFF212121),
                        unselectedTextColor = Color(0xFF212121)
                    ),
                    icon = {
                        val icon = if (selectedTab == "Perfil") Icons.Filled.Person else Icons.Outlined.Person
                        Icon(icon, contentDescription = "Perfil")
                    },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color.White, Color(0xFFEAEAEA))))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // 🟣 FOTO + NOMBRE Y CORREO EN UNA FILA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar a la izquierda
                Box(
                    modifier = Modifier
                        .size(96.dp)
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
                                modifier = Modifier.size(96.dp).clip(CircleShape)
                            )
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(R.drawable.ic_person),
                                contentDescription = "Foto de perfil",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(96.dp).clip(CircleShape)
                            )
                        }
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(user?.photoUrl ?: R.drawable.ic_person),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(96.dp).clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Nombre + editar + correo a la derecha
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF212121)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = {
                            val intent = Intent(context, EditarPerfilActivity::class.java)
                            context.startActivity(intent)
                        }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar perfil",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(email, fontSize = 14.sp, color = Color(0xFF616161))
                }
            }

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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tienes pedidos aún", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                pedidos.value.forEach { pedido ->
                    val colorEstado = when (pedido.estado) {
                        "Entregado" -> Color(0xFF4CAF50)
                        "En tránsito" -> Color(0xFF3F51B5)
                        else -> Color(0xFFFFC107)
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

            ProfileOptionCard(Icons.Default.RateReview, "Reseñas", "Ver tus reseñas") {
                val intent = Intent(context, MisResenasActivity::class.java)
                context.startActivity(intent)
            }

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
                intent.putExtra("direccionPerfil", direccion)
                context.startActivity(intent)
            }

            ProfileOptionCard(Icons.Default.Info, "Términos y políticas legales", "Revisa nuestras políticas") {
                val intent = Intent(context, TerminosLegalesActivity::class.java)
                context.startActivity(intent)
            }

            // 🟣 CERRAR SESIÓN
            ProfileOptionCard(Icons.AutoMirrored.Filled.ExitToApp, "Cerrar Sesión", "Salir de tu cuenta") {
                FavoritesManager.clearFavorites()
                Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
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

    if (showChangePasswordDialog) CambiarContrasenaDialog(onDismiss = { showChangePasswordDialog = false })
    if (showNotificacionesDialog) PreferenciasNotificacionesDialog(onDismiss = { showNotificacionesDialog = false })
}

// ✅ COMPONENTES REUTILIZABLES
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color(0xFF616161))
        Text(value, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
    }
}

// ✅ SE AGREGA BOTÓN "VER SEGUIMIENTO" AL PEDIDO
@Composable
fun PedidoItem(codigo: String, estado: String, colorEstado: Color, precio: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val intent = Intent(context, SeguimientoPedidoActivity::class.java)
                    intent.putExtra("codigo_pedido", codigo)
                    context.startActivity(intent)
                },
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107))
            ) {
                Text("Ver seguimiento", color = Color.Black, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ProfileOptionCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
