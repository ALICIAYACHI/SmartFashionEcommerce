package com.ropa.smartfashionecommerce.catalog

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.graphics.drawable.GradientDrawable
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView

import androidx.activity.enableEdgeToEdge

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ropa.smartfashionecommerce.R

import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.carrito.CartManager

import com.ropa.smartfashionecommerce.home.FavActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.miperfil.ProfileImageManager

import com.ropa.smartfashionecommerce.maps.MapsActivity
import androidx.compose.material.icons.outlined.LocationOn

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.network.SimpleCategoryDto

class CatalogActivity : AppCompatActivity() {

    // Texto de búsqueda para filtrar subcategorías en modo categorías
    private var categorySearchQuery by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalog)

        val mode = intent.getStringExtra("MODE") ?: "CATEGORIES"
        val category = intent.getStringExtra("CATEGORY") ?: "Hombres"
        val subcategory = intent.getStringExtra("SUBCATEGORY")
        val initialQuery = intent.getStringExtra("SEARCH_QUERY")?.trim().orEmpty()

        // Lista base de productos para modo GRID (se llenará desde el backend)
        var remoteProducts: List<Product> = emptyList()

        val recyclerView = findViewById<RecyclerView>(R.id.products_recycler_view)
        val categoriesCompose = findViewById<ComposeView>(R.id.categories_compose)
        val searchView = findViewById<SearchView>(R.id.search_view_products)
        val btnVerTienda = findViewById<ImageButton>(R.id.btn_ver_tienda)

        // Configuración básica del SearchView
        searchView.setIconifiedByDefault(false)
        searchView.clearFocus()

        // Mejorar visibilidad del texto y de los íconos del SearchView
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        searchEditText.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))

        val searchCloseButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchCloseButton.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))

        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon)
        searchIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))

        btnVerTienda.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }

        if (mode == "GRID") {
            // Modo productos (grid) usando backend Django (/api/home/)
            recyclerView.visibility = View.VISIBLE
            categoriesCompose.visibility = View.GONE
            searchView.visibility = View.VISIBLE

            recyclerView.layoutManager = GridLayoutManager(this, 2)
            val adapter = ViewHolderAdapter(this, emptyList())
            recyclerView.adapter = adapter

            // Cargar productos reales desde /api/home/
            lifecycleScope.launch {
                try {
                    val resp = ApiClient.apiService.getHome(
                        // Por ahora no filtramos por categoría backend; mostramos el mismo listado general
                        categoryId = null,
                        query = if (initialQuery.isNotEmpty()) initialQuery else null,
                        sizeId = null,
                        colorId = null,
                        page = 1,
                        limit = 20
                    )
                    if (resp.isSuccessful) {
                        val body = resp.body()
                        val apiProducts = body?.data?.featured_products.orEmpty()

                        remoteProducts = apiProducts.map { p ->
                            Product(
                                id = p.id,
                                name = p.nombre,
                                price = "S/ ${p.precio}",
                                imageRes = R.drawable.modelo_ropa,
                                imageUrl = p.image_preview,
                                description = p.descripcion
                            )
                        }

                        adapter.updateList(remoteProducts)

                        if (initialQuery.isNotEmpty()) {
                            searchView.setQuery(initialQuery, false)
                            val filtered = remoteProducts.filter { it.name.contains(initialQuery, ignoreCase = true) }
                            adapter.updateList(filtered)
                        }
                    }
                } catch (_: Exception) {
                    // Si falla la API dejamos la lista vacía por ahora
                }
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val text = newText?.trim().orEmpty()
                    val baseList = if (remoteProducts.isNotEmpty()) remoteProducts else emptyList()
                    val filtered = if (text.isEmpty()) {
                        baseList
                    } else {
                        baseList.filter { it.name.contains(text, ignoreCase = true) }
                    }
                    adapter.updateList(filtered)
                    return true
                }
            })
        } else {
            // Modo categorías (estilo Temu)
            recyclerView.visibility = View.GONE
            categoriesCompose.visibility = View.VISIBLE
            // Mostramos el SearchView XML en modo categorías también
            searchView.visibility = View.VISIBLE
            searchView.queryHint = "Buscar categorías"

            // Datos compartidos para la vista de categorías tipo web
            data class LeftCategory(val id: String, val nombre: String)
            data class CircleItem(val categoriaId: String, val subLabel: String, val imageUrl: String, val mapToCategory: String, val mapToSub: String)

            categoriesCompose.setContent {
                val ctx = this@CatalogActivity

                var selectedLeft by remember { mutableStateOf("all") }
                var backendCategories by remember { mutableStateOf<List<SimpleCategoryDto>>(emptyList()) }

                LaunchedEffect(Unit) {
                    try {
                        val res = ApiClient.apiService.getCatalogCategories()
                        if (res.isSuccessful) {
                            backendCategories = res.body()?.data.orEmpty()
                        }
                    } catch (_: Exception) {
                    }
                }

                // Construir leftCategories dinámicamente a partir del backend
                val leftCategories = listOf(LeftCategory("all", "Todas")) +
                        backendCategories.map { cat ->
                            LeftCategory(cat.id.toString(), cat.nombre)
                        }

                // Círculos por tipo de producto (mapToSub ajusta singular/plural para búsquedas)
                val circleItems = backendCategories.map { cat ->
                    val nombre = cat.nombre
                    val key = nombre.lowercase()
                    val mapToSub = when (key) {
                        "vestidos" -> "Vestido"
                        "pantalones" -> "Pantalón"
                        "polos" -> "Polo"
                        "camisas" -> "Camisa"
                        "casacas" -> "Casaca"
                        "faldas" -> "Falda"
                        else -> nombre
                    }

                    val imageUrl = when (key) {
                        "camisas" -> "https://images.pexels.com/photos/7697309/pexels-photo-7697309.jpeg?auto=compress&cs=tinysrgb&w=600"
                        "casacas" -> "https://cuerosvelezpe.vtexassets.com/arquivos/ids/358118/1035983-02-01--Chaqueta-ebro.jpg?v=638482126287130000"
                        "pantalones" -> "https://oggi.mx/cdn/shop/files/ATRACTIONGABAKHAKIVISTA2.jpg?v=1753209029"
                        "polos" -> "https://images.pexels.com/photos/428340/pexels-photo-428340.jpeg?auto=compress&cs=tinysrgb&w=600"
                        "vestidos" -> "https://i.pinimg.com/236x/e2/04/25/e20425efb02a5185ba8f4d1cd710183d.jpg"
                        "faldas" -> "https://i.pinimg.com/564x/6a/59/49/6a5949963b7705a7c3927c044b2f4c38.jpg"
                        "zapatillas" -> "https://hushpuppiespe.vtexassets.com/arquivos/ids/336908-800-auto?v=638446729033670000&width=800&height=auto&aspect=true"
                        else -> "https://i.pinimg.com/564x/6a/59/49/6a5949963b7705a7c3927c044b2f4c38.jpg" // placeholder
                    }

                    CircleItem(
                        categoriaId = cat.id.toString(),
                        subLabel = nombre,
                        imageUrl = imageUrl,
                        mapToCategory = "Mujeres",
                        mapToSub = mapToSub
                    )
                }

                // Copiamos el texto actual de búsqueda para que Compose lo observe
                val searchText = categorySearchQuery

                Row(modifier = Modifier.fillMaxSize()) {
                    // Lista izquierda
                    LazyColumn(
                        modifier = Modifier
                            .width(120.dp)
                    ) {
                        items(leftCategories) { item ->
                            val isSelected = item.id == selectedLeft
                            Surface(
                                color = if (isSelected) Color.White else Color(0xFFF5F5F5),
                                tonalElevation = if (isSelected) 2.dp else 0.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp)
                                        .clickable { selectedLeft = item.id }
                                ) {
                                    Text(
                                        text = item.nombre,
                                        color = if (isSelected) Color(0xFF212121) else Color(0xFF757575),
                                        modifier = Modifier.padding(horizontal = 8.dp),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Círculos derecha filtrados por categoría y texto de búsqueda
                    val filteredCircles = circleItems.filter { circle ->
                        (selectedLeft == "all" || circle.categoriaId == selectedLeft) &&
                                (searchText.isBlank() || circle.subLabel.contains(searchText, ignoreCase = true))
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredCircles.size) { index ->
                            val circle = filteredCircles[index]
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.clickable {
                                    val intent = Intent(ctx, SubcategoryCatalogActivity::class.java)
                                    intent.putExtra("CATEGORY", circle.mapToCategory)
                                    intent.putExtra("SUBCATEGORY", circle.mapToSub)
                                    ctx.startActivity(intent)
                                }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF5F5F5))
                                ) {
                                    coil.compose.AsyncImage(
                                        model = circle.imageUrl,
                                        contentDescription = circle.subLabel,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = circle.subLabel,
                                    fontSize = 11.sp,
                                    color = Color(0xFF212121)
                                )
                            }
                        }
                    }
                }
            }

            // Búsqueda para categorías: actualiza categorySearchQuery y fuerza recomposición
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    categorySearchQuery = newText?.trim().orEmpty()
                    return true
                }
            })
        }

        // Navegación inferior con Compose
        val composeView = findViewById<ComposeView>(R.id.bottom_navigation_compose)
        composeView.setContent {
            var selectedTab by remember { mutableStateOf("Categorias") }

            val cartCount by remember { derivedStateOf { CartManager.cartItems.sumOf { it.quantity } } }

            val context = this@CatalogActivity
            val profileImageUri by remember { ProfileImageManager.profileImageUri }
            val firebaseUser = Firebase.auth.currentUser
            val googlePhotoUrl = firebaseUser?.photoUrl

            LaunchedEffect(Unit) {
                ProfileImageManager.loadProfileImage(context)
            }

            data class NavItem(val key: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val action: () -> Unit)

            val navItems = listOf(
                NavItem("Inicio", "Inicio", Icons.Outlined.Home) {
                    selectedTab = "Inicio"
                    finish()
                },
                NavItem("Categorias", "Categorías", Icons.Outlined.Category) {
                    selectedTab = "Categorias"
                },
                NavItem("Carrito", "Carrito", Icons.Outlined.ShoppingCart) {
                    selectedTab = "Carrito"
                    val intent = Intent(this@CatalogActivity, Carrito::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                },
                NavItem("Favoritos", "Favoritos", Icons.Outlined.Favorite) {
                    selectedTab = "Favoritos"
                    startActivity(Intent(this@CatalogActivity, FavActivity::class.java))
                },
                NavItem("Perfil", "Perfil", Icons.Outlined.Person) {
                    selectedTab = "Perfil"
                    startActivity(Intent(this@CatalogActivity, MiPerfilActivity::class.java))
                }
            )

            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp,
                windowInsets = WindowInsets(0, 0, 0, 0)
            ) {
                val selectedColor = Color(0xFFE53935)

                navItems.forEach { item ->
                    val isSelected = selectedTab == item.key
                    val iconVector = when (item.key) {
                        "Inicio" -> if (isSelected) Icons.Filled.Home else Icons.Outlined.Home
                        "Categorias" -> if (isSelected) Icons.Filled.Category else Icons.Outlined.Category
                        "Carrito" -> if (isSelected) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart
                        "Favoritos" -> if (isSelected) Icons.Filled.Favorite else Icons.Outlined.Favorite
                        "Perfil" -> if (isSelected) Icons.Filled.Person else Icons.Outlined.Person
                        else -> item.icon
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = item.action,
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedIconColor = selectedColor,
                            selectedTextColor = selectedColor,
                            unselectedIconColor = Color(0xFF212121),
                            unselectedTextColor = Color(0xFF212121)
                        ),
                        icon = {
                            if (item.key == "Carrito") {
                                BadgedBox(
                                    badge = {
                                        if (cartCount > 0) {
                                            Badge {
                                                Text(cartCount.toString(), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(iconVector, contentDescription = item.label)
                                }
                            } else if (item.key == "Perfil") {
                                when {
                                    profileImageUri != null -> {
                                        val bitmap = ProfileImageManager.getBitmapFromUri(context, profileImageUri!!)
                                        if (bitmap != null) {
                                            androidx.compose.foundation.Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = item.label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else if (googlePhotoUrl != null) {
                                            AsyncImage(
                                                model = googlePhotoUrl,
                                                contentDescription = item.label,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Icon(iconVector, contentDescription = item.label)
                                        }
                                    }
                                    googlePhotoUrl != null -> {
                                        AsyncImage(
                                            model = googlePhotoUrl,
                                            contentDescription = item.label,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                        )
                                    }
                                    else -> {
                                        Icon(iconVector, contentDescription = item.label)
                                    }
                                }
                            } else {
                                Icon(iconVector, contentDescription = item.label)
                            }
                        },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    }

    private fun showFilterBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottomsheet_filter, null)
        dialog.setContentView(view)

        val chipHombres = view.findViewById<com.google.android.material.chip.Chip>(R.id.chipHombres)
        val chipMujeres = view.findViewById<com.google.android.material.chip.Chip>(R.id.chipMujeres)
        val chipNinos = view.findViewById<com.google.android.material.chip.Chip>(R.id.chipNinos)
        val btnApplyFilter = view.findViewById<MaterialButton>(R.id.btnApplyFilter)

        // Marcar el chip actual al abrir
        val currentCategory = intent.getStringExtra("CATEGORY")
        when (currentCategory) {
            "Hombres" -> chipHombres.isChecked = true
            "Mujeres" -> chipMujeres.isChecked = true
            "Niños" -> chipNinos.isChecked = true
        }

        btnApplyFilter.setOnClickListener {
            dialog.dismiss()
            val category: String = when {
                chipHombres.isChecked -> "Hombres"
                chipMujeres.isChecked -> "Mujeres"
                chipNinos.isChecked -> "Niños"
                else -> intent.getStringExtra("CATEGORY") ?: "Hombres"
            }

            // Inicia la nueva CatalogActivity con la categoría seleccionada
            val intent = Intent(this, CatalogActivity::class.java)
            intent.putExtra("CATEGORY", category)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }

        dialog.show()
    }
}