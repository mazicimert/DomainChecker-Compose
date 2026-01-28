package com.mehmetmertmazici.domaincheckercompose.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetmertmazici.domaincheckercompose.model.CartItem
import com.mehmetmertmazici.domaincheckercompose.ui.components.GradientBackground
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CartEffect
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CartUiState
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CartViewModel
import kotlinx.coroutines.flow.collectLatest
import java.text.DecimalFormat

@Composable
fun CartScreen(
    viewModel: CartViewModel,
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToSearch: () -> Unit,
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
                is CartEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CartEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is CartEffect.NavigateToCheckout -> {
                    onCheckoutClick()
                }
                is CartEffect.NavigateToLogin -> {
                    onNavigateToLogin()
                }
            }
        }
    }

    GradientBackground(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar
            CartTopBar(
                itemCount = uiState.itemCount,
                onBackClick = onBackClick,
                onClearCart = { viewModel.clearCart() },
                showClearButton = !uiState.isEmpty
            )

            // Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.isEmpty) {
                    EmptyCartView(
                        onSearchClick = onNavigateToSearch,
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    CartContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Loading overlay
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.Primary)
                    }
                }
            }

            // Bottom Summary Bar (only when cart has items)
            AnimatedVisibility(
                visible = !uiState.isEmpty,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                CartSummaryBar(
                    uiState = uiState,
                    onCheckoutClick = { viewModel.proceedToCheckout() },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

// ============================================
// TOP BAR
// ============================================
@Composable
private fun CartTopBar(
    itemCount: Int,
    onBackClick: () -> Unit,
    onClearCart: () -> Unit,
    showClearButton: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Sepetim",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )

                    if (itemCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(
                            containerColor = Color.White,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Text(text = itemCount.toString())
                        }
                    }
                }

                if (showClearButton) {
                    IconButton(onClick = onClearCart) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Sepeti Temizle",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// CART CONTENT
// ============================================
@Composable
private fun CartContent(
    uiState: CartUiState,
    viewModel: CartViewModel,
    isDarkTheme: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Info Card
        item {
            CartInfoCard(isDarkTheme = isDarkTheme)
        }

        // Cart Items
        items(
            items = uiState.cartItems,
            key = { it.domain }
        ) { cartItem ->
            CartItemCard(
                item = cartItem,
                uiState = uiState, // Pass full uiState for safe fee getters
                onPeriodChange = { period -> viewModel.updatePeriod(cartItem.domain, period) },
                onToggleDns = { viewModel.toggleDns(cartItem.domain) },
                onToggleEmail = { viewModel.toggleEmail(cartItem.domain) },
                onToggleIdProtect = { viewModel.toggleIdProtect(cartItem.domain) },
                onRemove = { viewModel.removeFromCart(cartItem.domain) },
                itemTotal = viewModel.calculateItemTotal(cartItem),
                isDarkTheme = isDarkTheme
            )
        }

        // Bottom spacing for summary bar
        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ============================================
// INFO CARD
// ============================================
@Composable
private fun CartInfoCard(isDarkTheme: Boolean) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.Primary.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = colors.Primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Domain kayıt süresini ve ek hizmetleri aşağıdan ayarlayabilirsiniz.",
                style = MaterialTheme.typography.bodySmall,
                color = colors.TextPrimary
            )
        }
    }
}

// ============================================
// CART ITEM CARD
// ============================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CartItemCard(
    item: CartItem,
    uiState: CartUiState, // Use uiState for safe fee getters
    onPeriodChange: (Int) -> Unit,
    onToggleDns: () -> Unit,
    onToggleEmail: () -> Unit,
    onToggleIdProtect: () -> Unit,
    onRemove: () -> Unit,
    itemTotal: Double,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val priceFormat = remember { DecimalFormat("#,##0.00") }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.CardBackground
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Domain Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        tint = colors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.domain,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kaldır",
                        tint = colors.Error
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Period Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kayıt Süresi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextSecondary
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = "${item.period} Yıl",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(120.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.Primary,
                            unfocusedBorderColor = colors.Outline
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = colors.TextPrimary
                        ),
                        singleLine = true
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        (1..10).forEach { year ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "$year Yıl",
                                        color = colors.TextPrimary
                                    )
                                },
                                onClick = {
                                    onPeriodChange(year)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Base Price
            val basePrice = item.price?.register?.get(item.period.toString()) ?: "0.00"
            Text(
                text = "Kayıt Ücreti: €$basePrice",
                style = MaterialTheme.typography.bodySmall,
                color = colors.TextTertiary,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            HorizontalDivider(color = colors.Outline.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(16.dp))

            // Addon Services
            Text(
                text = "Ek Hizmetler",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.TextPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // DNS Management - use uiState.dnsFee for safe value
            AddonCheckboxRow(
                title = "DNS Yönetimi",
                subtitle = "Özel DNS kayıtları yönetin",
                price = uiState.dnsFee,
                checked = item.dnsEnabled,
                onCheckedChange = onToggleDns,
                icon = Icons.Default.Dns,
                isDarkTheme = isDarkTheme
            )

            // Email Forwarding - use uiState.emailFee for safe value
            AddonCheckboxRow(
                title = "E-posta Yönlendirme",
                subtitle = "E-posta adresleri oluşturun",
                price = uiState.emailFee,
                checked = item.emailEnabled,
                onCheckedChange = onToggleEmail,
                icon = Icons.Default.Email,
                isDarkTheme = isDarkTheme
            )

            // ID Protection - use uiState.idProtectFee for safe value
            AddonCheckboxRow(
                title = "Kimlik Koruması",
                subtitle = "Whois bilgilerinizi gizleyin",
                price = uiState.idProtectFee,
                checked = item.idProtectEnabled,
                onCheckedChange = onToggleIdProtect,
                icon = Icons.Default.Shield,
                isDarkTheme = isDarkTheme
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = colors.Outline.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            // Item Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ara Toplam",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.TextSecondary
                )
                Text(
                    text = "€${priceFormat.format(itemTotal)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.Primary
                )
            }
        }
    }
}

// ============================================
// ADDON CHECKBOX ROW
// ============================================
@Composable
private fun AddonCheckboxRow(
    title: String,
    subtitle: String,
    price: String,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    icon: ImageVector,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = colors.Primary,
                uncheckedColor = colors.Outline
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) colors.Primary else colors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = colors.TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = colors.TextTertiary
            )
        }

        Text(
            text = "+€$price",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (checked) colors.Primary else colors.TextTertiary
        )
    }
}

// ============================================
// CART SUMMARY BAR
// ============================================
@Composable
private fun CartSummaryBar(
    uiState: CartUiState,
    onCheckoutClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val priceFormat = remember { DecimalFormat("#,##0.00") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.CardBackground,
        shadowElevation = 16.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            // Price breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Alt Toplam",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextSecondary
                )
                Text(
                    text = "€${priceFormat.format(uiState.subtotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.TextPrimary
                )
            }

            if (uiState.addonsTotal > 0) {
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Ek Hizmetler",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.TextSecondary
                    )
                    Text(
                        text = "+€${priceFormat.format(uiState.addonsTotal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = colors.Outline.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(12.dp))

            // Grand Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Toplam",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.TextSecondary
                    )
                    Text(
                        text = "${uiState.itemCount} ürün",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.TextTertiary
                    )
                }

                Text(
                    text = "€${priceFormat.format(uiState.grandTotal)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = colors.Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkout Button
            Button(
                onClick = onCheckoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Payment,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ödemeye Geç",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================
// EMPTY CART VIEW
// ============================================
@Composable
private fun EmptyCartView(
    onSearchClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.ShoppingCart,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sepetiniz Boş",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Domain arayın ve beğendiklerinizi\nsepete ekleyin.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onSearchClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(Color.White, Color.White))
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Domain Ara")
        }
    }
}