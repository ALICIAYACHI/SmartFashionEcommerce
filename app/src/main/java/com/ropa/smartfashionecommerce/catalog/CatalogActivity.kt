package com.ropa.smartfashionecommerce.catalog

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity
import android.content.Intent
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CatalogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_catalog)

        // RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.products_recycler_view)
        recyclerView.layoutManager = GridLayoutManager(this, 2) // 2 columnas verticales

        // Lista de prueba para diseño
        val dummyList = List(10) { index ->
            Product("Prenda $index", "S/ ${100 + index}", R.drawable.fondo)
        }

        // Adapter
        recyclerView.adapter = ProductAdapter(dummyList)

        // 🔹 Botón para ir a Mi Perfil
        val btnPerfil = findViewById<FloatingActionButton>(R.id.btn_perfil)
        btnPerfil.setOnClickListener {
            val intent = Intent(this, MiPerfilActivity::class.java)
            startActivity(intent)
        }
    }
}

// Clase de ejemplo para producto
data class Product(val name: String, val price: String, val imageRes: Int)
