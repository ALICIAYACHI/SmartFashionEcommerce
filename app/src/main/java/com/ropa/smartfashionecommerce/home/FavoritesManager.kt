package com.ropa.smartfashionecommerce.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ropa.smartfashionecommerce.utils.UserSessionManager

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

    // ✅ Inicializa y carga favoritos del usuario actual
    fun initialize(context: Context) {
        loadFavorites(context)
    }

    // ✅ Agregar favorito y guardar (evita duplicados por ID)
    fun addFavorite(context: Context, item: FavoriteItem) {
        if (_favoriteItems.none { it.id == item.id }) {
            _favoriteItems.add(item)
            saveFavorites(context)
        }
    }

    // ✅ Eliminar favorito y guardar
    fun removeFavorite(context: Context, item: FavoriteItem) {
        _favoriteItems.removeIf { it.id == item.id }
        saveFavorites(context)
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
    }
}