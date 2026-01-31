package com.mehmetmertmazici.domaincheckercompose.ui.components



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mehmetmertmazici.domaincheckercompose.model.Domain
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors


@Composable
fun DomainPriceItemCard(
    domain: Domain,
    getTLD: (Domain) -> String,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val tld = getTLD(domain)

    val isHot = domain.price?.group == "hot"

    PremiumCard(
        modifier = modifier.padding(vertical = 6.dp),
        isHot = isHot
    ) {
        // Header Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Domain Extension
            Row {
                Text(
                    text = ".",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.alignByBaseline()
                )
                Text(
                    text = tld,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.alignByBaseline()
                )
            }

            // Hot Badge with Glow Effect
            if (isHot) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = colors.ChipHotBg.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, colors.ChipHotBg.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "🔥 HOT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.ChipHotText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Price Grid
        domain.price?.let { price ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Register Price
                price.register?.get("1")?.let { registerPrice ->
                    PriceColumn(
                        label = "Kayıt",
                        price = "€$registerPrice",
                        color = colors.Success
                    )
                }
                
                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        .align(Alignment.CenterVertically)
                )

                // Transfer Price
                price.transfer?.get("1")?.let { transferPrice ->
                    PriceColumn(
                        label = "Transfer",
                        price = "€$transferPrice",
                        color = colors.Info
                    )
                }
                
                 // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(32.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        .align(Alignment.CenterVertically)
                )

                // Renew Price
                price.renew?.get("1")?.let { renewPrice ->
                    PriceColumn(
                        label = "Yenileme",
                        price = "€$renewPrice",
                        color = colors.Warning
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Categories
            price.categories?.let { categories ->
                if (categories.isNotEmpty()) {
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
                                shape = MaterialTheme.shapes.medium, // Softer corners
                                color = bgColor.copy(alpha = 0.2f), // More transparent
                                border = BorderStroke(1.dp, bgColor.copy(alpha = 0.4f)) // Subtle border
                            ) {
                                Text(
                                    text = category.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriceColumn(
    label: String,
    price: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = price,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}