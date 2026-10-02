package com.example.math

import kotlin.math.*

data class PhysicsCalculationResult(
    val title: String,
    val formula: String,
    val given: Map<String, String>,
    val substitution: String,
    val answer: String,
    val unit: String
)

object PhysicsEngine {

    // 1. Kinematics: v = u + at
    fun velocityFromAcceleration(u: Double, a: Double, t: Double): PhysicsCalculationResult {
        val v = u + a * t
        return PhysicsCalculationResult(
            title = "Final Velocity (Kinematics)",
            formula = "v = u + at",
            given = mapOf("Initial velocity (u)" to "$u m/s", "Acceleration (a)" to "$a m/s²", "Time (t)" to "$t s"),
            substitution = "v = $u + ($a)($t)",
            answer = roundVal(v),
            unit = "m/s"
        )
    }

    // 2. Distance: s = ut + 0.5 a t^2
    fun displacement(u: Double, a: Double, t: Double): PhysicsCalculationResult {
        val s = u * t + 0.5 * a * t * t
        return PhysicsCalculationResult(
            title = "Displacement (Kinematics)",
            formula = "s = ut + ½at²",
            given = mapOf("Initial velocity (u)" to "$u m/s", "Acceleration (a)" to "$a m/s²", "Time (t)" to "$t s"),
            substitution = "s = ($u)($t) + 0.5($a)($t)²",
            answer = roundVal(s),
            unit = "m"
        )
    }

    // 3. Newton's Second Law: F = ma
    fun force(m: Double, a: Double): PhysicsCalculationResult {
        val f = m * a
        return PhysicsCalculationResult(
            title = "Force (Newton's 2nd Law)",
            formula = "F = m · a",
            given = mapOf("Mass (m)" to "$m kg", "Acceleration (a)" to "$a m/s²"),
            substitution = "F = ($m kg) × ($a m/s²)",
            answer = roundVal(f),
            unit = "N"
        )
    }

    // 4. Kinetic Energy: KE = 0.5 m v^2
    fun kineticEnergy(m: Double, v: Double): PhysicsCalculationResult {
        val ke = 0.5 * m * v * v
        return PhysicsCalculationResult(
            title = "Kinetic Energy",
            formula = "KE = ½ m v²",
            given = mapOf("Mass (m)" to "$m kg", "Velocity (v)" to "$v m/s"),
            substitution = "KE = 0.5 × ($m) × ($v)²",
            answer = roundVal(ke),
            unit = "J"
        )
    }

    // 5. Potential Energy: PE = m g h
    fun potentialEnergy(m: Double, h: Double, g: Double = 9.80665): PhysicsCalculationResult {
        val pe = m * g * h
        return PhysicsCalculationResult(
            title = "Gravitational Potential Energy",
            formula = "PE = m · g · h",
            given = mapOf("Mass (m)" to "$m kg", "Height (h)" to "$h m", "Gravity (g)" to "$g m/s²"),
            substitution = "PE = ($m) × ($g) × ($h)",
            answer = roundVal(pe),
            unit = "J"
        )
    }

    // 6. Work: W = F d
    fun work(f: Double, d: Double): PhysicsCalculationResult {
        val w = f * d
        return PhysicsCalculationResult(
            title = "Work Done",
            formula = "W = F · d",
            given = mapOf("Force (F)" to "$f N", "Displacement (d)" to "$d m"),
            substitution = "W = ($f N) × ($d m)",
            answer = roundVal(w),
            unit = "J"
        )
    }

    // 7. Power: P = W / t
    fun power(w: Double, t: Double): PhysicsCalculationResult {
        require(t > 0.0) { "Time must be greater than zero" }
        val p = w / t
        return PhysicsCalculationResult(
            title = "Power",
            formula = "P = W / t",
            given = mapOf("Work (W)" to "$w J", "Time (t)" to "$t s"),
            substitution = "P = ($w J) / ($t s)",
            answer = roundVal(p),
            unit = "W"
        )
    }

    // 8. Ohm's Law: V = I R
    fun voltage(i: Double, r: Double): PhysicsCalculationResult {
        val v = i * r
        return PhysicsCalculationResult(
            title = "Voltage (Ohm's Law)",
            formula = "V = I · R",
            given = mapOf("Current (I)" to "$i A", "Resistance (R)" to "$r Ω"),
            substitution = "V = ($i A) × ($r Ω)",
            answer = roundVal(v),
            unit = "V"
        )
    }

    // 9. Current: I = V / R
    fun current(v: Double, r: Double): PhysicsCalculationResult {
        require(r > 0.0) { "Resistance must be greater than zero" }
        val i = v / r
        return PhysicsCalculationResult(
            title = "Current (Ohm's Law)",
            formula = "I = V / R",
            given = mapOf("Voltage (V)" to "$v V", "Resistance (R)" to "$r Ω"),
            substitution = "I = ($v V) / ($r Ω)",
            answer = roundVal(i),
            unit = "A"
        )
    }

    // 10. Electrical Power: P = V I
    fun electricalPower(v: Double, i: Double): PhysicsCalculationResult {
        val p = v * i
        return PhysicsCalculationResult(
            title = "Electrical Power",
            formula = "P = V · I",
            given = mapOf("Voltage (V)" to "$v V", "Current (I)" to "$i A"),
            substitution = "P = ($v V) × ($i A)",
            answer = roundVal(p),
            unit = "W"
        )
    }

    // 11. Resistors in Parallel: 1/R_eq = 1/R1 + 1/R2
    fun parallelResistance(r1: Double, r2: Double): PhysicsCalculationResult {
        require(r1 > 0.0 && r2 > 0.0) { "Resistances must be greater than zero" }
        val req = (r1 * r2) / (r1 + r2)
        return PhysicsCalculationResult(
            title = "Parallel Resistance",
            formula = "R_eq = (R₁ · R₂) / (R₁ + R₂)",
            given = mapOf("Resistor 1 (R₁)" to "$r1 Ω", "Resistor 2 (R₂)" to "$r2 Ω"),
            substitution = "R_eq = ($r1 × $r2) / ($r1 + $r2)",
            answer = roundVal(req),
            unit = "Ω"
        )
    }

    // 12. Wave Equation: v = f λ
    fun waveSpeed(f: Double, lambda: Double): PhysicsCalculationResult {
        val v = f * lambda
        return PhysicsCalculationResult(
            title = "Wave Speed",
            formula = "v = f · λ",
            given = mapOf("Frequency (f)" to "$f Hz", "Wavelength (λ)" to "$lambda m"),
            substitution = "v = ($f Hz) × ($lambda m)",
            answer = roundVal(v),
            unit = "m/s"
        )
    }

    private fun roundVal(v: Double): String {
        return MathEngine.formatNumber(v, NumberNotation.STANDARD, 6)
    }
}
