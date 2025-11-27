package com.ropa.smartfashionecommerce.model

// Representa el campo "data" que devuelve el endpoint /api/products/ de Django

data class ProductListData(
    val products: List<Producto>,
    val pagination: Pagination
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int
)
