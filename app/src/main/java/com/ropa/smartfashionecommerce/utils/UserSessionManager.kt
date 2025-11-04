package com.ropa.smartfashionecommerce.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

object UserSessionManager {

    private const val PREF_BACKUP_CART = "backup_cart"
    private const val KEY_LAST_USER_UID = "last_user_uid"

    /** 🔹 Obtiene el UID del usuario actual o "guest_user" si no hay sesión activa */
    fun getCurrentUserUID(): String {
        return Firebase.auth.currentUser?.uid ?: "guest_user"
    }

    /** 🔹 SharedPreferences del usuario actual */
    fun getUserPreferences(context: Context): SharedPreferences {
        val uid = getCurrentUserUID()
        return context.getSharedPreferences("user_${uid}_prefs", Context.MODE_PRIVATE)
    }

    /** 🔹 SharedPreferences del carrito del usuario actual */
    fun getCartPreferences(context: Context): SharedPreferences {
        val uid = getCurrentUserUID()
        return context.getSharedPreferences("cart_$uid", Context.MODE_PRIVATE)
    }

    /** 🔹 Respaldar carrito antes de cerrar sesión */
    fun backupCartBeforeLogout(context: Context) {
        val uid = getCurrentUserUID()
        if (uid == "guest_user") return

        val cartPrefs = context.getSharedPreferences("cart_$uid", Context.MODE_PRIVATE)
        val backupPrefs = context.getSharedPreferences(PREF_BACKUP_CART, Context.MODE_PRIVATE)
        val cartData = cartPrefs.all

        val backupEditor = backupPrefs.edit().clear()
        for ((key, value) in cartData) {
            when (value) {
                is String -> backupEditor.putString(key, value)
                is Boolean -> backupEditor.putBoolean(key, value)
                is Int -> backupEditor.putInt(key, value)
                is Float -> backupEditor.putFloat(key, value)
                is Long -> backupEditor.putLong(key, value)
            }
        }

        backupEditor.putString(KEY_LAST_USER_UID, uid)
        backupEditor.apply()
    }

    /** 🔹 Restaura carrito si el mismo usuario vuelve a iniciar */
    fun restoreCartAfterLogin(context: Context) {
        val uid = getCurrentUserUID()
        val backupPrefs = context.getSharedPreferences(PREF_BACKUP_CART, Context.MODE_PRIVATE)
        val lastUid = backupPrefs.getString(KEY_LAST_USER_UID, null)

        if (uid == lastUid && lastUid != null) {
            val cartPrefs = context.getSharedPreferences("cart_$uid", Context.MODE_PRIVATE)
            val editor = cartPrefs.edit().clear()

            for ((key, value) in backupPrefs.all) {
                if (key == KEY_LAST_USER_UID) continue
                when (value) {
                    is String -> editor.putString(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Int -> editor.putInt(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Long -> editor.putLong(key, value)
                }
            }
            editor.apply()
        }

        backupPrefs.edit().clear().apply()
    }

    /** 🔹 Limpia datos personales (sin tocar carrito si ya se respaldó) */
    fun clearUserData(context: Context) {
        val uid = getCurrentUserUID()
        val userPrefs = context.getSharedPreferences("user_${uid}_prefs", Context.MODE_PRIVATE)
        userPrefs.edit().clear().apply()
    }

    /** 🔹 Cerrar sesión de forma segura */
    fun logout(context: Context) {
        // 1️⃣ Respaldar carrito antes de cerrar sesión
        backupCartBeforeLogout(context)

        // 2️⃣ Limpiar datos del usuario
        clearUserData(context)

        // 3️⃣ Cerrar sesión de Firebase
        Firebase.auth.signOut()
    }
}