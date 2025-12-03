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
import android.util.Log
import com.ropa.smartfashionecommerce.sync.FirestoreSyncManager

object CartManager {

    private val gson = Gson()
    private val _cartItems: SnapshotStateList<CartItem> = mutableStateListOf()

    val cartItems: List<CartItem>
        get() = _cartItems

    private const val CART_KEY = "cart_items"
    private var appContext: Context? = null

    // Scope simple para llamadas de red en segundo plano
    private val ioScope = CoroutineScope(IO)
    
    // Job para monitoreo en tiempo real
    private var syncJob: Job? = null
    private val SYNC_INTERVAL_MS = 5000L // 5 segundos

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
        // Guardar en Firestore
        FirestoreSyncManager.saveCartToFirestore(_cartItems)
    }

    // ✅ Eliminar producto
    fun removeItem(item: CartItem) {
        _cartItems.remove(item)
        saveCart(appContext)
        syncCartToBackend()
        // Guardar en Firestore
        FirestoreSyncManager.saveCartToFirestore(_cartItems)
    }

    // ✅ Actualizar cantidad
    fun updateQuantity(item: CartItem, newQuantity: Int) {
        val index = _cartItems.indexOf(item)
        if (index != -1) {
            _cartItems[index] = _cartItems[index].copy(quantity = newQuantity)
            saveCart(appContext)
            syncCartToBackend()
            // Guardar en Firestore
            FirestoreSyncManager.saveCartToFirestore(_cartItems)
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
        
        // Cargar también desde Firestore
        FirestoreSyncManager.loadCartFromFirestore { firestoreItems ->
            if (firestoreItems.isNotEmpty()) {
                Log.d("CartManager", "loadCart: Cargando ${firestoreItems.size} items desde Firestore")
                ioScope.launch {
                    try {
                        // Obtener catálogos de tallas y colores
                        val sizesCatalog = try {
                            ApiClient.apiService.getSizes().body()?.data ?: emptyList()
                        } catch (_: Exception) {
                            emptyList()
                        }
                        val colorsCatalog = try {
                            ApiClient.apiService.getColors().body()?.data ?: emptyList()
                        } catch (_: Exception) {
                            emptyList()
                        }

                        val cartItems = firestoreItems.map { item ->
                            val sizeName = item.size_id?.let { id ->
                                sizesCatalog.find { it.id == id }?.nombre
                            } ?: ""

                            val colorName = item.color_id?.let { id ->
                                colorsCatalog.find { it.id == id }?.nombre
                            } ?: ""

                            CartItem(
                                productId = item.product_id,
                                sizeId = item.size_id,
                                colorId = item.color_id,
                                name = item.name,
                                size = sizeName,
                                color = colorName,
                                quantity = item.qty,
                                price = item.price,
                                imageRes = R.drawable.modelo_ropa,
                                imageUrl = item.imageUrl
                            )
                        }
                        withContext(Main) {
                            _cartItems.clear()
                            _cartItems.addAll(cartItems)
                            saveCart(context)
                        }
                    } catch (e: Exception) {
                        Log.e("CartManager", "Error loading cart from Firestore: ${e.message}", e)
                    }
                }
            }
        }
        
        // Escuchar cambios en tiempo real
        FirestoreSyncManager.listenToCartChanges { firestoreItems ->
            if (firestoreItems.isNotEmpty()) {
                ioScope.launch {
                    try {
                        // Obtener catálogos de tallas y colores
                        val sizesCatalog = try {
                            ApiClient.apiService.getSizes().body()?.data ?: emptyList()
                        } catch (_: Exception) {
                            emptyList()
                        }
                        val colorsCatalog = try {
                            ApiClient.apiService.getColors().body()?.data ?: emptyList()
                        } catch (_: Exception) {
                            emptyList()
                        }

                        val cartItems = firestoreItems.map { item ->
                            val sizeName = item.size_id?.let { id ->
                                sizesCatalog.find { it.id == id }?.nombre
                            } ?: ""

                            val colorName = item.color_id?.let { id ->
                                colorsCatalog.find { it.id == id }?.nombre
                            } ?: ""

                            CartItem(
                                productId = item.product_id,
                                sizeId = item.size_id,
                                colorId = item.color_id,
                                name = item.name,
                                size = sizeName,
                                color = colorName,
                                quantity = item.qty,
                                price = item.price,
                                imageRes = R.drawable.modelo_ropa,
                                imageUrl = item.imageUrl
                            )
                        }
                        withContext(Main) {
                            _cartItems.clear()
                            _cartItems.addAll(cartItems)
                            saveCart(context)
                            Log.d("CartManager", "listenToCartChanges: Carrito actualizado desde Firestore")
                        }
                    } catch (e: Exception) {
                        Log.e("CartManager", "Error listening to cart changes: ${e.message}", e)
                    }
                }
            }
        }
    }

    // ✅ Limpiar carrito temporalmente (sin guardar)
    fun clearCartMemory() {
        _cartItems.clear()
    }

    // ✅ Cuando el usuario cierra sesión
    fun onLogout() {
        stopRealtimeSync()
        clearCartMemory()
    }

    // ✅ Cuando un usuario inicia sesión
    fun onLogin(context: Context) {
        loadCart(context)
        // Al iniciar sesión, intentar subir el carrito actual al backend
        syncCartToBackend()
        // Iniciar monitoreo en tiempo real
        startRealtimeSync()
    }

    fun refreshFromBackend() {
        val context = appContext ?: return
        val email = Firebase.auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            Log.d("CartManager", "refreshFromBackend: Email vacío, no se sincroniza")
            return
        }

        Log.d("CartManager", "refreshFromBackend: Sincronizando carrito para $email")
        ioScope.launch {
            try {
                val api = ApiClient.apiService
                val resp = api.getCartState(email)
                Log.d("CartManager", "refreshFromBackend: Response code ${resp.code()}")
                
                if (!resp.isSuccessful) {
                    Log.e("CartManager", "refreshFromBackend: Error ${resp.code()} - ${resp.errorBody()?.string()}")
                    return@launch
                }
                
                val data = resp.body()?.data
                Log.d("CartManager", "refreshFromBackend: Data recibido: $data")
                
                if (data == null) {
                    Log.w("CartManager", "refreshFromBackend: Data es null")
                    return@launch
                }

                val backendItems = data.items
                Log.d("CartManager", "refreshFromBackend: ${backendItems.size} items en backend")
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
                    Log.d("CartManager", "refreshFromBackend: Carrito actualizado con ${newItems.size} items")
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
        if (uid.isNullOrEmpty() || email.isNullOrEmpty()) {
            Log.d("CartManager", "syncCartToBackend: UID o Email vacíos")
            return
        }

        val payloadItems = _cartItems.map {
            CartItemPayload(
                product_id = it.productId,
                qty = it.quantity,
                size_id = it.sizeId,
                color_id = it.colorId
            )
        }.filter { it.product_id > 0 && it.qty > 0 }

        Log.d("CartManager", "syncCartToBackend: Enviando ${payloadItems.size} items para $email")
        Log.d("CartManager", "syncCartToBackend: Items: $payloadItems")

        ioScope.launch {
            try {
                val api = ApiClient.apiService
                val resp = api.setCartState(CartStateData(items = payloadItems), email = email)
                Log.d("CartManager", "syncCartToBackend: Response code ${resp.code()}")
                
                if (!resp.isSuccessful) {
                    Log.e("CartManager", "syncCartToBackend: Error ${resp.code()} - ${resp.errorBody()?.string()}")
                } else {
                    Log.d("CartManager", "syncCartToBackend: Éxito - ${resp.body()}")
                }
            } catch (e: Exception) {
                Log.e("CartManager", "syncCartToBackend: Exception - ${e.message}", e)
            }
        }
    }

    // ✅ Iniciar monitoreo en tiempo real (polling cada 5 segundos)
    fun startRealtimeSync() {
        if (syncJob?.isActive == true) return // Ya está activo
        
        syncJob = ioScope.launch {
            while (isActive) {
                try {
                    delay(SYNC_INTERVAL_MS)
                    refreshFromBackend()
                } catch (_: Exception) {
                    // Ignorar errores, continuar intentando
                }
            }
        }
    }

    // ✅ Detener monitoreo en tiempo real
    fun stopRealtimeSync() {
        syncJob?.cancel()
        syncJob = null
    }
}
