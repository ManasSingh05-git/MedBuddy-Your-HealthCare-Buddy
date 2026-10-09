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
import com.manas.medbuddy.screens.MedicineScreen
import com.manas.medbuddy.screens.HealthScreen

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Signup = "signup"
    const val ForgotPassword = "forgot_password"
    const val Home = "home"
    const val Medicines = "medicines"
    const val Health = "health"
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
                onNavigateToMedicines = { navController.navigate(Routes.Medicines) },
                onNavigateToHealth = { navController.navigate(Routes.Health) },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.Medicines) {
            MedicineScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.Health) {
            HealthScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun NavHostController.navigateToHome() {
    navigate(Routes.Home) {
        popUpTo(Routes.Login) { inclusive = true }
    }
}
