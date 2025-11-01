package com.ropa.smartfashionecommerce.model

// 🔹 Clase para categorías (se mantiene igual)
data class Categoria(
    val id: Int,
    val nombre: String
)

// 🔹 Clase Producto actualizada para soportar tanto productos de API como locales
data class Producto(
    val id: Int,
    val nombre: String,
    val precio: String,
    val descripcion: String? = null,
    val categoria: Categoria? = null,
    val image_preview: String? = null,     // URL de imagen (API)
    val stock_total: Int = 0,              // Stock del producto
    val localImageRes: Int? = null         // Imagen local (drawable)
)
