package com.mehmetmertmazici.domaincheckercompose.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.window.Popup
import androidx.compose.animation.*
import com.mehmetmertmazici.domaincheckercompose.model.Domain
import com.mehmetmertmazici.domaincheckercompose.model.DomainAddons
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors


@Composable
fun DomainItemCard(
    domain: Domain,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false,
    isInCart: Boolean = false,
    onAddToCart: (() -> Unit)? = null,
    onRemoveFromCart: (() -> Unit)? = null,
    onGoToCart: (() -> Unit)? = null
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    val (statusColor, statusText, statusEmoji) = when (domain.status) {
        "available" -> Triple(colors.StatusAvailable, "Müsait", "😊")
        "registered" -> Triple(colors.StatusRegistered, "Kayıtlı", "😔")
        else -> Triple(colors.StatusUnknown, "Bilinmiyor", "❓")
    }

    // Cart button animation
    val cartButtonColor by animateColorAsState(
        targetValue = if (isInCart) colors.StatusAvailable else colors.Primary,
        animationSpec = tween(300),
        label = "cartButtonColor"
    )

    // Premium Card Implementation
    PremiumCard(
        onClick = onClick,
        modifier = modifier.padding(vertical = 8.dp),
        isHot = domain.status == "available"
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp) // Extra internal padding
        ) {
            // Header: Domain Name and Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon Surface
                IconSurface(
                    backgroundColor = if (isDarkTheme) colors.SurfaceVariant else colors.Primary.copy(alpha = 0.1f),
                    iconColor = if (isDarkTheme) colors.Primary else colors.Primary
                ) {
                    val icon = when (domain.status) {
                        "available" -> Icons.Default.Check
                        "registered" -> Icons.Default.Language
                        else -> Icons.Default.Language
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = domain.domain,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = statusColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(8.dp)
                        ) {}

                        Spacer(modifier = Modifier.width(6.dp))

                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status Emoji Badge
                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = statusEmoji,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Price and Details Section
            domain.price?.let { price ->
                val registerPrice = price.register?.get("1")
                val renewPrice = price.renew?.get("1")

                if (registerPrice != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "€$registerPrice",
                                style = MaterialTheme.typography.displaySmall, // Much larger price
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.Primary
                            )
                            Text(
                                text = "/yıl",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.TextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (renewPrice != null && renewPrice != registerPrice) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Yenileme",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.TextTertiary
                                )
                                Text(
                                    text = "€$renewPrice",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.TextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Categories & Addons
            if ((domain.price?.categories?.isNotEmpty() == true) ||
                (domain.price?.addons?.let { it.dns || it.email || it.idprotect } == true)) {

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = colors.Outline.copy(alpha = 0.1f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Categories (Chips) - Weighted to take up available space and scrollable
                    Box(modifier = Modifier.weight(1f)) {
                        domain.price?.categories?.let { categories ->
                             if (categories.isNotEmpty()) {
                                CategoryChipsRow(categories = categories, isDarkTheme = isDarkTheme)
                            }
                        }
                    }

                    // Spacer to separate categories from addons if both exist
                    Spacer(modifier = Modifier.width(8.dp))

                    // Addons Icons - Fixed on the right
                    domain.price?.addons?.let { addons ->
                         if (addons.dns || addons.email || addons.idprotect) {
                            AddonsRow(addons = addons, isDarkTheme = isDarkTheme)
                        }
                    }
                }
            }

            // Action Button (Bottom Right)
            if (domain.status == "available" && (onAddToCart != null || onRemoveFromCart != null)) {
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End // Align to right
                ) {
                    if (isInCart && onRemoveFromCart != null) {
                        // Left Button: Remove from Cart (Softer style)
                        Button(
                            onClick = onRemoveFromCart,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.SurfaceVariant,
                                contentColor = colors.TextSecondary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            modifier = Modifier.scaleClick(onClick = onRemoveFromCart)
                        ) {
                            Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sepetinizde", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Button: Go to Cart (Primary style)
                        if (onGoToCart != null) {
                            Button(
                                onClick = onGoToCart,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.Primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                                modifier = Modifier.scaleClick(onClick = onGoToCart)
                            ) {
                                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sepete Git", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else if (onAddToCart != null) {
                        Button(
                            onClick = onAddToCart,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.Primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp), // More rounded
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                            modifier = Modifier.scaleClick(onClick = onAddToCart)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sepete Ekle", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    categories: List<String>,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Row(
        modifier = Modifier
            .wrapContentWidth()
            .horizontalScroll(rememberScrollState()), // Make it scrollable
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val (bgColor, textColor) = when (category.lowercase()) {
                "popular" -> colors.ChipPopularBg to colors.ChipPopularText
                "other" -> colors.ChipOtherBg to colors.ChipOtherText
                else -> colors.ChipDefaultBg to colors.ChipDefaultText
            }

            Surface(
                shape = MaterialTheme.shapes.small,
                color = bgColor
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AddonsRow(
    addons: DomainAddons,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp) // Reduced spacing slightly for better fit
    ) {
        Text(
            text = "Ek Hizmetler:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )

        if (addons.dns) {
            AddonIconWithTooltip(
                imageVector = Icons.Default.Language,
                title = "DNS Yönetimi",
                description = "Ücretsiz yönlendirme ve DNS kayıtları yönetimi.",
                tint = MaterialTheme.colorScheme.primary,
                isDarkTheme = isDarkTheme
            )
        }

        if (addons.email) {
            AddonIconWithTooltip(
                imageVector = Icons.Default.Email,
                title = "E-posta",
                description = "Kişisel veya kurumsal profesyonel e-posta desteği.",
                tint = MaterialTheme.colorScheme.primary,
                isDarkTheme = isDarkTheme
            )
        }

        if (addons.idprotect) {
            AddonIconWithTooltip(
                imageVector = Icons.Default.Security,
                title = "Gizlilik Koruması",
                description = "WHOIS bilgileriniz gizlenir, spam'den korunursunuz.",
                tint = MaterialTheme.colorScheme.primary,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun AddonIconWithTooltip(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    tint: Color,
    isDarkTheme: Boolean
) {
    var showTooltip by remember { mutableStateOf(false) }
    var tooltipHeight by remember { mutableStateOf(0) }
    val colors = if (isDarkTheme) DarkColors else LightColors
    val density = LocalDensity.current

    Box(contentAlignment = Alignment.Center) {
        // Icon - clickable without scale effect
        Box(
            modifier = Modifier
                .size(32.dp)
                .clickable { showTooltip = !showTooltip },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = title,
                modifier = Modifier.size(20.dp),
                tint = tint
            )
        }

        if (showTooltip) {
            // Calculate dynamic offset based on measured tooltip height + icon size + small gap
            val iconSizePx = with(density) { 32.dp.toPx().toInt() }
            val gapPx = with(density) { 8.dp.toPx().toInt() }
            val dynamicOffset = -(tooltipHeight + (iconSizePx / 2) + gapPx)

            Popup(
                onDismissRequest = { showTooltip = false },
                alignment = Alignment.TopCenter,
                offset = IntOffset(0, dynamicOffset)
            ) {
                // Entrance animation
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.SurfaceVariant.copy(alpha = 0.95f),
                        shadowElevation = 8.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.Outline.copy(alpha = 0.1f)),
                        modifier = Modifier
                            .widthIn(max = 200.dp)
                            .onGloballyPositioned { coordinates ->
                                tooltipHeight = coordinates.size.height
                            }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = colors.TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}