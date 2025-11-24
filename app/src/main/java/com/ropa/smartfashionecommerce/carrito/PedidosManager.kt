package com.ropa.smartfashionecommerce.carrito

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class Pedido(
    val codigo: String,
    val fecha: String,
    val total: Double,
    val estado: String = "Procesando", // Procesando, En tránsito, Entregado
    val productos: List<String>,
    val direccionTexto: String,
    val latitud: Double? = null,
    val longitud: Double? = null
)

object PedidosManager {

    private const val KEY_PEDIDOS = "pedidos_list"

    val pedidos = mutableStateListOf<Pedido>()
    private val gson = Gson()

    // 🔹 Devuelve el nombre de archivo único para cada usuario
    private fun getPrefsName(): String {
        val user = Firebase.auth.currentUser
        val email = user?.email ?: "invitado"
        return "pedidos_prefs_$email"
    }

    // Cargar pedidos desde SharedPreferences (por usuario)
    fun cargarPedidos(context: Context) {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PEDIDOS, null)

        if (json != null) {
            val type = object : TypeToken<List<Pedido>>() {}.type
            val listaPedidos: List<Pedido> = gson.fromJson(json, type)
            pedidos.clear()
            pedidos.addAll(listaPedidos)
        } else {
            pedidos.clear() // Evita que queden pedidos del usuario anterior
        }
    }

    // Guardar pedidos en SharedPreferences (por usuario)
    private fun guardarPedidos(context: Context) {
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        val json = gson.toJson(pedidos)
        prefs.edit().putString(KEY_PEDIDOS, json).apply()
    }

    // Agregar un nuevo pedido
    fun agregarPedido(
        context: Context,
        total: Double,
        productos: List<String>,
        direccionTexto: String,
        latitud: Double? = null,
        longitud: Double? = null
    ) {
        // Asegurar que la lista en memoria esté sincronizada con lo que hay guardado
        // para no perder pedidos anteriores al agregar uno nuevo.
        cargarPedidos(context)

        val contador = pedidos.size + 1
        val codigo = "#ORD-${String.format("%03d", contador)}"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = dateFormat.format(Date())

        val nuevoPedido = Pedido(
            codigo = codigo,
            fecha = fecha,
            total = total,
            estado = "Procesando",
            productos = productos,
            direccionTexto = direccionTexto,
            latitud = latitud,
            longitud = longitud
        )

        pedidos.add(0, nuevoPedido) // Agregar al inicio
        guardarPedidos(context)
    }

    // Limpiar pedidos del usuario actual (por ejemplo, al cerrar sesión)
    fun limpiarPedidos(context: Context) {
        pedidos.clear()
        val prefs = context.getSharedPreferences(getPrefsName(), Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    // Actualizar estado de un pedido (opcional)
    fun actualizarEstado(context: Context, codigo: String, nuevoEstado: String) {
        val index = pedidos.indexOfFirst { it.codigo == codigo }
        if (index != -1) {
            pedidos[index] = pedidos[index].copy(estado = nuevoEstado)
            guardarPedidos(context)
        }
    }
}
