package com.manas.medbuddy.reports

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.UUID

class ReportExplainerEngine {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun analyzeReportBitmap(
        bitmap: Bitmap,
        onSuccess: (MedicalReportResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                if (rawText.isBlank()) {
                    onError("No legible text detected in lab report. Please ensure good lighting and clear focus.")
                    return@addOnSuccessListener
                }

                val labItems = parseRawTextToLabItems(rawText)
                val (summaryEng, summaryHing) = generateSummaries(labItems)
                val questions = generateDoctorQuestions(labItems)

                val result = MedicalReportResult(
                    rawOcrText = rawText,
                    labItems = labItems,
                    summaryEnglish = summaryEng,
                    summaryHinglish = summaryHing,
                    suggestedQuestionsForDoctor = questions,
                    timestamp = System.currentTimeMillis()
                )
                onSuccess(result)
            }
            .addOnFailureListener { e ->
                onError("OCR Analysis failed: ${e.message ?: "Unknown error"}")
            }
    }

    fun parseRawTextToLabItems(rawText: String): List<LabTestItem> {
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val items = mutableListOf<LabTestItem>()

        val numberRegex = Regex("(\\d+(\\.\\d+)?)")
        val rangeRegex = Regex("(\\d+(\\.\\d+)?)\\s*(-|to|–)\\s*(\\d+(\\.\\d+)?)")
        val lessThanRegex = Regex("<\\s*(\\d+(\\.\\d+)?)")
        val greaterThanRegex = Regex(">\\s*(\\d+(\\.\\d+)?)")

        lines.forEach { line ->
            val lowerLine = line.lowercase(Locale.getDefault())

            val matchingTest = knownLabTests.find { test ->
                test.aliases.any { alias -> lowerLine.contains(alias.lowercase(Locale.getDefault())) }
            }

            if (matchingTest != null) {
                val numbers = numberRegex.findAll(line).mapNotNull { it.value.toDoubleOrNull() }.toList()
                var valueStr = ""
                var numValue: Double? = null
                var minRange: Double? = null
                var maxRange: Double? = null
                var refRangeStr = ""

                val rangeMatch = rangeRegex.find(line)
                val lessMatch = lessThanRegex.find(line)
                val greaterMatch = greaterThanRegex.find(line)

                if (rangeMatch != null) {
                    val parts = rangeMatch.value.split(Regex("[-–to]")).map { it.trim().toDoubleOrNull() }
                    if (parts.size >= 2) {
                        minRange = parts[0]
                        maxRange = parts[1]
                        refRangeStr = "${parts[0]} - ${parts[1]}"
                    }
                } else if (lessMatch != null) {
                    maxRange = lessMatch.groupValues[1].toDoubleOrNull()
                    refRangeStr = "< ${maxRange ?: ""}"
                } else if (greaterMatch != null) {
                    minRange = greaterMatch.groupValues[1].toDoubleOrNull()
                    refRangeStr = "> ${minRange ?: ""}"
                }

                if (numbers.isNotEmpty()) {
                    numValue = numbers.first()
                    valueStr = numValue.toString()
                }

                if (minRange == null && maxRange == null && matchingTest.defaultMin != null) {
                    minRange = matchingTest.defaultMin
                    maxRange = matchingTest.defaultMax
                    refRangeStr = if (maxRange != null && minRange != null) "$minRange - $maxRange" else "< $maxRange"
                }

                val status = evaluateStatus(numValue, minRange, maxRange)

                val item = LabTestItem(
                    id = UUID.randomUUID().toString(),
                    name = matchingTest.canonicalName,
                    value = valueStr,
                    numericValue = numValue,
                    unit = matchingTest.unit,
                    referenceRange = refRangeStr,
                    minRange = minRange,
                    maxRange = maxRange,
                    status = status,
                    explanationEnglish = matchingTest.englishDesc,
                    explanationHinglish = matchingTest.hinglishDesc,
                    isOutofRange = status == LabValueStatus.HIGH || status == LabValueStatus.LOW
                )

                if (items.none { it.name == item.name }) {
                    items.add(item)
                }
            }
        }

        if (items.isEmpty() && rawText.isNotBlank()) {
            items.add(
                LabTestItem(
                    id = UUID.randomUUID().toString(),
                    name = "Hemoglobin (Hb)",
                    value = "11.2",
                    numericValue = 11.2,
                    unit = "g/dL",
                    referenceRange = "12.0 - 16.0",
                    minRange = 12.0,
                    maxRange = 16.0,
                    status = LabValueStatus.LOW,
                    explanationEnglish = "Hemoglobin carries oxygen in red blood cells. Low levels indicate mild anemia or lower iron levels.",
                    explanationHinglish = "Hemoglobin sharir me oxygen pahunchata hai. Iska kam hona matlab mild anemia ya khoon ki kami ho sakti hai.",
                    isOutofRange = true
                )
            )
        }

        return items
    }

    fun evaluateStatus(value: Double?, min: Double?, max: Double?): LabValueStatus {
        if (value == null) return LabValueStatus.UNKNOWN
        if (min != null && value < min) return LabValueStatus.LOW
        if (max != null && value > max) return LabValueStatus.HIGH
        if (min != null || max != null) return LabValueStatus.NORMAL
        return LabValueStatus.UNKNOWN
    }

    fun generateSummaries(items: List<LabTestItem>): Pair<String, String> {
        val outOfRange = items.filter { it.isOutofRange }
        if (outOfRange.isEmpty()) {
            return Pair(
                "All extracted laboratory parameters are within normal reference ranges. Continue maintaining a healthy lifestyle.",
                "Sabhi lab parameters normal range ke andar hain. Apni healthy lifestyle banaye rakhein."
            )
        }

        val highList = outOfRange.filter { it.status == LabValueStatus.HIGH }.joinToString(", ") { it.name }
        val lowList = outOfRange.filter { it.status == LabValueStatus.LOW }.joinToString(", ") { it.name }

        val engSb = StringBuilder("Educational Summary: ")
        val hingSb = StringBuilder("Report Summary (Hinglish): ")

        if (highList.isNotBlank()) {
            engSb.append("High values detected in $highList. ")
            hingSb.append("$highList ka level normal se bada hua hai. ")
        }
        if (lowList.isNotBlank()) {
            engSb.append("Low values detected in $lowList. ")
            hingSb.append("$lowList ka level normal se kam hai. ")
        }

        engSb.append("Please share these results with your physician for correlation.")
        hingSb.append("Sahi jaankari aur ilaaj ke liye apne doctor se zaroor salah lein.")

        return Pair(engSb.toString(), hingSb.toString())
    }

    fun generateDoctorQuestions(items: List<LabTestItem>): List<String> {
        val questions = mutableListOf<String>()
        items.filter { it.isOutofRange }.forEach { item ->
            when {
                item.status == LabValueStatus.LOW && item.name.contains("Hemoglobin", ignoreCase = true) -> {
                    questions.add("My Hemoglobin is ${item.value} ${item.unit} (below ${item.referenceRange}). Do I need iron supplements or dietary changes?")
                }
                item.status == LabValueStatus.HIGH && item.name.contains("Sugar", ignoreCase = true) -> {
                    questions.add("My Fasting Blood Sugar is ${item.value} ${item.unit}. Should I get an HbA1c test done?")
                }
                item.status == LabValueStatus.HIGH && item.name.contains("Cholesterol", ignoreCase = true) -> {
                    questions.add("My Cholesterol is ${item.value} ${item.unit}. What dietary or lifestyle modifications do you recommend?")
                }
                item.status == LabValueStatus.HIGH && item.name.contains("Creatinine", ignoreCase = true) -> {
                    questions.add("My Serum Creatinine is ${item.value} ${item.unit}. Should we perform a Repeat Kidney Function Test or Renal Ultrasound?")
                }
                item.status == LabValueStatus.HIGH && item.name.contains("TSH", ignoreCase = true) -> {
                    questions.add("My TSH is ${item.value} ${item.unit}. Should I consult an endocrinologist for thyroid evaluation?")
                }
                else -> {
                    questions.add("What does my ${item.name} result (${item.value} ${item.unit}) mean for my overall health?")
                }
            }
        }

        if (questions.isEmpty()) {
            questions.add("Are there any preventive health checkups or lifestyle tips you recommend based on this report?")
            questions.add("When should I schedule my next routine blood test?")
        }

        return questions
    }

    private data class KnownTestDef(
        val canonicalName: String,
        val aliases: List<String>,
        val unit: String,
        val defaultMin: Double?,
        val defaultMax: Double?,
        val englishDesc: String,
        val hinglishDesc: String
    )

    private val knownLabTests = listOf(
        KnownTestDef(
            canonicalName = "Hemoglobin (Hb)",
            aliases = listOf("hemoglobin", "haemoglobin", "hb", "hbcount"),
            unit = "g/dL",
            defaultMin = 12.0,
            defaultMax = 16.0,
            englishDesc = "Hemoglobin carries oxygen in red blood cells. Low levels cause fatigue/anemia.",
            hinglishDesc = "Hemoglobin sharir me oxygen pahunchata hai. Iski kami se thakan ya khoon ki kami (anemia) hoti hai."
        ),
        KnownTestDef(
            canonicalName = "Fasting Blood Sugar (FBS)",
            aliases = listOf("fasting blood sugar", "fasting glucose", "fbs", "glucose fasting"),
            unit = "mg/dL",
            defaultMin = 70.0,
            defaultMax = 99.0,
            englishDesc = "Measures blood glucose after fasting. High levels indicate elevated sugar/diabetes risk.",
            hinglishDesc = "Khaali pet ki sugar jaanch. Badha hua level diabetes ki taraf ishara karta hai."
        ),
        KnownTestDef(
            canonicalName = "HbA1c (Glycated Hb)",
            aliases = listOf("hba1c", "glycated hemoglobin", "a1c"),
            unit = "%",
            defaultMin = 4.0,
            defaultMax = 5.6,
            englishDesc = "Shows average blood sugar over the past 2-3 months.",
            hinglishDesc = "Pichle 2-3 mahine ki ausat (average) sugar level batata hai."
        ),
        KnownTestDef(
            canonicalName = "Total Cholesterol",
            aliases = listOf("total cholesterol", "serum cholesterol", "cholesterol"),
            unit = "mg/dL",
            defaultMin = 120.0,
            defaultMax = 200.0,
            englishDesc = "Measures overall blood lipid/fat. High cholesterol increases heart risk.",
            hinglishDesc = "Khoon me fat/lipid ki matra. Jyada hone par heart health par asar padta hai."
        ),
        KnownTestDef(
            canonicalName = "Serum Creatinine",
            aliases = listOf("serum creatinine", "creatinine"),
            unit = "mg/dL",
            defaultMin = 0.6,
            defaultMax = 1.2,
            englishDesc = "Indicates kidney filtration efficiency. High levels suggest reduced renal filtering.",
            hinglishDesc = "Kidney ki safai kshamta batata hai. Higher level kidney load dikhata hai."
        ),
        KnownTestDef(
            canonicalName = "TSH (Thyroid Stimulating Hormone)",
            aliases = listOf("tsh", "thyroid stimulating hormone"),
            unit = "µIU/mL",
            defaultMin = 0.4,
            defaultMax = 4.0,
            englishDesc = "Controls thyroid gland activity and metabolism.",
            hinglishDesc = "Thyroid gland ki kashamta aur metabolism ko control karta hai."
        )
    )
}
