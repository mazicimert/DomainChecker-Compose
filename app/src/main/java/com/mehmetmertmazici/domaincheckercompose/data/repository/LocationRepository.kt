package com.mehmetmertmazici.domaincheckercompose.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mehmetmertmazici.domaincheckercompose.model.Country
import com.mehmetmertmazici.domaincheckercompose.model.District
import com.mehmetmertmazici.domaincheckercompose.model.DistrictJsonWrapper
import com.mehmetmertmazici.domaincheckercompose.model.Province
import com.mehmetmertmazici.domaincheckercompose.model.ProvinceJsonWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext


class LocationRepository(private val context: Context) {

    private val gson = Gson()

    // Cache
    private var cachedCountries: List<Country>? = null
    private var cachedProvinces: List<Province>? = null
    private var cachedDistricts: List<District>? = null

    // ============================================
    // PUBLIC API
    // ============================================

    /**
     * Tüm lokasyon verilerini paralel olarak yükler
     */
    suspend fun loadAllLocationData(): Result<Triple<List<Country>, List<Province>, List<District>>> {
        return withContext(Dispatchers.IO) {
            try {
                coroutineScope {
                    val countriesDeferred = async { loadCountries() }
                    val provincesDeferred = async { loadProvinces() }
                    val districtsDeferred = async { loadDistricts() }

                    val countries = countriesDeferred.await().getOrThrow()
                    val provinces = provincesDeferred.await().getOrThrow()
                    val districts = districtsDeferred.await().getOrThrow()

                    Result.success(Triple(countries, provinces, districts))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Ülkeleri yükler
     */
    suspend fun loadCountries(forceRefresh: Boolean = false): Result<List<Country>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh && cachedCountries != null) {
                    return@withContext Result.success(cachedCountries!!)
                }

                val jsonString = readAssetFile("countries.json")
                val type = object : TypeToken<List<Country>>() {}.type
                val countries: List<Country> = gson.fromJson(jsonString, type)

                // Türkiye'yi en üste al, sonra alfabetik sırala
                val sortedCountries = countries.sortedWith(compareBy(
                    { it.iso2 != "TR" }, // TR en üstte
                    { it.name }          // Sonra alfabetik
                ))

                cachedCountries = sortedCountries
                Result.success(sortedCountries)
            } catch (e: Exception) {
                Result.failure(Exception("Ülkeler yüklenemedi: ${e.message}"))
            }
        }
    }

    /**
     * Türkiye illerini yükler
     */
    suspend fun loadProvinces(forceRefresh: Boolean = false): Result<List<Province>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh && cachedProvinces != null) {
                    return@withContext Result.success(cachedProvinces!!)
                }

                val jsonString = readAssetFile("il.json")
                val provinces = parsePhpMyAdminJson<Province>(jsonString)

                // Alfabetik sırala
                val sortedProvinces = provinces.sortedBy { it.name }

                cachedProvinces = sortedProvinces
                Result.success(sortedProvinces)
            } catch (e: Exception) {
                Result.failure(Exception("İller yüklenemedi: ${e.message}"))
            }
        }
    }

    /**
     * Türkiye ilçelerini yükler
     */
    suspend fun loadDistricts(forceRefresh: Boolean = false): Result<List<District>> {
        return withContext(Dispatchers.IO) {
            try {
                if (!forceRefresh && cachedDistricts != null) {
                    return@withContext Result.success(cachedDistricts!!)
                }

                val jsonString = readAssetFile("ilce.json")
                val districts = parsePhpMyAdminJsonDistrict(jsonString)

                // Alfabetik sırala
                val sortedDistricts = districts.sortedBy { it.name }

                cachedDistricts = sortedDistricts
                Result.success(sortedDistricts)
            } catch (e: Exception) {
                Result.failure(Exception("İlçeler yüklenemedi: ${e.message}"))
            }
        }
    }

    /**
     * Belirli bir ile ait ilçeleri döndürür
     */
    suspend fun getDistrictsForProvince(provinceId: String): Result<List<District>> {
        return withContext(Dispatchers.IO) {
            try {
                val allDistricts = loadDistricts().getOrThrow()
                val filtered = allDistricts
                    .filter { it.provinceId == provinceId }
                    .sortedBy { it.name }
                Result.success(filtered)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Ülke araması yapar
     */
    suspend fun searchCountries(query: String): List<Country> {
        val countries = cachedCountries ?: loadCountries().getOrNull() ?: emptyList()
        if (query.isBlank()) return countries

        return countries.filter { it.matchesSearch(query) }
    }

    /**
     * İl araması yapar
     */
    suspend fun searchProvinces(query: String): List<Province> {
        val provinces = cachedProvinces ?: loadProvinces().getOrNull() ?: emptyList()
        if (query.isBlank()) return provinces

        return provinces.filter { it.matchesSearch(query) }
    }

    /**
     * İlçe araması yapar (belirli bir il içinde)
     */
    suspend fun searchDistricts(query: String, provinceId: String): List<District> {
        val districts = cachedDistricts ?: loadDistricts().getOrNull() ?: emptyList()
        val filtered = districts.filter { it.provinceId == provinceId }

        if (query.isBlank()) return filtered.sortedBy { it.name }

        return filtered.filter { it.matchesSearch(query) }.sortedBy { it.name }
    }

    // ============================================
    // CACHE MANAGEMENT
    // ============================================

    fun clearCache() {
        cachedCountries = null
        cachedProvinces = null
        cachedDistricts = null
    }

    fun isCacheLoaded(): Boolean {
        return cachedCountries != null && cachedProvinces != null && cachedDistricts != null
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================

    private fun readAssetFile(fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    /**
     * PHPMyAdmin export formatındaki JSON'u parse eder.
     * Format: [header, database, {type: "table", data: [...]}]
     */
    private inline fun <reified T> parsePhpMyAdminJson(jsonString: String): List<T> {
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val jsonArray: List<Map<String, Any>> = gson.fromJson(jsonString, type)

        // "data" içeren objeyi bul
        for (item in jsonArray) {
            if (item.containsKey("data")) {
                val dataJson = gson.toJson(item["data"])
                val dataType = object : TypeToken<List<T>>() {}.type
                return gson.fromJson(dataJson, dataType)
            }
        }

        return emptyList()
    }

    private fun parsePhpMyAdminJsonDistrict(jsonString: String): List<District> {
        val type = object : TypeToken<List<Map<String, Any>>>() {}.type
        val jsonArray: List<Map<String, Any>> = gson.fromJson(jsonString, type)

        for (item in jsonArray) {
            if (item.containsKey("data")) {
                val dataJson = gson.toJson(item["data"])
                val dataType = object : TypeToken<List<District>>() {}.type
                return gson.fromJson(dataJson, dataType)
            }
        }

        return emptyList()
    }
}