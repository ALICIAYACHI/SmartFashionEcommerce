package com.ropa.smartfashionecommerce.network

// Body para crear una solicitud de devolución / reembolso en /api/returns/
data class ReturnRequestBody(
    val order_number: String,
    val motivo: String,
    val descripcion: String? = null,
    val metodo: String,
    val telefono: String? = null,
    val userEmail: String? = null
)

// Respuesta básica de /api/returns/
data class ReturnResponse(
    val status: String?,
    val id: Int? = null,
    val message: String? = null
)

data class ReturnItemDto(
    val id: Int,
    val order_number: String,
    val motivo: String,
    val descripcion: String?,
    val metodo: String,
    val estado: String,
    val created_at: String?
)

data class ReturnListResponse(
    val status: String?,
    val data: List<ReturnItemDto> = emptyList(),
    val message: String? = null
)
