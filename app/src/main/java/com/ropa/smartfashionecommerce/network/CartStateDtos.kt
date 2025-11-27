package com.ropa.smartfashionecommerce.network

// Item que viaja entre app y backend en /api/cart/
data class CartItemPayload(
    val product_id: Int,
    val qty: Int,
    val size_id: Int? = null,
    val color_id: Int? = null
)

// Body para setCartState y data de respuesta
 data class CartStateData(
    val items: List<CartItemPayload> = emptyList()
)

// Respuesta de /api/cart/
data class CartStateResponse(
    val status: String?,
    val data: CartStateData? = null,
    val message: String? = null
)
