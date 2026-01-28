package com.mehmetmertmazici.domaincheckercompose.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mehmetmertmazici.domaincheckercompose.data.ServiceLocator
import com.mehmetmertmazici.domaincheckercompose.ui.components.DomainCheckerDrawer
import com.mehmetmertmazici.domaincheckercompose.ui.screens.*
import com.mehmetmertmazici.domaincheckercompose.ui.screens.auth.ForgotPasswordScreen
import com.mehmetmertmazici.domaincheckercompose.ui.screens.auth.LoginScreen
import com.mehmetmertmazici.domaincheckercompose.ui.screens.auth.RegisterScreen
import com.mehmetmertmazici.domaincheckercompose.ui.screens.auth.MailVerificationScreen
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CartViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainPricesViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainSearchViewModel
import kotlinx.coroutines.launch

// Extension imports for the placeholder
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Payment
import androidx.compose.ui.unit.dp

// Screen Routes
sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object MailVerification : Screen("mail_verification")

    // Main Screens
    object Home : Screen("home")
    object DomainPrices : Screen("prices")
    object AppInfo : Screen("about")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
    object Checkout : Screen("checkout")
}

@Composable
fun DomainCheckerApp(
    searchViewModel: DomainSearchViewModel,
    pricesViewModel: DomainPricesViewModel,
    onHelpClick: () -> Unit,
    onIsimkayitClick: () -> Unit,
    onRateClick: () -> Unit,
    onShareClick: () -> Unit,
    onEmailClick: () -> Unit,
    onContractsClick: () -> Unit,
    onLicenseClick: () -> Unit,
    getVersionInfo: () -> Triple<String, Long, String>
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val isDarkTheme = isSystemInDarkTheme()

    var currentRoute by remember { mutableStateOf<String?>("home") }

    // ViewModels
    val authViewModel = remember { AuthViewModel() }
    val cartViewModel = remember { CartViewModel() }

    // Navigasyon değişimlerini dinle
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

    // Auth ekranlarında drawer gösterme
    val isAuthScreen = currentRoute in listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route,
        Screen.MailVerification.route
    )

    // Drawer'ı koşullu olarak göster
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isAuthScreen, // Auth ekranlarında gesture kapalı
        drawerContent = {
            if (!isAuthScreen) {
                DomainCheckerDrawer(
                    selectedRoute = currentRoute,
                    onNavigate = { route ->
                        scope.launch {
                            drawerState.close()
                            when (route) {
                                "home" -> navController.navigate(Screen.Home.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                                "prices" -> navController.navigate(Screen.DomainPrices.route)
                                "about" -> navController.navigate(Screen.AppInfo.route)
                                "cart" -> navController.navigate(Screen.Cart.route)
                                "profile" -> navController.navigate(Screen.Profile.route)
                                "login" -> navController.navigate(Screen.Login.route)
                                "register" -> navController.navigate(Screen.Register.route)
                                "logout" -> {
                                    // Logout işlemini yap
                                    scope.launch {
                                        try {
                                            ServiceLocator.authRepository.logout()
                                        } catch (e: Exception) {
                                            // ne olursa olsun devammm
                                        }
                                        // Login sayfasına yönlendir
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onHelp = {
                        scope.launch { drawerState.close() }
                        onHelpClick()
                    },
                    onIsimkayit = {
                        scope.launch { drawerState.close() }
                        onIsimkayitClick()
                    },
                    onRate = {
                        scope.launch { drawerState.close() }
                        onRateClick()
                    },
                    onShare = {
                        scope.launch { drawerState.close() }
                        onShareClick()
                    },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    ) {
        // TEK NavHost - Tüm ekranlar burada
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route
        ) {
            // ============================================
            // AUTH SCREENS
            // ============================================

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onNavigateToForgotPassword = {
                        navController.navigate(Screen.ForgotPassword.route)
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToVerification = {
                        navController.navigate(Screen.MailVerification.route)
                    },
                    isDarkTheme = isDarkTheme
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToVerification = {
                        navController.navigate(Screen.MailVerification.route)
                    },
                    onBackClick = { navController.navigateUp() },
                    isDarkTheme = isDarkTheme
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    viewModel = authViewModel,
                    onBackClick = { navController.navigateUp() },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.ForgotPassword.route) { inclusive = true }
                        }
                    },
                    isDarkTheme = isDarkTheme
                )
            }

            composable(Screen.MailVerification.route) {
                val mailVerificationState by authViewModel.mailVerificationState.collectAsState()

                MailVerificationScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        // Login'den geldiyse Login'e, Register'dan geldiyse Register'a dön
                        if (mailVerificationState.isFromLogin) {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.MailVerification.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Register.route) {
                                popUpTo(Screen.MailVerification.route) { inclusive = true }
                            }
                        }
                    },
                    isDarkTheme = isDarkTheme
                )
            }

            // ============================================
            // MAIN SCREENS
            // ============================================

            composable(Screen.Home.route) {
                val uiState by searchViewModel.uiState.collectAsState()

                MainSearchScreen(
                    viewModel = searchViewModel,
                    cartViewModel = cartViewModel,
                    onNavigationClick = {
                        scope.launch { drawerState.open() }
                    },
                    onHelpClick = onHelpClick,
                    isDarkTheme = isDarkTheme
                )

                uiState.showWhoisDialog?.let { (domain, data) ->
                    WhoisDialog(
                        domain = domain,
                        whoisData = data,
                        onDismiss = { searchViewModel.dismissWhois() },
                        onCopy = { searchViewModel.copyWhoisData(data) },
                        isDarkTheme = isDarkTheme
                    )
                }
            }

            composable(Screen.DomainPrices.route) {
                DomainPricesScreen(
                    viewModel = pricesViewModel,
                    onBackClick = { navController.navigateUp() },
                    isDarkTheme = isDarkTheme
                )
            }

            composable(Screen.AppInfo.route) {
                val (versionName, versionCode, installDate) = getVersionInfo()

                AppInfoScreen(
                    versionName = versionName,
                    versionCode = versionCode,
                    installDate = installDate,
                    onBackClick = { navController.navigateUp() },
                    onEmailClick = onEmailClick,
                    onIsimkayitClick = onIsimkayitClick,
                    onContractsClick = onContractsClick,
                    onLicenseClick = onLicenseClick,
                    isDarkTheme = isDarkTheme
                )
            }

            // ============================================
            // CART SCREEN
            // ============================================

            composable(Screen.Cart.route) {
                CartScreen(
                    viewModel = cartViewModel,
                    onBackClick = { navController.navigateUp() },
                    onCheckoutClick = {
                        // TODO: Adım 4'te Checkout ekranına yönlendir
                        navController.navigate(Screen.Checkout.route)
                    },
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    },
                    isDarkTheme = isDarkTheme
                )
            }

            // ============================================
            // CHECKOUT SCREEN (Placeholder - Adım 4'te yapılacak)
            // ============================================

            composable(Screen.Checkout.route) {
                // TODO: Adım 4'te gerçek Checkout ekranı eklenecek
                CheckoutPlaceholderScreen(
                    onBackClick = { navController.navigateUp() },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}

// Placeholder Checkout Screen (Adım 4'te değiştirilecek)
@Composable
private fun CheckoutPlaceholderScreen(
    onBackClick: () -> Unit,
    isDarkTheme: Boolean
) {
    com.mehmetmertmazici.domaincheckercompose.ui.components.GradientBackground {
        androidx.compose.foundation.layout.Column(
            modifier = androidx.compose.ui.Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                color = androidx.compose.ui.graphics.Color.Transparent
            ) {
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    androidx.compose.material3.IconButton(onClick = onBackClick) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Geri",
                            tint = androidx.compose.ui.graphics.Color.White
                        )
                    }

                    androidx.compose.material3.Text(
                        text = "Ödeme",
                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = androidx.compose.ui.Modifier.padding(start = 8.dp)
                    )
                }
            }

            // Content
            androidx.compose.foundation.layout.Box(
                modifier = androidx.compose.ui.Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Payment,
                        contentDescription = null,
                        tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                        modifier = androidx.compose.ui.Modifier.size(100.dp)
                    )

                    androidx.compose.foundation.layout.Spacer(
                        modifier = androidx.compose.ui.Modifier.height(24.dp)
                    )

                    androidx.compose.material3.Text(
                        text = "Ödeme Ekranı",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                        color = androidx.compose.ui.graphics.Color.White
                    )

                    androidx.compose.foundation.layout.Spacer(
                        modifier = androidx.compose.ui.Modifier.height(8.dp)
                    )

                    androidx.compose.material3.Text(
                        text = "Adım 4'te tamamlanacak",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
