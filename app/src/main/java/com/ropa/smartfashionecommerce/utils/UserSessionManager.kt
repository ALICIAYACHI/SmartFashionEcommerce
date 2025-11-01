package com.ropa.smartfashionecommerce.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object UserSessionManager {

    /**
     * Obtiene el UID del usuario actual de Firebase
     * Retorna un UID por defecto si no hay usuario logueado
     */
    fun getCurrentUserUID(): String {
        return Firebase.auth.currentUser?.uid ?: "guest_user"
    }

    /**
     * Obtiene SharedPreferences específicas para el usuario actual
     */
    fun getUserPreferences(context: Context): SharedPreferences {
        val uid = getCurrentUserUID()
        return context.getSharedPreferences("user_${uid}_prefs", Context.MODE_PRIVATE)
    }

    /**
     * Limpia todos los datos del usuario al cerrar sesión
     */
    fun clearUserData(context: Context) {
        val uid = getCurrentUserUID()

        // Limpiar preferencias del usuario
        val userPrefs = context.getSharedPreferences("user_${uid}_prefs", Context.MODE_PRIVATE)
        userPrefs.edit().clear().apply()

        // Limpiar favoritos del usuario
        val favPrefs = context.getSharedPreferences("favorites_$uid", Context.MODE_PRIVATE)
        favPrefs.edit().clear().apply()

        // Limpiar carrito del usuario
        val cartPrefs = context.getSharedPreferences("cart_$uid", Context.MODE_PRIVATE)
        cartPrefs.edit().clear().apply()
    }
}