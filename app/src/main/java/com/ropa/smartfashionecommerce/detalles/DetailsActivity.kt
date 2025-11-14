package com.ropa.smartfashionecommerce.detalles


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ropa.smartfashionecommerce.R
import android.content.Intent
// Importaciones de navegación no necesarias para esta clase
// import com.google.android.material.bottomnavigation.BottomNavigationView
// import com.ropa.smartfashionecommerce.home.HomeActivity
// import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity


class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        Toast.makeText(this, "Se cargó activity_details.xml", Toast.LENGTH_LONG).show()

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // El botón de retroceso utiliza el ícono ic_back y simplemente cierra la actividad.
        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener { finish() }

        toolbar.inflateMenu(R.menu.menu_details_toolbar)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_fav -> {
                    Toast.makeText(this, "Favorito", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // Obtener datos pasados desde el catálogo
        val productName = intent.getStringExtra("name") ?: "Producto Desconocido"
        val productPrice = intent.getStringExtra("price") ?: "S/ 0.00"
        val imageResId = intent.getIntExtra("imageRes", R.drawable.modelo_ropa) // Usar un default

        // Mostrar datos en la vista
        findViewById<TextView>(R.id.product_name).text = productName
        findViewById<TextView>(R.id.product_price).text = productPrice

        val productImage = findViewById<ImageView>(R.id.product_image)
        productImage.setImageResource(imageResId) // Usar el recurso de imagen real

        // Sizes
        val rgSizes = findViewById<RadioGroup>(R.id.rg_sizes)
        rgSizes.setOnCheckedChangeListener { _, checkedId ->
            val chosen = when (checkedId) {
                R.id.size_s -> "S"
                R.id.size_m -> "M"
                R.id.size_l -> "L"
                R.id.size_xl -> "XL"
                else -> ""
            }
            Toast.makeText(this, "Tamaño: $chosen", Toast.LENGTH_SHORT).show()
        }

        // Buttons
        val btnBuy = findViewById<Button>(R.id.btn_buy)
        val btnAdd = findViewById<Button>(R.id.btn_add_cart)

        btnBuy.setOnClickListener {
            Toast.makeText(this, "Comprar $productName ahora", Toast.LENGTH_SHORT).show()
            // Implementar lógica de checkout
        }

        btnAdd.setOnClickListener {
            // Lógica de añadir al carrito
            Toast.makeText(this, "$productName añadido al carrito", Toast.LENGTH_SHORT).show()
            // Aquí puedes llamar a una clase Singleton o Manager para añadir el ítem
        }

        // RecyclerView "More from" (opcional)
        val rvMore = findViewById<RecyclerView>(R.id.rv_more)
        rvMore.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val dummy = listOf(
            Pair("Prenda 1", R.drawable.modelo_ropa),
            Pair("Prenda 2", R.drawable.modelo_ropa),
            Pair("Prenda 3", R.drawable.modelo_ropa)
        )
        rvMore.adapter = MoreAdapter(dummy)

        // ** SECCIÓN DE NAVEGACIÓN INFERIOR ELIMINADA **
        // Se recomienda eliminar la BottomNavigationView de activity_details.xml
    }

    // Adapter simple para la lista horizontal "More from..."
    class MoreAdapter(private val items: List<Pair<String, Int>>) :
        RecyclerView.Adapter<MoreAdapter.VH>() {

        inner class VH(val view: android.view.View) : RecyclerView.ViewHolder(view) {
            val img: ImageView = view.findViewById(R.id.more_img)
            val name: TextView = view.findViewById(R.id.more_name)
        }

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): VH {
            val v = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_more_product, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val (name, res) = items[position]
            holder.img.setImageResource(res)
            holder.name.text = name
        }

        override fun getItemCount(): Int = items.size
    }
}