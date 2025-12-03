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
    val platform: String? = null,
    // Datos de dirección alternativa si no hay address_id
    val address: AddressData? = null,
    val phone: String? = null,
    val document_type: String? = null,
    val document_number: String? = null
)

// Datos de dirección para envío
data class AddressData(
    val address: String?,
    val department: String?,
    val province: String?,
    val district: String?,
    val postal_code: String?,
    val references: String?
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

// Respuesta del chatbot IA
data class ChatbotResponse(
    val response: String? = null,
    val message: String? = null,
    val status: String? = null
)

// Request para el chatbot - intentamos con múltiples nombres de campo
data class ChatbotRequest(
    val query: String? = null,
    val message: String? = null,
    val question: String? = null
)

data class ChatbotQueryRequest(
    val query: String,
    val question: String? = null
)
