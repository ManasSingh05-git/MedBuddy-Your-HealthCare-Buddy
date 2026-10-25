package com.manas.medbuddy.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.manas.medbuddy.screens.AddEditMedicineScreen
import com.manas.medbuddy.screens.AddMedicineScreen
import com.manas.medbuddy.screens.AdherenceReportScreen
import com.manas.medbuddy.screens.AiHealthInsightsScreen
import com.manas.medbuddy.screens.AllergyRegistryScreen
import com.manas.medbuddy.screens.AppointmentOrganizerScreen
import com.manas.medbuddy.screens.AppointmentsScreen
import com.manas.medbuddy.screens.CaregiverDashboardScreen
import com.manas.medbuddy.screens.DoctorPrepScreen
import com.manas.medbuddy.screens.DocumentSharingScreen
import com.manas.medbuddy.screens.FamilyCalendarScreen
import com.manas.medbuddy.screens.FamilyProfilesScreen
import com.manas.medbuddy.screens.ForgotPasswordScreen
import com.manas.medbuddy.screens.HealthCardScreen
import com.manas.medbuddy.screens.HealthMapScreen
import com.manas.medbuddy.screens.HealthScreen
import com.manas.medbuddy.screens.HealthTimelineScreen
import com.manas.medbuddy.screens.HomeScreen
import com.manas.medbuddy.screens.HospitalDetailsScreen
import com.manas.medbuddy.screens.HospitalFinderScreen
import com.manas.medbuddy.screens.LoginScreen
import com.manas.medbuddy.screens.MedBuddyAiScreen
import com.manas.medbuddy.screens.MedicalGlossaryScreen
import com.manas.medbuddy.screens.MedicalVaultScreen
import com.manas.medbuddy.screens.MedicineListScreen
import com.manas.medbuddy.screens.MedicineReminderScreen
import com.manas.medbuddy.screens.MedicineScreen
import com.manas.medbuddy.screens.MenstrualTrackerScreen
import com.manas.medbuddy.screens.MoreHealthOptionsScreen
import com.manas.medbuddy.screens.MovementCoachScreen
import com.manas.medbuddy.screens.PersonalInformationScreen
import com.manas.medbuddy.screens.PrescriptionScannerScreen
import com.manas.medbuddy.screens.ProfileScreen
import com.manas.medbuddy.screens.ReportComparisonScreen
import com.manas.medbuddy.screens.ReportExplainerScreen
import com.manas.medbuddy.screens.ReportsScreen
import com.manas.medbuddy.screens.SettingsScreen
import com.manas.medbuddy.screens.SignupScreen
import com.manas.medbuddy.screens.SleepReminderScreen
import com.manas.medbuddy.screens.SleepTrackerScreen
import com.manas.medbuddy.screens.SosScreen
import com.manas.medbuddy.screens.SosSettingsScreen
import com.manas.medbuddy.screens.SplashScreen
import com.manas.medbuddy.screens.SymptomJournalScreen
import com.manas.medbuddy.screens.ToolsScreen
import com.manas.medbuddy.screens.VaccinationManagerScreen
import com.manas.medbuddy.screens.WaterIntakeScreen
import com.manas.medbuddy.screens.WellnessCoachScreen

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Signup = "signup"
    const val ForgotPassword = "forgot_password"
    const val Home = "home"
    const val Tools = "tools"
    const val MoreHealthOptions = "more_health_options"
    const val Assistant = "assistant"
    const val Appointments = "appointments"
    const val Reports = "reports"
    const val SosEmergency = "sos_emergency"
    const val MedicineList = "medicine_list"
    const val AddEditMedicine = "add_edit_medicine"
    const val Medicines = "medicines"
    const val AddMedicine = "add_medicine"
    const val MedicineReminder = "medicine_reminder"
    const val PrescriptionScanner = "prescription_scanner"
    const val ReportExplainer = "report_explainer"
    const val MedicalVault = "medical_vault"
    const val AllergyRegistry = "allergy_registry"
    const val VaccinationManager = "vaccination_manager"
    const val HealthTimeline = "health_timeline"
    const val HealthCard = "health_card"
    const val Health = "health"
    const val Profile = "profile"
    const val PersonalInformation = "personal_information"
    const val Settings = "settings"
    const val SosSettings = "sos_settings"
    const val HospitalFinder = "hospital_finder"
    const val HospitalDetails = "hospital_details"
    const val HealthMap = "health_map"
    const val SleepTracker = "sleep_tracker"
    const val SleepReminders = "sleep_reminders"
    const val WaterTracker = "water_tracker"
    const val AiInsights = "ai_insights"
    const val SymptomJournal = "symptom_journal"
    const val DoctorPrep = "doctor_prep"
    const val MedicalGlossary = "medical_glossary"
    const val WellnessCoach = "wellness_coach"
    const val ReportComparison = "report_comparison"
    const val FamilyProfiles = "family_profiles"
    const val CaregiverDashboard = "caregiver_dashboard"
    const val FamilyCalendar = "family_calendar"
    const val AdherenceReports = "adherence_reports"
    const val DocumentSharing = "document_sharing"
    const val MenstrualTracker = "menstrual_tracker"
    const val MovementCoach = "movement_coach"
    const val AppointmentOrganizer = "appointment_organizer"
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
                onAddMedicine = { navController.navigate(Routes.AddEditMedicine) },
                onOpenMedicineList = { navController.navigate(Routes.MedicineList) },
                onEditMedicine = { medicineId ->
                    navController.navigate("${Routes.AddEditMedicine}?medicineId=$medicineId")
                },
                onNavigateToMedicines = { navController.navigate(Routes.Medicines) },
                onNavigateToHealth = { navController.navigate(Routes.Health) },
                onNavigateToSettings = { navController.navigate(Routes.Profile) },
                onNavigateToSosSettings = { navController.navigate(Routes.SosEmergency) },
                onNavigateToHospitalFinder = { navController.navigate(Routes.HospitalFinder) },
                onNavigateToHealthMap = { navController.navigate(Routes.HealthMap) },
                onNavigateToSleepTracker = { navController.navigate(Routes.SleepTracker) },
                onNavigateToPrescriptionScanner = { navController.navigate(Routes.PrescriptionScanner) },
                onNavigateToReportExplainer = { navController.navigate(Routes.ReportExplainer) },
                onNavigateToMedicalVault = { navController.navigate(Routes.MedicalVault) },
                onNavigateToAllergyRegistry = { navController.navigate(Routes.AllergyRegistry) },
                onNavigateToVaccinationManager = { navController.navigate(Routes.VaccinationManager) },
                onNavigateToHealthTimeline = { navController.navigate(Routes.HealthTimeline) },
                onNavigateToHealthCard = { navController.navigate(Routes.HealthCard) },
                onNavigateToSymptomJournal = { navController.navigate(Routes.SymptomJournal) },
                onNavigateToDoctorPrep = { navController.navigate(Routes.DoctorPrep) },
                onNavigateToMedicalGlossary = { navController.navigate(Routes.MedicalGlossary) },
                onNavigateToWellnessCoach = { navController.navigate(Routes.WellnessCoach) },
                onNavigateToReportComparison = { navController.navigate(Routes.ReportComparison) },
                onNavigateToFamilyProfiles = { navController.navigate(Routes.FamilyProfiles) },
                onNavigateToCaregiverDashboard = { navController.navigate(Routes.CaregiverDashboard) },
                onNavigateToFamilyCalendar = { navController.navigate(Routes.FamilyCalendar) },
                onNavigateToAdherenceReports = { navController.navigate(Routes.AdherenceReports) },
                onNavigateToDocumentSharing = { navController.navigate(Routes.DocumentSharing) },
                onNavigateToMenstrualTracker = { navController.navigate(Routes.MenstrualTracker) },
                onNavigateToMovementCoach = { navController.navigate(Routes.MovementCoach) },
                onNavigateToMoreHealthOptions = { navController.navigate(Routes.MoreHealthOptions) },
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

        // More Tools Screen
        composable(Routes.Tools) {
            ToolsScreen(
                onNavigateToRoute = { route ->
                    if (route.isNotBlank()) navController.navigate(route)
                },
                onTabSelected = { tab ->
                    handleBottomTabNavigation(navController, tab)
                }
            )
        }

        // Dedicated More Health Options Hub Screen
        composable(Routes.MoreHealthOptions) {
            MoreHealthOptionsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRoute = { route ->
                    if (route.isNotBlank()) navController.navigate(route)
                }
            )
        }

        // AI Assistant Screen
        composable(Routes.Assistant) {
            MedBuddyAiScreen(
                onBack = { navController.popBackStack() },
                onTabSelected = { tab ->
                    handleBottomTabNavigation(navController, tab)
                }
            )
        }

        // Appointments Screen
        composable(Routes.Appointments) {
            AppointmentsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Reports Screen
        composable(Routes.Reports) {
            ReportsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // SOS Emergency Screen
        composable(Routes.SosEmergency) {
            SosScreen(
                onBack = { navController.popBackStack() },
                onNavigateToSosSettings = { navController.navigate(Routes.SosSettings) }
            )
        }

        // Health Card Screen
        composable(Routes.HealthCard) {
            HealthCardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Health Timeline Screen
        composable(Routes.HealthTimeline) {
            HealthTimelineScreen(
                onBack = { navController.popBackStack() },
                onNavigateToRoute = { route ->
                    if (route.isNotBlank()) navController.navigate(route)
                }
            )
        }

        // Vaccination Manager Screen
        composable(Routes.VaccinationManager) {
            VaccinationManagerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Allergy Registry Screen
        composable(Routes.AllergyRegistry) {
            AllergyRegistryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Medical Vault Screen
        composable(Routes.MedicalVault) {
            MedicalVaultScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(Routes.Settings) {
            SettingsScreen(
                onNavigateToSosSettings = { navController.navigate(Routes.SosSettings) },
                onNavigateToSleepReminders = { navController.navigate(Routes.SleepReminders) },
                onNavigateToPersonalInformation = { navController.navigate(Routes.PersonalInformation) },
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Home) { inclusive = true }
                    }
                }
            )
        }

        // Personal Information Screen
        composable(Routes.PersonalInformation) {
            PersonalInformationScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // SOS Settings Screen
        composable(Routes.SosSettings) {
            SosSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Medicine List Screen
        composable(Routes.MedicineList) {
            MedicineListScreen(
                onBack = { navController.popBackStack() },
                onAddMedicine = { navController.navigate(Routes.AddEditMedicine) },
                onEditMedicine = { medicineId ->
                    navController.navigate("${Routes.AddEditMedicine}?medicineId=$medicineId")
                }
            )
        }

        // Add/Edit Medicine Screen
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

        // Prescription Scanner Screen
        composable(Routes.PrescriptionScanner) {
            PrescriptionScannerScreen(
                onBack = { navController.popBackStack() },
                onNavigateToMedicineList = { navController.navigate(Routes.MedicineList) }
            )
        }

        // Report Explainer Screen
        composable(Routes.ReportExplainer) {
            ReportExplainerScreen(
                onBack = { navController.popBackStack() }
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
                onTabSelected = { tab ->
                    handleBottomTabNavigation(navController, tab)
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

        // Hospital Finder
        composable(Routes.HospitalFinder) {
            HospitalFinderScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Hospital Details Screen
        composable(Routes.HospitalDetails) {
            HospitalDetailsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Health Map
        composable(Routes.HealthMap) {
            HealthMapScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Sleep Tracker
        composable(Routes.SleepTracker) {
            SleepTrackerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Sleep Reminders
        composable(Routes.SleepReminders) {
            SleepReminderScreen(
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

        composable(Routes.CaregiverDashboard) {
            CaregiverDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FamilyCalendar) {
            FamilyCalendarScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AdherenceReports) {
            AdherenceReportScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DocumentSharing) {
            DocumentSharingScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MenstrualTracker) {
            MenstrualTrackerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MovementCoach) {
            MovementCoachScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AppointmentOrganizer) {
            AppointmentOrganizerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Water Tracker
        composable(Routes.WaterTracker) {
            WaterIntakeScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // AI Insights
        composable(Routes.AiInsights) {
            AiHealthInsightsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

private fun handleBottomTabNavigation(navController: NavHostController, tab: String) {
    when (tab) {
        "home" -> navController.navigate(Routes.Home) { launchSingleTop = true }
        "tools" -> navController.navigate(Routes.Tools) { launchSingleTop = true }
        "assistant" -> navController.navigate(Routes.Assistant) { launchSingleTop = true }
        "insights" -> navController.navigate(Routes.Health) { launchSingleTop = true }
        "profile" -> navController.navigate(Routes.Profile) { launchSingleTop = true }
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
