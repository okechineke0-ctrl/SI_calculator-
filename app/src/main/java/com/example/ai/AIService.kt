package com.example.ai

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

enum class AISolutionMode(val label: String, val promptInstruction: String) {
    STEP_BY_STEP(
        "Step-by-Step",
        "Provide the complete, exhaustive step-by-step mathematical derivation and final answer. Show every calculation step clearly from beginning to end."
    ),
    ANSWER_ONLY(
        "Answer Only",
        "State strictly the complete final simplified mathematical result with no commentary."
    ),
    EXPLAIN_LEARNING(
        "Concept & Intuition",
        "Explain the core mathematical principle, conceptual foundation, and why this method solves the problem, followed by the complete step-by-step solution."
    ),
    SHOW_FORMULA(
        "Formula & Derivation",
        "State the governing mathematical theorem/formula, define all parameters, and provide the complete numerical substitution and solution."
    ),
    CHECK_ANSWER(
        "Verify & Validate",
        "Verify the calculation with complete proof, check domain boundaries, and validate the exact correctness of the result."
    ),
    ALTERNATIVE_METHOD(
        "Alternative Method",
        "Provide a complete alternative mathematical solving technique (e.g. substitution vs elimination, algebraic vs calculus, graphical vs matrix)."
    )
}

data class AISolveResult(
    val recognizedExpression: String? = null,
    val explanation: String,
    val isSuccess: Boolean,
    val errorMessage: String? = null,
    val isOfflineFallback: Boolean = false
)

object AIService {

    // High-availability model cascade: automatically falls back if Google experiences 503 / 429 demand spikes
    private val CANDIDATE_MODELS = listOf(
        "gemini-flash-latest",
        "gemini-3.1-flash-lite-preview",
        "gemini-3.8-flash"
    )

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getEffectiveApiKey(customKey: String): String {
        return if (customKey.isNotBlank()) customKey else BuildConfig.GEMINI_API_KEY
    }

    /**
     * Executes requests across the model cascade with retry and automatic fallback on 503 (Overloaded) or 429 (Rate Limit).
     */
    private suspend fun executeWithModelCascade(jsonBody: JSONObject, apiKey: String): Pair<Boolean, String> {
        var lastError = "No response"
        var lastStatusCode = 0

        for (model in CANDIDATE_MODELS) {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

            for (attempt in 0..1) {
                try {
                    val request = Request.Builder()
                        .url(url)
                        .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                        .build()

                    val response = httpClient.newCall(request).execute()
                    val responseString = response.body?.string() ?: ""

                    if (response.isSuccessful) {
                        return Pair(true, responseString)
                    }

                    lastStatusCode = response.code
                    lastError = responseString

                    // If 503 (Service Unavailable / Overloaded) or 429 (Rate limit)
                    if (response.code == 503 || response.code == 429) {
                        if (attempt == 0) {
                            delay(600) // Brief jitter wait before retrying once
                            continue
                        }
                        // Break to cascade to next model in list
                        break
                    } else {
                        // For other codes, immediately try next candidate model
                        break
                    }
                } catch (e: Exception) {
                    lastError = e.message ?: "Network error"
                }
            }
        }

        return Pair(false, "HTTP $lastStatusCode: $lastError")
    }

    /**
     * Cleans raw LaTeX formatting, strips all LaTeX dollar signs ($ and $$),
     * and converts math markup to clean, mature Unicode typography.
     */
    fun cleanMathTypography(raw: String): String {
        var text = raw

        // Remove LaTeX delimiters
        text = text.replace("$$", "")
        text = text.replace("$", "")
        text = text.replace("\\[", "")
        text = text.replace("\\]", "")
        text = text.replace("\\(", "")
        text = text.replace("\\)", "")

        // Replace \text{...} or \mathrm{...}
        text = text.replace(Regex("""\\(?:text|mathrm|mathbf|mathit)\{([^}]*)\}""")) { it.groupValues[1] }

        // Fractions \frac{a}{b} -> (a)/(b)
        text = text.replace(Regex("""\\frac\{([^}]*)\}\{([^}]*)\}""")) { "(${it.groupValues[1]})/(${it.groupValues[2]})" }

        // Square roots \sqrt{x} -> √(x)
        text = text.replace(Regex("""\\sqrt\[([^]]*)\]\{([^}]*)\}""")) { "${it.groupValues[1]}√(${it.groupValues[2]})" }
        text = text.replace(Regex("""\\sqrt\{([^}]*)\}""")) { "√(${it.groupValues[1]})" }

        // Common LaTeX symbols to clean Unicode
        text = text.replace("\\times", "×")
        text = text.replace("\\cdot", "·")
        text = text.replace("\\div", "÷")
        text = text.replace("\\pm", "±")
        text = text.replace("\\mp", "∓")
        text = text.replace("\\le", "≤")
        text = text.replace("\\leq", "≤")
        text = text.replace("\\ge", "≥")
        text = text.replace("\\geq", "≥")
        text = text.replace("\\ne", "≠")
        text = text.replace("\\neq", "≠")
        text = text.replace("\\approx", "≈")
        text = text.replace("\\infty", "∞")
        text = text.replace("\\degree", "°")
        text = text.replace("\\circ", "°")
        text = text.replace("\\pi", "π")
        text = text.replace("\\theta", "θ")
        text = text.replace("\\alpha", "α")
        text = text.replace("\\beta", "β")
        text = text.replace("\\gamma", "γ")
        text = text.replace("\\lambda", "λ")
        text = text.replace("\\mu", "μ")
        text = text.replace("\\sigma", "σ")
        text = text.replace("\\Delta", "Δ")
        text = text.replace("\\int", "∫")
        text = text.replace("\\sum", "∑")
        text = text.replace("\\partial", "∂")
        text = text.replace("\\left", "")
        text = text.replace("\\right", "")
        text = text.replace("\\quad", " ")
        text = text.replace("\\qquad", "  ")

        // Final pass: strip any leftover dollar signs
        text = text.replace("$", "")

        return text.trim()
    }

    /**
     * Solves a text math problem with mature, executive formatting, complete unabridged solution, and zero dollar signs.
     */
    suspend fun solveMathProblem(
        prompt: String,
        mode: AISolutionMode,
        apiKey: String
    ): AISolveResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AISolveResult(
                explanation = "Gemini API key is not configured. Configure your key in Settings or the AI Studio Secrets panel.\n\nYou can still use the local deterministic calculator, grapher, and equation solvers offline.",
                isSuccess = false,
                isOfflineFallback = true,
                errorMessage = "API Key not configured"
            )
        }

        try {
            val systemInstruction = "You are Ocean Math AI, a senior computational mathematician and verification engine. " +
                    "MANDATORY PROFESSIONAL STANDARDS: " +
                    "1. FULL UNABRIDGED SOLUTION: Provide the complete, exhaustive mathematical solution from start to finish. Never truncate, skip intermediate steps, or abbreviate calculations. " +
                    "2. STRICTLY NO LATEX DOLLAR SIGNS: Never use '$' or '$$' anywhere in your response. " +
                    "3. NO CHATBOT FILLER: No greetings ('Hello', 'Sure!'), no conversational preamble, and no redundant wordiness. Start immediately with the solution. " +
                    "4. UNICODE MATHEMATICS: Use standard Unicode symbols: exponents (x², x³, xⁿ), square roots (√), fractions (a/b), multiplication (×), plus-minus (±), Greek letters (π, θ), and degrees (°). " +
                    "5. ZERO CALCULATION ERRORS: Compute and double check all arithmetic with strict accuracy. " +
                    "6. STRUCTURE: " +
                    "• Final Result: [Clear, direct answer in bold] " +
                    "• Governing Formula / Theory: [State theorems and formulas used] " +
                    "• Complete Step-by-Step Derivation: [Every single step fully worked out in numbered detail] " +
                    "• Verification: [Validation check or domain check] " +
                    "Mode: ${mode.promptInstruction}"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", systemInstruction)
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.0)
                    put("topP", 0.95)
                    put("maxOutputTokens", 4096)
                })
            }

            val (success, responseString) = executeWithModelCascade(jsonBody, apiKey)

            if (!success) {
                return@withContext AISolveResult(
                    explanation = "AI Service temporarily unavailable. Please retry in a few moments or use the local math engine.",
                    isSuccess = false,
                    isOfflineFallback = true,
                    errorMessage = responseString
                )
            }

            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val pText = parts.optJSONObject(i)?.optString("text", "") ?: ""
                    if (pText.isNotBlank()) {
                        sb.append(pText)
                    }
                }
            }
            val rawText = if (sb.isNotEmpty()) sb.toString() else "No response generated"

            AISolveResult(
                explanation = cleanMathTypography(rawText),
                isSuccess = true
            )
        } catch (e: Exception) {
            AISolveResult(
                explanation = "Network connection error: ${e.localizedMessage}. Please check internet connection.",
                isSuccess = false,
                isOfflineFallback = true,
                errorMessage = e.message
            )
        }
    }

    /**
     * Snap & Solve: Recognizes mathematics expression from image and solves it with full unabridged steps and mature typography.
     */
    suspend fun recognizeAndSolveImage(
        bitmap: Bitmap,
        mode: AISolutionMode,
        apiKey: String
    ): AISolveResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext AISolveResult(
                explanation = "Snap & Solve AI requires a valid Gemini API key. Please set your key in Settings to analyze photographed problems.",
                isSuccess = false,
                isOfflineFallback = true,
                errorMessage = "API key missing"
            )
        }

        try {
            // Compress bitmap to JPEG Base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            val prompt = "Analyze this photographed mathematics problem with senior engineering precision. " +
                    "MANDATORY RULES: " +
                    "1. FULL UNABRIDGED SOLUTION: Provide the complete, full mathematical solution from start to finish without truncation or omissions. " +
                    "2. STRICTLY NO DOLLAR SIGNS: Never use '$' or '$$' anywhere. Use clean Unicode (², ³, √, ±, ×, π, θ). " +
                    "3. NO CHATBOT FLUFF: No greetings or unnecessary introductory text. " +
                    "4. Transcribe the exact problem statement without ambiguity. " +
                    "Format strictly as:\n" +
                    "RECOGNIZED_PROBLEM: <the transcribed equation or problem without dollar signs>\n" +
                    "SOLUTION:\n<${mode.promptInstruction}>"

            val jsonBody = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "image/jpeg")
                                    put("data", base64Image)
                                })
                            })
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.0)
                    put("topP", 0.95)
                    put("maxOutputTokens", 4096)
                })
            }

            val (success, responseString) = executeWithModelCascade(jsonBody, apiKey)

            if (!success) {
                return@withContext AISolveResult(
                    explanation = "AI vision service temporarily unavailable. Please retry in a few moments.",
                    isSuccess = false,
                    isOfflineFallback = true,
                    errorMessage = responseString
                )
            }

            val jsonResponse = JSONObject(responseString)
            val candidates = jsonResponse.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val sb = StringBuilder()
            if (parts != null) {
                for (i in 0 until parts.length()) {
                    val pText = parts.optJSONObject(i)?.optString("text", "") ?: ""
                    if (pText.isNotBlank()) {
                        sb.append(pText)
                    }
                }
            }
            val rawText = sb.toString()

            // Robust parsing of RECOGNIZED_PROBLEM: and SOLUTION:
            var recognized: String? = null
            var solution = rawText

            val solutionMatch = Regex("""(?i)\b(?:SOLUTION|FINAL SOLUTION)\s*:\s*""").find(rawText)
            if (solutionMatch != null) {
                val beforeSolution = rawText.substring(0, solutionMatch.range.first)
                solution = rawText.substring(solutionMatch.range.last + 1).trim()

                val problemMatch = Regex("""(?i)\bRECOGNIZED_PROBLEM\s*:\s*""").find(beforeSolution)
                if (problemMatch != null) {
                    recognized = beforeSolution.substring(problemMatch.range.last + 1).trim()
                } else if (beforeSolution.isNotBlank()) {
                    recognized = beforeSolution.trim()
                }
            }

            AISolveResult(
                recognizedExpression = recognized?.let { cleanMathTypography(it) },
                explanation = cleanMathTypography(solution),
                isSuccess = true
            )
        } catch (e: Exception) {
            AISolveResult(
                explanation = "Failed to process photo: ${e.localizedMessage}",
                isSuccess = false,
                isOfflineFallback = true,
                errorMessage = e.message
            )
        }
    }
}
