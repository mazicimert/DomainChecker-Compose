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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mehmetmertmazici.domaincheckercompose.model.AccountType
import com.mehmetmertmazici.domaincheckercompose.model.Contract
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthEffect
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.RegisterUiState
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onBackClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val uiState by viewModel.registerState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        viewModel.loadCountries()
    }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    if (uiState.currentStep > 1) {
                        viewModel.previousRegisterStep()
                    } else {
                        onBackClick()
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
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

            // Progress Indicator
            StepProgressIndicator(
                currentStep = uiState.currentStep,
                totalSteps = 3,
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
                val stepTitle = when (uiState.currentStep) {
                    1 -> "Kişisel Bilgiler"
                    2 -> "İletişim Bilgileri"
                    3 -> "Adres Bilgileri"
                    else -> ""
                }

                Text(
                    text = stepTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Adım ${uiState.currentStep}/3",
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
                                2 -> Step2ContactInfo(viewModel, uiState, colors)
                                3 -> Step3AddressAndContracts(viewModel, uiState, colors)
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Navigation Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (uiState.currentStep > 1) {
                                OutlinedButton(
                                    onClick = viewModel::previousRegisterStep,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.large
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Geri")
                                }
                            }

                            Button(
                                onClick = viewModel::nextRegisterStep,
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading &&
                                        (uiState.currentStep != 3 || uiState.allContractsAccepted),
                                shape = MaterialTheme.shapes.large,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.Primary
                                )
                            ) {
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (uiState.currentStep == 3) "Kayıt Ol" else "İleri",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (uiState.currentStep == 3)
                                            Icons.Default.Check else Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
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

        // Kurumsal bilgiler
        AnimatedVisibility(
            visible = uiState.isCorporate,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = { viewModel.updateRegisterField("companyName", it) },
                    label = { Text("Firma Adı *") },
                    leadingIcon = {
                        Icon(Icons.Default.Business, null, tint = colors.Primary)
                    },
                    isError = uiState.errors["companyName"] != null,
                    supportingText = uiState.errors["companyName"]?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )

                OutlinedTextField(
                    value = uiState.taxNumber,
                    onValueChange = { viewModel.updateRegisterField("taxNumber", it) },
                    label = { Text("Vergi Numarası *") },
                    leadingIcon = {
                        Icon(Icons.Default.Receipt, null, tint = colors.Primary)
                    },
                    isError = uiState.errors["taxNumber"] != null,
                    supportingText = uiState.errors["taxNumber"]?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )
            }
        }

        // Name
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateRegisterField("name", it) },
            label = { Text("Ad *") },
            leadingIcon = {
                Icon(Icons.Default.Person, null, tint = colors.Primary)
            },
            isError = uiState.errors["name"] != null,
            supportingText = uiState.errors["name"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Surname
        OutlinedTextField(
            value = uiState.surname,
            onValueChange = { viewModel.updateRegisterField("surname", it) },
            label = { Text("Soyad *") },
            leadingIcon = {
                Icon(Icons.Default.Person, null, tint = colors.Primary)
            },
            isError = uiState.errors["surname"] != null,
            supportingText = uiState.errors["surname"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Email
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateRegisterField("email", it) },
            label = { Text("E-posta *") },
            leadingIcon = {
                Icon(Icons.Default.Email, null, tint = colors.Primary)
            },
            isError = uiState.errors["email"] != null,
            supportingText = uiState.errors["email"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Password
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { viewModel.updateRegisterField("password", it) },
            label = { Text("Şifre *") },
            leadingIcon = {
                Icon(Icons.Default.Lock, null, tint = colors.Primary)
            },
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
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Password Confirm
        OutlinedTextField(
            value = uiState.passwordConfirm,
            onValueChange = { viewModel.updateRegisterField("passwordConfirm", it) },
            label = { Text("Şifre Tekrar *") },
            leadingIcon = {
                Icon(Icons.Default.Lock, null, tint = colors.Primary)
            },
            visualTransformation = if (uiState.isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            isError = uiState.errors["passwordConfirm"] != null,
            supportingText = uiState.errors["passwordConfirm"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )
    }
}


@Composable
private fun Step2ContactInfo(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Phone
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { viewModel.updateRegisterField("phone", it) },
            label = { Text("Telefon *") },
            placeholder = { Text("5XX XXX XX XX") },
            leadingIcon = {
                Icon(Icons.Default.Phone, null, tint = colors.Primary)
            },
            isError = uiState.errors["phone"] != null,
            supportingText = uiState.errors["phone"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Info Card
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
                    text = "Telefon numaranız hesap güvenliği ve bildirimler için kullanılacaktır.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun Step3AddressAndContracts(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Address
        OutlinedTextField(
            value = uiState.address,
            onValueChange = { viewModel.updateRegisterField("address", it) },
            label = { Text("Adres *") },
            leadingIcon = {
                Icon(Icons.Default.Home, null, tint = colors.Primary)
            },
            isError = uiState.errors["address"] != null,
            supportingText = uiState.errors["address"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // ÜLKE SEÇİCİ
        LocationDropdownField(
            label = "Ülke *",
            selectedText = uiState.selectedCountry?.name ?: "Ülke Seçin",
            isExpanded = uiState.isCountryDropdownExpanded,
            isLoading = uiState.isLoadingCountries,
            isError = uiState.errors["country"] != null,
            errorText = uiState.errors["country"],
            onExpandChange = { viewModel.toggleCountryDropdown() },
            colors = colors,
            leadingIcon = Icons.Default.Public,
            enabled = true
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            ) {
                items(uiState.countries) { country ->
                    DropdownMenuItem(
                        text = { Text(country.name) },
                        onClick = { viewModel.selectCountry(country) },
                        leadingIcon = {
                            Text(
                                text = getFlagEmoji(country.iso2),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    )
                }
            }
        }

        // ŞEHİR VE İLÇE SEÇİCİLERİ
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Şehir/İl Seçici
            Box(modifier = Modifier.weight(1f)) {
                LocationDropdownField(
                    label = "Şehir *",
                    selectedText = uiState.selectedState?.name ?: "Şehir Seçin",
                    isExpanded = uiState.isStateDropdownExpanded,
                    isLoading = uiState.isLoadingStates,
                    isError = uiState.errors["city"] != null,
                    errorText = uiState.errors["city"],
                    onExpandChange = {
                        if (uiState.selectedCountry != null && uiState.states.isNotEmpty()) {
                            viewModel.toggleStateDropdown()
                        }
                    },
                    enabled = uiState.selectedCountry != null && uiState.states.isNotEmpty(),
                    colors = colors,
                    leadingIcon = Icons.Default.LocationCity
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(uiState.states) { state ->
                            DropdownMenuItem(
                                text = { Text(state.name) },
                                onClick = { viewModel.selectState(state) }
                            )
                        }
                    }
                }
            }

            // İlçe Seçici
            Box(modifier = Modifier.weight(1f)) {
                LocationDropdownField(
                    label = "İlçe *",
                    selectedText = uiState.selectedCity?.name ?: "İlçe Seçin",
                    isExpanded = uiState.isCityDropdownExpanded,
                    isLoading = uiState.isLoadingCities,
                    isError = uiState.errors["district"] != null,
                    errorText = uiState.errors["district"],
                    onExpandChange = {
                        if (uiState.selectedState != null && uiState.cities.isNotEmpty()) {
                            viewModel.toggleCityDropdown()
                        }
                    },
                    enabled = uiState.selectedState != null && uiState.cities.isNotEmpty(),
                    colors = colors,
                    leadingIcon = Icons.Default.Place
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                    ) {
                        items(uiState.cities) { city ->
                            DropdownMenuItem(
                                text = { Text(city.name) },
                                onClick = { viewModel.selectCity(city) }
                            )
                        }
                    }
                }
            }
        }

        // POSTA KODU
        OutlinedTextField(
            value = uiState.zipCode,
            onValueChange = { viewModel.updateRegisterField("zipCode", it) },
            label = { Text("Posta Kodu *") },
            leadingIcon = {
                Icon(Icons.Default.MarkunreadMailbox, null, tint = colors.Primary)
            },
            isError = uiState.errors["zipCode"] != null,
            supportingText = uiState.errors["zipCode"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Sözleşmeler Bölümü
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = colors.OutlineVariant)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sözleşmeler*",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        // Loading state
        if (uiState.isLoadingContracts) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = colors.Primary
                )
            }
        }
        // Error state
        else if (uiState.contractsError != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.Error.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = colors.Error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.contractsError ?: "Hata",
                            color = colors.Error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    TextButton(onClick = { viewModel.retryLoadContracts() }) {
                        Text("Tekrar Dene")
                    }
                }
            }
        }
        // Contracts list
        else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                uiState.contracts.forEach { contractAcceptance ->
                    ContractCheckboxItem(
                        contract = contractAcceptance.contract,
                        isAccepted = contractAcceptance.isAccepted,
                        onClick = { viewModel.openContractDialog(contractAcceptance.contract) },
                        colors = colors
                    )
                }
            }

            // Uyarı mesajı
            if (uiState.contracts.isNotEmpty() && !uiState.allContractsAccepted) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lütfen tüm sözleşmeleri okuyup onaylayınız",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.TextSecondary
                )
            }
        }
    }
}


// YENİ EKLENEN: LocationDropdownField Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationDropdownField(
    label: String,
    selectedText: String,
    isExpanded: Boolean,
    isLoading: Boolean,
    isError: Boolean,
    errorText: String?,
    onExpandChange: () -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors,
    leadingIcon: ImageVector,
    enabled: Boolean = true,
    dropdownContent: @Composable () -> Unit
) {
    ExposedDropdownMenuBox(
        expanded = isExpanded && enabled,
        onExpandedChange = { if (enabled && !isLoading) onExpandChange() }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    leadingIcon,
                    null,
                    tint = if (enabled) colors.Primary else colors.Outline
                )
            },
            trailingIcon = {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = colors.Primary
                    )
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                }
            },
            isError = isError,
            supportingText = errorText?.let { { Text(it) } },
            enabled = enabled,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = colors.TextSecondary,
                disabledBorderColor = colors.Outline,
                disabledLeadingIconColor = colors.Outline,
                disabledLabelColor = colors.TextSecondary
            )
        )

        ExposedDropdownMenu(
            expanded = isExpanded && enabled,
            onDismissRequest = onExpandChange
        ) {
            dropdownContent()
        }
    }
}

// YENİ EKLENEN: Bayrak Emoji Helper
private fun getFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🏳️"

    val firstChar = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
    val secondChar = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6

    return String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
}

// ============================================
// AccountTypeChip - AYNI KALIYOR
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.Primary else colors.Outline,
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isSelected) colors.PrimaryLight.copy(alpha = 0.2f) else colors.Surface
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
                tint = if (isSelected) colors.Primary else colors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.Primary else colors.TextSecondary
            )
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isAccepted) colors.Success else colors.Outline,
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isAccepted) colors.Success.copy(alpha = 0.05f) else colors.Surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAccepted,
                onCheckedChange = null, // Click handled by Surface
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

    // Scroll sonuna ulaşıldığında callback çağır
    LaunchedEffect(scrollState.value, scrollState.maxValue) {
        if (scrollState.maxValue > 0) {
            val threshold = scrollState.maxValue * 0.95f
            if (scrollState.value >= threshold) {
                onScrolledToEnd()
            }
        } else if (scrollState.maxValue == 0) {
            // İçerik kısa, scroll gerekmiyor
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
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
                        color = colors.TextPrimary
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
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        // HTML içeriği plain text olarak göster
                        val plainText = contract.content
                            .replace(Regex("<[^>]*>"), "") // HTML tag'lerini kaldır
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

                // Footer Buttons
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