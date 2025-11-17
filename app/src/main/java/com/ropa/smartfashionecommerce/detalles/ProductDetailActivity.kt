package com.ropa.smartfashionecommerce.detalles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.carrito.CartItem
import com.ropa.smartfashionecommerce.carrito.CartManager
import com.ropa.smartfashionecommerce.home.FavActivity
import com.ropa.smartfashionecommerce.home.FavoriteItem
import com.ropa.smartfashionecommerce.home.FavoritesManager
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val categorias = listOf("ZARA", "VOGUE", "CHANEL", "RALPH")

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
                repeat(5) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    "(24 reseñas)",
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

            Spacer(modifier = Modifier.height(16.dp))
            Text("Talla", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                listOf("S", "M", "L", "XL").forEach { size ->
                    OutlinedButton(
                        onClick = { selectedSize = size },
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selectedSize == size) Color.Black else Color.Transparent,
                            contentColor = if (selectedSize == size) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.width(70.dp)
                    ) { Text(size) }
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
                        val item = CartItem(
                            name = productName,
                            price = productPrice,
                            quantity = quantity,
                            size = selectedSize,
                            color = selectedColor,
                            imageRes = productImageRes ?: R.drawable.modelo_ropa
                        )
                        CartManager.addItem(item)
                        CartManager.saveCart(context)

                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Producto agregado al carrito 🛒")
                        }

                        val intent = Intent(context, Carrito::class.java)
                        context.startActivity(intent)
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
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RelatedProduct(
                    "Vestido Dorado Noche",
                    159.90,
                    "Vestido elegante de noche con detalles dorados brillantes.",
                    R.drawable.vestidodorado
                )
                RelatedProduct(
                    "Casaca Moderna",
                    120.90,
                    "Casaca moderna ideal para el día a día, con estilo urbano y comodidad.",
                    R.drawable.casaca
                )
            }

            if (showReviewDialog) {
                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    title = { Text("Reseñar $productName") },
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
                                submitReviewCompose(
                                    productId = productName,
                                    rating = reviewRating,
                                    comment = reviewComment,
                                    context = context,
                                    snackbarHostState = snackbarHostState,
                                    coroutineScope = coroutineScope
                                )
                                showReviewDialog = false
                            } else {
                                Toast.makeText(context, "Selecciona una calificación", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text("Enviar")
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
    val reviewData = mapOf(
        "userId" to user.uid,
        "userName" to (user.displayName ?: "Cliente"),
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
