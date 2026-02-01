package com.mehmetmertmazici.domaincheckercompose.ui.screens

import com.mehmetmertmazici.domaincheckercompose.ui.components.AnimatedListItem
import com.mehmetmertmazici.domaincheckercompose.ui.components.GradientBackground
import com.mehmetmertmazici.domaincheckercompose.ui.components.scaleClick
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mehmetmertmazici.domaincheckercompose.model.CartItem
import com.mehmetmertmazici.domaincheckercompose.ui.components.IconSurface
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
    var showClearCartDialog by remember { mutableStateOf(false) }

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

    // Confirmation Dialog
    if (showClearCartDialog) {
        AlertDialog(
            onDismissRequest = { showClearCartDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(colors.Error.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = null,
                        tint = colors.Error,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Sepeti Temizle?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,
                     textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "Sepetinizdeki tüm ürünler silinecek. Bu işlemi onaylıyor musunuz?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearCart()
                        showClearCartDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.Error),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp).fillMaxWidth()
                ) {
                    Text("Evet, Temizle", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCartDialog = false },
                    modifier = Modifier.height(48.dp).fillMaxWidth()
                ) {
                    Text("Vazgeç", color = colors.TextSecondary)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = colors.Surface,
            tonalElevation = 6.dp
        )
    }

    GradientBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Modern Header
                CartTopBar(
                    itemCount = uiState.itemCount,
                    onBackClick = onBackClick,
                    onClearCart = { showClearCartDialog = true },
                    showClearButton = !uiState.isEmpty,
                    isDarkTheme = isDarkTheme
                )

                // Scrollable Content
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
            }
            
            // Floating Bottom Summary Bar
            if (!uiState.isEmpty) {
                 CartSummaryBar(
                    uiState = uiState,
                    onCheckoutClick = { viewModel.proceedToCheckout() },
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.align(Alignment.BottomCenter)
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
    showClearButton: Boolean,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button with Glass effect
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.SurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(44.dp),
            onClick = onBackClick
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = colors.TextPrimary
                )
            }
        }

        // Title
        Text(
            text = "Alışveriş Sepeti (${itemCount})",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        // Clear Action
        if (showClearButton) {
            TextButton(
                onClick = onClearCart,
                colors = ButtonDefaults.textButtonColors(contentColor = colors.TextSecondary)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Temizle",
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.size(44.dp)) // Balance layout
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
            AnimatedListItem {
                CartInfoCard(isDarkTheme = isDarkTheme)
            }
        }

        // Cart Items
        items(
            items = uiState.cartItems,
            key = { it.domain }
        ) { cartItem ->
            val index = uiState.cartItems.indexOf(cartItem)
            
            AnimatedListItem(delayMillis = 100 + (index * 80)) {
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
// ============================================
// CART ITEM CARD
// ============================================
@OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun CartItemCard(
    item: CartItem,
    uiState: CartUiState,
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
    
    // Glassmorphic Card Container
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = colors.Primary.copy(alpha = 0.15f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.Surface.copy(alpha = 0.8f) // Glass-like transparency
        ),
        border = BorderStroke(1.dp, colors.Outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header: Icon + Domain Name + Remove
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Domain Icon
                IconSurface(
                    backgroundColor = colors.Primary.copy(alpha = 0.1f),
                    iconColor = colors.Primary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.domain,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kayıt Ücreti: €${item.price?.register?.get(item.period.toString()) ?: "0.00"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.TextTertiary
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(36.dp)
                        .background(colors.Error.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kaldır",
                        tint = colors.Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Period Selector (Modern Pill/Slider Style)
            Text(
                text = "Kayıt Süresi",
                style = MaterialTheme.typography.labelMedium,
                color = colors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Horizontal scrollable years for modern selection
            val availableYears = remember(item.price?.register) {
                item.price?.register?.entries?.mapNotNull { (year, price) ->
                    if (price != "0.00" && price.isNotBlank() && price != "null") {
                        year.toIntOrNull()
                    } else {
                        null
                    }
                }?.sorted()?.takeIf { it.isNotEmpty() } ?: listOf(1)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableYears.forEach { year -> // Showing available years dynamically
                    val isSelected = item.period == year
                    val containerColor by animateColorAsState(
                        if (isSelected) colors.Primary else colors.SurfaceVariant.copy(alpha = 0.5f)
                    )
                    val contentColor by animateColorAsState(
                        if (isSelected) Color.White else colors.TextSecondary
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        modifier = Modifier
                            .height(36.dp)
                            .clickable { onPeriodChange(year) },
                        border = if (!isSelected) BorderStroke(1.dp, colors.Outline.copy(alpha = 0.2f)) else null
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "$year Yıl",
                                style = MaterialTheme.typography.labelLarge,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Addons (Horizontal Chips)
            Text(
                text = "Ek Hizmetler",
                style = MaterialTheme.typography.labelMedium,
                color = colors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            FlowRow(
                 modifier = Modifier.fillMaxWidth(),
                 horizontalArrangement = Arrangement.spacedBy(8.dp),
                 verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // DNS
                AddonChip(
                    title = "DNS",
                    price = uiState.dnsFee,
                    checked = item.dnsEnabled,
                    onToggle = onToggleDns,
                    icon = Icons.Default.Dns, // Dns icon
                    isDarkTheme = isDarkTheme
                )
                
                // Email
                AddonChip(
                    title = "E-posta",
                    price = uiState.emailFee,
                    checked = item.emailEnabled,
                    onToggle = onToggleEmail,
                    icon = Icons.Default.Email,
                    isDarkTheme = isDarkTheme
                )
                
                // ID Protect
                AddonChip(
                    title = "Gizlilik",
                    price = uiState.idProtectFee,
                    checked = item.idProtectEnabled,
                    onToggle = onToggleIdProtect,
                    icon = Icons.Default.Shield, // Shield icon
                    isDarkTheme = isDarkTheme
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = colors.Outline.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(16.dp))

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
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.Primary
                )
            }
        }
    }
}

// ============================================
// ADDON CHIP (MODERN COMPACT)
// ============================================
@Composable
private fun AddonChip(
    title: String,
    price: String,
    checked: Boolean,
    onToggle: () -> Unit,
    icon: ImageVector,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val backgroundColor by animateColorAsState(
        if (checked) colors.Primary.copy(alpha = 0.1f) else colors.Surface
    )
    val borderColor by animateColorAsState(
        if (checked) colors.Primary else colors.Outline.copy(alpha = 0.2f)
    )
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable { onToggle() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (checked) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (checked) colors.Primary else colors.TextTertiary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (checked) colors.Primary else colors.TextPrimary
                )
                Text(
                    text = "+€$price",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (checked) colors.Primary.copy(alpha = 0.8f) else colors.TextTertiary
                )
            }
        }
    }
}



// ============================================
// CART SUMMARY BAR (FLOATING)
// ============================================
@Composable
private fun CartSummaryBar(
    uiState: CartUiState,
    onCheckoutClick: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val priceFormat = remember { DecimalFormat("#,##0.00") }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 24.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color.Black.copy(alpha = 0.2f),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        color = colors.Surface.copy(alpha = 0.95f), // High opacity for readability
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, colors.Outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
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

            Spacer(modifier = Modifier.height(16.dp))
            
            // Grand Total and Checkout Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Toplam",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.TextTertiary
                    )
                    Text(
                        text = "€${priceFormat.format(uiState.grandTotal)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.Primary
                    )
                    Text(
                        text = "${uiState.itemCount} ürün",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.TextSecondary
                    )
                }

                Button(
                    onClick = onCheckoutClick,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(56.dp)
                        .scaleClick(onClick = onCheckoutClick),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.Primary,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
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
            tint = colors.TextTertiary,
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sepetiniz Boş",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Domain arayın ve beğendiklerinizi\nsepete ekleyin.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedButton(
            onClick = onSearchClick,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colors.TextPrimary
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.linearGradient(listOf(colors.Primary, colors.Primary))
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