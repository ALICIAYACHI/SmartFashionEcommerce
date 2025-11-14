package com.ropa.smartfashionecommerce.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import coil.compose.AsyncImage
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.catalog.CatalogActivity
import com.ropa.smartfashionecommerce.detalles.ProductDetailActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import com.ropa.smartfashionecommerce.miperfil.ProfileImageManager
import com.ropa.smartfashionecommerce.model.Producto
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import kotlinx.coroutines.launch

val localProducts = listOf(
    Producto(1, "Blusa Elegante Negra", "89.90", localImageRes = R.drawable.blusaelegante),
    Producto(2, "Vestido Dorado Noche", "159.90", localImageRes = R.drawable.vestidodorado),
    Producto(3, "Casaca Moderna", "120.00", localImageRes = R.drawable.casaca),
    Producto(4, "Pantalón Beige", "110.00", localImageRes = R.drawable.pantalonbeige),
    Producto(5, "Camisa Blanca", "95.00", localImageRes = R.drawable.camisablanca),
    Producto(6, "Vestido Floral", "150.00", localImageRes = R.drawable.vestidofloral)
)

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FavoritesManager.initialize(this)
        setContent {
            SmartFashionEcommerceTheme {
                FashionHomeScreen(activity = this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ProfileImageManager.loadProfileImage(this)
    }
}

@Composable
fun FashionHomeScreen(activity: ComponentActivity) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Home") }

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val profileImageUri by remember { ProfileImageManager.profileImageUri }

    // ✅ CORREGIDO: Obtener usuario actual de Firebase
    val firebaseUser = Firebase.auth.currentUser
    val googlePhotoUrl = firebaseUser?.photoUrl

    LaunchedEffect(Unit) {
        ProfileImageManager.loadProfileImage(context)
    }

    LaunchedEffect(Unit) {
        try {
            val response = ApiClient.apiService.getProductos()
            if (response.isSuccessful) {
                val apiProducts = response.body()?.data.orEmpty()
                productos = if (apiProducts.isNotEmpty()) apiProducts else localProducts
            } else {
                productos = localProducts
                Toast.makeText(context, "Error al cargar desde la API. Usando productos locales.", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            productos = localProducts
            Toast.makeText(context, "Sin conexión. Mostrando productos locales.", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
                NavigationBarItem(
                    selected = selectedTab == "Home",
                    onClick = { selectedTab = "Home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color(0xFF212121)) },
                    label = { Text("Home", color = Color(0xFF212121)) }
                )
                NavigationBarItem(
                    selected = selectedTab == "Cart",
                    onClick = {
                        selectedTab = "Cart"
                        activity.startActivity(Intent(activity, Carrito::class.java))
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = Color(0xFF212121)) },
                    label = { Text("Carrito", color = Color(0xFF212121)) }
                )
                NavigationBarItem(
                    selected = selectedTab == "Favorites",
                    onClick = {
                        selectedTab = "Favorites"
                        activity.startActivity(Intent(activity, FavActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos", tint = Color(0xFF212121)) },
                    label = { Text("Favoritos", color = Color(0xFF212121)) }
                )
                NavigationBarItem(
                    selected = selectedTab == "Profile",
                    onClick = {
                        selectedTab = "Profile"
                        activity.startActivity(Intent(activity, MiPerfilActivity::class.java))
                    },
                    icon = {
                        when {
                            profileImageUri != null -> {
                                val bitmap = ProfileImageManager.getBitmapFromUri(context, profileImageUri!!)
                                if (bitmap != null) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFF212121), CircleShape)
                                    )
                                } else if (googlePhotoUrl != null) {
                                    AsyncImage(
                                        model = googlePhotoUrl,
                                        contentDescription = "Perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFF212121), CircleShape)
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color(0xFF212121))
                                }
                            }
                            googlePhotoUrl != null -> {
                                AsyncImage(
                                    model = googlePhotoUrl,
                                    contentDescription = "Perfil",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color(0xFF212121), CircleShape)
                                )
                            }
                            else -> {
                                Icon(Icons.Default.Person, contentDescription = "Perfil", tint = Color(0xFF212121))
                            }
                        }
                    },
                    label = { Text("Perfil", color = Color(0xFF212121)) }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.imagendehomeprincipal),
                    contentDescription = "Banner",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Text(
                    text = "SMART FASHION",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Categorías",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF212121),
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            val categories = listOf("VERANO", "INVIERNO", "PRIMAVERA", "OTOÑO", "DEPORTIVO")
            LazyRow(modifier = Modifier.padding(start = 16.dp, bottom = 16.dp)) {
                items(categories) { category ->
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE0E0E0))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable {
                                if (category == "VERANO") {
                                    activity.startActivity(Intent(activity, CatalogActivity::class.java))
                                }
                            }
                    ) {
                        Text(category, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF212121))
                    }
                }
            }

            Text(
                "Productos",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF212121),
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(productos) { producto ->
                        ProductCard(producto)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCard(producto: Producto) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp)
    ) {
        Column {
            if (producto.image_preview.isNullOrEmpty()) {
                producto.localImageRes?.let { img ->
                    Image(
                        painter = painterResource(id = img),
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .height(180.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { showMenu = true },
                                    onTap = {
                                        val intent = Intent(context, ProductDetailActivity::class.java).apply {
                                            putExtra("productName", producto.nombre)
                                            putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                                            putExtra("imageType", "local")
                                            putExtra("productImageRes", img)
                                        }
                                        context.startActivity(intent)
                                    }
                                )
                            }
                    )
                }
            } else {
                AsyncImage(
                    model = producto.image_preview,
                    contentDescription = producto.nombre,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { showMenu = true },
                                onTap = {
                                    val intent = Intent(context, ProductDetailActivity::class.java).apply {
                                        putExtra("productName", producto.nombre)
                                        putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                                        putExtra("imageType", "url")
                                        putExtra("productImageUrl", producto.image_preview)
                                    }
                                    context.startActivity(intent)
                                }
                            )
                        }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(producto.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF212121))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("S/ ${producto.precio}", color = Color(0xFF424242), fontSize = 13.sp)
                IconButton(
                    onClick = {
                        val intent = Intent(context, ProductDetailActivity::class.java).apply {
                            putExtra("productName", producto.nombre)
                            putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                            if (producto.image_preview.isNullOrEmpty()) {
                                putExtra("imageType", "local")
                                putExtra("productImageRes", producto.localImageRes ?: R.drawable.modelo_ropa)
                            } else {
                                putExtra("imageType", "url")
                                putExtra("productImageUrl", producto.image_preview)
                            }
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Ir a detalles", tint = Color(0xFF505050))
                }
            }
        }

        if (showMenu) {
            Popup(alignment = Alignment.Center) {
                Card(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(0.8f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(8.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        MenuItem(Icons.Default.Visibility, "Ver detalles") {
                            showMenu = false
                            val intent = Intent(context, ProductDetailActivity::class.java).apply {
                                putExtra("productName", producto.nombre)
                                putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                                if (producto.image_preview.isNullOrEmpty()) {
                                    putExtra("imageType", "local")
                                    putExtra("productImageRes", producto.localImageRes ?: R.drawable.modelo_ropa)
                                } else {
                                    putExtra("imageType", "url")
                                    putExtra("productImageUrl", producto.image_preview)
                                }
                            }
                            context.startActivity(intent)
                        }

                        MenuItem(Icons.Default.Favorite, "Agregar a favoritos") {
                            showMenu = false
                            val favoriteItem = FavoriteItem(
                                id = producto.id,
                                name = producto.nombre,
                                price = "S/ ${producto.precio}",
                                sizes = listOf("S", "M", "L"),
                                imageRes = producto.localImageRes ?: R.drawable.modelo_ropa,
                                isFavorite = true
                            )
                            FavoritesManager.addFavorite(context, favoriteItem)
                            Toast.makeText(context, "Agregado a favoritos ❤️", Toast.LENGTH_SHORT).show()
                        }

                        MenuItem(Icons.AutoMirrored.Filled.Chat, "Consultar por WhatsApp") {
                            showMenu = false
                            val url = "https://wa.me/?text=${producto.nombre} - ${producto.precio}"
                            val whatsappIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(whatsappIntent)
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(onClick = { showMenu = false }) {
                            Text("Cerrar", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text, tint = Color(0xFF212121), modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, color = Color(0xFF212121), fontSize = 15.sp)
    }
}
