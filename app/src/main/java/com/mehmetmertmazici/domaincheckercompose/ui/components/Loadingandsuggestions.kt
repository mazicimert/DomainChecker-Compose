package com.mehmetmertmazici.domaincheckercompose.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.utils.DomainCorrector

@Composable
fun LoadingModal(
    isVisible: Boolean,
    isDarkTheme: Boolean = false
) {
    if (isVisible) {
        val colors = if (isDarkTheme) DarkColors else LightColors

        Dialog(
            onDismissRequest = { /* Non-dismissible */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false // Allow full customization
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Surface(
                    modifier = Modifier
                        .width(300.dp)
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(32.dp),
                    color = colors.Surface.copy(alpha = 0.95f), // Glass-like high opacity
                    shadowElevation = 24.dp,
                    border = BorderStroke(1.dp, Brush.linearGradient(
                        listOf(colors.Outline.copy(alpha = 0.2f), Color.Transparent)
                    ))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Custom Pulse Animation
                        PulseLoadingAnimation(color = colors.Primary)

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "Sorgulanıyor...",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Domain müsaitlik durumu\nkontrol ediliyor",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PulseLoadingAnimation(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(80.dp)
    ) {
        // Outer ripple
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .background(color.copy(alpha = alpha), CircleShape)
        )
        
        // Inner icon circle
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            border = BorderStroke(2.dp, color.copy(alpha = 0.2f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun DomainSuggestionCard(
    originalDomain: String,
    suggestions: List<DomainCorrector.Suggestion>,
    onSuggestionClick: (String) -> Unit,
    onTryAnyway: () -> Unit,
    onHelp: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp), // Extra Large
        colors = CardDefaults.cardColors(
            containerColor = colors.Surface.copy(alpha = 0.9f) // Light glass
        ),
        border = BorderStroke(1.dp, colors.Primary.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconSurface(
                        backgroundColor = colors.PrimaryContainer,
                        iconColor = colors.Primary,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "Bunu mu demek istediniz?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Kapat",
                        tint = colors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Context Text
            Text(
                text = "'$originalDomain' hatalı yazılmış olabilir. Aşağıdakilerden birini seçebilirsiniz:",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.TextSecondary
            )

            // Suggestions Chips
            if (suggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { suggestion ->
                        SuggestionChip(
                            suggestion = suggestion,
                            onClick = { onSuggestionClick(suggestion.domain) },
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onHelp,
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.TextSecondary)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Help,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Yardım", style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onTryAnyway,
                    border = BorderStroke(1.dp, colors.Outline.copy(alpha = 0.3f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = colors.TextPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(text = "Yine de Ara", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun SuggestionChip(
    suggestion: DomainCorrector.Suggestion,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val isHighConfidence = suggestion.confidenceScore >= 0.9

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isHighConfidence) colors.Primary.copy(alpha = 0.1f) else colors.Surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (isHighConfidence) colors.Primary else colors.Outline.copy(alpha = 0.2f)
        ),
        modifier = Modifier.height(32.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = suggestion.domain,
                style = MaterialTheme.typography.labelLarge,
                color = if (isHighConfidence) colors.Primary else colors.TextPrimary,
                fontWeight = if (isHighConfidence) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}