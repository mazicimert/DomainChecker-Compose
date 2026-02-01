package com.mehmetmertmazici.domaincheckercompose.ui.screens

import com.mehmetmertmazici.domaincheckercompose.ui.components.AnimatedListItem
import com.mehmetmertmazici.domaincheckercompose.ui.components.GradientBackground
import com.mehmetmertmazici.domaincheckercompose.ui.components.IconSurface
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Domain
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.dp
import com.mehmetmertmazici.domaincheckercompose.ui.components.DomainPriceItemCard
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainPricesViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.PricesEffect
import kotlinx.coroutines.flow.collectLatest

@Composable
fun DomainPricesScreen(
    viewModel: DomainPricesViewModel,
    onBackClick: () -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    // UI State'i dinliyoruz
    val uiState by viewModel.uiState.collectAsState()

    // Filtre menüsü kontrolü için yerel state
    var showFilterSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        viewModel.effect.collectLatest { effect ->
            when(effect) {
                is PricesEffect.ShowError -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // İçeriği status bar'ın altına iter
        ) {
            // Glassy Top Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp) // Slightly taller for premium feel
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = colors.Primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = colors.Primary
                                )
                            }
                        }

                        Text(
                            text = "Domain Fiyatları",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = colors.TextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        // Filtre Butonu
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = colors.Primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Filled.FilterList,
                                    contentDescription = "Filter",
                                    tint = colors.Primary
                                )
                            }
                        }
                    }
                }
            }

            // Search Bar (Sticky/Fixed below TopBar)
            PricesSearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                isDarkTheme = isDarkTheme
            )

            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Info Card
                    item {
                        AnimatedListItem {
                            InfoCard(isDarkTheme = isDarkTheme)
                        }
                    }

                    // Price Items
                    if (uiState.domains.isNotEmpty()) {
                        items(
                            items = uiState.domains,
                            key = { it.domain }
                        ) { domain ->
                            val index = uiState.domains.indexOf(domain)
                            
                            AnimatedListItem(delayMillis = minOf(index * 20, 200)) {
                                DomainPriceItemCard(
                                    domain = domain,
                                    getTLD = viewModel::getTLD,
                                    isDarkTheme = isDarkTheme
                                )
                            }
                        }
                    }
                }

                // Loading State
                if (uiState.isLoading) {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center),
                        isDarkTheme = isDarkTheme
                    )
                }

                // Empty State (Search or Load Error)
                if (uiState.showEmptyState) {
                    val searchString = uiState.searchQuery
                    val isSearchEmpty = searchString.isNotEmpty()
                    EmptyState(
                        modifier = Modifier.align(Alignment.Center),
                        isDarkTheme = isDarkTheme,
                        title = if (isSearchEmpty) "Uzantı Bulunamadı" else "Fiyat Bilgisi Yüklenemedi",
                        message = if (isSearchEmpty) "'$searchString' ile eşleşen bir sonuç bulunamadı.\nFarklı bir arama yapmayı deneyin." else "Fiyat bilgileri şu anda yüklenemiyor.\nLütfen daha sonra tekrar deneyin."
                    )
                }
            }
        }

        // Filter Bottom Sheet
        if (showFilterSheet) {
            FilterBottomSheet(
                selectedCategory = uiState.activeCategory,
                onFilterSelected = { categoryId ->
                    viewModel.filterByCategory(categoryId)
                    showFilterSheet = false
                },
                onDismiss = { showFilterSheet = false },
                isDarkTheme = isDarkTheme
            )
        }
    }
}

@Composable
private fun InfoCard(
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = colors.CardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.AttachMoney,
                    contentDescription = null,
                    tint = colors.Primary,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Domain Uzantı Fiyatları",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Farklı domain uzantılarının kayıt, transfer ve yenileme fiyatlarını karşılaştırın.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.TextSecondary
            )
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = colors.CardBackground.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = colors.Primary,
                trackColor = colors.OutlineVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fiyatlar yükleniyor...",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.TextSecondary
            )
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    title: String = "Fiyat Bilgisi Yüklenemedi",
    message: String = "Fiyat bilgileri şu anda yüklenemiyor.\nLütfen daha sonra tekrar deneyin."
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Search, // Changed to generic search/info icon contextually? Keep generic or swap
            contentDescription = null,
            tint = colors.TextTertiary,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.TextTertiary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun PricesSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(1f),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Uzantı ara... (örn: .com)",
                        color = colors.TextTertiary
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = colors.Primary
                    )
                },
                trailingIcon = if (query.isNotEmpty()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = colors.TextSecondary
                            )
                        }
                    }
                } else null,
                shape = RoundedCornerShape(24.dp), // Extra Large rounded for modern feel
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.Primary,
                    unfocusedBorderColor = colors.Outline.copy(alpha = 0.3f),
                    focusedContainerColor = colors.Surface,
                    unfocusedContainerColor = colors.Surface,
                    cursorColor = colors.Primary,
                    focusedTextColor = colors.TextPrimary,
                    unfocusedTextColor = colors.TextPrimary
                ),
                singleLine = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    selectedCategory: String,
    onFilterSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean = false
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = "Kategori Seçin",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colors.TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Filter Options
            val filterOptions = listOf(
                FilterOption("all", "Tümü", "Tüm kategorilerdeki domainleri göster", Icons.Filled.FilterList, colors.Primary),
                FilterOption("popular", "Popüler", ".com, .net, .org, .biz", Icons.Filled.Star, colors.Warning),
                FilterOption("international", "Uluslararası", ".name, .info, .pro, .social", Icons.Filled.Public, colors.Info),
                FilterOption("country", "Ülke Kodları", ".us, .uk, .de, .fr", Icons.Filled.Domain, colors.Success),
                FilterOption("turkey", "Türkiye", ".com.tr, .net.tr, .org.tr", Icons.Filled.Flag, colors.Error)
            )

            filterOptions.forEach { option ->
                FilterOptionCard(
                    option = option,
                    isSelected = option.id == selectedCategory,
                    onClick = {
                        onFilterSelected(option.id)
                        onDismiss()
                    },
                    isDarkTheme = isDarkTheme
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private data class FilterOption(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconTint: Color
)

@Composable
private fun FilterOptionCard(
    option: FilterOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    isDarkTheme: Boolean
) {
    val colors = if (isDarkTheme) DarkColors else LightColors

    // Visual selection states
    val borderColor = if (isSelected) colors.Primary else Color.Transparent
    val backgroundColor = if (isSelected) colors.Primary.copy(alpha = 0.05f) else colors.SurfaceVariant
    val iconBgColor = if (isSelected) colors.Primary else colors.Primary.copy(alpha = 0.1f)
    val iconTintColor = if (isSelected) Color.White else option.iconTint

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconSurface(
                modifier = Modifier.size(44.dp),
                backgroundColor = iconBgColor,
                iconColor = iconTintColor
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = iconTintColor, // Enforce tint here as IconSurface expects content
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) colors.Primary else colors.TextPrimary
                )

                Text(
                    text = option.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.TextSecondary
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = colors.Primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = colors.TextTertiary.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}