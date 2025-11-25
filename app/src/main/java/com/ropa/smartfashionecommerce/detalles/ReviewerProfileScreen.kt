package com.ropa.smartfashionecommerce.detalles

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ropa.smartfashionecommerce.model.Review

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewerProfileScreen(
    userId: String,
    userName: String,
    onBack: () -> Unit
) {
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }

    LaunchedEffect(userId, userName) {
        loadReviewsForProfile(userId = userId, userName = userName) { loaded ->
            reviews = loaded
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de $userName") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .background(Color.White)
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${reviews.size} reseñas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (reviews.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Este usuario aún no tiene reseñas.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reviews) { review ->
                        ReviewCard(
                            review = review,
                            currentUserId = FirebaseAuth.getInstance().currentUser?.uid,
                            onUserClick = {},
                            onEdit = {},
                            onDelete = {}
                        )
                    }
                }
            }
        }
    }
}

private fun loadReviewsForProfile(
    userId: String,
    userName: String,
    onReviewsLoaded: (List<Review>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()

    // Recorremos todos los productos y obtenemos TODAS las reseñas,
    // luego filtramos por userName en el cliente
    db.collection("products")
        .get()
        .addOnSuccessListener { productsSnapshot ->
            if (productsSnapshot.isEmpty) {
                onReviewsLoaded(emptyList())
                return@addOnSuccessListener
            }

            val allReviews = mutableListOf<Review>()
            var pending = productsSnapshot.size()

            fun finishIfDone() {
                pending--
                if (pending == 0) {
                    val sorted = allReviews.sortedByDescending { it.createdAt?.toDate() }
                    onReviewsLoaded(sorted)
                }
            }

            for (productDoc in productsSnapshot.documents) {
                val productId = productDoc.id
                productDoc.reference
                    .collection("reviews")
                    .get()
                    .addOnSuccessListener { reviewsSnapshot ->
                        if (!reviewsSnapshot.isEmpty) {
                            reviewsSnapshot.documents.forEach { doc ->
                                val docUserName = doc.getString("userName") ?: ""
                                if (docUserName == userName) {
                                    val review = try {
                                        Review(
                                            productId = productId,
                                            id = doc.id,
                                            userId = doc.getString("userId") ?: "",
                                            userName = docUserName,
                                            userPhotoUrl = doc.getString("userPhotoUrl"),
                                            rating = doc.getLong("rating")?.toInt() ?: 0,
                                            comment = doc.getString("comment") ?: "",
                                            createdAt = doc.getTimestamp("createdAt"),
                                            isVerifiedPurchase = doc.getBoolean("isVerifiedPurchase") ?: false,
                                            likedUserIds = (doc.get("likedUserIds") as? List<*>)
                                                ?.filterIsInstance<String>()
                                                ?: emptyList()
                                        )
                                    } catch (e: Exception) {
                                        null
                                    }
                                    review?.let { allReviews.add(it) }
                                }
                            }
                        }
                        finishIfDone()
                    }
                    .addOnFailureListener {
                        finishIfDone()
                    }
            }
        }
        .addOnFailureListener {
            onReviewsLoaded(emptyList())
        }
}
