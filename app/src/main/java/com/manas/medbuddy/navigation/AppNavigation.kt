package com.manas.medbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manas.medbuddy.screens.ForgotPasswordScreen
import com.manas.medbuddy.screens.HomeScreen
import com.manas.medbuddy.screens.LoginScreen
import com.manas.medbuddy.screens.SettingsScreen
import com.manas.medbuddy.screens.SignupScreen
import com.manas.medbuddy.screens.SosSettingsScreen
import com.manas.medbuddy.screens.SplashScreen

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Signup = "signup"
    const val ForgotPassword = "forgot_password"
    const val Home = "home"
    const val Settings = "settings"
    const val SosSettings = "sos_settings"
}

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen(onFinished = {
                navController.navigate(Routes.Login) {
                    popUpTo(Routes.Splash) { inclusive = true }
                }
            })
        }
        composable(Routes.Login) {
            LoginScreen(
                onLogin = { navController.navigateToHome() },
                onSignup = { navController.navigate(Routes.Signup) },
                onForgotPassword = { navController.navigate(Routes.ForgotPassword) }
            )
        }
        composable(Routes.Signup) {
            SignupScreen(
                onSignup = { navController.navigateToHome() },
                onLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.ForgotPassword) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Home) {
            HomeScreen(
                onAddMedicine = {},
                onNavigateToSettings = { navController.navigate(Routes.Settings) },
                onNavigateToSosSettings = { navController.navigate(Routes.SosSettings) },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Settings) {
            SettingsScreen(
                onNavigateToSosSettings = { navController.navigate(Routes.SosSettings) },
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SosSettings) {
            SosSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun NavHostController.navigateToHome() {
    navigate(Routes.Home) {
        popUpTo(Routes.Login) { inclusive = true }
    }
}
