package com.ropa.smartfashionecommerce.network

// Payload de ítems para checkout backend (/api/checkout/...)
data class CheckoutItemPayload(
    val product_id: Int,
    val size_id: Int?,
    val color_id: Int?,
    val qty: Int
)

// Request genérico que usan tanto /api/checkout/confirm/ como /api/payments/create_session/
data class CheckoutConfirmRequest(
    val userEmail: String?,
    val address_id: Int? = null,
    val items: List<CheckoutItemPayload>,
    val pre_order: String? = null,
    val platform: String? = null
)

// Respuesta de /api/checkout/confirm/
data class CheckoutConfirmResponse(
    val status: String?,
    val order_number: String?,
    val message: String? = null
)

// Respuesta de /api/payments/create_session/
data class CreateSessionResponse(
    val status: String?,
    val url: String?,
    val message: String? = null
)
