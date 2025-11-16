package com.ropa.smartfashionecommerce.model
// o .detalles, según dónde lo crees

import com.google.firebase.Timestamp

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Timestamp? = null,
    val isVerifiedPurchase: Boolean = false
)