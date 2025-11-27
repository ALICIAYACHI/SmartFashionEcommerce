package com.ropa.smartfashionecommerce.model

// DTOs para el detalle de producto y catálogos de tallas/colores

// /api/sizes/
data class SizeDto(
    val id: Int,
    val nombre: String,
    val tipo: String? = null
)

// /api/colors/
data class ColorDto(
    val id: Int,
    val nombre: String,
    val codigo_hex: String? = null
)

// Variantes dentro de /api/products/{id}/
data class ProductVariantDto(
    val size_id: Int,
    val color_id: Int,
    val stock: Int
)

// Parte "product" del detalle. No la usamos aún directamente en Android, pero la modelamos por compatibilidad.
data class ProductDetailProductDto(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val precio_descuento: Double?,
    val categoria: Categoria,
    val image_preview: String?,
    val stock_total: Int
)

// Estructura completa de data en /api/products/{id}/
data class ProductDetailData(
    val product: ProductDetailProductDto?,
    val images: List<String>?,
    val imagesByColor: Map<String, List<String>>?,
    val imagesByVariant: Map<String, List<String>>?,
    val variants: List<ProductVariantDto>?,
    val related: List<Producto>?
)
