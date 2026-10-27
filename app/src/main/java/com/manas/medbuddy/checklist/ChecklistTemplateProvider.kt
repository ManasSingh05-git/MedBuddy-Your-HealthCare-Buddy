package com.manas.medbuddy.checklist

class ChecklistTemplateProvider {

    fun getOutpatientConsultationTemplate(): List<ChecklistItem> {
        return listOf(
            ChecklistItem(title = "Previous Doctor Prescription Slips", category = ChecklistItemCategory.PRESCRIPTIONS.name),
            ChecklistItem(title = "Recent Blood & Lab Test Reports", category = ChecklistItemCategory.REPORTS.name),
            ChecklistItem(title = "Imaging Films (X-Ray / MRI / CT Scans)", category = ChecklistItemCategory.REPORTS.name),
            ChecklistItem(title = "Government Photo ID (Aadhaar / Passport / DL)", category = ChecklistItemCategory.DOCUMENTS.name),
            ChecklistItem(title = "Health Insurance e-Card / Policy Copy", category = ChecklistItemCategory.DOCUMENTS.name),
            ChecklistItem(title = "Current Active Medicines Box / List", category = ChecklistItemCategory.PRESCRIPTIONS.name),
            ChecklistItem(title = "Question: What are the side effects of new medication?", category = ChecklistItemCategory.QUESTIONS_FOR_DOCTOR.name),
            ChecklistItem(title = "Question: When is the next follow-up required?", category = ChecklistItemCategory.QUESTIONS_FOR_DOCTOR.name)
        )
    }

    fun getHospitalAdmissionTemplate(): List<ChecklistItem> {
        return listOf(
            ChecklistItem(title = "Doctor Admission Referral Letter", category = ChecklistItemCategory.DOCUMENTS.name),
            ChecklistItem(title = "Hospital UHID Patient Card", category = ChecklistItemCategory.DOCUMENTS.name),
            ChecklistItem(title = "Insurance Pre-Authorization Approval Letter", category = ChecklistItemCategory.DOCUMENTS.name),
            ChecklistItem(title = "All Pre-Op Blood Tests & Diagnostic Reports", category = ChecklistItemCategory.REPORTS.name),
            ChecklistItem(title = "Blood Group Card & Allergy Record", category = ChecklistItemCategory.DOCUMENTS.name),
            ChecklistItem(title = "Essential Daily Prescription Medicines", category = ChecklistItemCategory.PRESCRIPTIONS.name),
            ChecklistItem(title = "Personal Toiletries & Change of Clothing", category = ChecklistItemCategory.PERSONAL_ITEMS.name),
            ChecklistItem(title = "Question: What is the expected hospital stay length?", category = ChecklistItemCategory.QUESTIONS_FOR_DOCTOR.name),
            ChecklistItem(title = "Question: Are there post-procedure dietary restrictions?", category = ChecklistItemCategory.QUESTIONS_FOR_DOCTOR.name)
        )
    }
}
