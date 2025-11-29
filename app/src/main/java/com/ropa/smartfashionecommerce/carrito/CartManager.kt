package com.ropa.smartfashionecommerce.carrito

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ropa.smartfashionecommerce.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.ropa.smartfashionecommerce.utils.UserSessionManager
import com.ropa.smartfashionecommerce.network.ApiClient
import com.ropa.smartfashionecommerce.network.CartItemPayload
import com.ropa.smartfashionecommerce.network.CartStateData
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

object CartManager {

    private val gson = Gson()
    private val _cartItems: SnapshotStateList<CartItem> = mutableStateListOf()

    val cartItems: List<CartItem>
        get() = _cartItems

    private const val CART_KEY = "cart_items"
    private var appContext: Context? = null

    // Scope simple para llamadas de red en segundo plano
    private val ioScope = CoroutineScope(IO)

    // ✅ Inicializar con contexto
    fun initialize(context: Context) {
        appContext = context.applicationContext
        // Solo cargar desde almacenamiento local (comportamiento original)
        loadCart(context)
    }

    // ✅ Agregar producto
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
        syncCartToBackend()
    }

    // ✅ Eliminar producto
    fun removeItem(item: CartItem) {
        _cartItems.remove(item)
        saveCart(appContext)
        syncCartToBackend()
    }

    // ✅ Actualizar cantidad
    fun updateQuantity(item: CartItem, newQuantity: Int) {
        val index = _cartItems.indexOf(item)
        if (index != -1) {
            _cartItems[index] = _cartItems[index].copy(quantity = newQuantity)
            saveCart(appContext)
            syncCartToBackend()
        }
    }

    // ✅ Vaciar carrito (y almacenamiento)
    fun clear() {
        _cartItems.clear()
        saveCart(appContext)
        syncCartToBackend()
    }

    // ✅ Total del carrito
    fun getTotal(): Double {
        return _cartItems.sumOf { it.price * it.quantity }
    }

    // ✅ Guardar carrito del usuario actual
    fun saveCart(context: Context?) {
        if (context == null) return
        val uid = UserSessionManager.getCurrentUserUID()
        val prefs = context.getSharedPreferences("cart_$uid", Context.MODE_PRIVATE)
        val json = gson.toJson(_cartItems)
        prefs.edit().putString(CART_KEY, json).apply()
    }

    // ✅ Cargar carrito según usuario
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

    // ✅ Limpiar carrito temporalmente (sin guardar)
    fun clearCartMemory() {
        _cartItems.clear()
    }

    // ✅ Cuando el usuario cierra sesión
    fun onLogout() {
        clearCartMemory()
    }

    // ✅ Cuando un usuario inicia sesión
    fun onLogin(context: Context) {
        loadCart(context)
        // Al iniciar sesión, intentar subir el carrito actual al backend
        syncCartToBackend()
    }

    fun refreshFromBackend() {
        val context = appContext ?: return
        val email = Firebase.auth.currentUser?.email
        if (email.isNullOrEmpty()) return

        ioScope.launch {
            try {
                val api = ApiClient.apiService
                val resp = api.getCartState(email)
                if (!resp.isSuccessful) return@launch
                val data = resp.body()?.data ?: return@launch

                val backendItems = data.items
                val newItems = mutableListOf<CartItem>()

                // Catálogos globales de tallas y colores (como en el web)
                val sizesCatalog = try {
                    api.getSizes().body()?.data ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }
                val colorsCatalog = try {
                    api.getColors().body()?.data ?: emptyList()
                } catch (_: Exception) {
                    emptyList()
                }

                for (payload in backendItems) {
                    try {
                        val detailResp = api.getProductDetail(payload.product_id)
                        if (!detailResp.isSuccessful) continue
                        val body: com.ropa.smartfashionecommerce.model.ApiResponse<com.ropa.smartfashionecommerce.model.ProductDetailData>? = detailResp.body()
                        val detail = body?.data
                        val product = detail?.product

                        val basePrice = product?.precio ?: 0.0
                        val discountPrice = product?.precio_descuento
                        val price = discountPrice ?: basePrice

                        val images = detail?.images.orEmpty()
                        val preview = product?.image_preview
                        val imageUrl = when {
                            !preview.isNullOrBlank() -> preview
                            images.isNotEmpty() -> images.first()
                            else -> null
                        }

                        val sizeName = payload.size_id?.let { id ->
                            sizesCatalog.find { it.id == id }?.nombre
                        } ?: ""

                        val colorName = payload.color_id?.let { id ->
                            colorsCatalog.find { it.id == id }?.nombre
                        } ?: ""

                        val item = CartItem(
                            productId = payload.product_id,
                            sizeId = payload.size_id,
                            colorId = payload.color_id,
                            name = product?.nombre ?: "Producto ${payload.product_id}",
                            size = sizeName,
                            color = colorName,
                            quantity = payload.qty,
                            price = price,
                            imageRes = R.drawable.modelo_ropa,
                            imageUrl = imageUrl
                        )
                        newItems.add(item)
                    } catch (_: Exception) {
                    }
                }

                withContext(Main) {
                    _cartItems.clear()
                    _cartItems.addAll(newItems)
                    saveCart(context)
                }
            } catch (_: Exception) {
            }
        }
    }

    // Sincronizar carrito local completo con backend (/api/cart/)
    private fun syncCartToBackend() {
        val context = appContext ?: return
        val uid = UserSessionManager.getCurrentUserUID()
        val email = Firebase.auth.currentUser?.email
        if (uid.isNullOrEmpty() || email.isNullOrEmpty()) return

        val payloadItems = _cartItems.map {
            CartItemPayload(
                product_id = it.productId,
                qty = it.quantity,
                size_id = it.sizeId,
                color_id = it.colorId
            )
        }.filter { it.product_id > 0 && it.qty > 0 }

        ioScope.launch {
            try {
                val api = ApiClient.apiService
                api.setCartState(CartStateData(items = payloadItems), email = email)
            } catch (_: Exception) {
                // Ignorar errores de red; el carrito local sigue funcionando
            }
        }
    }
}