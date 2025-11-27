package com.ropa.smartfashionecommerce.carrito

data class CartItem(
    // IDs reales del backend (para checkout y stock por variante)
    val productId: Int,
    val sizeId: Int?,
    val colorId: Int?,

    // Datos legibles para mostrar en UI
    val name: String,
    val size: String,
    val color: String,
    var quantity: Int,
    val price: Double,
    val imageRes: Int,
    val imageUrl: String? = null
)
