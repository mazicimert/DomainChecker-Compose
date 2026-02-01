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
import com.mehmetmertmazici.domaincheckercompose.ui.screens.checkout.CheckoutScreen
import com.mehmetmertmazici.domaincheckercompose.ui.screens.checkout.OrderSuccessScreen
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CartViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.CheckoutViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainPricesViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainSearchViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding

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
    object OrderSuccess : Screen("order_success/{invoiceId}/{status}") {
        fun createRoute(invoiceId: Int, status: String) = "order_success/$invoiceId/$status"
    }
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
    val checkoutViewModel = remember { CheckoutViewModel() }

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
                    onCartClick = {
                        navController.navigate(Screen.Cart.route)
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
            // CHECKOUT SCREEN
            // ============================================

            composable(Screen.Checkout.route) {
                CheckoutScreen(
                    viewModel = checkoutViewModel,
                    onBackClick = { navController.navigateUp() },
                    onNavigateToSuccess = { invoiceId, status ->
                        navController.navigate(Screen.OrderSuccess.createRoute(invoiceId, status)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                    },
                    isDarkTheme = isDarkTheme
                )
            }

            // ============================================
            // ORDER SUCCESS SCREEN
            // ============================================

            composable(
                route = Screen.OrderSuccess.route,
                arguments = listOf(
                    navArgument("invoiceId") { type = NavType.IntType },
                    navArgument("status") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val invoiceId = backStackEntry.arguments?.getInt("invoiceId") ?: 0
                val status = backStackEntry.arguments?.getString("status") ?: "Paid"

                OrderSuccessScreen(
                    invoiceId = invoiceId,
                    status = status,
                    onNavigateToHome = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToInvoice = { id ->
                        // TODO: Navigate to invoice detail screen if available
                    },
                    isDarkTheme = isDarkTheme
                )
            }
        }
    }
}
