package com.mehmetmertmazici.domaincheckercompose.ui.components

import androidx.compose.foundation.layout.*
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = colors.CardBackground
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Domain Extension
                Text(
                    text = ".$tld",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Hot Badge
                if (domain.price?.group == "hot") {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = colors.ChipHotBg
                    ) {
                        Text(
                            text = "🔥 HOT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.ChipHotText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price Grid
            domain.price?.let { price ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Register Price
                    price.register?.get("1")?.let { registerPrice ->
                        PriceColumn(
                            label = "Kayıt",
                            price = "€$registerPrice",
                            color = colors.Success,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Transfer Price
                    price.transfer?.get("1")?.let { transferPrice ->
                        PriceColumn(
                            label = "Transfer",
                            price = "€$transferPrice",
                            color = colors.Info,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Renew Price
                    price.renew?.get("1")?.let { renewPrice ->
                        PriceColumn(
                            label = "Yenileme",
                            price = "€$renewPrice",
                            color = colors.Warning,
                            modifier = Modifier.weight(1f)
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