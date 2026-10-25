package com.manas.medbuddy.scanner

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.Locale
import java.util.UUID

class PrescriptionScannerEngine {

    private val textRecognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    fun scanPrescriptionBitmap(
        bitmap: Bitmap,
        onSuccess: (PrescriptionScanResult) -> Unit,
        onError: (String) -> Unit
    ) {
        val image = InputImage.fromBitmap(bitmap, 0)
        textRecognizer.process(image)
            .addOnSuccessListener { visionText ->
                val rawText = visionText.text
                if (rawText.isBlank()) {
                    onError("No legible text detected in prescription image. Please ensure good lighting and clear camera focus.")
                    return@addOnSuccessListener
                }

                val candidates = parseRawTextToCandidates(rawText)
                val result = PrescriptionScanResult(
                    rawOcrText = rawText,
                    candidates = candidates,
                    scanTimestamp = System.currentTimeMillis()
                )
                onSuccess(result)
            }
            .addOnFailureListener { e ->
                onError("OCR Text Recognition failed: ${e.message ?: "Unknown error"}")
            }
    }

    fun parseRawTextToCandidates(rawText: String): List<ExtractedMedicineCandidate> {
        val headerKeywords = listOf("dr.", "doctor", "clinic", "hospital", "date", "patient", "address", "phone", "rx", "prescribed")
        val lines = rawText.split("\n")
            .map { it.trim() }
            .filter { line -> line.isNotBlank() && headerKeywords.none { line.lowercase(Locale.getDefault()).startsWith(it) && !line.lowercase(Locale.getDefault()).contains("tab") && !line.lowercase(Locale.getDefault()).contains("cap") } }

        val candidates = mutableListOf<ExtractedMedicineCandidate>()

        var currentName = ""
        var currentStrength = ""
        var currentDosage = ""
        var currentFrequency = ""
        var currentInstructions = ""
        var rawLineBuffer = mutableListOf<String>()

        val strengthRegex = Regex("(?i)\\b(\\d+(\\.\\d+)?\\s*(mg|g|ml|mcg|iu|meq|µg))\\b")
        val dosageRegex = Regex("(?i)\\b(\\d+\\s*(tablet|tablets|tab|capsule|capsules|cap|pills?|ml|drops?|puff|puffs)|1-0-1|1-1-1|0-0-1|1-0-0|0-1-0)\\b")
        val frequencyRegex = Regex("(?i)\\b((once|twice|thrice|3 times|2 times|4 times)\\s*(daily|a day|per day)?|od|bd|tds|qds|prn|hs|qid|bid)\\b")
        val instructionsRegex = Regex("(?i)\\b(after food|before food|with meals|after meals|before bedtime|at bedtime|empty stomach|with water|as needed|for \\d+ days)\\b")

        val knownMedPrefixes = listOf("tab", "cap", "syr", "inj", "ointment", "rx", "t.", "c.", "syp")

        lines.forEach { line ->
            val cleanLine = line.trim()
            rawLineBuffer.add(cleanLine)

            val strengthMatch = strengthRegex.find(cleanLine)?.value
            val dosageMatch = dosageRegex.find(cleanLine)?.value
            val frequencyMatch = frequencyRegex.find(cleanLine)?.value
            val instructionMatch = instructionsRegex.find(cleanLine)?.value

            val containsMedPrefix = knownMedPrefixes.any { cleanLine.lowercase(Locale.getDefault()).startsWith(it) }
            val containsCapWord = cleanLine.split(" ").any { word ->
                word.length > 2 && word.first().isUpperCase() &&
                headerKeywords.none { word.lowercase(Locale.getDefault()).contains(it) }
            }

            if (strengthMatch != null || containsMedPrefix || (containsCapWord && currentName.isBlank())) {
                if (currentName.isNotBlank() && (strengthMatch != null || containsMedPrefix)) {
                    val candidate = buildCandidate(
                        rawLineBuffer.dropLast(1).joinToString(" "),
                        currentName,
                        currentStrength,
                        currentDosage,
                        currentFrequency,
                        currentInstructions
                    )
                    candidates.add(candidate)
                    currentName = ""
                    currentStrength = ""
                    currentDosage = ""
                    currentFrequency = ""
                    currentInstructions = ""
                    rawLineBuffer = mutableListOf(cleanLine)
                }

                if (currentName.isBlank()) {
                    currentName = extractMedicineName(cleanLine)
                }
            }

            if (strengthMatch != null && currentStrength.isBlank()) {
                currentStrength = strengthMatch
            }

            if (dosageMatch != null && currentDosage.isBlank()) {
                currentDosage = dosageMatch
            }

            if (frequencyMatch != null && currentFrequency.isBlank()) {
                currentFrequency = frequencyMatch
            }

            if (instructionMatch != null && currentInstructions.isBlank()) {
                currentInstructions = instructionMatch
            }
        }

        if (currentName.isNotBlank() || currentStrength.isNotBlank()) {
            val candidate = buildCandidate(
                rawLineBuffer.joinToString(" "),
                currentName,
                currentStrength,
                currentDosage,
                currentFrequency,
                currentInstructions
            )
            candidates.add(candidate)
        }

        if (candidates.isEmpty() && rawText.isNotBlank()) {
            candidates.add(
                ExtractedMedicineCandidate(
                    id = UUID.randomUUID().toString(),
                    rawText = rawText,
                    name = lines.firstOrNull { line -> headerKeywords.none { line.lowercase(Locale.getDefault()).contains(it) } } ?: "Extracted Prescription Item",
                    dosage = "1 Tablet",
                    frequency = "Twice Daily",
                    instructions = "Take as prescribed",
                    needsPharmacistReview = true,
                    flagReason = "⚠️ Automatic OCR confidence low or non-standard format. Requires review."
                )
            )
        }

        return candidates
    }

    private fun extractMedicineName(line: String): String {
        val words = line.split(" ")
        val filteredWords = words.filter { word ->
            !word.equals("Rx", ignoreCase = true) &&
            !word.equals("Tab", ignoreCase = true) &&
            !word.equals("Cap", ignoreCase = true) &&
            !word.equals("Syr", ignoreCase = true) &&
            !word.matches(Regex("\\d+(mg|g|ml|mcg)?", RegexOption.IGNORE_CASE))
        }
        return filteredWords.joinToString(" ").ifBlank { line }
    }

    private fun buildCandidate(
        rawText: String,
        name: String,
        strength: String,
        dosage: String,
        frequency: String,
        instructions: String
    ): ExtractedMedicineCandidate {
        var needsReview = false
        var flagReason = ""

        val isHandwrittenAmbiguous = rawText.contains("?") || rawText.contains("~") || (rawText.contains("x") && rawText.contains("?"))

        if (isHandwrittenAmbiguous) {
            needsReview = true
            flagReason = "⚠️ Unclear handwriting or ambiguous dosage notation detected."
        }

        return ExtractedMedicineCandidate(
            id = UUID.randomUUID().toString(),
            rawText = rawText,
            name = name.ifBlank { "Prescribed Medicine" },
            strength = strength,
            dosage = dosage.ifBlank { "1 Tablet" },
            frequency = frequency.ifBlank { "Twice Daily" },
            instructions = instructions.ifBlank { "Take after food" },
            needsPharmacistReview = needsReview,
            flagReason = flagReason
        )
    }
}
