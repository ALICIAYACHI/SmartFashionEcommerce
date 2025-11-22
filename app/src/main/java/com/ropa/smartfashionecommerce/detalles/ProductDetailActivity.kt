package com.ropa.smartfashionecommerce.detalles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.ropa.smartfashionecommerce.home.FavActivity
import com.ropa.smartfashionecommerce.home.FavoriteItem
import com.ropa.smartfashionecommerce.home.FavoritesManager
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import com.ropa.smartfashionecommerce.miperfil.ProfileImageManager
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

data class RelatedProductData(
    val name: String,
    val price: Double,
    val description: String,
    val imageRes: Int,
    val tags: List<String>,
    val category: String
)

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
    val productPrice = intent?.getDoubleExtra("productPrice", 0.0) ?: 0.0
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
                    price = "S/ %.2f".format(productPrice),
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
    var selectedSize by remember { mutableStateOf("M") }
    var selectedColor by remember { mutableStateOf("Negro") }
    var quantity by remember { mutableIntStateOf(1) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? ProductDetailActivity
    val intent = activity?.intent

    val productId = intent?.getIntExtra("productId", 0) ?: 0
    val productName = intent?.getStringExtra("productName") ?: "Blusa Elegante Negra"
    val productPrice = intent?.getDoubleExtra("productPrice", 89.90) ?: 89.90
    val productDescription = intent?.getStringExtra("productDescription")
        ?: "Blusa elegante de corte moderno, perfecta para ocasiones especiales. Confeccionada en tela de alta calidad con acabados refinados."

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
    
    // 🔄 Cargar reseñas en tiempo real
    LaunchedEffect(productName) {
        loadReviews(
            productId = productName,
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

    val painter = if (imageType == "url" && !productImageUrl.isNullOrEmpty()) {
        rememberAsyncImagePainter(productImageUrl)
    } else {
        painterResource(id = productImageRes ?: R.drawable.modelo_ropa)
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
            // 🔹 Imagen principal
            Image(
                painter = painter,
                contentDescription = productName,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

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
                val sizes = when (productCategory) {
                    "BEBE" -> {
                        // Tallas tipo bebé
                        listOf("0-3M", "3-6M", "6-9M", "9-12M", "12-18M", "18-24M")
                    }
                    else -> listOf("S", "M", "L", "XL")
                }

                sizes.forEach { size ->
                    OutlinedButton(
                        onClick = { selectedSize = size },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedSize == size) Color.Black else Color.Transparent,
                            contentColor = if (selectedSize == size) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text(size, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Color", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ColorOption(Color.Black, "Negro", selectedColor) { selectedColor = it }
                ColorOption(Color(0xFF607D8B), "Gris", selectedColor) { selectedColor = it }
                ColorOption(Color(0xFFD1B2FF), "Lila", selectedColor) { selectedColor = it }
            }

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
                    onClick = { quantity++ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = CircleShape
                ) { Text("+", fontSize = 20.sp) }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("15 en stock", color = Color(0xFF0D47A1), fontSize = 14.sp)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            val item = CartItem(
                                name = productName,
                                price = productPrice,
                                quantity = quantity,
                                size = selectedSize,
                                color = selectedColor,
                                imageRes = productImageRes ?: R.drawable.modelo_ropa,
                                imageUrl = if (imageType == "url") productImageUrl else null
                            )
                            CartManager.addItem(item)
                            CartManager.saveCart(context)

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

            Spacer(modifier = Modifier.height(28.dp))
            Text("Productos relacionados", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Productos relacionados dinámicos según la categoría del producto actual (solo ropa)
            val relatedCandidates = remember {
                listOf(
                    // MUJER
                    RelatedProductData(
                        name = "Vestido Dorado Noche",
                        price = 159.90,
                        description = "Vestido elegante de noche con detalles dorados brillantes.",
                        imageRes = R.drawable.vestidodorado,
                        tags = listOf("vestido", "noche", "elegante"),
                        category = "MUJER"
                    ),
                    RelatedProductData(
                        name = "Vestido Negro Clásico",
                        price = 139.90,
                        description = "Vestido negro clásico ideal para eventos formales.",
                        imageRes = R.drawable.modelo_ropa,
                        tags = listOf("vestido", "negro"),
                        category = "MUJER"
                    ),
                    RelatedProductData(
                        name = "Blusa Casual Beige",
                        price = 89.90,
                        description = "Blusa casual en tono beige, perfecta para el uso diario.",
                        imageRes = R.drawable.modelo_ropa,
                        tags = listOf("blusa", "casual"),
                        category = "MUJER"
                    ),

                    // HOMBRE
                    RelatedProductData(
                        name = "Casaca Moderna Hombre",
                        price = 129.90,
                        description = "Casaca moderna para hombre, ideal para el día a día.",
                        imageRes = R.drawable.casaca,
                        tags = listOf("casaca", "hombre"),
                        category = "HOMBRE"
                    ),

                    // NIÑO / BEBÉ
                    RelatedProductData(
                        name = "Conjunto Niño Urbano",
                        price = 79.90,
                        description = "Conjunto cómodo y moderno para niño.",
                        imageRes = R.drawable.modelo_ropa,
                        tags = listOf("niño", "nino"),
                        category = "NINO"
                    ),
                    RelatedProductData(
                        name = "Conjunto Bebé Niña",
                        price = 69.90,
                        description = "Conjunto tierno y cómodo para bebé niña.",
                        imageRes = R.drawable.modelo_ropa,
                        tags = listOf("bebé", "bebe"),
                        category = "BEBE"
                    )
                )
            }

            val filteredRelated = remember(productCategory, relatedCandidates, productName) {
                val matches = relatedCandidates.filter { candidate ->
                    candidate.category == productCategory && candidate.name != productName
                }
                if (matches.isNotEmpty()) matches else relatedCandidates
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredRelated.take(2).forEach { rp ->
                    RelatedProduct(
                        name = rp.name,
                        price = rp.price,
                        description = rp.description,
                        imageRes = rp.imageRes
                    )
                }
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
        "userId" to user.uid,
        "userName" to (user.displayName ?: "Cliente"),
        "userPhotoUrl" to photoUrlToSave,
        "rating" to rating,
        "comment" to comment,
        "createdAt" to com.google.firebase.Timestamp.now(),
        "isVerifiedPurchase" to false,
        "status" to "APPROVED"
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = review.comment,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
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
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Usuario",
                        userPhotoUrl = doc.getString("userPhotoUrl"),
                        rating = doc.getLong("rating")?.toInt() ?: 0,
                        comment = doc.getString("comment") ?: "",
                        createdAt = doc.getTimestamp("createdAt"),
                        isVerifiedPurchase = doc.getBoolean("isVerifiedPurchase") ?: false
                    )
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()
            
            onReviewsLoaded(reviewsList)
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
