package com.mehmetmertmazici.domaincheckercompose.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mehmetmertmazici.domaincheckercompose.R
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
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

    // Session durumunu dinle
    val isLoggedIn by ServiceLocator.sessionManager.isLoggedIn.collectAsState(initial = false)
    val userName by ServiceLocator.sessionManager.userName.collectAsState(initial = null)
    val userEmail by ServiceLocator.sessionManager.userEmail.collectAsState(initial = null)

    // Sepet sayısını dinle
    val cartItems by ServiceLocator.cartRepository.cartItems.collectAsState()
    val cartItemCount = cartItems.size

    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = colors.Primary.copy(alpha = 0.1f),
        selectedIconColor = colors.Primary,
        selectedTextColor = colors.Primary,
        unselectedIconColor = colors.Primary,
        unselectedTextColor = colors.TextPrimary
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

                    Icon(
                        imageVector = Icons.Default.Domain,
                        contentDescription = "Domain Icon",
                        tint = Color.White,
                        modifier = Modifier
                            .size(40.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoggedIn && userName != null) {
                        // Logged in user info
                        Text(
                            text = userName ?: "Kullanıcı",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = userEmail ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    } else {
                        Text(
                            text = "Domain Checker",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = "Bizimle domain arayın",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
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
                colors = itemColors,
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
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Sepet (Cart) - Badge ile
            NavigationDrawerItem(
                label = { Text("Sepetim") },
                icon = {
                    BadgedBox(
                        badge = {
                            if (cartItemCount > 0) {
                                Badge(
                                    containerColor = colors.Error,
                                    contentColor = Color.White
                                ) {
                                    Text(cartItemCount.toString())
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null
                        )
                    }
                },
                selected = selectedRoute == "cart",
                onClick = { onNavigate("cart") },
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            )

            // Account Section
            Text(
                text = "Hesap",
                style = MaterialTheme.typography.labelLarge,
                color = colors.TextTertiary,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            if (isLoggedIn) {
                // Logged in options
                NavigationDrawerItem(
                    label = { Text("Profilim") },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null
                        )
                    },
                    selected = selectedRoute == "profile",
                    onClick = { onNavigate("profile") },
                    colors = itemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Siparişlerim") },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Receipt,
                            contentDescription = null
                        )
                    },
                    selected = selectedRoute == "orders",
                    onClick = { onNavigate("orders") },
                    colors = itemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Çıkış Yap") },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = null,
                            tint = colors.Error
                        )
                    },
                    selected = false,
                    onClick = { onNavigate("logout") },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = colors.Error
                    ),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            } else {
                // Not logged in options
                NavigationDrawerItem(
                    label = { Text("Giriş Yap") },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Login,
                            contentDescription = null
                        )
                    },
                    selected = selectedRoute == "login",
                    onClick = { onNavigate("login") },
                    colors = itemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Kayıt Ol") },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null
                        )
                    },
                    selected = selectedRoute == "register",
                    onClick = { onNavigate("register") },
                    colors = itemColors,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            HorizontalDivider(
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
                colors = itemColors,
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
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            HorizontalDivider(
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
                label = { Text("Uygulamayı Değerlendir") },
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null
                    )
                },
                selected = false,
                onClick = onRate,
                colors = itemColors,
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
                colors = itemColors,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}