package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

// ============================================
// COUNTRY (Ülke)
// ============================================

data class Country(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("iso2")
    val iso2: String,

    @SerializedName("iso3")
    val iso3: String? = null,

    @SerializedName("phonecode")
    val phoneCode: String? = null,

    @SerializedName("emoji")
    val emoji: String? = null
) {
    // Arama için kullanılacak
    fun matchesSearch(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return name.lowercase().contains(lowerQuery) ||
                iso2.lowercase().contains(lowerQuery)
    }
}

// ============================================
// PROVINCE (İl - Türkiye)
// ============================================

data class Province(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
) {
    fun matchesSearch(query: String): Boolean {
        return name.lowercase().contains(query.lowercase())
    }
}

// ============================================
// DISTRICT (İlçe - Türkiye)
// ============================================

data class District(
    @SerializedName("id")
    val id: String,

    @SerializedName("il_id")
    val provinceId: String,

    @SerializedName("name")
    val name: String
) {
    fun matchesSearch(query: String): Boolean {
        return name.lowercase().contains(query.lowercase())
    }
}

// ============================================
// JSON Wrapper Classes (PHPMyAdmin Export Format)
// ============================================

/**
 * il.json ve ilce.json dosyaları PHPMyAdmin'den export edilmiş.
 * Format: [header, database, table] şeklinde 3 obje içeriyor.
 */
data class ProvinceJsonWrapper(
    @SerializedName("type")
    val type: String,

    @SerializedName("data")
    val data: List<Province>? = null
)

data class DistrictJsonWrapper(
    @SerializedName("type")
    val type: String,

    @SerializedName("data")
    val data: List<District>? = null
)

// ============================================
// Location State for UI
// ============================================

data class LocationState(
    val countries: List<Country> = emptyList(),
    val provinces: List<Province> = emptyList(),
    val districts: List<District> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    // Seçilen ile göre filtrelenmiş ilçeler
    fun getDistrictsForProvince(provinceId: String): List<District> {
        return districts.filter { it.provinceId == provinceId }
    }

    // İl ID'sine göre il bul
    fun findProvinceById(id: String): Province? {
        return provinces.find { it.id == id }
    }

    // İl adına göre il bul
    fun findProvinceByName(name: String): Province? {
        return provinces.find { it.name.equals(name, ignoreCase = true) }
    }

    // İlçe ID'sine göre ilçe bul
    fun findDistrictById(id: String): District? {
        return districts.find { it.id == id }
    }

    // İlçe adına göre ilçe bul (belirli bir il içinde)
    fun findDistrictByName(name: String, provinceId: String): District? {
        return districts.find {
            it.name.equals(name, ignoreCase = true) && it.provinceId == provinceId
        }
    }

    // Ülke ISO koduna göre bul
    fun findCountryByIso2(iso2: String): Country? {
        return countries.find { it.iso2.equals(iso2, ignoreCase = true) }
    }

    // Ülke adına göre bul
    fun findCountryByName(name: String): Country? {
        return countries.find { it.name.equals(name, ignoreCase = true) }
    }
}