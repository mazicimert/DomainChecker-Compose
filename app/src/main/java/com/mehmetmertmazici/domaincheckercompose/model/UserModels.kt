package com.mehmetmertmazici.domaincheckercompose.model

import com.google.gson.annotations.SerializedName

// ============================================
// USER INFO - Ortak kullanıcı bilgisi modeli
// ============================================

data class UserInfo(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("surname")
    val surname: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("companyname")
    val companyName: String?,

    @SerializedName("address")
    val address: String?,

    @SerializedName("address2")
    val address2: String?,

    @SerializedName("city")
    val city: String?,

    @SerializedName("district")
    val district: String?,

    @SerializedName("zipcode")
    val zipCode: String?,

    @SerializedName("country")
    val country: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("gsm")
    val gsm: String?,

    @SerializedName("vergino")
    val taxNumber: String?
) {
    val fullName: String
        get() = "${name.orEmpty()} ${surname.orEmpty()}".trim()
}

// ============================================
// USER DATA RESPONSE
// ============================================

data class UserDataResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("user")
    val user: UserInfo?,

    @SerializedName("balance")
    val balance: BalanceInfo?,

    @SerializedName("domains")
    val domains: List<UserDomain>?,

    @SerializedName("orders")
    val orders: List<OrderSummary>?,

    @SerializedName("invoices")
    val invoices: List<InvoiceSummary>?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class BalanceInfo(
    @SerializedName("amount")
    val amount: String?,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("formatted")
    val formatted: String?
)

data class UserDomain(
    @SerializedName("id")
    val id: Int,

    @SerializedName("domain")
    val domain: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("registrationdate")
    val registrationDate: String?,

    @SerializedName("expirydate")
    val expiryDate: String?,

    @SerializedName("nextduedate")
    val nextDueDate: String?
)

// ============================================
// ORDER MODELS
// ============================================

data class OrderSummary(
    @SerializedName("id")
    val id: Int,

    @SerializedName("ordernum")
    val orderNumber: String?,

    @SerializedName("date")
    val date: String?,

    @SerializedName("amount")
    val amount: String?,

    @SerializedName("status")
    val status: String?
)

data class OrderDetailResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("order")
    val order: OrderDetail?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class OrderDetail(
    @SerializedName("id")
    val id: Int,

    @SerializedName("ordernum")
    val orderNumber: String?,

    @SerializedName("date")
    val date: String?,

    @SerializedName("amount")
    val amount: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("paymentmethod")
    val paymentMethod: String?,

    @SerializedName("items")
    val items: List<OrderItem>?,

    @SerializedName("invoiceid")
    val invoiceId: Int?
)

data class OrderItem(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("amount")
    val amount: String?,

    @SerializedName("domain")
    val domain: String?,

    @SerializedName("status")
    val status: String?
)

// ============================================
// INVOICE MODELS
// ============================================

data class InvoiceSummary(
    @SerializedName("id")
    val id: Int,

    @SerializedName("invoicenum")
    val invoiceNumber: String?,

    @SerializedName("date")
    val date: String?,

    @SerializedName("duedate")
    val dueDate: String?,

    @SerializedName("total")
    val total: String?,

    @SerializedName("status")
    val status: String?
)

data class InvoiceDetailResponse(
    @SerializedName("code")
    val code: Int,

    @SerializedName("status")
    val status: String,

    @SerializedName("message")
    val message: String?,

    @SerializedName("invoice")
    val invoice: InvoiceDetail?
) {
    val isSuccess: Boolean
        get() = code == 1 && status == "success"
}

data class InvoiceDetail(
    @SerializedName("id")
    val id: Int,

    @SerializedName("invoicenum")
    val invoiceNumber: String?,

    @SerializedName("date")
    val date: String?,

    @SerializedName("duedate")
    val dueDate: String?,

    @SerializedName("datepaid")
    val datePaid: String?,

    @SerializedName("subtotal")
    val subtotal: String?,

    @SerializedName("tax")
    val tax: String?,

    @SerializedName("total")
    val total: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("paymentmethod")
    val paymentMethod: String?,

    @SerializedName("items")
    val items: List<InvoiceItem>?
)

data class InvoiceItem(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("amount")
    val amount: String?
)