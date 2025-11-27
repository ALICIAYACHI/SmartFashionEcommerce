package com.ropa.smartfashionecommerce.detalles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ropa.smartfashionecommerce.model.Review
import java.text.SimpleDateFormat
import java.util.Locale
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.carrito.CartItem
import com.ropa.smartfashionecommerce.carrito.CartManager
import com.ropa.smartfashionecommerce.carrito.StockManager
import com.ropa.smartfashionecommerce.home.FavoriteItem
import com.ropa.smartfashionecommerce.home.FavoritesManager
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import com.ropa.smartfashionecommerce.miperfil.ProfileImageManager
import com.ropa.smartfashionecommerce.model.ColorDto
import com.ropa.smartfashionecommerce.model.ProductDetailData
import com.ropa.smartfashionecommerce.model.ProductVariantDto
import com.ropa.smartfashionecommerce.model.Producto
import com.ropa.smartfashionecommerce.model.SizeDto
import com.ropa.smartfashionecommerce.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class ProductDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FavoritesManager.initialize(this)
        setContent {
            MaterialTheme {
                Surface(color = Color.White, modifier = Modifier.fillMaxSize()) {
                    ProductDetailScreen()
                }
            }
        }
    }
}

@Composable
fun RelatedProductFromApi(producto: Producto) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9F9F9))
            .clickable {
                val intent = Intent(context, ProductDetailActivity::class.java).apply {
                    putExtra("productId", producto.id)
                    putExtra("productName", producto.nombre)
                    putExtra("productPrice", producto.precio.toDoubleOrNull() ?: 0.0)
                    putExtra("productDescription", producto.descripcion)
                    putExtra("imageType", "url")
                    putExtra("productImageUrl", producto.image_preview)
                    putExtra("productStock", producto.stock_total)
                }
                context.startActivity(intent)
            }
            .padding(8.dp)
    ) {
        if (!producto.image_preview.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(producto.image_preview),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.modelo_ropa),
                contentDescription = producto.nombre,
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(producto.nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
        Text(
            text = "S/ %.2f".format(producto.precio.toDoubleOrNull() ?: 0.0),
            color = Color(0xFF0D47A1),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

data class ProductImageSource(val resId: Int? = null, val url: String? = null)

// Utilidad simple para convertir un código hex (#RRGGBB) en Color de Compose
fun parseHexColor(hex: String): Color {
    val clean = hex.trim().removePrefix("#")
    if (clean.length != 6) return Color.Black
    return try {
        val value = clean.toLong(16)
        val r = ((value shr 16) and 0xFF).toInt()
        val g = ((value shr 8) and 0xFF).toInt()
        val b = (value and 0xFF).toInt()
        Color(r / 255f, g / 255f, b / 255f)
    } catch (_: Exception) {
        Color.Black
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    // Categorías reales de la app (las mismas que usa CatalogActivity)
    val categorias = listOf("Mujeres", "Hombres", "Niños")

    // ✅ Cargar imagen de perfil guardada (reactiva)
    val sharedPref = context.getSharedPreferences("MiPerfil", Context.MODE_PRIVATE)
    var profileImageUri by remember {
        mutableStateOf(sharedPref.getString("profile_image_uri", null))
    }

    // 🔄 Si se vuelve del perfil, recarga la imagen
    LaunchedEffect(Unit) {
        profileImageUri = sharedPref.getString("profile_image_uri", null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 🖋️ Logo SMARTFASHION
                        Text(
                            text = "SMARTFASHION",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF111111),
                            modifier = Modifier.clickable {
                                val intent = Intent(context, HomeActivity::class.java)
                                context.startActivity(intent)
                            }
                        )

                        // 🔽 Menú Categorías
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = { expanded = true }) {
                                Text("Categorías ▼", color = Color.Black)
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color.White)
                            ) {
                                categorias.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria) },
                                        onClick = {
                                            expanded = false
                                            val intent = Intent(
                                                context,
                                                com.ropa.smartfashionecommerce.catalog.CatalogActivity::class.java
                                            )
                                            intent.putExtra("CATEGORY", categoria)
                                            context.startActivity(intent)
                                        }
                                    )
                                }
                            }
                        }

                        // ❤️ 🛒 👤 Acciones
                        ProductDetailActions(
                            context = context,
                            profileImageUri = profileImageUri
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        ProductDetailContent(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ProductDetailActions(
    context: Context,
    profileImageUri: String?
) {
    val activity = context as? ProductDetailActivity
    val intent = activity?.intent

    val productId = intent?.getIntExtra("productId", 0) ?: 0
    val productName = intent?.getStringExtra("productName") ?: "Producto"
    val initialPrice = intent?.getDoubleExtra("productPrice", 0.0) ?: 0.0
    val productImageRes = intent?.getIntExtra("productImageRes", R.drawable.modelo_ropa) ?: R.drawable.modelo_ropa

    // ✅ Verificar si el producto está en favoritos - Solo si tiene ID válido
    val favoriteItems = FavoritesManager.favoriteItems
    var isFavorite by remember(productId) {
        mutableStateOf(favoriteItems.any { it.id == productId && productId != 0 })
    }

    // Actualizar estado cuando cambia la lista de favoritos
    LaunchedEffect(favoriteItems.size, productId) {
        isFavorite = favoriteItems.any { it.id == productId && productId != 0 }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        // ❤️ Botón Favoritos
        IconButton(onClick = {
            if (productId == 0) {
                Toast.makeText(context, "Error: Producto sin ID válido", Toast.LENGTH_SHORT).show()
                return@IconButton
            }

            if (isFavorite) {
                // Eliminar de favoritos
                val itemToRemove = favoriteItems.find { it.id == productId }
                itemToRemove?.let {
                    FavoritesManager.removeFavorite(context, it)
                    isFavorite = false
                    Toast.makeText(context, "Eliminado de favoritos 💔", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Agregar a favoritos
                val favoriteItem = FavoriteItem(
                    id = productId,
                    name = productName,
                    price = "S/ %.2f".format(initialPrice),
                    sizes = listOf("S", "M", "L", "XL"),
                    imageRes = productImageRes,
                    imageUrl = null,
                    isFavorite = true
                )
                FavoritesManager.addFavorite(context, favoriteItem)
                isFavorite = true
                Toast.makeText(context, "Agregado a favoritos ❤️", Toast.LENGTH_SHORT).show()
            }
        }) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "Quitar de favoritos" else "Agregar a favoritos",
                tint = if (isFavorite) Color.Red else Color.Black
            )
        }

        // 🛒 Botón Carrito
        IconButton(onClick = {
            context.startActivity(Intent(context, Carrito::class.java))
        }) {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = "Carrito",
                tint = Color.Black
            )
        }

        // 👤 Foto de perfil (o ícono por defecto)
        IconButton(onClick = {
            context.startActivity(Intent(context, MiPerfilActivity::class.java))
        }) {
            if (profileImageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(Uri.parse(profileImageUri)),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(1.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "Perfil",
                    tint = Color.Black
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductDetailContent(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    var selectedSize by remember { mutableStateOf<String?>(null) }
    var selectedColor by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableIntStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? ProductDetailActivity
    val intent = activity?.intent

    val productId = intent?.getIntExtra("productId", 0) ?: 0
    val productName = intent?.getStringExtra("productName") ?: ""
    val initialPrice = intent?.getDoubleExtra("productPrice", 0.0) ?: 0.0

    // Descripción y precio reactivos del producto: se rellenarán con lo que devuelva el backend.
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf(initialPrice) }
    // Si viene una descripción por Intent (por ejemplo, desde el listado), la usamos como valor inicial.
    LaunchedEffect(Unit) {
        val fromIntent = intent?.getStringExtra("productDescription")
        if (!fromIntent.isNullOrBlank()) {
            productDescription = fromIntent
        }
    }

    // ✅ Manejo de imágenes locales y por URL
    val imageType = intent?.getStringExtra("imageType")
    val productImageRes = intent?.getIntExtra("productImageRes", R.drawable.modelo_ropa)
    val productImageUrl = intent?.getStringExtra("productImageUrl")

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewRating by remember { mutableIntStateOf(0) }
    var reviewComment by remember { mutableStateOf("") }

    // 📝 Estado para reseñas
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var averageRating by remember { mutableFloatStateOf(0f) }
    var editingReview by remember { mutableStateOf<Review?>(null) }

    // Categoría aproximada del producto según su nombre (solo ropa: mujer, hombre, niño, bebé)
    val productCategory = remember(productName) {
        val nameLower = productName.lowercase()
        when {
            "bebé" in nameLower || "bebe" in nameLower -> "BEBE"
            "niño" in nameLower || "niña" in nameLower || "nino" in nameLower -> "NINO"
            "hombre" in nameLower -> "HOMBRE"
            else -> "MUJER"
        }
    }

    // 🔄 Cargar reseñas en tiempo real (usando el ID real del producto en Firestore)
    LaunchedEffect(productId) {
        if (productId == 0) return@LaunchedEffect
        loadReviews(
            productId = productId.toString(),
            onReviewsLoaded = { loadedReviews ->
                reviews = loadedReviews
                averageRating = if (loadedReviews.isNotEmpty()) {
                    loadedReviews.map { it.rating }.average().toFloat()
                } else {
                    0f
                }
            }
        )
    }

    val baseImageResId = productImageRes ?: R.drawable.modelo_ropa

    // ✅ Datos de variantes, tallas, colores e imágenes reales desde el backend (similar a ProductDetail.jsx)
    var variants by remember { mutableStateOf<List<ProductVariantDto>>(emptyList()) }
    var sizes by remember { mutableStateOf<List<SizeDto>>(emptyList()) }
    var colors by remember { mutableStateOf<List<ColorDto>>(emptyList()) }
    var images by remember { mutableStateOf<List<String>>(emptyList()) }
    var imagesByColor by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var imagesByVariant by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var relatedProducts by remember { mutableStateOf<List<Producto>>(emptyList()) }

    // 🧮 Stock actual del producto
    // Empezamos con el stock que llega desde el listado (si lo hay) y luego lo
    // actualizamos con el stock_total real del backend cuando llega el detalle.
    val productStockFromList = intent?.getIntExtra("productStock", -1) ?: -1
    var baseStock by remember { mutableIntStateOf(if (productStockFromList >= 0) productStockFromList else 0) }
    var currentStock by remember(productName) {
        mutableIntStateOf(StockManager.getStock(context, productName, baseStock))
    }

    LaunchedEffect(productId) {
        if (productId == 0) return@LaunchedEffect
        try {
            val api = ApiClient.apiService

            // Detalle de producto (variantes + imágenes)
            val detailResp = api.getProductDetail(productId)
            if (detailResp.isSuccessful) {
                val body: com.ropa.smartfashionecommerce.model.ApiResponse<ProductDetailData>? = detailResp.body()
                val data = body?.data
                variants = data?.variants.orEmpty()
                images = data?.images.orEmpty()
                imagesByColor = data?.imagesByColor ?: emptyMap()
                imagesByVariant = data?.imagesByVariant ?: emptyMap()
                relatedProducts = data?.related.orEmpty()

                // Stock general desde backend (stock_total) para que coincida con el web
                val backendStockTotal = data?.product?.stock_total
                if (backendStockTotal != null) {
                    baseStock = backendStockTotal
                    currentStock = StockManager.getStock(context, productName, baseStock)
                }

                // Actualizar descripción con la que viene del backend (product.descripcion), si existe
                val backendDescription = data?.product?.descripcion
                if (!backendDescription.isNullOrEmpty()) {
                    productDescription = backendDescription
                }

                // Actualizar precio con el del backend (product.precio) para que no quede en 0.0
                val backendPrice = data?.product?.precio
                if (backendPrice != null) {
                    productPrice = backendPrice
                }
            }

            // Catálogo de tallas y colores (como en el frontend React)
            val sizesResp = api.getSizes()
            if (sizesResp.isSuccessful) {
                sizes = sizesResp.body()?.data.orEmpty()
            }
            val colorsResp = api.getColors()
            if (colorsResp.isSuccessful) {
                colors = colorsResp.body()?.data.orEmpty()
            }
        } catch (_: Exception) {
            // Silenciar errores de red aquí; la UI seguirá usando valores por defecto
        }
    }

    // 🖼️ Galería de imágenes del producto (se alimenta de images / imagesByColor / imagesByVariant como en la web)
    var currentImageIndex by remember { mutableIntStateOf(0) }

    // URLs priorizadas según selección de talla/color
    val galleryUrls = remember(images, imagesByColor, imagesByVariant, selectedSize, selectedColor, productImageUrl) {
        // 1) Imágenes específicas de variante talla+color
        if (selectedSize != null && selectedColor != null) {
            val key = "${selectedSize}-${selectedColor}"
            val byVar = imagesByVariant[key]
            if (!byVar.isNullOrEmpty()) return@remember byVar
        }

        // 2) Imágenes por color seleccionado
        if (selectedColor != null) {
            val byColor = imagesByColor[selectedColor]
            if (!byColor.isNullOrEmpty()) return@remember byColor
        }

        // 3) Imágenes generales del producto
        if (images.isNotEmpty()) return@remember images

        // 4) Fallback: preview del listado (si es URL)
        if (imageType == "url" && !productImageUrl.isNullOrEmpty()) return@remember listOf(productImageUrl)

        emptyList()
    }

    // Construir lista de fuentes para Compose (URL o recurso local)
    val productImageSources = remember(galleryUrls, baseImageResId, productImageUrl, imageType) {
        buildList {
            if (galleryUrls.isNotEmpty()) {
                galleryUrls.forEach { url -> add(ProductImageSource(url = url)) }
            } else if (imageType == "url" && !productImageUrl.isNullOrEmpty()) {
                add(ProductImageSource(url = productImageUrl))
            } else {
                add(ProductImageSource(resId = baseImageResId))
            }
        }
    }

    // Cuando cambie la galería (por color/talla), volver a la primera imagen
    LaunchedEffect(galleryUrls) {
        currentImageIndex = 0
    }

    val totalImages = productImageSources.size

    // Mapas de tallas/colores por id (como en React)
    val sizeMap = remember(sizes) {
        sizes.associateBy { it.id.toString() }
    }
    val colorMap = remember(colors) {
        colors.associateBy { it.id.toString() }
    }

    // IDs disponibles a partir de las variantes
    val variantsState by rememberUpdatedState(variants)
    val availableSizeIds = remember(variantsState) {
        variantsState.map { it.size_id.toString() }.toSet().toList()
    }
    val availableColorIds = remember(variantsState) {
        variantsState.map { it.color_id.toString() }.toSet().toList()
    }

    // Filtrado cruzado: colores válidos para una talla y viceversa
    val filteredColorsForSize = remember(variantsState, selectedSize, availableColorIds) {
        if (selectedSize == null) availableColorIds
        else variantsState.filter { it.size_id.toString() == selectedSize }
            .map { it.color_id.toString() }
            .toSet()
            .toList()
    }

    val filteredSizesForColor = remember(variantsState, selectedColor, availableSizeIds) {
        if (selectedColor == null) availableSizeIds
        else variantsState.filter { it.color_id.toString() == selectedColor }
            .map { it.size_id.toString() }
            .toSet()
            .toList()
    }

    // Stock de la combinación seleccionada (si existe)
    val selectedStock = remember(variantsState, selectedSize, selectedColor) {
        if (selectedSize == null || selectedColor == null) null
        else variantsState.find {
            it.size_id.toString() == selectedSize && it.color_id.toString() == selectedColor
        }?.stock
    }

    // 💖 Estado de favorito para este producto - SINCRONIZADO con el header
    val favoriteItems = FavoritesManager.favoriteItems
    var isFavorite by remember(productId) {
        mutableStateOf(favoriteItems.any { it.id == productId && productId != 0 })
    }

    // Actualizar estado cuando cambia la lista de favoritos
    LaunchedEffect(favoriteItems.size, productId) {
        isFavorite = favoriteItems.any { it.id == productId && productId != 0 }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = modifier
                .verticalScroll(scrollState)
                .fillMaxSize()
                .padding(top = 16.dp)
                .padding(paddingValues)
        ) {
            // 🔹 Imagen principal con indicador (galería + swipe izquierda/derecha)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .pointerInput(totalImages) {
                        detectHorizontalDragGestures { change, dragAmount: Float ->
                            // Consumir el cambio para que no se propague a otros gestos
                            change.consume()
                            if (totalImages <= 1) return@detectHorizontalDragGestures

                            if (dragAmount > 20f) {
                                // Deslizar de izquierda a derecha -> imagen anterior
                                currentImageIndex =
                                    if (currentImageIndex > 0) currentImageIndex - 1 else totalImages - 1
                            } else if (dragAmount < -20f) {
                                // Deslizar de derecha a izquierda -> siguiente imagen
                                currentImageIndex =
                                    if (currentImageIndex < totalImages - 1) currentImageIndex + 1 else 0
                            }
                        }
                    }
            ) {
                val currentSource = productImageSources.getOrNull(currentImageIndex)
                val mainPainter = when {
                    currentSource?.url != null -> rememberAsyncImagePainter(currentSource.url)
                    currentSource?.resId != null -> painterResource(id = currentSource.resId)
                    imageType == "url" && !productImageUrl.isNullOrEmpty() -> rememberAsyncImagePainter(productImageUrl)
                    else -> painterResource(id = baseImageResId)
                }

                Image(
                    painter = mainPainter,
                    contentDescription = productName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (totalImages > 1) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xCC000000))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${currentImageIndex + 1}/$totalImages",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(productName, fontSize = 24.sp, fontWeight = FontWeight.Bold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { index ->
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        tint = if (index < averageRating.toInt()) Color(0xFFFFC107) else Color.LightGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = if (reviews.isNotEmpty()) {
                        val reviewText = if (reviews.size == 1) "reseña" else "reseñas"
                        "%.1f (${reviews.size} $reviewText)".format(averageRating)
                    } else {
                        "Sin reseñas"
                    },
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = {
                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Toast.makeText(context, "Inicia sesión para reseñar", Toast.LENGTH_SHORT).show()
                } else {
                    editingReview = null
                    reviewRating = 0
                    reviewComment = ""
                    showReviewDialog = true
                }
            }) {
                Text("Escribir una reseña", color = Color(0xFF0D47A1))
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "S/ %.2f".format(productPrice),
                fontSize = 20.sp,
                color = Color(0xFF0D47A1),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = productDescription,
                fontSize = 15.sp,
                color = Color.Gray,
                lineHeight = 22.sp
            )

            // Siempre mostramos tallas para ropa (Mujer, Hombre, Niño, Bebé)
            Spacer(modifier = Modifier.height(16.dp))
            val sizeLabel = "Talla"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(sizeLabel, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                if (productCategory == "BEBE") {
                    TextButton(onClick = {
                        val intent = Intent(context, com.ropa.smartfashionecommerce.detalles.BabySizeGuideActivity::class.java)
                        context.startActivity(intent)
                    }) {
                        Text("Guía de tallas", color = Color(0xFF0D47A1), fontSize = 14.sp)
                    }
                }
            }

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opciones de talla reales a partir de variants + sizes
                val sizeOptions: List<Pair<String, String>> = availableSizeIds.mapNotNull { idStr ->
                    val s = sizeMap[idStr]
                    if (s != null) idStr to s.nombre else null
                }

                // Si solo hay una talla disponible y no hay selección, la seleccionamos por defecto
                LaunchedEffect(sizeOptions.size) {
                    if (selectedSize == null && sizeOptions.size == 1) {
                        selectedSize = sizeOptions.first().first
                    }
                }

                sizeOptions.forEach { (idStr, displayName) ->
                    // Solo mostrar tallas compatibles con el color seleccionado
                    if (filteredSizesForColor.contains(idStr)) {
                        val isSelected = selectedSize == idStr
                        OutlinedButton(
                            onClick = { selectedSize = idStr },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) Color.Black else Color.Transparent,
                                contentColor = if (isSelected) Color.White else Color.Black
                            ),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(displayName, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Color", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (!selectedColor.isNullOrEmpty()) {
                    val colorName = colorMap[selectedColor]?.nombre ?: selectedColor.orEmpty()
                    Text(
                        text = "Seleccionado: [$colorName]",
                        fontSize = 13.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opciones de color reales a partir de variants + colors
                val colorOptions: List<Pair<String, String>> = availableColorIds.mapNotNull { idStr ->
                    val c = colorMap[idStr]
                    if (c != null) idStr to c.nombre else null
                }

                // Si solo hay un color disponible y no hay selección, seleccionarlo por defecto
                LaunchedEffect(colorOptions.size) {
                    if (selectedColor == null && colorOptions.size == 1) {
                        selectedColor = colorOptions.first().first
                    }
                }

                colorOptions.forEach { (idStr, _) ->
                    if (filteredColorsForSize.contains(idStr)) {
                        val colorDto = colorMap[idStr]
                        val isSelected = selectedColor == idStr

                        val dotColor = remember(colorDto?.codigo_hex) {
                            colorDto?.codigo_hex?.let { hex ->
                                runCatching { parseHexColor(hex) }.getOrElse { Color.Black }
                            } ?: Color.Black
                        }

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color.Black else Color.LightGray,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = idStr },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }
                }
            }

            val effectiveStock = selectedStock ?: currentStock

            if (effectiveStock > 0) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cantidad", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { if (quantity > 1) quantity-- },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        shape = CircleShape
                    ) { Text("-", fontSize = 20.sp) }

                    Text(text = quantity.toString(), fontSize = 18.sp, fontWeight = FontWeight.Medium)

                    Button(
                        onClick = {
                            // No permitir seleccionar más cantidad que el stock disponible
                            if (quantity < effectiveStock) quantity++
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                        shape = CircleShape
                    ) { Text("+", fontSize = 20.sp) }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("$effectiveStock en stock", color = Color(0xFF0D47A1), fontSize = 14.sp)

                    when {
                        effectiveStock in 3..5 -> {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ÚLTIMOS",
                                color = Color(0xFFD32F2F),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        effectiveStock in 1..2 -> {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Casi agotados",
                                color = Color(0xFFFF6F00),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user == null) {
                                Toast.makeText(context, "Inicia sesión para agregar al carrito", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(context, com.ropa.smartfashionecommerce.DarkLoginActivity::class.java))
                            } else {
                                // Si el producto tiene variantes, exigir selección de talla y color
                                if (variants.isNotEmpty() && (selectedSize == null || selectedColor == null)) {
                                    val msg = when {
                                        selectedSize == null && selectedColor == null -> "Selecciona talla y color"
                                        selectedSize == null -> "Selecciona una talla"
                                        else -> "Selecciona un color"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                if (effectiveStock <= 0) {
                                    Toast.makeText(context, "Producto sin stock", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val sizeNameForCart = selectedSize?.let { idStr ->
                                    sizeMap[idStr]?.nombre
                                } ?: selectedSize ?: ""
                                val colorNameForCart = selectedColor?.let { idStr ->
                                    colorMap[idStr]?.nombre
                                } ?: selectedColor ?: ""

                                // IDs numéricos para checkout/backend
                                val sizeIdInt = selectedSize?.toIntOrNull()
                                val colorIdInt = selectedColor?.toIntOrNull()

                                val item = CartItem(
                                    productId = productId,
                                    sizeId = sizeIdInt,
                                    colorId = colorIdInt,
                                    name = productName,
                                    size = sizeNameForCart,
                                    color = colorNameForCart,
                                    quantity = quantity,
                                    price = productPrice,
                                    imageRes = productImageRes ?: R.drawable.modelo_ropa,
                                    imageUrl = if (imageType == "url") productImageUrl else null
                                )
                                CartManager.addItem(item)
                                CartManager.saveCart(context)

                                // Actualizar stock localmente (se descuenta al añadir al carrito)
                                StockManager.reduceStock(context, productName, quantity)
                                currentStock = StockManager.getStock(context, productName, baseStock)

                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Producto agregado al carrito 🛒")
                                }

                                val intent = Intent(context, Carrito::class.java)
                                context.startActivity(intent)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .height(55.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar al carrito", color = Color.White, fontSize = 18.sp)
                    }

                    IconButton(
                        onClick = {
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user == null) {
                                Toast.makeText(context, "Inicia sesión para usar favoritos", Toast.LENGTH_SHORT).show()
                                context.startActivity(Intent(context, com.ropa.smartfashionecommerce.DarkLoginActivity::class.java))
                                return@IconButton
                            }

                            if (productId == 0) {
                                Toast.makeText(context, "Error: Producto sin ID válido", Toast.LENGTH_SHORT).show()
                                return@IconButton
                            }

                            if (isFavorite) {
                                // Eliminar de favoritos
                                val itemToRemove = favoriteItems.find { it.id == productId }
                                itemToRemove?.let {
                                    FavoritesManager.removeFavorite(context, it)
                                    isFavorite = false
                                    Toast.makeText(context, "Eliminado de favoritos 💔", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Agregar a favoritos
                                val favoriteItem = FavoriteItem(
                                    id = productId,
                                    name = productName,
                                    price = "S/ %.2f".format(productPrice),
                                    sizes = listOf("S", "M", "L", "XL"),
                                    imageRes = productImageRes ?: R.drawable.modelo_ropa,
                                    imageUrl = if (imageType == "url") productImageUrl else null,
                                    isFavorite = true
                                )
                                FavoritesManager.addFavorite(context, favoriteItem)
                                isFavorite = true
                                Toast.makeText(context, "Agregado a favoritos ❤️", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .size(55.dp)
                            .border(1.dp, Color.LightGray, CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (isFavorite) Color.Red else Color.Black
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Producto agotado",
                    color = Color.Red,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Este producto ya no tiene stock disponible",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("Productos relacionados", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            if (relatedProducts.isNotEmpty()) {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    relatedProducts.forEach { rp ->
                        RelatedProductFromApi(producto = rp)
                    }
                }
            } else {
                Text(
                    text = "No hay productos relacionados.",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }

            // 📝 Sección de reseñas
            Spacer(modifier = Modifier.height(28.dp))
            Text("Reseñas de clientes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            if (reviews.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_star),
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Aún no hay reseñas",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                        Text(
                            "Sé el primero en reseñar este producto",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                reviews.forEach { review ->
                    ReviewCard(
                        review = review,
                        currentUserId = currentUserId,
                        onUserClick = {
                            if (currentUserId != null && currentUserId == review.userId) {
                                // Si es mi propia reseña, voy a "Mis reseñas" en mi perfil
                                val intent = Intent(context, com.ropa.smartfashionecommerce.miperfil.MisResenasActivity::class.java)
                                context.startActivity(intent)
                            } else {
                                // Si es otro usuario, sigo yendo al perfil del reseñador
                                val intent = Intent(context, ReviewerProfileActivity::class.java).apply {
                                    putExtra("userId", review.userId)
                                    putExtra("userName", review.userName)
                                }
                                context.startActivity(intent)
                            }
                        },
                        onEdit = {
                            editingReview = review
                            reviewRating = review.rating
                            reviewComment = review.comment
                            showReviewDialog = true
                        },
                        onDelete = {
                            deleteReview(
                                productId = productName,
                                reviewId = review.id,
                                context = context,
                                onSuccess = {
                                    Toast.makeText(context, "Reseña eliminada", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (showReviewDialog) {
                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    title = { Text(if (editingReview != null) "Editar reseña" else "Reseñar $productName") },
                    text = {
                        Column {
                            Text("Selecciona tu calificación", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row {
                                (1..5).forEach { star ->
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_star),
                                        contentDescription = null,
                                        tint = if (star <= reviewRating) Color(0xFFFFC107) else Color.LightGray,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { reviewRating = star }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = reviewComment,
                                onValueChange = { reviewComment = it },
                                label = { Text("Comentario (opcional)") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (reviewRating in 1..5) {
                                if (editingReview != null) {
                                    updateReview(
                                        productId = productName,
                                        reviewId = editingReview!!.id,
                                        rating = reviewRating,
                                        comment = reviewComment,
                                        context = context,
                                        onSuccess = {
                                            Toast.makeText(context, "Reseña actualizada", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                } else {
                                    submitReviewCompose(
                                        productId = productName,
                                        rating = reviewRating,
                                        comment = reviewComment,
                                        context = context,
                                        snackbarHostState = snackbarHostState,
                                        coroutineScope = coroutineScope
                                    )
                                }
                                showReviewDialog = false
                            } else {
                                Toast.makeText(context, "Selecciona una calificación", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text(if (editingReview != null) "Actualizar" else "Enviar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

private fun submitReviewCompose(
    productId: String,
    rating: Int,
    comment: String,
    context: Context,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        Toast.makeText(context, "Inicia sesión para reseñar", Toast.LENGTH_SHORT).show()
        return
    }

    val db = FirebaseFirestore.getInstance()
    // Determinar qué foto de perfil guardar en la reseña
    // 1) Si el usuario tiene foto local en Mi Perfil, usamos esa
    // 2) Si no, usamos la foto del proveedor (por ejemplo Google)
    val localProfileUri = ProfileImageManager.profileImageUri.value
    val photoUrlToSave = when {
        localProfileUri != null -> localProfileUri.toString()
        user.photoUrl != null -> user.photoUrl.toString()
        else -> ""
    }

    val reviewData = mapOf(
        "productId" to productId,
        "userId" to user.uid,
        "userName" to (user.displayName ?: "Cliente"),
        "userPhotoUrl" to photoUrlToSave,
        "rating" to rating,
        "comment" to comment,
        "createdAt" to com.google.firebase.Timestamp.now(),
        "isVerifiedPurchase" to false,
        "status" to "APPROVED",
        "likedUserIds" to emptyList<String>()
    )

    db.collection("products")
        .document(productId)
        .collection("reviews")
        .add(reviewData)
        .addOnSuccessListener {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Gracias por tu reseña")
            }
        }
        .addOnFailureListener {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Error al guardar reseña")
            }
        }
}

@Composable
fun ColorOption(color: Color, label: String, selected: String, onSelect: (String) -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .border(
                width = if (selected == label) 3.dp else 1.dp,
                color = if (selected == label) Color.Black else Color.LightGray,
                shape = CircleShape
            )
            .background(color, CircleShape)
            .clickable { onSelect(label) }
    )
}

@Composable
fun RelatedProduct(name: String, price: Double, description: String, imageRes: Int) {
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(150.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9F9F9))
            .clickable {
                val intent = Intent(context, ProductDetailActivity::class.java).apply {
                    putExtra("productName", name)
                    putExtra("productPrice", price)
                    putExtra("productDescription", description)
                    putExtra("imageType", "local")
                    putExtra("productImageRes", imageRes)
                }
                context.startActivity(intent)
            }
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = name,
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text("S/ %.2f".format(price), color = Color(0xFF0D47A1), fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReviewCard(
    review: Review,
    currentUserId: String?,
    onUserClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val isOwner = currentUserId == review.userId
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    val dateString = review.createdAt?.toDate()?.let { dateFormat.format(it) } ?: ""
    var liked by remember(review.likedUserIds, currentUserId) {
        mutableStateOf(currentUserId != null && review.likedUserIds.contains(currentUserId))
    }

    // Cargar la foto de perfil local (si existe) para poder usarla en las reseñas propias
    LaunchedEffect(Unit) {
        ProfileImageManager.loadProfileImage(context)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Avatar del usuario
                    val localProfileUri = ProfileImageManager.profileImageUri.value
                    val avatarPainter = when {
                        // Si es mi reseña y tengo foto local en Mi Perfil, usar esa
                        currentUserId != null && currentUserId == review.userId && localProfileUri != null -> {
                            rememberAsyncImagePainter(localProfileUri)
                        }
                        // Si la reseña tiene URL de foto guardada en Firestore (por ejemplo Google), usarla
                        !review.userPhotoUrl.isNullOrBlank() -> {
                            rememberAsyncImagePainter(review.userPhotoUrl)
                        }
                        else -> {
                            painterResource(id = R.drawable.ic_person)
                        }
                    }
                    Image(
                        painter = avatarPainter,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = review.userName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0D47A1),
                                modifier = Modifier.clickable { onUserClick() }
                            )
                            if (dateString.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateString,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(5) { index ->
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star),
                                    contentDescription = null,
                                    tint = if (index < review.rating) Color(0xFFFFC107) else Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (review.isVerifiedPurchase) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "✓ Compra verificada",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }

                if (isOwner) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color(0xFF0D47A1)
                            )
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFD32F2F)
                            )
                        }
                    }
                }
            }
            if (review.comment.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = if (liked) Icons.Filled.ThumbUp else Icons.Outlined.ThumbUp
                    val tint = if (liked) Color(0xFFD32F2F) else Color.Gray
                    val likesCount = review.likedUserIds.size

                    Icon(
                        imageVector = icon,
                        contentDescription = "Útil",
                        tint = tint,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                liked = !liked
                                toggleReviewLike(review.productId, review.id, context)
                            }
                    )

                    if (likesCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Útil ($likesCount)",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun loadReviews(
    productId: String,
    onReviewsLoaded: (List<Review>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("products")
        .document(productId)
        .collection("reviews")
        .orderBy("createdAt", Query.Direction.DESCENDING)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onReviewsLoaded(emptyList())
                return@addSnapshotListener
            }

            val reviewsList = snapshot?.documents?.mapNotNull { doc ->
                try {
                    Review(
                        productId = productId,
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Usuario",
                        userPhotoUrl = doc.getString("userPhotoUrl"),
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        comment = doc.getString("comment") ?: "",
                        createdAt = doc.getTimestamp("createdAt"),
                        isVerifiedPurchase = doc.getBoolean("isVerifiedPurchase") ?: false,
                        likedUserIds = (doc.get("likedUserIds") as? List<*>)
                            ?.filterIsInstance<String>()
                            ?: emptyList()
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            onReviewsLoaded(reviewsList)
        }
}

private fun toggleReviewLike(
    productId: String,
    reviewId: String,
    context: Context
) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseFirestore.getInstance()

    val reviewRef = db.collection("products")
        .document(productId)
        .collection("reviews")
        .document(reviewId)

    db.runTransaction { transaction ->
        val snapshot = transaction.get(reviewRef)
        val currentList = (snapshot.get("likedUserIds") as? List<*>)
            ?.filterIsInstance<String>()
            ?: emptyList()

        val updatedList = if (currentList.contains(user.uid)) {
            currentList.filter { it != user.uid }
        } else {
            currentList + user.uid
        }

        transaction.update(reviewRef, "likedUserIds", updatedList)
    }.addOnFailureListener {
        Toast.makeText(context, "No se pudo actualizar el like", Toast.LENGTH_SHORT).show()
    }
}

private fun updateReview(
    productId: String,
    reviewId: String,
    rating: Int,
    comment: String,
    context: Context,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val updates = mapOf(
        "rating" to rating,
        "comment" to comment,
        "updatedAt" to com.google.firebase.Timestamp.now()
    )

    db.collection("products")
        .document(productId)
        .collection("reviews")
        .document(reviewId)
        .update(updates)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener {
            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
        }
}

private fun deleteReview(
    productId: String,
    reviewId: String,
    context: Context,
    onSuccess: () -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    db.collection("products")
        .document(productId)
        .collection("reviews")
        .document(reviewId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener {
            Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
        }
}
