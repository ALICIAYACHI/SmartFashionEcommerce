package com.ropa.smartfashionecommerce.model

// DTOs que representan la respuesta JSON del backend Java
// Product.java tiene: id, nombre, descripcion, precio, categoria, imagePreview

data class CategoryApiDto(
    val id: Long?,
    val nombre: String?
)

data class ProductApiDto(
    val id: Long?,
    val nombre: String?,
    val descripcion: String?,
    val precio: Double?,
    val imagePreview: String?,
    val categoria: CategoryApiDto?
)
