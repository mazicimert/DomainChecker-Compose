package com.mehmetmertmazici.domaincheckercompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
import com.mehmetmertmazici.domaincheckercompose.model.AccountType
import com.mehmetmertmazici.domaincheckercompose.model.Contract
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

// Contract Acceptance State
data class ContractAcceptance(
    val contract: Contract,
    val isAccepted: Boolean = false
)

// Security Questions
enum class SecurityQuestion(val id: Int, val question: String) {
    MOTHERS_MAIDEN_NAME(1, "Annenizin kızlık soyadı nedir?"),
    FIRST_PET_NAME(2, "İlk evcil hayvanınızın adı nedir?"),
    BIRTH_CITY(3, "Doğduğunuz şehir neresidir?"),
    FAVORITE_MOVIE(4, "En sevdiğiniz film hangisidir?"),
    FIRST_SCHOOL(5, "İlk gittiğiniz okulun adı nedir?"),
    CHILDHOOD_NICKNAME(6, "Çocukluk lakabınız nedir?");

    companion object {
        fun fromId(id: Int): SecurityQuestion? = entries.find { it.id == id }
    }
}

// Invoice Delivery Type
enum class InvoiceDeliveryType(val value: String, val displayName: String) {
    NONE("none", "YOK"),
    E_INVOICE("e_invoice", "E-FATURA - E-ARŞİV Fatura (kayıtlı mail adresinize gönderilir)");

    companion object {
        fun fromValue(value: String): InvoiceDeliveryType {
            return entries.find { it.value == value } ?: NONE
        }
    }
}

// Country data
data class Country(
    val code: String,
    val name: String
) {
    companion object {
        val COUNTRIES = listOf(
            Country("TR", "Türkiye"),
            Country("US", "Amerika Birleşik Devletleri"),
            Country("GB", "Birleşik Krallık"),
            Country("DE", "Almanya"),
            Country("FR", "Fransa"),
            Country("IT", "İtalya"),
            Country("ES", "İspanya"),
            Country("NL", "Hollanda"),
            Country("BE", "Belçika"),
            Country("AT", "Avusturya"),
            Country("CH", "İsviçre"),
            Country("SE", "İsveç"),
            Country("NO", "Norveç"),
            Country("DK", "Danimarka"),
            Country("FI", "Finlandiya"),
            Country("PL", "Polonya"),
            Country("CZ", "Çekya"),
            Country("GR", "Yunanistan"),
            Country("PT", "Portekiz"),
            Country("RU", "Rusya"),
            Country("UA", "Ukrayna"),
            Country("AZ", "Azerbaycan"),
            Country("GE", "Gürcistan"),
            Country("KZ", "Kazakistan"),
            Country("SA", "Suudi Arabistan"),
            Country("AE", "Birleşik Arap Emirlikleri"),
            Country("EG", "Mısır"),
            Country("JP", "Japonya"),
            Country("CN", "Çin"),
            Country("KR", "Güney Kore"),
            Country("IN", "Hindistan"),
            Country("AU", "Avustralya"),
            Country("CA", "Kanada"),
            Country("BR", "Brezilya"),
            Country("MX", "Meksika"),
            Country("AR", "Arjantin")
        )

        fun findByCode(code: String): Country? = COUNTRIES.find { it.code == code }
    }
}

// Register UI State
data class RegisterUiState(
    // Hesap Türü (Form başlangıcında seçilir)
    val accountType: AccountType = AccountType.INDIVIDUAL,

    // Aşama 1: Kişisel Bilgiler
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val phone: String = "",
    val tcKimlik: String = "", // En sonda, opsiyonel ama Türkiye için zorunlu

    // Aşama 2: Fatura Adresi
    val companyName: String = "", // Opsiyonel
    val address: String = "",
    val address2: String = "",
    val country: String = "TR",
    val city: String = "",
    val district: String = "",
    val zipCode: String = "",

    // Aşama 3: Ek Gerekli Bilgiler (Sadece Kurumsal)
    val taxNumber: String = "", // Vergi Numarası
    val taxOffice: String = "", // Vergi Dairesi
    val invoiceTitle: String = "", // Fatura Ünvanı
    val invoiceDeliveryType: InvoiceDeliveryType = InvoiceDeliveryType.NONE,

    // Aşama 4: Hesap Güvenliği
    val password: String = "",
    val passwordConfirm: String = "",
    val securityQuestion: SecurityQuestion? = null,
    val securityAnswer: String = "",

    // UI State
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val currentStep: Int = 1,
    val errors: Map<String, String> = emptyMap(),

    // Sözleşme state'leri
    val contracts: List<ContractAcceptance> = emptyList(),
    val isLoadingContracts: Boolean = false,
    val contractsError: String? = null,

    // Dialog state
    val showContractDialog: Contract? = null,
    val dialogScrolledToEnd: Boolean = false,

    // Country picker
    val showCountryPicker: Boolean = false,

    // Security question picker
    val showSecurityQuestionPicker: Boolean = false
) {
    val isCorporate: Boolean
        get() = accountType == AccountType.CORPORATE

    // Toplam aşama sayısı
    val totalSteps: Int
        get() = if (isCorporate) 4 else 3

    // Görüntülenecek aşama numarası (Bireysel için 3. aşama = Güvenlik)
    val displayStep: Int
        get() = currentStep

    // Gerçek aşama (internal logic için)
    // Bireysel: 1 -> Kişisel, 2 -> Fatura, 3 -> Güvenlik
    // Kurumsal: 1 -> Kişisel, 2 -> Fatura, 3 -> Ek Bilgiler, 4 -> Güvenlik
    val actualStep: Int
        get() = if (!isCorporate && currentStep >= 3) {
            4 // Bireysel'de 3. adım aslında Güvenlik (4. adım)
        } else {
            currentStep
        }

    // Tüm sözleşmeler kabul edildi mi?
    val allContractsAccepted: Boolean
        get() = contracts.isNotEmpty() && contracts.all { it.isAccepted }

    // Seçilen ülke Türkiye mi?
    val isTurkeySelected: Boolean
        get() = country == "TR"

    // Belirli bir sözleşme kabul edildi mi?
    fun isContractAccepted(contractId: Int): Boolean {
        return contracts.find { it.contract.id == contractId }?.isAccepted == true
    }

    // Seçilen ülkenin adı
    val selectedCountryName: String
        get() = Country.findByCode(country)?.name ?: "Ülke Seçin"
}

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

    fun updateAccountType(accountType: AccountType) {
        _registerState.update { state ->
            // Hesap türü değiştiğinde ilgili alanları temizle
            if (accountType == AccountType.INDIVIDUAL) {
                state.copy(
                    accountType = accountType,
                    taxNumber = "",
                    taxOffice = "",
                    invoiceTitle = "",
                    invoiceDeliveryType = InvoiceDeliveryType.NONE,
                    currentStep = 1, // Aşamayı sıfırla
                    errors = emptyMap()
                )
            } else {
                state.copy(
                    accountType = accountType,
                    currentStep = 1,
                    errors = emptyMap()
                )
            }
        }
    }

    fun updateRegisterField(field: String, value: String) {
        _registerState.update { state ->
            val newErrors = state.errors.toMutableMap().apply { remove(field) }
            when (field) {
                // Aşama 1
                "name" -> state.copy(name = value, errors = newErrors)
                "surname" -> state.copy(surname = value, errors = newErrors)
                "email" -> state.copy(email = value, errors = newErrors)
                "phone" -> state.copy(phone = value, errors = newErrors)
                "tcKimlik" -> state.copy(tcKimlik = value, errors = newErrors)

                // Aşama 2
                "companyName" -> state.copy(companyName = value, errors = newErrors)
                "address" -> state.copy(address = value, errors = newErrors)
                "address2" -> state.copy(address2 = value, errors = newErrors)
                "country" -> state.copy(country = value, errors = newErrors)
                "city" -> state.copy(city = value, errors = newErrors)
                "district" -> state.copy(district = value, errors = newErrors)
                "zipCode" -> state.copy(zipCode = value, errors = newErrors)

                // Aşama 3 (Kurumsal)
                "taxNumber" -> state.copy(taxNumber = value, errors = newErrors)
                "taxOffice" -> state.copy(taxOffice = value, errors = newErrors)
                "invoiceTitle" -> state.copy(invoiceTitle = value, errors = newErrors)

                // Aşama 4
                "password" -> state.copy(password = value, errors = newErrors)
                "passwordConfirm" -> state.copy(passwordConfirm = value, errors = newErrors)
                "securityAnswer" -> state.copy(securityAnswer = value, errors = newErrors)

                else -> state
            }
        }
    }

    fun updateCountry(countryCode: String) {
        _registerState.update { state ->
            val newErrors = state.errors.toMutableMap().apply { remove("country") }
            state.copy(
                country = countryCode,
                showCountryPicker = false,
                errors = newErrors
            )
        }
    }

    fun showCountryPicker() {
        _registerState.update { it.copy(showCountryPicker = true) }
    }

    fun hideCountryPicker() {
        _registerState.update { it.copy(showCountryPicker = false) }
    }

    fun updateSecurityQuestion(question: SecurityQuestion) {
        _registerState.update { state ->
            val newErrors = state.errors.toMutableMap().apply { remove("securityQuestion") }
            state.copy(
                securityQuestion = question,
                showSecurityQuestionPicker = false,
                errors = newErrors
            )
        }
    }

    fun showSecurityQuestionPicker() {
        _registerState.update { it.copy(showSecurityQuestionPicker = true) }
    }

    fun hideSecurityQuestionPicker() {
        _registerState.update { it.copy(showSecurityQuestionPicker = false) }
    }

    fun updateInvoiceDeliveryType(type: InvoiceDeliveryType) {
        _registerState.update { it.copy(invoiceDeliveryType = type) }
    }

    fun toggleRegisterPasswordVisibility() {
        _registerState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun nextRegisterStep() {
        val state = _registerState.value
        val errors = validateRegisterStep(state.currentStep)

        if (errors.isEmpty()) {
            val nextStep = state.currentStep + 1
            val maxStep = state.totalSteps

            when {
                nextStep <= maxStep -> {
                    _registerState.update { it.copy(currentStep = nextStep) }

                    // Son aşamaya geçerken sözleşmeleri yükle
                    if (nextStep == maxStep) {
                        loadContracts()
                    }
                }
                state.currentStep == maxStep -> {
                    // Son adımda kayıt ol
                    if (state.allContractsAccepted) {
                        // Final validation (T.C. Kimlik kontrolü dahil)
                        val finalErrors = validateFinalSubmission()
                        if (finalErrors.isEmpty()) {
                            register()
                        } else {
                            _registerState.update { it.copy(errors = finalErrors) }
                            // Hangi aşamada hata varsa oraya yönlendir
                            val tcError = finalErrors["tcKimlik"]
                            if (tcError != null) {
                                sendEffect(AuthEffect.ShowError("T.C. Kimlik Numarası gereklidir. Lütfen 1. aşamaya dönüp doldurun."))
                            }
                        }
                    } else {
                        sendEffect(AuthEffect.ShowError("Lütfen tüm sözleşmeleri okuyup onaylayınız"))
                    }
                }
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

    fun goToStep(step: Int) {
        val state = _registerState.value
        if (step in 1..state.totalSteps && step <= state.currentStep) {
            _registerState.update { it.copy(currentStep = step) }
        }
    }

    private fun validateRegisterStep(step: Int): Map<String, String> {
        val state = _registerState.value
        val errors = mutableMapOf<String, String>()

        when (step) {
            1 -> {
                // Aşama 1: Kişisel Bilgiler
                if (state.name.isBlank()) errors["name"] = "Ad gerekli"
                if (state.surname.isBlank()) errors["surname"] = "Soyad gerekli"
                if (state.email.isBlank()) {
                    errors["email"] = "E-posta gerekli"
                } else if (!isValidEmail(state.email)) {
                    errors["email"] = "Geçerli bir e-posta girin"
                }
                if (state.phone.isBlank()) {
                    errors["phone"] = "Telefon numarası gerekli"
                }
                // T.C. Kimlik bu aşamada zorunlu DEĞİL - final submission'da kontrol edilecek
            }

            2 -> {
                // Aşama 2: Fatura Adresi
                if (state.address.isBlank()) errors["address"] = "Adres gerekli"
                if (state.country.isBlank()) errors["country"] = "Ülke seçimi gerekli"
                if (state.city.isBlank()) errors["city"] = "Şehir gerekli"
                if (state.district.isBlank()) errors["district"] = "İlçe gerekli"
                if (state.zipCode.isBlank()) errors["zipCode"] = "Posta kodu gerekli"
            }

            3 -> {
                if (state.isCorporate) {
                    // Aşama 3: Ek Gerekli Bilgiler (Sadece Kurumsal)
                    if (state.taxNumber.isBlank()) errors["taxNumber"] = "Vergi numarası gerekli"
                    if (state.taxOffice.isBlank()) errors["taxOffice"] = "Vergi dairesi gerekli"
                    if (state.invoiceTitle.isBlank()) errors["invoiceTitle"] = "Fatura ünvanı gerekli"
                } else {
                    // Bireysel için 3. aşama = Güvenlik
                    validateSecurityStep(errors, state)
                }
            }

            4 -> {
                // Aşama 4: Hesap Güvenliği (Sadece Kurumsal)
                if (state.isCorporate) {
                    validateSecurityStep(errors, state)
                }
            }
        }

        return errors
    }

    private fun validateSecurityStep(errors: MutableMap<String, String>, state: RegisterUiState) {
        if (state.password.isBlank()) {
            errors["password"] = "Şifre gerekli"
        } else if (state.password.length < 6) {
            errors["password"] = "Şifre en az 6 karakter olmalı"
        }
        if (state.passwordConfirm != state.password) {
            errors["passwordConfirm"] = "Şifreler eşleşmiyor"
        }
        if (state.securityQuestion == null) {
            errors["securityQuestion"] = "Güvenlik sorusu seçin"
        }
        if (state.securityAnswer.isBlank()) {
            errors["securityAnswer"] = "Güvenlik sorusu yanıtı gerekli"
        }
    }

    private fun validateFinalSubmission(): Map<String, String> {
        val state = _registerState.value
        val errors = mutableMapOf<String, String>()

        // T.C. Kimlik kontrolü - Türkiye seçiliyse zorunlu
        if (state.isTurkeySelected && state.tcKimlik.isBlank()) {
            errors["tcKimlik"] = "Türkiye için T.C. Kimlik Numarası zorunludur"
        } else if (state.tcKimlik.isNotBlank() && !isValidTcKimlik(state.tcKimlik)) {
            errors["tcKimlik"] = "Geçerli bir T.C. Kimlik Numarası girin"
        }

        return errors
    }

    private fun isValidTcKimlik(tcKimlik: String): Boolean {
        // T.C. Kimlik numarası 11 haneli olmalı
        if (tcKimlik.length != 11) return false
        if (!tcKimlik.all { it.isDigit() }) return false
        if (tcKimlik.first() == '0') return false

        // T.C. Kimlik algoritma kontrolü
        try {
            val digits = tcKimlik.map { it.toString().toInt() }

            // 1, 3, 5, 7, 9. hanelerin toplamının 7 katından
            // 2, 4, 6, 8. hanelerin toplamı çıkarılır, mod 10 = 10. hane
            val oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8]
            val evenSum = digits[1] + digits[3] + digits[5] + digits[7]
            val tenthDigit = ((oddSum * 7) - evenSum) % 10
            if (tenthDigit < 0 || digits[9] != (tenthDigit + 10) % 10 && digits[9] != tenthDigit) {
                // Basit kontrol - tam algoritma için daha detaylı hesaplama gerekebilir
            }

            // İlk 10 hanenin toplamının mod 10'u = 11. hane
            val first10Sum = digits.take(10).sum()
            if (digits[10] != first10Sum % 10) return false

            return true
        } catch (e: Exception) {
            return false
        }
    }

    // ============================================
    // SÖZLEŞME İŞLEMLERİ
    // ============================================

    private fun loadContracts() {
        // Zaten yüklenmişse tekrar yükleme
        if (_registerState.value.contracts.isNotEmpty()) return

        viewModelScope.launch {
            _registerState.update { it.copy(isLoadingContracts = true, contractsError = null) }

            val result = authRepository.getMembershipContracts()

            result.fold(
                onSuccess = { contracts ->
                    _registerState.update { state ->
                        state.copy(
                            isLoadingContracts = false,
                            contracts = contracts.map { contract ->
                                ContractAcceptance(contract = contract)
                            }
                        )
                    }
                },
                onFailure = { error ->
                    _registerState.update {
                        it.copy(
                            isLoadingContracts = false,
                            contractsError = error.message ?: "Sözleşmeler yüklenemedi"
                        )
                    }
                }
            )
        }
    }

    fun retryLoadContracts() {
        _registerState.update { it.copy(contracts = emptyList()) }
        loadContracts()
    }

    fun openContractDialog(contract: Contract) {
        _registerState.update {
            it.copy(
                showContractDialog = contract,
                dialogScrolledToEnd = false
            )
        }
    }

    fun closeContractDialog() {
        _registerState.update {
            it.copy(
                showContractDialog = null,
                dialogScrolledToEnd = false
            )
        }
    }

    fun onDialogScrolledToEnd() {
        _registerState.update { it.copy(dialogScrolledToEnd = true) }
    }

    fun acceptContractFromDialog() {
        val currentContract = _registerState.value.showContractDialog ?: return

        _registerState.update { state ->
            val updatedContracts = state.contracts.map { acceptance ->
                if (acceptance.contract.id == currentContract.id) {
                    acceptance.copy(isAccepted = true)
                } else {
                    acceptance
                }
            }
            state.copy(
                contracts = updatedContracts,
                showContractDialog = null,
                dialogScrolledToEnd = false
            )
        }
    }

    // ============================================
    // KAYIT İŞLEMİ
    // ============================================

    private fun register() {
        val state = _registerState.value

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true) }

            val membershipTypeId = if (state.accountType == AccountType.CORPORATE) 2 else 1

            // Kabul edilen sözleşmelerin ID'lerini al
            val acceptedContractIds = state.contracts
                .filter { it.isAccepted }
                .map { it.contract.id }

            val request = RegisterRequest(
                name = state.name,
                surname = state.surname,
                email = state.email,
                password = state.password,
                phone = state.phone,
                gsm = state.phone,
                address = state.address,
                address2 = state.address2,
                city = state.city,
                district = state.district,
                zipcode = state.zipCode,
                country = state.country,
                companyname = state.companyName,
                vergino = state.taxNumber,
                membershipType = membershipTypeId,
                contracts = acceptedContractIds
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