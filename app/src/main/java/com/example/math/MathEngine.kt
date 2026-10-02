package com.example.math

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.*

enum class AngleMode {
    DEG, RAD, GRAD
}

enum class NumberNotation {
    STANDARD, SCIENTIFIC, ENGINEERING
}

/**
 * Result of evaluating a mathematical expression.
 */
data class MathResult(
    val value: Double,
    val formatted: String,
    val scientific: String,
    val engineering: String,
    val fraction: String? = null,
    val error: String? = null
) {
    val isSuccess: Boolean get() = error == null
}

/**
 * Professional deterministic mathematical evaluator.
 * Uses recursive descent / shunting-yard with full support for:
 * - Order of operations (BODMAS / PEMDAS)
 * - Scientific functions (trig, inverse trig, hyperbolic, log, ln, exp, powers, roots)
 * - Factorials, permutations (nPr), combinations (nCr)
 * - Percentages, absolute value
 * - Constants (pi, e, phi)
 * - Angle modes (DEG, RAD, GRAD)
 * - High precision and graceful error handling
 */
class MathEngine(
    var angleMode: AngleMode = AngleMode.DEG,
    var precision: Int = 10,
    var notation: NumberNotation = NumberNotation.STANDARD
) {

    fun evaluate(expression: String, previousAnswer: Double = 0.0): MathResult {
        if (expression.isBlank()) {
            return MathResult(0.0, "0", "0", "0", "0")
        }

        try {
            val sanitized = sanitize(expression, previousAnswer)
            val tokens = tokenize(sanitized)
            val rpn = toRpn(tokens)
            val value = evaluateRpn(rpn)

            if (value.isNaN()) {
                return MathResult(Double.NaN, "Undefined", "Undefined", "Undefined", error = "Undefined mathematical expression")
            }
            if (value.isInfinite()) {
                return MathResult(value, if (value > 0) "Infinity" else "-Infinity", "", "", error = "Value overflow / Division by zero")
            }

            val formatted = formatNumber(value, notation, precision)
            val scientific = formatScientific(value, precision)
            val engineering = formatEngineering(value, precision)
            val fraction = toFraction(value)

            return MathResult(
                value = value,
                formatted = formatted,
                scientific = scientific,
                engineering = engineering,
                fraction = fraction
            )
        } catch (e: ArithmeticException) {
            return MathResult(Double.NaN, "Error", "Error", "Error", error = e.message ?: "Arithmetic error")
        } catch (e: IllegalArgumentException) {
            return MathResult(Double.NaN, "Invalid Expression", "Error", "Error", error = e.message ?: "Invalid syntax")
        } catch (e: Exception) {
            return MathResult(Double.NaN, "Syntax Error", "Error", "Error", error = "Syntax error. Please check parentheses and operators.")
        }
    }

    private fun sanitize(input: String, ans: Double): String {
        return input
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "pi")
            .replace("φ", "phi")
            .replace("Ans", "($ans)")
            .replace("ans", "($ans)")
            .replace("sin⁻¹", "asin")
            .replace("cos⁻¹", "acos")
            .replace("tan⁻¹", "atan")
            .replace("sinh⁻¹", "asinh")
            .replace("cosh⁻¹", "acosh")
            .replace("tanh⁻¹", "atanh")
            .replace("√", "sqrt")
            .replace("∛", "cbrt")
            .trim()
    }

    private sealed class Token {
        data class Number(val value: Double) : Token()
        data class Operator(val op: Char, val precedence: Int, val isRightAssociative: Boolean = false) : Token()
        data class Function(val name: String, val argCount: Int = 1) : Token()
        data object LeftParen : Token()
        data object RightParen : Token()
        data object Comma : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var i = 0
        val len = expr.length

        var lastToken: Token? = null

        while (i < len) {
            val c = expr[i]

            if (c.isWhitespace()) {
                i++
                continue
            }

            if (c.isDigit() || c == '.') {
                val start = i
                var hasDot = (c == '.')
                i++
                while (i < len && (expr[i].isDigit() || (!hasDot && expr[i] == '.'))) {
                    if (expr[i] == '.') hasDot = true
                    i++
                }
                // Check for scientific notation in number: e.g., 1e-4 or 2.5E3
                if (i < len && (expr[i] == 'e' || expr[i] == 'E') && i + 1 < len) {
                    val next = expr[i + 1]
                    if (next.isDigit() || next == '+' || next == '-') {
                        i++ // consume 'e'
                        if (expr[i] == '+' || expr[i] == '-') i++
                        while (i < len && expr[i].isDigit()) i++
                    }
                }
                val numStr = expr.substring(start, i)
                val num = numStr.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: $numStr")
                
                // Implicit multiplication: e.g. 2(3) or 2pi
                if (lastToken is Token.Number || lastToken is Token.RightParen) {
                    tokens.add(Token.Operator('*', 3))
                }
                val token = Token.Number(num)
                tokens.add(token)
                lastToken = token
                continue
            }

            if (c.isLetter()) {
                val start = i
                while (i < len && (expr[i].isLetter() || expr[i].isDigit())) {
                    i++
                }
                val word = expr.substring(start, i).lowercase()

                val token: Token = when (word) {
                    "pi" -> {
                        if (lastToken is Token.Number || lastToken is Token.RightParen) {
                            tokens.add(Token.Operator('*', 3))
                        }
                        Token.Number(Math.PI)
                    }
                    "e" -> {
                        if (lastToken is Token.Number || lastToken is Token.RightParen) {
                            tokens.add(Token.Operator('*', 3))
                        }
                        Token.Number(Math.E)
                    }
                    "phi" -> {
                        if (lastToken is Token.Number || lastToken is Token.RightParen) {
                            tokens.add(Token.Operator('*', 3))
                        }
                        Token.Number((1.0 + sqrt(5.0)) / 2.0)
                    }
                    "npr", "perm" -> Token.Operator('P', 4)
                    "ncr", "comb" -> Token.Operator('C', 4)
                    "mod" -> Token.Operator('%', 3)
                    else -> {
                        if (lastToken is Token.Number || lastToken is Token.RightParen) {
                            tokens.add(Token.Operator('*', 3))
                        }
                        Token.Function(word)
                    }
                }
                tokens.add(token)
                lastToken = token
                continue
            }

            // Unary operators vs Binary operators
            if (c == '+' || c == '-') {
                val isUnary = lastToken == null || lastToken is Token.LeftParen || lastToken is Token.Operator || lastToken is Token.Comma
                if (isUnary) {
                    if (c == '-') {
                        // Unary minus has precedence 5, right associative
                        val token = Token.Operator('u', 5, isRightAssociative = true)
                        tokens.add(token)
                        lastToken = token
                    }
                    // Unary plus is ignored
                } else {
                    val token = Token.Operator(c, 2)
                    tokens.add(token)
                    lastToken = token
                }
                i++
                continue
            }

            when (c) {
                '*' -> {
                    val token = Token.Operator('*', 3)
                    tokens.add(token)
                    lastToken = token
                }
                '/' -> {
                    val token = Token.Operator('/', 3)
                    tokens.add(token)
                    lastToken = token
                }
                '^' -> {
                    val token = Token.Operator('^', 6, isRightAssociative = true)
                    tokens.add(token)
                    lastToken = token
                }
                '%' -> {
                    // Postfix percentage: 50% = 0.5
                    val token = Token.Operator('%', 7)
                    tokens.add(token)
                    lastToken = token
                }
                '!' -> {
                    // Postfix factorial
                    val token = Token.Operator('!', 7)
                    tokens.add(token)
                    lastToken = token
                }
                '(' -> {
                    if (lastToken is Token.Number || lastToken is Token.RightParen) {
                        tokens.add(Token.Operator('*', 3))
                    }
                    val token = Token.LeftParen
                    tokens.add(token)
                    lastToken = token
                }
                ')' -> {
                    val token = Token.RightParen
                    tokens.add(token)
                    lastToken = token
                }
                ',' -> {
                    val token = Token.Comma
                    tokens.add(token)
                    lastToken = token
                }
                else -> throw IllegalArgumentException("Unknown character: $c")
            }
            i++
        }

        return tokens
    }

    private fun toRpn(tokens: List<Token>): List<Token> {
        val output = mutableListOf<Token>()
        val stack = ArrayDeque<Token>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> output.add(token)
                is Token.Function -> stack.addLast(token)
                is Token.Operator -> {
                    // Postfix operator can immediately be applied or processed
                    while (stack.isNotEmpty()) {
                        val top = stack.last()
                        if (top is Token.Operator) {
                            val shouldPop = if (token.isRightAssociative) {
                                token.precedence < top.precedence
                            } else {
                                token.precedence <= top.precedence
                            }
                            if (shouldPop) {
                                output.add(stack.removeLast())
                            } else break
                        } else break
                    }
                    stack.addLast(token)
                }
                is Token.Comma -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.LeftParen) {
                        output.add(stack.removeLast())
                    }
                    if (stack.isEmpty()) throw IllegalArgumentException("Misplaced comma or mismatched parentheses")
                }
                is Token.LeftParen -> stack.addLast(token)
                is Token.RightParen -> {
                    while (stack.isNotEmpty() && stack.last() !is Token.LeftParen) {
                        output.add(stack.removeLast())
                    }
                    if (stack.isEmpty()) throw IllegalArgumentException("Mismatched parentheses")
                    stack.removeLast() // discard '('
                    if (stack.isNotEmpty() && stack.last() is Token.Function) {
                        output.add(stack.removeLast())
                    }
                }
            }
        }

        while (stack.isNotEmpty()) {
            val top = stack.removeLast()
            if (top is Token.LeftParen || top is Token.RightParen) {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(top)
        }

        return output
    }

    private fun evaluateRpn(rpn: List<Token>): Double {
        val stack = ArrayDeque<Double>()

        for (token in rpn) {
            when (token) {
                is Token.Number -> stack.addLast(token.value)
                is Token.Operator -> {
                    when (token.op) {
                        'u' -> {
                            if (stack.isEmpty()) throw IllegalArgumentException("Invalid unary minus")
                            val a = stack.removeLast()
                            stack.addLast(-a)
                        }
                        '!' -> {
                            if (stack.isEmpty()) throw IllegalArgumentException("Missing operand for factorial")
                            val a = stack.removeLast()
                            if (a < 0 || a != floor(a)) throw ArithmeticException("Factorial only defined for non-negative integers")
                            if (a > 170) throw ArithmeticException("Factorial overflow (>170)")
                            var res = 1.0
                            val n = a.toLong()
                            for (k in 1..n) res *= k
                            stack.addLast(res)
                        }
                        '%' -> {
                            if (stack.isEmpty()) throw IllegalArgumentException("Missing operand for %")
                            val a = stack.removeLast()
                            stack.addLast(a / 100.0)
                        }
                        '+' -> {
                            val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            stack.addLast(a + b)
                        }
                        '-' -> {
                            val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            stack.addLast(a - b)
                        }
                        '*' -> {
                            val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            stack.addLast(a * b)
                        }
                        '/' -> {
                            val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            if (b == 0.0) throw ArithmeticException("Undefined: division by zero.")
                            stack.addLast(a / b)
                        }
                        '^' -> {
                            val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            stack.addLast(a.pow(b))
                        }
                        'P' -> {
                            val r = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            val n = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            stack.addLast(permutation(n, r))
                        }
                        'C' -> {
                            val r = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            val n = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing operand")
                            stack.addLast(combination(n, r))
                        }
                    }
                }
                is Token.Function -> {
                    val res = executeFunction(token.name, stack)
                    stack.addLast(res)
                }
                else -> {}
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Invalid expression syntax")
        }

        return stack.last()
    }

    private fun executeFunction(name: String, stack: ArrayDeque<Double>): Double {
        return when (name.lowercase()) {
            "sin" -> {
                val x = toRadians(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
                val s = sin(x)
                if (abs(s) < 1e-15) 0.0 else s
            }
            "cos" -> {
                val x = toRadians(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
                val c = cos(x)
                if (abs(c) < 1e-15) 0.0 else c
            }
            "tan" -> {
                val raw = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                val x = toRadians(raw)
                val c = cos(x)
                if (abs(c) < 1e-15) throw ArithmeticException("Tangent undefined at asymptote")
                val t = tan(x)
                if (abs(t) < 1e-15) 0.0 else t
            }
            "cot" -> {
                val raw = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                val x = toRadians(raw)
                val s = sin(x)
                if (abs(s) < 1e-15) throw ArithmeticException("Cotangent undefined at asymptote")
                1.0 / tan(x)
            }
            "sec" -> {
                val x = toRadians(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
                val c = cos(x)
                if (abs(c) < 1e-15) throw ArithmeticException("Secant undefined")
                1.0 / c
            }
            "csc" -> {
                val x = toRadians(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
                val s = sin(x)
                if (abs(s) < 1e-15) throw ArithmeticException("Cosecant undefined")
                1.0 / s
            }
            "asin", "arcsin" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error: asin domain is [-1, 1]")
                fromRadians(asin(x))
            }
            "acos", "arccos" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error: acos domain is [-1, 1]")
                fromRadians(acos(x))
            }
            "atan", "arctan" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                fromRadians(atan(x))
            }
            "sinh" -> sinh(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
            "cosh" -> cosh(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
            "tanh" -> tanh(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
            "asinh" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                ln(x + sqrt(x * x + 1.0))
            }
            "acosh" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x < 1.0) throw ArithmeticException("Domain error: acosh domain is [1, ∞)")
                ln(x + sqrt(x * x - 1.0))
            }
            "atanh" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x <= -1.0 || x >= 1.0) throw ArithmeticException("Domain error: atanh domain is (-1, 1)")
                0.5 * ln((1.0 + x) / (1.0 - x))
            }
            "ln" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x <= 0.0) throw ArithmeticException("Domain error: ln requires positive value")
                ln(x)
            }
            "log", "log10" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x <= 0.0) throw ArithmeticException("Domain error: log requires positive value")
                log10(x)
            }
            "log2" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x <= 0.0) throw ArithmeticException("Domain error: log2 requires positive value")
                ln(x) / ln(2.0)
            }
            "sqrt" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x < 0.0) throw ArithmeticException("Domain error: sqrt of negative number. Use complex mode for imaginary results.")
                sqrt(x)
            }
            "cbrt" -> {
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (x >= 0.0) x.pow(1.0 / 3.0) else -((-x).pow(1.0 / 3.0))
            }
            "abs" -> abs(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
            "exp" -> exp(stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument"))
            "root" -> {
                // root(n, x) -> n-th root of x
                val x = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                val n = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (n == 0.0) throw ArithmeticException("0th root is undefined")
                if (x < 0.0 && n % 2.0 == 0.0) throw ArithmeticException("Even root of negative number is undefined in real numbers")
                if (x < 0.0) -((-x).pow(1.0 / n)) else x.pow(1.0 / n)
            }
            "fact" -> {
                val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Missing argument")
                if (a < 0 || a != floor(a)) throw ArithmeticException("Factorial only defined for non-negative integers")
                var res = 1.0
                for (k in 1..a.toLong()) res *= k
                res
            }
            else -> throw IllegalArgumentException("Unknown function: $name")
        }
    }

    private fun toRadians(angle: Double): Double {
        return when (angleMode) {
            AngleMode.DEG -> Math.toRadians(angle)
            AngleMode.RAD -> angle
            AngleMode.GRAD -> angle * Math.PI / 200.0
        }
    }

    private fun fromRadians(rad: Double): Double {
        return when (angleMode) {
            AngleMode.DEG -> Math.toDegrees(rad)
            AngleMode.RAD -> rad
            AngleMode.GRAD -> rad * 200.0 / Math.PI
        }
    }

    private fun permutation(n: Double, r: Double): Double {
        if (n < 0 || r < 0 || n != floor(n) || r != floor(r) || r > n) {
            throw ArithmeticException("nPr requires integers with 0 <= r <= n")
        }
        var res = 1.0
        val ni = n.toLong()
        val ri = r.toLong()
        for (i in (ni - ri + 1)..ni) {
            res *= i
        }
        return res
    }

    private fun combination(n: Double, r: Double): Double {
        if (n < 0 || r < 0 || n != floor(n) || r != floor(r) || r > n) {
            throw ArithmeticException("nCr requires integers with 0 <= r <= n")
        }
        val ni = n.toLong()
        var ri = r.toLong()
        if (ri > ni - ri) ri = ni - ri
        var res = 1.0
        for (i in 1..ri) {
            res = res * (ni - i + 1) / i
        }
        return res
    }

    companion object {
        fun formatNumber(value: Double, notation: NumberNotation, precision: Int): String {
            if (value.isNaN()) return "NaN"
            if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
            if (value == 0.0) return "0"

            return when (notation) {
                NumberNotation.STANDARD -> {
                    if (abs(value) >= 1e12 || (abs(value) < 1e-4 && abs(value) > 0.0)) {
                        formatScientific(value, precision)
                    } else {
                        val rounded = BigDecimal(value, MathContext(precision + 4, RoundingMode.HALF_UP))
                            .setScale(precision, RoundingMode.HALF_UP)
                            .stripTrailingZeros()
                        rounded.toPlainString()
                    }
                }
                NumberNotation.SCIENTIFIC -> formatScientific(value, precision)
                NumberNotation.ENGINEERING -> formatEngineering(value, precision)
            }
        }

        fun formatScientific(value: Double, precision: Int): String {
            if (value == 0.0) return "0"
            val absVal = abs(value)
            val exp = floor(log10(absVal)).toInt()
            val mantissa = value / 10.0.pow(exp.toDouble())
            val roundedMantissa = BigDecimal(mantissa).setScale(min(precision, 6), RoundingMode.HALF_UP).stripTrailingZeros()
            return "${roundedMantissa.toPlainString()} × 10^$exp"
        }

        fun formatEngineering(value: Double, precision: Int): String {
            if (value == 0.0) return "0"
            val absVal = abs(value)
            val rawExp = floor(log10(absVal)).toInt()
            val engExp = if (rawExp >= 0) (rawExp / 3) * 3 else ((rawExp - 2) / 3) * 3
            val mantissa = value / 10.0.pow(engExp.toDouble())
            val roundedMantissa = BigDecimal(mantissa).setScale(min(precision, 6), RoundingMode.HALF_UP).stripTrailingZeros()

            val prefix = when (engExp) {
                -15 -> " f"
                -12 -> " p"
                -9 -> " n"
                -6 -> " µ"
                -3 -> " m"
                0 -> ""
                3 -> " k"
                6 -> " M"
                9 -> " G"
                12 -> " T"
                15 -> " P"
                else -> " × 10^$engExp"
            }
            return "${roundedMantissa.toPlainString()}$prefix"
        }

        fun toFraction(value: Double, tolerance: Double = 1.0E-6): String? {
            if (abs(value) > 1e6 || value.isNaN() || value.isInfinite()) return null
            if (abs(value - round(value)) < 1e-9) return null // already integer

            var h1 = 1L; var h2 = 0L
            var k1 = 0L; var k2 = 1L
            var b = value
            do {
                val a = floor(b).toLong()
                var aux = h1
                h1 = a * h1 + h2
                h2 = aux
                aux = k1
                k1 = a * k1 + k2
                k2 = aux
                b = 1.0 / (b - a)
            } while (abs(value - h1.toDouble() / k1.toDouble()) > value * tolerance && k1 < 100000L && b.isFinite() && abs(b) < 1e9)

            return if (k1 in 2..100000L) {
                if (abs(h1) > k1) {
                    val whole = h1 / k1
                    val rem = abs(h1 % k1)
                    if (rem != 0L) "$whole $rem/$k1" else "$h1/$k1"
                } else {
                    "$h1/$k1"
                }
            } else null
        }
    }
}
