package com.mehmetmertmazici.domaincheckercompose.ui.screens

import com.mehmetmertmazici.domaincheckercompose.ui.components.AnimatedListItem
import com.mehmetmertmazici.domaincheckercompose.ui.components.GradientBackground
import com.mehmetmertmazici.domaincheckercompose.ui.components.IconSurface
import com.mehmetmertmazici.domaincheckercompose.ui.components.scaleClick
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import com.mehmetmertmazici.domaincheckercompose.R
import com.mehmetmertmazici.domaincheckercompose.ui.components.DomainItemCard
import com.mehmetmertmazici.domaincheckercompose.ui.components.DomainSuggestionCard
import com.mehmetmertmazici.domaincheckercompose.ui.components.LoadingModal
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CartEffect
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CartViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainSearchViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.SearchEffect
import kotlinx.coroutines.flow.collectLatest


@Composable
fun MainSearchScreen(
    viewModel: DomainSearchViewModel,
    cartViewModel: CartViewModel,
    onNavigationClick: () -> Unit,
    onCartClick: () -> Unit,
    onHelpClick: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val uiState by viewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Search effects
    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SearchEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is SearchEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                is SearchEffect.OpenUrl -> {
                   // Intent.ACTION_VIEW removed as per request
                }
                is SearchEffect.CopyToClipboard -> {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Copied Text", effect.text)
                    clipboard.setPrimaryClip(clip)
                }
            }
        }
    }

    // Cart effects
    LaunchedEffect(key1 = true) {
        cartViewModel.effect.collectLatest { effect ->
            when (effect) {
                is CartEffect.ShowMessage -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
                is CartEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
                else -> { /* Handled in Cart screen */ }
            }
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar with Cart Badge
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
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onNavigationClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = colors.TextPrimary
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Domain Checker",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = colors.TextPrimary
                            )
                        }

                        // Cart Badge - Shows item count
                        IconButton(onClick = onCartClick) {
                            if (cartUiState.itemCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge(
                                            containerColor = colors.Error,
                                            contentColor = Color.White
                                        ) {
                                            Text(cartUiState.itemCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingCart,
                                        contentDescription = "Sepet",
                                        tint = colors.TextPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Sepet",
                                    tint = colors.TextPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AnimatedListItem {
                        SearchCard(
                            searchQuery = uiState.searchQuery,
                            onSearchQueryChange = viewModel::updateSearchQuery,
                            onSearchClick = viewModel::performSearch,
                            onClearClick = { viewModel.updateSearchQuery("") },
                            isSearchEnabled = uiState.searchQuery.length >= 2,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }

                if (uiState.suggestions != null) {
                    item {
                        AnimatedListItem(delayMillis = 50) {
                            DomainSuggestionCard(
                                originalDomain = uiState.originalSearchQuery ?: "",
                                suggestions = uiState.suggestions!!,
                                onSuggestionClick = viewModel::applySuggestion,
                                onTryAnyway = viewModel::searchAnyway,
                                onHelp = onHelpClick,
                                onClose = viewModel::closeSuggestions,
                                isDarkTheme = isDarkTheme
                            )
                        }
                    }
                }

                if (uiState.domains.isNotEmpty()) {
                    items(
                        items = uiState.domains,
                        key = { domain -> domain.domain }
                    ) { domain ->
                        val isInCart = cartUiState.cartItems.any { it.domain == domain.domain }
                        val index = uiState.domains.indexOf(domain)

                        AnimatedListItem(delayMillis = minOf(index * 40, 300)) {
                            DomainItemCard(
                                domain = domain,
                                onClick = {
                                    if (domain.status == "registered") {
                                        viewModel.showWhois(domain.domain)
                                    }
                                    // available status clicks do nothing now
                                },
                                isDarkTheme = isDarkTheme,
                                isInCart = isInCart,
                                onAddToCart = if (domain.status == "available") {
                                    { cartViewModel.addToCart(domain) }
                                } else null,
                                onRemoveFromCart = if (domain.status == "available" && isInCart) {
                                    { cartViewModel.removeFromCart(domain.domain) }
                                } else null,
                                onGoToCart = onCartClick
                            )
                        }
                    }
                }

                if (uiState.showEmptyState && uiState.domains.isEmpty() && uiState.suggestions == null) {
                    item {
                        AnimatedListItem {
                            EmptyStateView(isDarkTheme = isDarkTheme)
                        }
                    }
                }
            }
        }
        LoadingModal(isVisible = uiState.isLoading, isDarkTheme = isDarkTheme)
    }
}

@Composable
private fun SearchCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onClearClick: () -> Unit,
    isSearchEnabled: Boolean,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    // Floating Glass Search Bar
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = colors.Primary.copy(alpha = 0.25f),
                ambientColor = colors.Primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = colors.CardBackground.copy(alpha = 0.95f), // Slightly translucent
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            // Header with IconSurface
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
               IconSurface(
                   backgroundColor = colors.PrimaryContainer.copy(alpha = 0.5f),
                   iconColor = colors.Primary
               ) {
                   Icon(
                       imageVector = Icons.Default.Search,
                       contentDescription = null,
                       modifier = Modifier.size(24.dp)
                   )
               }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = "Domain Sorgula",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary
                    )
                    Text(
                        text = "Hayalindeki ismi bul",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.TextSecondary
                    )
                }
            }

            // Search Input Area
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("örn: google.com", color = colors.TextTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.Primary,
                    unfocusedBorderColor = colors.Outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearClick) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = colors.TextTertiary
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchClick() })
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Search Button with Scale Effect
            Button(
                onClick = onSearchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scaleClick(onClick = onSearchClick),
                enabled = isSearchEnabled,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Primary,
                    disabledContainerColor = colors.Primary.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 2.dp
                )
            ) {
                if (isSearchEnabled) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = "Müsaitlik Kontrol Et",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyStateView(
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Modern Circular Icon Background
        Surface(
            shape = CircleShape,
            color = colors.SurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(120.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.DomainDisabled,
                    contentDescription = null,
                    tint = colors.TextTertiary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Sonuç Bulunamadı",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = colors.TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aradığınız domain için uygun sonuç yok.\nFarklı anahtar kelimelerle tekrar deneyin.",
            style = MaterialTheme.typography.bodyLarge,
            color = colors.TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}