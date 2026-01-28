package com.mehmetmertmazici.domaincheckercompose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
import com.mehmetmertmazici.domaincheckercompose.model.AddonFeeResponse
import com.mehmetmertmazici.domaincheckercompose.model.CartItem
import com.mehmetmertmazici.domaincheckercompose.model.Domain
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ============================================
// CART EFFECTS
// ============================================
sealed interface CartEffect {
    data class ShowMessage(val message: String) : CartEffect
    data class ShowError(val message: String) : CartEffect
    object NavigateToCheckout : CartEffect
    object NavigateToLogin : CartEffect
}

// ============================================
// CART UI STATE
// ============================================
data class CartUiState(
    val cartItems: List<CartItem> = emptyList(),
    val addonFees: AddonFeeResponse? = null,
    val isLoading: Boolean = false,
    val isLoadingFees: Boolean = true, // Addon fees yüklenirken true
    val subtotal: Double = 0.0,
    val addonsTotal: Double = 0.0,
    val grandTotal: Double = 0.0,
    val currencySymbol: String = "€",
    val error: String? = null
) {
    val isEmpty: Boolean get() = cartItems.isEmpty()
    val itemCount: Int get() = cartItems.size

    // Addon fiyatları için safe getters
    val dnsFee: String get() = addonFees?.dns ?: DEFAULT_DNS_FEE
    val emailFee: String get() = addonFees?.email ?: DEFAULT_EMAIL_FEE
    val idProtectFee: String get() = addonFees?.idProtect ?: DEFAULT_ID_PROTECT_FEE

    companion object {
        // Fallback değerler - API'den alınamazsa kullanılır
        const val DEFAULT_DNS_FEE = "1.11"
        const val DEFAULT_EMAIL_FEE = "1.12"
        const val DEFAULT_ID_PROTECT_FEE = "1.13"
    }
}

// ============================================
// CART VIEW MODEL
// ============================================
class CartViewModel : ViewModel() {

    private val cartRepository = ServiceLocator.cartRepository

    private val _addonFees = MutableStateFlow<AddonFeeResponse?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _isLoadingFees = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    private val _effect = Channel<CartEffect>()
    val effect = _effect.receiveAsFlow()

    // Combine cart items with addon fees to create UI state
    val uiState: StateFlow<CartUiState> = combine(
        cartRepository.cartItems,
        _addonFees,
        _isLoading,
        _isLoadingFees,
        _error
    ) { items, fees, loading, loadingFees, error ->
        val subtotal = calculateSubtotal(items)
        val addonsTotal = calculateAddonsTotal(items, fees)
        val grandTotal = subtotal + addonsTotal

        CartUiState(
            cartItems = items,
            addonFees = fees,
            isLoading = loading,
            isLoadingFees = loadingFees,
            subtotal = subtotal,
            addonsTotal = addonsTotal,
            grandTotal = grandTotal,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CartUiState()
    )

    companion object {
        private const val TAG = "CartViewModel"
    }

    init {
        Log.d(TAG, "CartViewModel initialized, loading addon fees...")
        loadAddonFees()
    }

    // ============================================
    // ADDON FEES
    // ============================================
    fun loadAddonFees() {
        viewModelScope.launch {
            _isLoadingFees.value = true
            _error.value = null

            Log.d(TAG, "Starting to load addon fees from API...")

            cartRepository.getAddonFees()
                .onSuccess { response ->
                    Log.d(TAG, "=== ADDON FEES API RESPONSE ===")
                    Log.d(TAG, "Full response: $response")
                    Log.d(TAG, "Code: ${response.code}")
                    Log.d(TAG, "Status: ${response.status}")
                    Log.d(TAG, "Message object: ${response.message}")
                    Log.d(TAG, "DNS (via accessor): ${response.dns}")
                    Log.d(TAG, "Email (via accessor): ${response.email}")
                    Log.d(TAG, "IDProtect (via accessor): ${response.idProtect}")
                    Log.d(TAG, "================================")

                    _addonFees.value = response

                    // Verify it was set correctly
                    Log.d(TAG, "Addon fees set to state: ${_addonFees.value}")
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load addon fees", e)
                    Log.e(TAG, "Error message: ${e.message}")
                    _error.value = "Ek hizmet fiyatları yüklenemedi: ${e.message}"

                    // Fallback: Use default values
                    Log.d(TAG, "Using fallback default addon fees")
                }

            _isLoadingFees.value = false
        }
    }

    // ============================================
    // CART OPERATIONS
    // ============================================

    fun addToCart(domain: Domain) {
        if (domain.status != "available") {
            sendEffect(CartEffect.ShowError("Bu domain müsait değil"))
            return
        }

        val cartItem = CartItem(
            domain = domain.domain,
            domainType = "register",
            period = 1,
            price = domain.price,
            status = domain.status,
            dnsEnabled = false,
            emailEnabled = false,
            idProtectEnabled = false
        )

        cartRepository.addToCart(cartItem)
        sendEffect(CartEffect.ShowMessage("${domain.domain} sepete eklendi"))
    }

    fun removeFromCart(domain: String) {
        cartRepository.removeFromCart(domain)
        sendEffect(CartEffect.ShowMessage("$domain sepetten çıkarıldı"))
    }

    fun isInCart(domain: String): Boolean {
        return cartRepository.isInCart(domain)
    }

    fun updatePeriod(domain: String, period: Int) {
        if (period < 1 || period > 10) return

        cartRepository.updateCartItem(domain) { item ->
            item.copy(period = period)
        }
    }

    fun toggleDns(domain: String) {
        cartRepository.updateCartItem(domain) { item ->
            item.copy(dnsEnabled = !item.dnsEnabled)
        }
    }

    fun toggleEmail(domain: String) {
        cartRepository.updateCartItem(domain) { item ->
            item.copy(emailEnabled = !item.emailEnabled)
        }
    }

    fun toggleIdProtect(domain: String) {
        cartRepository.updateCartItem(domain) { item ->
            item.copy(idProtectEnabled = !item.idProtectEnabled)
        }
    }

    fun clearCart() {
        cartRepository.clearCart()
        sendEffect(CartEffect.ShowMessage("Sepet temizlendi"))
    }

    // ============================================
    // CHECKOUT
    // ============================================

    fun proceedToCheckout() {
        val state = uiState.value

        if (state.isEmpty) {
            sendEffect(CartEffect.ShowError("Sepetiniz boş"))
            return
        }

        sendEffect(CartEffect.NavigateToCheckout)
    }

    // ============================================
    // PRICE CALCULATIONS
    // ============================================

    private fun calculateSubtotal(items: List<CartItem>): Double {
        return items.sumOf { item ->
            val priceString = item.price?.register?.get(item.period.toString()) ?: "0"
            priceString.replace(",", ".").toDoubleOrNull() ?: 0.0
        }
    }

    private fun calculateAddonsTotal(items: List<CartItem>, fees: AddonFeeResponse?): Double {
        // Fallback değerler kullan
        val dnsFee = fees?.dns?.replace(",", ".")?.toDoubleOrNull()
            ?: CartUiState.DEFAULT_DNS_FEE.toDouble()
        val emailFee = fees?.email?.replace(",", ".")?.toDoubleOrNull()
            ?: CartUiState.DEFAULT_EMAIL_FEE.toDouble()
        val idProtectFee = fees?.idProtect?.replace(",", ".")?.toDoubleOrNull()
            ?: CartUiState.DEFAULT_ID_PROTECT_FEE.toDouble()

        return items.sumOf { item ->
            var total = 0.0
            if (item.dnsEnabled) total += dnsFee
            if (item.emailEnabled) total += emailFee
            if (item.idProtectEnabled) total += idProtectFee
            total
        }
    }

    /**
     * Tek bir cart item için toplam fiyatı hesaplar
     */
    fun calculateItemTotal(item: CartItem): Double {
        val fees = _addonFees.value
        val basePrice = item.price?.register?.get(item.period.toString())
            ?.replace(",", ".")
            ?.toDoubleOrNull() ?: 0.0

        // Fallback değerler kullan
        val dnsFee = fees?.dns?.replace(",", ".")?.toDoubleOrNull()
            ?: CartUiState.DEFAULT_DNS_FEE.toDouble()
        val emailFee = fees?.email?.replace(",", ".")?.toDoubleOrNull()
            ?: CartUiState.DEFAULT_EMAIL_FEE.toDouble()
        val idProtectFee = fees?.idProtect?.replace(",", ".")?.toDoubleOrNull()
            ?: CartUiState.DEFAULT_ID_PROTECT_FEE.toDouble()

        var addons = 0.0
        if (item.dnsEnabled) addons += dnsFee
        if (item.emailEnabled) addons += emailFee
        if (item.idProtectEnabled) addons += idProtectFee

        return basePrice + addons
    }

    /**
     * Seçilen yıla göre kayıt fiyatını döner
     */
    fun getRegisterPrice(item: CartItem): String {
        val price = item.price?.register?.get(item.period.toString()) ?: "0.00"
        return "€$price"
    }

    // ============================================
    // HELPERS
    // ============================================

    private fun sendEffect(effect: CartEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}