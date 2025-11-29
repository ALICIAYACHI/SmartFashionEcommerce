package com.ropa.smartfashionecommerce.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.network.FavoriteItemPayload
import com.ropa.smartfashionecommerce.network.FavoritesStateData
import com.ropa.smartfashionecommerce.utils.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 💖 Data class para representar un artículo favorito */
data class FavoriteItem(
    val id: Int,
    val name: String,
    val price: String,
    val sizes: List<String> = listOf("S", "M", "L"),
    val imageRes: Int,
    val imageUrl: String? = null,
    var isFavorite: Boolean = true
)

/** ⭐️ Gestor de Favoritos: Almacena y recupera datos usando SharedPreferences por usuario. */
object FavoritesManager {

    private const val KEY_FAVORITES = "favorite_items"

    // Lista mutable para la reactividad en Jetpack Compose
    private val _favoriteItems = mutableStateListOf<FavoriteItem>()
    val favoriteItems: SnapshotStateList<FavoriteItem>
        get() = _favoriteItems

    private var appContext: Context? = null
    private val ioScope = CoroutineScope(Dispatchers.IO)

    // ✅ Inicializa y carga favoritos del usuario actual
    fun initialize(context: Context) {
        appContext = context.applicationContext
        loadFavorites(context)
        refreshFromBackend()
    }

    // ✅ Agregar favorito y guardar (evita duplicados por ID)
    fun addFavorite(context: Context, item: FavoriteItem) {
        if (_favoriteItems.none { it.id == item.id }) {
            _favoriteItems.add(item)
            saveFavorites(context)
            syncToBackend()
        }
    }

    // ✅ Eliminar favorito y guardar
    fun removeFavorite(context: Context, item: FavoriteItem) {
        _favoriteItems.removeIf { it.id == item.id }
        saveFavorites(context)
        syncToBackend()
    }

    // ✅ Guardar favoritos con UID del usuario
    private fun saveFavorites(context: Context) {
        val uid = UserSessionManager.getCurrentUserUID()
        // SharedPreferences específicas para este usuario
        val prefs = context.getSharedPreferences("favorites_$uid", Context.MODE_PRIVATE)
        val json = Gson().toJson(_favoriteItems)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    // ✅ Cargar favoritos del usuario actual
    fun loadFavorites(context: Context) {
        val uid = UserSessionManager.getCurrentUserUID()
        val prefs = context.getSharedPreferences("favorites_$uid", Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_FAVORITES, null)

        _favoriteItems.clear()

        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<FavoriteItem>>() {}.type
            val list: List<FavoriteItem> = Gson().fromJson(json, type)
            _favoriteItems.addAll(list)
        }
    }

    // ✅ Limpiar favoritos en memoria (útil al cerrar sesión o cambiar de usuario)
    fun clearFavorites() {
        _favoriteItems.clear()
        // No sincronizamos una lista vacía al backend aquí para no borrar favoritos guardados en servidor.
        // El backend seguirá teniendo los favoritos del usuario.
    }

    // 🔄 Subir favoritos actuales al backend compartido (/api/favorites/)
    private fun syncToBackend() {
        val context = appContext ?: return
        val email = Firebase.auth.currentUser?.email
        if (email.isNullOrEmpty()) return

        val payloadItems = _favoriteItems.map { FavoriteItemPayload(product_id = it.id) }

        ioScope.launch {
            try {
                val api = ApiClient.apiService
                api.setFavoritesState(FavoritesStateData(items = payloadItems), email)
            } catch (_: Exception) {
                // ignorar errores de red
            }
        }
    }

    // 🔄 Traer favoritos desde el backend y reemplazar lista local
    fun refreshFromBackend() {
        val context = appContext ?: return
        val email = Firebase.auth.currentUser?.email
        if (email.isNullOrEmpty()) return

        ioScope.launch {
            try {
                val api = ApiClient.apiService
                val resp = api.getFavoritesState(email)
                if (!resp.isSuccessful) return@launch
                val data = resp.body()?.data ?: return@launch

                val backendItems = data.items
                val newList = mutableListOf<FavoriteItem>()

                for (fav in backendItems) {
                    try {
                        val detailResp = api.getProductDetail(fav.product_id)
                        if (!detailResp.isSuccessful) continue
                        val body = detailResp.body()?.data
                        val product = body?.product
                        val price = "S/ %.2f".format(product?.precio ?: 0.0)
                        val imageUrl = product?.image_preview ?: body?.images?.firstOrNull()

                        val item = FavoriteItem(
                            id = fav.product_id,
                            name = product?.nombre ?: "Producto ${fav.product_id}",
                            price = price,
                            sizes = listOf("S", "M", "L", "XL"),
                            imageRes = 0,
                            imageUrl = imageUrl,
                            isFavorite = true
                        )
                        newList.add(item)
                    } catch (_: Exception) {
                    }
                }

                // Reemplazar lista local y guardar
                _favoriteItems.clear()
                _favoriteItems.addAll(newList)
                saveFavorites(context)
            } catch (_: Exception) {
            }
        }
    }
}