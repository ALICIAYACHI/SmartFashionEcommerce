package com.ropa.smartfashionecommerce.catalog

data class Product(
    val id: Int,                 // id real del producto en backend
    val name: String,
    val price: String,
    val imageRes: Int,
    val imageUrl: String? = null,
    val colorTag: String? = null,
    val sizeTag: String? = null,
    val description: String? = null
)
