package com.mehmetmertmazici.domaincheckercompose.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mehmetmertmazici.domaincheckercompose.R
import com.mehmetmertmazici.domaincheckercompose.ui.theme.DarkColors
import com.mehmetmertmazici.domaincheckercompose.ui.theme.LightColors

@Composable
fun DomainCheckerDrawer(
    selectedRoute: String?,
    onNavigate: (String) -> Unit,
    onHelp: () -> Unit,
    onIsimkayit: () -> Unit,
    onRate: () -> Unit,
    onShare: () -> Unit,
    isDarkTheme: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = if (isDarkTheme) DarkColors else LightColors
    val scrollState = rememberScrollState()

    // Tüm öğeler için ortak renk ayarı
    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = colors.Primary.copy(alpha = 0.1f),
        selectedIconColor = colors.Primary,
        selectedTextColor = colors.Primary,
        unselectedIconColor = colors.Primary, // Tutarlılık için eklendi
        unselectedTextColor = colors.TextPrimary // Tutarlılık için eklendi
    )

    ModalDrawerSheet(
        modifier = modifier.width(300.dp),
        drawerContainerColor = colors.Surface,
        windowInsets = WindowInsets(0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(colors.Primary, colors.PrimaryDark)
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = painterResource(id = R.drawable.ic_isimkayit_logo),
                        contentDescription = "İsimKayıt Logo",
                        modifier = Modifier
                            .width(140.dp)
                            .height(40.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Domain Checker",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "İsimKayıt ile domain arayın",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Navigation
            NavigationDrawerItem(
                label = { Text("Ana Sayfa") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null
                    )
                },
                selected = selectedRoute == "home",
                onClick = { onNavigate("home") },
                colors = itemColors, // Ortak renkler kullanılıyor
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Domain Fiyatları") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.AttachMoney,
                        contentDescription = null
                    )
                },
                selected = selectedRoute == "prices",
                onClick = { onNavigate("prices") },
                colors = itemColors, // Ortak renkler kullanılıyor
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )

            // App Section
            Text(
                text = "Uygulama",
                style = MaterialTheme.typography.labelLarge,
                color = colors.TextTertiary,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            NavigationDrawerItem(
                label = { Text("Uygulama Hakkında") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null
                    )
                },
                selected = selectedRoute == "about",
                onClick = { onNavigate("about") },
                colors = itemColors, // Ortak renkler kullanılıyor
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Yardım") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Help,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = onHelp,
                colors = itemColors, // Ortak renkler kullanılıyor
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Divider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )

            // External Links Section
            Text(
                text = "Dış Bağlantılar",
                style = MaterialTheme.typography.labelLarge,
                color = colors.TextTertiary,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            NavigationDrawerItem(
                label = { Text("İsimKayıt.com") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Language,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = onIsimkayit,
                colors = itemColors, // Ortak renkler kullanılıyor
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Uygulamayı Değerlendir") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = onRate,
                colors = itemColors, // Ortak renkler kullanılıyor
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            NavigationDrawerItem(
                label = { Text("Uygulamayı Paylaş") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = onShare,
                colors = itemColors, // Ortak renkler kullanılıyor
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}