package com.ropa.smartfashionecommerce.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ropa.smartfashionecommerce.model.Producto
import com.ropa.smartfashionecommerce.network.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val apiService = ApiClient.apiService

    private val _productos = MutableStateFlow<List<Producto>>(emptyList())
    val productos: StateFlow<List<Producto>> = _productos

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun cargarProductos() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = apiService.getProductos()
                if (response.isSuccessful && response.body()?.status == "ok") {
                    _productos.value = response.body()?.data ?: emptyList()
                } else {
                    _error.value = "Error al obtener productos"
                }
            } catch (e: Exception) {
                _error.value = "No se pudo conectar con el servidor"
            } finally {
                _loading.value = false
            }
        }
    }
}
