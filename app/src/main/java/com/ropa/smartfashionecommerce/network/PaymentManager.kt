package com.ropa.smartfashionecommerce.network

import android.content.Context
import android.util.Log
import com.ropa.smartfashionecommerce.carrito.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 🔹 MANAGER PARA GESTIONAR PAGOS
 * 
 * Este manager facilita la creación de pagos con Mercado Pago
 * y maneja los errores de forma centralizada
 */
object PaymentManager {
    
    private const val TAG = "PaymentManager"
    
    /**
     * Crea una preferencia de pago en Mercado Pago
     * 
     * @param items Lista de productos del carrito
     * @param payerInfo Información del comprador (opcional)
     * @param externalReference ID de tu pedido (opcional pero recomendado)
     * @return URL donde el usuario puede pagar, o null si hay error
     */
    suspend fun createMercadoPagoPayment(
        items: List<CartItem>,
        payerInfo: PayerInfo? = null,
        externalReference: String? = null
    ): PaymentResult = withContext(Dispatchers.IO) {
        try {
            // Verificar que el token esté configurado
            if (!MercadoPagoClient.isTokenConfigured()) {
                Log.e(TAG, "❌ ACCESS TOKEN no configurado en MercadoPagoClient")
                return@withContext PaymentResult.Error(
                    "Mercado Pago no está configurado. Por favor configura tu ACCESS TOKEN."
                )
            }
            
            // Convertir items del carrito a formato Mercado Pago
            val mercadoPagoItems = items.map { cartItem ->
                MercadoPagoItem(
                    title = cartItem.name,
                    quantity = cartItem.quantity,
                    unit_price = cartItem.price,
                    currency_id = "PEN"
                )
            }
            
            // Construir información del comprador si está disponible
            val payer = payerInfo?.let {
                MercadoPagoPayer(
                    name = it.firstName,
                    surname = it.lastName,
                    email = it.email,
                    phone = it.phone?.let { phone ->
                        MercadoPagoPhone(
                            area_code = "51", // Código de Perú
                            number = phone
                        )
                    },
                    identification = it.documentNumber?.let { doc ->
                        MercadoPagoIdentification(
                            type = it.documentType ?: "DNI",
                            number = doc
                        )
                    }
                )
            }
            
            // URLs de retorno (personalízalas según tu app)
            val backUrls = MercadoPagoBackUrls(
                success = "smartfashion://payment/success",
                failure = "smartfashion://payment/failure",
                pending = "smartfashion://payment/pending"
            )
            
            // Crear request
            val request = MercadoPagoPreferenceRequest(
                items = mercadoPagoItems,
                payer = payer,
                back_urls = backUrls,
                auto_return = "approved",
                external_reference = externalReference
            )
            
            // Llamar a la API
            Log.d(TAG, "🔄 Creando preferencia de pago...")
            val response = MercadoPagoClient.api.createPreference(
                authorization = MercadoPagoClient.getAuthHeader(),
                request = request
            )
            
            if (response.isSuccessful && response.body() != null) {
                val preference = response.body()!!
                Log.d(TAG, "✅ Preferencia creada: ${preference.id}")
                
                // En testing usa sandbox_init_point, en producción usa init_point
                val paymentUrl = preference.sandbox_init_point ?: preference.init_point
                
                return@withContext PaymentResult.Success(
                    preferenceId = preference.id,
                    paymentUrl = paymentUrl
                )
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "❌ Error al crear preferencia: $errorBody")
                return@withContext PaymentResult.Error(
                    "Error al procesar el pago: ${response.code()}"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception al crear pago", e)
            return@withContext PaymentResult.Error(
                "Error de conexión: ${e.message}"
            )
        }
    }
    
    /**
     * Verifica el estado de un pago
     */
    suspend fun checkPaymentStatus(paymentId: Long): PaymentStatusResult = withContext(Dispatchers.IO) {
        try {
            if (!MercadoPagoClient.isTokenConfigured()) {
                return@withContext PaymentStatusResult.Error("Mercado Pago no configurado")
            }
            
            Log.d(TAG, "🔄 Verificando estado del pago $paymentId...")
            val response = MercadoPagoClient.api.getPaymentStatus(
                authorization = MercadoPagoClient.getAuthHeader(),
                paymentId = paymentId
            )
            
            if (response.isSuccessful && response.body() != null) {
                val payment = response.body()!!
                Log.d(TAG, "✅ Estado del pago: ${payment.status}")
                
                return@withContext PaymentStatusResult.Success(
                    status = payment.status,
                    statusDetail = payment.status_detail,
                    amount = payment.transaction_amount,
                    paymentMethodId = payment.payment_method_id
                )
            } else {
                Log.e(TAG, "❌ Error al verificar pago: ${response.code()}")
                return@withContext PaymentStatusResult.Error(
                    "No se pudo verificar el pago"
                )
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Exception al verificar pago", e)
            return@withContext PaymentStatusResult.Error(
                "Error de conexión: ${e.message}"
            )
        }
    }
}

// ============== MODELOS DE DATOS ==============

data class PayerInfo(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val phone: String?,
    val documentType: String?, // "DNI", "RUC", etc.
    val documentNumber: String?
)

sealed class PaymentResult {
    data class Success(
        val preferenceId: String,
        val paymentUrl: String
    ) : PaymentResult()
    
    data class Error(
        val message: String
    ) : PaymentResult()
}

sealed class PaymentStatusResult {
    data class Success(
        val status: String, // "approved", "pending", "rejected"
        val statusDetail: String,
        val amount: Double,
        val paymentMethodId: String?
    ) : PaymentStatusResult()
    
    data class Error(
        val message: String
    ) : PaymentStatusResult()
}
