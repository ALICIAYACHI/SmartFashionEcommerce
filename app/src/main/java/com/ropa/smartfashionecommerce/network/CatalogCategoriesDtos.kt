package com.ropa.smartfashionecommerce.network

data class SimpleCategoryDto(
    val id: Int,
    val nombre: String
)

data class CatalogCategoriesResponse(
    val status: String?,
    val data: List<SimpleCategoryDto> = emptyList(),
    val message: String? = null
)
