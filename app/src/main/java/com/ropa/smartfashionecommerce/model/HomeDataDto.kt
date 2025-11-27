package com.ropa.smartfashionecommerce.model

// DTO para /api/home/ (catálogo web)
// Coincide con la estructura que usa el frontend React (Catalog.jsx / Catalogo.jsx)

data class HomeDataDto(
    val categories: List<Categoria> = emptyList(),
    val featured_products: List<Producto> = emptyList(),
    val pagination: Pagination = Pagination(page = 1, limit = 12, total = 0)
)
