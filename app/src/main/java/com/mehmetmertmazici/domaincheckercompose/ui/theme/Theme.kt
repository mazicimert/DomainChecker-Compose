package com.mehmetmertmazici.domaincheckercompose.ui.theme


import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = LightColors.Primary,
    onPrimary = LightColors.OnPrimary,
    primaryContainer = LightColors.PrimaryLight,
    onPrimaryContainer = LightColors.PrimaryDark,
    secondary = LightColors.Secondary,
    onSecondary = LightColors.OnSecondary,
    secondaryContainer = LightColors.SecondaryVariant,
    tertiary = LightColors.Accent,
    onTertiary = LightColors.OnPrimary,
    background = LightColors.Surface,
    onBackground = LightColors.OnBackground,
    surface = LightColors.Surface,
    onSurface = LightColors.OnSurface,
    surfaceVariant = LightColors.SurfaceVariant,
    onSurfaceVariant = LightColors.OnSurfaceVariant,
    outline = LightColors.Outline,
    outlineVariant = LightColors.OutlineVariant,
    error = LightColors.Error,
    onError = LightColors.OnPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkColors.Primary,
    onPrimary = DarkColors.OnPrimary,
    primaryContainer = DarkColors.PrimaryDark,
    onPrimaryContainer = DarkColors.PrimaryLight,
    secondary = DarkColors.Secondary,
    onSecondary = DarkColors.OnSecondary,
    secondaryContainer = DarkColors.SecondaryVariant,
    tertiary = DarkColors.Accent,
    onTertiary = DarkColors.OnPrimary,
    background = DarkColors.Surface,
    onBackground = DarkColors.OnBackground,
    surface = DarkColors.Surface,
    onSurface = DarkColors.OnSurface,
    surfaceVariant = DarkColors.SurfaceVariant,
    onSurfaceVariant = DarkColors.OnSurfaceVariant,
    outline = DarkColors.Outline,
    outlineVariant = DarkColors.OutlineVariant,
    error = DarkColors.Error,
    onError = DarkColors.OnPrimary
)

@Composable
fun DomainCheckerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Gradient colors helper
@Composable
fun gradientColors(): List<Color> {
    val colors = if (isSystemInDarkTheme()) DarkColors else LightColors
    return listOf(
        colors.GradientStart,
        colors.GradientCenter,
        colors.GradientEnd
    )
}

@Composable
fun getTextFieldColors(colors: AppColors): TextFieldColors {
    // Modern, temiz ve okunabilir input renkleri
    return OutlinedTextFieldDefaults.colors(
        // Zemin: Hafif gri/yumuşak ton (Flat tasarım hissi)
        focusedContainerColor = colors.SurfaceVariant.copy(alpha = 0.5f),
        unfocusedContainerColor = colors.SurfaceVariant.copy(alpha = 0.3f),
        disabledContainerColor = colors.SurfaceVariant.copy(alpha = 0.1f),
        errorContainerColor = colors.Error.copy(alpha = 0.1f),

        // Çerçeve: Odaklanılmadığında çok silik veya yok, odaklanınca belirgin
        focusedBorderColor = colors.Primary,
        unfocusedBorderColor = colors.Outline.copy(alpha = 0.3f), // Daha silik çerçeve
        errorBorderColor = colors.Error,
        disabledBorderColor = colors.Outline.copy(alpha = 0.1f),

        // Yazı renkleri
        focusedTextColor = colors.TextPrimary,
        unfocusedTextColor = colors.TextPrimary,
        disabledTextColor = colors.TextTertiary,
        errorTextColor = colors.Error,

        // Label (Etiket) renkleri
        focusedLabelColor = colors.Primary,
        unfocusedLabelColor = colors.TextSecondary,
        disabledLabelColor = colors.TextTertiary,
        errorLabelColor = colors.Error,

        // Placeholder
        focusedPlaceholderColor = colors.TextTertiary,
        unfocusedPlaceholderColor = colors.TextTertiary.copy(alpha = 0.7f),

        // İkonlar
        focusedLeadingIconColor = colors.Primary,
        unfocusedLeadingIconColor = colors.TextSecondary,
        focusedTrailingIconColor = colors.TextSecondary,
        unfocusedTrailingIconColor = colors.TextSecondary,

        cursorColor = colors.Primary
    )
}