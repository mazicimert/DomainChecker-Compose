package com.mehmetmertmazici.domaincheckercompose.model

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.mehmetmertmazici.domaincheckercompose.utils.DomainExtensions

data class Domain(
    @SerializedName("domain")
    val domain: String,

    @SerializedName("status")
    val status: String, // "available" or "registered"

    @SerializedName("price")
    val price: DomainPrice?
) {
    fun getTLD(context: Context): String {
        val twoPartTLDs = DomainExtensions.getTwoPartExtensions(context)
        val parts = domain.split(".")

        if (parts.size >= 3) {
            val lastTwoParts = "${parts[parts.size-2]}.${parts[parts.size-1]}"
            if (twoPartTLDs.contains(lastTwoParts.lowercase())) {
                return lastTwoParts
            }
        }
        return parts.last()
    }
}


data class DomainPrice(
    @SerializedName("categories")
    val categories: List<String>?,

    @SerializedName("addons")
    val addons: DomainAddons?,

    @SerializedName("group")
    val group: String?,

    @SerializedName("register")
    val register: Map<String, String>?,

    @SerializedName("transfer")
    val transfer: Map<String, String>?,

    @SerializedName("renew")
    val renew: Map<String, String>?,

    @SerializedName("grace_period")
    val gracePeriod: GracePeriod?,

    @SerializedName("grace_period_days")
    val gracePeriodDays: Int?,

    @SerializedName("grace_period_fee")
    val gracePeriodFee: String?,

    @SerializedName("redemption_period")
    val redemptionPeriod: RedemptionPeriod?,

    @SerializedName("redemption_period_days")
    val redemptionPeriodDays: Int?,

    @SerializedName("redemption_period_fee")
    val redemptionPeriodFee: String?
)


data class DomainAddons(
    @SerializedName("dns")
    val dns: Boolean,

    @SerializedName("email")
    val email: Boolean,

    @SerializedName("idprotect")
    val idprotect: Boolean
)