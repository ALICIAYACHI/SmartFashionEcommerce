package com.ropa.smartfashionecommerce.catalog

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ropa.smartfashionecommerce.HomeActivity
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.carrito.Carrito
import com.ropa.smartfashionecommerce.FavActivity
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp

// Aquí importas la clase Product desde Product.kt
import com.ropa.smartfashionecommerce.catalog.Product
// Clase de ejemplo para producto


class CatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalog)

        // RecyclerView con Grid
        val recyclerView = findViewById<RecyclerView>(R.id.products_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2)

        val dummyList = List(10) { index ->
            Product("Prenda $index", "S/ ${100 + index}", R.drawable.fondo)
        }

        recyclerView.adapter = ViewHolderAdapter(this, dummyList)



        // FAB para ir a MiPerfil
        val btnPerfil = findViewById<FloatingActionButton>(R.id.btn_perfil)
        btnPerfil.setOnClickListener {
            startActivity(Intent(this, MiPerfilActivity::class.java))
        }

        // Bottom Navigation en Compose
        val composeView = findViewById<ComposeView>(R.id.bottom_navigation_compose)
        composeView.setContent {
            var selectedTab by remember { mutableStateOf("Catalog") }

            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 4.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == "Home",
                    onClick = {
                        selectedTab = "Home"
                        startActivity(Intent(this@CatalogActivity, HomeActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Cart",
                    onClick = {
                        selectedTab = "Cart"
                        startActivity(Intent(this@CatalogActivity, Carrito::class.java))
                    },
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Cart") },
                    label = { Text("Cart") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Favorites",
                    onClick = {
                        selectedTab = "Favorites"
                        startActivity(Intent(this@CatalogActivity, FavActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = selectedTab == "Profile",
                    onClick = {
                        selectedTab = "Profile"
                        startActivity(Intent(this@CatalogActivity, MiPerfilActivity::class.java))
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    }
}
