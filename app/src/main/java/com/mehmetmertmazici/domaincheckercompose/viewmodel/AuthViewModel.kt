package com.mehmetmertmazici.domaincheckercompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
import com.mehmetmertmazici.domaincheckercompose.model.RegisterRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Side Effects
sealed interface AuthEffect {
    data class ShowError(val message: String) : AuthEffect
    data class ShowSuccess(val message: String) : AuthEffect
    object NavigateToHome : AuthEffect
    object NavigateToLogin : AuthEffect
}

// Login UI State
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

// Register UI State
data class RegisterUiState(
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val phone: String = "",
    val gsm: String = "",
    val address: String = "",
    val city: String = "",
    val district: String = "",
    val zipCode: String = "",
    val country: String = "TR",
    val companyName: String = "",
    val taxNumber: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val currentStep: Int = 1, // 1: Kişisel, 2: İletişim, 3: Adres
    val errors: Map<String, String> = emptyMap()
)

// Forgot Password UI State
data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val isEmailSent: Boolean = false
)

class AuthViewModel : ViewModel() {

    private val authRepository = ServiceLocator.authRepository

    // Login State
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    // Register State
    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    // Forgot Password State
    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState.asStateFlow()

    // Effects
    private val _effect = Channel<AuthEffect>()
    val effect = _effect.receiveAsFlow()


    // ============================================
    // LOGIN İŞLEMLERİ
    // ============================================

    fun updateLoginEmail(email: String) {
        _loginState.update { it.copy(email = email, emailError = null) }
    }

    fun updateLoginPassword(password: String) {
        _loginState.update { it.copy(password = password, passwordError = null) }
    }

    fun toggleLoginPasswordVisibility() {
        _loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun login() {
        val state = _loginState.value

        // Validation
        var hasError = false
        var emailError: String? = null
        var passwordError: String? = null

        if (state.email.isBlank()) {
            emailError = "E-posta adresi gerekli"
            hasError = true
        } else if (!isValidEmail(state.email)) {
            emailError = "Geçerli bir e-posta adresi girin"
            hasError = true
        }

        if (state.password.isBlank()) {
            passwordError = "Şifre gerekli"
            hasError = true
        } else if (state.password.length < 6) {
            passwordError = "Şifre en az 6 karakter olmalı"
            hasError = true
        }

        if (hasError) {
            _loginState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        // Login işlemi
        viewModelScope.launch {
            _loginState.update { it.copy(isLoading = true) }

            val result = authRepository.login(state.email, state.password)

            result.fold(
                onSuccess = {
                    _loginState.update { it.copy(isLoading = false) }
                    sendEffect(AuthEffect.ShowSuccess("Giriş başarılı"))
                    sendEffect(AuthEffect.NavigateToHome)
                },
                onFailure = { error ->
                    _loginState.update { it.copy(isLoading = false) }
                    sendEffect(AuthEffect.ShowError(error.message ?: "Giriş başarısız"))
                }
            )
        }
    }

    fun clearLoginState() {
        _loginState.value = LoginUiState()
    }

    // ============================================
    // REGISTER İŞLEMLERİ
    // ============================================

    fun updateRegisterField(field: String, value: String) {
        _registerState.update { state ->
            val newErrors = state.errors.toMutableMap().apply { remove(field) }
            when (field) {
                "name" -> state.copy(name = value, errors = newErrors)
                "surname" -> state.copy(surname = value, errors = newErrors)
                "email" -> state.copy(email = value, errors = newErrors)
                "password" -> state.copy(password = value, errors = newErrors)
                "passwordConfirm" -> state.copy(passwordConfirm = value, errors = newErrors)
                "phone" -> state.copy(phone = value, errors = newErrors)
                "gsm" -> state.copy(gsm = value, errors = newErrors)
                "address" -> state.copy(address = value, errors = newErrors)
                "city" -> state.copy(city = value, errors = newErrors)
                "district" -> state.copy(district = value, errors = newErrors)
                "zipCode" -> state.copy(zipCode = value, errors = newErrors)
                "country" -> state.copy(country = value, errors = newErrors)
                "companyName" -> state.copy(companyName = value, errors = newErrors)
                "taxNumber" -> state.copy(taxNumber = value, errors = newErrors)
                else -> state
            }
        }
    }

    fun toggleRegisterPasswordVisibility() {
        _registerState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun nextRegisterStep() {
        val state = _registerState.value
        val errors = validateRegisterStep(state.currentStep)

        if (errors.isEmpty()) {
            if (state.currentStep < 3) {
                _registerState.update { it.copy(currentStep = state.currentStep + 1) }
            } else {
                register()
            }
        } else {
            _registerState.update { it.copy(errors = errors) }
        }
    }

    fun previousRegisterStep() {
        _registerState.update { state ->
            if (state.currentStep > 1) {
                state.copy(currentStep = state.currentStep - 1)
            } else {
                state
            }
        }
    }

    private fun validateRegisterStep(step: Int): Map<String, String> {
        val state = _registerState.value
        val errors = mutableMapOf<String, String>()

        when (step) {
            1 -> {
                if (state.name.isBlank()) errors["name"] = "Ad gerekli"
                if (state.surname.isBlank()) errors["surname"] = "Soyad gerekli"
                if (state.email.isBlank()) {
                    errors["email"] = "E-posta gerekli"
                } else if (!isValidEmail(state.email)) {
                    errors["email"] = "Geçerli bir e-posta girin"
                }
                if (state.password.isBlank()) {
                    errors["password"] = "Şifre gerekli"
                } else if (state.password.length < 6) {
                    errors["password"] = "En az 6 karakter"
                }
                if (state.passwordConfirm != state.password) {
                    errors["passwordConfirm"] = "Şifreler eşleşmiyor"
                }
            }
            2 -> {
                if (state.phone.isBlank()) errors["phone"] = "Telefon gerekli"
                if (state.gsm.isBlank()) errors["gsm"] = "GSM gerekli"
            }
            3 -> {
                if (state.address.isBlank()) errors["address"] = "Adres gerekli"
                if (state.city.isBlank()) errors["city"] = "Şehir gerekli"
                if (state.district.isBlank()) errors["district"] = "İlçe gerekli"
                if (state.zipCode.isBlank()) errors["zipCode"] = "Posta kodu gerekli"
            }
        }

        return errors
    }

    private fun register() {
        val state = _registerState.value

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true) }

            val request = RegisterRequest(
                name = state.name,
                surname = state.surname,
                email = state.email,
                password = state.password,
                phone = state.phone,
                gsm = state.gsm,
                adres = state.address,
                sehir = state.city,
                ilce = state.district,
                zipcode = state.zipCode,
                ulke = state.country,
                companyname = state.companyName,
                vergino = state.taxNumber
            )

            val result = authRepository.register(request)

            result.fold(
                onSuccess = {
                    _registerState.update { it.copy(isLoading = false) }
                    sendEffect(AuthEffect.ShowSuccess("Kayıt başarılı"))
                    sendEffect(AuthEffect.NavigateToHome)
                },
                onFailure = { error ->
                    _registerState.update { it.copy(isLoading = false) }
                    sendEffect(AuthEffect.ShowError(error.message ?: "Kayıt başarısız"))
                }
            )
        }
    }

    fun clearRegisterState() {
        _registerState.value = RegisterUiState()
    }

    // ============================================
    // FORGOT PASSWORD İŞLEMLERİ
    // ============================================

    fun updateForgotPasswordEmail(email: String) {
        _forgotPasswordState.update { it.copy(email = email, emailError = null) }
    }

    fun sendPasswordResetEmail() {
        val state = _forgotPasswordState.value

        // Validation
        if (state.email.isBlank()) {
            _forgotPasswordState.update { it.copy(emailError = "E-posta adresi gerekli") }
            return
        }

        if (!isValidEmail(state.email)) {
            _forgotPasswordState.update { it.copy(emailError = "Geçerli bir e-posta adresi girin") }
            return
        }

        viewModelScope.launch {
            _forgotPasswordState.update { it.copy(isLoading = true) }

            val result = authRepository.forgetPassword(state.email)

            result.fold(
                onSuccess = {
                    _forgotPasswordState.update { it.copy(isLoading = false, isEmailSent = true) }
                    sendEffect(AuthEffect.ShowSuccess("Şifre sıfırlama bağlantısı gönderildi"))
                },
                onFailure = { error ->
                    _forgotPasswordState.update { it.copy(isLoading = false) }
                    sendEffect(AuthEffect.ShowError(error.message ?: "İşlem başarısız"))
                }
            )
        }
    }

    fun clearForgotPasswordState() {
        _forgotPasswordState.value = ForgotPasswordUiState()
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun sendEffect(effect: AuthEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}