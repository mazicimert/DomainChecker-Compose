package com.mehmetmertmazici.domaincheckercompose.ui.screens.auth

import android.widget.Toast
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
import com.mehmetmertmazici.domaincheckercompose.ui.theme.getTextFieldColors
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthEffect
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthViewModel
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
    val focusManager = LocalFocusManager.current


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
                onBackClick = onBackClick
            )

            // Content - TEK SAYFA SCROLL
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. HESAP TÜRÜ & KİŞİSEL BİLGİLER
                Text(
                    text = "Hesap Bilgileri",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
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

                // Ad & Soyad
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateRegisterField("name", it) },
                    label = { Text("Ad *") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.Primary) },

                    colors = getTextFieldColors(colors),

                    isError = uiState.errors["name"] != null,
                    supportingText = uiState.errors["name"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.surname,
                    onValueChange = { viewModel.updateRegisterField("surname", it) },
                    label = { Text("Soyad *") },
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = colors.Primary) },
                    colors = getTextFieldColors(colors),
                    isError = uiState.errors["surname"] != null,
                    supportingText = uiState.errors["surname"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true
                )

                // Email & Telefon
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateRegisterField("email", it) },
                    label = { Text("E-posta *") },
                    leadingIcon = { Icon(Icons.Default.Email, null, tint = colors.Primary) },
                    colors = getTextFieldColors(colors),
                    isError = uiState.errors["email"] != null,
                    supportingText = uiState.errors["email"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = { viewModel.updateRegisterField("phone", it) },
                    label = { Text("Telefon *") },
                    placeholder = { Text("5XX XXX XX XX") },
                    leadingIcon = { Icon(Icons.Default.Phone, null, tint = colors.Primary) },
                    colors = getTextFieldColors(colors),
                    isError = uiState.errors["phone"] != null,
                    supportingText = uiState.errors["phone"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true
                )

                // Şifre Alanları
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
                    colors = getTextFieldColors(colors),
                    isError = uiState.errors["password"] != null,
                    supportingText = uiState.errors["password"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.passwordConfirm,
                    onValueChange = { viewModel.updateRegisterField("passwordConfirm", it) },
                    label = { Text("Şifre Tekrar *") },
                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = colors.Primary) },
                    visualTransformation = if (uiState.isPasswordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    colors = getTextFieldColors(colors),
                    isError = uiState.errors["passwordConfirm"] != null,
                    supportingText = uiState.errors["passwordConfirm"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // 2. ADRES & LOKASYON
                Text(
                    text = "Adres Bilgileri",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
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
                    itemContent = { country -> CountryListItem(country = country) },
                    onItemClick = { country -> viewModel.selectCountry(country) },
                    leadingIcon = Icons.Default.Public,
                    colors = colors
                )

                // Şehir & İlçe (Koşullu Görünüm)
                if (uiState.isTurkey) {
                    TurkeyLocationFields(viewModel, uiState, colors)
                } else {
                    ForeignLocationFields(viewModel, uiState, colors, focusManager)
                }

                // Adres
                OutlinedTextField(
                    value = uiState.address,
                    onValueChange = { viewModel.updateRegisterField("address", it) },
                    label = { Text("Adres *") },
                    leadingIcon = { Icon(Icons.Default.Home, null, tint = colors.Primary) },
                    colors = getTextFieldColors(colors),
                    isError = uiState.errors["address"] != null,
                    supportingText = uiState.errors["address"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // Adres 2 (Opsiyonel)
                OutlinedTextField(
                    value = uiState.address2,
                    onValueChange = { viewModel.updateRegisterField("address2", it) },
                    label = { Text("Adres Devamı (Opsiyonel)") },
                    leadingIcon = { Icon(Icons.Default.Home, null, tint = colors.Primary) },
                    colors = getTextFieldColors(colors),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // Posta Kodu
                OutlinedTextField(
                    value = uiState.zipCode,
                    onValueChange = { viewModel.updateRegisterField("zipCode", it) },
                    label = { Text("Posta Kodu *") },
                    leadingIcon = { Icon(Icons.Default.MarkunreadMailbox, null, tint = colors.Primary) },
                    colors = getTextFieldColors(colors),
                    isError = uiState.errors["zipCode"] != null,
                    supportingText = uiState.errors["zipCode"]?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                // 3. T.C. KİMLİK (SADECE TÜRKİYE İSE GÖSTER)
                if (uiState.isTurkey) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kimlik Bilgisi",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = uiState.citizen,
                        onValueChange = { viewModel.updateRegisterField("citizen", it) },
                        label = { Text("T.C. Kimlik Numarası") },
                        placeholder = { Text("Boş bırakabilirsiniz") },
                        leadingIcon = { Icon(Icons.Default.Badge, null, tint = colors.Primary) },
                        colors = getTextFieldColors(colors),
                        isError = uiState.errors["citizen"] != null,
                        supportingText = {
                            if (uiState.errors["citizen"] != null) {
                                Text(uiState.errors["citizen"]!!)
                            } else {
                                Text("Sadece bireysel fatura için gereklidir, boş bırakılabilir.")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

                // 4. KURUMSAL BİLGİLER (SADECE KURUMSAL İSE)
                if (uiState.isCorporate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Kurumsal Bilgiler",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = uiState.companyName,
                        onValueChange = { viewModel.updateRegisterField("companyName", it) },
                        label = { Text("Firma Adı (Opsiyonel)") },
                        leadingIcon = { Icon(Icons.Default.Business, null, tint = colors.Primary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = getTextFieldColors(colors),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    OutlinedTextField(
                        value = uiState.taxNumber,
                        onValueChange = { viewModel.updateRegisterField("taxNumber", it) },
                        label = { Text("Vergi Numarası *") },
                        leadingIcon = { Icon(Icons.Default.Numbers, null, tint = colors.Primary) },
                        colors = getTextFieldColors(colors),
                        isError = uiState.errors["taxNumber"] != null,
                        supportingText = uiState.errors["taxNumber"]?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    OutlinedTextField(
                        value = uiState.taxOffice,
                        onValueChange = { viewModel.updateRegisterField("taxOffice", it) },
                        label = { Text("Vergi Dairesi *") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, null, tint = colors.Primary) },
                        colors = getTextFieldColors(colors),
                        isError = uiState.errors["taxOffice"] != null,
                        supportingText = uiState.errors["taxOffice"]?.let { { Text(it) } },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                // 5. SÖZLEŞMELER
                Text(
                    text = "Sözleşmeler",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                if (uiState.isLoadingContracts) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                } else if (uiState.contractsError != null) {
                    Text(
                        text = "Sözleşmeler yüklenemedi: ${uiState.contractsError}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    TextButton(onClick = viewModel::retryLoadContracts) {
                        Text("Tekrar Dene", color = colors.Primary)
                    }
                } else {
                    uiState.contracts.forEach { acceptance ->
                        ContractCheckboxItem(
                            contract = acceptance.contract,
                            isAccepted = acceptance.isAccepted,
                            onClick = { viewModel.openContractDialog(acceptance.contract) },
                            colors = colors
                        )
                    }
                    if (uiState.errors["contracts"] != null) {
                        Text(
                            text = uiState.errors["contracts"]!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 6. KAYIT BUTONU
                Button(
                    onClick = { viewModel.onRegisterClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.Primary,
                        disabledContainerColor = colors.Primary.copy(alpha = 0.5f)
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Kayıt Ol",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Giriş Linki
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ============================================
// TOP BAR
// ============================================

@Composable
private fun RegisterTopBar(
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
            text = "Hesap Oluştur",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

// ============================================
// LOCATION HELPER COMPOSABLES
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
            Text(text = province.name, style = MaterialTheme.typography.bodyLarge)
        },
        onItemClick = { province -> viewModel.selectProvince(province) },
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
            Text(text = district.name, style = MaterialTheme.typography.bodyLarge)
        },
        onItemClick = { district -> viewModel.selectDistrict(district) },
        leadingIcon = Icons.Default.Place,
        colors = colors,
        isError = uiState.errors["district"] != null,
        errorMessage = uiState.errors["district"],
        enabled = uiState.selectedProvinceId.isNotEmpty(),
        placeholder = if (uiState.selectedProvinceId.isEmpty()) "Önce il seçiniz" else "İlçe Seçiniz"
    )
}

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
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )

    // İlçe/Bölge (Manuel Giriş)
    OutlinedTextField(
        value = uiState.manualDistrict,
        onValueChange = { viewModel.updateRegisterField("manualDistrict", it) },
        label = { Text("İlçe/Bölge *") },
        leadingIcon = { Icon(Icons.Default.Place, null, tint = colors.Primary) },
        isError = uiState.errors["manualDistrict"] != null,
        supportingText = uiState.errors["manualDistrict"]?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
}

// ============================================
// OTHER REUSABLE COMPONENTS
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
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.Primary else colors.Outline,
                shape = MaterialTheme.shapes.large
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
            shape = MaterialTheme.shapes.large,

            colors = getTextFieldColors(colors)
        )

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
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            placeholder = { Text("Ara...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = MaterialTheme.shapes.large,

                            colors = getTextFieldColors(colors)
                        )

                        HorizontalDivider()

                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(items) { item ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onItemClick(item) }
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
                            .trim()

                        Text(
                            text = plainText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.TextPrimary
                        )
                    }
                }

                HorizontalDivider(color = colors.OutlineVariant)

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
                        Text(text = "Kabul Ediyorum", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}