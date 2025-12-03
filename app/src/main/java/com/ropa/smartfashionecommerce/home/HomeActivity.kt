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
import androidx.compose.ui.focus.onFocusChanged
import coil.compose.AsyncImage
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.carrito.CartManager
import com.ropa.smartfashionecommerce.catalog.CatalogActivity
import com.ropa.smartfashionecommerce.detalles.ProductDetailActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import com.ropa.smartfashionecommerce.miperfil.ProfileImageManager
import com.ropa.smartfashionecommerce.model.Producto
import com.ropa.smartfashionecommerce.model.Categoria
import com.ropa.smartfashionecommerce.network.ApiClient
import com.google.firebase.firestore.FirebaseFirestore
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import kotlinx.coroutines.launch
import com.ropa.smartfashionecommerce.maps.MapsActivity
import androidx.compose.material.icons.outlined.LocationOn
import com.ropa.smartfashionecommerce.chat.ChatFAB





val localProducts = listOf(
    Producto(1, "Blusa Elegante Negra", "43.76", categoria = Categoria(1, "Mujer"), localImageRes = R.drawable.blusaelegante),
    Producto(2, "Vestido Dorado Noche", "43.59", categoria = Categoria(1, "Mujer"), localImageRes = R.drawable.vestidodorado),
    Producto(3, "Casaca Moderna", "59.66", categoria = Categoria(2, "Hombre"), localImageRes = R.drawable.casaca),
    Producto(4, "Pantalón Beige", "45.77", categoria = Categoria(2, "Hombre"), localImageRes = R.drawable.pantalonbeige),
    Producto(5, "Camisa Blanca", "36.74", categoria = Categoria(2, "Hombre"), localImageRes = R.drawable.camisablanca),
    Producto(6, "Vestido Floral", "38.86", categoria = Categoria(1, "Mujer"), localImageRes = R.drawable.vestidofloral),
    // Ofertas Black Friday (solo online)
    Producto(7, "Sudadera Oversize", "41.74", descripcion = "Moda", categoria = Categoria(3, "Black Friday"), image_preview = "https://www.desire.pe/cdn/shop/files/CRI_2554_398b7b94-a0a2-4dc5-be42-30995f0c04e2.png?v=1726679347"),
    Producto(8, "Vestido Aire Barcelona", "28.30", descripcion = "Moda", categoria = Categoria(3, "Black Friday"), image_preview = "https://d23ye9eewymoys.cloudfront.net/colecciones/web/1c45-miren-aire-barcelona-1-thumb.jpg"),
    Producto(9, "Outfit Casual Mujer", "62.06", descripcion = "Moda", categoria = Categoria(3, "Black Friday"), image_preview = "https://i.pinimg.com/564x/6a/59/49/6a5949963b7705a7c3927c044b2f4c38.jpg"),
    Producto(10, "Vestido Casual", "49.96", descripcion = "Moda", categoria = Categoria(3, "Black Friday"), image_preview = "https://i.pinimg.com/236x/e2/04/25/e20425efb02a5185ba8f4d1cd710183d.jpg"),
    // Niños
    Producto(11, "Camisa Casual Niño", "28.98", categoria = Categoria(4, "Niños"), image_preview = "https://hushpuppiespe.vtexassets.com/arquivos/ids/348758/https---s3.amazonaws.com-ecom-imagenes.forus-digital.xyz.peru-HUSHPUPPIESKIDS-HK211021504_287_1.jpg?v=638604628092130000"),
    Producto(12, "Casaca Abrigadora Niño", "63.82", categoria = Categoria(4, "Niños"), image_preview = "https://media.falabella.com/falabellaPE/883289216_001/w=800,h=800,fit=pad"),
    Producto(13, "Pantalón Niño Denim", "49.47", categoria = Categoria(4, "Niños"), image_preview = "https://hushpuppiespe.vtexassets.com/arquivos/ids/336908-800-auto?v=638446729033670000&width=800&height=auto&aspect=true"),
    // Bebé
    Producto(14, "Conjunto Bebé Niño", "48.88", categoria = Categoria(5, "Bebé"), image_preview = "https://img.kwcdn.com/product/Fancyalgo/VirtualModelMatting/fb03dd58d895a6436b3cfee2b5d7d766.jpg?imageMogr2/auto-orient%7CimageView2/2/w/800/q/70/format/webp"),
    Producto(15, "Conjunto Bebé Niña", "40.30", categoria = Categoria(5, "Bebé"), image_preview = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQmGawIE-1oJ5wtsBQ9p9DFelbIJtmzeJd8ga0iA6SW3gaTX_-VHNCs02I33J5WvOiAe0s&usqp=CAU")
)

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FavoritesManager.initialize(this)
        CartManager.initialize(this)
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

// Cargar promedio de rating y cantidad de reseñas desde Firestore para un producto
private fun loadProductRating(
    productId: String,
    onResult: (Float, Int) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("products")
        .document(productId)
        .collection("reviews")
        .get()
        .addOnSuccessListener { snapshot ->
            val ratings = snapshot.documents.mapNotNull { doc ->
                doc.getLong("rating")?.toInt()
            }

            if (ratings.isNotEmpty()) {
                val avg = ratings.average().toFloat()
                onResult(avg, ratings.size)
            } else {
                onResult(0f, 0)
            }
        }
        .addOnFailureListener {
            onResult(0f, 0)
        }
}

@Composable
fun FashionHomeScreen(activity: ComponentActivity) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf("Inicio") }

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var apiCategories by remember { mutableStateOf<List<Categoria>>(emptyList()) }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedBFSubcategory by remember { mutableStateOf("Todos") }
    var selectedCircleFilter by remember { mutableStateOf("Todos") }
    var searchHasFocus by remember { mutableStateOf(false) }
    var recentSearches by remember { mutableStateOf(listOf<String>()) }

    val profileImageUri by remember { ProfileImageManager.profileImageUri }

    val cartCount by remember { derivedStateOf { CartManager.cartItems.sumOf { it.quantity } } }

    // ✅ CORREGIDO: Obtener usuario actual de Firebase
    val firebaseUser = Firebase.auth.currentUser
    val googlePhotoUrl = firebaseUser?.photoUrl

    LaunchedEffect(Unit) {
        ProfileImageManager.loadProfileImage(context)
    }

    // Cargar productos desde /api/home/ y aplicar filtros según tab y circulitos
    LaunchedEffect(selectedCategory, selectedCircleFilter, searchText) {
        isLoading = true
        try {
            // Determinar category_id a partir de las categorías devueltas por la API
            val categoryId: Int? = when (selectedCategory) {
                // En "Todos" no filtramos por categoría
                "Todos" -> null
                else -> apiCategories.firstOrNull { it.nombre.equals(selectedCategory, ignoreCase = true) }?.id
            }

            // Determinar término de búsqueda según circulito
            val circleTerm: String? = when {
                // En "Todos" ignoramos los circulitos
                selectedCategory == "Todos" -> null
                selectedCircleFilter == "Todos" -> null
                selectedCircleFilter == "Vestidos" -> "Vestido"
                selectedCircleFilter == "Camisas" -> "Camisa"
                selectedCircleFilter == "Casacas" -> "Casaca"
                selectedCircleFilter == "Polos" -> "Polo"
                selectedCircleFilter == "Pantalones" -> "Pantalón"
                selectedCircleFilter == "Ofertas" -> "Oferta"
                else -> selectedCircleFilter
            }

            // Combinar texto escrito con el filtro del circulito
            val manualQuery = searchText.trim().takeIf { it.isNotEmpty() }
            val finalQuery = listOfNotNull(manualQuery, circleTerm)
                .joinToString(" ")
                .ifBlank { null }

            val response = ApiClient.apiService.getHome(
                categoryId = categoryId,
                query = finalQuery,
                sizeId = null,
                colorId = null,
                page = 1,
                // Subimos el límite para aproximarnos a "todos" los productos del web
                limit = 100
            )

            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                apiCategories = data?.categories.orEmpty()
                val apiProducts = data?.featured_products.orEmpty()
                productos = if (apiProducts.isNotEmpty()) apiProducts else localProducts
            } else {
                // Si falla la API, mantenemos el fallback local
                productos = localProducts
                Toast.makeText(context, "Error al cargar desde la API (home). Usando productos locales.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            productos = localProducts
            Toast.makeText(
                context,
                "Error de red: ${e.localizedMessage ?: "desconocido"}. Mostrando productos locales.",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            ChatFAB(modifier = Modifier.padding(bottom = 16.dp, end = 16.dp))
        },
        floatingActionButtonPosition = FabPosition.End,
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 4.dp) {
                val selectedColor = Color(0xFFE53935)

                NavigationBarItem(
                    selected = selectedTab == "Inicio",
                    onClick = { selectedTab = "Inicio" },
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
                    selected = false,
                    onClick = {
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
                    selected = false,
                    onClick = {
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
                    selected = false,
                    onClick = {
                        activity.startActivity(Intent(activity, FavActivity::class.java))
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            searchHasFocus = focusState.isFocused
                        },
                    placeholder = { Text("Buscar productos", color = Color(0xFF9E9E9E)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black,
                        focusedBorderColor = Color(0xFF212121),
                        unfocusedBorderColor = Color(0xFFBDBDBD),
                        focusedPlaceholderColor = Color(0xFF9E9E9E),
                        unfocusedPlaceholderColor = Color(0xFF9E9E9E)
                    ),
                    trailingIcon = {
                        if (searchText.isNotEmpty()) {
                            IconButton(onClick = { searchText = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Borrar texto",
                                    tint = Color(0xFF616161)
                                )
                            }
                        }
                    },
                    // Detectar foco para mostrar búsquedas recientes
                    supportingText = null,
                )
                IconButton(
                    onClick = {
                        val term = searchText.trim()
                        if (term.isNotEmpty() && term !in recentSearches) {
                            recentSearches = (listOf(term) + recentSearches).take(5)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = Color(0xFF212121)
                    )
                }
                IconButton(
                    onClick = {
                        activity.startActivity(Intent(activity, MapsActivity::class.java))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Ver tienda",
                        tint = Color(0xFF212121)
                    )
                }
            }

            // Búsquedas recientes (solo cuando el buscador tiene foco)
            if (recentSearches.isNotEmpty() && searchHasFocus) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Búsquedas recientes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF212121)
                        )
                        IconButton(onClick = { recentSearches = emptyList(); searchText = "" }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Borrar historial",
                                tint = Color(0xFF757575),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    recentSearches.forEach { term ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchText = term
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF616161),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = term,
                                fontSize = 13.sp,
                                color = Color(0xFF424242)
                            )
                        }
                    }
                }
            }

            val categoriesTabs = listOf("Todos", "Black Friday", "Mujer", "Hombre", "Niños", "Bebé")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                items(categoriesTabs) { category ->
                    val isSelected = selectedCategory == category
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color(0xFF212121) else Color(0xFFE0E0E0))
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color(0xFF212121),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Circulitos de filtro por tipo de producto según categoría
            if (selectedCategory in listOf("Mujer", "Hombre")) {
                Spacer(modifier = Modifier.height(4.dp))

                data class CircleFilter(val label: String, val iconUrl: String)

                val circleFilters = when (selectedCategory) {
                    "Mujer" -> listOf(
                        CircleFilter("Todos", "https://i.pinimg.com/564x/6a/59/49/6a5949963b7705a7c3927c044b2f4c38.jpg"),
                        CircleFilter("Vestidos", "https://i.pinimg.com/236x/e2/04/25/e20425efb02a5185ba8f4d1cd710183d.jpg"),
                        CircleFilter("Camisas", "https://images.pexels.com/photos/7697309/pexels-photo-7697309.jpeg?auto=compress&cs=tinysrgb&w=600"),
                        CircleFilter("Pantalones", "https://oggi.mx/cdn/shop/files/ATRACTIONGABAKHAKIVISTA2.jpg?v=1753209029"),
                        CircleFilter("Casacas", "https://cuerosvelezpe.vtexassets.com/arquivos/ids/358118/1035983-02-01--Chaqueta-ebro.jpg?v=638482126287130000"),
                    )
                    "Hombre" -> listOf(
                        CircleFilter("Todos", "https://images.pexels.com/photos/428340/pexels-photo-428340.jpeg?auto=compress&cs=tinysrgb&w=600"),
                        CircleFilter("Casacas", "https://cuerosvelezpe.vtexassets.com/arquivos/ids/358118/1035983-02-01--Chaqueta-ebro.jpg?v=638482126287130000"),
                        CircleFilter("Pantalones", "https://oggi.mx/cdn/shop/files/ATRACTIONGABAKHAKIVISTA2.jpg?v=1753209029"),
                        CircleFilter("Polos", "https://images.pexels.com/photos/428340/pexels-photo-428340.jpeg?auto=compress&cs=tinysrgb&w=600"),
                    )
                    else -> emptyList()
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(circleFilters) { item ->
                        val isSelected = selectedCircleFilter == item.label
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { selectedCircleFilter = item.label }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF212121) else Color(0xFFE0E0E0))
                            ) {
                                AsyncImage(
                                    model = item.iconUrl,
                                    contentDescription = item.label,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.label,
                                fontSize = 12.sp,
                                color = if (isSelected) Color(0xFF212121) else Color(0xFF616161)
                            )
                        }
                    }
                }
            }

            if (selectedCategory == "Black Friday") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFB71C1C))
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Ofertas de BLACK FRIDAY",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )

                    val blackFridayProducts = productos.filter {
                        it.categoria?.nombre.equals("Black Friday", ignoreCase = true)
                    }

                    val carouselProducts = blackFridayProducts.filter { producto ->
                        when (selectedBFSubcategory) {
                            "Todos" -> true
                            else -> producto.descripcion.equals(selectedBFSubcategory, ignoreCase = true)
                        }
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(carouselProducts) { producto ->
                            Column(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.White)
                                    .clickable {
                                        val intent = Intent(context, ProductDetailActivity::class.java).apply {
                                            putExtra("productId", producto.id)
                                            putExtra("productName", producto.nombre)
                                            putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                                            putExtra("productDescription", producto.descripcion)
                                            putExtra("productStock", producto.stock_total)
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
                                    .padding(6.dp)
                            ) {
                                if (producto.image_preview.isNullOrEmpty()) {
                                    producto.localImageRes?.let { img ->
                                        Image(
                                            painter = painterResource(id = img),
                                            contentDescription = producto.nombre,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .height(90.dp)
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    }
                                } else {
                                    AsyncImage(
                                        model = producto.image_preview,
                                        contentDescription = producto.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .height(90.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = producto.nombre,
                                    fontSize = 12.sp,
                                    maxLines = 2,
                                    color = Color(0xFF212121)
                                )
                                Text(
                                    text = "S/ ${producto.precio}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                                Text(
                                    text = "-60%",
                                    fontSize = 11.sp,
                                    color = Color(0xFFFF6F00)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ofertas de Black Friday",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF212121),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )

                data class BFSubcategory(val label: String, val iconUrl: String)
                val bfSubcategories = listOf(
                    BFSubcategory("Todos", "https://d23ye9eewymoys.cloudfront.net/colecciones/web/1c45-miren-aire-barcelona-1-thumb.jpg"),
                    BFSubcategory("Moda", "https://i.pinimg.com/564x/6a/59/49/6a5949963b7705a7c3927c044b2f4c38.jpg")
                )
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    items(bfSubcategories) { item ->
                        val isSelected = selectedBFSubcategory == item.label
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) Color(0xFF212121) else Color(0xFFF5F5F5))
                                .clickable { selectedBFSubcategory = item.label }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = item.iconUrl,
                                    contentDescription = item.label,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.label,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color.White else Color(0xFF424242)
                                )
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // El backend ya aplica categoría y texto (q). Aquí solo refinamos por búsqueda local.
                val filteredProducts = productos
                    .filter { producto ->
                        if (searchText.isBlank()) true
                        else producto.nombre.contains(searchText, ignoreCase = true)
                    }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxHeight()
                ) {
                    items(filteredProducts) { producto ->
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

    var averageRating by remember { mutableStateOf(0f) }
    var reviewCount by remember { mutableStateOf(0) }

    // Cargar promedio de reseñas para este producto (mismo ID que en ProductDetail: nombre)
    LaunchedEffect(producto.nombre) {
        loadProductRating(
            productId = producto.nombre,
            onResult = { avg, count ->
                averageRating = avg
                reviewCount = count
            }
        )
    }

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
                                            putExtra("productId", producto.id)
                                            putExtra("productName", producto.nombre)
                                            putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                                            putExtra("productDescription", producto.descripcion)
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
                                        putExtra("productId", producto.id)
                                        putExtra("productName", producto.nombre)
                                        putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                                        putExtra("productDescription", producto.descripcion)
                                        putExtra("productStock", producto.stock_total)
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

            if (reviewCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { index ->
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            tint = if (index < averageRating.toInt()) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "%.1f".format(averageRating),
                        fontSize = 11.sp,
                        color = Color(0xFF616161),
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("S/ ${producto.precio}", color = Color(0xFF424242), fontSize = 13.sp)
                IconButton(
                    onClick = {
                        val intent = Intent(context, ProductDetailActivity::class.java).apply {
                            putExtra("productId", producto.id)
                            putExtra("productName", producto.nombre)
                            putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                            putExtra("productDescription", producto.descripcion)
                            putExtra("productStock", producto.stock_total)
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
                                putExtra("productId", producto.id)
                                putExtra("productName", producto.nombre)
                                putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                                putExtra("productDescription", producto.descripcion)
                                putExtra("productStock", producto.stock_total)
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

                            val currentUser = Firebase.auth.currentUser
                            if (currentUser == null) {
                                Toast.makeText(context, "Inicia sesión para guardar favoritos", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(context, com.ropa.smartfashionecommerce.DarkLoginActivity::class.java))
                            } else {
                                val favoriteItem = FavoriteItem(
                                    id = producto.id,
                                    name = producto.nombre,
                                    price = "S/ ${producto.precio}",
                                    sizes = listOf("S", "M", "L"),
                                    imageRes = producto.localImageRes ?: R.drawable.modelo_ropa,
                                    imageUrl = producto.image_preview,
                                    isFavorite = true
                                )
                                FavoritesManager.addFavorite(context, favoriteItem)
                                Toast.makeText(context, "Agregado a favoritos ❤️", Toast.LENGTH_SHORT).show()
                            }
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
