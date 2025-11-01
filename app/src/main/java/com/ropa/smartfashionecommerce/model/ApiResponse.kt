package com.ropa.smartfashionecommerce.model

data class ApiResponse<T>(
    val status: String,
    val data: T
)
