package com.ropa.smartfashionecommerce.carrito

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

data class Pedido(
    val codigo: String,
    val fecha: String,
    val total: Double,
    val estado: String = "Procesando", // Procesando, En tránsito, Entregado
    val productos: List<String>
)

object PedidosManager {
    private const val PREFS_NAME = "pedidos_prefs"
    private const val KEY_PEDIDOS = "pedidos_list"

    val pedidos = mutableStateListOf<Pedido>()
    private val gson = Gson()

    // Cargar pedidos desde SharedPreferences
    fun cargarPedidos(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PEDIDOS, null)

        if (json != null) {
            val type = object : TypeToken<List<Pedido>>() {}.type
            val listaPedidos: List<Pedido> = gson.fromJson(json, type)
            pedidos.clear()
            pedidos.addAll(listaPedidos)
        }
    }

    // Guardar pedidos en SharedPreferences
    private fun guardarPedidos(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(pedidos)
        prefs.edit().putString(KEY_PEDIDOS, json).apply()
    }

    // Agregar un nuevo pedido
    fun agregarPedido(context: Context, total: Double, productos: List<String>) {
        val contador = pedidos.size + 1
        val codigo = "#ORD-${String.format("%03d", contador)}"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fecha = dateFormat.format(Date())

        val nuevoPedido = Pedido(
            codigo = codigo,
            fecha = fecha,
            total = total,
            estado = "Procesando",
            productos = productos
        )

        pedidos.add(0, nuevoPedido) // Agregar al inicio de la lista
        guardarPedidos(context)
    }

    // Limpiar todos los pedidos (opcional, para pruebas)
    fun limpiarPedidos(context: Context) {
        pedidos.clear()
        guardarPedidos(context)
    }

    // Actualizar estado de un pedido (opcional, para simular cambios)
    fun actualizarEstado(context: Context, codigo: String, nuevoEstado: String) {
        val index = pedidos.indexOfFirst { it.codigo == codigo }
        if (index != -1) {
            pedidos[index] = pedidos[index].copy(estado = nuevoEstado)
            guardarPedidos(context)
        }
    }
}