package com.ropa.smartfashionecommerce.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val BASE_URL = "http://10.147.1.123:8000/"

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
