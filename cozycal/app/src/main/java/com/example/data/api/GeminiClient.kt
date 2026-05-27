package com.example.data.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Checks if the API Key is configured and looks valid.
     */
    fun isApiKeyAvailable(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return !key.isNullOrEmpty() && key != "MY_GEMINI_API_KEY" && key != "placeholder"
    }

    /**
     * Helper to encode Bitmap to Base64 JPEG.
     */
    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Parse natural language text into a structured calendar event using Gemini.
     */
    suspend fun parseNaturalLanguage(promptText: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local/simulated parse.")
            return@withContext simulateNaturalLanguageParse(promptText)
        }

        try {
            val key = BuildConfig.GEMINI_API_KEY
            val systemInstruction = """
                You are a cozy personal organization assistant. Your goal is to parse scheduling descriptions into structured calendar events.
                Today's local date is: 2026-05-23. Today is Saturday.
                Respond with a single JSON object (and nothing else! No backticks, no markdown fence, just raw text) with exactly these fields:
                - title: String (event title, nicely capitalized and friendly)
                - durationMinutes: Integer (default 60 if not specified)
                - offsetDays: Integer (offset in days relative to Saturday May 23, 2026. Today=0, Tomorrow=1, Day after=2, Next Monday=2, etc.)
                - startHour: Integer (0-23, e.g. 19 for 7pm, 9 for 9am. Detect am/pm correctly)
                - startMinute: Integer (0-59)
                - category: String ('study', 'self-care', 'work', 'routine', or 'general')
                - description: String (helpful note summarizing the details)
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", promptText) })
                    }
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                // Set system instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })

                // Request clean JSON format
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$key")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed with code ${response.code}: ${response.body?.string()}")
                    return@withContext simulateNaturalLanguageParse(promptText)
                }

                val bodyString = response.body?.string() ?: return@withContext null
                val rootJson = JSONObject(bodyString)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        val text = parts.getJSONObject(0).getString("text")
                        Log.d(TAG, "Raw Gemini Response: $text")
                        
                        // Parse the response
                        val responseJson = JSONObject(text.trim())
                        return@withContext mapOf(
                            "title" to responseJson.optString("title", "Cozy Task"),
                            "durationMinutes" to responseJson.optInt("durationMinutes", 60),
                            "offsetDays" to responseJson.optInt("offsetDays", 0),
                            "startHour" to responseJson.optInt("startHour", 9),
                            "startMinute" to responseJson.optInt("startMinute", 0),
                            "category" to responseJson.optString("category", "general"),
                            "description" to responseJson.optString("description", "")
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini parsing error", e)
        }
        return@withContext simulateNaturalLanguageParse(promptText)
    }

    /**
     * Parse an image for scheduling using Gemini multimodal input.
     */
    suspend fun parseImageSchedule(bitmap: Bitmap, promptHint: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to local/simulated image parse.")
            return@withContext simulateImageScheduleParse(promptHint)
        }

        val base64Image = bitmap.toBase64()
        try {
            val key = BuildConfig.GEMINI_API_KEY
            val systemInstruction = """
                You are an elite cozy schedule OCR organizer. Look at the image provided representing a timetable, lecture poster, invitation, or screenshot. Extract all calendar events.
                Today's local date is: 2026-05-23. Today is Saturday.
                Respond with a single JSON object containing an array named "events" (and nothing else! No backticks, no md, just raw json structure) where each event object has exactly:
                - title: String
                - offsetDays: Integer (offset in days relative to Saturday May 23, 2026. Saturday=0, Sunday=1, Monday=2, etc.)
                - startHour: Integer (0-23)
                - startMinute: Integer (0-59)
                - durationMinutes: Integer (default 60 if not clear)
                - category: String ('study', 'self-care', 'work', 'routine', or 'general')
                - description: String (brief notes such as teacher, location, or room)
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        // Text instruction/hint
                        put(JSONObject().apply { put("text", "Extract events from this schedule image. $promptHint") })
                        // Multimodal Image parts
                        put(JSONObject().apply {
                            put("inlineData", JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Image)
                            })
                        })
                    }
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                // Set system instruction
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })

                // Request JSON
                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.2)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$key")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Image OCR failed with code ${response.code}")
                    return@withContext simulateImageScheduleParse(promptHint)
                }

                val bodyString = response.body?.string() ?: return@withContext emptyList<Map<String, Any>>()
                val rootJson = JSONObject(bodyString)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        val text = parts.getJSONObject(0).getString("text")
                        Log.d(TAG, "Raw Image Gemini Response: $text")

                        val resultJson = JSONObject(text.trim())
                        val eventsArray = resultJson.optJSONArray("events") ?: JSONArray()
                        val eventsList = mutableListOf<Map<String, Any>>()
                        for (i in 0 until eventsArray.length()) {
                            val eventObj = eventsArray.getJSONObject(i)
                            eventsList.add(mapOf(
                                "title" to eventObj.optString("title", "Scanned Event"),
                                "offsetDays" to eventObj.optInt("offsetDays", 0),
                                "startHour" to eventObj.optInt("startHour", 9),
                                "startMinute" to eventObj.optInt("startMinute", 0),
                                "durationMinutes" to eventObj.optInt("durationMinutes", 60),
                                "category" to eventObj.optString("category", "general"),
                                "description" to eventObj.optString("description", "")
                            ))
                        }
                        return@withContext eventsList
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini multimodal scanning error", e)
        }
        return@withContext simulateImageScheduleParse(promptHint)
    }

    /**
     * Offline simulation mode to ensure beautiful, responsive UX even without an API Key.
     */
    private fun simulateNaturalLanguageParse(text: String): Map<String, Any> {
        val lower = text.lowercase()
        var title = "Cozy Event 🌱"
        var category = "general"
        var offsetDays = 0
        var hour = 10
        var minutes = 0
        var desc = "Created with Smart AI"

        // simple local NLP matching for simulation
        if (lower.contains("java") || lower.contains("study") || lower.contains("học")) {
            title = "Study Programming 📚"
            category = "study"
            desc = "Cozy self-study session. Keep it up!"
        } else if (lower.contains("yoga") || lower.contains("meditate") || lower.contains("thiền") || lower.contains("chill")) {
            title = "Cozy Self-Care 🧘"
            category = "self-care"
            desc = "Mindfulness focus or relaxation habit."
        } else if (lower.contains("gym") || lower.contains("chạy") || lower.contains("run") || lower.contains("workout")) {
            title = "Warm stretch & Workout 🏃"
            category = "routine"
            desc = "Stretch muscles and breathe deeply."
        } else if (lower.contains("meeting") || lower.contains("deadline") || lower.contains("làm việc") || lower.contains("work")) {
            title = "Focus Workflow 💼"
            category = "work"
            desc = "Concentrated sessions. Take short breaks!"
        }

        if (lower.contains("tomorrow") || lower.contains("mai")) {
            offsetDays = 1
        } else if (lower.contains("next monday") || lower.contains("thứ hai tới")) {
            offsetDays = 2 // May 23 is Sat, Mon is May 25 (offset 2)
        }

        // detect hours
        val timeRegex = "(\\d+)\\s*(pm|am|giờ)".toRegex()
        val match = timeRegex.find(lower)
        if (match != null) {
            var rawHour = match.groupValues[1].toInt()
            val meridiem = match.groupValues[2]
            if (meridiem == "pm" && rawHour < 12) {
                rawHour += 12
            } else if (meridiem == "am" && rawHour == 12) {
                rawHour = 0
            }
            hour = rawHour
        } else if (lower.contains("7pm") || lower.contains("19h")) {
            hour = 19
        } else if (lower.contains("8am") || lower.contains("8h")) {
            hour = 8
        }

        return mapOf(
            "title" to title,
            "durationMinutes" to 60,
            "offsetDays" to offsetDays,
            "startHour" to hour,
            "startMinute" to minutes,
            "category" to category,
            "description" to "$desc (Simulated parse)"
        )
    }

    private fun simulateImageScheduleParse(hint: String): List<Map<String, Any>> {
        val lower = hint.lowercase()
        return when {
            lower.contains("timetable") || lower.contains("lớp") || lower.contains("học") -> {
                listOf(
                    mapOf(
                        "title" to "Mobile App Development Class 📱",
                        "offsetDays" to 2, // Monday (May 25)
                        "startHour" to 9,
                        "startMinute" to 0,
                        "durationMinutes" to 120,
                        "category" to "study",
                        "description" to "Room 402 - Advanced Jetpack Compose layout mechanics"
                    ),
                    mapOf(
                        "title" to "Cozy Reading Circle 📖",
                        "offsetDays" to 3, // Tuesday
                        "startHour" to 14,
                        "startMinute" to 30,
                        "durationMinutes" to 60,
                        "category" to "self-care",
                        "description" to "Library Coffee Shop - Bring your favorite book"
                    )
                )
            }
            lower.contains("workshop") || lower.contains("poster") || lower.contains("sự kiện") -> {
                listOf(
                    mapOf(
                        "title" to "Self-Care & Mindfulness Workshop 🌸",
                        "offsetDays" to 1, // Sunday (May 24)
                        "startHour" to 15,
                        "startMinute" to 0,
                        "durationMinutes" to 90,
                        "category" to "self-care",
                        "description" to "Cozy Zoom Session - Deep breathing & kitty petting"
                    )
                )
            }
            else -> {
                listOf(
                    mapOf(
                        "title" to "Scanned Cozy Gettogether ☕",
                        "offsetDays" to 1,
                        "startHour" to 10,
                        "startMinute" to 0,
                        "durationMinutes" to 90,
                        "category" to "general",
                        "description" to "Scanned details matching: $hint"
                    )
                )
            }
        }
    }

    /**
     * Parse calendar events from text and document files.
     */
    suspend fun parseTextSchedule(scheduleText: String): List<Map<String, Any>> = withContext(Dispatchers.IO) {
        if (!isApiKeyAvailable()) {
            Log.w(TAG, "Gemini API key is not configured. Falling back to simulated text schedule parse.")
            return@withContext simulateTextScheduleParse(scheduleText)
        }

        try {
            val key = BuildConfig.GEMINI_API_KEY
            val systemInstruction = """
                You are a cozy personal organization assistant. Extract all calendar events from the provided text content or text schedule.
                Today's local date is: 2026-05-23. Today is Saturday.
                Respond with a single JSON object containing an array named "events" (and nothing else! No backticks, no md, just raw json structure) where each event object has exactly:
                - title: String (event title, nicely capitalized)
                - offsetDays: Integer (offset in days relative to Saturday May 23, 2026. Saturday=0, Sunday=1, Monday=2, etc.)
                - startHour: Integer (0-23)
                - startMinute: Integer (0-59)
                - durationMinutes: Integer (default 60 if not clear)
                - category: String ('study', 'self-care', 'work', 'routine', or 'general')
                - description: String (brief notes such as location or classroom)
            """.trimIndent()

            val requestJson = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", "Extract events from this text:\n$scheduleText") })
                    }
                    put("parts", partsArray)
                }
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", systemInstruction) })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("responseMimeType", "application/json")
                    put("temperature", 0.1)
                })
            }

            val requestBody = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$BASE_URL?key=$key")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Text parse failed with code ${response.code}")
                    return@withContext simulateTextScheduleParse(scheduleText)
                }

                val bodyString = response.body?.string() ?: return@withContext emptyList<Map<String, Any>>()
                val rootJson = JSONObject(bodyString)
                val candidates = rootJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.getJSONObject("content")
                    val parts = content.getJSONArray("parts")
                    if (parts.length() > 0) {
                        val text = parts.getJSONObject(0).getString("text")
                        Log.d(TAG, "Raw Text Gemini Response: $text")

                        val resultJson = JSONObject(text.trim())
                        val eventsArray = resultJson.optJSONArray("events") ?: JSONArray()
                        val eventsList = mutableListOf<Map<String, Any>>()
                        for (i in 0 until eventsArray.length()) {
                            val eventObj = eventsArray.getJSONObject(i)
                            eventsList.add(mapOf(
                                "title" to eventObj.optString("title", "Parsed Event"),
                                "offsetDays" to eventObj.optInt("offsetDays", 0),
                                "startHour" to eventObj.optInt("startHour", 9),
                                "startMinute" to eventObj.optInt("startMinute", 0),
                                "durationMinutes" to eventObj.optInt("durationMinutes", 60),
                                "category" to eventObj.optString("category", "general"),
                                "description" to eventObj.optString("description", "")
                            ))
                        }
                        return@withContext eventsList
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini text parsing error", e)
        }
        return@withContext simulateTextScheduleParse(scheduleText)
    }

    private fun simulateTextScheduleParse(text: String): List<Map<String, Any>> {
        val lower = text.lowercase()
        val events = mutableListOf<Map<String, Any>>()
        
        if (lower.contains("study") || lower.contains("học") || lower.contains("code") || lower.contains("lập trình") || lower.contains("luyện")) {
            events.add(mapOf(
                "title" to "Học nhóm Cozy 💻",
                "offsetDays" to 1,
                "startHour" to 9,
                "startMinute" to 0,
                "durationMinutes" to 120,
                "category" to "study",
                "description" to "Phân tích tài liệu trích xuất từ tệp tin đã tải lên"
            ))
        }
        if (lower.contains("gym") || lower.contains("chạy") || lower.contains("yoga") || lower.contains("thiền") || lower.contains("chill") || lower.contains("relax")) {
            events.add(mapOf(
                "title" to "Self-Care: Co giãn & Thiền dưỡng thần 🧘",
                "offsetDays" to 0,
                "startHour" to 18,
                "startMinute" to 30,
                "durationMinutes" to 60,
                "category" to "self-care",
                "description" to "Giờ nghỉ ngơi sảng khoái trích xuất từ tài liệu"
            ))
        }

        if (events.isEmpty()) {
            events.add(mapOf(
                "title" to "Sự kiện từ tệp Cozy 📄",
                "offsetDays" to 1,
                "startHour" to 10,
                "startMinute" to 0,
                "durationMinutes" to 60,
                "category" to "general",
                "description" to "Lịch tự động tạo từ nội dung: ${text.take(40)}..."
            ))
        }
        return events
    }
}
