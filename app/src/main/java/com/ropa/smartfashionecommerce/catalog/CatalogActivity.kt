package com.ropa.smartfashionecommerce.catalog

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import android.view.View
import android.graphics.drawable.GradientDrawable

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ropa.smartfashionecommerce.R
import androidx.appcompat.R as AppCompatR

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.cardview.widget.CardView

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

        // Datos dummy para productos
        val dummyList = when (category) {
            "Hombres" -> listOf(
                Product("Camisa Hombre", "S/120", R.drawable.hombres),
                Product("Pantalón Hombre", "S/150", R.drawable.hombres),
                Product("Zapatos Hombre", "S/200", R.drawable.hombres),
                Product("Camisa Hombre", "S/120", R.drawable.hombres),
                Product("Camisa Hombre", "S/120", R.drawable.hombres),
                Product("Camisa Hombre", "S/120", R.drawable.hombres)
            )
            "Mujeres" -> listOf(
                Product("Blusa Mujer", "S/130", R.drawable.mujeres),
                Product("Falda Mujer", "S/160", R.drawable.mujeres),
                Product("Tacones Mujer", "S/220", R.drawable.mujeres),
                Product("Camisa Hombre", "S/120", R.drawable.mujeres),
                Product("Camisa Hombre", "S/120", R.drawable.mujeres),
                Product("Camisa Hombre", "S/120", R.drawable.mujeres)
            )
            "Niños" -> listOf(
                Product("Camiseta Niño", "S/80", R.drawable.nino),
                Product("Pantalón Niño", "S/100", R.drawable.nino),
                Product("Zapatillas Niño", "S/120", R.drawable.nino),
                Product("Camisa Hombre", "S/120", R.drawable.nino),
                Product("Camisa Hombre", "S/120", R.drawable.nino),
                Product("Camisa Hombre", "S/120", R.drawable.nino)
            )
            else -> emptyList()
        }

        val recyclerView = findViewById<RecyclerView>(R.id.products_recycler_view)
        val categoriesCompose = findViewById<ComposeView>(R.id.categories_compose)
        val searchView = findViewById<SearchView>(R.id.search_view_products)
        val btnSearchIcon = findViewById<android.widget.ImageButton>(R.id.btn_search_catalog)

        // Mejorar visibilidad del buscador (solo borde negro, fondo del mismo color que la pantalla y texto negro)
        searchView.setIconifiedByDefault(false)
        searchView.clearFocus()

        val searchText = searchView.findViewById<android.widget.TextView>(AppCompatR.id.search_src_text)
        val searchIcon = searchView.findViewById<android.widget.ImageView>(AppCompatR.id.search_mag_icon)

        searchText?.let {
            it.setTextColor(android.graphics.Color.BLACK)
            it.setHintTextColor(android.graphics.Color.DKGRAY)
            it.textSize = 16f
        }

        // Ocultar ícono de lupa interno para que solo se vea el de la derecha
        searchIcon?.visibility = android.view.View.GONE

        val plate = searchView.findViewById<android.view.View>(AppCompatR.id.search_plate)
        plate?.background = GradientDrawable().apply {
            // Color de fondo muy claro para que el borde se note
            setColor(android.graphics.Color.parseColor("#FAFAFA"))
            // Borde negro un poco más grueso para que sea visible
            setStroke(3, android.graphics.Color.BLACK)
            cornerRadius = 24f
        }

        if (mode == "GRID") {
            // Modo productos (grid)
            val baseList = dummyList
            val listForRecycler = if (!subcategory.isNullOrBlank()) {
                baseList.filter { it.name.contains(subcategory, ignoreCase = true) }
            } else baseList

            recyclerView.visibility = View.VISIBLE
            categoriesCompose.visibility = View.GONE
            searchView.visibility = View.VISIBLE

            recyclerView.layoutManager = GridLayoutManager(this, 2)
            val adapter = ViewHolderAdapter(this, listForRecycler)
            recyclerView.adapter = adapter

            if (initialQuery.isNotEmpty()) {
                searchView.setQuery(initialQuery, false)
                val filtered = listForRecycler.filter { it.name.contains(initialQuery, ignoreCase = true) }
                adapter.updateList(filtered)
            }

            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val text = newText?.trim().orEmpty()
                    val filtered = if (text.isEmpty()) {
                        listForRecycler
                    } else {
                        listForRecycler.filter { it.name.contains(text, ignoreCase = true) }
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

            // Datos compartidos para la vista de categorías
            data class LeftCategory(val id: String, val nombre: String)
            data class CircleItem(val categoriaId: String, val subLabel: String, val imageUrl: String, val mapToCategory: String, val mapToSub: String)

            val leftCategories = listOf(
                LeftCategory("mujer", "Ropa de mujer"),
                LeftCategory("hombre", "Ropa de hombre"),
                LeftCategory("ninos", "Ropa de niño"),
                LeftCategory("bebe", "Ropa de bebé"),
                LeftCategory("black", "Ofertas Black Friday")
            )

            val circleItems = listOf(
                // Mujer
                CircleItem("mujer", "Vestidos de mujer", "https://i.pinimg.com/236x/e2/04/25/e20425efb02a5185ba8f4d1cd710183d.jpg", "Mujeres", "Vestido"),
                CircleItem("mujer", "Casacas de mujer", "https://cuerosvelezpe.vtexassets.com/arquivos/ids/358118/1035983-02-01--Chaqueta-ebro.jpg?v=638482126287130000", "Mujeres", "Casaca"),
                // Hombre
                CircleItem("hombre", "Casacas hombre", "https://cuerosvelezpe.vtexassets.com/arquivos/ids/358118/1035983-02-01--Chaqueta-ebro.jpg?v=638482126287130000", "Hombres", "Casaca"),
                CircleItem("hombre", "Camisas hombre", "https://images.pexels.com/photos/7697309/pexels-photo-7697309.jpeg?auto=compress&cs=tinysrgb&w=600", "Hombres", "Camisa"),
                // Niños
                CircleItem("ninos", "Ropa de niño", "https://hushpuppiespe.vtexassets.com/arquivos/ids/348758/https---s3.amazonaws.com-ecom-imagenes.forus-digital.xyz.peru-HUSHPUPPIESKIDS-HK211021504_287_1.jpg?v=638604628092130000", "Niños", "Niño"),
                CircleItem("ninos", "Pantalones niño", "https://hushpuppiespe.vtexassets.com/arquivos/ids/336908-800-auto?v=638446729033670000&width=800&height=auto&aspect=true", "Niños", "Pantalón"),
                // Bebé
                CircleItem("bebe", "Conjuntos bebé niño", "https://img.kwcdn.com/product/Fancyalgo/VirtualModelMatting/fb03dd58d895a6436b3cfee2b5d7d766.jpg?imageMogr2/auto-orient%7CimageView2/2/w/800/q/70/format/webp", "Niños", "Bebé"),
                CircleItem("bebe", "Conjuntos bebé niña", "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQmGawIE-1oJ5wtsBQ9p9DFelbIJtmzeJd8ga0iA6SW3gaTX_-VHNCs02I33J5WvOiAe0s&usqp=CAU", "Niños", "Bebé"),
                // Black Friday
                CircleItem("black", "Ofertas Black Friday", "https://www.desire.pe/cdn/shop/files/CRI_2554_398b7b94-a0a2-4dc5-be42-30995f0c04e2.png?v=1726679347", "Mujeres", "Oferta")
            )

            categoriesCompose.setContent {
                val ctx = this@CatalogActivity

                var selectedLeft by remember { mutableStateOf(leftCategories.first().id) }
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
                        circle.categoriaId == selectedLeft &&
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