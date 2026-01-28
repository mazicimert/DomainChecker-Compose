package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

// ============================================
// CART ITEM - Sepetteki Domain
// ============================================

data class CartItem(
    val domain: String,
    val domainType: String = "register",
    val period: Int = 1,
    val price: DomainPrice?,
    val status: String = "available",
    val dnsEnabled: Boolean = false,
    val emailEnabled: Boolean = false,
    val idProtectEnabled: Boolean = false,
    val nameserver1: String = "ns1.new.ikwebtasarim.com",
    val nameserver2: String = "ns2.new.ikwebtasarim.com",
    val nameserver3: String = "",
    val nameserver4: String = "",
    val nameserver5: String = "",
    val eppCode: String = "",
    val additionalFields: Map<String, String> = emptyMap()
) {
    fun getBasePrice(): Double {
        val priceString = price?.register?.get(period.toString()) ?: "0.0"
        return priceString.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    fun getTotalPrice(addonFees: AddonFeeResponse?): Double {
        var total = getBasePrice()

        if (dnsEnabled) {
            val dnsFee = addonFees?.dns?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            total += dnsFee
        }
        if (emailEnabled) {
            val emailFee = addonFees?.email?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            total += emailFee
        }
        if (idProtectEnabled) {
            val idProtectFee = addonFees?.idProtect?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            total += idProtectFee
        }

        return total
    }

    fun toBasketItem(): BasketItem {
        return BasketItem(
            type = "domain",
            domainType = domainType,
            domain = domain,
            name = domain,
            billingCycle = "annually",
            price = price,
            status = status,
            period = period,
            nameserver1 = nameserver1,
            nameserver2 = nameserver2,
            nameserver3 = nameserver3,
            nameserver4 = nameserver4,
            nameserver5 = nameserver5,
            idProtection = idProtectEnabled.toString(),
            additionalFields = additionalFields,
            total = getBasePrice().toString(),
            eppCode = eppCode
        )
    }
}

// ============================================
// BASKET ITEM - API'ye gönderilecek format
// ============================================

data class BasketItem(
    @SerializedName("type")
    val type: String = "domain",

    @SerializedName("domaintype")
    val domainType: String = "register",

    @SerializedName("domain")
    val domain: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("billingcycle")
    val billingCycle: String = "annually",

    @SerializedName("price")
    val price: DomainPrice?,

    @SerializedName("status")
    val status: String = "available",

    @SerializedName("period")
    val period: Int = 1,

    @SerializedName("nameserver1")
    val nameserver1: String = "ns1.new.ikwebtasarim.com",

    @SerializedName("nameserver2")
    val nameserver2: String = "ns2.new.ikwebtasarim.com",

    @SerializedName("nameserver3")
    val nameserver3: String = "",

    @SerializedName("nameserver4")
    val nameserver4: String = "",

    @SerializedName("nameserver5")
    val nameserver5: String = "",

    @SerializedName("idprotection")
    val idProtection: String = "false",

    @SerializedName("additional_fields")
    val additionalFields: Map<String, String> = emptyMap(),

    @SerializedName("total")
    val total: String,

    @SerializedName("eppcode")
    val eppCode: String = ""
)

// ============================================
// ADDON FEE - Ek Hizmet Fiyatları (DÜZELTİLMİŞ)
// ============================================

/**
 * API Response Format:
 * {
 *   "code": 1,
 *   "status": "success",
 *   "message": {
 *     "currency_id": 1,
 *     "dns_management": "1.11",
 *     "email_forwarding": "1.12",
 *     "id_protection": "1.13"
 *   }
 * }
 */
data class AddonFeeResponse(
    @SerializedName("code")
    val code: Int? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: AddonFeeMessage? = null
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"

    // Convenience accessors with null safety
    val dns: String?
        get() = message?.dnsManagement

    val email: String?
        get() = message?.emailForwarding

    val idProtect: String?
        get() = message?.idProtection

    val currencyId: Int?
        get() = message?.currencyId

    // Debug helper
    override fun toString(): String {
        return "AddonFeeResponse(code=$code, status=$status, dns=$dns, email=$email, idProtect=$idProtect)"
    }
}

/**
 * Nested message object - MUST be a separate class for Gson to parse correctly
 */
data class AddonFeeMessage(
    @SerializedName("currency_id")
    val currencyId: Int? = null,

    @SerializedName("dns_management")
    val dnsManagement: String? = null,

    @SerializedName("email_forwarding")
    val emailForwarding: String? = null,

    @SerializedName("id_protection")
    val idProtection: String? = null
) {
    override fun toString(): String {
        return "AddonFeeMessage(dns=$dnsManagement, email=$emailForwarding, idProtect=$idProtection)"
    }
}

// ============================================
// PAYMENT METHODS - Ödeme Yöntemleri
// ============================================

data class PaymentMethodsResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: List<PaymentMethod>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"

    val paymentMethods: List<PaymentMethod>
        get() = message ?: emptyList()
}

data class PaymentMethod(
    @SerializedName("module")
    val module: String,

    @SerializedName("displayname")
    val displayName: String
) {
    val isBankTransfer: Boolean
        get() = module == "banktransfer"

    val isStripe: Boolean
        get() = module == "stripe"

    val icon: String
        get() = when (module) {
            "banktransfer" -> "🏦"
            "stripe" -> "💳"
            else -> "💰"
        }
}

// ============================================
// BANK TRANSFER INFO
// ============================================

data class BankTransferInfoResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("banks")
    val banks: List<BankInfo>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class BankInfo(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("branch")
    val branch: String?,

    @SerializedName("account_name")
    val accountName: String?,

    @SerializedName("account_number")
    val accountNumber: String?,

    @SerializedName("iban")
    val iban: String?,

    @SerializedName("swift")
    val swift: String?
)

// ============================================
// COMPLETE ORDER
// ============================================

data class CompleteOrderRequest(
    @SerializedName("payment_method")
    val paymentMethod: String,

    @SerializedName("card_number")
    val cardNumber: String? = null,

    @SerializedName("card_cv2")
    val cardCv2: String? = null,

    @SerializedName("card_exp")
    val cardExp: String? = null,

    @SerializedName("promocode")
    val promoCode: String? = null,

    @SerializedName("basket")
    val basket: List<BasketItem>
)

data class CompleteOrderResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("order_id")
    val orderId: Int?,

    @SerializedName("invoice_id")
    val invoiceId: Int?,

    @SerializedName("redirect_url")
    val redirectUrl: String?,

    @SerializedName("total")
    val total: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

// ============================================
// PROMO CODE
// ============================================

data class PromoCodeResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("discount")
    val discount: DiscountInfo?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class DiscountInfo(
    @SerializedName("type")
    val type: String?,

    @SerializedName("value")
    val value: String?,

    @SerializedName("description")
    val description: String?
)