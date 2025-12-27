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
import com.mehmetmertmazici.domaincheckercompose.viewmodel.Country
import com.mehmetmertmazici.domaincheckercompose.viewmodel.InvoiceDeliveryType
import com.mehmetmertmazici.domaincheckercompose.viewmodel.RegisterUiState
import com.mehmetmertmazici.domaincheckercompose.viewmodel.SecurityQuestion
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

    // Country Picker Dialog
    if (uiState.showCountryPicker) {
        CountryPickerDialog(
            selectedCountryCode = uiState.country,
            onCountrySelected = { viewModel.updateCountry(it) },
            onDismiss = { viewModel.hideCountryPicker() },
            isDarkTheme = isDarkTheme
        )
    }

    // Security Question Picker Dialog
    if (uiState.showSecurityQuestionPicker) {
        SecurityQuestionPickerDialog(
            selectedQuestion = uiState.securityQuestion,
            onQuestionSelected = { viewModel.updateSecurityQuestion(it) },
            onDismiss = { viewModel.hideSecurityQuestionPicker() },
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
                val stepTitle = getStepTitle(uiState.currentStep, uiState.isCorporate)
                val stepIcon = getStepIcon(uiState.currentStep, uiState.isCorporate)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = stepIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stepTitle,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Adım ${uiState.currentStep}/${uiState.totalSteps}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        // İlk aşamada hesap türü seçimi
                        if (uiState.currentStep == 1) {
                            AccountTypeSelector(
                                selectedType = uiState.accountType,
                                onTypeSelected = { viewModel.updateAccountType(it) },
                                colors = colors
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            HorizontalDivider(color = colors.OutlineVariant)
                            Spacer(modifier = Modifier.height(24.dp))
                        }

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
                            when {
                                step == 1 -> Step1PersonalInfo(viewModel, uiState, colors)
                                step == 2 -> Step2InvoiceAddress(viewModel, uiState, colors)
                                step == 3 && uiState.isCorporate -> Step3CorporateInfo(viewModel, uiState, colors)
                                (step == 3 && !uiState.isCorporate) || step == 4 -> Step4Security(viewModel, uiState, colors)
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
                                    onClick = { viewModel.previousRegisterStep() },
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

                            val isLastStep = uiState.currentStep == uiState.totalSteps
                            val buttonEnabled = !uiState.isLoading &&
                                    (!isLastStep || uiState.allContractsAccepted)

                            Button(
                                onClick = { viewModel.nextRegisterStep() },
                                modifier = Modifier.weight(1f),
                                enabled = buttonEnabled,
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
                                        text = if (isLastStep) "Kayıt Ol" else "İleri",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = if (isLastStep)
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

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private fun getStepTitle(step: Int, isCorporate: Boolean): String {
    return when {
        step == 1 -> "Kişisel Bilgiler"
        step == 2 -> "Fatura Adresi"
        step == 3 && isCorporate -> "Ek Gerekli Bilgiler"
        (step == 3 && !isCorporate) || step == 4 -> "Hesap Güvenliği"
        else -> ""
    }
}

private fun getStepIcon(step: Int, isCorporate: Boolean): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        step == 1 -> Icons.Default.Person
        step == 2 -> Icons.Default.LocationOn
        step == 3 && isCorporate -> Icons.Default.Business
        (step == 3 && !isCorporate) || step == 4 -> Icons.Default.Security
        else -> Icons.Default.Circle
    }
}

@Composable
private fun StepProgressIndicator(
    currentStep: Int,
    totalSteps: Int,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(modifier = Modifier.height(8.dp))

        // Step indicators with numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(totalSteps) { index ->
                val stepNumber = index + 1
                val isCompleted = stepNumber < currentStep
                val isCurrent = stepNumber == currentStep

                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = MaterialTheme.shapes.small,
                    color = when {
                        isCompleted -> colors.Success
                        isCurrent -> Color.White
                        else -> Color.White.copy(alpha = 0.3f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = stepNumber.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) colors.Primary else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AccountTypeSelector(
    selectedType: AccountType,
    onTypeSelected: (AccountType) -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Column {
        Text(
            text = "Hesap Türü",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccountTypeChip(
                title = "Bireysel",
                icon = Icons.Default.Person,
                isSelected = selectedType == AccountType.INDIVIDUAL,
                onClick = { onTypeSelected(AccountType.INDIVIDUAL) },
                colors = colors,
                modifier = Modifier.weight(1f)
            )

            AccountTypeChip(
                title = "Kurumsal",
                icon = Icons.Default.Business,
                isSelected = selectedType == AccountType.CORPORATE,
                onClick = { onTypeSelected(AccountType.CORPORATE) },
                colors = colors,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AccountTypeChip(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

// ============================================
// AŞAMA 1: KİŞİSEL BİLGİLER
// ============================================

@Composable
private fun Step1PersonalInfo(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // İsim
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { viewModel.updateRegisterField("name", it) },
            label = { Text("İsim *") },
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

        // Soyisim
        OutlinedTextField(
            value = uiState.surname,
            onValueChange = { viewModel.updateRegisterField("surname", it) },
            label = { Text("Soyisim *") },
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

        // E-posta
        OutlinedTextField(
            value = uiState.email,
            onValueChange = { viewModel.updateRegisterField("email", it) },
            label = { Text("E-Posta Adresi *") },
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

        // Telefon
        OutlinedTextField(
            value = uiState.phone,
            onValueChange = { viewModel.updateRegisterField("phone", it) },
            label = { Text("Telefon Numarası *") },
            placeholder = { Text("5XX XXX XX XX") },
            leadingIcon = {
                Icon(Icons.Default.Phone, null, tint = colors.Primary)
            },
            isError = uiState.errors["phone"] != null,
            supportingText = uiState.errors["phone"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // T.C. Kimlik Numarası (Opsiyonel - son alanda)
        OutlinedTextField(
            value = uiState.tcKimlik,
            onValueChange = { viewModel.updateRegisterField("tcKimlik", it) },
            label = { Text("T.C. Kimlik Numarası") },
            leadingIcon = {
                Icon(Icons.Default.Badge, null, tint = colors.Primary)
            },
            isError = uiState.errors["tcKimlik"] != null,
            supportingText = {
                if (uiState.errors["tcKimlik"] != null) {
                    Text(uiState.errors["tcKimlik"]!!)
                } else {
                    Text("Türkiye için zorunlu, diğer ülkeler için opsiyonel")
                }
            },
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

        // Bilgi kartı
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.Info.copy(alpha = 0.1f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.Info,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "T.C. Kimlik numarası sadece Türkiye'de ikamet edenler için zorunludur.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.TextSecondary
                )
            }
        }
    }
}

// ============================================
// AŞAMA 2: FATURA ADRESİ
// ============================================

@Composable
private fun Step2InvoiceAddress(
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
            leadingIcon = {
                Icon(Icons.Default.Business, null, tint = colors.Primary)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Adres
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

        // Adres Devamı
        OutlinedTextField(
            value = uiState.address2,
            onValueChange = { viewModel.updateRegisterField("address2", it) },
            label = { Text("Adres Devamı") },
            leadingIcon = {
                Icon(Icons.Default.Home, null, tint = colors.Primary)
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            maxLines = 2,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Ülke Seçimi
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.showCountryPicker() }
                .border(
                    width = 1.dp,
                    color = if (uiState.errors["country"] != null) colors.Error else colors.Outline,
                    shape = MaterialTheme.shapes.large
                ),
            shape = MaterialTheme.shapes.large,
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Public,
                    contentDescription = null,
                    tint = colors.Primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Ülke *",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.TextSecondary
                    )
                    Text(
                        text = uiState.selectedCountryName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.TextPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = colors.TextSecondary
                )
            }
        }

        // Şehir / İlçe
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = uiState.city,
                onValueChange = { viewModel.updateRegisterField("city", it) },
                label = { Text("Şehir *") },
                isError = uiState.errors["city"] != null,
                supportingText = uiState.errors["city"]?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Right) }
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            )

            OutlinedTextField(
                value = uiState.district,
                onValueChange = { viewModel.updateRegisterField("district", it) },
                label = { Text("İlçe *") },
                isError = uiState.errors["district"] != null,
                supportingText = uiState.errors["district"]?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            )
        }

        // Posta Kodu
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
    }
}

// ============================================
// AŞAMA 3: EK GEREKLİ BİLGİLER (KURUMSAL)
// ============================================

@Composable
private fun Step3CorporateInfo(
    viewModel: AuthViewModel,
    uiState: RegisterUiState,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Bilgi kartı
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.Warning.copy(alpha = 0.1f)
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
                    tint = colors.Warning,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kurumsal hesap için fatura bilgilerinizi girin",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextSecondary
                )
            }
        }

        // Vergi Numarası
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

        // Vergi Dairesi
        OutlinedTextField(
            value = uiState.taxOffice,
            onValueChange = { viewModel.updateRegisterField("taxOffice", it) },
            label = { Text("Vergi Dairesi *") },
            leadingIcon = {
                Icon(Icons.Default.AccountBalance, null, tint = colors.Primary)
            },
            isError = uiState.errors["taxOffice"] != null,
            supportingText = uiState.errors["taxOffice"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Fatura Ünvanı
        OutlinedTextField(
            value = uiState.invoiceTitle,
            onValueChange = { viewModel.updateRegisterField("invoiceTitle", it) },
            label = { Text("Fatura Ünvanı *") },
            leadingIcon = {
                Icon(Icons.Default.Description, null, tint = colors.Primary)
            },
            isError = uiState.errors["invoiceTitle"] != null,
            supportingText = uiState.errors["invoiceTitle"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // Fatura Teslimat Seçimi
        Text(
            text = "Fatura Teslimatı",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InvoiceDeliveryType.entries.forEach { type ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable { viewModel.updateInvoiceDeliveryType(type) }
                        .border(
                            width = if (uiState.invoiceDeliveryType == type) 2.dp else 1.dp,
                            color = if (uiState.invoiceDeliveryType == type) colors.Primary else colors.Outline,
                            shape = MaterialTheme.shapes.medium
                        ),
                    color = if (uiState.invoiceDeliveryType == type)
                        colors.PrimaryLight.copy(alpha = 0.3f) else Color.Transparent
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.invoiceDeliveryType == type,
                            onClick = { viewModel.updateInvoiceDeliveryType(type) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.Primary
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.TextPrimary
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// AŞAMA 4: HESAP GÜVENLİĞİ
// ============================================

@Composable
private fun Step4Security(
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
            leadingIcon = {
                Icon(Icons.Default.Lock, null, tint = colors.Primary)
            },
            trailingIcon = {
                IconButton(onClick = { viewModel.toggleRegisterPasswordVisibility() }) {
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

        // Şifre Tekrar
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
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        HorizontalDivider(color = colors.OutlineVariant)

        // Güvenlik Sorusu Seçimi
        Text(
            text = "Güvenlik Sorusu *",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.showSecurityQuestionPicker() }
                .border(
                    width = 1.dp,
                    color = if (uiState.errors["securityQuestion"] != null) colors.Error else colors.Outline,
                    shape = MaterialTheme.shapes.large
                ),
            shape = MaterialTheme.shapes.large,
            color = Color.Transparent
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.QuestionAnswer,
                    contentDescription = null,
                    tint = colors.Primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = uiState.securityQuestion?.question ?: "Güvenlik sorusu seçin",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (uiState.securityQuestion != null)
                        colors.TextPrimary else colors.TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = colors.TextSecondary
                )
            }
        }

        if (uiState.errors["securityQuestion"] != null) {
            Text(
                text = uiState.errors["securityQuestion"]!!,
                style = MaterialTheme.typography.bodySmall,
                color = colors.Error
            )
        }

        // Güvenlik Sorusu Yanıtı
        OutlinedTextField(
            value = uiState.securityAnswer,
            onValueChange = { viewModel.updateRegisterField("securityAnswer", it) },
            label = { Text("Güvenlik Sorusu Yanıtı *") },
            leadingIcon = {
                Icon(Icons.Default.Edit, null, tint = colors.Primary)
            },
            isError = uiState.errors["securityAnswer"] != null,
            supportingText = uiState.errors["securityAnswer"]?.let { { Text(it) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        HorizontalDivider(color = colors.OutlineVariant)

        // Sözleşmeler Bölümü
        Text(
            text = "Kullanım Koşulları *",
            style = MaterialTheme.typography.titleSmall,
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
                    text = "Devam etmek için tüm sözleşmeleri okuyup onaylamanız gerekmektedir",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.Warning
                )
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

// ============================================
// DİALOGLAR
// ============================================

@Composable
private fun CountryPickerDialog(
    selectedCountryCode: String,
    onCountrySelected: (String) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(16.dp),
            color = colors.CardBackground
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ülke Seçin",
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(Country.COUNTRIES) { country ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCountrySelected(country.code) },
                            color = if (country.code == selectedCountryCode)
                                colors.PrimaryLight.copy(alpha = 0.3f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = country.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (country.code == selectedCountryCode) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityQuestionPickerDialog(
    selectedQuestion: SecurityQuestion?,
    onQuestionSelected: (SecurityQuestion) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = colors.CardBackground
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Güvenlik Sorusu Seçin",
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    SecurityQuestion.entries.forEach { question ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onQuestionSelected(question) },
                            color = if (question == selectedQuestion)
                                colors.PrimaryLight.copy(alpha = 0.3f) else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = question.question,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = colors.TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (question == selectedQuestion) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
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