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
import androidx.compose.ui.focus.onFocusChanged
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
import com.ropa.smartfashionecommerce.model.Categoria
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.ui.theme.SmartFashionEcommerceTheme
import kotlinx.coroutines.launch

val localProducts = listOf(
    Producto(1, "Blusa Elegante Negra", "89.90", categoria = Categoria(1, "Mujer"), localImageRes = R.drawable.blusaelegante),
    Producto(2, "Vestido Dorado Noche", "159.90", categoria = Categoria(1, "Mujer"), localImageRes = R.drawable.vestidodorado),
    Producto(3, "Casaca Moderna", "120.00", categoria = Categoria(2, "Hombre"), localImageRes = R.drawable.casaca),
    Producto(4, "Pantalón Beige", "110.00", categoria = Categoria(2, "Hombre"), localImageRes = R.drawable.pantalonbeige),
    Producto(5, "Camisa Blanca", "95.00", categoria = Categoria(2, "Hombre"), localImageRes = R.drawable.camisablanca),
    Producto(6, "Vestido Floral", "150.00", categoria = Categoria(1, "Mujer"), localImageRes = R.drawable.vestidofloral),
    // Ofertas Black Friday (solo online)
    Producto(7, "Sudadera Oversize", "120.00", descripcion = "Moda", categoria = Categoria(3, "Black Friday"), image_preview = "https://www.desire.pe/cdn/shop/files/CRI_2554_398b7b94-a0a2-4dc5-be42-30995f0c04e2.png?v=1726679347"),
    Producto(8, "Vestido Aire Barcelona", "249.90", descripcion = "Moda", categoria = Categoria(3, "Black Friday"), image_preview = "https://d23ye9eewymoys.cloudfront.net/colecciones/web/1c45-miren-aire-barcelona-1-thumb.jpg"),
    Producto(9, "Outfit Casual Mujer", "139.90", descripcion = "Moda", categoria = Categoria(3, "Black Friday"), image_preview = "https://i.pinimg.com/564x/6a/59/49/6a5949963b7705a7c3927c044b2f4c38.jpg"),
    Producto(10, "Vestido Casual", "119.90", descripcion = "Moda", categoria = Categoria(3, "Black Friday"), image_preview = "https://i.pinimg.com/236x/e2/04/25/e20425efb02a5185ba8f4d1cd710183d.jpg"),
    Producto(11, "Zapatillas de Diseñador", "199.90", descripcion = "Calzado", categoria = Categoria(3, "Black Friday"), image_preview = "https://images.pexels.com/photos/18155790/pexels-photo-18155790/free-photo-of-moda-disenador-zapatos-estudio.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),
    Producto(12, "Zapatillas Blancas", "189.90", descripcion = "Calzado", categoria = Categoria(3, "Black Friday"), image_preview = "https://media.falabella.com/falabellaPE/115093604_01/w=800,h=800,fit=pad"),
    Producto(13, "Set Collar Dorado", "89.90", descripcion = "Accesorios", categoria = Categoria(3, "Black Friday"), image_preview = "https://m.media-amazon.com/images/I/51zKPZEEDlL._AC_UF1000,1000_QL80_.jpg"),
    Producto(14, "Set Pulseras Elegantes", "79.90", descripcion = "Accesorios", categoria = Categoria(3, "Black Friday"), image_preview = "https://m.media-amazon.com/images/I/71Pl18rqbHL._AC_UF1000,1000_QL80_.jpg"),
    // Niños
    Producto(15, "Camisa Casual Niño", "89.90", categoria = Categoria(4, "Niños"), image_preview = "https://hushpuppiespe.vtexassets.com/arquivos/ids/348758/https---s3.amazonaws.com-ecom-imagenes.forus-digital.xyz.peru-HUSHPUPPIESKIDS-HK211021504_287_1.jpg?v=638604628092130000"),
    Producto(16, "Casaca Abrigadora Niño", "129.90", categoria = Categoria(4, "Niños"), image_preview = "https://media.falabella.com/falabellaPE/883289216_001/w=800,h=800,fit=pad"),
    Producto(17, "Pantalón Niño Denim", "99.90", categoria = Categoria(4, "Niños"), image_preview = "https://hushpuppiespe.vtexassets.com/arquivos/ids/336908-800-auto?v=638446729033670000&width=800&height=auto&aspect=true"),
    // Bebé
    Producto(18, "Conjunto Bebé Niño", "79.90", categoria = Categoria(5, "Bebé"), image_preview = "https://img.kwcdn.com/product/Fancyalgo/VirtualModelMatting/fb03dd58d895a6436b3cfee2b5d7d766.jpg?imageMogr2/auto-orient%7CimageView2/2/w/800/q/70/format/webp"),
    Producto(19, "Conjunto Bebé Niña", "79.90", categoria = Categoria(5, "Bebé"), image_preview = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQmGawIE-1oJ5wtsBQ9p9DFelbIJtmzeJd8ga0iA6SW3gaTX_-VHNCs02I33J5WvOiAe0s&usqp=CAU")
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
    var selectedTab by remember { mutableStateOf("Inicio") }

    var productos by remember { mutableStateOf<List<Producto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var searchText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    var selectedBFSubcategory by remember { mutableStateOf("Todos") }
    var selectedCircleFilter by remember { mutableStateOf("Todos") }
    var searchHasFocus by remember { mutableStateOf(false) }
    var recentSearches by remember { mutableStateOf(listOf<String>()) }

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
                    selected = selectedTab == "Inicio",
                    onClick = { selectedTab = "Inicio" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio", tint = Color(0xFF212121)) },
                    label = { Text("Inicio", color = Color(0xFF212121)) }
                )
                NavigationBarItem(
                    selected = selectedTab == "Categorias",
                    onClick = {
                        selectedTab = "Categorias"
                        activity.startActivity(Intent(activity, CatalogActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Category, contentDescription = "Categorías", tint = Color(0xFF212121)) },
                    label = { Text("Categorías", color = Color(0xFF212121)) }
                )
                NavigationBarItem(
                    selected = selectedTab == "Carrito",
                    onClick = {
                        selectedTab = "Carrito"
                        activity.startActivity(Intent(activity, Carrito::class.java))
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito", tint = Color(0xFF212121)) },
                    label = { Text("Carrito", color = Color(0xFF212121)) }
                )
                NavigationBarItem(
                    selected = selectedTab == "Favoritos",
                    onClick = {
                        selectedTab = "Favoritos"
                        activity.startActivity(Intent(activity, FavActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoritos", tint = Color(0xFF212121)) },
                    label = { Text("Favoritos", color = Color(0xFF212121)) }
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
                    placeholder = { Text("Buscar productos") },
                    singleLine = true,
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

            if (selectedCategory in listOf("Mujer", "Hombre", "Niños", "Bebé")) {
                Spacer(modifier = Modifier.height(4.dp))

                data class CircleFilter(val label: String, val iconUrl: String)
                val circleFilters = listOf(
                    CircleFilter("Todos", "https://i.pinimg.com/564x/6a/59/49/6a5949963b7705a7c3927c044b2f4c38.jpg"),
                    CircleFilter("Ofertas", "https://d23ye9eewymoys.cloudfront.net/colecciones/web/1c45-miren-aire-barcelona-1-thumb.jpg"),
                    CircleFilter("Calzado", "https://images.pexels.com/photos/18155790/pexels-photo-18155790/free-photo-of-moda-disenador-zapatos-estudio.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),
                    CircleFilter("Vestidos", "https://i.pinimg.com/236x/e2/04/25/e20425efb02a5185ba8f4d1cd710183d.jpg"),
                    CircleFilter("Casacas", "https://cuerosvelezpe.vtexassets.com/arquivos/ids/358118/1035983-02-01--Chaqueta-ebro.jpg?v=638482126287130000")
                )

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
                    BFSubcategory("Moda", "https://i.pinimg.com/564x/6a/59/49/6a5949963b7705a7c3927c044b2f4c38.jpg"),
                    BFSubcategory("Calzado", "https://images.pexels.com/photos/18155790/pexels-photo-18155790/free-photo-of-moda-disenador-zapatos-estudio.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=1"),
                    BFSubcategory("Accesorios", "https://m.media-amazon.com/images/I/51zKPZEEDlL._AC_UF1000,1000_QL80_.jpg")
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
                val filteredProducts = productos
                    .filter { producto ->
                        when (selectedCategory) {
                            "Todos" -> true
                            "Black Friday" -> producto.categoria?.nombre.equals("Black Friday", ignoreCase = true)
                            "Mujer" -> producto.categoria?.nombre.equals("Mujer", ignoreCase = true)
                            "Hombre" -> producto.categoria?.nombre.equals("Hombre", ignoreCase = true)
                            "Niños" -> producto.categoria?.nombre.equals("Niños", ignoreCase = true)
                            "Bebé" -> producto.categoria?.nombre.equals("Bebé", ignoreCase = true)
                            else -> true
                        }
                    }
                    .filter { producto ->
                        if (searchText.isBlank()) true
                        else producto.nombre.contains(searchText, ignoreCase = true)
                    }
                    .filter { producto ->
                        if (selectedCategory !in listOf("Mujer", "Hombre", "Niños", "Bebé")) {
                            true
                        } else {
                            when (selectedCircleFilter) {
                                "Todos" -> true
                                "Ofertas" -> producto.categoria?.nombre.equals("Black Friday", ignoreCase = true)
                                "Calzado" -> producto.descripcion.equals("Calzado", ignoreCase = true) || producto.nombre.contains("Zapatilla", ignoreCase = true)
                                "Vestidos" -> producto.nombre.contains("Vestido", ignoreCase = true)
                                "Casacas" -> producto.nombre.contains("Casaca", ignoreCase = true)
                                else -> true
                            }
                        }
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
                            putExtra("productId", producto.id)
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
                                putExtra("productId", producto.id)
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
