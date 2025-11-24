package com.ropa.smartfashionecommerce.carrito

import android.content.Context

object StockManager {
    private const val PREFS_NAME = "StockPrefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Obtiene el stock actual de un producto. Si no existe aún en preferencias,
     * se inicializa con [defaultStock] y se devuelve ese valor.
     */
    fun getStock(context: Context, productKey: String, defaultStock: Int): Int {
        if (productKey.isBlank()) return defaultStock
        val p = prefs(context)
        val current = p.getInt(productKey, -1)
        return if (current >= 0) {
            current
        } else {
            p.edit().putInt(productKey, defaultStock).apply()
            defaultStock
        }
    }

    /**
     * Reduce el stock de un producto en [quantity]. Nunca baja de 0.
     */
    fun reduceStock(context: Context, productKey: String, quantity: Int) {
        if (productKey.isBlank() || quantity <= 0) return
        val p = prefs(context)
        val current = p.getInt(productKey, 0)
        val newStock = (current - quantity).coerceAtLeast(0)
        p.edit().putInt(productKey, newStock).apply()
    }

    /**
     * Aumenta el stock de un producto en [quantity]. Se usa al eliminar del carrito.
     */
    fun increaseStock(context: Context, productKey: String, quantity: Int) {
        if (productKey.isBlank() || quantity <= 0) return
        val p = prefs(context)
        val current = p.getInt(productKey, 0)
        val newStock = current + quantity
        p.edit().putInt(productKey, newStock).apply()
    }
}
