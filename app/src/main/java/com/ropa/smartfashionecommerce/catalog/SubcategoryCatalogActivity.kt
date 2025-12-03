package com.ropa.smartfashionecommerce.catalog

import android.content.Intent
import android.os.Bundle
import android.graphics.drawable.GradientDrawable
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ropa.smartfashionecommerce.R
import com.ropa.smartfashionecommerce.network.ApiClient
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.appcompat.R as AppCompatR

class SubcategoryCatalogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subcategory_catalog)

        val category = intent.getStringExtra("CATEGORY") ?: "Mujeres"
        val subcategory = intent.getStringExtra("SUBCATEGORY") ?: "Vestidos"

        val titleView = findViewById<android.widget.TextView>(R.id.tvSubcategoryTitle)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_subcategory_products)
        val searchView = findViewById<SearchView>(R.id.search_view_subcategory)
        val btnFilter = findViewById<android.widget.Button>(R.id.btn_filter)
        val btnBack = findViewById<android.widget.ImageButton>(R.id.btn_back_subcategory)
        val btnSearchIcon = findViewById<android.widget.ImageButton>(R.id.btn_search_subcategory)

        // Ocultamos el título para que no se muestre "Camisa de Mujeres", etc.
        titleView.visibility = android.view.View.GONE

        // Botón atrás: cerrar esta activity
        btnBack.setOnClickListener { finish() }

        recyclerView.layoutManager = GridLayoutManager(this, 2)
        val adapter = ViewHolderAdapter(this, emptyList())
        recyclerView.adapter = adapter

        // Lista remota cargada desde /api/home/
        var remoteProducts: List<Product> = emptyList()

        val categoryId = intent.getIntExtra("CATEGORY_ID", 0)

        // Cargar productos reales filtrando por categoryId
        lifecycleScope.launch {
            try {
                val resp = ApiClient.apiService.getHome(
                    // Filtrar por ID de categoría en lugar de text search
                    categoryId = if (categoryId > 0) categoryId else null,
                    query = null,
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
                            imageRes = android.R.color.transparent,
                            imageUrl = p.image_preview,
                            description = p.descripcion
                        )
                    }

                    adapter.updateList(remoteProducts)
                }
            } catch (_: Exception) {
                // Si falla la API dejamos la lista vacía
            }
        }

        // Estado de filtros/orden/búsqueda
        var currentColorFilter: String? = null
        var currentSizeFilter: String? = null
        var currentSortOption: String = "DESTACADOS"
        var currentSearchQuery: String = ""

        fun Product.priceValue(): Double {
            return price
                .replace("S/", "")
                .replace("s/", "")
                .replace(" ", "")
                .replace(",", ".")
                .toDoubleOrNull() ?: 0.0
        }

        fun applyFiltersAndSort() {
            // Partimos de la lista remota cargada desde la API
            var sequence = remoteProducts.asSequence()

            if (currentSearchQuery.isNotEmpty()) {
                sequence = sequence.filter { it.name.contains(currentSearchQuery, ignoreCase = true) }
            }

            currentColorFilter?.let { color ->
                sequence = sequence.filter { it.colorTag == color }
            }

            currentSizeFilter?.let { size ->
                sequence = sequence.filter { it.sizeTag == size }
            }

            val result = when (currentSortOption) {
                "PRECIO_ASC" -> sequence.sortedBy { it.priceValue() }.toList()
                "PRECIO_DESC" -> sequence.sortedByDescending { it.priceValue() }.toList()
                else -> sequence.toList() // DESTACADOS y otros
            }

            adapter.updateList(result)
        }

        fun showFilterSheet(showSizes: Boolean, showColors: Boolean) {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_filters, null)

            var tempColorFilter: String? = currentColorFilter
            var tempSizeFilter: String? = currentSizeFilter

            fun selectColor(tag: String?) {
                tempColorFilter = tag
            }

            fun selectSize(tag: String?) {
                tempSizeFilter = tag
            }

            // Referencias de vistas para mostrar/ocultar secciones
            val sizeTitle = view.findViewById<android.widget.TextView>(R.id.tv_filter_size_title)
            val sizeLayout = view.findViewById<android.view.View>(R.id.layout_filter_sizes)
            val divider = view.findViewById<android.view.View>(R.id.view_filter_divider)
            val colorTitle = view.findViewById<android.widget.TextView>(R.id.tv_filter_color_title)
            val colorLayout = view.findViewById<android.view.View>(R.id.layout_filter_colors)

            // Mostrar solo lo que corresponde según quién abrió el panel
            sizeTitle.visibility = if (showSizes) android.view.View.VISIBLE else android.view.View.GONE
            sizeLayout.visibility = if (showSizes) android.view.View.VISIBLE else android.view.View.GONE
            colorTitle.visibility = if (showColors) android.view.View.VISIBLE else android.view.View.GONE
            colorLayout.visibility = if (showColors) android.view.View.VISIBLE else android.view.View.GONE
            divider.visibility = if (showSizes && showColors) android.view.View.VISIBLE else android.view.View.GONE

            // Colores
            val colorNegro = view.findViewById<android.view.View>(R.id.item_color_negro)
            val colorBlanco = view.findViewById<android.view.View>(R.id.item_color_blanco)
            val colorRojo = view.findViewById<android.view.View>(R.id.item_color_rojo)
            val colorAzul = view.findViewById<android.view.View>(R.id.item_color_azul)
            val circleNegro = view.findViewById<android.view.View>(R.id.view_color_negro)
            val circleBlanco = view.findViewById<android.view.View>(R.id.view_color_blanco)
            val circleRojo = view.findViewById<android.view.View>(R.id.view_color_rojo)
            val circleAzul = view.findViewById<android.view.View>(R.id.view_color_azul)

            val colorMap = listOf(
                "negro" to circleNegro,
                "blanco" to circleBlanco,
                "rojo" to circleRojo,
                "azul" to circleAzul
            )

            fun updateColorSelection() {
                colorMap.forEach { (tag, viewCircle) ->
                    val selected = tempColorFilter == tag
                    viewCircle.setBackgroundResource(
                        if (selected) R.drawable.bg_color_circle_selected else R.drawable.bg_color_circle
                    )
                }
            }

            // Estado inicial de colores
            updateColorSelection()

            colorNegro.setOnClickListener { selectColor("negro"); updateColorSelection() }
            colorBlanco.setOnClickListener { selectColor("blanco"); updateColorSelection() }
            colorRojo.setOnClickListener { selectColor("rojo"); updateColorSelection() }
            colorAzul.setOnClickListener { selectColor("azul"); updateColorSelection() }

            // Tallas (S, M, L, XL)
            val btnSizeS = view.findViewById<android.widget.Button>(R.id.btn_size_s)
            val btnSizeM = view.findViewById<android.widget.Button>(R.id.btn_size_m)
            val btnSizeL = view.findViewById<android.widget.Button>(R.id.btn_size_l)
            val btnSizeXL = view.findViewById<android.widget.Button>(R.id.btn_size_xl)

            val allSizeButtons = listOf(
                "S" to btnSizeS,
                "M" to btnSizeM,
                "L" to btnSizeL,
                "XL" to btnSizeXL
            )

            fun updateSizeSelection() {
                allSizeButtons.forEach { (tag, button) ->
                    val selected = tempSizeFilter == tag
                    button.isSelected = selected
                    button.setBackgroundColor(
                        if (selected) android.graphics.Color.parseColor("#111111") else android.graphics.Color.TRANSPARENT
                    )
                    button.setTextColor(
                        if (selected) android.graphics.Color.WHITE else android.graphics.Color.parseColor("#111111")
                    )
                }
            }

            // Estado inicial de tallas
            updateSizeSelection()

            btnSizeS.setOnClickListener { selectSize("S"); updateSizeSelection() }
            btnSizeM.setOnClickListener { selectSize("M"); updateSizeSelection() }
            btnSizeL.setOnClickListener { selectSize("L"); updateSizeSelection() }
            btnSizeXL.setOnClickListener { selectSize("XL"); updateSizeSelection() }

            val btnReset = view.findViewById<android.widget.Button>(R.id.btn_reset_filters)
            val btnApply = view.findViewById<android.widget.Button>(R.id.btn_apply_filters)

            btnReset.setOnClickListener {
                currentColorFilter = null
                currentSizeFilter = null
                applyFiltersAndSort()
                dialog.dismiss()
            }

            btnApply.setOnClickListener {
                currentColorFilter = tempColorFilter
                currentSizeFilter = tempSizeFilter
                applyFiltersAndSort()
                dialog.dismiss()
            }

            dialog.setContentView(view)
            dialog.show()
        }

        btnFilter.setOnClickListener { showFilterSheet(showSizes = true, showColors = true) }

        // Botón Talla: solo mostrar sección de tallas
        val btnSizeFilter = findViewById<android.widget.Button>(R.id.btn_size_filter)
        btnSizeFilter.setOnClickListener { showFilterSheet(showSizes = true, showColors = false) }

        // Botón Color: solo mostrar sección de colores
        val btnColorFilter = findViewById<android.widget.Button>(R.id.btn_color_filter)
        btnColorFilter.setOnClickListener { showFilterSheet(showSizes = false, showColors = true) }

        // Ordenar por
        val btnSort = findViewById<android.widget.Button>(R.id.btn_sort)
        btnSort.setOnClickListener {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_sort, null)

            val tvFeatured = view.findViewById<android.widget.TextView>(R.id.tv_sort_featured)
            val tvBestSelling = view.findViewById<android.widget.TextView>(R.id.tv_sort_best_selling)
            val tvRecent = view.findViewById<android.widget.TextView>(R.id.tv_sort_recent)
            val tvPriceLowHigh = view.findViewById<android.widget.TextView>(R.id.tv_sort_price_low_high)
            val tvPriceHighLow = view.findViewById<android.widget.TextView>(R.id.tv_sort_price_high_low)

            val allOptions = listOf(
                "DESTACADOS" to tvFeatured,
                "BEST_SELLING" to tvBestSelling,
                "RECENT" to tvRecent,
                "PRECIO_ASC" to tvPriceLowHigh,
                "PRECIO_DESC" to tvPriceHighLow
            )

            fun updateSortSelection() {
                val selectedColor = android.graphics.Color.parseColor("#FF6D00")
                val normalColor = android.graphics.Color.parseColor("#111111")

                allOptions.forEach { (key, textView) ->
                    if (currentSortOption == key) {
                        textView.setTextColor(selectedColor)
                        textView.setTypeface(textView.typeface, android.graphics.Typeface.BOLD)
                    } else {
                        textView.setTextColor(normalColor)
                        textView.setTypeface(textView.typeface, android.graphics.Typeface.NORMAL)
                    }
                }
            }

            // Estado inicial
            updateSortSelection()

            tvFeatured.setOnClickListener {
                currentSortOption = "DESTACADOS"
                updateSortSelection()
                applyFiltersAndSort()
                dialog.dismiss()
            }
            tvBestSelling.setOnClickListener {
                currentSortOption = "BEST_SELLING" // por ahora mismo comportamiento visual
                updateSortSelection()
                applyFiltersAndSort()
                dialog.dismiss()
            }
            tvRecent.setOnClickListener {
                currentSortOption = "RECENT" // por ahora mismo comportamiento visual
                updateSortSelection()
                applyFiltersAndSort()
                dialog.dismiss()
            }
            tvPriceLowHigh.setOnClickListener {
                currentSortOption = "PRECIO_ASC"
                updateSortSelection()
                applyFiltersAndSort()
                dialog.dismiss()
            }
            tvPriceHighLow.setOnClickListener {
                currentSortOption = "PRECIO_DESC"
                updateSortSelection()
                applyFiltersAndSort()
                dialog.dismiss()
            }

            dialog.setContentView(view)
            dialog.show()
        }

        searchView.setIconifiedByDefault(false)
        searchView.clearFocus()

        // ✅ Mejorar visibilidad del texto y de los íconos del SearchView
        val searchEditText = searchView.findViewById<EditText>(AppCompatR.id.search_src_text)
        searchEditText.setTextColor(ContextCompat.getColor(this, android.R.color.black))
        searchEditText.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray))

        val searchCloseButton = searchView.findViewById<ImageView>(AppCompatR.id.search_close_btn)
        searchCloseButton.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))

        val searchIcon = searchView.findViewById<ImageView>(AppCompatR.id.search_mag_icon)
        searchIcon.setColorFilter(ContextCompat.getColor(this, android.R.color.darker_gray))

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                val text = newText?.trim().orEmpty()
                currentSearchQuery = text
                applyFiltersAndSort()
                return true
            }
        })

        // Ícono de lupa a la derecha: dispara la búsqueda actual (no hace nada extra por ahora)
        btnSearchIcon.setOnClickListener {
            // Podríamos cerrar el teclado o forzar applyFiltersAndSort, pero ya se aplica en onQueryTextChange
            applyFiltersAndSort()
        }
    }
}
