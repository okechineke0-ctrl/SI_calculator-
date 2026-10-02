package com.example.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class PythonEvaluateResult(
    val isSuccess: Boolean,
    val result: String,
    val exactFraction: String? = null,
    val latex: String? = null,
    val error: String? = null
)

object PythonBackendClient {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .build()

    suspend fun evaluateWithPython(
        baseUrl: String,
        expression: String,
        precision: Int = 10
    ): PythonEvaluateResult = withContext(Dispatchers.IO) {
        if (baseUrl.isBlank()) {
            return@withContext PythonEvaluateResult(
                isSuccess = false,
                result = "",
                error = "Python backend URL not configured"
            )
        }

        try {
            val cleanUrl = baseUrl.trim().removeSuffix("/")
            val jsonBody = JSONObject().apply {
                put("expression", expression)
                put("precision", precision)
            }

            val request = Request.Builder()
                .url("$cleanUrl/api/evaluate")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                return@withContext PythonEvaluateResult(
                    isSuccess = false,
                    result = "",
                    error = "Python backend error: HTTP ${response.code}"
                )
            }

            val json = JSONObject(responseString)
            val success = json.optBoolean("is_success", false)
            val result = json.optString("result", "")
            val fraction = if (json.has("exact_fraction") && !json.isNull("exact_fraction")) json.getString("exact_fraction") else null
            val latex = if (json.has("latex") && !json.isNull("latex")) json.getString("latex") else null
            val error = if (json.has("error") && !json.isNull("error")) json.getString("error") else null

            PythonEvaluateResult(
                isSuccess = success,
                result = result,
                exactFraction = fraction,
                latex = latex,
                error = error
            )
        } catch (e: Exception) {
            PythonEvaluateResult(
                isSuccess = false,
                result = "",
                error = e.localizedMessage
            )
        }
    }
}
