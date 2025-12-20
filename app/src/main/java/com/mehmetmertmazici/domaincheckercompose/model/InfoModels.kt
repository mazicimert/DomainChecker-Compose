package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

// ============================================
// ANNOUNCEMENTS - Duyurular
// ============================================

data class AnnouncementsResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("announcements")
    val announcements: List<Announcement>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class Announcement(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String?,

    @SerializedName("announcement")
    val content: String?,

    @SerializedName("date")
    val date: String?,

    @SerializedName("published")
    val published: Boolean?
)

// ============================================
// KNOWLEDGEBASES - Bilgi Tabanı
// ============================================

data class KnowledgeBasesResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("categories")
    val categories: List<KnowledgeBaseCategory>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class KnowledgeBaseCategory(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("articles")
    val articles: List<KnowledgeBaseArticle>?
)

data class KnowledgeBaseArticle(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String?,

    @SerializedName("article")
    val content: String?,

    @SerializedName("views")
    val views: Int?,

    @SerializedName("useful")
    val useful: Int?,

    @SerializedName("date")
    val date: String?
)

// ============================================
// CONFIG MODELS - Yapılandırma
// ============================================

data class ConfigResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("value")
    val value: String?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class KdvConfigResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("kdv")
    val kdv: String?,

    @SerializedName("kdv_enabled")
    val kdvEnabled: Boolean?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"

    val kdvRate: Double
        get() = kdv?.toDoubleOrNull() ?: 0.0
}

// ============================================
// TLDs Response - Domain Uzantıları
// ============================================

data class TldsResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("tlds")
    val tlds: List<TldInfo>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class TldInfo(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("extension")
    val extension: String,

    @SerializedName("group")
    val group: String?,

    @SerializedName("register")
    val registerPrices: Map<String, String>?,

    @SerializedName("transfer")
    val transferPrices: Map<String, String>?,

    @SerializedName("renew")
    val renewPrices: Map<String, String>?
)

// ============================================
// ADDITIONAL FIELDS - Ek Alanlar
// ============================================

data class AdditionalFieldsResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("fields")
    val fields: Map<String, List<AdditionalField>>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class AdditionalField(
    @SerializedName("name")
    val name: String,

    @SerializedName("label")
    val label: String?,

    @SerializedName("type")
    val type: String?, // text, select, checkbox

    @SerializedName("required")
    val required: Boolean?,

    @SerializedName("options")
    val options: List<FieldOption>?,

    @SerializedName("description")
    val description: String?
)

data class FieldOption(
    @SerializedName("value")
    val value: String,

    @SerializedName("label")
    val label: String?
)

// ============================================
// CONTRACTS - Sözleşmeler (Üyelik)
// ============================================

data class ContractsResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: List<Contract>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"

    val contracts: List<Contract>
        get() = message ?: emptyList()
}

data class Contract(
    @SerializedName("id")
    val id: Int,

    @SerializedName("baslik")
    val title: String,

    @SerializedName("icerik")
    val content: String, // HTML içerik

    @SerializedName("versiyon")
    val version: String
)