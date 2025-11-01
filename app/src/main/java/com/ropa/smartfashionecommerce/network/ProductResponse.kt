package com.ropa.smartfashionecommerce.network

data class ProductListResponse(
    val status: String,
    val data: List<ProductResponse>
)

data class ProductResponse(
    val id: Int,
    val nombre: String,
    val descripcion: String?,
    val precio: String,
    val precio_descuento: String?,
    val categoria: CategoriaResponse?,
    val image_preview: String?,
    val stock_total: Int
)

data class CategoriaResponse(
    val id: Int,
    val nombre: String
)
