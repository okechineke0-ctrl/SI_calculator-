package com.example.math

import kotlin.math.*

object CalculusEngine {

    /**
     * Numerical derivative f'(x) using high-accuracy 5-point stencil central difference.
     */
    fun derivative(
        x: Double,
        h: Double = 1e-5,
        f: (Double) -> Double
    ): Double {
        val f1 = f(x - 2 * h)
        val f2 = f(x - h)
        val f3 = f(x + h)
        val f4 = f(x + 2 * h)
        return (-f4 + 8 * f3 - 8 * f2 + f1) / (12 * h)
    }

    /**
     * Second derivative f''(x)
     */
    fun secondDerivative(
        x: Double,
        h: Double = 1e-4,
        f: (Double) -> Double
    ): Double {
        return (f(x + h) - 2 * f(x) + f(x - h)) / (h * h)
    }

    /**
     * Definite integral \int_a^b f(x) dx using Composite Simpson's 1/3 Rule (adaptive).
     */
    fun definiteIntegral(
        a: Double,
        b: Double,
        intervals: Int = 1000,
        f: (Double) -> Double
    ): Double {
        if (abs(b - a) < 1e-14) return 0.0
        val n = if (intervals % 2 == 0) intervals else intervals + 1
        val h = (b - a) / n
        var sum = f(a) + f(b)

        for (i in 1 until n) {
            val x = a + i * h
            val fx = f(x)
            sum += if (i % 2 == 0) 2 * fx else 4 * fx
        }

        return (h / 3.0) * sum
    }

    /**
     * Numerical limit as x -> c from left, right, and two-sided.
     */
    data class LimitResult(
        val leftLimit: Double,
        val rightLimit: Double,
        val twoSidedLimit: Double?,
        val exists: Boolean
    )

    fun limit(
        c: Double,
        f: (Double) -> Double
    ): LimitResult {
        val deltas = doubleArrayOf(1e-4, 1e-6, 1e-8)
        var left = 0.0
        var right = 0.0
        for (d in deltas) {
            left = f(c - d)
            right = f(c + d)
        }
        val exists = abs(left - right) < 1e-4 && !left.isNaN() && !right.isNaN() && !left.isInfinite() && !right.isInfinite()
        return LimitResult(
            leftLimit = left,
            rightLimit = right,
            twoSidedLimit = if (exists) (left + right) / 2.0 else null,
            exists = exists
        )
    }

    /**
     * Tangent line equation: y = mx + c at x0
     */
    data class TangentLine(
        val x0: Double,
        val y0: Double,
        val slope: Double,
        val yIntercept: Double,
        val equationString: String
    )

    fun tangentLineAt(x0: Double, f: (Double) -> Double): TangentLine {
        val y0 = f(x0)
        val m = derivative(x0, f = f)
        val c = y0 - m * x0
        val sign = if (c >= 0) "+ ${roundVal(c)}" else "- ${roundVal(abs(c))}"
        val eq = "y = ${roundVal(m)}x $sign"
        return TangentLine(x0, y0, m, c, eq)
    }

    // Sequences & Series
    data class ArithmeticProgression(
        val a1: Double,
        val d: Double,
        val n: Int,
        val nthTerm: Double,
        val sum: Double
    )

    fun arithmeticProgression(a1: Double, d: Double, n: Int): ArithmeticProgression {
        val an = a1 + (n - 1) * d
        val sum = (n.toDouble() / 2.0) * (a1 + an)
        return ArithmeticProgression(a1, d, n, an, sum)
    }

    data class GeometricProgression(
        val a1: Double,
        val r: Double,
        val n: Int,
        val nthTerm: Double,
        val sum: Double,
        val sumToInfinity: Double?
    )

    fun geometricProgression(a1: Double, r: Double, n: Int): GeometricProgression {
        val an = a1 * r.pow(n - 1)
        val sum = if (abs(r - 1.0) < 1e-12) a1 * n else a1 * (1.0 - r.pow(n)) / (1.0 - r)
        val sumInf = if (abs(r) < 1.0) a1 / (1.0 - r) else null
        return GeometricProgression(a1, r, n, an, sum, sumInf)
    }

    fun fibonacci(n: Int): Long {
        if (n <= 0) return 0
        if (n == 1) return 1
        var a = 0L
        var b = 1L
        for (i in 2..n) {
            val c = a + b
            a = b
            b = c
        }
        return b
    }

    private fun roundVal(v: Double): String {
        return MathEngine.formatNumber(v, NumberNotation.STANDARD, 6)
    }
}
