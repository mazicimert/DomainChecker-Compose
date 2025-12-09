package com.mehmetmertmazici.domaincheckercompose.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mehmetmertmazici.domaincheckercompose.ui.components.DomainCheckerDrawer
import com.mehmetmertmazici.domaincheckercompose.ui.screens.*
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainPricesViewModel
import com.mehmetmertmazici.domaincheckercompose.viewmodel.DomainSearchViewModel
import kotlinx.coroutines.launch

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object DomainPrices : Screen("prices")
    object AppInfo : Screen("about")
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

    // Navigasyon değişimlerini dinleyip drawer seçimini güncelle
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            currentRoute = destination.route
        }
        navController.addOnDestinationChangedListener(listener)
        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }

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
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                            "prices" -> navController.navigate(Screen.DomainPrices.route)
                            "about" -> navController.navigate(Screen.AppInfo.route)
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
            // ---------------------------------------------------------
            // 1. Home Screen (MainSearchScreen)
            // ---------------------------------------------------------
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

                // Whois Dialog Gösterimi
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

            // ---------------------------------------------------------
            // 2. Domain Prices Screen
            // ---------------------------------------------------------
            composable(Screen.DomainPrices.route) {
                DomainPricesScreen(
                    viewModel = pricesViewModel,
                    onBackClick = { navController.navigateUp() },
                    isDarkTheme = isDarkTheme
                )
            }

            // ---------------------------------------------------------
            // 3. App Info Screen
            // ---------------------------------------------------------
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
        }
    }
}