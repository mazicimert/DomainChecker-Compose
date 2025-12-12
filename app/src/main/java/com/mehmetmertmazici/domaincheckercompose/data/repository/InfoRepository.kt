package com.mehmetmertmazici.domaincheckercompose.data.repository

import com.mehmetmertmazici.domaincheckercompose.model.AdditionalFieldsResponse
import com.mehmetmertmazici.domaincheckercompose.model.AnnouncementsResponse
import com.mehmetmertmazici.domaincheckercompose.model.KdvConfigResponse
import com.mehmetmertmazici.domaincheckercompose.model.KnowledgeBasesResponse
import com.mehmetmertmazici.domaincheckercompose.model.TldsResponse
import com.mehmetmertmazici.domaincheckercompose.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class InfoRepository {
    private val apiService = ApiClient.apiService

    // Cache for frequently accessed data
    private var cachedAnnouncements: AnnouncementsResponse? = null
    private var cachedKdvConfig: KdvConfigResponse? = null
    private var cachedTlds: TldsResponse? = null

    // ============================================
    // ANNOUNCEMENTS
    // ============================================

    suspend fun getAnnouncements(forceRefresh: Boolean = false): Result<AnnouncementsResponse> {
        // Cache varsa ve refresh istenmiyorsa döndür
        if (!forceRefresh && cachedAnnouncements != null) {
            return Result.success(cachedAnnouncements!!)
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAnnouncements()

                if (response.isSuccess) {
                    cachedAnnouncements = response
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Duyurular alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // KNOWLEDGE BASES
    // ============================================

    suspend fun getKnowledgeBases(): Result<KnowledgeBasesResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getKnowledgeBases()

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Bilgi tabanı alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // TLDs
    // ============================================

    suspend fun getTlds(forceRefresh: Boolean = false): Result<TldsResponse> {
        if (!forceRefresh && cachedTlds != null) {
            return Result.success(cachedTlds!!)
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTlds()

                if (response.isSuccess) {
                    cachedTlds = response
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "TLD listesi alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getTopTlds(): Result<TldsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTopTlds()

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Popüler TLD listesi alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // CONFIG
    // ============================================

    suspend fun getKdvConfig(forceRefresh: Boolean = false): Result<KdvConfigResponse> {
        if (!forceRefresh && cachedKdvConfig != null) {
            return Result.success(cachedKdvConfig!!)
        }

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getKdvConfig()

                if (response.isSuccess) {
                    cachedKdvConfig = response
                    Result.success(response)
                } else {
                    Result.failure(Exception("KDV bilgisi alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // ADDITIONAL FIELDS (Domain kayıt için ek alanlar)
    // ============================================

    suspend fun getAdditionalFields(): Result<AdditionalFieldsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAdditionalFields()

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Ek alanlar alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCustomFields(): Result<AdditionalFieldsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCustomFields()

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Özel alanlar alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // CACHE OPERATIONS
    // ============================================

    fun clearCache() {
        cachedAnnouncements = null
        cachedKdvConfig = null
        cachedTlds = null
    }
}