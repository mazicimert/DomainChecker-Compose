package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

data class Currency(
    @SerializedName("id")
    val id: Int,

    @SerializedName("code")
    val code: String,

    @SerializedName("prefix")
    val prefix: String,

    @SerializedName("suffix")
    val suffix: String,

    @SerializedName("format")
    val format: Int,

    @SerializedName("rate")
    val rate: String
)

data class GracePeriod(
    @SerializedName("days")
    val days: Int,

    @SerializedName("price")
    val price: String
)

data class RedemptionPeriod(
    @SerializedName("days")
    val days: Int,

    @SerializedName("price")
    val price: String
)