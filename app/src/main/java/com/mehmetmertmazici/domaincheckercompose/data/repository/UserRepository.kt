package com.mehmetmertmazici.domaincheckercompose.data.repository

import com.mehmetmertmazici.domaincheckercompose.data.SessionManager
import com.mehmetmertmazici.domaincheckercompose.model.InvoiceDetailResponse
import com.mehmetmertmazici.domaincheckercompose.model.OrderDetailResponse
import com.mehmetmertmazici.domaincheckercompose.model.UserDataResponse
import com.mehmetmertmazici.domaincheckercompose.model.UserInfo
import com.mehmetmertmazici.domaincheckercompose.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserRepository(
    private val sessionManager: SessionManager
) {
    private val apiService = ApiClient.apiService

    // ============================================
    // USER DATA
    // ============================================

    suspend fun getUserData(): Result<UserDataResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserData()

                if (response.isSuccess) {
                    // Kullanıcı bilgilerini güncelle
                    response.user?.let { user ->
                        sessionManager.updateUserInfo(user)
                    }
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Kullanıcı bilgileri alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // ORDER DETAIL
    // ============================================

    suspend fun getOrder(orderId: Int): Result<OrderDetailResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getOrder(orderId)

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Sipariş bilgisi alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // INVOICE DETAIL
    // ============================================

    suspend fun getInvoice(invoiceId: Int): Result<InvoiceDetailResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getInvoice(invoiceId)

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Fatura bilgisi alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // LOCAL USER INFO (DataStore'dan)
    // ============================================

    fun getUserInfo(): Flow<UserInfo?> {
        return sessionManager.userInfo
    }

    fun getUserName(): Flow<String?> {
        return sessionManager.userName
    }

    fun getUserEmail(): Flow<String?> {
        return sessionManager.userEmail
    }

    fun isLoggedIn(): Flow<Boolean> {
        return sessionManager.isLoggedIn
    }
}