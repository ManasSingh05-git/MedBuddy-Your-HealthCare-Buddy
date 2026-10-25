package com.manas.medbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.manas.medbuddy.screens.AddMedicineScreen
import com.manas.medbuddy.screens.AiHealthInsightsScreen
import com.manas.medbuddy.screens.ForgotPasswordScreen
import com.manas.medbuddy.screens.HealthMapScreen
import com.manas.medbuddy.screens.HealthScreen
import com.manas.medbuddy.screens.HomeScreen
import com.manas.medbuddy.screens.HospitalDetailsScreen
import com.manas.medbuddy.screens.HospitalFinderScreen
import com.manas.medbuddy.screens.LoginScreen
import com.manas.medbuddy.screens.MedicineReminderScreen
import com.manas.medbuddy.screens.MedicineScreen
import com.manas.medbuddy.screens.ProfileScreen
import com.manas.medbuddy.screens.SettingsScreen
import com.manas.medbuddy.screens.SignupScreen
import com.manas.medbuddy.screens.SleepTrackerScreen
import com.manas.medbuddy.screens.SosSettingsScreen
import com.manas.medbuddy.screens.SplashScreen
import com.manas.medbuddy.screens.SymptomJournalScreen
import com.manas.medbuddy.screens.DoctorPrepScreen
import com.manas.medbuddy.screens.MedicalGlossaryScreen
import com.manas.medbuddy.screens.WellnessCoachScreen
import com.manas.medbuddy.screens.ReportComparisonScreen
import com.manas.medbuddy.screens.FamilyProfilesScreen
import com.manas.medbuddy.screens.WaterIntakeScreen

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Signup = "signup"
    const val ForgotPassword = "forgot_password"
    const val Home = "home"
    const val Medicines = "medicines"
    const val AddMedicine = "add_medicine"
    const val MedicineReminder = "medicine_reminder"
    const val Health = "health"
    const val Profile = "profile"
    const val Settings = "settings"
    const val SosSettings = "sos_settings"
    const val HospitalFinder = "hospital_finder"
    const val HospitalDetails = "hospital_details"
    const val HealthMap = "health_map"
    const val SleepTracker = "sleep_tracker"
    const val WaterTracker = "water_tracker"
    const val AiInsights = "ai_insights"
    const val SymptomJournal = "symptom_journal"
    const val DoctorPrep = "doctor_prep"
    const val MedicalGlossary = "medical_glossary"
    const val WellnessCoach = "wellness_coach"
    const val ReportComparison = "report_comparison"
    const val FamilyProfiles = "family_profiles"
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
                },
                onAlreadyAuthenticated = {
                    navController.navigateToHome()
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
                    navController.navigate(Routes.Profile)
                },
                onNavigateToSosSettings = {
                    navController.navigate(Routes.SosSettings)
                },
                onNavigateToHospitalFinder = { navController.navigate(Routes.HospitalFinder) },
                onNavigateToHealthMap = { navController.navigate(Routes.HealthMap) },
                onNavigateToSleepTracker = { navController.navigate(Routes.SleepTracker) },
                onNavigateToSymptomJournal = { navController.navigate(Routes.SymptomJournal) },
                onNavigateToDoctorPrep = { navController.navigate(Routes.DoctorPrep) },
                onNavigateToMedicalGlossary = { navController.navigate(Routes.MedicalGlossary) },
                onNavigateToWellnessCoach = { navController.navigate(Routes.WellnessCoach) },
                onNavigateToReportComparison = { navController.navigate(Routes.ReportComparison) },
                onNavigateToFamilyProfiles = { navController.navigate(Routes.FamilyProfiles) },
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
                },
                onNavigateToAddMedicine = {
                    navController.navigate(Routes.AddMedicine)
                }
            )
        }

        // Add Medicine Screen
        composable(Routes.AddMedicine) {
            AddMedicineScreen(
                onBack = {
                    navController.popBackStack()
                },
                onMedicineSaved = {
                    navController.popBackStack()
                }
            )
        }

        // Medicine Reminder Experience
        composable(Routes.MedicineReminder) {
            MedicineReminderScreen(
                onClose = {
                    navController.popBackStack()
                },
                onMarkTaken = {
                    navController.popBackStack()
                },
                onSnooze = {
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

        // Profile Screen
        composable(Routes.Profile) {
            ProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToSosSettings = {
                    navController.navigate(Routes.SosSettings)
                },
                onNavigateToSettings = {
                    navController.navigate(Routes.Settings)
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

        composable(Routes.SymptomJournal) {
            SymptomJournalScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DoctorPrep) {
            DoctorPrepScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MedicalGlossary) {
            MedicalGlossaryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.WellnessCoach) {
            WellnessCoachScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ReportComparison) {
            ReportComparisonScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FamilyProfiles) {
            FamilyProfilesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.WaterTracker) {
            WaterIntakeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AiInsights) {
            AiHealthInsightsScreen(
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
