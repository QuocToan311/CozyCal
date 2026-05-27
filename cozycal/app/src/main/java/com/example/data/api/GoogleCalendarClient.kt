package com.example.data.api

import android.content.Context
import android.util.Log
import com.example.data.model.CalendarEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

object GoogleCalendarClient {
    private const val TAG = "GoogleCalendarClient"
    private const val PREFS_NAME = "cozycal_google_auth"
    private const val KEY_ACCESS_TOKEN = "google_access_token"
    private const val KEY_ID_TOKEN = "google_id_token"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Store the Google OAuth tokens securely.
     */
    fun saveTokens(context: Context, accessToken: String, idToken: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_ID_TOKEN, idToken)
            .apply()
    }

    /**
     * Retrieve the stored access token.
     */
    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Clear OAuth tokens for safe logout.
     */
    fun clearTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    /**
     * Fetches events from Google Calendar API.
     */
    suspend fun fetchGoogleEvents(context: Context, userEmail: String): List<CalendarEvent> = withContext(Dispatchers.IO) {
        val accessToken = getAccessToken(context)
        if (accessToken.isNullOrEmpty()) {
            Log.w(TAG, "No Google OAuth access token found. Gracefully fallback.")
            return@withContext emptyList()
        }

        val url = "https://www.googleapis.com/calendar/v3/calendars/primary/events?singleEvents=true&maxResults=50"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Fetch events failed: code=${response.code} body=${response.body?.string()}")
                    return@withContext emptyList()
                }

                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val items = json.optJSONArray("items") ?: return@withContext emptyList()
                val events = mutableListOf<CalendarEvent>()

                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    val title = item.optString("summary", "Sự kiện Google Calendar 🗓️")
                    val desc = item.optString("description", "Nhập từ Google Calendar")
                    
                    val startObj = item.optJSONObject("start")
                    val endObj = item.optJSONObject("end")
                    if (startObj == null || endObj == null) continue

                    val startStr = startObj.optString("dateTime", startObj.optString("date"))
                    val endStr = endObj.optString("dateTime", endObj.optString("date"))

                    val startTime = parseGoogleDateString(startStr, formatter)
                    val endTime = parseGoogleDateString(endStr, formatter)

                    events.add(
                        CalendarEvent(
                            userEmail = userEmail,
                            title = title,
                            description = desc,
                            startTime = startTime,
                            endTime = endTime,
                            colorHex = "#D6EDF8", // Pastel Blue for connected Google events
                            category = "general"
                        )
                    )
                }
                return@withContext events
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Google Calendar events", e)
            return@withContext emptyList()
        }
    }

    /**
     * Fetch user profile (email and name) using the access token from the oauth userinfo endpoint.
     */
    suspend fun fetchGoogleProfile(context: Context): Pair<String, String>? = withContext(Dispatchers.IO) {
        val accessToken = getAccessToken(context) ?: return@withContext null
        val url = "https://www.googleapis.com/oauth2/v3/userinfo"
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: return@withContext null
                    val json = JSONObject(body)
                    val email = json.optString("email", "")
                    val name = json.optString("name", "Google Companion")
                    if (email.isNotEmpty()) {
                        return@withContext Pair(email, name)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching Google user profile", e)
        }
        null
    }

    /**
     * Push a locally created CalendarEvent to Google Calendar API.
     */
    suspend fun pushEventToGoogle(context: Context, event: CalendarEvent): Boolean = withContext(Dispatchers.IO) {
        val accessToken = getAccessToken(context) ?: return@withContext false
        val url = "https://www.googleapis.com/calendar/v3/calendars/primary/events"

        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        val startStr = sdf.format(Date(event.startTime))
        val endStr = sdf.format(Date(event.endTime))

        val eventJson = JSONObject().apply {
            put("summary", event.title)
            put("description", event.description + "\n\n(Tạo từ CozyCal App)")
            put("start", JSONObject().apply {
                put("dateTime", startStr)
                put("timeZone", "UTC")
            })
            put("end", JSONObject().apply {
                put("dateTime", endStr)
                put("timeZone", "UTC")
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = eventJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .header("Authorization", "Bearer $accessToken")
            .post(body)
            .build()

        return@withContext try {
            client.newCall(request).execute().use { response ->
                val result = response.isSuccessful
                if (!result) {
                    Log.e(TAG, "Push event failed: code=${response.code} error=${response.body?.string()}")
                }
                result
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pushing event to Google Calendar", e)
            false
        }
    }

    private fun parseGoogleDateString(dateStr: String, formatter: SimpleDateFormat): Long {
        if (dateStr.isEmpty()) return System.currentTimeMillis()
        return try {
            // Trim timezone offset if present for unified parsing or handle UTC
            val cleaned = if (dateStr.length > 19) dateStr.substring(0, 19) else dateStr
            val date = formatter.parse(cleaned)
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
