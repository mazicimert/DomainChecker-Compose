package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

// ============================================
// LOGIN
// ============================================

data class LoginRequest(
    val email: String,
    val password: String
)

/**
 * Login başarılı olduğunda message içinde gelen kullanıcı bilgileri
 */
data class LoginUserData(
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

data class LoginResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: Any?, // String "mail" veya LoginUserData object olabilir

    @SerializedName("user")
    val user: UserInfo?,

    @SerializedName("token")
    val token: String?,

    @SerializedName("clientid")
    val clientId: Int? = null // 2FA için client ID - Login'de mail doğrulaması gerektiğinde döner
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"

    /**
     * Mail doğrulaması gerekiyor mu?
     * Backend message: "mail" döndüğünde true
     */
    val requiresMailVerification: Boolean
        get() = isSuccess && message == "mail"

    /**
     * message alanından kullanıcı bilgilerini çıkarır
     * Backend başarılı girişte message içinde user data döndürüyor
     */
    @Suppress("UNCHECKED_CAST")
    val loginUserData: LoginUserData?
        get() {
            if (message is Map<*, *>) {
                val map = message as Map<String, Any?>
                return LoginUserData(
                    id = (map["id"] as? Number)?.toInt(),
                    name = map["name"] as? String,
                    surname = map["surname"] as? String,
                    credit = map["credit"] as? String,
                    url = map["url"] as? String
                )
            }
            return null
        }

    /**
     * Kullanıcı ID'si - clientId, message veya user'dan alınır
     */
    val userId: Int?
        get() = clientId ?: loginUserData?.id ?: user?.id

    /**
     * Kullanıcı adı - message'dan veya user'dan alınır
     */
    val userName: String?
        get() = loginUserData?.name ?: user?.name

    /**
     * Kullanıcı soyadı - message'dan veya user'dan alınır
     */
    val userSurname: String?
        get() = loginUserData?.surname ?: user?.surname
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
    val vergidairesi: String = "",
    val password: String,

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
    val message: Any?, // String (hata mesajı) veya VerificationUserData object olabilir

    @SerializedName("token")
    val token: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"

    /**
     * Hata mesajı - message String ise döner
     */
    val errorMessage: String?
        get() = if (message is String) message else null

    /**
     * message alanından kullanıcı bilgilerini çıkarır
     * Backend başarılı doğrulamada message içinde user data döndürüyor
     */
    @Suppress("UNCHECKED_CAST")
    private val verificationUserData: VerificationUserData?
        get() {
            if (message is Map<*, *>) {
                val map = message as Map<String, Any?>
                return VerificationUserData(
                    id = (map["id"] as? Number)?.toInt(),
                    name = map["name"] as? String,
                    surname = map["surname"] as? String,
                    credit = map["credit"] as? String,
                    url = map["url"] as? String
                )
            }
            return null
        }

    /**
     * Kullanıcı ID'si
     */
    val userId: Int?
        get() = verificationUserData?.id

    /**
     * Kullanıcı adı
     */
    val userName: String?
        get() = verificationUserData?.name

    /**
     * Kullanıcı soyadı
     */
    val userSurname: String?
        get() = verificationUserData?.surname

    /**
     * Yönlendirme URL'i
     */
    val redirectUrl: String?
        get() = verificationUserData?.url

    /**
     * Doğrulama sonrası kullanıcı bilgilerini UserInfo'ya dönüştürür
     */
    fun toUserInfo(email: String): UserInfo {
        return UserInfo(
            id = verificationUserData?.id,
            name = verificationUserData?.name,
            surname = verificationUserData?.surname,
            email = email,
            companyName = null,
            address = null,
            address2 = null,
            city = null,
            district = null,
            zipCode = null,
            country = null,
            phone = null,
            taxNumber = null,
            taxOffice = null
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