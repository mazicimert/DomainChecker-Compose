package com.mehmetmertmazici.domaincheckercompose.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.dp
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
                                3 -> Step3AddressInfo(viewModel, uiState, colors)
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
                                enabled = !uiState.isLoading,
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
                        text = "Zaten hesabınız var mı?",
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
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        )

        // GSM
        OutlinedTextField(
            value = uiState.gsm,
            onValueChange = { viewModel.updateRegisterField("gsm", it) },
            label = { Text("GSM *") },
            placeholder = { Text("5XX XXX XX XX") },
            leadingIcon = {
                Icon(Icons.Default.Smartphone, null, tint = colors.Primary)
            },
            isError = uiState.errors["gsm"] != null,
            supportingText = uiState.errors["gsm"]?.let { { Text(it) } },
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

        // Optional Fields Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = colors.SurfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Kurumsal Bilgiler (Opsiyonel)",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.TextSecondary
                )

                // Company Name
                OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = { viewModel.updateRegisterField("companyName", it) },
                    label = { Text("Firma Adı") },
                    leadingIcon = {
                        Icon(Icons.Default.Business, null, tint = colors.Primary)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )

                // Tax Number
                OutlinedTextField(
                    value = uiState.taxNumber,
                    onValueChange = { viewModel.updateRegisterField("taxNumber", it) },
                    label = { Text("Vergi No") },
                    leadingIcon = {
                        Icon(Icons.Default.Receipt, null, tint = colors.Primary)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                )
            }
        }
    }
}

@Composable
private fun Step3AddressInfo(
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // City
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

            // District
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Zip Code
            OutlinedTextField(
                value = uiState.zipCode,
                onValueChange = { viewModel.updateRegisterField("zipCode", it) },
                label = { Text("Posta Kodu *") },
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
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            )

            // Country (Fixed)
            OutlinedTextField(
                value = "Türkiye",
                onValueChange = {},
                label = { Text("Ülke") },
                enabled = false,
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large
            )
        }
    }
}