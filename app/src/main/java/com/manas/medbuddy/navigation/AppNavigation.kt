
package com.manas.medbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.manas.medbuddy.screens.ForgotPasswordScreen
import com.manas.medbuddy.screens.HealthMapScreen
import com.manas.medbuddy.screens.HomeScreen
import com.manas.medbuddy.screens.HospitalFinderScreen
import com.manas.medbuddy.screens.LoginScreen
import com.manas.medbuddy.screens.SettingsScreen
import com.manas.medbuddy.screens.SignupScreen
import com.manas.medbuddy.screens.SleepTrackerScreen
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
    const val Settings = "settings"
    const val SosSettings = "sos_settings"
    const val HospitalFinder = "hospital_finder"
    const val HealthMap = "health_map"
    const val SleepTracker = "sleep_tracker"
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Splash
    ) {

        // Splash Screen
        composable(Routes.Splash) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Splash) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Login Screen
        composable(Routes.Login) {
            LoginScreen(
                onLogin = {
                    navController.navigateToHome()
                },
                onSignup = {
                    navController.navigate(Routes.Signup)
                },
                onForgotPassword = {
                    navController.navigate(Routes.ForgotPassword)
                }
            )
        }

        // Signup Screen
        composable(Routes.Signup) {
            SignupScreen(
                onSignup = {
                    navController.navigateToHome()
                },
                onLogin = {
                    navController.popBackStack()
                }
            )
        }

        // Forgot Password Screen
        composable(Routes.ForgotPassword) {
            ForgotPasswordScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Home Screen
        composable(Routes.Home) {
            HomeScreen(
                onNavigateToMedicines = {
                    navController.navigate(Routes.Medicines)
                },
                onNavigateToHealth = {
                    navController.navigate(Routes.Health)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings)
                },
                onNavigateToSosSettings = {
                    navController.navigate(Routes.SosSettings)
                },
                onNavigateToHospitalFinder = { navController.navigate(Routes.HospitalFinder) },
                onNavigateToHealthMap = { navController.navigate(Routes.HealthMap) },
                onNavigateToSleepTracker = { navController.navigate(Routes.SleepTracker) },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Medicines Screen
        composable(Routes.Medicines) {
            MedicineScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Health Screen
        composable(Routes.Health) {
            HealthScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // Settings Screen
        composable(Routes.Settings) {
            SettingsScreen(
                onNavigateToSosSettings = {
                    navController.navigate(Routes.SosSettings)
                },
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // SOS Settings Screen
        composable(Routes.SosSettings) {
            SosSettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        composable(Routes.HospitalFinder) {
            HospitalFinderScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HealthMap) {
            HealthMapScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.SleepTracker) {
            SleepTrackerScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun NavHostController.navigateToHome() {
    navigate(Routes.Home) {
        popUpTo(Routes.Login) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
