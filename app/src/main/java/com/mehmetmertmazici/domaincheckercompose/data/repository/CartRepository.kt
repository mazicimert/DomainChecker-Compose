package com.mehmetmertmazici.domaincheckercompose.data.repository

import com.mehmetmertmazici.domaincheckercompose.model.AddonFeeResponse
import com.mehmetmertmazici.domaincheckercompose.model.BankTransferInfoResponse
import com.mehmetmertmazici.domaincheckercompose.model.CartItem
import com.mehmetmertmazici.domaincheckercompose.model.CompleteOrderRequest
import com.mehmetmertmazici.domaincheckercompose.model.CompleteOrderResponse
import com.mehmetmertmazici.domaincheckercompose.model.PaymentMethodsResponse
import com.mehmetmertmazici.domaincheckercompose.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class CartRepository {
    private val apiService = ApiClient.apiService

    // In-memory cart storage
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Cached addon fees
    private var cachedAddonFees: AddonFeeResponse? = null

    // ============================================
    // CART OPERATIONS
    // ============================================

    fun addToCart(item: CartItem) {
        _cartItems.update { currentItems ->
            // Aynı domain zaten varsa güncelle, yoksa ekle
            val existingIndex = currentItems.indexOfFirst { it.domain == item.domain }
            if (existingIndex >= 0) {
                currentItems.toMutableList().apply {
                    this[existingIndex] = item
                }
            } else {
                currentItems + item
            }
        }
    }

    fun removeFromCart(domain: String) {
        _cartItems.update { currentItems ->
            currentItems.filter { it.domain != domain }
        }
    }

    fun updateCartItem(domain: String, update: (CartItem) -> CartItem) {
        _cartItems.update { currentItems ->
            currentItems.map { item ->
                if (item.domain == domain) update(item) else item
            }
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
    }

    fun getCartItemCount(): Int {
        return _cartItems.value.size
    }

    fun isInCart(domain: String): Boolean {
        return _cartItems.value.any { it.domain == domain }
    }

    // ============================================
    // ADDON FEES
    // ============================================

    suspend fun getAddonFees(): Result<AddonFeeResponse> {
        // Cache varsa döndür
        cachedAddonFees?.let { return Result.success(it) }

        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAddonFees()

                if (response.isSuccess) {
                    cachedAddonFees = response
                    Result.success(response)
                } else {
                    Result.failure(Exception("Ek hizmet fiyatları alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // PAYMENT METHODS
    // ============================================

    suspend fun getPaymentMethods(): Result<PaymentMethodsResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPaymentMethods()

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Ödeme yöntemleri alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // BANK TRANSFER INFO
    // ============================================

    suspend fun getBankTransferInfo(): Result<BankTransferInfoResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBankTransferInfo()

                if (response.isSuccess) {
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Banka bilgileri alınamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // COMPLETE ORDER
    // ============================================

    suspend fun completeOrder(
        paymentMethod: String,
        cardNumber: String? = null,
        cardCv2: String? = null,
        cardExp: String? = null,
        promoCode: String? = null
    ): Result<CompleteOrderResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val basketItems = _cartItems.value.map { it.toBasketItem() }

                if (basketItems.isEmpty()) {
                    return@withContext Result.failure(Exception("Sepet boş"))
                }

                val request = CompleteOrderRequest(
                    paymentMethod = paymentMethod,
                    cardNumber = cardNumber,
                    cardCv2 = cardCv2,
                    cardExp = cardExp,
                    promoCode = promoCode,
                    basket = basketItems
                )

                val response = apiService.completeOrder(request)

                if (response.isSuccess) {
                    // Sipariş başarılı, sepeti temizle
                    clearCart()
                    Result.success(response)
                } else {
                    Result.failure(Exception(response.message ?: "Sipariş tamamlanamadı"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ============================================
    // PRICE CALCULATIONS
    // ============================================

    fun calculateSubtotal(): Double {
        return _cartItems.value.sumOf { item ->
            item.getBasePrice()
        }
    }

    suspend fun calculateTotal(): Double {
        val addonFees = getAddonFees().getOrNull()
        return _cartItems.value.sumOf { item ->
            item.getTotalPrice(addonFees)
        }
    }

    fun calculateAddonsTotal(): Double {
        val addonFees = cachedAddonFees ?: return 0.0

        return _cartItems.value.sumOf { item ->
            var addonsTotal = 0.0
            if (item.dnsEnabled) {
                addonsTotal += addonFees.dns?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            }
            if (item.emailEnabled) {
                addonsTotal += addonFees.email?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            }
            if (item.idProtectEnabled) {
                addonsTotal += addonFees.idProtect?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            }
            addonsTotal
        }
    }
}