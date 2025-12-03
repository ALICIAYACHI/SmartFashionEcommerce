package com.ropa.smartfashionecommerce.network

// DTO para una dirección guardada
data class AddressDto(
    val id: Int,
    val address: String,
    val department: String? = null,
    val province: String? = null,
    val district: String? = null,
    val postal_code: String? = null,
    val references: String? = null,
    val is_default: Boolean = false
)

// Respuesta del endpoint /api/profile/direcciones/
data class UserAddressesResponse(
    val status: String?,
    val data: List<AddressDto>? = null,
    val message: String? = null
)
