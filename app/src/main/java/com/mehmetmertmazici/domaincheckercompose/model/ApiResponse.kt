package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: ApiMessage?
)

data class ApiMessage(
    @SerializedName("currency")
    val currency: Currency?,

    @SerializedName("domains")
    val domains: List<Domain>?
){

    override fun toString(): String {
        return when {
            domains != null -> "Found ${domains.size} domains"
            else -> "No domains found"
        }
    }
}