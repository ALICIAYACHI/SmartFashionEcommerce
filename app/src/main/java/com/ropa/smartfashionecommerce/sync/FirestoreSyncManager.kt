package com.ropa.smartfashionecommerce.sync

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.ropa.smartfashionecommerce.carrito.CartItem
import com.ropa.smartfashionecommerce.home.FavoriteItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * 🔄 FirestoreSyncManager: Sincroniza Carrito y Favoritos entre web y mobile usando Firestore
 * Es más confiable que confiar solo en los endpoints del backend Django
 */
object FirestoreSyncManager {

    private val db = Firebase.firestore
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // Estructura de datos para Firestore
    data class CartItemData(
        val product_id: Int = 0,
        val qty: Int = 0,
        val size_id: Int? = null,
        val color_id: Int? = null,
        val name: String = "",
        val price: Double = 0.0,
        val imageUrl: String? = null
    )

    data class FavoriteItemData(
        val product_id: Int = 0,
        val name: String = "",
        val price: String = "",
        val imageUrl: String? = null
    )

    // ✅ Guardar carrito en Firestore
    fun saveCartToFirestore(cartItems: List<CartItem>) {
        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid
        
        Log.d("FirestoreSyncManager", "saveCartToFirestore: Guardando ${cartItems.size} items para $uid")
        
        val cartData = cartItems.map {
            CartItemData(
                product_id = it.productId,
                qty = it.quantity,
                size_id = it.sizeId,
                color_id = it.colorId,
                name = it.name,
                price = it.price,
                imageUrl = it.imageUrl
            )
        }

        ioScope.launch {
            try {
                db.collection("users").document(uid)
                    .collection("cart").document("items")
                    .set(mapOf("items" to cartData))
                    .await()
                Log.d("FirestoreSyncManager", "saveCartToFirestore: ✅ Éxito")
            } catch (e: Exception) {
                Log.e("FirestoreSyncManager", "saveCartToFirestore: Error - ${e.message}", e)
            }
        }
    }

    // ✅ Cargar carrito desde Firestore
    fun loadCartFromFirestore(onLoaded: (List<CartItemData>) -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid

        Log.d("FirestoreSyncManager", "loadCartFromFirestore: Cargando carrito para $uid")

        ioScope.launch {
            try {
                val doc = db.collection("users").document(uid)
                    .collection("cart").document("items")
                    .get()
                    .await()

                val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                val cartItems = items.mapNotNull { item ->
                    try {
                        CartItemData(
                            product_id = (item["product_id"] as? Number)?.toInt() ?: 0,
                            qty = (item["qty"] as? Number)?.toInt() ?: 0,
                            size_id = (item["size_id"] as? Number)?.toInt(),
                            color_id = (item["color_id"] as? Number)?.toInt(),
                            name = item["name"] as? String ?: "",
                            price = (item["price"] as? Number)?.toDouble() ?: 0.0,
                            imageUrl = item["imageUrl"] as? String
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                Log.d("FirestoreSyncManager", "loadCartFromFirestore: ${cartItems.size} items cargados")
                onLoaded(cartItems)
            } catch (e: Exception) {
                Log.e("FirestoreSyncManager", "loadCartFromFirestore: Error - ${e.message}", e)
                onLoaded(emptyList())
            }
        }
    }

    // ✅ Guardar favoritos en Firestore
    fun saveFavoritesToFirestore(favoriteItems: List<FavoriteItem>) {
        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid

        Log.d("FirestoreSyncManager", "saveFavoritesToFirestore: Guardando ${favoriteItems.size} items para $uid")

        val favData = favoriteItems.map {
            FavoriteItemData(
                product_id = it.id,
                name = it.name,
                price = it.price,
                imageUrl = it.imageUrl
            )
        }

        ioScope.launch {
            try {
                db.collection("users").document(uid)
                    .collection("favorites").document("items")
                    .set(mapOf("items" to favData))
                    .await()
                Log.d("FirestoreSyncManager", "saveFavoritesToFirestore: ✅ Éxito")
            } catch (e: Exception) {
                Log.e("FirestoreSyncManager", "saveFavoritesToFirestore: Error - ${e.message}", e)
            }
        }
    }

    // ✅ Cargar favoritos desde Firestore
    fun loadFavoritesFromFirestore(onLoaded: (List<FavoriteItemData>) -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid

        Log.d("FirestoreSyncManager", "loadFavoritesFromFirestore: Cargando favoritos para $uid")

        ioScope.launch {
            try {
                val doc = db.collection("users").document(uid)
                    .collection("favorites").document("items")
                    .get()
                    .await()

                val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                val favItems = items.mapNotNull { item ->
                    try {
                        FavoriteItemData(
                            product_id = (item["product_id"] as? Number)?.toInt() ?: 0,
                            name = item["name"] as? String ?: "",
                            price = item["price"] as? String ?: "",
                            imageUrl = item["imageUrl"] as? String
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                Log.d("FirestoreSyncManager", "loadFavoritesFromFirestore: ${favItems.size} items cargados")
                onLoaded(favItems)
            } catch (e: Exception) {
                Log.e("FirestoreSyncManager", "loadFavoritesFromFirestore: Error - ${e.message}", e)
                onLoaded(emptyList())
            }
        }
    }

    // ✅ Escuchar cambios en tiempo real del carrito
    fun listenToCartChanges(onChanged: (List<CartItemData>) -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid

        Log.d("FirestoreSyncManager", "listenToCartChanges: Escuchando cambios en carrito para $uid")

        db.collection("users").document(uid)
            .collection("cart").document("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreSyncManager", "listenToCartChanges: Error - ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val items = snapshot.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val cartItems = items.mapNotNull { item ->
                        try {
                            CartItemData(
                                product_id = (item["product_id"] as? Number)?.toInt() ?: 0,
                                qty = (item["qty"] as? Number)?.toInt() ?: 0,
                                size_id = (item["size_id"] as? Number)?.toInt(),
                                color_id = (item["color_id"] as? Number)?.toInt(),
                                name = item["name"] as? String ?: "",
                                price = (item["price"] as? Number)?.toDouble() ?: 0.0,
                                imageUrl = item["imageUrl"] as? String
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    Log.d("FirestoreSyncManager", "listenToCartChanges: ${cartItems.size} items detectados")
                    onChanged(cartItems)
                }
            }
    }

    // ✅ Escuchar cambios en tiempo real de favoritos
    fun listenToFavoritesChanges(onChanged: (List<FavoriteItemData>) -> Unit) {
        val user = Firebase.auth.currentUser ?: return
        val uid = user.uid

        Log.d("FirestoreSyncManager", "listenToFavoritesChanges: Escuchando cambios en favoritos para $uid")

        db.collection("users").document(uid)
            .collection("favorites").document("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("FirestoreSyncManager", "listenToFavoritesChanges: Error - ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val items = snapshot.get("items") as? List<Map<String, Any>> ?: emptyList()
                    val favItems = items.mapNotNull { item ->
                        try {
                            FavoriteItemData(
                                product_id = (item["product_id"] as? Number)?.toInt() ?: 0,
                                name = item["name"] as? String ?: "",
                                price = item["price"] as? String ?: "",
                                imageUrl = item["imageUrl"] as? String
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    Log.d("FirestoreSyncManager", "listenToFavoritesChanges: ${favItems.size} items detectados")
                    onChanged(favItems)
                }
            }
    }
}
