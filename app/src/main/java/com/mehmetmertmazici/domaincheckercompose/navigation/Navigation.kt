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
import com.mehmetmertmazici.domaincheckercompose.viewmodel.AuthViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainPricesViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainSearchViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Screen Routes
sealed class Screen(val route: String) {
    // Auth Screens
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")

    // Main Screens
    object Home : Screen("home")
    object DomainPrices : Screen("prices")
    object AppInfo : Screen("about")
    object Cart : Screen("cart")
    object Profile : Screen("profile")
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

    // Auth ViewModel
    val authViewModel = remember { AuthViewModel() }

    // Session check
    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        isLoggedIn = ServiceLocator.sessionManager.isLoggedIn.first()
    }

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

    // Ana içerik - Auth ekranları drawer dışında
    val isAuthScreen = currentRoute in listOf(
        Screen.Login.route,
        Screen.Register.route,
        Screen.ForgotPassword.route
    )

    if (isAuthScreen) {
        // Auth ekranları drawer olmadan
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route
        ) {
            // Auth Screens
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

            // Main Screens (tekrar tanımlanacak)
            composable(Screen.Home.route) {
                MainContentWithDrawer(
                    navController = navController,
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                    isDarkTheme = isDarkTheme,
                    searchViewModel = searchViewModel,
                    pricesViewModel = pricesViewModel,
                    authViewModel = authViewModel,
                    onHelpClick = onHelpClick,
                    onIsimkayitClick = onIsimkayitClick,
                    onRateClick = onRateClick,
                    onShareClick = onShareClick,
                    onEmailClick = onEmailClick,
                    onContractsClick = onContractsClick,
                    onLicenseClick = onLicenseClick,
                    getVersionInfo = getVersionInfo
                )
            }
        }
    } else {
        // Ana ekranlar drawer ile
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
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
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                // Home Screen
                composable(Screen.Home.route) {
                    val uiState by searchViewModel.uiState.collectAsState()

                    MainSearchScreen(
                        viewModel = searchViewModel,
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

                // Domain Prices Screen
                composable(Screen.DomainPrices.route) {
                    DomainPricesScreen(
                        viewModel = pricesViewModel,
                        onBackClick = { navController.navigateUp() },
                        isDarkTheme = isDarkTheme
                    )
                }

                // App Info Screen
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

                // Auth Screens (drawer içinden erişim için)
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
            }
        }
    }
}

@Composable
private fun MainContentWithDrawer(
    navController: NavController,
    drawerState: DrawerState,
    currentRoute: String?,
    isDarkTheme: Boolean,
    searchViewModel: DomainSearchViewModel,
    pricesViewModel: DomainPricesViewModel,
    authViewModel: AuthViewModel,
    onHelpClick: () -> Unit,
    onIsimkayitClick: () -> Unit,
    onRateClick: () -> Unit,
    onShareClick: () -> Unit,
    onEmailClick: () -> Unit,
    onContractsClick: () -> Unit,
    onLicenseClick: () -> Unit,
    getVersionInfo: () -> Triple<String, Long, String>
) {
    val scope = rememberCoroutineScope()
    val uiState by searchViewModel.uiState.collectAsState()

    MainSearchScreen(
        viewModel = searchViewModel,
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