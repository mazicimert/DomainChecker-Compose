package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

// ============================================
// LOGIN
// ============================================

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("user")
    val user: UserInfo?,

    @SerializedName("token")
    val token: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"

    val requiresMailVerification: Boolean
        get() = isSuccess && message == "mail"


}

// ============================================
// REGISTER
// ============================================

/**
 * Hesap Türü
 */
enum class AccountType(val value: String, val displayName: String) {
    INDIVIDUAL("individual", "Bireysel"),
    CORPORATE("corporate", "Kurumsal");

    companion object {
        fun fromValue(value: String): AccountType {
            return entries.find { it.value == value } ?: INDIVIDUAL
        }
    }
}

data class RegisterRequest(
    val name: String,
    val surname: String,
    val citizen: String = "",
    val companyname: String = "",
    val email: String,
    val address: String,
    val address2: String = "",
    val city: String,
    val district: String,
    val zipcode: String,
    val country: String = "TR",
    val phone: String,
    val vergino: String = "",
    val password: String,
    val gsm: String,
    val gsmCode: String = "",

    @SerializedName("uyelik_turu")
    val membershipType: Int, // 1: Bireysel, 2: Kurumsal

    val contracts: List<Int> = emptyList()

)

data class RegisterResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("user")
    val user: UserInfo?,

    @SerializedName("token")
    val token: String?,

    @SerializedName("data")
    val data: RegisterData?

) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"


    val requiresMailVerification: Boolean
        get() = isSuccess && data?.status == "mail_verification_required"


    val clientId: Int?
        get() = data?.clientId


    val verificationEmail: String?
        get() = data?.email
}


data class RegisterData(
    @SerializedName("status")
    val status: String?,

    @SerializedName("message")
    val message: String?,

    @SerializedName("clientid")
    val clientId: Int?,

    @SerializedName("email")
    val email: String?
)


// ============================================
// FORGET PASSWORD
// ============================================

data class ForgetPasswordRequest(
    val email: String
)

data class ForgetPasswordResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

// ============================================
// SIGNOUT
// ============================================

data class SignOutResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}


// ============================================
// MAIL VERIFICATION - E-posta Doğrulama
// ============================================

data class VerifyMailCodeRequest(
    val clientId: Int,
    val email: String,
    val code: String
)

data class VerifyMailCodeResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: VerificationUserData?,

    @SerializedName("token")
    val token: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"

    /**
     * Kullanıcı ID'si
     */
    val userId: Int?
        get() = message?.id

    /**
     * Kullanıcı adı
     */
    val userName: String?
        get() = message?.name

    /**
     * Kullanıcı soyadı
     */
    val userSurname: String?
        get() = message?.surname

    /**
     * Yönlendirme URL'i
     */
    val redirectUrl: String?
        get() = message?.url

    /**
     * Doğrulama sonrası kullanıcı bilgilerini UserInfo'ya dönüştürür
     */
    fun toUserInfo(email: String): UserInfo {
        return UserInfo(
            id = message?.id,
            name = message?.name,
            surname = message?.surname,
            email = email,
            companyName = null,
            address = null,
            address2 = null,
            city = null,
            district = null,
            zipCode = null,
            country = null,
            phone = null,
            gsm = null,
            taxNumber = null
        )
    }
}

/**
 * E-posta doğrulama başarılı olduğunda dönen kullanıcı bilgileri
 */
data class VerificationUserData(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("surname")
    val surname: String?,

    @SerializedName("credit")
    val credit: String?,

    @SerializedName("url")
    val url: String?
)