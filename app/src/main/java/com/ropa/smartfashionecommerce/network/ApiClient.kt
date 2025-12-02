package com.ropa.smartfashionecommerce.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "https://smarthfashion.shop/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 🔹 Aquí creamos la instancia del servicio
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
