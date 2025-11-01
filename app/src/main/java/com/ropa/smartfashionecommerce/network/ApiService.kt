package com.ropa.smartfashionecommerce.network

import com.ropa.smartfashionecommerce.model.ApiResponse
import com.ropa.smartfashionecommerce.model.Producto
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    @GET("productos/")
    suspend fun getProductos(): Response<ApiResponse<List<Producto>>>
}
