package com.mehmetmertmazici.domaincheckercompose.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mehmetmertmazici.domaincheckercompose.model.AddonFeeResponse
import com.mehmetmertmazici.domaincheckercompose.model.BankTransferInfoResponse
import com.mehmetmertmazici.domaincheckercompose.model.CartItem
import com.mehmetmertmazici.domaincheckercompose.model.CompleteOrderRequest
import com.mehmetmertmazici.domaincheckercompose.model.CompleteOrderResponse
import com.mehmetmertmazici.domaincheckercompose.model.PaymentMethodsResponse
import com.mehmetmertmazici.domaincheckercompose.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// DataStore extension for cart
private val Context.cartDataStore: DataStore<Preferences> by preferencesDataStore(name = "cart_prefs")

class CartRepository(private val context: Context) {

    private val apiService = ApiClient.apiService
    private val gson = Gson()

    // Coroutine scope for background operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In-memory cart storage (synced with DataStore)
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    // Cached addon fees
    private var cachedAddonFees: AddonFeeResponse? = null

    companion object {
        private const val TAG = "CartRepository"
        private val KEY_CART_ITEMS = stringPreferencesKey("cart_items")
    }

    init {
        // Uygulama başladığında sepeti restore et
        scope.launch {
            restoreCart()
        }
    }

    // ============================================
    // PERSISTENCE - DataStore Operations
    // ============================================

    /**
     * Sepeti DataStore'dan yükle
     */
    private suspend fun restoreCart() {
        try {
            val prefs = context.cartDataStore.data.first()
            val cartJson = prefs[KEY_CART_ITEMS]

            if (!cartJson.isNullOrEmpty()) {
                val type = object : TypeToken<List<CartItem>>() {}.type
                val items: List<CartItem> = gson.fromJson(cartJson, type) ?: emptyList()
                _cartItems.value = items
                Log.d(TAG, "Cart restored: ${items.size} items")
            } else {
                Log.d(TAG, "No saved cart found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring cart", e)
            _cartItems.value = emptyList()
        }
    }

    /**
     * Sepeti DataStore'a kaydet
     */
    private suspend fun saveCart() {
        try {
            val cartJson = gson.toJson(_cartItems.value)
            context.cartDataStore.edit { prefs ->
                prefs[KEY_CART_ITEMS] = cartJson
            }
            Log.d(TAG, "Cart saved: ${_cartItems.value.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving cart", e)
        }
    }

    /**
     * Sepet değiştiğinde otomatik kaydet
     */
    private fun persistCart() {
        scope.launch {
            saveCart()
        }
    }

    // ============================================
    // CART OPERATIONS
    // ============================================

    fun addToCart(item: CartItem) {
        _cartItems.update { currentItems ->
            val existingIndex = currentItems.indexOfFirst { it.domain == item.domain }
            if (existingIndex >= 0) {
                currentItems.toMutableList().apply {
                    this[existingIndex] = item
                }
            } else {
                currentItems + item
            }
        }
        persistCart() // Değişikliği kaydet
    }

    fun removeFromCart(domain: String) {
        _cartItems.update { currentItems ->
            currentItems.filter { it.domain != domain }
        }
        persistCart() // Değişikliği kaydet
    }

    fun updateCartItem(domain: String, update: (CartItem) -> CartItem) {
        _cartItems.update { currentItems ->
            currentItems.map { item ->
                if (item.domain == domain) update(item) else item
            }
        }
        persistCart() // Değişikliği kaydet
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        persistCart() // Değişikliği kaydet
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
                    Result.failure(Exception("Ödeme yöntemleri alınamadı"))
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