package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

// ============================================
// CART ITEM - Sepetteki Domain
// ============================================

data class CartItem(
    val domain: String,
    val domainType: String = "register", // register, transfer, renew
    val period: Int = 1, // 1-10 yıl
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
    // Domain'in seçilen periyoda göre fiyatını hesapla
    fun getBasePrice(): Double {
        val priceString = price?.register?.get(period.toString()) ?: "0.0"
        return priceString.replace(",", ".").toDoubleOrNull() ?: 0.0
    }

    // Toplam fiyat (addon'lar dahil)
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

    // API'ye gönderilecek formata çevir
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
// ADDON FEE - Ek Hizmet Fiyatları
// ============================================

data class AddonFeeResponse(
    @SerializedName("code")
    val code: Int?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("dns")
    val dns: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("idprotect")
    val idProtect: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
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
    val message: String?,

    @SerializedName("payment_methods")
    val paymentMethods: List<PaymentMethod>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class PaymentMethod(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String?, // credit_card, bank_transfer, balance, etc.

    @SerializedName("icon")
    val icon: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("enabled")
    val enabled: Boolean?
)

// ============================================
// BANK TRANSFER INFO - Havale Bilgileri
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
// COMPLETE ORDER - Sipariş Tamamlama
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
// PROMO CODE VALIDATION
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
    val type: String?, // percentage, fixed

    @SerializedName("value")
    val value: String?,

    @SerializedName("description")
    val description: String?
)