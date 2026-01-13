package com.mehmetmertmazici.domaincheckercompose.network

import com.mehmetmertmazici.domaincheckercompose.model.*
import retrofit2.http.*

interface ApiService {

    // ============================================
    // DOMAIN SEARCH - Mevcut
    // ============================================

    @POST("tr/api/search")
    @FormUrlEncoded
    suspend fun searchDomains(
        @Field("domain") domain: String
    ): ApiResponse

    @POST("tr/api/search_domain")
    @FormUrlEncoded
    suspend fun searchSingleDomain(
        @Field("domain") domain: String,
        @Field("tld") tld: String
    ): ApiResponse

    @POST("tr/api/whois")
    @FormUrlEncoded
    suspend fun getWhoisInfo(
        @Field("domain") domain: String
    ): String

    // ============================================
    // AUTH - Kimlik Doğrulama
    // ============================================

    @POST("tr/api/login")
    @FormUrlEncoded
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @POST("tr/api/register")
    @FormUrlEncoded
    suspend fun register(
        @Field("name") name: String,
        @Field("surname") surname: String,
        @Field("citizen") citizen: String,
        @Field("companyname") companyName: String,
        @Field("email") email: String,
        @Field("address") address: String,
        @Field("address2") address2: String,
        @Field("city") city: String,
        @Field("district") district: String,
        @Field("zipcode") zipCode: String,
        @Field("country") country: String,
        @Field("phone") phone: String,
        @Field("vergino") taxNumber: String,
        @Field("vergidairesi") taxOffice: String,
        @Field("password") password: String,
        @Field("uyelik_turu") membershipType: Int,
        @Field("contracts[]") contracts: List<Int>
    ): RegisterResponse

    @GET("tr/api/contracts/membership")
    suspend fun getMembershipContracts(): ContractsResponse

    @POST("tr/api/forget_password")
    @FormUrlEncoded
    suspend fun forgetPassword(
        @Field("email") email: String
    ): ForgetPasswordResponse

    @GET("tr/api/signout")
    suspend fun signOut(): SignOutResponse

    @POST("tr/api/verify_mail_code")
    @FormUrlEncoded
    suspend fun verifyMailCode(
        @Field("clientid") clientId: Int,
        @Field("email") email: String,
        @Field("code") code: String
    ): VerifyMailCodeResponse

    // ============================================
    // USER - Kullanıcı İşlemleri
    // ============================================

    @GET("tr/api/userData")
    suspend fun getUserData(): UserDataResponse

    @GET("tr/api/getOrder/{id}")
    suspend fun getOrder(
        @Path("id") orderId: Int
    ): OrderDetailResponse

    @GET("tr/api/getInvoice/{id}")
    suspend fun getInvoice(
        @Path("id") invoiceId: Int
    ): InvoiceDetailResponse

    // ============================================
    // CART & ORDER - Sepet ve Sipariş
    // ============================================

    @GET("tr/api/addon_fee")
    suspend fun getAddonFees(): AddonFeeResponse

    @GET("tr/api/payment_methods")
    suspend fun getPaymentMethods(): PaymentMethodsResponse

    @GET("tr/api/bank_transfer_info")
    suspend fun getBankTransferInfo(): BankTransferInfoResponse

    @POST("tr/api/complete_order")
    suspend fun completeOrder(
        @Body request: CompleteOrderRequest
    ): CompleteOrderResponse

    // ============================================
    // INFO - Bilgi Servisleri
    // ============================================

    @GET("tr/api/announcements")
    suspend fun getAnnouncements(): AnnouncementsResponse

    @GET("tr/api/knowledgebases")
    suspend fun getKnowledgeBases(): KnowledgeBasesResponse

    // ============================================
    // CONFIG - Yapılandırma
    // ============================================

    @GET("tr/api/tlds")
    suspend fun getTlds(): TldsResponse

    @GET("tr/api/top_tlds")
    suspend fun getTopTlds(): TldsResponse

    @GET("tr/api/list_products")
    suspend fun getProducts(): ApiResponse

    @GET("tr/api/additional_fields")
    suspend fun getAdditionalFields(): AdditionalFieldsResponse

    @GET("tr/api/custom_fields")
    suspend fun getCustomFields(): AdditionalFieldsResponse

    @GET("tr/api/conf/kdv")
    suspend fun getKdvConfig(): KdvConfigResponse

    @GET("tr/api/conf/funds")
    suspend fun getFundsConfig(): ConfigResponse

    @GET("tr/api/ping")
    suspend fun ping(): ConfigResponse
}