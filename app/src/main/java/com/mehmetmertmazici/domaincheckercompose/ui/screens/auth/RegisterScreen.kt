package com.mehmetmertmazici.domaincheckercompose.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
        ModernContractDialog(
            contract = contract,
            isScrolledToEnd = uiState.dialogScrolledToEnd,
            onScrolledToEnd = { viewModel.onDialogScrolledToEnd() },
            onAccept = { viewModel.acceptContractFromDialog() },
            onDismiss = { viewModel.closeContractDialog() },
            colors = colors
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.AuthBackground)
    ) {
        // Decorative top gradient - daha kısa
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            colors.AuthButtonGradientStart,
                            colors.AuthButtonGradientEnd,
                            colors.AuthBackground
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Modern Top Bar
            ModernRegisterTopBar(
                onBackClick = onBackClick,
                colors = colors
            )

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.05f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.AuthCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Section Title
                        SectionHeader(
                            title = "Hesap Türü",
                            icon = Icons.Outlined.AccountCircle,
                            colors = colors
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Account Type Selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernAccountTypeChip(
                                title = "Bireysel",
                                subtitle = "Kişisel kullanım",
                                icon = Icons.Outlined.Person,
                                isSelected = uiState.accountType == AccountType.INDIVIDUAL,
                                onClick = { viewModel.updateAccountType(AccountType.INDIVIDUAL) },
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )

                            ModernAccountTypeChip(
                                title = "Kurumsal",
                                subtitle = "Şirket hesabı",
                                icon = Icons.Outlined.Business,
                                isSelected = uiState.accountType == AccountType.CORPORATE,
                                onClick = { viewModel.updateAccountType(AccountType.CORPORATE) },
                                colors = colors,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Personal Info Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.05f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.AuthCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionHeader(
                            title = "Kişisel Bilgiler",
                            icon = Icons.Outlined.Badge,
                            colors = colors
                        )

                        // Ad & Soyad - Yan yana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernTextField(
                                value = uiState.name,
                                onValueChange = { viewModel.updateRegisterField("name", it) },
                                label = "Ad",
                                placeholder = "Adınız",
                                leadingIcon = Icons.Outlined.Person,
                                isRequired = true,
                                isError = uiState.errors["name"] != null,
                                errorMessage = uiState.errors["name"],
                                colors = colors,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) })
                            )

                            ModernTextField(
                                value = uiState.surname,
                                onValueChange = { viewModel.updateRegisterField("surname", it) },
                                label = "Soyad",
                                placeholder = "Soyadınız",
                                leadingIcon = Icons.Outlined.Person,
                                isRequired = true,
                                isError = uiState.errors["surname"] != null,
                                errorMessage = uiState.errors["surname"],
                                colors = colors,
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }

                        // Email
                        ModernTextField(
                            value = uiState.email,
                            onValueChange = { viewModel.updateRegisterField("email", it) },
                            label = "E-posta",
                            placeholder = "ornek@email.com",
                            leadingIcon = Icons.Outlined.Email,
                            isRequired = true,
                            isError = uiState.errors["email"] != null,
                            errorMessage = uiState.errors["email"],
                            colors = colors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )

                        // Telefon
                        ModernTextField(
                            value = uiState.phone,
                            onValueChange = { viewModel.updateRegisterField("phone", it) },
                            label = "Telefon",
                            placeholder = "5XX XXX XX XX",
                            leadingIcon = Icons.Outlined.Phone,
                            isRequired = true,
                            isError = uiState.errors["phone"] != null,
                            errorMessage = uiState.errors["phone"],
                            colors = colors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )

                        // T.C. Kimlik (Sadece Türkiye + Bireysel için)
                        AnimatedVisibility(
                            visible = uiState.isTurkey && !uiState.isCorporate,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            ModernTextField(
                                value = uiState.citizen,
                                onValueChange = { viewModel.updateRegisterField("citizen", it) },
                                label = "T.C. Kimlik No",
                                placeholder = "11 haneli kimlik numaranız",
                                leadingIcon = Icons.Outlined.Badge,
                                isRequired = false,
                                isError = uiState.errors["citizen"] != null,
                                errorMessage = uiState.errors["citizen"],
                                colors = colors,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }
                    }
                }

                // Address Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.05f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.AuthCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionHeader(
                            title = "Adres Bilgileri",
                            icon = Icons.Outlined.LocationOn,
                            colors = colors
                        )

                        // Ülke Dropdown
                        ModernDropdownField(
                            value = uiState.selectedCountryName,
                            label = "Ülke",
                            placeholder = "Ülke seçin",
                            leadingIcon = Icons.Outlined.Public,
                            isRequired = true,
                            onClick = { viewModel.toggleCountryDropdown() },
                            colors = colors
                        )

                        // İl/Şehir
                        if (uiState.isTurkey) {
                            ModernDropdownField(
                                value = uiState.selectedProvinceName,
                                label = "İl",
                                placeholder = "İl seçin",
                                leadingIcon = Icons.Outlined.LocationCity,
                                isRequired = true,
                                onClick = { viewModel.toggleProvinceDropdown() },
                                colors = colors
                            )
                        } else {
                            ModernTextField(
                                value = uiState.manualCity,
                                onValueChange = { viewModel.updateRegisterField("manualCity", it) },
                                label = "Şehir",
                                placeholder = "Şehir adı",
                                leadingIcon = Icons.Outlined.LocationCity,
                                isRequired = true,
                                isError = uiState.errors["city"] != null,
                                errorMessage = uiState.errors["city"],
                                colors = colors,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }

                        // İlçe
                        if (uiState.isTurkey && uiState.selectedProvinceId.isNotEmpty()) {
                            ModernDropdownField(
                                value = uiState.selectedDistrictName,
                                label = "İlçe",
                                placeholder = "İlçe seçin",
                                leadingIcon = Icons.Outlined.Map,
                                isRequired = true,
                                onClick = { viewModel.toggleDistrictDropdown() },
                                colors = colors
                            )
                        } else if (!uiState.isTurkey) {
                            ModernTextField(
                                value = uiState.manualDistrict,
                                onValueChange = { viewModel.updateRegisterField("manualDistrict", it) },
                                label = "İlçe/Bölge",
                                placeholder = "İlçe adı",
                                leadingIcon = Icons.Outlined.Map,
                                isRequired = true,
                                isError = uiState.errors["district"] != null,
                                errorMessage = uiState.errors["district"],
                                colors = colors,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }

                        // Adres
                        ModernTextField(
                            value = uiState.address,
                            onValueChange = { viewModel.updateRegisterField("address", it) },
                            label = "Adres",
                            placeholder = "Açık adresiniz",
                            leadingIcon = Icons.Outlined.Home,
                            isRequired = true,
                            isError = uiState.errors["address"] != null,
                            errorMessage = uiState.errors["address"],
                            colors = colors,
                            singleLine = false,
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )

                        // Adres (Devamı) - İsteğe bağlı
                        ModernTextField(
                            value = uiState.address2,
                            onValueChange = { viewModel.updateRegisterField("address2", it) },
                            label = "Adres (Devamı)",
                            placeholder = "Apartman, daire, kat vb. (İsteğe bağlı)",
                            leadingIcon = Icons.Outlined.Home,
                            isRequired = false,
                            isError = false,
                            errorMessage = null,
                            colors = colors,
                            singleLine = false,
                            maxLines = 2,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )

                        // Posta Kodu
                        ModernTextField(
                            value = uiState.zipCode,
                            onValueChange = { viewModel.updateRegisterField("zipCode", it) },
                            label = "Posta Kodu",
                            placeholder = "34000",
                            leadingIcon = Icons.Outlined.MarkunreadMailbox,
                            isRequired = true,
                            isError = uiState.errors["zipCode"] != null,
                            errorMessage = uiState.errors["zipCode"],
                            colors = colors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                    }
                }

                // Corporate Info Card (sadece kurumsal için)
                AnimatedVisibility(
                    visible = uiState.isCorporate,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(20.dp),
                                ambientColor = Color.Black.copy(alpha = 0.05f)
                            ),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.AuthCardBackground)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            SectionHeader(
                                title = "Kurumsal Bilgiler",
                                icon = Icons.Outlined.Business,
                                colors = colors
                            )

                            ModernTextField(
                                value = uiState.companyName,
                                onValueChange = { viewModel.updateRegisterField("companyName", it) },
                                label = "Firma Adı",
                                placeholder = "Şirket unvanı",
                                leadingIcon = Icons.Outlined.Business,
                                isRequired = true,
                                isError = uiState.errors["companyName"] != null,
                                errorMessage = uiState.errors["companyName"],
                                colors = colors,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            ModernTextField(
                                value = uiState.taxNumber,
                                onValueChange = { viewModel.updateRegisterField("taxNumber", it) },
                                label = "Vergi Numarası",
                                placeholder = "10 haneli vergi no",
                                leadingIcon = Icons.Outlined.Numbers,
                                isRequired = true,
                                isError = uiState.errors["taxNumber"] != null,
                                errorMessage = uiState.errors["taxNumber"],
                                colors = colors,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )

                            ModernTextField(
                                value = uiState.taxOffice,
                                onValueChange = { viewModel.updateRegisterField("taxOffice", it) },
                                label = "Vergi Dairesi",
                                placeholder = "Vergi dairesi adı",
                                leadingIcon = Icons.Outlined.AccountBalance,
                                isRequired = true,
                                isError = uiState.errors["taxOffice"] != null,
                                errorMessage = uiState.errors["taxOffice"],
                                colors = colors,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                        }
                    }
                }

                // Security Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.05f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.AuthCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SectionHeader(
                            title = "Hesap Güvenliği",
                            icon = Icons.Outlined.Security,
                            colors = colors
                        )

                        ModernTextField(
                            value = uiState.password,
                            onValueChange = { viewModel.updateRegisterField("password", it) },
                            label = "Şifre",
                            placeholder = "En az 8 karakter",
                            leadingIcon = Icons.Outlined.Lock,
                            isRequired = true,
                            isPassword = true,
                            isPasswordVisible = uiState.isPasswordVisible,
                            onPasswordVisibilityToggle = viewModel::toggleRegisterPasswordVisibility,
                            isError = uiState.errors["password"] != null,
                            errorMessage = uiState.errors["password"],
                            colors = colors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )

                        ModernTextField(
                            value = uiState.passwordConfirm,
                            onValueChange = { viewModel.updateRegisterField("passwordConfirm", it) },
                            label = "Şifre Tekrar",
                            placeholder = "Şifrenizi tekrar girin",
                            leadingIcon = Icons.Outlined.Lock,
                            isRequired = true,
                            isPassword = true,
                            isPasswordVisible = uiState.isPasswordVisible,
                            onPasswordVisibilityToggle = viewModel::toggleRegisterPasswordVisibility,
                            isError = uiState.errors["passwordConfirm"] != null,
                            errorMessage = uiState.errors["passwordConfirm"],
                            colors = colors,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )

                        // Password strength indicator
                        if (uiState.password.isNotEmpty()) {
                            PasswordStrengthIndicator(
                                password = uiState.password,
                                colors = colors
                            )
                        }
                    }
                }

                // Contracts Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color.Black.copy(alpha = 0.05f)
                        ),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.AuthCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SectionHeader(
                            title = "Sözleşmeler",
                            icon = Icons.Outlined.Description,
                            colors = colors
                        )

                        if (uiState.isLoadingContracts) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = colors.AuthButtonGradientStart,
                                    strokeWidth = 3.dp
                                )
                            }
                        } else if (uiState.contractsError != null) {
                            // Hata durumu
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = colors.Error,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Sözleşmeler yüklenemedi",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.Error,
                                    textAlign = TextAlign.Center
                                )
                                OutlinedButton(
                                    onClick = { viewModel.retryLoadContracts() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colors.AuthButtonGradientStart
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Yeniden Dene")
                                }
                            }
                        } else if (uiState.contracts.isEmpty()) {
                            // Sözleşme bulunamadı
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Description,
                                    contentDescription = null,
                                    tint = colors.AuthHintText,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = "Sözleşme bulunamadı",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.AuthHintText,
                                    textAlign = TextAlign.Center
                                )
                                OutlinedButton(
                                    onClick = { viewModel.retryLoadContracts() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = colors.AuthButtonGradientStart
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Yeniden Dene")
                                }
                            }
                        } else {
                            uiState.contracts.forEach { contractAcceptance ->
                                ModernContractCheckbox(
                                    contract = contractAcceptance.contract,
                                    isAccepted = contractAcceptance.isAccepted,
                                    onClick = { viewModel.openContractDialog(contractAcceptance.contract) },
                                    colors = colors
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Register Button
                Button(
                    onClick = viewModel::onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    enabled = !uiState.isLoading && uiState.allContractsAccepted,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = colors.AuthDivider
                    ),
                    contentPadding = PaddingValues()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if (!uiState.isLoading && uiState.allContractsAccepted) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            colors.AuthButtonGradientStart,
                                            colors.AuthButtonGradientEnd
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            colors.AuthDivider,
                                            colors.AuthDivider
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Kayıt Ol",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Login Link
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zaten hesabınız var mı?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.AuthSubHeaderText
                    )
                    TextButton(
                        onClick = onNavigateToLogin,
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Text(
                            text = "Giriş Yap",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.AuthLinkText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Dropdown Dialogs
        if (uiState.showCountryDropdown) {
            ModernSearchableDropdownDialog(
                title = "Ülke Seçin",
                searchQuery = uiState.countrySearchQuery,
                onSearchQueryChange = { viewModel.updateCountrySearchQuery(it) },
                items = uiState.filteredCountries,
                onItemClick = { country ->
                    viewModel.selectCountry(country)
                },
                onDismiss = { viewModel.toggleCountryDropdown() },
                colors = colors
            ) { country ->
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.AuthHeaderText
                )
            }
        }

        if (uiState.showProvinceDropdown) {
            ModernSearchableDropdownDialog(
                title = "İl Seçin",
                searchQuery = uiState.provinceSearchQuery,
                onSearchQueryChange = { viewModel.updateProvinceSearchQuery(it) },
                items = uiState.filteredProvinces,
                onItemClick = { province ->
                    viewModel.selectProvince(province)
                },
                onDismiss = { viewModel.toggleProvinceDropdown() },
                colors = colors
            ) { province ->
                Text(
                    text = province.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.AuthHeaderText
                )
            }
        }

        if (uiState.showDistrictDropdown) {
            ModernSearchableDropdownDialog(
                title = "İlçe Seçin",
                searchQuery = uiState.districtSearchQuery,
                onSearchQueryChange = { viewModel.updateDistrictSearchQuery(it) },
                items = uiState.searchedDistricts,
                onItemClick = { district ->
                    viewModel.selectDistrict(district)
                },
                onDismiss = { viewModel.toggleDistrictDropdown() },
                colors = colors
            ) { district ->
                Text(
                    text = district.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.AuthHeaderText
                )
            }
        }
    }
}

// ============================================
// MODERN COMPONENTS
// ============================================

@Composable
private fun ModernRegisterTopBar(
    onBackClick: () -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Geri",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Hesap Oluştur",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.AuthButtonGradientStart.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.AuthButtonGradientStart,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = colors.AuthHeaderText
        )
    }
}

@Composable
private fun ModernAccountTypeChip(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.AuthButtonGradientStart else colors.AuthInputBorder,
                shape = RoundedCornerShape(14.dp)
            ),
        color = if (isSelected) colors.AuthButtonGradientStart.copy(alpha = 0.08f) else colors.AuthInputBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.AuthButtonGradientStart else colors.AuthHintText,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) colors.AuthButtonGradientStart else colors.AuthHeaderText
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.AuthHintText
            )
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    isRequired: Boolean = false,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        Row(modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (isError) colors.Error else colors.AuthHeaderText
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.Error
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = colors.AuthHintText
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) colors.Error else colors.AuthHintText,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onPasswordVisibilityToggle?.invoke() }) {
                        Icon(
                            imageVector = if (isPasswordVisible)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = colors.AuthHintText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !isPasswordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.AuthInputBackground,
                unfocusedContainerColor = colors.AuthInputBackground,
                errorContainerColor = colors.AuthInputBackground,
                focusedBorderColor = colors.AuthInputFocusedBorder,
                unfocusedBorderColor = colors.AuthInputBorder,
                errorBorderColor = colors.Error,
                focusedTextColor = colors.AuthHeaderText,
                unfocusedTextColor = colors.AuthHeaderText,
                cursorColor = colors.AuthInputFocusedBorder
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )

        // Error Message
        AnimatedVisibility(
            visible = isError && errorMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ErrorOutline,
                    contentDescription = null,
                    tint = colors.Error,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = errorMessage ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.Error
                )
            }
        }
    }
}

@Composable
private fun ModernDropdownField(
    value: String,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isRequired: Boolean = false,
    onClick: () -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.AuthHeaderText
            )
            if (isRequired) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.Error
                )
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .border(
                    width = 1.dp,
                    color = colors.AuthInputBorder,
                    shape = RoundedCornerShape(12.dp)
                ),
            color = colors.AuthInputBackground
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = colors.AuthHintText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = value.ifEmpty { placeholder },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isEmpty()) colors.AuthHintText else colors.AuthHeaderText,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.AuthHintText
                )
            }
        }
    }
}

@Composable
private fun ModernContractCheckbox(
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
                color = if (isAccepted) colors.Success else colors.AuthInputBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        color = if (isAccepted) colors.Success.copy(alpha = 0.08f) else colors.AuthInputBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isAccepted,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(
                    checkedColor = colors.Success,
                    uncheckedColor = colors.AuthInputBorder
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = contract.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.AuthHeaderText,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Oku",
                tint = colors.AuthHintText
            )
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(
    password: String,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    val strength = calculatePasswordStrength(password)
    val (label, color, progress) = when {
        strength < 2 -> Triple("Zayıf", colors.Error, 0.25f)
        strength < 3 -> Triple("Orta", colors.Warning, 0.5f)
        strength < 4 -> Triple("Güçlü", colors.Info, 0.75f)
        else -> Triple("Çok Güçlü", colors.Success, 1f)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Şifre Gücü",
                style = MaterialTheme.typography.bodySmall,
                color = colors.AuthHintText
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = colors.AuthInputBorder
        )
    }
}

private fun calculatePasswordStrength(password: String): Int {
    var strength = 0
    if (password.length >= 8) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isLowerCase() }) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { !it.isLetterOrDigit() }) strength++
    return strength
}

@Composable
private fun <T> ModernSearchableDropdownDialog(
    title: String,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    items: List<T>,
    onItemClick: (T) -> Unit,
    onDismiss: () -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors,
    itemContent: @Composable (T) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(20.dp),
            color = colors.AuthCardBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.AuthHeaderText
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.AuthInputBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = colors.AuthHintText
                        )
                    }
                }

                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Ara...", color = colors.AuthHintText) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = colors.AuthHintText
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = colors.AuthInputBackground,
                        unfocusedContainerColor = colors.AuthInputBackground,
                        focusedBorderColor = colors.AuthInputFocusedBorder,
                        unfocusedBorderColor = colors.AuthInputBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.AuthDivider)

                // List
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items) { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(item) }
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            color = Color.Transparent
                        ) {
                            itemContent(item)
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            color = colors.AuthDivider.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernContractDialog(
    contract: Contract,
    isScrolledToEnd: Boolean,
    onScrolledToEnd: () -> Unit,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
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
            shape = RoundedCornerShape(20.dp),
            color = colors.AuthCardBackground
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = contract.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.AuthHeaderText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.AuthInputBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = colors.AuthHintText
                        )
                    }
                }

                HorizontalDivider(color = colors.AuthDivider)

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
                            .padding(20.dp)
                    ) {
                        val plainText = contract.content
                            .replace(Regex("<[^>]*>"), "")
                            .replace("&nbsp;", " ")
                            .trim()

                        Text(
                            text = plainText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.AuthHeaderText,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                        )
                    }
                }

                HorizontalDivider(color = colors.AuthDivider)

                // Footer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                colors = listOf(colors.AuthInputBorder, colors.AuthInputBorder)
                            )
                        )
                    ) {
                        Text(
                            text = "Kapat",
                            color = colors.AuthHeaderText
                        )
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        enabled = isScrolledToEnd,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.AuthButtonGradientStart,
                            disabledContainerColor = colors.AuthDivider
                        )
                    ) {
                        Text(
                            text = "Kabul Et",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}