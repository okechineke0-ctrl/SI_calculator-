package com.example.math

import kotlin.math.*

data class ComplexNumber(
    val real: Double,
    val imag: Double
) {
    val magnitude: Double get() = sqrt(real * real + imag * imag)
    val argumentRad: Double get() = atan2(imag, real)
    val argumentDeg: Double get() = Math.toDegrees(argumentRad)

    val conjugate: ComplexNumber get() = ComplexNumber(real, -imag)

    operator fun plus(other: ComplexNumber) = ComplexNumber(real + other.real, imag + other.imag)
    operator fun minus(other: ComplexNumber) = ComplexNumber(real - other.real, imag - other.imag)
    operator fun times(other: ComplexNumber) = ComplexNumber(
        real * other.real - imag * other.imag,
        real * other.imag + imag * other.real
    )
    operator fun times(scalar: Double) = ComplexNumber(real * scalar, imag * scalar)
    operator fun div(other: ComplexNumber): ComplexNumber {
        val denom = other.real * other.real + other.imag * other.imag
        if (denom == 0.0) throw ArithmeticException("Division by zero in complex numbers")
        return ComplexNumber(
            (real * other.real + imag * other.imag) / denom,
            (imag * other.real - real * other.imag) / denom
        )
    }

    fun toRectangularString(): String {
        val rStr = roundVal(real)
        val absI = abs(imag)
        val iStr = roundVal(absI)
        return when {
            imag == 0.0 -> rStr
            real == 0.0 -> if (imag == 1.0) "i" else if (imag == -1.0) "-i" else "${roundVal(imag)}i"
            imag > 0 -> "$rStr + ${if (absI == 1.0) "" else iStr}i"
            else -> "$rStr - ${if (absI == 1.0) "" else iStr}i"
        }
    }

    fun toPolarString(): String {
        val r = roundVal(magnitude)
        val deg = roundVal(argumentDeg)
        return "$r ∠ $deg°"
    }

    fun toEulerString(): String {
        val r = roundVal(magnitude)
        val rad = roundVal(argumentRad)
        return "$r e^(${rad}i)"
    }

    companion object {
        val ZERO = ComplexNumber(0.0, 0.0)
        val ONE = ComplexNumber(1.0, 0.0)
        val I = ComplexNumber(0.0, 1.0)

        fun fromPolar(r: Double, thetaRad: Double) = ComplexNumber(r * cos(thetaRad), r * sin(thetaRad))

        private fun roundVal(v: Double): String {
            return MathEngine.formatNumber(v, NumberNotation.STANDARD, 5)
        }
    }
}
