package com.ropa.smartfashionecommerce.catalog

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.home.HomeActivity
import com.ropa.smartfashionecommerce.home.FavActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.cardview.widget.CardView

class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalog)

        val category = intent.getStringExtra("CATEGORY") ?: "Hombres"

        // === MODIFICACIONES DE DISEÑO SIN AFECTAR FUNCIONALIDAD ===

        // 1. Título dinámico: Muestra la categoría actual en el TextView del toolbar
        val titleTextView = findViewById<TextView>(R.id.tv_title)
        titleTextView.text = "Catálogo de $category"

        // 2. Funcionalidad para el nuevo botón "Atrás" (ahora un CardView en el XML)
        val backCard = findViewById<CardView>(R.id.btn_back_card)
        backCard.setOnClickListener {
            // Mantiene la funcionalidad de retroceso
            finish()
        }

        // 3. CORRECCIÓN DEL BOTÓN PERFIL
        val btnPerfilImage = findViewById<ImageView>(R.id.btn_perfil_image)
        btnPerfilImage.setOnClickListener {
            startActivity(Intent(this, MiPerfilActivity::class.java))
        }

        // ==========================================================

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
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        // Usamos el adaptador corregido que apunta a DetailsActivity
        recyclerView.adapter = ViewHolderAdapter(this, dummyList)

        val filterButton = findViewById<androidx.cardview.widget.CardView>(R.id.filter_card)
        filterButton.setOnClickListener { showFilterBottomSheet() }

        // Navegación inferior con Compose (SECCIÓN ESTABILIZADA)
        val composeView = findViewById<ComposeView>(R.id.bottom_navigation_compose)
        composeView.setContent {
            // Usamos "Catalog" como el valor inicial para que la lógica de selección no cierre la app.
            var selectedTab by remember { mutableStateOf("Catalog") }

            // Definimos la lista de ítems de navegación
            val navItems = listOf(
                // Triple(etiqueta, ícono) { acción al clic }
                Triple("Home", Icons.Default.Home) {
                    selectedTab = "Home"
                    finish() // ✅ Al cerrar, vuelve a la HomeActivity (CORRECCIÓN)
                },
                Triple("Cart", Icons.Default.ShoppingCart) {
                    selectedTab = "Cart"
                    val intent = Intent(this@CatalogActivity, Carrito::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                },
                Triple("Favorites", Icons.Default.Favorite) {
                    selectedTab = "Favorites"
                    startActivity(Intent(this@CatalogActivity, FavActivity::class.java))
                },
                Triple("Profile", Icons.Default.Person) {
                    selectedTab = "Profile"
                    startActivity(Intent(this@CatalogActivity, MiPerfilActivity::class.java))
                }
            )

            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp
            ) {
                // Iteramos sobre los ítems de navegación
                navItems.forEach { (label, icon, action) ->

                    // Lógica de selección estable: Si no es Home, se basa en el label. Si es Home,
                    // solo se marca si nos hemos movido de la pestaña Catálogo original.
                    val isSelected = if (label == "Home") selectedTab != "Catalog" else selectedTab == label

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = action,
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
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

            // 💡 CORRECCIÓN APLICADA AQUÍ: Limpia las instancias viejas de CatalogActivity
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            startActivity(intent)
            finish() // Cierra la instancia actual
        }

        dialog.show()
    }

}