package com.mehmetmertmazici.domaincheckercompose.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
import com.mehmetmertmazici.domaincheckercompose.model.CartItem
import com.mehmetmertmazici.domaincheckercompose.model.PaymentMethod
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// ============================================
// CHECKOUT EFFECTS
// ============================================
sealed interface CheckoutEffect {
    data class ShowMessage(val message: String) : CheckoutEffect
    data class ShowError(val message: String) : CheckoutEffect
    data class NavigateToSuccess(val invoiceId: Int, val status: String) : CheckoutEffect
    object NavigateBack : CheckoutEffect
}

// ============================================
// CARD INPUT STATE
// ============================================
data class CardInputState(
    val cardName: String = "",
    val cardNumber: String = "",
    val cardExpiry: String = "",
    val cardCvv: String = "",
    // Validation error messages (null = valid)
    val cardNumberError: String? = null,
    val cardExpiryError: String? = null,
    val cardCvvError: String? = null
) {
    val isValid: Boolean
        get() = cardName.isNotBlank() &&
                cardNumber.length >= 15 &&
                cardNumberError == null &&
                cardExpiry.filter { it.isDigit() }.length >= 4 &&
                cardExpiryError == null &&
                cardCvv.length >= 3 &&
                cardCvvError == null

    val hasAnyError: Boolean
        get() = cardNumberError != null || cardExpiryError != null || cardCvvError != null
}

// ============================================
// CHECKOUT UI STATE
// ============================================
data class CheckoutUiState(
    val cartItems: List<CartItem> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val bankTransferInfo: String = "",
    val taxRate: Double = 0.0,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val cardInput: CardInputState = CardInputState(),
    val error: String? = null
) {
    val subtotal: Double
        get() = cartItems.sumOf { it.getBasePrice() }

    val taxAmount: Double
        get() = if (taxRate > 0) subtotal * (taxRate / 100) else 0.0

    val grandTotal: Double
        get() = subtotal + taxAmount

    val currencySymbol: String = "€"

    val isStripeSelected: Boolean
        get() = selectedPaymentMethod?.isStripe == true

    val isBankTransferSelected: Boolean
        get() = selectedPaymentMethod?.isBankTransfer == true

    val canSubmit: Boolean
        get() = when {
            isSubmitting -> false
            cartItems.isEmpty() -> false
            selectedPaymentMethod == null -> false
            isStripeSelected -> cardInput.isValid
            isBankTransferSelected -> true
            else -> true
        }

    // Extract IBAN from bank transfer info
    val iban: String?
        get() {
            val regex = Regex("IBAN:\\s*([A-Z0-9]+)")
            return regex.find(bankTransferInfo)?.groupValues?.get(1)
        }
}

// ============================================
// CHECKOUT VIEW MODEL
// ============================================
class CheckoutViewModel : ViewModel() {

    private val cartRepository = ServiceLocator.cartRepository

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    private val _selectedPaymentMethod = MutableStateFlow<PaymentMethod?>(null)
    private val _bankTransferInfo = MutableStateFlow("")
    private val _taxRate = MutableStateFlow(0.0)
    private val _isLoading = MutableStateFlow(true)
    private val _isSubmitting = MutableStateFlow(false)
    private val _cardInput = MutableStateFlow(CardInputState())
    private val _error = MutableStateFlow<String?>(null)

    private val _effect = Channel<CheckoutEffect>()
    val effect = _effect.receiveAsFlow()

    val uiState: StateFlow<CheckoutUiState> = combine(
        cartRepository.cartItems,
        _paymentMethods,
        _selectedPaymentMethod,
        _bankTransferInfo,
        combine(_taxRate, _isLoading, _isSubmitting, _cardInput, _error) { taxRate, isLoading, isSubmitting, cardInput, error ->
            CheckoutStatePartial(taxRate, isLoading, isSubmitting, cardInput, error)
        }
    ) { items, methods, selected, bankInfo, partial ->
        CheckoutUiState(
            cartItems = items,
            paymentMethods = methods,
            selectedPaymentMethod = selected,
            bankTransferInfo = bankInfo,
            taxRate = partial.taxRate,
            isLoading = partial.isLoading,
            isSubmitting = partial.isSubmitting,
            cardInput = partial.cardInput,
            error = partial.error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CheckoutUiState()
    )

    private data class CheckoutStatePartial(
        val taxRate: Double,
        val isLoading: Boolean,
        val isSubmitting: Boolean,
        val cardInput: CardInputState,
        val error: String?
    )

    companion object {
        private const val TAG = "CheckoutViewModel"
    }

    init {
        loadCheckoutData()
    }

    // ============================================
    // DATA LOADING
    // ============================================

    private fun loadCheckoutData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Load payment methods
            cartRepository.getPaymentMethods()
                .onSuccess { response ->
                    Log.d(TAG, "Payment methods loaded: ${response.paymentMethods}")
                    _paymentMethods.value = response.paymentMethods
                    // Auto-select first payment method
                    if (response.paymentMethods.isNotEmpty()) {
                        _selectedPaymentMethod.value = response.paymentMethods.first()
                    }
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load payment methods", e)
                    _error.value = "Ödeme yöntemleri yüklenemedi: ${e.message}"
                }

            // Load KDV config
            cartRepository.getKdvConfig()
                .onSuccess { response ->
                    Log.d(TAG, "KDV rate loaded: ${response.kdvRate}")
                    _taxRate.value = response.kdvRate
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load KDV config", e)
                    // Don't show error, just use 0% tax
                }

            // Load bank transfer info
            cartRepository.getBankTransferInfo()
                .onSuccess { response ->
                    Log.d(TAG, "Bank info loaded: ${response.message}")
                    _bankTransferInfo.value = response.message ?: ""
                }
                .onFailure { e ->
                    Log.e(TAG, "Failed to load bank info", e)
                }

            _isLoading.value = false
        }
    }

    // ============================================
    // PAYMENT METHOD SELECTION
    // ============================================

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
        Log.d(TAG, "Payment method selected: ${method.module}")
    }

    // ============================================
    // CARD INPUT & VALIDATION
    // ============================================

    fun updateCardName(name: String) {
        _cardInput.value = _cardInput.value.copy(cardName = name)
    }

    fun updateCardNumber(number: String) {
        // Remove non-digit characters and limit to 16 digits
        val cleanNumber = number.filter { it.isDigit() }.take(16)
        val error = validateCardNumber(cleanNumber)
        _cardInput.value = _cardInput.value.copy(
            cardNumber = cleanNumber,
            cardNumberError = error
        )
    }

    fun updateCardExpiry(expiry: String) {
        // Store only digits (MMYY format)
        val cleanExpiry = expiry.filter { it.isDigit() }.take(4)
        val error = validateExpiryDate(cleanExpiry)
        _cardInput.value = _cardInput.value.copy(
            cardExpiry = cleanExpiry,
            cardExpiryError = error
        )
    }

    fun updateCardCvv(cvv: String) {
        // Remove non-digit and limit to 4 (Amex uses 4)
        val cleanCvv = cvv.filter { it.isDigit() }.take(4)
        val error = validateCvv(cleanCvv)
        _cardInput.value = _cardInput.value.copy(
            cardCvv = cleanCvv,
            cardCvvError = error
        )
    }

    // ============================================
    // VALIDATION FUNCTIONS
    // ============================================

    /**
     * Validates card number using Luhn algorithm.
     * Returns error message if invalid, null if valid.
     */
    private fun validateCardNumber(number: String): String? {
        if (number.isEmpty()) return null // Don't show error for empty field
        if (number.length < 15) return "Kart numarası eksik."
        if (!isValidLuhn(number)) return "Kart numarası geçersiz."
        return null
    }

    /**
     * Luhn algorithm implementation for credit card validation.
     */
    private fun isValidLuhn(number: String): Boolean {
        if (number.isEmpty()) return false
        var sum = 0
        var alternate = false
        for (i in number.length - 1 downTo 0) {
            var digit = number[i].digitToInt()
            if (alternate) {
                digit *= 2
                if (digit > 9) digit -= 9
            }
            sum += digit
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    /**
     * Son kullanma tarihi formatını (AAYY) doğrular ve tarihin gelecekte olup olmadığını kontrol eder.
     * Geçersizse hata mesajı, geçerliyse null döndürür.
     */
    private fun validateExpiryDate(expiry: String): String? {
        // Alan boşsa hata gösterme
        if (expiry.isEmpty()) return null

        // 4 haneye (AA/YY) ulaşmadıysa eksik uyarısı ver
        if (expiry.length < 4) return "Son kullanma tarihi eksik"

        val month = expiry.take(2).toIntOrNull() ?: return "Geçersiz ay"
        val year = expiry.drop(2).toIntOrNull() ?: return "Geçersiz yıl"

        // Ay aralığını doğrula (01-12)
        if (month < 1 || month > 12) return "Ay 01-12 arasında olmalıdır"

        // Tarihin geçmişte olup olmadığını kontrol et
        val calendar = java.util.Calendar.getInstance()
        val currentYear = calendar.get(java.util.Calendar.YEAR) % 100
        val currentMonth = calendar.get(java.util.Calendar.MONTH) + 1

        if (year < currentYear || (year == currentYear && month < currentMonth)) {
            return "Geçmiş tarih kullanılamaz"
        }

        return null
    }

    /**
     * Validates CVV length (3 or 4 digits).
     * Returns error message if invalid, null if valid.
     */
    private fun validateCvv(cvv: String): String? {
        if (cvv.isEmpty()) return null // Don't show error for empty field
        if (cvv.length < 3) return "CVV numarası eksik."
        // 3-4 digits are valid (Amex uses 4)
        return null
    }

    // ============================================
    // ORDER SUBMISSION
    // ============================================

    fun completeOrder() {
        val state = uiState.value

        if (!state.canSubmit) {
            sendEffect(CheckoutEffect.ShowError("Lütfen tüm alanları doldurun"))
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null

            val paymentMethod = state.selectedPaymentMethod!!.module
            val cardInput = state.cardInput

            // Format card expiry as MM/YY (digits only from formatted "09 / 27")
            val expiryDigits = cardInput.cardExpiry.filter { it.isDigit() }
            val cardExp = if (state.isStripeSelected && expiryDigits.length == 4) {
                "${expiryDigits.take(2)}/${expiryDigits.takeLast(2)}"
            } else null

            cartRepository.completeOrder(
                paymentMethod = paymentMethod,
                cardName = if (state.isStripeSelected) cardInput.cardName else null,
                cardNumber = if (state.isStripeSelected) cardInput.cardNumber else null,
                cardCv2 = if (state.isStripeSelected) cardInput.cardCvv else null,
                cardExp = cardExp,
                subtotal = state.subtotal,
                taxRate = state.taxRate,
                taxes = state.taxAmount,
                total = state.grandTotal
            )
                .onSuccess { response ->
                    Log.d(TAG, "Order completed: invoiceId=${response.invoiceId}, orderId=${response.orderId}")

                    // Determine status based on payment method
                    val status = if (state.isBankTransferSelected) "Unpaid" else "Paid"

                    sendEffect(
                        CheckoutEffect.NavigateToSuccess(
                            invoiceId = response.invoiceId ?: 0,
                            status = status
                        )
                    )
                }
                .onFailure { e ->
                    Log.e(TAG, "Order failed", e)
                    _error.value = e.message ?: "Sipariş tamamlanamadı"
                    sendEffect(CheckoutEffect.ShowError(e.message ?: "Sipariş tamamlanamadı"))
                }

            _isSubmitting.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    // ============================================
    // HELPERS
    // ============================================

    private fun sendEffect(effect: CheckoutEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}