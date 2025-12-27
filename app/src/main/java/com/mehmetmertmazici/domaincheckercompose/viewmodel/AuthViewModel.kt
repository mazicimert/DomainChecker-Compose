package com.mehmetmertmazici.domaincheckercompose.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
import com.mehmetmertmazici.domaincheckercompose.model.AccountType
import com.mehmetmertmazici.domaincheckercompose.model.City
import com.mehmetmertmazici.domaincheckercompose.model.Contract
import com.mehmetmertmazici.domaincheckercompose.model.Country
import com.mehmetmertmazici.domaincheckercompose.model.RegisterRequest
import com.mehmetmertmazici.domaincheckercompose.model.State
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

// Register UI State
data class RegisterUiState(
    val accountType: AccountType = AccountType.INDIVIDUAL,
    val name: String = "",
    val surname: String = "",
    val citizen: String = "",
    val email: String = "",
    val password: String = "",
    val passwordConfirm: String = "",
    val phone: String = "",
    val gsm: String = "",
    val address: String = "",
    val address2: String = "",
    val city: String = "",
    val district: String = "",
    val zipCode: String = "",
    val country: String = "TR",
    val companyName: String = "",
    val taxNumber: String = "",
    val isLoading: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val currentStep: Int = 1, // 1: Kişisel, 2: İletişim, 3: Adres + Sözleşmeler
    val errors: Map<String, String> = emptyMap(),

    // Location seçimleri için yeni alanlar
    val countries: List<Country> = emptyList(),
    val states: List<State> = emptyList(),
    val cities: List<City> = emptyList(),
    val selectedCountry: Country? = null,
    val selectedState: State? = null,
    val selectedCity: City? = null,
    val isLoadingCountries: Boolean = false,
    val isLoadingStates: Boolean = false,
    val isLoadingCities: Boolean = false,

    // Dropdown açık/kapalı durumları
    val isCountryDropdownExpanded: Boolean = false,
    val isStateDropdownExpanded: Boolean = false,
    val isCityDropdownExpanded: Boolean = false,

    // Sözleşme state'leri
    val contracts: List<ContractAcceptance> = emptyList(),
    val isLoadingContracts: Boolean = false,
    val contractsError: String? = null,

    // Dialog state
    val showContractDialog: Contract? = null,
    val dialogScrolledToEnd: Boolean = false
) {
    val isCorporate: Boolean
        get() = accountType == AccountType.CORPORATE

    // Tüm sözleşmeler kabul edildi mi?
    val allContractsAccepted: Boolean
        get() = contracts.isNotEmpty() && contracts.all { it.isAccepted }

    // Belirli bir sözleşme kabul edildi mi?
    fun isContractAccepted(contractId: Int): Boolean {
        return contracts.find { it.contract.id == contractId }?.isAccepted == true
    }
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
    private val locationRepository = ServiceLocator.locationRepository


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
    // LOCATION İŞLEMLERİ
    // ============================================

    /**
     * Ülkeleri yükle
     */
    fun loadCountries() {
        // Zaten yüklenmişse tekrar yükleme
        if (_registerState.value.countries.isNotEmpty()) return

        viewModelScope.launch {
            _registerState.update { it.copy(isLoadingCountries = true) }

            val result = locationRepository.getCountries()

            result.fold(
                onSuccess = { countries ->
                    _registerState.update { state ->
                        // Varsayılan olarak Türkiye'yi seç
                        val defaultCountry = countries.find { it.iso2 == "TR" }
                        state.copy(
                            isLoadingCountries = false,
                            countries = countries,
                            selectedCountry = defaultCountry
                        )
                    }
                    // Türkiye seçiliyse illeri yükle
                    _registerState.value.selectedCountry?.let {
                        loadStates(it.id)
                    }
                },
                onFailure = {
                    _registerState.update { it.copy(isLoadingCountries = false) }
                    sendEffect(AuthEffect.ShowError("Ülkeler yüklenemedi"))
                }
            )
        }
    }

    /**
     * Seçilen ülkenin illerini/eyaletlerini yükle
     */
    fun loadStates(countryId: Int) {
        viewModelScope.launch {
            _registerState.update {
                it.copy(
                    isLoadingStates = true,
                    states = emptyList(),
                    cities = emptyList(),
                    selectedState = null,
                    selectedCity = null
                )
            }

            val result = locationRepository.getStatesByCountry(countryId)

            result.fold(
                onSuccess = { states ->
                    _registerState.update { it.copy(isLoadingStates = false, states = states) }
                },
                onFailure = {
                    _registerState.update { it.copy(isLoadingStates = false) }
                }
            )
        }
    }

    /**
     * Seçilen ilin ilçelerini/şehirlerini yükle
     */
    fun loadCities(stateId: Int) {
        viewModelScope.launch {
            _registerState.update {
                it.copy(
                    isLoadingCities = true,
                    cities = emptyList(),
                    selectedCity = null
                )
            }

            val result = locationRepository.getCitiesByState(stateId)

            result.fold(
                onSuccess = { cities ->
                    _registerState.update { it.copy(isLoadingCities = false, cities = cities) }
                },
                onFailure = {
                    _registerState.update { it.copy(isLoadingCities = false) }
                }
            )
        }
    }

    /**
     * Ülke seçimi
     */
    fun selectCountry(country: Country) {
        _registerState.update { state ->
            state.copy(
                selectedCountry = country,
                country = country.iso2,
                isCountryDropdownExpanded = false,
                // İl ve ilçe seçimlerini sıfırla
                selectedState = null,
                selectedCity = null,
                states = emptyList(),
                cities = emptyList(),
                city = "",
                district = "",
                errors = state.errors.toMutableMap().apply {
                    remove("country")
                    remove("city")
                    remove("district")
                }
            )
        }
        loadStates(country.id)
    }

    /**
     * İl/Eyalet seçimi
     */
    fun selectState(state: State) {
        _registerState.update { currentState ->
            currentState.copy(
                selectedState = state,
                city = state.name, // city alanına il adını yaz
                isStateDropdownExpanded = false,
                // İlçe seçimini sıfırla
                selectedCity = null,
                cities = emptyList(),
                district = "",
                errors = currentState.errors.toMutableMap().apply {
                    remove("city")
                    remove("district")
                }
            )
        }
        loadCities(state.id)
    }

    /**
     * İlçe/Şehir seçimi
     */
    fun selectCity(city: City) {
        _registerState.update { state ->
            state.copy(
                selectedCity = city,
                district = city.name, // district alanına ilçe adını yaz
                isCityDropdownExpanded = false,
                errors = state.errors.toMutableMap().apply { remove("district") }
            )
        }
    }

    /**
     * Dropdown durumlarını değiştir
     */
    fun toggleCountryDropdown() {
        _registerState.update { it.copy(isCountryDropdownExpanded = !it.isCountryDropdownExpanded) }
    }

    fun toggleStateDropdown() {
        _registerState.update { it.copy(isStateDropdownExpanded = !it.isStateDropdownExpanded) }
    }

    fun toggleCityDropdown() {
        _registerState.update { it.copy(isCityDropdownExpanded = !it.isCityDropdownExpanded) }
    }

    fun closeAllDropdowns() {
        _registerState.update {
            it.copy(
                isCountryDropdownExpanded = false,
                isStateDropdownExpanded = false,
                isCityDropdownExpanded = false
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
            if (accountType == AccountType.INDIVIDUAL) {
                state.copy(
                    accountType = accountType,
                    companyName = "",
                    taxNumber = "",
                    errors = state.errors.toMutableMap().apply {
                        remove("companyName")
                        remove("taxNumber")
                    }
                )
            } else {
                state.copy(accountType = accountType)
            }
        }
    }

    fun updateRegisterField(field: String, value: String) {
        _registerState.update { state ->
            val newErrors = state.errors.toMutableMap().apply { remove(field) }
            when (field) {
                "name" -> state.copy(name = value, errors = newErrors)
                "surname" -> state.copy(surname = value, errors = newErrors)
                "citizen" -> state.copy(citizen = value, errors = newErrors)
                "email" -> state.copy(email = value, errors = newErrors)
                "password" -> state.copy(password = value, errors = newErrors)
                "passwordConfirm" -> state.copy(passwordConfirm = value, errors = newErrors)
                "phone" -> state.copy(phone = value, errors = newErrors)
                "gsm" -> state.copy(gsm = value, errors = newErrors)
                "address" -> state.copy(address = value, errors = newErrors)
                "address2" -> state.copy(address2 = value, errors = newErrors)
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
            when {
                state.currentStep < 3 -> {
                    _registerState.update { it.copy(currentStep = state.currentStep + 1) }
                    // 3. adıma geçerken sözleşmeleri yükle
                    if (state.currentStep == 2) {
                        loadContracts()
                    }
                }
                state.currentStep == 3 -> {
                    // Son adımda kayıt ol
                    if (state.allContractsAccepted) {
                        register()
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

    private fun validateRegisterStep(step: Int): Map<String, String> {
        val state = _registerState.value
        val errors = mutableMapOf<String, String>()

        when (step) {
            1 -> {
                if (state.name.isBlank()) errors["name"] = "Ad gerekli"
                if (state.surname.isBlank()) errors["surname"] = "Soyad gerekli"
                // Sadece ülke TR ise ve hesap bireysel ise zorunlu olsun (Tercihen kurumsalda da olabilir ama genelde şahısta istenir)
                if (state.country == "TR") {
                    if (state.citizen.isBlank()) {
                        errors["citizen"] = "T.C. Kimlik No gerekli"
                    } else if (state.citizen.length != 11) {
                        errors["citizen"] = "T.C. Kimlik No 11 haneli olmalıdır"
                    }
                }

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

                // Kurumsal hesap için ek validasyon
                if (state.isCorporate) {
                    if (state.companyName.isBlank()) {
                        errors["companyName"] = "Firma adı gerekli"
                    }
                    if (state.taxNumber.isBlank()) {
                        errors["taxNumber"] = "Vergi numarası gerekli"
                    } else if (state.taxNumber.length < 10) {
                        errors["taxNumber"] = "Geçerli bir vergi numarası girin"
                    }
                }
            }
            2 -> {
                if (state.phone.isBlank()) errors["phone"] = "Telefon gerekli"
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

    /**
     * Sözleşme checkbox'ına tıklandığında dialog aç
     */
    fun openContractDialog(contract: Contract) {
        _registerState.update {
            it.copy(
                showContractDialog = contract,
                dialogScrolledToEnd = false
            )
        }
    }

    /**
     * Dialog'u kapat
     */
    fun closeContractDialog() {
        _registerState.update {
            it.copy(
                showContractDialog = null,
                dialogScrolledToEnd = false
            )
        }
    }

    /**
     * Dialog'da scroll sonuna ulaşıldı
     */
    fun onDialogScrolledToEnd() {
        _registerState.update { it.copy(dialogScrolledToEnd = true) }
    }

    /**
     * Dialog'daki "Kabul Ediyorum" butonuna basıldı
     */
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
                citizen = if (state.country == "TR") state.citizen else "",
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




