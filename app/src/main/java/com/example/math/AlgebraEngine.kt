package com.example.math

import kotlin.math.*

sealed class EquationSolution {
    data class Linear(
        val x: Double,
        val steps: List<String>
    ) : EquationSolution()

    data class Quadratic(
        val x1Real: Double,
        val x1Imag: Double = 0.0,
        val x2Real: Double,
        val x2Imag: Double = 0.0,
        val discriminant: Double,
        val vertexX: Double,
        val vertexY: Double,
        val steps: List<String>
    ) : EquationSolution()

    data class Cubic(
        val roots: List<String>,
        val steps: List<String>
    ) : EquationSolution()

    data class System2x2(
        val x: Double,
        val y: Double,
        val determinant: Double,
        val steps: List<String>
    ) : EquationSolution()

    data class System3x3(
        val x: Double,
        val y: Double,
        val z: Double,
        val determinant: Double,
        val steps: List<String>
    ) : EquationSolution()

    data class Error(val message: String) : EquationSolution()
}

object AlgebraEngine {

    fun solveLinear(a: Double, b: Double, c: Double = 0.0): EquationSolution {
        // ax + b = c  =>  ax = c - b  =>  x = (c - b) / a
        if (abs(a) < 1e-12) {
            return if (abs(b - c) < 1e-12) {
                EquationSolution.Error("Infinite solutions (identity equation 0 = 0)")
            } else {
                EquationSolution.Error("No solution (contradiction: $b ≠ $c)")
            }
        }
        val x = (c - b) / a
        val steps = listOf(
            "Original Equation: ${formatCoeff(a)}x + $b = $c",
            "Subtract $b from both sides: ${formatCoeff(a)}x = ${c - b}",
            "Divide both sides by $a: x = ${c - b} / $a",
            "Result: x = ${roundVal(x)}"
        )
        return EquationSolution.Linear(x, steps)
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): EquationSolution {
        // ax² + bx + c = 0
        if (abs(a) < 1e-12) {
            return solveLinear(b, c, 0.0)
        }

        val d = b * b - 4 * a * c
        val vertexX = -b / (2 * a)
        val vertexY = c - (b * b) / (4 * a)

        val steps = mutableListOf<String>()
        steps.add("Standard form: ${formatCoeff(a)}x² + ${formatCoeff(b)}x + $c = 0")
        steps.add("Discriminant Δ = b² - 4ac = ($b)² - 4($a)($c) = ${roundVal(d)}")
        steps.add("Parabola Vertex: ($vertexX, $vertexY)")

        if (d > 1e-12) {
            val sqrtD = sqrt(d)
            val x1 = (-b + sqrtD) / (2 * a)
            val x2 = (-b - sqrtD) / (2 * a)
            steps.add("Δ > 0: Two distinct real roots.")
            steps.add("x₁ = (-b + √Δ) / 2a = (-$b + ${roundVal(sqrtD)}) / ${2 * a} = ${roundVal(x1)}")
            steps.add("x₂ = (-b - √Δ) / 2a = (-$b - ${roundVal(sqrtD)}) / ${2 * a} = ${roundVal(x2)}")
            return EquationSolution.Quadratic(
                x1Real = x1, x1Imag = 0.0,
                x2Real = x2, x2Imag = 0.0,
                discriminant = d, vertexX = vertexX, vertexY = vertexY,
                steps = steps
            )
        } else if (abs(d) <= 1e-12) {
            val x = -b / (2 * a)
            steps.add("Δ = 0: One repeated real root (tangent to x-axis).")
            steps.add("x = -b / 2a = -$b / ${2 * a} = ${roundVal(x)}")
            return EquationSolution.Quadratic(
                x1Real = x, x1Imag = 0.0,
                x2Real = x, x2Imag = 0.0,
                discriminant = d, vertexX = vertexX, vertexY = vertexY,
                steps = steps
            )
        } else {
            val realPart = -b / (2 * a)
            val imagPart = sqrt(-d) / (2 * abs(a))
            steps.add("Δ < 0: Two complex conjugate roots.")
            steps.add("x₁ = ${roundVal(realPart)} + ${roundVal(imagPart)}i")
            steps.add("x₂ = ${roundVal(realPart)} - ${roundVal(imagPart)}i")
            return EquationSolution.Quadratic(
                x1Real = realPart, x1Imag = imagPart,
                x2Real = realPart, x2Imag = -imagPart,
                discriminant = d, vertexX = vertexX, vertexY = vertexY,
                steps = steps
            )
        }
    }

    fun solveCubic(a: Double, b: Double, c: Double, d: Double): EquationSolution {
        // ax³ + bx² + cx + d = 0
        if (abs(a) < 1e-12) {
            return solveQuadratic(b, c, d)
        }

        // Depress the cubic: x = t - b/(3a)
        val p = (3 * a * c - b * b) / (3 * a * a)
        val q = (2 * b.pow(3) - 9 * a * b * c + 27 * a * a * d) / (27 * a.pow(3))
        val discriminant = (q / 2).pow(2) + (p / 3).pow(3)

        val shift = -b / (3 * a)
        val roots = mutableListOf<String>()
        val steps = mutableListOf<String>()

        steps.add("Depressed cubic: t³ + pt + q = 0")
        steps.add("where p = ${roundVal(p)}, q = ${roundVal(q)}")
        steps.add("Cubic Discriminant Δ = (q/2)² + (p/3)³ = ${roundVal(discriminant)}")

        if (discriminant > 1e-12) {
            // One real root, two complex
            val u = cbrt(-q / 2 + sqrt(discriminant))
            val v = cbrt(-q / 2 - sqrt(discriminant))
            val t1 = u + v
            val x1 = t1 + shift
            roots.add("x₁ = ${roundVal(x1)} (real)")

            val realPart = -t1 / 2 + shift
            val imagPart = (sqrt(3.0) / 2) * (u - v)
            roots.add("x₂ = ${roundVal(realPart)} + ${roundVal(abs(imagPart))}i")
            roots.add("x₃ = ${roundVal(realPart)} - ${roundVal(abs(imagPart))}i")
            steps.add("One real root and two complex conjugate roots.")
        } else if (abs(discriminant) <= 1e-12) {
            // All roots real, at least two equal
            val u = cbrt(-q / 2)
            val t1 = 2 * u
            val t2 = -u
            roots.add("x₁ = ${roundVal(t1 + shift)}")
            roots.add("x₂ = ${roundVal(t2 + shift)} (multiplicity 2)")
            steps.add("Three real roots with repetition.")
        } else {
            // Three distinct real roots (casus irreducibilis)
            val r = sqrt(-p.pow(3) / 27)
            val phi = acos(-q / (2 * r))
            val m = 2 * sqrt(-p / 3)
            val t1 = m * cos(phi / 3)
            val t2 = m * cos((phi + 2 * Math.PI) / 3)
            val t3 = m * cos((phi + 4 * Math.PI) / 3)
            roots.add("x₁ = ${roundVal(t1 + shift)}")
            roots.add("x₂ = ${roundVal(t2 + shift)}")
            roots.add("x₃ = ${roundVal(t3 + shift)}")
            steps.add("Three distinct real roots using trigonometric method.")
        }

        return EquationSolution.Cubic(roots, steps)
    }

    fun solveSystem2x2(
        a1: Double, b1: Double, c1: Double,
        a2: Double, b2: Double, c2: Double
    ): EquationSolution {
        // a1 x + b1 y = c1
        // a2 x + b2 y = c2
        val det = a1 * b2 - a2 * b1
        if (abs(det) < 1e-12) {
            return EquationSolution.Error("System has no unique solution (Determinant = 0). Lines are parallel or coincident.")
        }

        val detX = c1 * b2 - c2 * b1
        val detY = a1 * c2 - a2 * c1

        val x = detX / det
        val y = detY / det

        val steps = listOf(
            "Equations:",
            "  (1) ${formatCoeff(a1)}x + ${formatCoeff(b1)}y = $c1",
            "  (2) ${formatCoeff(a2)}x + ${formatCoeff(b2)}y = $c2",
            "Cramer's Rule:",
            "  Det(A) = ($a1)($b2) - ($a2)($b1) = ${roundVal(det)}",
            "  Det(X) = ($c1)($b2) - ($c2)($b1) = ${roundVal(detX)}",
            "  Det(Y) = ($a1)($c2) - ($a2)($c1) = ${roundVal(detY)}",
            "Solution:",
            "  x = Det(X) / Det(A) = ${roundVal(x)}",
            "  y = Det(Y) / Det(A) = ${roundVal(y)}"
        )

        return EquationSolution.System2x2(x, y, det, steps)
    }

    fun solveSystem3x3(
        row1: DoubleArray, // a1, b1, c1, d1
        row2: DoubleArray, // a2, b2, c2, d2
        row3: DoubleArray  // a3, b3, c3, d3
    ): EquationSolution {
        // [a b c | d]
        val a = Array(3) { DoubleArray(4) }
        a[0] = row1.clone()
        a[1] = row2.clone()
        a[2] = row3.clone()

        // Gaussian elimination with partial pivoting
        for (i in 0 until 3) {
            var maxRow = i
            for (k in i + 1 until 3) {
                if (abs(a[k][i]) > abs(a[maxRow][i])) maxRow = k
            }
            val temp = a[i]
            a[i] = a[maxRow]
            a[maxRow] = temp

            if (abs(a[i][i]) < 1e-12) {
                return EquationSolution.Error("Matrix is singular or linearly dependent. No unique solution.")
            }

            for (k in i + 1 until 3) {
                val factor = a[k][i] / a[i][i]
                for (j in i until 4) {
                    a[k][j] -= factor * a[i][j]
                }
            }
        }

        // Back substitution
        val z = a[2][3] / a[2][2]
        val y = (a[1][3] - a[1][2] * z) / a[1][1]
        val x = (a[0][3] - a[0][2] * z - a[0][1] * y) / a[0][0]

        val steps = listOf(
            "Forward Gaussian Elimination to Upper Triangular Matrix",
            "Back Substitution:",
            "  z = ${roundVal(z)}",
            "  y = ${roundVal(y)}",
            "  x = ${roundVal(x)}"
        )

        return EquationSolution.System3x3(x, y, z, 1.0, steps)
    }

    private fun cbrt(v: Double): Double = if (v >= 0) v.pow(1.0 / 3.0) else -((-v).pow(1.0 / 3.0))

    private fun formatCoeff(c: Double): String = if (c == 1.0) "" else if (c == -1.0) "-" else roundVal(c)

    private fun roundVal(v: Double): String {
        return MathEngine.formatNumber(v, NumberNotation.STANDARD, 6)
    }
}
