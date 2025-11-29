package com.ropa.smartfashionecommerce.model

data class UserOrdersResponse(
    val status: String,
    val data: List<UserOrderDto>
)

data class UserOrderDto(
    val order_id: Int,
    val order_number: String,
    val created_at: String?,
    val subtotal: Double,
    val igv: Double,
    val total: Double,
    val items: List<UserOrderItemDto>,
    val envio: UserOrderEnvioDto?
)

data class UserOrderItemDto(
    val product_id: Int,
    val name: String,
    val image: String?,
    val qty: Int,
    val size_id: Int?,
    val color_id: Int?,
    val amount: Double
)

data class UserOrderEnvioDto(
    val destinatario: String?,
    val direccion: String?,
    val region: String?,
    val email: String?,
    val telefono: String?,
    val status: String?,
    val created_at: String?
)
