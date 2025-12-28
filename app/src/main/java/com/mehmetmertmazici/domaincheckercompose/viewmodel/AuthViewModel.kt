package com.mehmetmertmazici.domaincheckercompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
import com.mehmetmertmazici.domaincheckercompose.model.AccountType
import com.mehmetmertmazici.domaincheckercompose.model.Contract
import com.mehmetmertmazici.domaincheckercompose.model.Country
import com.mehmetmertmazici.domaincheckercompose.model.District
import com.mehmetmertmazici.domaincheckercompose.model.Province
import com.mehmetmertmazici.domaincheckercompose.model.RegisterRequest
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ============================================
// SIDE EFFECTS
// ============================================

sealed interface AuthEffect {
    data class ShowError(val message: String) : AuthEffect
    data class ShowSuccess(val message: String) : AuthEffect
    data object NavigateToHome : AuthEffect
    data object NavigateToLogin : AuthEffect
}

// ============================================
// LOGIN UI STATE
// ============================================

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null
)

// ============================================
// FORGOT PASSWORD UI STATE
// ============================================

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val isEmailSent: Boolean = false
)

// ============================================
// CONTRACT ACCEPTANCE STATE
// ============================================

data class ContractAcceptance(
    val contract: Contract,
    val isAccepted: Boolean = false
)

// ============================================
// FATURA TESLİMATI SEÇENEKLERİ
// ============================================

enum class InvoiceDeliveryType(val value: String, val displayName: String) {
    NONE("", "Yok"),
    E_FATURA("e-fatura", "E-FATURA - E-ARŞİV Fatura");

    companion object {
        fun fromValue(value: String): InvoiceDeliveryType {
            return entries.find { it.value == value } ?: NONE
        }
    }
}

// ============================================
// REGISTER STEPS
// ============================================

/**
 * Kayıt Aşamaları:
 * - Bireysel: 1 -> 2 -> 4 (3 atlanır)
 * - Kurumsal: 1 -> 2 -> 3 -> 4
 */
enum class RegisterStep(val stepNumber: Int, val title: String) {
    PERSONAL_INFO(1, "Kişisel Bilgiler"),
    BILLING_ADDRESS(2, "Fatura Adresi"),
    ADDITIONAL_INFO(3, "Ek Gerekli Bilgiler"), // Sadece Kurumsal
    ACCOUNT_SECURITY(4, "Hesap Güvenliği");

    companion object {
        fun fromNumber(number: Int): RegisterStep {
            return entries.find { it.stepNumber == number } ?: PERSONAL_INFO
        }
    }
}

// ============================================
// REGISTER UI STATE
// ============================================

data class RegisterUiState(
    // === Hesap Türü ===
    val accountType: AccountType = AccountType.INDIVIDUAL,

    // === Aşama 1: Kişisel Bilgiler ===
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val phone: String = "",
    val citizen: String = "", // T.C. Kimlik Numarası

    // === Aşama 2: Fatura Adresi ===
    val companyName: String = "", // Firma Adı (Opsiyonel)
    val address: String = "",
    val address2: String = "", // Adres Devamı
    val zipCode: String = "",

    // Ülke (ISO2 kodu saklanır)
    val selectedCountryIso2: String = "TR",
    val selectedCountryName: String = "Turkey",

    // İl/Şehir - TR için dropdown, diğerleri için manuel
    val selectedProvinceId: String = "",
    val selectedProvinceName: String = "",
    val manualCity: String = "", // TR dışı için

    // İlçe - TR için dropdown, diğerleri için manuel
    val selectedDistrictId: String = "",
    val selectedDistrictName: String = "",
    val manualDistrict: String = "", // TR dışı için

    // === Aşama 3: Ek Bilgiler (Sadece Kurumsal) ===
    val taxNumber: String = "", // Vergi Numarası
    val taxOffice: String = "", // Vergi Dairesi
    val invoiceTitle: String = "", // Fatura Ünvanı
    val invoiceDeliveryType: InvoiceDeliveryType = InvoiceDeliveryType.NONE,

    // === Aşama 4: Hesap Güvenliği ===
    val password: String = "",
    val passwordConfirm: String = "",
    val securityQuestion: String = "",
    val securityAnswer: String = "",

    // === Sözleşmeler ===
    val contracts: List<ContractAcceptance> = emptyList(),
    val isLoadingContracts: Boolean = false,
    val contractsError: String? = null,
    val showContractDialog: Contract? = null,
    val dialogScrolledToEnd: Boolean = false,

    // === Lokasyon Verileri ===
    val countries: List<Country> = emptyList(),
    val provinces: List<Province> = emptyList(),
    val districts: List<District> = emptyList(),
    val filteredDistricts: List<District> = emptyList(),
    val isLoadingLocations: Boolean = false,
    val locationError: String? = null,

    // === Dropdown States ===
    val showCountryDropdown: Boolean = false,
    val showProvinceDropdown: Boolean = false,
    val showDistrictDropdown: Boolean = false,
    val countrySearchQuery: String = "",
    val provinceSearchQuery: String = "",
    val districtSearchQuery: String = "",

    // === UI State ===
    val currentStep: Int = 1,
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val errors: Map<String, String> = emptyMap()
) {
    // === Computed Properties ===

    val isCorporate: Boolean
        get() = accountType == AccountType.CORPORATE

    val isTurkey: Boolean
        get() = selectedCountryIso2 == "TR"

    /**
     * Toplam adım sayısı:
     * - Bireysel: 3 (1, 2, 4)
     * - Kurumsal: 4 (1, 2, 3, 4)
     */
    val totalSteps: Int
        get() = if (isCorporate) 4 else 3

    /**
     * Görüntülenecek adım numarası (progress bar için)
     */
    val displayStepNumber: Int
        get() = when {
            !isCorporate && currentStep == 4 -> 3 // Bireysel için 4. adım, 3 olarak gösterilir
            else -> currentStep
        }

    /**
     * Mevcut adımın başlığı
     */
    val currentStepTitle: String
        get() = RegisterStep.fromNumber(currentStep).title

    /**
     * API'ye gönderilecek şehir değeri
     */
    val cityValue: String
        get() = if (isTurkey) selectedProvinceName else manualCity

    /**
     * API'ye gönderilecek ilçe değeri
     */
    val districtValue: String
        get() = if (isTurkey) selectedDistrictName else manualDistrict

    /**
     * Tüm sözleşmeler kabul edildi mi?
     */
    val allContractsAccepted: Boolean
        get() = contracts.isNotEmpty() && contracts.all { it.isAccepted }

    /**
     * Belirli bir sözleşme kabul edildi mi?
     */
    fun isContractAccepted(contractId: Int): Boolean {
        return contracts.find { it.contract.id == contractId }?.isAccepted == true
    }

    /**
     * Filtrelenmiş ülke listesi (arama için)
     */
    val filteredCountries: List<Country>
        get() {
            if (countrySearchQuery.isBlank()) return countries
            return countries.filter { it.matchesSearch(countrySearchQuery) }
        }

    /**
     * Filtrelenmiş il listesi (arama için)
     */
    val filteredProvinces: List<Province>
        get() {
            if (provinceSearchQuery.isBlank()) return provinces
            return provinces.filter { it.matchesSearch(provinceSearchQuery) }
        }

    /**
     * Seçilen ile göre filtrelenmiş ve aranmış ilçeler
     */
    val searchedDistricts: List<District>
        get() {
            val baseList = filteredDistricts
            if (districtSearchQuery.isBlank()) return baseList
            return baseList.filter { it.matchesSearch(districtSearchQuery) }
        }
}

// ============================================
// AUTH VIEW MODEL
// ============================================

class AuthViewModel : ViewModel() {

    private val authRepository = ServiceLocator.authRepository
    private val locationRepository = ServiceLocator.locationRepository

    // === States ===
    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow(ForgotPasswordUiState())
    val forgotPasswordState: StateFlow<ForgotPasswordUiState> = _forgotPasswordState.asStateFlow()

    // === Effects ===
    private val _effect = Channel<AuthEffect>()
    val effect = _effect.receiveAsFlow()

    // ============================================
    // INITIALIZATION
    // ============================================

    init {
        loadLocationData()
    }

    private fun loadLocationData() {
        viewModelScope.launch {
            _registerState.update { it.copy(isLoadingLocations = true, locationError = null) }

            val result = locationRepository.loadAllLocationData()

            result.fold(
                onSuccess = { (countries, provinces, districts) ->
                    _registerState.update { state ->
                        state.copy(
                            isLoadingLocations = false,
                            countries = countries,
                            provinces = provinces,
                            districts = districts,
                            // Varsayılan olarak Türkiye seçili
                            selectedCountryIso2 = "TR",
                            selectedCountryName = countries.find { it.iso2 == "TR" }?.name ?: "Turkey"
                        )
                    }
                },
                onFailure = { error ->
                    _registerState.update {
                        it.copy(
                            isLoadingLocations = false,
                            locationError = error.message ?: "Lokasyon verileri yüklenemedi"
                        )
                    }
                }
            )
        }
    }

    fun retryLoadLocationData() {
        loadLocationData()
    }

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
    // REGISTER - HESAP TÜRÜ
    // ============================================

    fun updateAccountType(accountType: AccountType) {
        _registerState.update { state ->
            if (accountType == AccountType.INDIVIDUAL) {
                // Bireysel seçildiğinde kurumsal alanları temizle
                state.copy(
                    accountType = accountType,
                    taxNumber = "",
                    taxOffice = "",
                    invoiceTitle = "",
                    invoiceDeliveryType = InvoiceDeliveryType.NONE,
                    errors = state.errors.toMutableMap().apply {
                        remove("taxNumber")
                        remove("taxOffice")
                        remove("invoiceTitle")
                    }
                )
            } else {
                state.copy(accountType = accountType)
            }
        }
    }

    // ============================================
    // REGISTER - ALAN GÜNCELLEMELERİ
    // ============================================

    fun updateRegisterField(field: String, value: String) {
        _registerState.update { state ->
            val newErrors = state.errors.toMutableMap().apply { remove(field) }
            when (field) {
                // Aşama 1
                "name" -> state.copy(name = value, errors = newErrors)
                "surname" -> state.copy(surname = value, errors = newErrors)
                "email" -> state.copy(email = value, errors = newErrors)
                "phone" -> state.copy(phone = value, errors = newErrors)
                "citizen" -> state.copy(citizen = value.filter { it.isDigit() }.take(11), errors = newErrors)

                // Aşama 2
                "companyName" -> state.copy(companyName = value, errors = newErrors)
                "address" -> state.copy(address = value, errors = newErrors)
                "address2" -> state.copy(address2 = value, errors = newErrors)
                "zipCode" -> state.copy(zipCode = value, errors = newErrors)
                "manualCity" -> state.copy(manualCity = value, errors = newErrors)
                "manualDistrict" -> state.copy(manualDistrict = value, errors = newErrors)

                // Aşama 3 (Kurumsal)
                "taxNumber" -> state.copy(taxNumber = value, errors = newErrors)
                "taxOffice" -> state.copy(taxOffice = value, errors = newErrors)
                "invoiceTitle" -> state.copy(invoiceTitle = value, errors = newErrors)

                // Aşama 4
                "password" -> state.copy(password = value, errors = newErrors)
                "passwordConfirm" -> state.copy(passwordConfirm = value, errors = newErrors)
                "securityQuestion" -> state.copy(securityQuestion = value, errors = newErrors)
                "securityAnswer" -> state.copy(securityAnswer = value, errors = newErrors)

                else -> state
            }
        }
    }

    fun updateInvoiceDeliveryType(type: InvoiceDeliveryType) {
        _registerState.update { it.copy(invoiceDeliveryType = type) }
    }

    fun toggleRegisterPasswordVisibility() {
        _registerState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    // ============================================
    // REGISTER - ÜLKE SEÇİMİ
    // ============================================

    fun toggleCountryDropdown() {
        _registerState.update {
            it.copy(
                showCountryDropdown = !it.showCountryDropdown,
                showProvinceDropdown = false,
                showDistrictDropdown = false,
                countrySearchQuery = ""
            )
        }
    }

    fun updateCountrySearchQuery(query: String) {
        _registerState.update { it.copy(countrySearchQuery = query) }
    }

    fun selectCountry(country: Country) {
        _registerState.update { state ->
            val isTurkeyNow = country.iso2 == "TR"
            val wasTurkey = state.selectedCountryIso2 == "TR"

            // Ülke değiştiğinde ilgili alanları sıfırla
            state.copy(
                selectedCountryIso2 = country.iso2,
                selectedCountryName = country.name,
                showCountryDropdown = false,
                countrySearchQuery = "",
                // Ülke değiştiğinde şehir/ilçe sıfırla
                selectedProvinceId = if (isTurkeyNow) "" else state.selectedProvinceId,
                selectedProvinceName = if (isTurkeyNow) "" else state.selectedProvinceName,
                selectedDistrictId = "",
                selectedDistrictName = "",
                manualCity = if (!isTurkeyNow) "" else state.manualCity,
                manualDistrict = if (!isTurkeyNow) "" else state.manualDistrict,
                filteredDistricts = emptyList(),
                // Hataları temizle
                errors = state.errors.toMutableMap().apply {
                    remove("city")
                    remove("district")
                    remove("manualCity")
                    remove("manualDistrict")
                    // TR değilse TCKN hatasını temizle
                    if (!isTurkeyNow) remove("citizen")
                }
            )
        }
    }

    // ============================================
    // REGISTER - İL SEÇİMİ (Türkiye)
    // ============================================

    fun toggleProvinceDropdown() {
        _registerState.update {
            it.copy(
                showProvinceDropdown = !it.showProvinceDropdown,
                showCountryDropdown = false,
                showDistrictDropdown = false,
                provinceSearchQuery = ""
            )
        }
    }

    fun updateProvinceSearchQuery(query: String) {
        _registerState.update { it.copy(provinceSearchQuery = query) }
    }

    fun selectProvince(province: Province) {
        _registerState.update { state ->
            // İl değiştiğinde ilçeyi sıfırla ve ilçeleri filtrele
            val newFilteredDistricts = state.districts.filter { it.provinceId == province.id }

            state.copy(
                selectedProvinceId = province.id,
                selectedProvinceName = province.name,
                showProvinceDropdown = false,
                provinceSearchQuery = "",
                // İlçe sıfırla
                selectedDistrictId = "",
                selectedDistrictName = "",
                filteredDistricts = newFilteredDistricts,
                errors = state.errors.toMutableMap().apply {
                    remove("city")
                    remove("district")
                }
            )
        }
    }

    // ============================================
    // REGISTER - İLÇE SEÇİMİ (Türkiye)
    // ============================================

    fun toggleDistrictDropdown() {
        _registerState.update {
            it.copy(
                showDistrictDropdown = !it.showDistrictDropdown,
                showCountryDropdown = false,
                showProvinceDropdown = false,
                districtSearchQuery = ""
            )
        }
    }

    fun updateDistrictSearchQuery(query: String) {
        _registerState.update { it.copy(districtSearchQuery = query) }
    }

    fun selectDistrict(district: District) {
        _registerState.update { state ->
            state.copy(
                selectedDistrictId = district.id,
                selectedDistrictName = district.name,
                showDistrictDropdown = false,
                districtSearchQuery = "",
                errors = state.errors.toMutableMap().apply { remove("district") }
            )
        }
    }

    // ============================================
    // REGISTER - ADIM NAVİGASYONU
    // ============================================

    /**
     * Sonraki adıma geç
     */
    fun nextRegisterStep() {
        val state = _registerState.value
        val errors = validateCurrentStep(state)

        if (errors.isNotEmpty()) {
            _registerState.update { it.copy(errors = errors) }
            return
        }

        // Son adımda (4) kayıt işlemini başlat
        if (state.currentStep == 4) {
            register()
            return
        }

        // Sözleşmeleri yükle (3. veya 4. adıma geçmeden)
        if ((state.isCorporate && state.currentStep == 2) ||
            (!state.isCorporate && state.currentStep == 2)) {
            loadContracts()
        }

        // Sonraki adıma geç
        val nextStep = when {
            // Bireysel: 2'den 4'e atla
            !state.isCorporate && state.currentStep == 2 -> 4
            else -> state.currentStep + 1
        }

        _registerState.update { it.copy(currentStep = nextStep) }
    }

    /**
     * Önceki adıma dön
     */
    fun previousRegisterStep() {
        val state = _registerState.value

        val prevStep = when {
            // Bireysel: 4'ten 2'ye dön
            !state.isCorporate && state.currentStep == 4 -> 2
            else -> (state.currentStep - 1).coerceAtLeast(1)
        }

        _registerState.update { it.copy(currentStep = prevStep) }
    }

    /**
     * Mevcut adımın validasyonu
     */
    private fun validateCurrentStep(state: RegisterUiState): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        when (state.currentStep) {
            1 -> {
                // Aşama 1: Kişisel Bilgiler
                if (state.name.isBlank()) errors["name"] = "Ad gerekli"
                if (state.surname.isBlank()) errors["surname"] = "Soyad gerekli"
                if (state.email.isBlank()) {
                    errors["email"] = "E-posta gerekli"
                } else if (!isValidEmail(state.email)) {
                    errors["email"] = "Geçerli bir e-posta adresi girin"
                }
                if (state.phone.isBlank()) errors["phone"] = "Telefon gerekli"
                // NOT: TCKN validasyonu son adımda yapılacak
            }

            2 -> {
                // Aşama 2: Fatura Adresi
                if (state.address.isBlank()) errors["address"] = "Adres gerekli"
                if (state.zipCode.isBlank()) errors["zipCode"] = "Posta kodu gerekli"

                if (state.isTurkey) {
                    // Türkiye seçili - dropdown validasyonu
                    if (state.selectedProvinceId.isBlank()) errors["city"] = "İl seçimi gerekli"
                    if (state.selectedDistrictId.isBlank()) errors["district"] = "İlçe seçimi gerekli"
                } else {
                    // Yurt dışı - manuel giriş validasyonu
                    if (state.manualCity.isBlank()) errors["manualCity"] = "Şehir gerekli"
                    if (state.manualDistrict.isBlank()) errors["manualDistrict"] = "İlçe/Bölge gerekli"
                }
            }

            3 -> {
                // Aşama 3: Ek Bilgiler (Sadece Kurumsal)
                if (state.isCorporate) {
                    if (state.taxNumber.isBlank()) {
                        errors["taxNumber"] = "Vergi numarası gerekli"
                    } else if (state.taxNumber.length < 10) {
                        errors["taxNumber"] = "Geçerli bir vergi numarası girin"
                    }
                    if (state.taxOffice.isBlank()) errors["taxOffice"] = "Vergi dairesi gerekli"
                    if (state.invoiceTitle.isBlank()) errors["invoiceTitle"] = "Fatura ünvanı gerekli"
                }
            }

            4 -> {
                // Aşama 4: Hesap Güvenliği + Son Validasyon
                if (state.password.isBlank()) {
                    errors["password"] = "Şifre gerekli"
                } else if (state.password.length < 6) {
                    errors["password"] = "Şifre en az 6 karakter olmalı"
                }

                if (state.passwordConfirm.isBlank()) {
                    errors["passwordConfirm"] = "Şifre tekrarı gerekli"
                } else if (state.password != state.passwordConfirm) {
                    errors["passwordConfirm"] = "Şifreler eşleşmiyor"
                }

                // Sözleşme kontrolü
                if (!state.allContractsAccepted) {
                    errors["contracts"] = "Tüm sözleşmeleri kabul etmelisiniz"
                }

                // KRİTİK: TCKN Validasyonu (Son Aşama)
                if (state.isTurkey) {
                    // Türkiye seçili ise TCKN zorunlu
                    if (state.citizen.isBlank()) {
                        errors["citizen"] = "T.C. Kimlik Numarası gerekli"
                    } else if (state.citizen.length != 11) {
                        errors["citizen"] = "T.C. Kimlik Numarası 11 haneli olmalı"
                    } else if (!isValidTCKN(state.citizen)) {
                        errors["citizen"] = "Geçersiz T.C. Kimlik Numarası"
                    }
                }
                // Yurt dışı seçili ise TCKN opsiyonel - validasyon yok
            }
        }

        return errors
    }

    // ============================================
    // REGISTER - SÖZLEŞMELER
    // ============================================

    private fun loadContracts() {
        if (_registerState.value.contracts.isNotEmpty()) return

        viewModelScope.launch {
            _registerState.update { it.copy(isLoadingContracts = true, contractsError = null) }

            val result = authRepository.getMembershipContracts()

            result.fold(
                onSuccess = { contracts ->
                    _registerState.update { state ->
                        state.copy(
                            isLoadingContracts = false,
                            contracts = contracts.map { ContractAcceptance(contract = it) }
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
            it.copy(showContractDialog = contract, dialogScrolledToEnd = false)
        }
    }

    fun closeContractDialog() {
        _registerState.update {
            it.copy(showContractDialog = null, dialogScrolledToEnd = false)
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
                dialogScrolledToEnd = false,
                errors = state.errors.toMutableMap().apply { remove("contracts") }
            )
        }
    }

    // ============================================
    // REGISTER - KAYIT İŞLEMİ
    // ============================================

    private fun register() {
        val state = _registerState.value

        viewModelScope.launch {
            _registerState.update { it.copy(isLoading = true) }

            val membershipTypeId = if (state.isCorporate) 2 else 1
            val acceptedContractIds = state.contracts
                .filter { it.isAccepted }
                .map { it.contract.id }

            val request = RegisterRequest(
                name = state.name,
                surname = state.surname,
                citizen = if (state.isTurkey) state.citizen else "",
                email = state.email,
                password = state.password,
                phone = state.phone,
                gsm = state.phone,
                address = state.address,
                address2 = state.address2,
                city = state.cityValue,
                district = state.districtValue,
                zipcode = state.zipCode,
                country = state.selectedCountryIso2,
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
        loadLocationData()
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

    /**
     * T.C. Kimlik Numarası doğrulama algoritması
     */
    private fun isValidTCKN(tckn: String): Boolean {
        if (tckn.length != 11) return false
        if (tckn[0] == '0') return false
        if (!tckn.all { it.isDigit() }) return false

        val digits = tckn.map { it.toString().toInt() }

        // 10. hane kontrolü
        val oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8]
        val evenSum = digits[1] + digits[3] + digits[5] + digits[7]
        val tenthDigit = ((oddSum * 7) - evenSum) % 10
        if (tenthDigit < 0 || digits[9] != (tenthDigit + 10) % 10 && digits[9] != tenthDigit) {
            // Alternatif hesaplama
            val altTenth = ((oddSum * 7) - evenSum).mod(10)
            if (digits[9] != altTenth) return false
        }

        // 11. hane kontrolü
        val first10Sum = digits.take(10).sum()
        if (digits[10] != first10Sum % 10) return false

        return true
    }

    private fun sendEffect(effect: AuthEffect) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }
}