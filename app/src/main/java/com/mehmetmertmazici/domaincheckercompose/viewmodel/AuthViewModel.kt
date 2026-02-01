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
    data object NavigateToVerification : AuthEffect
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
// MAIL VERIFICATION UI STATE
// ============================================

data class MailVerificationUiState(
    val clientId: Int = 0,
    val email: String = "",
    val verificationCode: String = "",
    val isLoading: Boolean = false,
    val codeError: String? = null,
    val remainingTime: Int = 120, // 2 dakika countdown
    val canResend: Boolean = false,
    val isFromLogin: Boolean = false, // Login'den gelme durumu - kodu tekrar gönder seçeneğini gizlemek için
    val hasFailedAttempt: Boolean = false
)

// ============================================
// CONTRACT ACCEPTANCE STATE
// ============================================

data class ContractAcceptance(
    val contract: Contract,
    val isAccepted: Boolean = false
)

// ============================================
// REGISTER UI STATE
// ============================================

data class RegisterUiState(
    // === Hesap Türü ===
    val accountType: AccountType = AccountType.INDIVIDUAL,

    // === Kişisel Bilgiler ===
    val name: String = "",
    val surname: String = "",
    val email: String = "",
    val phone: String = "",
    val citizen: String = "", // T.C. Kimlik Numarası (Opsiyonel)

    // === Adres Bilgileri ===
    val companyName: String = "", // Firma Adı (Opsiyonel)
    val address: String = "",
    val address2: String = "", // Adres Devamı (Opsiyonel)
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

    // === Kurumsal Bilgiler (Sadece Kurumsal) ===
    val taxNumber: String = "", // Vergi Numarası
    val taxOffice: String = "", // Vergi Dairesi

    // === Hesap Güvenliği ===
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

    private val _mailVerificationState = MutableStateFlow(MailVerificationUiState())
    val mailVerificationState: StateFlow<MailVerificationUiState> = _mailVerificationState.asStateFlow()

    // === Effects ===
    private val _effect = Channel<AuthEffect>()
    val effect = _effect.receiveAsFlow()

    // ============================================
    // INITIALIZATION
    // ============================================

    init {
        loadLocationData()
        // Sözleşmeleri en başta yüklemeye çalışalım
        loadContracts()
        
        // Session'ı restore et (Uygulama yeniden başladığında token'ı geri yükle)
        viewModelScope.launch {
            authRepository.restoreSession()
        }
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
                onSuccess = { response ->
                    _loginState.update { it.copy(isLoading = false) }

                    // DOĞRU YER: Sunucudan cevap geldi
                    // Not: Doğrulama gerekiyorsa token henüz session'a kaydedilmemiş olabilir (Repository mantığınıza göre)
                    // Ancak response içindeki token'ı burada görebilirsiniz.
                    android.util.Log.d("TOKEN_DEBUG", "Gelen Token (Login): ${response.token}")
                    android.util.Log.d("TOKEN_DEBUG", "Session Token: ${com.mehmetmertmazici.domaincheckercompose.network.SessionHolder.token}")

                    // Mail doğrulaması gerekiyor mu kontrol et
                    if (response.requiresMailVerification) {
                        // Mail verification state'i güncelle
                        _mailVerificationState.update {
                            it.copy(
                                clientId = response.userId ?: 0,
                                email = state.email,
                                isFromLogin = true, // Login'den geldiğini işaretle
                                verificationCode = "",
                                remainingTime = 120,
                                canResend = false
                            )
                        }
                        sendEffect(AuthEffect.ShowSuccess("Doğrulama kodu e-posta adresinize gönderildi"))
                        sendEffect(AuthEffect.NavigateToVerification)
                    } else {
                        // Doğrulama gerekmiyorsa direkt ana sayfaya git
                        sendEffect(AuthEffect.ShowSuccess("Giriş başarılı"))
                        sendEffect(AuthEffect.NavigateToHome)
                    }
                },
                onFailure = { error ->
                    _loginState.update { it.copy(isLoading = false) }
                    android.util.Log.e("TOKEN_DEBUG", "Login Hatası: ${error.message}")
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
                    errors = state.errors.toMutableMap().apply {
                        remove("taxNumber")
                        remove("taxOffice")
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
                "name" -> state.copy(name = value, errors = newErrors)
                "surname" -> state.copy(surname = value, errors = newErrors)
                "email" -> state.copy(email = value, errors = newErrors)
                "phone" -> state.copy(phone = value, errors = newErrors)
                "citizen" -> state.copy(citizen = value.filter { it.isDigit() }.take(11), errors = newErrors)

                "companyName" -> state.copy(companyName = value, errors = newErrors)
                "address" -> state.copy(address = value, errors = newErrors)
                "address2" -> state.copy(address2 = value, errors = newErrors)
                "zipCode" -> state.copy(zipCode = value, errors = newErrors)
                "manualCity" -> state.copy(manualCity = value, errors = newErrors)
                "manualDistrict" -> state.copy(manualDistrict = value, errors = newErrors)

                "taxNumber" -> state.copy(taxNumber = value, errors = newErrors)
                "taxOffice" -> state.copy(taxOffice = value, errors = newErrors)

                "password" -> state.copy(password = value, errors = newErrors)
                "passwordConfirm" -> state.copy(passwordConfirm = value, errors = newErrors)
                "securityQuestion" -> state.copy(securityQuestion = value, errors = newErrors)
                "securityAnswer" -> state.copy(securityAnswer = value, errors = newErrors)

                else -> state
            }
        }
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
                    if (!isTurkeyNow) remove("citizen") // TR değilse TCKN hatasını sil
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
            val newFilteredDistricts = state.districts.filter { it.provinceId == province.id }
            state.copy(
                selectedProvinceId = province.id,
                selectedProvinceName = province.name,
                showProvinceDropdown = false,
                provinceSearchQuery = "",
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
    // REGISTER - VALİDASYON VE KAYIT
    // ============================================

    fun onRegisterClick() {
        if (validateRegisterForm()) {
            register()
        } else {
            sendEffect(AuthEffect.ShowError("Lütfen zorunlu alanları doldurun ve hataları düzeltin."))
        }
    }

    private fun validateRegisterForm(): Boolean {
        val state = _registerState.value
        val errors = mutableMapOf<String, String>()

        // 1. Kişisel Bilgiler & İletişim
        if (state.name.isBlank()) errors["name"] = "Ad gerekli"
        if (state.surname.isBlank()) errors["surname"] = "Soyad gerekli"
        if (state.email.isBlank()) {
            errors["email"] = "E-posta gerekli"
        } else if (!isValidEmail(state.email)) {
            errors["email"] = "Geçerli bir e-posta adresi girin"
        }
        if (state.phone.isBlank()) errors["phone"] = "Telefon gerekli"

        // 2. Güvenlik (Şifre)
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

        // 3. Adres Bilgileri
        if (state.address.isBlank()) errors["address"] = "Adres gerekli"
        if (state.zipCode.isBlank()) errors["zipCode"] = "Posta kodu gerekli"

        // Lokasyon Validasyonu
        if (state.isTurkey) {
            if (state.selectedProvinceId.isBlank()) errors["city"] = "İl seçimi gerekli"
            if (state.selectedDistrictId.isBlank()) errors["district"] = "İlçe seçimi gerekli"
        } else {
            if (state.manualCity.isBlank()) errors["manualCity"] = "Şehir gerekli"
            if (state.manualDistrict.isBlank()) errors["manualDistrict"] = "İlçe/Bölge gerekli"
        }

        // 4. T.C. Kimlik (Sadece TR ve Doluysa Kontrol Et - Opsiyonel)
        if (state.isTurkey && state.citizen.isNotBlank()) {
            if (state.citizen.length != 11) {
                errors["citizen"] = "T.C. Kimlik Numarası 11 haneli olmalı"
            } else if (!isValidTCKN(state.citizen)) {
                errors["citizen"] = "Geçersiz T.C. Kimlik Numarası"
            }
        }
        // Yurt dışı seçiliyse citizen alanı zaten UI'da gizli ve boş gider, validasyona takılmaz.

        // 5. Kurumsal Kontrolleri
        if (state.isCorporate) {
            if (state.taxNumber.isBlank()) errors["taxNumber"] = "Vergi numarası gerekli"
            if (state.taxOffice.isBlank()) errors["taxOffice"] = "Vergi dairesi gerekli"
        }

        // 6. Sözleşmeler
        if (!state.allContractsAccepted) {
            errors["contracts"] = "Kayıt olmak için sözleşmeleri kabul etmelisiniz"
        }

        _registerState.update { it.copy(errors = errors) }
        return errors.isEmpty()
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
    // REGISTER - API ÇAĞRISI
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
                address = state.address,
                address2 = state.address2,
                city = state.cityValue,
                district = state.districtValue,
                zipcode = state.zipCode,
                country = state.selectedCountryIso2,
                companyname = state.companyName,
                vergino = state.taxNumber,
                vergidairesi = state.taxOffice,
                membershipType = membershipTypeId,
                contracts = acceptedContractIds
            )

            val result = authRepository.register(request)

            result.fold(
                onSuccess = { response ->
                    _registerState.update { it.copy(isLoading = false) }

                    if (response.requiresMailVerification) {
                        _mailVerificationState.update {
                            MailVerificationUiState(
                                clientId = response.clientId ?: 0,
                                email = response.verificationEmail ?: state.email,
                                remainingTime = 120,
                                canResend = false
                            )
                        }
                        sendEffect(AuthEffect.ShowSuccess(response.data?.message ?: "Kayıt başarılı! Doğrulama kodu gönderildi."))
                        sendEffect(AuthEffect.NavigateToVerification)
                    } else {
                        sendEffect(AuthEffect.ShowSuccess("Kayıt başarılı"))
                        sendEffect(AuthEffect.NavigateToHome)
                    }
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
        loadContracts()
    }

    // ============================================
    // MAIL VERIFICATION İŞLEMLERİ
    // ============================================

    fun updateVerificationCode(code: String) {
        val filteredCode = code.filter { it.isDigit() }.take(6)
        _mailVerificationState.update {
            it.copy(verificationCode = filteredCode, codeError = null)
        }
    }

    fun verifyMailCode() {
        val state = _mailVerificationState.value

        if (state.verificationCode.isBlank()) {
            _mailVerificationState.update { it.copy(codeError = "Doğrulama kodu gerekli") }
            return
        }

        if (state.verificationCode.length < 6) {
            _mailVerificationState.update { it.copy(codeError = "Doğrulama kodu 6 haneli olmalı") }
            return
        }

        viewModelScope.launch {
            _mailVerificationState.update { it.copy(isLoading = true, codeError = null) }

            val result = authRepository.verifyMailCode(
                clientId = state.clientId,
                email = state.email,
                code = state.verificationCode
            )

            result.fold(
                onSuccess = { response ->
                    _mailVerificationState.update { it.copy(isLoading = false) }

                    ServiceLocator.sessionManager.createSessionAfterVerification(
                        userId = response.userId ?: 0,
                        email = state.email,
                        name = response.userName ?: "",
                        surname = response.userSurname ?: "",
                        token = response.token
                    )

                    // İŞTE ASIL BURADA TOKEN GÖZÜKMELİ
                    android.util.Log.d("TOKEN_DEBUG", "--- DOĞRULAMA BAŞARILI ---")
                    android.util.Log.d("TOKEN_DEBUG", "Kaydedilen Token: ${com.mehmetmertmazici.domaincheckercompose.network.SessionHolder.token}")

                    sendEffect(AuthEffect.ShowSuccess("Doğrulama başarılı! Hoş geldiniz."))
                    sendEffect(AuthEffect.NavigateToHome)
                },
                onFailure = { error ->
                    _mailVerificationState.update {
                        it.copy(
                            isLoading = false,
                            codeError = error.message ?: "Doğrulama başarısız",
                            hasFailedAttempt = true
                        )
                    }
                    sendEffect(AuthEffect.ShowError(error.message ?: "Doğrulama başarısız"))
                }
            )
        }
    }

    fun resendVerificationCode() {
        val state = _mailVerificationState.value
        val registerState = _registerState.value

        if (!state.canResend) return

        viewModelScope.launch {
            _mailVerificationState.update { it.copy(isLoading = true) }

            val membershipTypeId = if (registerState.isCorporate) 2 else 1
            val acceptedContractIds = registerState.contracts
                .filter { it.isAccepted }
                .map { it.contract.id }

            val request = RegisterRequest(
                name = registerState.name,
                surname = registerState.surname,
                citizen = if (registerState.isTurkey) registerState.citizen else "",
                email = registerState.email,
                password = registerState.password,
                phone = registerState.phone,
                address = registerState.address,
                address2 = registerState.address2,
                city = registerState.cityValue,
                district = registerState.districtValue,
                zipcode = registerState.zipCode,
                country = registerState.selectedCountryIso2,
                companyname = registerState.companyName,
                vergino = registerState.taxNumber,
                vergidairesi = registerState.taxOffice,
                membershipType = membershipTypeId,
                contracts = acceptedContractIds
            )

            val result = authRepository.register(request)

            result.fold(
                onSuccess = { response ->
                    _mailVerificationState.update {
                        it.copy(
                            isLoading = false,
                            clientId = response.clientId ?: state.clientId,
                            remainingTime = 120,
                            canResend = false,
                            verificationCode = ""
                        )
                    }
                    sendEffect(AuthEffect.ShowSuccess("Yeni doğrulama kodu gönderildi"))
                },
                onFailure = { error ->
                    _mailVerificationState.update { it.copy(isLoading = false) }
                    sendEffect(AuthEffect.ShowError(error.message ?: "Kod gönderilemedi"))
                }
            )
        }
    }

    fun startVerificationCountdown() {
        viewModelScope.launch {
            for (i in 120 downTo 0) {
                _mailVerificationState.update {
                    it.copy(
                        remainingTime = i,
                        canResend = i == 0
                    )
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun clearMailVerificationState() {
        _mailVerificationState.value = MailVerificationUiState()
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

    private fun isValidTCKN(tckn: String): Boolean {
        if (tckn.length != 11) return false
        if (tckn[0] == '0') return false
        if (!tckn.all { it.isDigit() }) return false

        val digits = tckn.map { it.toString().toInt() }

        val oddSum = digits[0] + digits[2] + digits[4] + digits[6] + digits[8]
        val evenSum = digits[1] + digits[3] + digits[5] + digits[7]
        val tenthDigit = ((oddSum * 7) - evenSum) % 10
        if (tenthDigit < 0 || digits[9] != (tenthDigit + 10) % 10 && digits[9] != tenthDigit) {
            val altTenth = ((oddSum * 7) - evenSum).mod(10)
            if (digits[9] != altTenth) return false
        }

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