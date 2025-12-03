package com.ropa.smartfashionecommerce.network

import com.ropa.smartfashionecommerce.model.ApiResponse
import com.ropa.smartfashionecommerce.model.ColorDto
import com.ropa.smartfashionecommerce.model.HomeDataDto
import com.ropa.smartfashionecommerce.model.ProductDetailData
import com.ropa.smartfashionecommerce.model.ProductListData
import com.ropa.smartfashionecommerce.model.SizeDto
import com.ropa.smartfashionecommerce.model.UserOrdersResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Field
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // Catálogo tipo web: /api/home/
    @GET("api/home/")
    suspend fun getHome(
        @Query("category_id") categoryId: Int? = null,
        @Query("q") query: String? = null,
        @Query("size") sizeId: Int? = null,
        @Query("color") colorId: Int? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null
    ): Response<ApiResponse<HomeDataDto>>

    @GET("api/products/")
    suspend fun getProductos(): Response<ApiResponse<ProductListData>>

    @GET("api/products/{id}/")
    suspend fun getProductDetail(@Path("id") id: Int): Response<ApiResponse<ProductDetailData>>

    @GET("api/sizes/")
    suspend fun getSizes(): Response<ApiResponse<List<SizeDto>>>

    @GET("api/colors/")
    suspend fun getColors(): Response<ApiResponse<List<ColorDto>>>

    // Checkout backend (Stripe como en el web)
    @POST("api/checkout/confirm/")
    suspend fun checkoutConfirm(
        @Body body: CheckoutConfirmRequest
    ): Response<CheckoutConfirmResponse>

    @POST("api/payments/create_session/")
    suspend fun createStripeSession(
        @Body body: CheckoutConfirmRequest
    ): Response<CreateSessionResponse>

    // Solicitudes de devolución / reembolso
    @POST("api/returns/")
    suspend fun createReturnRequest(
        @Body body: ReturnRequestBody
    ): Response<ReturnResponse>

    @GET("api/returns/")
    suspend fun listReturnRequests(): Response<ReturnListResponse>

    // Historial de pedidos del usuario (web/móvil comparten orders)
    @GET("api/profile/envios/")
    suspend fun getUserOrders(
        @Query("email") email: String? = null
    ): Response<UserOrdersResponse>

    // Direcciones guardadas del usuario
    @GET("api/addresses/")
    suspend fun getUserAddresses(
        @Query("email") email: String? = null
    ): Response<UserAddressesResponse>

    // Categorías de catálogo tipo web (para CatalogActivity)
    @GET("api/catalog/categories/")
    suspend fun getCatalogCategories(): Response<CatalogCategoriesResponse>

    // Carrito compartido web/móvil
    @GET("api/cart/")
    suspend fun getCartState(
        @Query("email") email: String? = null
    ): Response<CartStateResponse>

    @POST("api/cart/")
    suspend fun setCartState(
        @Body body: CartStateData,
        @Query("email") email: String? = null
    ): Response<CartStateResponse>

    // Favoritos compartidos web/móvil
    @GET("api/favorites/")
    suspend fun getFavoritesState(
        @Query("email") email: String? = null
    ): Response<FavoritesStateResponse>

    @POST("api/favorites/")
    suspend fun setFavoritesState(
        @Body body: FavoritesStateData,
        @Query("email") email: String? = null
    ): Response<FavoritesStateResponse>

    // Chat IA con el asistente de SmartFashion
    @POST("api/chatbot/query")
    suspend fun chatbotQuery(
        @Body body: ChatbotRequest
    ): Response<ChatbotResponse>
}
