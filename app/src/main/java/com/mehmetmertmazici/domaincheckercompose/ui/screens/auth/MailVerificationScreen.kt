package com.mehmetmertmazici.domaincheckercompose.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthEffect
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

@Composable
fun MailVerificationScreen(
    viewModel: AuthViewModel,
    onNavigateToHome: () -> Unit,
    onBackClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val uiState by viewModel.mailVerificationState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // Countdown başlat
    LaunchedEffect(Unit) {
        viewModel.startVerificationCountdown()
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

    // 6 hane tamamlandığında otomatik doğrula (sadece ilk denemede, hata olmadıysa)
    LaunchedEffect(uiState.verificationCode) {
        if (uiState.verificationCode.length == 6 && !uiState.isLoading && !uiState.hasFailedAttempt) {
            keyboardController?.hide()
            delay(300) // Küçük bir gecikme ile UX iyileştirme
            viewModel.verifyMailCode()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.AuthBackground)
    ) {
        // Decorative top gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
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
            // Top Bar
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
                    text = "E-posta Doğrulama",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Icon
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(
                            elevation = 16.dp,
                            shape = CircleShape,
                            ambientColor = colors.AuthButtonGradientStart.copy(alpha = 0.3f),
                            spotColor = colors.AuthButtonGradientStart.copy(alpha = 0.3f)
                        )
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.MarkEmailUnread,
                        contentDescription = null,
                        tint = colors.AuthButtonGradientStart,
                        modifier = Modifier.size(45.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Title
                Text(
                    text = "Doğrulama Kodu Gönderildi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${uiState.email} adresine gönderilen 6 haneli doğrulama kodunu girin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Form Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.AuthCardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Modern OTP Input
                        Text(
                            text = "Doğrulama Kodu",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.AuthHeaderText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        ModernOtpInput(
                            otpValue = uiState.verificationCode,
                            onOtpChange = { newValue ->
                                viewModel.updateVerificationCode(newValue)
                            },
                            isError = uiState.codeError != null,
                            colors = colors
                        )

                        // Error Message
                        AnimatedVisibility(
                            visible = uiState.codeError != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = colors.Error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = uiState.codeError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.Error
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // Verify Button
                        Button(
                            onClick = viewModel::verifyMailCode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            enabled = !uiState.isLoading && uiState.verificationCode.length == 6,
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
                                        brush = if (!uiState.isLoading && uiState.verificationCode.length == 6) {
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
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Doğrula",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Resend Section - Sadece Register'dan gelenler için göster
                        if (!uiState.isFromLogin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (uiState.canResend) {
                                    TextButton(
                                        onClick = viewModel::resendVerificationCode,
                                        enabled = !uiState.isLoading
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp),
                                            tint = colors.AuthLinkText
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Kodu Tekrar Gönder",
                                            color = colors.AuthLinkText,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    // Timer badge
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = colors.AuthInputBackground,
                                        modifier = Modifier
                                            .border(
                                                width = 1.dp,
                                                color = colors.AuthInputBorder,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Timer,
                                                contentDescription = null,
                                                tint = colors.AuthHintText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Yeni kod için: ${formatTime(uiState.remainingTime)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = colors.AuthSubHeaderText,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colors.Info.copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = colors.Info,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Kod gelmedi mi? Spam klasörünü kontrol edin veya birkaç dakika bekleyip tekrar deneyin.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.AuthSubHeaderText,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.4
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// MODERN OTP INPUT COMPONENT
// ============================================

@Composable
private fun ModernOtpInput(
    otpValue: String,
    onOtpChange: (String) -> Unit,
    isError: Boolean,
    colors: AppColors,
    otpLength: Int = 6
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Otomatik focus
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Hidden TextField for input handling
    BasicTextField(
        value = otpValue,
        onValueChange = { newValue ->
            if (newValue.length <= otpLength && newValue.all { it.isDigit() }) {
                onOtpChange(newValue)
            }
        },
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() }
        ),
        singleLine = true,
        cursorBrush = SolidColor(Color.Transparent),
        decorationBox = { _ ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                repeat(otpLength) { index ->
                    val char = otpValue.getOrNull(index)?.toString() ?: ""
                    val isFocused = otpValue.length == index
                    val isFilled = char.isNotEmpty()

                    OtpDigitBox(
                        digit = char,
                        isFocused = isFocused,
                        isFilled = isFilled,
                        isError = isError,
                        colors = colors,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    isFilled: Boolean,
    isError: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    // Animasyonlar
    val scale by animateFloatAsState(
        targetValue = if (isFilled) 1.05f else 1f,
        animationSpec = tween(150),
        label = "scale"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> colors.Error
            isFocused -> colors.AuthButtonGradientStart
            isFilled -> colors.Success
            else -> colors.AuthInputBorder
        },
        animationSpec = tween(200),
        label = "borderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isError -> colors.Error.copy(alpha = 0.05f)
            isFilled -> colors.Success.copy(alpha = 0.05f)
            isFocused -> colors.AuthButtonGradientStart.copy(alpha = 0.05f)
            else -> colors.AuthInputBackground
        },
        animationSpec = tween(200),
        label = "backgroundColor"
    )

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = if (isFocused || isFilled) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (digit.isNotEmpty()) {
            Text(
                text = digit,
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isError) colors.Error else colors.AuthHeaderText,
                    textAlign = TextAlign.Center
                )
            )
        } else if (isFocused) {
            // Yanıp sönen cursor
            val cursorVisible = remember { mutableStateOf(true) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(500)
                    cursorVisible.value = !cursorVisible.value
                }
            }
            if (cursorVisible.value) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(
                            colors.AuthButtonGradientStart,
                            RoundedCornerShape(1.dp)
                        )
                )
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%d:%02d", minutes, secs)
}