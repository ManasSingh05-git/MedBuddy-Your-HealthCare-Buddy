package com.manas.medbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.manas.medbuddy.screens.AddEditMedicineScreen
import com.manas.medbuddy.screens.ForgotPasswordScreen
import com.manas.medbuddy.screens.HealthMapScreen
import com.manas.medbuddy.screens.HealthScreen
import com.manas.medbuddy.screens.HomeScreen
import com.manas.medbuddy.screens.HospitalFinderScreen
import com.manas.medbuddy.screens.LoginScreen
import com.manas.medbuddy.screens.MedicineListScreen
import com.manas.medbuddy.screens.SettingsScreen
import com.manas.medbuddy.screens.SignupScreen
import com.manas.medbuddy.screens.SleepReminderScreen
import com.manas.medbuddy.screens.SleepTrackerScreen
import com.manas.medbuddy.screens.SosSettingsScreen
import com.manas.medbuddy.screens.SplashScreen

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Signup = "signup"
    const val ForgotPassword = "forgot_password"
    const val Home = "home"
    const val MedicineList = "medicine_list"
    const val AddEditMedicine = "add_edit_medicine"
    const val Medicines = "medicines"
    const val Health = "health"
    const val Settings = "settings"
    const val SosSettings = "sos_settings"
    const val HospitalFinder = "hospital_finder"
    const val HealthMap = "health_map"
    const val SleepTracker = "sleep_tracker"
    const val SleepReminders = "sleep_reminders"
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
                onAddMedicine = { navController.navigate(Routes.AddEditMedicine) },
                onOpenMedicineList = { navController.navigate(Routes.MedicineList) },
                onEditMedicine = { medicineId ->
                    navController.navigate("${Routes.AddEditMedicine}?medicineId=$medicineId")
                },
                onNavigateToMedicines = { navController.navigate(Routes.MedicineList) },
                onNavigateToHealth = { navController.navigate(Routes.Health) },
                onNavigateToSettings = { navController.navigate(Routes.Settings) },
                onNavigateToSosSettings = { navController.navigate(Routes.SosSettings) },
                onNavigateToHospitalFinder = { navController.navigate(Routes.HospitalFinder) },
                onNavigateToHealthMap = { navController.navigate(Routes.HealthMap) },
                onNavigateToSleepTracker = { navController.navigate(Routes.SleepTracker) },
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
                onNavigateToSleepReminders = { navController.navigate(Routes.SleepReminders) },
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
        composable(Routes.MedicineList) {
            MedicineListScreen(
                onBack = { navController.popBackStack() },
                onAddMedicine = { navController.navigate(Routes.AddEditMedicine) },
                onEditMedicine = { medicineId ->
                    navController.navigate("${Routes.AddEditMedicine}?medicineId=$medicineId")
                }
            )
        }
        composable(
            route = "${Routes.AddEditMedicine}?medicineId={medicineId}",
            arguments = listOf(
                navArgument("medicineId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getString("medicineId")
            AddEditMedicineScreen(
                medicineId = medicineId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Medicines) {
            MedicineListScreen(
                onBack = { navController.popBackStack() },
                onAddMedicine = { navController.navigate(Routes.AddEditMedicine) },
                onEditMedicine = { medicineId ->
                    navController.navigate("${Routes.AddEditMedicine}?medicineId=$medicineId")
                }
            )
        }
        composable(Routes.Health) {
            HealthScreen(onBack = { navController.popBackStack() })
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
        composable(Routes.SleepReminders) {
            SleepReminderScreen(
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
