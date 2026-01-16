package com.mehmetmertmazici.domaincheckercompose.ui.screens.auth

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mehmetmertmazici.domaincheckercompose.R
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthEffect
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToVerification: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val uiState by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
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
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Image(
                painter = painterResource(id = R.drawable.ic_isimkayit_logo),
                contentDescription = "İsimKayıt Logo",
                modifier = Modifier
                    .height(70.dp)
                    .padding(horizontal = 24.dp)
                    .shadow(elevation = 20.dp, shape = CircleShape, spotColor = Color.White) // Beyaz gölge/parlama
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Welcome Text
            Text(
                text = "Hoş Geldiniz",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "Hesabınıza giriş yapın",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Login Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(24.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colors.AuthCardBackground
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Email Field
                    AuthTextField(
                        value = uiState.email,
                        onValueChange = viewModel::updateLoginEmail,
                        label = "E-posta",
                        placeholder = "ornek@email.com",
                        leadingIcon = Icons.Outlined.Email,
                        isError = uiState.emailError != null,
                        errorMessage = uiState.emailError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        colors = colors
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Password Field
                    AuthTextField(
                        value = uiState.password,
                        onValueChange = viewModel::updateLoginPassword,
                        label = "Şifre",
                        placeholder = "••••••••",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        isPasswordVisible = uiState.isPasswordVisible,
                        onPasswordVisibilityToggle = viewModel::toggleLoginPasswordVisibility,
                        isError = uiState.passwordError != null,
                        errorMessage = uiState.passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                viewModel.login()
                            }
                        ),
                        colors = colors
                    )

                    // Forgot Password Link
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onNavigateToForgotPassword,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Şifremi Unuttum",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.AuthLinkText,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Login Button
                    Button(
                        onClick = viewModel::login,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = colors.AuthButtonGradientStart.copy(alpha = 0.5f)
                        ),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            colors.AuthButtonGradientStart,
                                            colors.AuthButtonGradientEnd
                                        )
                                    ),
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
                                    Text(
                                        text = "Giriş Yap",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Divider with "veya" text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colors.AuthDivider,
                    thickness = 1.dp
                )
                Text(
                    text = "veya",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.AuthHintText,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = colors.AuthDivider,
                    thickness = 1.dp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Register Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hesabınız yok mu?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.AuthSubHeaderText
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text(
                        text = "Kayıt Ol",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.AuthLinkText
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ============================================
// REUSABLE AUTH TEXT FIELD COMPONENT
// ============================================

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onPasswordVisibilityToggle: (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: com.mehmetmertmazici.domaincheckercompose.ui.theme.AppColors
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = if (isError) colors.Error else colors.AuthHeaderText,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        // Text Field
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
                    modifier = Modifier.size(22.dp)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { onPasswordVisibilityToggle?.invoke() }) {
                        Icon(
                            imageVector = if (isPasswordVisible)
                                Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible)
                                "Şifreyi gizle" else "Şifreyi göster",
                            tint = colors.AuthHintText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !isPasswordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            isError = isError,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                // Container
                focusedContainerColor = colors.AuthInputBackground,
                unfocusedContainerColor = colors.AuthInputBackground,
                errorContainerColor = colors.AuthInputBackground,
                // Border
                focusedBorderColor = colors.AuthInputFocusedBorder,
                unfocusedBorderColor = colors.AuthInputBorder,
                errorBorderColor = colors.Error,
                // Text
                focusedTextColor = colors.AuthHeaderText,
                unfocusedTextColor = colors.AuthHeaderText,
                errorTextColor = colors.AuthHeaderText,
                // Cursor
                cursorColor = colors.AuthInputFocusedBorder,
                errorCursorColor = colors.Error
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
                modifier = Modifier.padding(top = 6.dp, start = 4.dp),
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