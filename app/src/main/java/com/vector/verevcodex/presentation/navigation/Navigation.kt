package com.vector.verevcodex.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vector.verevcodex.R
import com.vector.verevcodex.domain.model.ScanMethod
import com.vector.verevcodex.presentation.analytics.AnalyticsDashboardScreen
import com.vector.verevcodex.presentation.analytics.StaffAnalyticsScreen
import com.vector.verevcodex.presentation.auth.forgot.ForgotPasswordScreen
import com.vector.verevcodex.presentation.auth.forgot.RecoveryMode
import com.vector.verevcodex.presentation.auth.login.LoginScreen
import com.vector.verevcodex.presentation.auth.signup.SignupScreen
import com.vector.verevcodex.presentation.customers.AddCustomerScreen
import com.vector.verevcodex.presentation.customers.CustomerCredentialManagementScreen
import com.vector.verevcodex.presentation.customers.CustomerListScreen
import com.vector.verevcodex.presentation.customers.CustomerProfileScreen
import com.vector.verevcodex.presentation.dashboard.DashboardScreen
import com.vector.verevcodex.presentation.merchant.common.MerchantBottomBar
import com.vector.verevcodex.presentation.merchant.common.MerchantBottomDestination
import com.vector.verevcodex.presentation.merchant.common.MerchantTopBar
import com.vector.verevcodex.presentation.programs.CampaignManagementScreen
import com.vector.verevcodex.presentation.programs.LoyaltyProgramManagementScreen
import com.vector.verevcodex.presentation.programs.RewardManagementScreen
import com.vector.verevcodex.presentation.reports.ReportExportScreen
import com.vector.verevcodex.presentation.scan.ScanScreen
import com.vector.verevcodex.presentation.scan.ScanViewModel
import com.vector.verevcodex.presentation.settings.BrandingScreen
import com.vector.verevcodex.presentation.settings.BusinessDetailsScreen
import com.vector.verevcodex.presentation.settings.BusinessSettingsScreen
import com.vector.verevcodex.presentation.settings.PaymentMethodsScreen
import com.vector.verevcodex.presentation.settings.PrivacyTermsScreen
import com.vector.verevcodex.presentation.settings.StoreManagementScreen
import com.vector.verevcodex.presentation.staff.StaffManagementScreen
import com.vector.verevcodex.presentation.stores.StoreSelectionScreen
import com.vector.verevcodex.presentation.theme.VerevColors
import com.vector.verevcodex.presentation.transactions.TransactionEntryScreen

sealed class Screen(val route: String, @StringRes val labelRes: Int) {
    data object Login : Screen("login", R.string.auth_sign_in)
    data object Signup : Screen("signup", R.string.auth_create_account)
    data object ForgotPassword : Screen("forgot_password", R.string.auth_forgot_password_title)
    data object ForgotPin : Screen("forgot_pin", R.string.auth_forgot_pin_title)
    data object StoreSelection : Screen("store_selection", R.string.merchant_select_store)
    data object Dashboard : Screen("dashboard", R.string.merchant_tab_home)
    data object Scan : Screen("scan?method={method}", R.string.merchant_scan) {
        const val ARG_METHOD = "method"
        fun createRoute(method: ScanMethod? = null): String = if (method == null) {
            "scan"
        } else {
            "scan?method=${method.name.lowercase()}"
        }
    }
    data object Customers : Screen("customers", R.string.merchant_tab_customers)
    data object AddCustomer : Screen("add_customer", R.string.merchant_add_customer_title)
    data object CustomerProfile : Screen("customer_profile/{customerId}", R.string.merchant_customer_profile) {
        const val ARG_CUSTOMER_ID = "customerId"
        fun createRoute(customerId: String): String = "customer_profile/$customerId"
    }
    data object CustomerCredentialManagement : Screen("customer_credentials/{customerId}", R.string.merchant_customer_credentials_section) {
        const val ARG_CUSTOMER_ID = "customerId"
        fun createRoute(customerId: String): String = "customer_credentials/$customerId"
    }
    data object Transactions : Screen("transactions", R.string.merchant_transactions)
    data object Rewards : Screen("rewards", R.string.merchant_rewards_title)
    data object LoyaltyPrograms : Screen("programs", R.string.merchant_tab_programs)
    data object Campaigns : Screen("campaigns", R.string.merchant_campaigns_title)
    data object Staff : Screen("staff", R.string.merchant_staff)
    data object BusinessDetails : Screen("business_details", R.string.merchant_business_details_title)
    data object PaymentMethods : Screen("payment_methods", R.string.merchant_payment_methods_title)
    data object Branding : Screen("branding", R.string.merchant_branding_title)
    data object Privacy : Screen("privacy_terms", R.string.merchant_privacy_title)
    data object StoreManagement : Screen("store_management", R.string.merchant_store_management_title)
    data object Analytics : Screen("analytics", R.string.merchant_tab_analytics)
    data object StaffAnalytics : Screen("staff_analytics", R.string.merchant_staff_analytics_title)
    data object Reports : Screen("reports", R.string.merchant_reports)
    data object Settings : Screen("settings", R.string.merchant_tab_settings)
}

private data class TopLevelDestination(val screen: Screen, val icon: ImageVector)

@Composable
fun AuthNavHost(
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Login.route,
    navController: NavHostController = rememberNavController(),
    onAuthenticated: () -> Unit,
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoggedIn = onAuthenticated,
                onSignup = { navController.navigate(Screen.Signup.route) },
                onForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
            )
        }
        composable(Screen.Signup.route) {
            SignupScreen(
                onBack = { navController.popBackStack() },
                onLoginRequested = { navController.popBackStack(Screen.Login.route, false) },
                onForgotPasswordRequested = { navController.navigate(Screen.ForgotPassword.route) },
                onSignupCompleted = onAuthenticated,
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(
                mode = RecoveryMode.PASSWORD,
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Screen.ForgotPin.route) {
            ForgotPasswordScreen(
                mode = RecoveryMode.PIN,
                onBackToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Screen.StoreSelection.route) {
            StoreSelectionScreen(onSelected = { onAuthenticated() })
        }
    }
}

@Composable
fun MerchantAppNavHost(
    modifier: Modifier = Modifier,
    scanViewModel: ScanViewModel,
    startDestination: String = Screen.Dashboard.route,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(navController = navController, startDestination = startDestination, modifier = modifier) {
        composable(Screen.Dashboard.route) {
            MerchantShell(navController = navController) { padding ->
                DashboardScreen(
                    contentPadding = padding,
                    onOpenScan = { method, rememberChoice ->
                        scanViewModel.enterScan(method = method, rememberChoice = rememberChoice)
                        navController.navigate(Screen.Scan.createRoute(method))
                    },
                    onOpenAddCustomer = { navController.navigate(Screen.AddCustomer.route) },
                    onOpenPromotions = { navController.navigate(Screen.Campaigns.route) },
                )
            }
        }
        composable(
            route = Screen.Scan.route,
            arguments = listOf(navArgument(Screen.Scan.ARG_METHOD) {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }),
        ) { entry ->
            val methodArg = entry.arguments?.getString(Screen.Scan.ARG_METHOD)
            val initialMethod = when (methodArg?.lowercase()) {
                "nfc" -> ScanMethod.NFC
                "barcode" -> ScanMethod.BARCODE
                else -> null
            }
            MerchantShell(navController = navController) { padding ->
                ScanScreen(
                    viewModel = scanViewModel,
                    contentPadding = padding,
                    initialMethod = initialMethod,
                    onBack = { navController.popBackStack() },
                    onOpenCustomer = { customerId ->
                        navController.navigate(Screen.CustomerProfile.createRoute(customerId))
                    },
                )
            }
        }
        composable(Screen.Customers.route) {
            MerchantShell(navController = navController) { padding ->
                CustomerListScreen(
                    contentPadding = padding,
                    onOpenCustomer = { customerId ->
                        navController.navigate(Screen.CustomerProfile.createRoute(customerId))
                    },
                )
            }
        }
        composable(Screen.AddCustomer.route) {
            MerchantShell(navController = navController) { padding ->
                AddCustomerScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                    onOpenProfile = { customerId ->
                        navController.navigate(Screen.CustomerProfile.createRoute(customerId)) {
                            popUpTo(Screen.AddCustomer.route) { inclusive = true }
                        }
                    },
                )
            }
        }
        composable(
            route = Screen.CustomerProfile.route,
            arguments = listOf(navArgument(Screen.CustomerProfile.ARG_CUSTOMER_ID) { type = NavType.StringType }),
        ) {
            MerchantShell(navController = navController) { padding ->
                CustomerProfileScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                    onManageCredentials = { customerId ->
                        navController.navigate(Screen.CustomerCredentialManagement.createRoute(customerId))
                    },
                )
            }
        }
        composable(
            route = Screen.CustomerCredentialManagement.route,
            arguments = listOf(navArgument(Screen.CustomerCredentialManagement.ARG_CUSTOMER_ID) { type = NavType.StringType }),
        ) {
            MerchantShell(navController = navController) { padding ->
                CustomerCredentialManagementScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.Transactions.route) {
            MerchantShell(navController = navController) { padding ->
                TransactionEntryScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.Rewards.route) {
            MerchantShell(navController = navController) { padding ->
                RewardManagementScreen(contentPadding = padding)
            }
        }
        composable(Screen.LoyaltyPrograms.route) {
            MerchantShell(navController = navController) { padding ->
                LoyaltyProgramManagementScreen(
                    contentPadding = padding,
                    onOpenRewards = { navController.navigate(Screen.Rewards.route) },
                    onOpenCampaigns = { navController.navigate(Screen.Campaigns.route) },
                )
            }
        }
        composable(Screen.Campaigns.route) {
            MerchantShell(navController = navController) { padding ->
                CampaignManagementScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.Staff.route) {
            MerchantShell(navController = navController) { padding ->
                StaffManagementScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.BusinessDetails.route) {
            MerchantShell(navController = navController) { padding ->
                BusinessDetailsScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                    onOpenStoreManagement = { navController.navigate(Screen.StoreManagement.route) },
                    onOpenBranding = { navController.navigate(Screen.Branding.route) },
                )
            }
        }
        composable(Screen.PaymentMethods.route) {
            MerchantShell(navController = navController) { padding ->
                PaymentMethodsScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.Branding.route) {
            MerchantShell(navController = navController) { padding ->
                BrandingScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.Privacy.route) {
            MerchantShell(navController = navController) { padding ->
                PrivacyTermsScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.StoreManagement.route) {
            MerchantShell(navController = navController) { padding ->
                StoreManagementScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.Analytics.route) {
            MerchantShell(navController = navController) { padding ->
                AnalyticsDashboardScreen(
                    contentPadding = padding,
                    onOpenStaffAnalytics = { navController.navigate(Screen.StaffAnalytics.route) },
                    onOpenReports = { navController.navigate(Screen.Reports.route) },
                )
            }
        }
        composable(Screen.StaffAnalytics.route) {
            MerchantShell(navController = navController) { padding ->
                StaffAnalyticsScreen(contentPadding = padding)
            }
        }
        composable(Screen.Reports.route) {
            MerchantShell(navController = navController) { padding ->
                ReportExportScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                )
            }
        }
        composable(Screen.Settings.route) {
            MerchantShell(navController = navController) { padding ->
                BusinessSettingsScreen(
                    contentPadding = padding,
                    onOpenBusinessDetails = { navController.navigate(Screen.BusinessDetails.route) },
                    onOpenPrograms = { navController.navigate(Screen.LoyaltyPrograms.route) },
                    onOpenStaff = { navController.navigate(Screen.Staff.route) },
                    onOpenReports = { navController.navigate(Screen.Reports.route) },
                    onOpenPayments = { navController.navigate(Screen.PaymentMethods.route) },
                    onOpenBranding = { navController.navigate(Screen.Branding.route) },
                    onOpenPrivacy = { navController.navigate(Screen.Privacy.route) },
                )
            }
        }
    }
}

@Composable
private fun MerchantShell(
    navController: NavHostController,
    shellViewModel: ShellViewModel = hiltViewModel(),
    content: @Composable (PaddingValues) -> Unit,
) {
    val destinations = listOf(
        TopLevelDestination(Screen.Dashboard, Icons.Default.Home),
        TopLevelDestination(Screen.Analytics, Icons.Default.Analytics),
        TopLevelDestination(Screen.LoyaltyPrograms, Icons.Default.Campaign),
        TopLevelDestination(Screen.Customers, Icons.Default.Groups),
        TopLevelDestination(Screen.Settings, Icons.Default.Settings),
    )
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val topLevelRoutes = destinations.map { it.screen.route }.toSet()
    val shellState by shellViewModel.uiState.collectAsStateWithLifecycle()
    val showTopBar = currentRoute in topLevelRoutes
    val showBottomBar = currentRoute in topLevelRoutes
    val showFab = currentRoute in topLevelRoutes

    Scaffold(
        containerColor = VerevColors.AppBackground,
        topBar = {
            if (showTopBar) {
                MerchantTopBar(
                    currentUser = shellState.currentUser,
                    selectedStore = shellState.selectedStore,
                    stores = shellState.stores,
                    onStoreSelected = shellViewModel::selectStore,
                )
            }
        },
        floatingActionButton = {
            if (showFab) {
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Scan.createRoute(ScanMethod.NFC)) },
                    containerColor = VerevColors.Gold,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Icon(Icons.Default.CreditCard, contentDescription = stringResource(R.string.merchant_scan))
                }
            }
        },
        bottomBar = {
            if (showBottomBar) {
                MerchantBottomBar(
                    destinations = destinations.map { MerchantBottomDestination(it.screen.route, it.screen.labelRes, it.icon) },
                    currentRoute = currentRoute,
                    onDestinationClick = { destination ->
                        navController.navigate(destination.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().background(VerevColors.AppBackground)) {
            content(padding)
        }
    }
}
