package com.mehmetmertmazici.domaincheckercompose.data.repository

import com.mehmetmertmazici.domaincheckercompose.data.SessionManager
import com.mehmetmertmazici.domaincheckercompose.model.Contract
import com.mehmetmertmazici.domaincheckercompose.model.ForgetPasswordResponse
import com.mehmetmertmazici.domaincheckercompose.model.LoginResponse
import com.mehmetmertmazici.domaincheckercompose.model.RegisterRequest
import com.mehmetmertmazici.domaincheckercompose.model.RegisterResponse
import com.mehmetmertmazici.domaincheckercompose.model.SignOutResponse
import com.mehmetmertmazici.domaincheckercompose.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(
    private val sessionManager: SessionManager
) {
    private val apiService = ApiClient.apiService

    // Cache for contracts
    private var cachedContracts: List<Contract>? = null

    // ============================================
    // LOGIN
    // ============================================

    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(email, password)

                if (response.isSuccess) {
                    // Session'ı kaydet
                    sessionManager.saveLoginSession(response)
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Giriş başarısız"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // REGISTER
    // ============================================

    suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.register(
                    name = request.name,
                    surname = request.surname,
                    citizen = request.citizen,
                    companyName = request.companyname,
                    email = request.email,
                    address = request.address,
                    address2 = request.address2,
                    city = request.city,
                    district = request.district,
                    zipCode = request.zipcode,
                    country = request.country,
                    phone = request.phone,
                    taxNumber = request.vergino,
                    password = request.password,
                    gsm = request.gsm,
                    gsmCode = request.gsmCode,
                    membershipType = request.membershipType,
                    contracts = request.contracts
                )

                if (response.isSuccess) {
                    // Session'ı kaydet
                    sessionManager.saveRegisterSession(response)
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Kayıt başarısız"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // MEMBERSHIP CONTRACTS - Üyelik Sözleşmeleri
    // ============================================

    suspend fun getMembershipContracts(forceRefresh: Boolean = false): Result<List<Contract>> {
        return withContext(Dispatchers.IO) {
            try {
                // Cache kontrolü
                if (!forceRefresh && cachedContracts != null) {
                    return@withContext Result.success(cachedContracts!!)
                }

                val response = apiService.getMembershipContracts()

                if (response.isSuccess) {
                    cachedContracts = response.contracts
                    Result.success(response.contracts)
                } else {
                    Result.failure(Exception("Sözleşmeler yüklenemedi"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // FORGET PASSWORD
    // ============================================

    suspend fun forgetPassword(email: String): Result<ForgetPasswordResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.forgetPassword(email)

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "İşlem başarısız"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // LOGOUT
    // ============================================

    suspend fun logout(): Result<SignOutResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.signOut()

                // Her durumda session'ı temizle
                sessionManager.clearSession()
                ApiClient.clearSession()

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    // API hatası olsa bile local session temizlendi
                    Result.success(response)
                }
            } catch (e: Exception) {
                // Hata olsa bile local session'ı temizle
                sessionManager.clearSession()
                ApiClient.clearSession()
                Result.failure(e)
            }
        }
    }

    // ============================================
    // SESSION CHECK
    // ============================================

    suspend fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedInSync()
    }

    suspend fun restoreSession() {
        sessionManager.restoreSession()
    }
}
