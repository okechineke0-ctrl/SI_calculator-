package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.AIService
import com.example.ai.AISolutionMode
import com.example.ai.AISolveResult
import com.example.data.AppDatabase
import com.example.data.AppPreferences
import com.example.data.CalculationEntity
import com.example.data.CalculationRepository
import com.example.math.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    CALCULATOR("Calculator"),
    GRAPH("Graph"),
    AI_SOLVE("AI Snap & Solve"),
    MATH_TOOLS("Math Tools"),
    FORMULAS_HISTORY("Formulas & History"),
    SETTINGS("Settings")
}

enum class CalculatorMode(val title: String) {
    BASIC("Basic"),
    SCIENTIFIC("Scientific"),
    ENGINEERING("Engineering")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val repository = CalculationRepository(db.calculationDao())
    val preferences = AppPreferences(application)

    // Active Tab
    private val _currentTab = MutableStateFlow(AppNavTab.CALCULATOR)
    val currentTab: StateFlow<AppNavTab> = _currentTab.asStateFlow()

    // Calculator State
    private val _calculatorMode = MutableStateFlow(CalculatorMode.SCIENTIFIC)
    val calculatorMode: StateFlow<CalculatorMode> = _calculatorMode.asStateFlow()

    private val _expression = MutableStateFlow("")
    val expression: StateFlow<String> = _expression.asStateFlow()

    private val _result = MutableStateFlow("")
    val result: StateFlow<String> = _result.asStateFlow()

    private val _previewResult = MutableStateFlow("")
    val previewResult: StateFlow<String> = _previewResult.asStateFlow()

    private val _fractionResult = MutableStateFlow<String?>(null)
    val fractionResult: StateFlow<String?> = _fractionResult.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _memory = MutableStateFlow(0.0)
    val memory: StateFlow<Double> = _memory.asStateFlow()

    private val _previousAnswer = MutableStateFlow(0.0)
    val previousAnswer: StateFlow<Double> = _previousAnswer.asStateFlow()

    private val mathEngine = MathEngine()

    // Graphing State
    val graphFunctions = MutableStateFlow(listOf("sin(x)", "0.2*x^2 - 3", "cos(x)"))
    val graphFunctionColors = listOf(0xFF38BDF8, 0xFFF59E0B, 0xFF10B981)
    val graphActiveIndex = MutableStateFlow(0)

    // AI Solve State
    val aiPrompt = MutableStateFlow("")
    val aiCapturedBitmap = MutableStateFlow<Bitmap?>(null)
    val aiRecognizedProblem = MutableStateFlow("")
    val aiSolutionMode = MutableStateFlow(AISolutionMode.STEP_BY_STEP)
    val aiSolveResult = MutableStateFlow<AISolveResult?>(null)
    val isAiLoading = MutableStateFlow(false)

    // Supabase State
    val supabaseSyncMessage = MutableStateFlow<String?>(null)
    val isSupabaseSyncing = MutableStateFlow(false)

    // Formula & History State
    val formulaSearchQuery = MutableStateFlow("")
    val historySearchQuery = MutableStateFlow("")

    val allHistory: StateFlow<List<CalculationEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteHistory: StateFlow<List<CalculationEntity>> = repository.favorites
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Sync preferences with engine
        viewModelScope.launch {
            preferences.angleMode.collect { mode ->
                mathEngine.angleMode = mode
                recalculatePreview()
            }
        }
        viewModelScope.launch {
            preferences.precision.collect { p ->
                mathEngine.precision = p
                recalculatePreview()
            }
        }
        viewModelScope.launch {
            preferences.notation.collect { n ->
                mathEngine.notation = n
                recalculatePreview()
            }
        }
    }

    fun selectTab(tab: AppNavTab) {
        _currentTab.value = tab
    }

    fun setCalculatorMode(mode: CalculatorMode) {
        _calculatorMode.value = mode
    }

    fun toggleAngleMode() {
        val next = when (preferences.angleMode.value) {
            AngleMode.DEG -> AngleMode.RAD
            AngleMode.RAD -> AngleMode.GRAD
            AngleMode.GRAD -> AngleMode.DEG
        }
        preferences.setAngleMode(next)
    }

    // Keypad Input
    fun onKeyPress(key: String) {
        when (key) {
            "AC" -> clearAll()
            "DEL" -> backspace()
            "=" -> evaluateFinal()
            "Ans" -> appendText("Ans")
            "±" -> toggleSign()
            "MC" -> _memory.value = 0.0
            "MR" -> appendText(MathEngine.formatNumber(_memory.value, preferences.notation.value, preferences.precision.value))
            "M+" -> addToMemory()
            "M-" -> subtractFromMemory()
            "MS" -> saveToMemory()
            "x²" -> appendText("^2")
            "x³" -> appendText("^3")
            "xʸ" -> appendText("^")
            "√" -> appendText("sqrt(")
            "∛" -> appendText("cbrt(")
            "10ˣ" -> appendText("10^")
            "eˣ" -> appendText("exp(")
            "2ˣ" -> appendText("2^")
            "log" -> appendText("log(")
            "ln" -> appendText("ln(")
            "log₂" -> appendText("log2(")
            "sin" -> appendText("sin(")
            "cos" -> appendText("cos(")
            "tan" -> appendText("tan(")
            "cot" -> appendText("cot(")
            "sec" -> appendText("sec(")
            "csc" -> appendText("csc(")
            "sin⁻¹" -> appendText("sin⁻¹(")
            "cos⁻¹" -> appendText("cos⁻¹(")
            "tan⁻¹" -> appendText("tan⁻¹(")
            "sinh" -> appendText("sinh(")
            "cosh" -> appendText("cosh(")
            "tanh" -> appendText("tanh(")
            "nPr" -> appendText(" nPr ")
            "nCr" -> appendText(" nCr ")
            "mod" -> appendText(" mod ")
            "π" -> appendText("π")
            "e" -> appendText("e")
            "φ" -> appendText("φ")
            "!" -> appendText("!")
            "%" -> appendText("%")
            "abs" -> appendText("abs(")
            else -> appendText(key)
        }
    }

    private fun appendText(str: String) {
        _errorMessage.value = null
        _expression.value = _expression.value + str
        recalculatePreview()
    }

    private fun backspace() {
        _errorMessage.value = null
        val current = _expression.value
        if (current.isNotEmpty()) {
            _expression.value = current.dropLast(1)
            recalculatePreview()
        }
    }

    private fun clearAll() {
        _expression.value = ""
        _result.value = ""
        _previewResult.value = ""
        _fractionResult.value = null
        _errorMessage.value = null
    }

    private fun toggleSign() {
        val current = _expression.value
        if (current.isEmpty()) {
            _expression.value = "-"
        } else if (current.startsWith("-")) {
            _expression.value = current.removePrefix("-")
        } else {
            _expression.value = "-($current)"
        }
        recalculatePreview()
    }

    private fun syncEngineConfig() {
        mathEngine.precision = preferences.precision.value
        mathEngine.angleMode = preferences.angleMode.value
        mathEngine.notation = preferences.notation.value
    }

    private fun recalculatePreview() {
        val expr = _expression.value
        if (expr.isBlank()) {
            _previewResult.value = ""
            _fractionResult.value = null
            return
        }
        syncEngineConfig()
        val res = mathEngine.evaluate(expr, _previousAnswer.value)
        if (res.isSuccess) {
            _previewResult.value = res.formatted
            _fractionResult.value = res.fraction
        } else {
            _previewResult.value = ""
            _fractionResult.value = null
        }
    }

    private fun evaluateFinal() {
        val expr = _expression.value
        if (expr.isBlank()) return

        syncEngineConfig()
        val res = mathEngine.evaluate(expr, _previousAnswer.value)
        if (res.isSuccess) {
            _result.value = res.formatted
            _fractionResult.value = res.fraction
            _previousAnswer.value = res.value
            _errorMessage.value = null

            // Save to Room DB History
            viewModelScope.launch {
                repository.saveCalculation(expr, res.formatted, _calculatorMode.value.title)
            }
        } else {
            _errorMessage.value = res.error ?: "Invalid expression"
            _result.value = "Error"
        }
    }

    private fun addToMemory() {
        val curVal = _previewResult.value.toDoubleOrNull() ?: _result.value.toDoubleOrNull() ?: 0.0
        _memory.value = _memory.value + curVal
    }

    private fun subtractFromMemory() {
        val curVal = _previewResult.value.toDoubleOrNull() ?: _result.value.toDoubleOrNull() ?: 0.0
        _memory.value = _memory.value - curVal
    }

    private fun saveToMemory() {
        val curVal = _previewResult.value.toDoubleOrNull() ?: _result.value.toDoubleOrNull() ?: 0.0
        _memory.value = curVal
    }

    fun useHistoryItem(entity: CalculationEntity) {
        _expression.value = entity.expression
        _result.value = entity.result
        _currentTab.value = AppNavTab.CALCULATOR
        recalculatePreview()
    }

    fun useFormulaInCalculator(formula: FormulaItem) {
        _expression.value = formula.defaultExpression
        _currentTab.value = AppNavTab.CALCULATOR
        recalculatePreview()
    }

    fun verifyExpressionWithAi(customExpr: String? = null) {
        val expr = customExpr ?: _expression.value
        if (expr.isBlank()) return
        aiPrompt.value = "Evaluate, verify with concise mathematical proof, and explain: $expr"
        _currentTab.value = AppNavTab.AI_SOLVE
        solveWithAi()
    }

    // AI Solve Actions
    fun solveWithAi() {
        val prompt = aiPrompt.value.ifBlank { aiRecognizedProblem.value }
        if (prompt.isBlank()) return

        isAiLoading.value = true
        aiSolveResult.value = null

        viewModelScope.launch {
            val key = AIService.getEffectiveApiKey(preferences.customApiKey.value)
            val result = AIService.solveMathProblem(prompt, aiSolutionMode.value, key)
            aiSolveResult.value = result
            isAiLoading.value = false

            // Auto-save to user history so they can always revisit it!
            if (result.isSuccess) {
                val finalAns = result.explanation.lines().lastOrNull { it.isNotBlank() } ?: "Solved"
                repository.saveCalculation(
                    expression = prompt,
                    result = finalAns,
                    solution = result.explanation,
                    type = "AI Solve",
                    bitmap = aiCapturedBitmap.value
                )
            }
        }
    }

    fun processCapturedImage(bitmap: Bitmap) {
        aiCapturedBitmap.value = bitmap
        isAiLoading.value = true
        aiSolveResult.value = null

        viewModelScope.launch {
            val key = AIService.getEffectiveApiKey(preferences.customApiKey.value)
            val result = AIService.recognizeAndSolveImage(bitmap, aiSolutionMode.value, key)
            aiSolveResult.value = result
            if (result.recognizedExpression != null) {
                aiRecognizedProblem.value = result.recognizedExpression
            }
            isAiLoading.value = false

            // Auto-save snapped photo, recognized equation and solution into local history!
            if (result.isSuccess) {
                val probText = result.recognizedExpression?.ifBlank { "Snapped Math Problem" } ?: "Snapped Math Problem"
                val finalAns = result.explanation.lines().lastOrNull { it.isNotBlank() } ?: "Solved"
                repository.saveCalculation(
                    expression = probText,
                    result = finalAns,
                    solution = result.explanation,
                    type = "Snap & Solve",
                    bitmap = bitmap
                )

                // If Supabase cloud is configured, sync silently in background
                val supaUrl = preferences.supabaseUrl.value
                val supaKey = preferences.supabaseAnonKey.value
                if (com.example.data.SupabaseService.isConfigured(supaUrl, supaKey)) {
                    repository.saveAndSyncToSupabase(
                        expression = probText,
                        result = finalAns,
                        solution = result.explanation,
                        bitmap = bitmap,
                        type = "Snap & Solve",
                        supabaseUrl = supaUrl,
                        supabaseKey = supaKey
                    )
                }
            }
        }
    }

    // Graph function update
    fun updateGraphFunction(index: Int, equation: String) {
        val list = graphFunctions.value.toMutableList()
        if (index in list.indices) {
            list[index] = equation
            graphFunctions.value = list
        }
    }

    // Supabase Cloud Sync Actions
    fun saveAndSyncSnappedMath(
        expression: String,
        result: String,
        solution: String?,
        bitmap: Bitmap?
    ) {
        viewModelScope.launch {
            isSupabaseSyncing.value = true
            supabaseSyncMessage.value = "Saving to database & syncing photo to Supabase..."

            val pair = repository.saveAndSyncToSupabase(
                expression = expression,
                result = result,
                solution = solution,
                bitmap = bitmap,
                type = "Snap & Solve",
                supabaseUrl = preferences.supabaseUrl.value,
                supabaseKey = preferences.supabaseAnonKey.value
            )

            val syncRes = pair.second
            supabaseSyncMessage.value = syncRes.message
            isSupabaseSyncing.value = false
        }
    }
}
