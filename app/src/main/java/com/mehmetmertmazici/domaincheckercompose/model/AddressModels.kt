package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName


data class Country(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("iso2")
    val iso2: String,
    @SerializedName("iso3")
    val iso3: String? = null,
    @SerializedName("phone_code")
    val phoneCode: String? = null
)


data class State(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("country_id")
    val countryId: Int,
    @SerializedName("country_code")
    val countryCode: String? = null,
    @SerializedName("state_code")
    val stateCode: String? = null
)


data class City(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("state_id")
    val stateId: Int,
    @SerializedName("state_code")
    val stateCode: String? = null,
    @SerializedName("country_id")
    val countryId: Int? = null,
    @SerializedName("country_code")
    val countryCode: String? = null
)