package com.mehmetmertmazici.domaincheckercompose.ui.theme

import androidx.compose.ui.graphics.Color

// İsimKayıt Brand Colors - Light Theme
class AppColors(
    val Primary: Color,
    val PrimaryDark: Color,
    val PrimaryVariant: Color,
    val PrimaryLight: Color,
    val OnPrimary: Color,
    val Secondary: Color,
    val SecondaryVariant: Color,
    val OnSecondary: Color,
    val Accent: Color,
    val AccentLight: Color,
    val Surface: Color,
    val SurfaceVariant: Color,
    val OnBackground: Color,
    val OnSurface: Color,
    val OnSurfaceVariant: Color,
    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextTertiary: Color,
    val Outline: Color,
    val OutlineVariant: Color,
    val StatusAvailable: Color,
    val StatusAvailableLight: Color,
    val StatusAvailableDark: Color,
    val StatusRegistered: Color,
    val StatusRegisteredLight: Color,
    val StatusRegisteredDark: Color,
    val StatusUnknown: Color,
    val StatusUnknownLight: Color,
    val StatusUnknownDark: Color,
    val ChipPopularBg: Color,
    val ChipPopularText: Color,
    val ChipHotBg: Color,
    val ChipHotText: Color,
    val ChipOtherBg: Color,
    val ChipOtherText: Color,
    val ChipDefaultBg: Color,
    val ChipDefaultText: Color,
    val Success: Color,
    val Error: Color,
    val Warning: Color,
    val Info: Color,
    val GradientStart: Color,
    val GradientCenter: Color,
    val GradientEnd: Color,
    val CardBackground: Color,
    val SemiTransparentBlack: Color,
    val SemiTransparentWhite: Color
)

val LightColors = AppColors(
    Primary = Color(0xFF2B9BD1),
    PrimaryDark = Color(0xFF1976D2),
    PrimaryVariant = Color(0xFF0D47A1),
    PrimaryLight = Color(0xFFBBDEFB),
    OnPrimary = Color(0xFFFFFFFF),

    Secondary = Color(0xFF546E7A),
    SecondaryVariant = Color(0xFF37474F),
    OnSecondary = Color(0xFFFFFFFF),

    Accent = Color(0xFF00BCD4),
    AccentLight = Color(0xFF26C6DA),

    Surface = Color(0xFFFFFFFF),
    SurfaceVariant = Color(0xFFF1F5F9),
    OnBackground = Color(0xFF1A202C),
    OnSurface = Color(0xFF1A202C),
    OnSurfaceVariant = Color(0xFF4A5568),

    TextPrimary = Color(0xFF1A202C),
    TextSecondary = Color(0xFF4A5568),
    TextTertiary = Color(0xFF718096),

    Outline = Color(0xFFCBD5E0),
    OutlineVariant = Color(0xFFE2E8F0),

    // Status Colors
    StatusAvailable = Color(0xFF10B981),
    StatusAvailableLight = Color(0xFFD1FAE5),
    StatusAvailableDark = Color(0xFF047857),

    StatusRegistered = Color(0xFFEF4444),
    StatusRegisteredLight = Color(0xFFFEE2E2),
    StatusRegisteredDark = Color(0xFFDC2626),

    StatusUnknown = Color(0xFFF59E0B),
    StatusUnknownLight = Color(0xFFFEF3C7),
    StatusUnknownDark = Color(0xFFD97706),

    // Chip Colors
    ChipPopularBg = Color(0xFFDBEAFE),
    ChipPopularText = Color(0xFF1E40AF),
    ChipHotBg = Color(0xFFFEE2E2),
    ChipHotText = Color(0xFFDC2626),
    ChipOtherBg = Color(0xFFF3F4F6),
    ChipOtherText = Color(0xFF374151),
    ChipDefaultBg = Color(0xFFF1F5F9),
    ChipDefaultText = Color(0xFF475569),

    // Semantic Colors
    Success = Color(0xFF10B981),
    Error = Color(0xFFEF4444),
    Warning = Color(0xFFF59E0B),
    Info = Color(0xFF3B82F6),

    // Gradient
    GradientStart = Color(0xFFA2B8D2),
    GradientCenter = Color(0xFF7596BA),
    GradientEnd = Color(0xFF5A7CA0),

    CardBackground = Color(0xFFFFFFFF),
    SemiTransparentBlack = Color(0x80000000),
    SemiTransparentWhite = Color(0x80FFFFFF)
)

// Dark Theme Colors
val DarkColors = AppColors(
    Primary = Color(0xFF42A5F5),
    PrimaryDark = Color(0xFF1976D2),
    PrimaryVariant = Color(0xFF0D47A1),
    PrimaryLight = Color(0xFF64B5F6),
    OnPrimary = Color(0xFFFFFFFF),

    Secondary = Color(0xFF78909C),
    SecondaryVariant = Color(0xFF455A64),
    OnSecondary = Color(0xFFFFFFFF),

    Accent = Color(0xFF26C6DA),
    AccentLight = Color(0xFF4DD0E1),

    Surface = Color(0xFF1C232A),
    SurfaceVariant = Color(0xFF2A3540),
    OnBackground = Color(0xFFE5E7EB),
    OnSurface = Color(0xFFE5E7EB),
    OnSurfaceVariant = Color(0xFF9CA3AF),

    TextPrimary = Color(0xFFE5E7EB),
    TextSecondary = Color(0xFF9CA3AF),
    TextTertiary = Color(0xFF6B7280),

    Outline = Color(0xFF4A5662),
    OutlineVariant = Color(0xFF3E4A56),

    // Status Colors
    StatusAvailable = Color(0xFF34D399),
    StatusAvailableLight = Color(0xFF064E3B),
    StatusAvailableDark = Color(0xFF10B981),

    StatusRegistered = Color(0xFFF87171),
    StatusRegisteredLight = Color(0xFF7F1D1D),
    StatusRegisteredDark = Color(0xFFEF4444),

    StatusUnknown = Color(0xFFFBBF24),
    StatusUnknownLight = Color(0xFF78350F),
    StatusUnknownDark = Color(0xFFF59E0B),

    // Chip Colors
    ChipPopularBg = Color(0xFF1E3A8A),
    ChipPopularText = Color(0xFF93C5FD),
    ChipHotBg = Color(0xFF7F1D1D),
    ChipHotText = Color(0xFFFECACA),
    ChipOtherBg = Color(0xFF374151),
    ChipOtherText = Color(0xFFD1D5DB),
    ChipDefaultBg = Color(0xFF475569),
    ChipDefaultText = Color(0xFFCBD5E0),

    Success = Color(0xFF34D399),
    Error = Color(0xFFF87171),
    Warning = Color(0xFFFBBF24),
    Info = Color(0xFF60A5FA),

    GradientStart = Color(0xFF9FABB8),
    GradientCenter = Color(0xFF6B7A8A),
    GradientEnd = Color(0xFF3F4B5C),

    CardBackground = Color(0xFF2A3540),
    SemiTransparentBlack = Color(0xCC000000),
    SemiTransparentWhite = Color(0x80FFFFFF)
)