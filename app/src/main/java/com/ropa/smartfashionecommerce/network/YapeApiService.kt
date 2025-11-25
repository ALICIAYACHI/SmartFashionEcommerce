package com.ropa.smartfashionecommerce.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Modelos base para integrar Yape vía backend propio.
// Ajusta estos campos cuando tengas la especificación real de tu API.
data class YapeCreatePaymentRequest(
    val amount: Double,
    val currency: String = "PEN",
    val customerPhone: String,
    val description: String,
    val orderId: String
)

data class YapeCreatePaymentResponse(
    val paymentId: String,
    val paymentUrl: String?
)

data class YapePaymentStatusResponse(
    val paymentId: String,
    val status: String,      // e.g. "PENDING", "APPROVED", "REJECTED"
    val detail: String? = null
)

interface YapeApiService {

    @POST("/yape/create-payment")
    suspend fun createPayment(
        @Body request: YapeCreatePaymentRequest
    ): Response<YapeCreatePaymentResponse>

    @GET("/yape/payment-status/{paymentId}")
    suspend fun getPaymentStatus(
        @Path("paymentId") paymentId: String
    ): Response<YapePaymentStatusResponse>
}
