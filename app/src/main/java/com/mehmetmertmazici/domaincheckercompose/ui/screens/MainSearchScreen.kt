package com.mehmetmertmazici.domaincheckercompose.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.DomainDisabled
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.mehmetmertmazici.domaincheckercompose.R
import com.mehmetmertmazici.domaincheckercompose.ui.components.DomainItemCard
import com.mehmetmertmazici.domaincheckercompose.ui.components.DomainSuggestionCard
import com.mehmetmertmazici.domaincheckercompose.ui.components.GradientBackground
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
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, effect.url.toUri())
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Tarayıcı açılamadı", Toast.LENGTH_SHORT).show()
                    }
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
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = onNavigationClick) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Domain Checker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )

                        // Cart Badge - Shows item count
                        if (cartUiState.itemCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = colors.Error,
                                        contentColor = Color.White
                                    ) {
                                        Text(cartUiState.itemCount.toString())
                                    }
                                },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ShoppingCart,
                                    contentDescription = "Sepet",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Image(
                            painter = painterResource(id = R.drawable.ic_isimkayit_logo),
                            contentDescription = "İsimKayıt Logo",
                            modifier = Modifier
                                .width(100.dp)
                                .height(40.dp)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Fit
                        )
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
                    SearchCard(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = viewModel::updateSearchQuery,
                        onSearchClick = viewModel::performSearch,
                        onClearClick = { viewModel.updateSearchQuery("") },
                        isSearchEnabled = uiState.searchQuery.length >= 2,
                        isDarkTheme = isDarkTheme
                    )
                }

                if (uiState.suggestions != null) {
                    item {
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

                if (uiState.domains.isNotEmpty()) {
                    items(
                        items = uiState.domains,
                        key = { domain -> domain.domain }
                    ) { domain ->
                        val isInCart = cartViewModel.isInCart(domain.domain)

                        DomainItemCard(
                            domain = domain,
                            onClick = {
                                if (domain.status == "registered") {
                                    viewModel.showWhois(domain.domain)
                                } else {
                                    viewModel.openRegistration(domain.domain)
                                }
                            },
                            isDarkTheme = isDarkTheme,
                            isInCart = isInCart,
                            onAddToCart = if (domain.status == "available") {
                                { cartViewModel.addToCart(domain) }
                            } else null,
                            onRemoveFromCart = if (domain.status == "available" && isInCart) {
                                { cartViewModel.removeFromCart(domain.domain) }
                            } else null
                        )
                    }
                }

                if (uiState.showEmptyState && uiState.domains.isEmpty() && uiState.suggestions == null) {
                    item { EmptyStateView(isDarkTheme = isDarkTheme) }
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = colors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Header
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
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.Primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Domain Arama",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = colors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "İstediğiniz domain adını arayın ve müsaitlik durumunu kontrol edin",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("örn: example.com") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Domain,
                        contentDescription = null,
                        tint = colors.Primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = onClearClick) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { if (isSearchEnabled) onSearchClick() }
                ),
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.Primary,
                    unfocusedBorderColor = colors.Outline
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search Button
            Button(
                onClick = onSearchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isSearchEnabled,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.Primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sorgula",
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.DomainDisabled,
            contentDescription = null,
            tint = colors.TextTertiary,
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = "Sonuç bulunamadı",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.TextSecondary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Aradığınız domain için sonuç bulunamadı.\nFarklı bir domain deneyin.",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.TextTertiary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}