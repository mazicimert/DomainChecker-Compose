package com.mehmetmertmazici.domaincheckercompose.ui.screens.checkout

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mehmetmertmazici.domaincheckercompose.model.PaymentMethod
import com.mehmetmertmazici.domaincheckercompose.ui.components.AnimatedListItem
import com.mehmetmertmazici.domaincheckercompose.ui.components.GradientBackground
import com.mehmetmertmazici.domaincheckercompose.ui.components.scaleClick
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.ui.util.CreditCardVisualTransformation
import com.mehmetmertmazici.domaincheckercompose.ui.util.ExpiryDateVisualTransformation
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CheckoutEffect
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CheckoutUiState
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CheckoutViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormat

@Composable
fun CheckoutScreen(
    viewModel: CheckoutViewModel,
    onBackClick: () -> Unit,
    onNavigateToSuccess: (invoiceId: Int, status: String) -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Effect handling
    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is CheckoutEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CheckoutEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is CheckoutEffect.NavigateToSuccess -> {
                    onNavigateToSuccess(effect.invoiceId, effect.status)
                }
                is CheckoutEffect.NavigateBack -> {
                    onBackClick()
                }
            }
        }
    }

    GradientBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Bar
                CheckoutTopBar(
                    onBackClick = onBackClick,
                    isDarkTheme = isDarkTheme
                )

                // Scrollable Content
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (uiState.isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = colors.Primary)
                        }
                    } else {
                        CheckoutContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

            // Floating Bottom Summary Bar
            if (!uiState.isLoading) {
                OrderSummaryBar(
                    uiState = uiState,
                    onCheckoutClick = { viewModel.completeOrder() },
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // Loading Overlay for submission
            if (uiState.isSubmitting) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.Surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = colors.Primary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Siparişiniz işleniyor...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.TextPrimary
                            )
                        }
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
private fun CheckoutTopBar(
    onBackClick: () -> Unit,
    isDarkTheme: Boolean
) {

    val colors = if (isDarkTheme) DarkColors else LightColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.SurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(44.dp),
            onClick = onBackClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri",
                    tint = colors.TextPrimary
                )
            }
        }

        // Title
        Text(
            text = "Ödeme",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        // Balance spacer
        Spacer(modifier = Modifier.size(44.dp))
    }
}

// ============================================
// CHECKOUT CONTENT
// ============================================
@Composable
private fun CheckoutContent(
    uiState: CheckoutUiState,
    viewModel: CheckoutViewModel,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Payment Method Selection
        AnimatedListItem {
            PaymentMethodSection(
                paymentMethods = uiState.paymentMethods,
                selectedMethod = uiState.selectedPaymentMethod,
                onSelectMethod = { viewModel.selectPaymentMethod(it) },
                isDarkTheme = isDarkTheme
            )
        }

        // Dynamic Content based on selected payment method
        AnimatedVisibility(
            visible = uiState.isStripeSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            AnimatedListItem(delayMillis = 100) {
                StripePaymentForm(
                    cardInput = uiState.cardInput,
                    onCardNameChange = { viewModel.updateCardName(it) },
                    onCardNumberChange = { viewModel.updateCardNumber(it) },
                    onExpiryChange = { viewModel.updateCardExpiry(it) },
                    onCvvChange = { viewModel.updateCardCvv(it) },
                    isDarkTheme = isDarkTheme
                )
            }
        }

        AnimatedVisibility(
            visible = uiState.isBankTransferSelected,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            AnimatedListItem(delayMillis = 100) {
                BankTransferInfo(
                    bankInfo = uiState.bankTransferInfo,
                    iban = uiState.iban,
                    isDarkTheme = isDarkTheme
                )
            }
        }

        // Error Display
        uiState.error?.let { error ->
            AnimatedListItem {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.Error.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
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
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.Error
                        )
                    }
                }
            }
        }

        // Bottom spacing for summary bar
        Spacer(modifier = Modifier.height(180.dp))
    }
}

// ============================================
// PAYMENT METHOD SECTION
// ============================================
@Composable
private fun PaymentMethodSection(
    paymentMethods: List<PaymentMethod>,
    selectedMethod: PaymentMethod?,
    onSelectMethod: (PaymentMethod) -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Column {
        Text(
            text = "Ödeme Yöntemi",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            paymentMethods.forEach { method ->
                val isSelected = selectedMethod?.module == method.module

                PaymentMethodCard(
                    method = method,
                    isSelected = isSelected,
                    onClick = { onSelectMethod(method) },
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PaymentMethodCard(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) colors.Primary else colors.Outline.copy(alpha = 0.2f)
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) colors.Primary.copy(alpha = 0.1f) else colors.Surface.copy(alpha = 0.8f)
    )

    val icon = when (method.module) {
        "stripe" -> Icons.Default.CreditCard
        "banktransfer" -> Icons.Default.AccountBalance
        else -> Icons.Default.Payment
    }

    Card(
        modifier = modifier
            .height(100.dp)
            .shadow(
                elevation = if (isSelected) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isSelected) colors.Primary.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.1f)
            )
            .scaleClick(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(2.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) colors.Primary else colors.TextSecondary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = method.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) colors.Primary else colors.TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ============================================
// STRIPE PAYMENT FORM
// ============================================
@Composable
private fun StripePaymentForm(
    cardInput: com.mehmetmertmazici.domaincheckercompose.viewmodel.CardInputState,
    onCardNameChange: (String) -> Unit,
    onCardNumberChange: (String) -> Unit,
    onExpiryChange: (String) -> Unit,
    onCvvChange: (String) -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = colors.Primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.Surface.copy(alpha = 0.9f)
        ),
        border = BorderStroke(1.dp, colors.Outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = null,
                    tint = colors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Kart Bilgileri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextPrimary
                )
            }

            HorizontalDivider(color = colors.Outline.copy(alpha = 0.1f))

            // Card Holder Name
            PaymentTextField(
                value = cardInput.cardName,
                onValueChange = onCardNameChange,
                label = "Kart Sahibi",
                placeholder = "Ad Soyad",
                leadingIcon = Icons.Default.Person,
                isDarkTheme = isDarkTheme
            )

            // Card Number with VisualTransformation and Error
            CardNumberField(
                value = cardInput.cardNumber,
                onValueChange = onCardNumberChange,
                errorMessage = cardInput.cardNumberError,
                isDarkTheme = isDarkTheme
            )

            // Expiry and CVV Row
            val cvvFocusRequester = remember { FocusRequester() }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Son Kullanma - with VisualTransformation and Error
                ExpiryDateField(
                    value = cardInput.cardExpiry,
                    onValueChange = onExpiryChange,
                    errorMessage = cardInput.cardExpiryError,
                    onComplete = {
                        try { cvvFocusRequester.requestFocus() } catch (_: Exception) {}
                    },
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.weight(1f)
                )

                // CVV with Error
                CvvField(
                    value = cardInput.cardCvv,
                    onValueChange = onCvvChange,
                    errorMessage = cardInput.cardCvvError,
                    isDarkTheme = isDarkTheme,
                    focusRequester = cvvFocusRequester,
                    modifier = Modifier.weight(1f)
                )
            }

            // Security note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colors.Success.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = colors.Success,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ödemeleriniz SSL ile güvendedir.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.Success
                )
            }
        }
    }
}

@Composable
private fun PaymentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    isDarkTheme: Boolean,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, color = colors.TextTertiary) },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.Primary,
            unfocusedBorderColor = colors.Outline.copy(alpha = 0.3f),
            focusedLabelColor = colors.Primary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// ============================================
// CARD NUMBER FIELD - with VisualTransformation
// ============================================
@Composable
private fun CardNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val isError = errorMessage != null

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Kart Numarası") },
        placeholder = { Text("**** **** **** ****", color = colors.TextTertiary) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.CreditCard,
                contentDescription = null,
                tint = if (isError) colors.Error else colors.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        },
        isError = isError,
        supportingText = if (isError) {
            { Text(errorMessage!!, color = colors.Error) }
        } else null,
        visualTransformation = CreditCardVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = if (isError) colors.Error else colors.Primary,
            unfocusedBorderColor = if (isError) colors.Error.copy(alpha = 0.5f) else colors.Outline.copy(alpha = 0.3f),
            focusedLabelColor = if (isError) colors.Error else colors.Primary,
            errorBorderColor = colors.Error
        ),
        modifier = modifier.fillMaxWidth()
    )
}

// ============================================
// EXPIRY DATE FIELD - with VisualTransformation and Error
// ============================================
@Composable
private fun ExpiryDateField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    onComplete: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val isError = errorMessage != null

    Column(modifier = modifier) {
        // Kutu üstü etiket
        Text(
            text = "Son Kullanma",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (isError) colors.Error else colors.TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = { newValue ->
                // Filter to only digits and limit to 4
                val digitsOnly = newValue.filter { it.isDigit() }.take(4)
                onValueChange(digitsOnly)
                
                // Auto-advance to CVV when all 4 digits entered
                if (digitsOnly.length == 4 && digitsOnly.length > value.length) {
                    onComplete()
                }
            },
            // No placeholder needed - mask shows AA/YY template
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = if (isError) colors.Error else colors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            isError = isError,
            supportingText = if (isError) {
                { Text(errorMessage!!, color = colors.Error, style = MaterialTheme.typography.labelSmall) }
            } else null,
            visualTransformation = ExpiryDateVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) colors.Error else colors.Primary,
                unfocusedBorderColor = if (isError) colors.Error.copy(alpha = 0.5f) else colors.Outline.copy(alpha = 0.3f),
                errorBorderColor = colors.Error
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================
// CVV FIELD - with Error
// ============================================
@Composable
private fun CvvField(
    value: String,
    onValueChange: (String) -> Unit,
    errorMessage: String?,
    isDarkTheme: Boolean,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val isError = errorMessage != null

    Column(modifier = modifier) {
        // Kutu üstü etiket (ExpiryDateField ile aynı stil)
        Text(
            text = "CVV",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = if (isError) colors.Error else colors.TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text("123", color = colors.TextTertiary)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (isError) colors.Error else colors.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            },
            isError = isError,
            supportingText = if (isError) {
                { Text(errorMessage!!, color = colors.Error, style = MaterialTheme.typography.labelSmall) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (isError) colors.Error else colors.Primary,
                unfocusedBorderColor = if (isError) colors.Error.copy(alpha = 0.5f) else colors.Outline.copy(alpha = 0.3f),
                errorBorderColor = colors.Error
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )
    }
}

// ============================================
// BANK TRANSFER INFO
// ============================================
@Composable
private fun BankTransferInfo(
    bankInfo: String,
    iban: String?,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = colors.Primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.Surface.copy(alpha = 0.9f)
        ),
        border = BorderStroke(1.dp, colors.Outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = colors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Banka Havale Bilgileri",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextPrimary
                )
            }

            HorizontalDivider(color = colors.Outline.copy(alpha = 0.1f))

            // Bank Info Text - Glassmorphism box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                colors.Primary.copy(alpha = 0.05f),
                                colors.Primary.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = bankInfo.replace("\\n", "\n"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextPrimary,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.4
                )
            }

            // Copy IBAN Button
            iban?.let { ibanValue ->
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(ibanValue))
                        Toast.makeText(context, "IBAN kopyalandı", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.Primary.copy(alpha = 0.15f),
                        contentColor = colors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "IBAN'ı Kopyala",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        colors.Warning.copy(alpha = 0.1f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = colors.Warning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Havale açıklamasına fatura numaranızı yazmayı unutmayın.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.Warning
                )
            }
        }
    }
}

// ============================================
// ORDER SUMMARY BAR (FLOATING)
// ============================================
@Composable
private fun OrderSummaryBar(
    uiState: CheckoutUiState,
    onCheckoutClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val priceFormat = remember { DecimalFormat("#,##0.00") }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.2f),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        color = colors.Surface.copy(alpha = 0.98f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, colors.Outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Price breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Ara Toplam",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextSecondary
                )
                Text(
                    text = "${uiState.currencySymbol}${priceFormat.format(uiState.subtotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextPrimary
                )
            }

            if (uiState.taxRate > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "KDV (%${uiState.taxRate.toInt()})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.TextSecondary
                    )
                    Text(
                        text = "+${uiState.currencySymbol}${priceFormat.format(uiState.taxAmount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = colors.Outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

            // Grand Total and Checkout Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Toplam",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.TextTertiary
                    )
                    Text(
                        text = "${uiState.currencySymbol}${priceFormat.format(uiState.grandTotal)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.Primary
                    )
                }

                Button(
                    onClick = onCheckoutClick,
                    enabled = uiState.canSubmit,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.Primary,
                        contentColor = Color.White,
                        disabledContainerColor = colors.Primary.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Siparişi Tamamla",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}