package com.ropa.smartfashionecommerce.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ropa.smartfashionecommerce.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.carrito.CartItem
import com.ropa.smartfashionecommerce.carrito.CartManager
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import com.ropa.smartfashionecommerce.miperfil.ProfileImageManager
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import com.ropa.smartfashionecommerce.catalog.CatalogActivity
import com.ropa.smartfashionecommerce.maps.MapsActivity
import androidx.compose.material.icons.outlined.LocationOn

class FavActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FavoritesManager.initialize(this)
        setContent {
            SmartFashionEcommerceTheme {
                FavApp(activity = this)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavApp(activity: ComponentActivity) {
    val context = LocalContext.current
    val favoriteItems = remember { FavoritesManager.favoriteItems }

    var selectedTab by remember { mutableStateOf("Favoritos") }

    val cartCount by remember { derivedStateOf { CartManager.cartItems.sumOf { it.quantity } } }

    val profileImageUri by remember { ProfileImageManager.profileImageUri }
    val firebaseUser = Firebase.auth.currentUser
    val googlePhotoUrl = firebaseUser?.photoUrl

    LaunchedEffect(Unit) {
        ProfileImageManager.loadProfileImage(context)
    }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
                val selectedColor = Color(0xFFE53935)
                NavigationBarItem(
                    selected = selectedTab == "Inicio",
                    onClick = {
                        selectedTab = "Inicio"
                        activity.finish()
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
                        activity.startActivity(Intent(activity, CatalogActivity::class.java))
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
                        activity.startActivity(Intent(activity, Carrito::class.java))
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
                        activity.startActivity(Intent(activity, MiPerfilActivity::class.java))
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = Color(0xFF212121),
                        unselectedTextColor = Color(0xFF212121)
                    ),
                    icon = {
                        when {
                            profileImageUri != null -> {
                                val bitmap = ProfileImageManager.getBitmapFromUri(context, profileImageUri!!)
                                if (bitmap != null) {
                                    androidx.compose.foundation.Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "Perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFF212121), CircleShape)
                                    )
                                } else if (googlePhotoUrl != null) {
                                    coil.compose.AsyncImage(
                                        model = googlePhotoUrl,
                                        contentDescription = "Perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .border(1.dp, Color(0xFF212121), CircleShape)
                                    )
                                } else {
                                    val icon = if (selectedTab == "Perfil") Icons.Filled.Person else Icons.Outlined.Person
                                    Icon(icon, contentDescription = "Perfil")
                                }
                            }
                            googlePhotoUrl != null -> {
                                coil.compose.AsyncImage(
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
                                val icon = if (selectedTab == "Perfil") Icons.Filled.Person else Icons.Outlined.Person
                                Icon(icon, contentDescription = "Perfil")
                            }
                        }
                    },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF9F9F9))
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)) {
                Text(
                    "Mis Favoritos 💖",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    "${favoriteItems.size} productos guardados",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (favoriteItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No tienes productos en favoritos 😢",
                        color = Color.Gray,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteItems) { item ->
                        FavoriteProductCard(
                            item = item,
                            onFavoriteClick = {
                                FavoritesManager.removeFavorite(context, item)
                                Toast.makeText(context, "Eliminado de favoritos ❌", Toast.LENGTH_SHORT).show()
                            },
                            onAddToCart = { selectedItem ->
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user == null) {
                                    Toast.makeText(context, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
                                    context.startActivity(Intent(context, com.ropa.smartfashionecommerce.DarkLoginActivity::class.java))
                                } else {
                                    val priceValue = selectedItem.price.replace("S/", "").trim().toDoubleOrNull() ?: 0.0
                                    val cartItem = CartItem(
                                        name = selectedItem.name,
                                        size = "M",
                                        color = "Negro",
                                        quantity = 1,
                                        price = priceValue,
                                        imageRes = selectedItem.imageRes,
                                        imageUrl = selectedItem.imageUrl
                                    )
                                    CartManager.addItem(cartItem)
                                    CartManager.saveCart(context)
                                    Toast.makeText(context, "Agregado al carrito 🛍️", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FavoriteProductCard(
    item: FavoriteItem,
    onFavoriteClick: (FavoriteItem) -> Unit,
    onAddToCart: (FavoriteItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Imagen + botón corazón
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White, Color(0xFFF8F8F8))
                        )
                    )
            ) {
                val model: Any? = item.imageUrl ?: item.imageRes
                if (model != null) {
                    coil.compose.AsyncImage(
                        model = model,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                // Botón flotante dentro de la imagen
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.8f))
                        .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                ) {
                    IconButton(
                        onClick = { onFavoriteClick(item) },
                        modifier = Modifier.align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = "Eliminar de favoritos",
                            tint = Color.Red,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Texto y botón agregar
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121),
                    maxLines = 2
                )
                Text(
                    text = item.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BCD4)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { onAddToCart(item) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agregar al carrito", color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}
