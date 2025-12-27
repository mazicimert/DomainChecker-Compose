package com.mehmetmertmazici.domaincheckercompose.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mehmetmertmazici.domaincheckercompose.model.City
import com.mehmetmertmazici.domaincheckercompose.model.Country
import com.mehmetmertmazici.domaincheckercompose.model.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository(private val context: Context) {

    private val gson = Gson()

    // Cache
    private var cachedCountries: List<Country>? = null
    private var cachedStates: List<State>? = null
    private var cachedCities: List<City>? = null


     //Tüm ülkeleri yükle

    suspend fun getCountries(): Result<List<Country>> = withContext(Dispatchers.IO) {
        try {
            cachedCountries?.let { return@withContext Result.success(it) }

            val jsonString = context.assets.open("countries.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Country>>() {}.type
            val countries: List<Country> = gson.fromJson(jsonString, type)
            cachedCountries = countries.sortedBy { it.name }
            Result.success(cachedCountries!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Seçilen ülkeye göre eyalet/illeri getir
    suspend fun getStatesByCountry(countryId: Int): Result<List<State>> = withContext(Dispatchers.IO) {
        try {
            // Cache yoksa yükle
            if (cachedStates == null) {
                val jsonString = context.assets.open("states.json").bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<State>>() {}.type
                cachedStates = gson.fromJson(jsonString, type)
            }

            val filteredStates = cachedStates!!
                .filter { it.countryId == countryId }
                .sortedBy { it.name }

            Result.success(filteredStates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


     //Seçilen eyalet/ile göre şehir/ilçeleri getir
    suspend fun getCitiesByState(stateId: Int): Result<List<City>> = withContext(Dispatchers.IO) {
        try {
            // Cache yoksa yükle
            if (cachedCities == null) {
                val jsonString = context.assets.open("cities.json").bufferedReader().use { it.readText() }
                val type = object : TypeToken<List<City>>() {}.type
                cachedCities = gson.fromJson(jsonString, type)
            }

            val filteredCities = cachedCities!!
                .filter { it.stateId == stateId }
                .sortedBy { it.name }

            Result.success(filteredCities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Ülke koduna göre ülke bul
    suspend fun getCountryByCode(iso2: String): Country? = withContext(Dispatchers.IO) {
        if (cachedCountries == null) {
            getCountries()
        }
        cachedCountries?.find { it.iso2 == iso2 }
    }


    // Cache'i temizle
    fun clearCache() {
        cachedCountries = null
        cachedStates = null
        cachedCities = null
    }
}