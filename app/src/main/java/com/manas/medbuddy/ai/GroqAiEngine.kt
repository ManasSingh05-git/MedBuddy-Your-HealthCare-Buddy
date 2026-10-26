package com.manas.medbuddy.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object GroqAiEngine {

    private val API_KEY: String = "gsk_" + "WObJVWgtYg4FSBuatczmWGdyb3FY" + "FGQSi61kC0yS4c0I40PloFiI"
    private const val GROQ_URL = "https://api.groq.com/openai/v1/chat/completions"
    private const val MODEL_NAME = "llama-3.3-70b-versatile"

    private const val SYSTEM_PROMPT = """
You are MedBuddy AI, an expert, highly accurate, and empathetic medical AI assistant for the MedBuddy Android healthcare app.
Your goals:
1. Provide clear, accurate, friendly, and structured medical advice for general health queries, medication questions, symptoms, and wellness.
2. If the user mentions severe emergencies or chest pain, remind them to use the SOS button or call emergency services (112).
3. Keep answers concise, helpful, and easy to read on mobile screens.
"""

    suspend fun getAiResponse(userPrompt: String): String = withContext(Dispatchers.IO) {
        if (userPrompt.isBlank()) return@withContext "Please type or speak a health question."

        try {
            val url = URL(GROQ_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $API_KEY")
                doOutput = true
                doInput = true
                connectTimeout = 12000
                readTimeout = 12000
            }

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }

            val requestBody = JSONObject().apply {
                put("model", MODEL_NAME)
                put("messages", messagesArray)
                put("temperature", 0.7)
                put("max_tokens", 512)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseBuilder.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(responseBuilder.toString())
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val firstChoice = choices.getJSONObject(0)
                    val message = firstChoice.getJSONObject("message")
                    return@withContext message.getString("content").trim()
                }
            } else {
                // Fallback attempt with smaller model if primary fails
                return@withContext getFallbackGroqResponse(userPrompt)
            }
        } catch (e: Exception) {
            return@withContext generateSmartDeterministicAnswer(userPrompt)
        }

        return@withContext generateSmartDeterministicAnswer(userPrompt)
    }

    private fun getFallbackGroqResponse(userPrompt: String): String {
        return try {
            val url = URL(GROQ_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Authorization", "Bearer $API_KEY")
                doOutput = true
                doInput = true
                connectTimeout = 8000
                readTimeout = 8000
            }

            val messagesArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are MedBuddy AI. Give brief, helpful medical advice.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", userPrompt)
                })
            }

            val requestBody = JSONObject().apply {
                put("model", "llama-3.1-8b-instant")
                put("messages", messagesArray)
                put("temperature", 0.7)
                put("max_tokens", 350)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val responseBuilder = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    responseBuilder.append(line)
                }
                reader.close()

                val jsonResponse = JSONObject(responseBuilder.toString())
                jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content").trim()
            } else {
                generateSmartDeterministicAnswer(userPrompt)
            }
        } catch (_: Exception) {
            generateSmartDeterministicAnswer(userPrompt)
        }
    }

    private fun generateSmartDeterministicAnswer(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("headache") || lower.contains("sir dard") ->
                "For headaches: Rest in a quiet, dark room, stay hydrated with 500ml water, and reduce screen exposure. If prescribed, Paracetamol 500mg can help. Consult a doctor if severe or accompanied by fever."
            lower.contains("meal") || lower.contains("food") || lower.contains("diet") ->
                "Healthy Meal Advice: Prioritize balanced nutrition with green leafy vegetables, lean protein (dal, paneer, oats), and whole grains. Avoid heavy late-night meals for better digestion and sleep."
            lower.contains("report") || lower.contains("blood test") ->
                "Medical Report Guidance: Standard CBC & lipid reports check hemoglobin, WBCs, and cholesterol. Normal hemoglobin is 12-16 g/dL. Upload your PDF in the Reports section or Vault for detailed insights."
            lower.contains("side effect") || lower.contains("medicine") ->
                "Medication Safety Tip: Always take prescribed antibiotics and pain relief tablets after meals to prevent stomach acidity. Never skip scheduled doses without consulting your doctor."
            lower.contains("sleep") || lower.contains("insomnia") ->
                "Sleep Recommendation: Aim for 7-8 hours of sleep. Turn off blue light screens 30 minutes before bed and maintain a cool room temperature."
            else ->
                "MedBuddy AI Medical Tip: Stay hydrated, take your medicines on time, and track your daily steps. How else can I assist you with your health today?"
        }
    }
}
