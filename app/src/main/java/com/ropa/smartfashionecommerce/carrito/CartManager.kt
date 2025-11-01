package com.ropa.smartfashionecommerce.carrito

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ropa.smartfashionecommerce.utils.UserSessionManager

object CartManager {

    private val gson = Gson()
    private val _cartItems: SnapshotStateList<CartItem> = mutableStateListOf()
    val cartItems: List<CartItem>
        get() = _cartItems

    private const val CART_KEY = "cart_items"
    private var appContext: Context? = null

    // ✅ Inicializa el carrito cargando los datos del usuario actual
    fun initialize(context: Context) {
        appContext = context.applicationContext
        loadCart(context)
    }

    // Agregar un producto
    fun addItem(item: CartItem) {
        val existingItem = _cartItems.find {
            it.name == item.name && it.size == item.size && it.color == item.color
        }

        if (existingItem != null) {
            existingItem.quantity += item.quantity
        } else {
            _cartItems.add(item)
        }

        saveCart(appContext)
    }

    // Quitar producto
    fun removeItem(item: CartItem) {
        _cartItems.remove(item)
        saveCart(appContext)
    }

    // Actualizar cantidad
    fun updateQuantity(item: CartItem, newQuantity: Int) {
        val index = _cartItems.indexOf(item)
        if (index != -1) {
            _cartItems[index] = _cartItems[index].copy(quantity = newQuantity)
            saveCart(appContext)
        }
    }

    // Vaciar carrito
    fun clear() {
        _cartItems.clear()
        saveCart(appContext)
    }

    // Total del carrito
    fun getTotal(): Double {
        return _cartItems.sumOf { it.price * it.quantity }
    }

    // ✅ Guardar carrito con UID del usuario
    fun saveCart(context: Context?) {
        if (context == null) return
        val uid = UserSessionManager.getCurrentUserUID()
        val prefs = context.getSharedPreferences("cart_$uid", Context.MODE_PRIVATE)
        val json = gson.toJson(_cartItems)
        prefs.edit().putString(CART_KEY, json).apply()
    }

    // ✅ Cargar carrito del usuario actual
    fun loadCart(context: Context) {
        val uid = UserSessionManager.getCurrentUserUID()
        val prefs = context.getSharedPreferences("cart_$uid", Context.MODE_PRIVATE)
        val json = prefs.getString(CART_KEY, null)

        _cartItems.clear()

        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            val items: List<CartItem> = gson.fromJson(json, type)
            _cartItems.addAll(items)
        }
    }

    // ✅ Limpiar carrito en memoria (al cambiar de usuario)
    fun clearCart() {
        _cartItems.clear()
    }
}