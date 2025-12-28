package com.mehmetmertmazici.domaincheckercompose.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mehmetmertmazici.domaincheckercompose.model.AccountType
import com.mehmetmertmazici.domaincheckercompose.model.Contract
import com.mehmetmertmazici.domaincheckercompose.model.Country
import com.mehmetmertmazici.domaincheckercompose.model.District
import com.mehmetmertmazici.domaincheckercompose.model.Province
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthEffect
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.InvoiceDeliveryType
import com.mehmetmertmazici.domaincheckercompose.viewmodel.RegisterUiState
import kotlinx.coroutines.flow.collectLatest

// ============================================
// MAIN REGISTER SCREEN
// ============================================

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToVerification: () -> Unit,
    onBackClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val uiState by viewModel.registerState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Effects
    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AuthEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is AuthEffect.ShowSuccess -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is AuthEffect.NavigateToHome -> {
                    onNavigateToHome()
                }
                is AuthEffect.NavigateToVerification -> {
                    onNavigateToVerification()
                }
                else -> {}
            }
        }
    }

    // Contract Dialog
    uiState.showContractDialog?.let { contract ->
        ContractDialog(
            contract = contract,
            isScrolledToEnd = uiState.dialogScrolledToEnd,
            onScrolledToEnd = { viewModel.onDialogScrolledToEnd() },
            onAccept = { viewModel.acceptContractFromDialog() },
            onDismiss = { viewModel.closeContractDialog() },
            isDarkTheme = isDarkTheme
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.GradientStart,
                        colors.GradientCenter,
                        colors.GradientEnd
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            RegisterTopBar(
                currentStep = uiState.currentStep,
                onBackClick = {
                    if (uiState.currentStep > 1) {
                        viewModel.previousRegisterStep()
                    } else {
                        onBackClick()
                    }
                }
            )

            // Progress Indicator
            StepProgressIndicator(
                currentStep = uiState.displayStepNumber,
                totalSteps = uiState.totalSteps,
                colors = colors
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(24.dp)
            ) {
                // Step Title
                Text(
                    text = uiState.currentStepTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Adım ${uiState.displayStepNumber}/${uiState.totalSteps}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Form Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = colors.CardBackground),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        // Step Content
                        AnimatedContent(
                            targetState = uiState.currentStep,
                            transitionSpec = {
                                if (targetState > initialState) {
                                    slideInHorizontally { it } + fadeIn() togetherWith
                                            slideOutHorizontally { -it } + fadeOut()
                                } else {
                                    slideInHorizontally { -it } + fadeIn() togetherWith
                                            slideOutHorizontally { it } + fadeOut()
                                }
                            },
                            label = "step_animation"
                        ) { step ->
                            when (step) {
                                1 -> Step1PersonalInfo(viewModel, uiState, colors)
                                2 -> Step2BillingAddress(viewModel, uiState, colors)
                                3 -> Step3AdditionalInfo(viewModel, uiState, colors)
                                4 -> Step4AccountSecurity(viewModel, uiState, colors)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Navigation Buttons
                        NavigationButtons(
                            viewModel = viewModel,
                            uiState = uiState,
                            colors = colors
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hesabınız Var mı?",
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            text = "Giriş Yap",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// TOP BAR
// ============================================

@Composable
private fun RegisterTopBar(
    currentStep: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Geri",
                tint = Color.White
            )
        }

        Text(
            text = "Kayıt Ol",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// ============================================
// PROGRESS INDICATOR
// ============================================

@Composable
private fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < currentStep
            val isCurrent = stepNumber == currentStep

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = when {
                            isCompleted -> colors.Success
                            isCurrent -> Color.White
                            else -> Color.White.copy(alpha = 0.3f)
                        },
                        shape = MaterialTheme.shapes.small
                    )
            )
        }
    }
}

// ============================================
// NAVIGATION BUTTONS
// ============================================

@Composable
private fun NavigationButtons(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Geri Butonu
        if (uiState.currentStep > 1) {
            OutlinedButton(
                onClick = viewModel::previousRegisterStep,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Geri")
            }
        }

        // İleri / Kayıt Ol Butonu
        val isLastStep = uiState.currentStep == 4
        val buttonEnabled = !uiState.isLoading && (!isLastStep || uiState.allContractsAccepted)

        Button(
            onClick = viewModel::nextRegisterStep,
            modifier = Modifier.weight(1f),
            enabled = buttonEnabled,
            shape = MaterialTheme.shapes.large,
            colors = ButtonDefaults.buttonColors(containerColor = colors.Primary)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isLastStep) "Kayıt Ol" else "İleri",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = if (isLastStep) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ============================================
// STEP 1: KİŞİSEL BİLGİLER
// ============================================

@Composable
private fun Step1PersonalInfo(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Account Type Selector
        Text(
            text = "Hesap Türü",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountTypeChip(
                title = "Bireysel",
                icon = Icons.Default.Person,
                isSelected = uiState.accountType == AccountType.INDIVIDUAL,
                onClick = { viewModel.updateAccountType(AccountType.INDIVIDUAL) },
                colors = colors,
                modifier = Modifier.weight(1f)
            )

            AccountTypeChip(
                title = "Kurumsal",
                icon = Icons.Default.Business,
                isSelected = uiState.accountType == AccountType.CORPORATE,
                onClick = { viewModel.updateAccountType(AccountType.CORPORATE) },
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }

        // Name
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateRegisterField("name", it) },
            label = { Text("Ad *") },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.Primary) },
            isError = uiState.errors["name"] != null,
            supportingText = uiState.errors["name"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Surname
        OutlinedTextField(
            value = uiState.surname,
            onValueChange = { viewModel.updateRegisterField("surname", it) },
            label = { Text("Soyad *") },
            leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.Primary) },
            isError = uiState.errors["surname"] != null,
            supportingText = uiState.errors["surname"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Email
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateRegisterField("email", it) },
            label = { Text("E-posta *") },
            leadingIcon = { Icon(Icons.Default.Email, null, tint = colors.Primary) },
            isError = uiState.errors["email"] != null,
            supportingText = uiState.errors["email"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Phone
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { viewModel.updateRegisterField("phone", it) },
            label = { Text("Telefon Numarası *") },
            placeholder = { Text("5XX XXX XX XX") },
            leadingIcon = { Icon(Icons.Default.Phone, null, tint = colors.Primary) },
            isError = uiState.errors["phone"] != null,
            supportingText = uiState.errors["phone"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // T.C. Kimlik Numarası
        OutlinedTextField(
            value = uiState.citizen,
            onValueChange = { viewModel.updateRegisterField("citizen", it) },
            label = { Text(if (uiState.isTurkey) "T.C. Kimlik Numarası *" else "T.C. Kimlik Numarası (Opsiyonel)") },
            leadingIcon = { Icon(Icons.Default.Badge, null, tint = colors.Primary) },
            isError = uiState.errors["citizen"] != null,
            supportingText = {
                when {
                    uiState.errors["citizen"] != null -> Text(uiState.errors["citizen"]!!)
                    !uiState.isTurkey -> Text("Yurt dışı için opsiyoneldir")
                    else -> null
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Info Card
        InfoCard(
            text = "T.C. Kimlik Numarası, Türkiye seçiliyse zorunludur. Yurt dışı için opsiyoneldir.",
            colors = colors
        )
    }
}

// ============================================
// STEP 2: FATURA ADRESİ
// ============================================

@Composable
private fun Step2BillingAddress(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Firma Adı (Opsiyonel)
        OutlinedTextField(
            value = uiState.companyName,
            onValueChange = { viewModel.updateRegisterField("companyName", it) },
            label = { Text("Firma Adı (Opsiyonel)") },
            leadingIcon = { Icon(Icons.Default.Business, null, tint = colors.Primary) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Adres
        OutlinedTextField(
            value = uiState.address,
            onValueChange = { viewModel.updateRegisterField("address", it) },
            label = { Text("Adres *") },
            leadingIcon = { Icon(Icons.Default.Home, null, tint = colors.Primary) },
            isError = uiState.errors["address"] != null,
            supportingText = uiState.errors["address"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Adres Devamı
        OutlinedTextField(
            value = uiState.address2,
            onValueChange = { viewModel.updateRegisterField("address2", it) },
            label = { Text("Adres Devamı (Opsiyonel)") },
            leadingIcon = { Icon(Icons.Default.Home, null, tint = colors.Primary) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Posta Kodu
        OutlinedTextField(
            value = uiState.zipCode,
            onValueChange = { viewModel.updateRegisterField("zipCode", it) },
            label = { Text("Posta Kodu *") },
            leadingIcon = { Icon(Icons.Default.MarkunreadMailbox, null, tint = colors.Primary) },
            isError = uiState.errors["zipCode"] != null,
            supportingText = uiState.errors["zipCode"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Ülke Seçimi
        SearchableDropdown(
            label = "Ülke *",
            selectedText = uiState.selectedCountryName,
            searchQuery = uiState.countrySearchQuery,
            isExpanded = uiState.showCountryDropdown,
            onExpandChange = { viewModel.toggleCountryDropdown() },
            onSearchQueryChange = { viewModel.updateCountrySearchQuery(it) },
            items = uiState.filteredCountries,
            itemContent = { country ->
                CountryListItem(country = country)
            },
            onItemClick = { country ->
                viewModel.selectCountry(country)
            },
            leadingIcon = Icons.Default.Public,
            colors = colors,
            isError = false,
            errorMessage = null
        )

        // Dinamik Şehir/İlçe Alanları
        if (uiState.isTurkey) {
            // Türkiye - Dropdown'lar
            TurkeyLocationFields(viewModel, uiState, colors)
        } else {
            // Yurt Dışı - Manuel Giriş
            ForeignLocationFields(viewModel, uiState, colors, focusManager)
        }
    }
}

// ============================================
// TÜRKİYE LOKASYON ALANLARI (Dropdown)
// ============================================

@Composable
private fun TurkeyLocationFields(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    // İl Seçimi
    SearchableDropdown(
        label = "İl *",
        selectedText = uiState.selectedProvinceName.ifEmpty { "İl Seçiniz" },
        searchQuery = uiState.provinceSearchQuery,
        isExpanded = uiState.showProvinceDropdown,
        onExpandChange = { viewModel.toggleProvinceDropdown() },
        onSearchQueryChange = { viewModel.updateProvinceSearchQuery(it) },
        items = uiState.filteredProvinces,
        itemContent = { province ->
            Text(
                text = province.name,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        onItemClick = { province ->
            viewModel.selectProvince(province)
        },
        leadingIcon = Icons.Default.LocationCity,
        colors = colors,
        isError = uiState.errors["city"] != null,
        errorMessage = uiState.errors["city"]
    )

    // İlçe Seçimi
    SearchableDropdown(
        label = "İlçe *",
        selectedText = uiState.selectedDistrictName.ifEmpty { "İlçe Seçiniz" },
        searchQuery = uiState.districtSearchQuery,
        isExpanded = uiState.showDistrictDropdown,
        onExpandChange = {
            if (uiState.selectedProvinceId.isNotEmpty()) {
                viewModel.toggleDistrictDropdown()
            }
        },
        onSearchQueryChange = { viewModel.updateDistrictSearchQuery(it) },
        items = uiState.searchedDistricts,
        itemContent = { district ->
            Text(
                text = district.name,
                style = MaterialTheme.typography.bodyLarge
            )
        },
        onItemClick = { district ->
            viewModel.selectDistrict(district)
        },
        leadingIcon = Icons.Default.Place,
        colors = colors,
        isError = uiState.errors["district"] != null,
        errorMessage = uiState.errors["district"],
        enabled = uiState.selectedProvinceId.isNotEmpty(),
        placeholder = if (uiState.selectedProvinceId.isEmpty()) "Önce il seçiniz" else "İlçe Seçiniz"
    )
}

// ============================================
// YURT DIŞI LOKASYON ALANLARI (Manuel)
// ============================================

@Composable
private fun ForeignLocationFields(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    // Şehir (Manuel Giriş)
    OutlinedTextField(
        value = uiState.manualCity,
        onValueChange = { viewModel.updateRegisterField("manualCity", it) },
        label = { Text("Şehir *") },
        leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = colors.Primary) },
        isError = uiState.errors["manualCity"] != null,
        supportingText = uiState.errors["manualCity"]?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    )

    // İlçe/Bölge (Manuel Giriş)
    OutlinedTextField(
        value = uiState.manualDistrict,
        onValueChange = { viewModel.updateRegisterField("manualDistrict", it) },
        label = { Text("İlçe/Bölge *") },
        leadingIcon = { Icon(Icons.Default.Place, null, tint = colors.Primary) },
        isError = uiState.errors["manualDistrict"] != null,
        supportingText = uiState.errors["manualDistrict"]?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    )
}

// ============================================
// STEP 3: EK GEREKLİ BİLGİLER (Kurumsal)
// ============================================

@Composable
private fun Step3AdditionalInfo(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Info Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.PrimaryLight.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = colors.Primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kurumsal hesaplar için ek bilgiler gereklidir.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextPrimary
                )
            }
        }

        // Vergi Numarası
        OutlinedTextField(
            value = uiState.taxNumber,
            onValueChange = { viewModel.updateRegisterField("taxNumber", it) },
            label = { Text("Vergi Numarası *") },
            leadingIcon = { Icon(Icons.Default.Numbers, null, tint = colors.Primary) },
            isError = uiState.errors["taxNumber"] != null,
            supportingText = uiState.errors["taxNumber"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Vergi Dairesi
        OutlinedTextField(
            value = uiState.taxOffice,
            onValueChange = { viewModel.updateRegisterField("taxOffice", it) },
            label = { Text("Vergi Dairesi *") },
            leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = colors.Primary) },
            isError = uiState.errors["taxOffice"] != null,
            supportingText = uiState.errors["taxOffice"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Fatura Ünvanı
        OutlinedTextField(
            value = uiState.invoiceTitle,
            onValueChange = { viewModel.updateRegisterField("invoiceTitle", it) },
            label = { Text("Fatura Ünvanı *") },
            leadingIcon = { Icon(Icons.Default.Receipt, null, tint = colors.Primary) },
            isError = uiState.errors["invoiceTitle"] != null,
            supportingText = uiState.errors["invoiceTitle"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Fatura Teslimatı
        var invoiceDropdownExpanded by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = uiState.invoiceDeliveryType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Fatura Teslimatı") },
                leadingIcon = { Icon(Icons.Default.LocalShipping, null, tint = colors.Primary) },
                trailingIcon = {
                    IconButton(onClick = { invoiceDropdownExpanded = !invoiceDropdownExpanded }) {
                        Icon(
                            imageVector = if (invoiceDropdownExpanded)
                                Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { invoiceDropdownExpanded = true },
                shape = MaterialTheme.shapes.large
            )

            DropdownMenu(
                expanded = invoiceDropdownExpanded,
                onDismissRequest = { invoiceDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                InvoiceDeliveryType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            viewModel.updateInvoiceDeliveryType(type)
                            invoiceDropdownExpanded = false
                        }
                    )
                }
            }
        }
    }
}

// ============================================
// STEP 4: HESAP GÜVENLİĞİ
// ============================================

@Composable
private fun Step4AccountSecurity(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Şifre
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updateRegisterField("password", it) },
            label = { Text("Şifre *") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = colors.Primary) },
            trailingIcon = {
                IconButton(onClick = viewModel::toggleRegisterPasswordVisibility) {
                    Icon(
                        imageVector = if (uiState.isPasswordVisible)
                            Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (uiState.isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.errors["password"] != null,
            supportingText = uiState.errors["password"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Şifre Tekrar
        OutlinedTextField(
            value = uiState.passwordConfirm,
            onValueChange = { viewModel.updateRegisterField("passwordConfirm", it) },
            label = { Text("Şifre Tekrar *") },
            leadingIcon = { Icon(Icons.Default.Lock, null, tint = colors.Primary) },
            visualTransformation = if (uiState.isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.errors["passwordConfirm"] != null,
            supportingText = uiState.errors["passwordConfirm"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sözleşmeler
        Text(
            text = "Üyelik Sözleşmeleri",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        if (uiState.isLoadingContracts) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.contractsError != null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.contractsError,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = viewModel::retryLoadContracts) {
                    Text("Tekrar Dene")
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                uiState.contracts.forEach { acceptance ->
                    ContractCheckboxItem(
                        contract = acceptance.contract,
                        isAccepted = acceptance.isAccepted,
                        onClick = { viewModel.openContractDialog(acceptance.contract) },
                        colors = colors
                    )
                }
            }
        }

        // Sözleşme hatası
        uiState.errors["contracts"]?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// ============================================
// REUSABLE COMPONENTS
// ============================================

@Composable
private fun AccountTypeChip(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, colors.Primary, MaterialTheme.shapes.large)
                } else {
                    Modifier.border(1.dp, colors.Outline, MaterialTheme.shapes.large)
                }
            ),
        color = if (isSelected) colors.PrimaryLight else colors.Surface,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.Primary else colors.TextSecondary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.Primary else colors.TextPrimary
            )
        }
    }
}

@Composable
private fun InfoCard(
    text: String,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.PrimaryLight.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = colors.Primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = colors.TextSecondary
            )
        }
    }
}

@Composable
private fun CountryListItem(country: Country) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = country.emoji ?: "",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = country.name,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = country.iso2,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// SEARCHABLE DROPDOWN
// ============================================

@Composable
private fun <T> SearchableDropdown(
    label: String,
    selectedText: String,
    searchQuery: String,
    isExpanded: Boolean,
    onExpandChange: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    onItemClick: (T) -> Unit,
    leadingIcon: ImageVector,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    placeholder: String = ""
) {
    Column {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text(label) },
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
            leadingIcon = { Icon(leadingIcon, null, tint = if (enabled) colors.Primary else colors.Outline) },
            trailingIcon = {
                IconButton(onClick = { if (enabled) onExpandChange() }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            },
            isError = isError,
            supportingText = errorMessage?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onExpandChange() },
            shape = MaterialTheme.shapes.large
        )

        // Dropdown Dialog
        if (isExpanded) {
            Dialog(
                onDismissRequest = onExpandChange,
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .fillMaxHeight(0.7f),
                    shape = RoundedCornerShape(16.dp),
                    color = colors.CardBackground
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Search Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Ara...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = MaterialTheme.shapes.large
                        )

                        HorizontalDivider()

                        // Items List
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(items) { item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onItemClick(item)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    color = Color.Transparent
                                ) {
                                    itemContent(item)
                                }
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// CONTRACT COMPONENTS
// ============================================

@Composable
private fun ContractCheckboxItem(
    contract: Contract,
    isAccepted: Boolean,
    onClick: () -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isAccepted) colors.Success else colors.Outline,
                shape = MaterialTheme.shapes.medium
            ),
        color = if (isAccepted) colors.Success.copy(alpha = 0.1f) else colors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAccepted,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.Success,
                    uncheckedColor = colors.Outline
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contract.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = colors.TextPrimary
                )
                Text(
                    text = "(v${contract.version})",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.TextTertiary
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Oku",
                tint = colors.TextSecondary
            )
        }
    }
}

@Composable
private fun ContractDialog(
    contract: Contract,
    isScrolledToEnd: Boolean,
    onScrolledToEnd: () -> Unit,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val scrollState = rememberScrollState()

    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            val threshold = scrollState.maxValue * 0.95f
            if (scrollState.value >= threshold) {
                onScrolledToEnd()
            }
        } else if (scrollState.maxValue == 0) {
            onScrolledToEnd()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = colors.CardBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = contract.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = colors.TextSecondary
                        )
                    }
                }

                HorizontalDivider(color = colors.OutlineVariant)

                // Content
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        val plainText = contract.content
                            .replace(Regex("<[^>]*>"), "")
                            .replace("&nbsp;", " ")
                            .replace("&amp;", "&")
                            .replace("&lt;", "<")
                            .replace("&gt;", ">")
                            .replace("\\r\\n", "\n")
                            .replace("\\n", "\n")
                            .trim()

                        Text(
                            text = plainText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.TextPrimary,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                        )
                    }

                    // Scroll indicator
                    if (!isScrolledToEnd && scrollState.maxValue > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                        ) {
                            Surface(
                                color = colors.Primary.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Aşağı kaydırın",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = colors.OutlineVariant)

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Kapat")
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        enabled = isScrolledToEnd,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.Primary,
                            disabledContainerColor = colors.Outline.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "Kabul Ediyorum",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}