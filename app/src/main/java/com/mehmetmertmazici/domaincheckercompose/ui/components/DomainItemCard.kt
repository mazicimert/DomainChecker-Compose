package com.mehmetmertmazici.domaincheckercompose.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    onRemoveFromCart: (() -> Unit)? = null
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

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) colors.CardBackground else Color.White
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Indicator
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = CircleShape,
                    color = statusColor
                ) {}

                Spacer(modifier = Modifier.width(12.dp))

                // Domain Name
                Text(
                    text = domain.domain,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Status Badge with Emoji
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = statusColor
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = statusEmoji,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // Price Section
            domain.price?.let { price ->
                val registerPrice = price.register?.get("1")
                val renewPrice = price.renew?.get("1")

                if (registerPrice != null) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "€$registerPrice",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (renewPrice != null && renewPrice != registerPrice) {
                            Text(
                                text = "Yenileme: €$renewPrice",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Categories
            domain.price?.categories?.let { categories ->
                if (categories.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryChipsRow(
                        categories = categories,
                        isDarkTheme = isDarkTheme
                    )
                }
            }

            // Addons Section
            domain.price?.addons?.let { addons ->
                if (addons.dns || addons.email || addons.idprotect) {
                    Spacer(modifier = Modifier.height(12.dp))

                    AddonsRow(
                        addons = addons,
                        isDarkTheme = isDarkTheme
                    )
                }
            }

            // Action Section - Sepete Ekle / Çıkar Butonu
            if (domain.status == "available" && (onAddToCart != null || onRemoveFromCart != null)) {
                Spacer(modifier = Modifier.height(12.dp))

                HorizontalDivider(
                    color = colors.Outline.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hint Text
                    Text(
                        text = if (isInCart) "Sepete eklendi ✓" else "Kayıt için tıklayın",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isInCart) colors.StatusAvailable else colors.StatusAvailableDark
                    )

                    // Add/Remove Cart Button
                    if (isInCart && onRemoveFromCart != null) {
                        // Sepette - Çıkar butonu
                        FilledTonalButton(
                            onClick = {
                                onRemoveFromCart()
                            },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colors.StatusAvailable.copy(alpha = 0.15f),
                                contentColor = colors.StatusAvailable
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sepette",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (onAddToCart != null) {
                        // Sepette değil - Ekle butonu
                        Button(
                            onClick = {
                                onAddToCart()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.Primary,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sepete Ekle",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                // Kayıtlı domain için sadece hint text
                val hintText = when (domain.status) {
                    "registered" -> "Whois bilgisi için tıklayın"
                    else -> null
                }

                hintText?.let {
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.fillMaxWidth()
                    )
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
        modifier = Modifier.fillMaxWidth(),
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
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Ek Hizmetler:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (addons.dns) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "DNS Yönetimi",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        if (addons.email) {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = "E-posta Hesabı",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        if (addons.idprotect) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Kimlik Koruması",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}