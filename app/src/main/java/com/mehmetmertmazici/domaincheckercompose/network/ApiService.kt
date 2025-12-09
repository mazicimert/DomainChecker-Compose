package com.mehmetmertmazici.domaincheckercompose.network

import com.mehmetmertmazici.domaincheckercompose.model.ApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

// ApiService
interface ApiService {
    // Domain sorgulama için POST isteği
    @POST("tr/api/search")
    @FormUrlEncoded
    suspend fun searchDomains(@Field("domain") domain: String): ApiResponse

    // Whois bilgisi için POST isteği (String döner)
    @POST("tr/api/whois")
    @FormUrlEncoded
    suspend fun getWhoisInfo(@Field("domain") domain: String): String
}