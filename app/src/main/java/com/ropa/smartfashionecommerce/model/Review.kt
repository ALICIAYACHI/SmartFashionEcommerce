package com.ropa.smartfashionecommerce.model
// o .detalles, según dónde lo crees

import com.google.firebase.Timestamp

data class Review(
    val productId: String = "",
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val rating: Int = 0,
    val comment: String = "",
    val createdAt: Timestamp? = null,
    val isVerifiedPurchase: Boolean = false,
    val likedUserIds: List<String> = emptyList()
)