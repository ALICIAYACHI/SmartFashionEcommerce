package com.ropa.smartfashionecommerce.home

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ropa.smartfashionecommerce.utils.UserSessionManager

data class FavoriteItem(
    val id: Int,
    val name: String,
    val price: String,
    val sizes: List<String> = listOf("S", "M", "L"),
    val imageRes: Int,
    var isFavorite: Boolean = true
)

object FavoritesManager {

    private const val KEY_FAVORITES = "favorite_items"

    private val _favoriteItems = mutableStateListOf<FavoriteItem>()
    val favoriteItems: SnapshotStateList<FavoriteItem>
        get() = _favoriteItems

    // ✅ Inicializa y carga favoritos del usuario actual
    fun initialize(context: Context) {
        loadFavorites(context)
    }

    // ✅ Agregar favorito y guardar
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
        val prefs = context.getSharedPreferences("favorites_$uid", Context.MODE_PRIVATE)
        val json = Gson().toJson(_favoriteItems)
        prefs.edit().putString(KEY_FAVORITES, json).apply()
    }

    // ✅ Cargar favoritos del usuario actual
    private fun loadFavorites(context: Context) {
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

    // ✅ Limpiar favoritos en memoria (al cambiar de usuario)
    fun clearFavorites() {
        _favoriteItems.clear()
    }
}