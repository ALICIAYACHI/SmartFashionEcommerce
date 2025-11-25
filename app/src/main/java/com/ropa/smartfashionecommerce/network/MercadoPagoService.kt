package com.ropa.smartfashionecommerce.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 🔹 SERVICIO DE MERCADO PAGO
 * 
 * Para usar este servicio necesitas:
 * 1. Crear cuenta en https://www.mercadopago.com.pe
 * 2. Ir a https://www.mercadopago.com.pe/developers/panel
 * 3. Crear una aplicación
 * 4. Copiar tu ACCESS TOKEN de prueba (comienza con TEST-...)
 * 5. Reemplazar YOUR_ACCESS_TOKEN más abajo
 * 
 * IMPORTANTE: Para producción usa el ACCESS TOKEN de producción
 */

// ============== MODELOS DE REQUEST ==============

data class MercadoPagoPreferenceRequest(
    val items: List<MercadoPagoItem>,
    val payer: MercadoPagoPayer? = null,
    val back_urls: MercadoPagoBackUrls? = null,
    val auto_return: String = "approved",
    val payment_methods: MercadoPagoPaymentMethods? = null,
    val notification_url: String? = null,
    val external_reference: String? = null
)

data class MercadoPagoItem(
    val title: String,
    val quantity: Int,
    val unit_price: Double,
    val currency_id: String = "PEN"
)

data class MercadoPagoPayer(
    val name: String? = null,
    val surname: String? = null,
    val email: String? = null,
    val phone: MercadoPagoPhone? = null,
    val identification: MercadoPagoIdentification? = null,
    val address: MercadoPagoAddress? = null
)

data class MercadoPagoPhone(
    val area_code: String? = null,
    val number: String? = null
)

data class MercadoPagoIdentification(
    val type: String? = null, // "DNI", "RUC", etc.
    val number: String? = null
)

data class MercadoPagoAddress(
    val street_name: String? = null,
    val street_number: String? = null,
    val zip_code: String? = null
)

data class MercadoPagoBackUrls(
    val success: String,
    val failure: String,
    val pending: String
)

data class MercadoPagoPaymentMethods(
    val excluded_payment_types: List<MercadoPagoPaymentType>? = null,
    val excluded_payment_methods: List<MercadoPagoPaymentMethod>? = null,
    val installments: Int? = null
)

data class MercadoPagoPaymentType(
    val id: String
)

data class MercadoPagoPaymentMethod(
    val id: String
)

// ============== MODELOS DE RESPONSE ==============

data class MercadoPagoPreferenceResponse(
    val id: String,
    val init_point: String, // URL para pago en web
    val sandbox_init_point: String? = null // URL para testing
)

data class MercadoPagoPaymentResponse(
    val id: Long,
    val status: String, // "approved", "pending", "rejected", "cancelled"
    val status_detail: String,
    val transaction_amount: Double,
    val description: String?,
    val payment_method_id: String?,
    val payment_type_id: String?,
    val date_created: String?,
    val date_approved: String?,
    val external_reference: String?
)

// ============== INTERFAZ API ==============

interface MercadoPagoApiService {

    /**
     * Crea una preferencia de pago
     * Devuelve un link donde el usuario puede pagar
     */
    @POST("checkout/preferences")
    suspend fun createPreference(
        @Header("Authorization") authorization: String,
        @Body request: MercadoPagoPreferenceRequest
    ): Response<MercadoPagoPreferenceResponse>

    /**
     * Obtiene el estado de un pago
     */
    @GET("v1/payments/{id}")
    suspend fun getPaymentStatus(
        @Header("Authorization") authorization: String,
        @Path("id") paymentId: Long
    ): Response<MercadoPagoPaymentResponse>
}

// ============== SINGLETON RETROFIT ==============

object MercadoPagoClient {
    
    /**
     * 🔑 REEMPLAZA ESTO CON TU ACCESS TOKEN DE MERCADO PAGO
     * 
     * Para obtenerlo:
     * 1. Regístrate en https://www.mercadopago.com.pe
     * 2. Ve a https://www.mercadopago.com.pe/developers/panel/app
     * 3. Crea una aplicación
     * 4. Copia tu "Access Token de prueba" (comienza con TEST-...)
     * 
     * Para PRODUCCIÓN usa el Access Token de producción (comienza con APP_USR-...)
     */
    private const val ACCESS_TOKEN = "APP_USR-7905318521014331-111622-2c00e350cd37aec892df2d904808fa76-2996192170"
    
    private const val BASE_URL = "https://api.mercadopago.com/"
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val api: MercadoPagoApiService by lazy {
        retrofit.create(MercadoPagoApiService::class.java)
    }
    
    /**
     * Retorna el header de autorización formateado
     */
    fun getAuthHeader(): String = "Bearer $ACCESS_TOKEN"
    
    /**
     * Verifica si el token está configurado
     */
    fun isTokenConfigured(): Boolean {
        return ACCESS_TOKEN != "TEST-YOUR_ACCESS_TOKEN_HERE"
    }
}
