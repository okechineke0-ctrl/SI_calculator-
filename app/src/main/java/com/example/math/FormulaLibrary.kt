package com.example.math

data class FormulaItem(
    val id: String,
    val name: String,
    val category: String,
    val formula: String,
    val variables: List<String>,
    val example: String,
    val defaultExpression: String
)

object FormulaLibrary {

    val allFormulas: List<FormulaItem> = listOf(
        // Algebra
        FormulaItem(
            id = "quad_formula",
            name = "Quadratic Formula",
            category = "Algebra",
            formula = "x = (-b ± √(b² - 4ac)) / (2a)",
            variables = listOf("a: coefficient of x²", "b: coefficient of x", "c: constant term"),
            example = "For x² - 5x + 6 = 0, a=1, b=-5, c=6 gives x = 2, 3",
            defaultExpression = "(-(-5) + sqrt((-5)^2 - 4*1*6)) / (2*1)"
        ),
        FormulaItem(
            id = "binom_theorem",
            name = "Binomial Expansion",
            category = "Algebra",
            formula = "(a + b)ⁿ = Σₖ (nCk · aⁿ⁻ᵏ · bᵏ)",
            variables = listOf("n: non-negative integer power", "k: index from 0 to n"),
            example = "(x + y)² = x² + 2xy + y²",
            defaultExpression = "3*3 + 2*3*4 + 4*4"
        ),
        FormulaItem(
            id = "log_change_base",
            name = "Logarithm Change of Base",
            category = "Algebra",
            formula = "log_b(x) = ln(x) / ln(b)",
            variables = listOf("x: argument", "b: base"),
            example = "log₂(8) = ln(8) / ln(2) = 3",
            defaultExpression = "ln(8) / ln(2)"
        ),

        // Trigonometry
        FormulaItem(
            id = "pythagorean_trig",
            name = "Pythagorean Identity",
            category = "Trigonometry",
            formula = "sin²(θ) + cos²(θ) = 1",
            variables = listOf("θ: angle in radians or degrees"),
            example = "sin²(30°) + cos²(30°) = 0.25 + 0.75 = 1",
            defaultExpression = "sin(30)^2 + cos(30)^2"
        ),
        FormulaItem(
            id = "law_of_cosines",
            name = "Law of Cosines",
            category = "Trigonometry",
            formula = "c² = a² + b² - 2ab · cos(C)",
            variables = listOf("a, b: adjacent sides", "C: included angle", "c: opposite side"),
            example = "a=3, b=4, C=90° => c = √(9+16) = 5",
            defaultExpression = "sqrt(3^2 + 4^2 - 2*3*4*cos(90))"
        ),
        FormulaItem(
            id = "double_angle_sin",
            name = "Double Angle Sine",
            category = "Trigonometry",
            formula = "sin(2θ) = 2 · sin(θ) · cos(θ)",
            variables = listOf("θ: angle"),
            example = "sin(60°) = 2 sin(30°) cos(30°)",
            defaultExpression = "2 * sin(30) * cos(30)"
        ),

        // Calculus
        FormulaItem(
            id = "power_rule_deriv",
            name = "Power Rule for Derivatives",
            category = "Calculus",
            formula = "d/dx [xⁿ] = n · xⁿ⁻¹",
            variables = listOf("n: real exponent", "x: variable"),
            example = "d/dx [x³] = 3x²",
            defaultExpression = "3 * 4^2"
        ),
        FormulaItem(
            id = "simpson_integral",
            name = "Simpson's 1/3 Rule",
            category = "Calculus",
            formula = "∫ f(x)dx ≈ (h/3) [f(a) + 4f(m) + f(b)]",
            variables = listOf("h: step size (b-a)/2", "m: midpoint (a+b)/2"),
            example = "Numerical integration for arbitrary curves",
            defaultExpression = "(1.0/3) * (0 + 4*0.25 + 1)"
        ),
        FormulaItem(
            id = "taylor_series",
            name = "Taylor Series Expansion",
            category = "Calculus",
            formula = "f(x) = Σ (f⁽ⁿ⁾(a)/n!) · (x - a)ⁿ",
            variables = listOf("a: center point", "n: derivative order"),
            example = "eˣ = 1 + x + x²/2! + x³/3! + ...",
            defaultExpression = "1 + 1 + 1/2 + 1/6"
        ),

        // Physics
        FormulaItem(
            id = "newton_second",
            name = "Newton's Second Law",
            category = "Physics",
            formula = "F = m · a",
            variables = listOf("F: Net force (N)", "m: mass (kg)", "a: acceleration (m/s²)"),
            example = "Mass 10 kg accelerated at 2 m/s² => F = 20 N",
            defaultExpression = "10 * 2"
        ),
        FormulaItem(
            id = "kinetic_energy",
            name = "Kinetic Energy",
            category = "Physics",
            formula = "KE = ½ m v²",
            variables = listOf("m: mass (kg)", "v: speed (m/s)"),
            example = "Car of 1000 kg at 20 m/s => KE = 200,000 J",
            defaultExpression = "0.5 * 1000 * 20^2"
        ),
        FormulaItem(
            id = "ohms_law",
            name = "Ohm's Law",
            category = "Physics",
            formula = "V = I · R",
            variables = listOf("V: Voltage (V)", "I: Current (A)", "R: Resistance (Ω)"),
            example = "Current 2 A through 6 Ω resistor => 12 V",
            defaultExpression = "2 * 6"
        ),
        FormulaItem(
            id = "gravitational_force",
            name = "Newton's Universal Gravitation",
            category = "Physics",
            formula = "F = G · (m₁ · m₂) / r²",
            variables = listOf("G: 6.67430e-11 N·m²/kg²", "m1, m2: masses", "r: separation distance"),
            example = "Attractive force between two celestial bodies",
            defaultExpression = "6.6743e-11 * (5.972e24 * 7.342e22) / (3.844e8)^2"
        ),

        // Statistics
        FormulaItem(
            id = "std_deviation",
            name = "Sample Standard Deviation",
            category = "Statistics",
            formula = "s = √[ Σ(xᵢ - x̄)² / (n - 1) ]",
            variables = listOf("xᵢ: individual values", "x̄: sample mean", "n: sample size"),
            example = "Measures spread or dispersion of data points around mean",
            defaultExpression = "sqrt(((1-3)^2 + (3-3)^2 + (5-3)^2) / 2)"
        ),
        FormulaItem(
            id = "bayes_theorem",
            name = "Bayes' Theorem",
            category = "Statistics",
            formula = "P(A|B) = [ P(B|A) · P(A) ] / P(B)",
            variables = listOf("P(A|B): Posterior", "P(B|A): Likelihood", "P(A): Prior", "P(B): Evidence"),
            example = "Updating probability given new observed evidence",
            defaultExpression = "(0.99 * 0.01) / ((0.99 * 0.01) + (0.05 * 0.99))"
        ),

        // Finance
        FormulaItem(
            id = "compound_interest_f",
            name = "Compound Interest Future Value",
            category = "Finance",
            formula = "A = P · (1 + r/n)ⁿᵗ",
            variables = listOf("P: principal", "r: annual interest rate", "n: compounds per year", "t: years"),
            example = "$1000 at 5% compounded monthly for 3 years => $1161.47",
            defaultExpression = "1000 * (1 + 0.05/12)^(12*3)"
        ),
        FormulaItem(
            id = "loan_emi_f",
            name = "Loan Monthly Payment (EMI)",
            category = "Finance",
            formula = "EMI = [P · r · (1+r)ⁿ] / [(1+r)ⁿ - 1]",
            variables = listOf("P: principal loan", "r: monthly rate (annual/12)", "n: total number of months"),
            example = "Equal monthly installments for home or auto loans",
            defaultExpression = "(10000 * (0.06/12) * (1 + 0.06/12)^36) / ((1 + 0.06/12)^36 - 1)"
        )
    )
}
