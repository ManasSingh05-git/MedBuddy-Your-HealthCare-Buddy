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
import com.manas.medbuddy.screens.ExpenseTrackerScreen
import com.manas.medbuddy.screens.FamilyCalendarScreen
import com.manas.medbuddy.screens.FamilyProfilesScreen
import com.manas.medbuddy.screens.ForgotPasswordScreen
import com.manas.medbuddy.screens.HealthCardScreen
import com.manas.medbuddy.screens.HealthMapScreen
import com.manas.medbuddy.screens.HealthScreen
import com.manas.medbuddy.screens.HealthTimelineScreen
import com.manas.medbuddy.screens.HomeScreen
import com.manas.medbuddy.screens.HospitalChecklistScreen
import com.manas.medbuddy.screens.HospitalDetailsScreen
import com.manas.medbuddy.screens.HospitalFinderScreen
import com.manas.medbuddy.screens.InsuranceOrganizerScreen
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
import com.manas.medbuddy.screens.RefillPlannerScreen
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
    const val Assistant = "assistant"
    const val Appointments = "appointments"
    const val Reports = "reports"
    const val SosEmergency = "sos_emergency"
    const val MoreHealthOptions = "more_health_options"
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
    const val AppointmentOrganizer = "appointment_organizer"
    const val RefillPlanner = "refill_planner"
    const val ExpenseTracker = "expense_tracker"
    const val InsuranceOrganizer = "insurance_organizer"
    const val HospitalChecklist = "hospital_checklist"
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
                onAddMedicine = { safeNavigate(navController, Routes.AddEditMedicine) },
                onOpenMedicineList = { safeNavigate(navController, Routes.MedicineList) },
                onEditMedicine = { medicineId ->
                    safeNavigate(navController, "${Routes.AddEditMedicine}?medicineId=$medicineId")
                },
                onNavigateToMedicines = { safeNavigate(navController, Routes.Medicines) },
                onNavigateToHealth = { safeNavigate(navController, Routes.Health) },
                onNavigateToSettings = { safeNavigate(navController, Routes.Profile) },
                onNavigateToSosSettings = { safeNavigate(navController, Routes.SosEmergency) },
                onNavigateToHospitalFinder = { safeNavigate(navController, Routes.HospitalFinder) },
                onNavigateToHealthMap = { safeNavigate(navController, Routes.HealthMap) },
                onNavigateToSleepTracker = { safeNavigate(navController, Routes.SleepTracker) },
                onNavigateToPrescriptionScanner = { safeNavigate(navController, Routes.PrescriptionScanner) },
                onNavigateToReportExplainer = { safeNavigate(navController, Routes.ReportExplainer) },
                onNavigateToMedicalVault = { safeNavigate(navController, Routes.MedicalVault) },
                onNavigateToAllergyRegistry = { safeNavigate(navController, Routes.AllergyRegistry) },
                onNavigateToVaccinationManager = { safeNavigate(navController, Routes.VaccinationManager) },
                onNavigateToHealthTimeline = { safeNavigate(navController, Routes.HealthTimeline) },
                onNavigateToHealthCard = { safeNavigate(navController, Routes.HealthCard) },
                onNavigateToAppointmentOrganizer = { safeNavigate(navController, Routes.AppointmentOrganizer) },
                onNavigateToSymptomJournal = { safeNavigate(navController, Routes.SymptomJournal) },
                onNavigateToDoctorPrep = { safeNavigate(navController, Routes.DoctorPrep) },
                onNavigateToMedicalGlossary = { safeNavigate(navController, Routes.MedicalGlossary) },
                onNavigateToWellnessCoach = { safeNavigate(navController, Routes.WellnessCoach) },
                onNavigateToReportComparison = { safeNavigate(navController, Routes.ReportComparison) },
                onNavigateToFamilyProfiles = { safeNavigate(navController, Routes.FamilyProfiles) },
                onNavigateToCaregiverDashboard = { safeNavigate(navController, Routes.CaregiverDashboard) },
                onNavigateToFamilyCalendar = { safeNavigate(navController, Routes.FamilyCalendar) },
                onNavigateToAdherenceReports = { safeNavigate(navController, Routes.AdherenceReports) },
                onNavigateToDocumentSharing = { safeNavigate(navController, Routes.DocumentSharing) },
                onNavigateToMenstrualTracker = { safeNavigate(navController, Routes.MenstrualTracker) },
                onNavigateToMovementCoach = { safeNavigate(navController, Routes.MovementCoach) },
                onNavigateToRefillPlanner = { safeNavigate(navController, Routes.RefillPlanner) },
                onNavigateToExpenseTracker = { safeNavigate(navController, Routes.ExpenseTracker) },
                onNavigateToInsuranceOrganizer = { safeNavigate(navController, Routes.InsuranceOrganizer) },
                onNavigateToHospitalChecklist = { safeNavigate(navController, Routes.HospitalChecklist) },
                onNavigateToTools = { safeNavigate(navController, Routes.Tools) },
                onNavigateToAssistant = { safeNavigate(navController, Routes.Assistant) },
                onNavigateToMoreHealthOptions = { safeNavigate(navController, Routes.MoreHealthOptions) },
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

        // Tools Screen
        composable(Routes.Tools) {
            ToolsScreen(
                onNavigateToRoute = { route ->
                    safeNavigate(navController, route)
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
                    safeNavigate(navController, route)
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
                onNavigateToSosSettings = { safeNavigate(navController, Routes.SosSettings) }
            )
        }

        // Hospital Visit Checklist Screen
        composable(Routes.HospitalChecklist) {
            HospitalChecklistScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Insurance Organizer Screen
        composable(Routes.InsuranceOrganizer) {
            InsuranceOrganizerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Expense Tracker Screen
        composable(Routes.ExpenseTracker) {
            ExpenseTrackerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Refill Planner Screen
        composable(Routes.RefillPlanner) {
            RefillPlannerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Appointment Organizer Screen
        composable(Routes.AppointmentOrganizer) {
            AppointmentOrganizerScreen(
                onBack = { navController.popBackStack() }
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
                    safeNavigate(navController, route)
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

        // Symptom Journal Screen
        composable(Routes.SymptomJournal) {
            SymptomJournalScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Doctor Prep Screen
        composable(Routes.DoctorPrep) {
            DoctorPrepScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Medical Glossary Screen
        composable(Routes.MedicalGlossary) {
            MedicalGlossaryScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Wellness Coach Screen
        composable(Routes.WellnessCoach) {
            WellnessCoachScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Report Comparison Screen
        composable(Routes.ReportComparison) {
            ReportComparisonScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Family Profiles Screen
        composable(Routes.FamilyProfiles) {
            FamilyProfilesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Caregiver Dashboard Screen
        composable(Routes.CaregiverDashboard) {
            CaregiverDashboardScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Family Calendar Screen
        composable(Routes.FamilyCalendar) {
            FamilyCalendarScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Adherence Reports Screen
        composable(Routes.AdherenceReports) {
            AdherenceReportScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Document Sharing Screen
        composable(Routes.DocumentSharing) {
            DocumentSharingScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Menstrual Tracker Screen
        composable(Routes.MenstrualTracker) {
            MenstrualTrackerScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Movement Coach Screen
        composable(Routes.MovementCoach) {
            MovementCoachScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(Routes.Settings) {
            SettingsScreen(
                onNavigateToSosSettings = { safeNavigate(navController, Routes.SosSettings) },
                onNavigateToSleepReminders = { safeNavigate(navController, Routes.SleepReminders) },
                onNavigateToPersonalInformation = { safeNavigate(navController, Routes.PersonalInformation) },
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
                onAddMedicine = { safeNavigate(navController, Routes.AddEditMedicine) },
                onEditMedicine = { medicineId ->
                    safeNavigate(navController, "${Routes.AddEditMedicine}?medicineId=$medicineId")
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
                onNavigateToMedicineList = { safeNavigate(navController, Routes.MedicineList) }
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
                    safeNavigate(navController, Routes.AddMedicine)
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
                    safeNavigate(navController, Routes.SosSettings)
                },
                onNavigateToSettings = {
                    safeNavigate(navController, Routes.Settings)
                },
                onNavigateToRoute = { route ->
                    safeNavigate(navController, route)
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

private fun safeNavigate(navController: NavHostController, route: String) {
    if (route.isBlank()) return
    try {
        navController.navigate(route) {
            launchSingleTop = true
        }
    } catch (_: Exception) {
        // Safe navigation fallback prevents app crash on invalid routes
    }
}

private fun handleBottomTabNavigation(navController: NavHostController, tab: String) {
    val route = when (tab) {
        "home" -> Routes.Home
        "tools" -> Routes.Tools
        "assistant" -> Routes.Assistant
        "insights" -> Routes.Health
        "profile" -> Routes.Profile
        else -> Routes.Home
    }
    safeNavigate(navController, route)
}

private fun NavHostController.navigateToHome() {
    navigate(Routes.Home) {
        popUpTo(Routes.Login) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
