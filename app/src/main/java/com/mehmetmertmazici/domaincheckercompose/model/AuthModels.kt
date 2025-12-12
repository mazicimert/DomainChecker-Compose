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
}

// ============================================
// REGISTER
// ============================================

data class RegisterRequest(
    val name: String,
    val surname: String,
    val companyname: String = "",
    val email: String,
    val adres: String,
    val adres2: String = "",
    val sehir: String,
    val ilce: String,
    val zipcode: String,
    val ulke: String = "TR",
    val phone: String,
    val vergino: String = "",
    val password: String,
    val gsm: String,
    val gsmCode: String = ""
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
    val token: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

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