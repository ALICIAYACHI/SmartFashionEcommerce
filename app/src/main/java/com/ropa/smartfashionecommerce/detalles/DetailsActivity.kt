package com.ropa.smartfashionecommerce.detalles


import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ropa.smartfashionecommerce.R
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ropa.smartfashionecommerce.home.FavoriteItem
import com.ropa.smartfashionecommerce.home.FavoritesManager
import com.ropa.smartfashionecommerce.model.Review

// Importaciones de navegación no necesarias para esta clase
// import com.google.android.material.bottomnavigation.BottomNavigationView
// import com.ropa.smartfashionecommerce.home.HomeActivity
// import com.ropa.smartfashionecommerce.miperfil.MiPerfilActivity


class DetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        Toast.makeText(this, "Se cargó activity_details.xml", Toast.LENGTH_LONG).show()

        FavoritesManager.initialize(this)

        val productName = intent.getStringExtra("name") ?: "Producto Desconocido"
        val productPrice = intent.getStringExtra("price") ?: "S/ 0.00"
        val imageResId = intent.getIntExtra("imageRes", R.drawable.modelo_ropa)
        val productId = productName // TODO: reemplazar por un ID real de producto cuando lo tengas


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
                    val favoriteItem = FavoriteItem(
                        id = productName.hashCode(),
                        name = productName,
                        price = productPrice,
                        sizes = listOf("S", "M", "L", "XL"),
                        imageRes = imageResId,
                        isFavorite = true
                    )
                    FavoritesManager.addFavorite(this, favoriteItem)
                    Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }


        // Mostrar datos en la vista
        findViewById<TextView>(R.id.product_name).text = productName
        findViewById<TextView>(R.id.product_price).text = productPrice

        val productImage = findViewById<ImageView>(R.id.product_image)

        productImage.setImageResource(imageResId) // Usar el recurso de imagen real

        val writeReview = findViewById<TextView>(R.id.btn_write_review)
        writeReview?.setOnClickListener {
            showReviewDialog(productId, productName)
        }


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
        val btnFav = findViewById<ImageButton>(R.id.btn_fav)

        btnBuy.setOnClickListener {
            Toast.makeText(this, "Comprar $productName ahora", Toast.LENGTH_SHORT).show()
            // Implementar lógica de checkout
        }

        btnAdd.setOnClickListener {
            // Lógica de añadir al carrito
            Toast.makeText(this, "$productName añadido al carrito", Toast.LENGTH_SHORT).show()
            // Aquí puedes llamar a una clase Singleton o Manager para añadir el ítem
        }

        btnFav.setOnClickListener {
            val favoriteItem = FavoriteItem(
                id = productName.hashCode(),
                name = productName,
                price = productPrice,
                sizes = listOf("S", "M", "L", "XL"),
                imageRes = imageResId,
                isFavorite = true
            )
            FavoritesManager.addFavorite(this, favoriteItem)
            Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
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

    private fun showReviewDialog(productId: String, productName: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.ratingBar)
        val commentEdit = dialogView.findViewById<EditText>(R.id.editComment)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reseñar $productName")
            .setView(dialogView)
            .setPositiveButton("Enviar") { _, _ ->
                val rating = ratingBar.rating.toInt()
                val comment = commentEdit.text.toString().trim()
                if (rating in 1..5) {
                    submitReview(productId, rating, comment)
                } else {
                    Toast.makeText(this, "Selecciona una calificación", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun submitReview(productId: String, rating: Int, comment: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Inicia sesión para reseñar", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val reviewData = mapOf(
            "userId" to user.uid,
            "userName" to (user.displayName ?: "Cliente"),
            "rating" to rating,
            "comment" to comment,
            "createdAt" to FieldValue.serverTimestamp(),
            "isVerifiedPurchase" to false,
            "status" to "APPROVED"
        )

        db.collection("products")
            .document(productId)
            .collection("reviews")
            .add(reviewData)
            .addOnSuccessListener {
                Toast.makeText(this, "Gracias por tu reseña", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al guardar reseña", Toast.LENGTH_SHORT).show()
            }
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