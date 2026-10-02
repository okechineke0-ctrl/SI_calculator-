package com.example.math

import kotlin.math.*

data class FinanceResult(
    val title: String,
    val outputs: Map<String, String>,
    val explanation: String
)

object FinanceEngine {

    fun simpleInterest(principal: Double, annualRatePercent: Double, timeYears: Double): FinanceResult {
        val interest = principal * (annualRatePercent / 100.0) * timeYears
        val total = principal + interest
        return FinanceResult(
            title = "Simple Interest",
            outputs = mapOf(
                "Interest Earned" to "$${roundVal(interest)}",
                "Total Amount" to "$${roundVal(total)}"
            ),
            explanation = "I = P × r × t = $principal × ${annualRatePercent / 100.0} × $timeYears"
        )
    }

    fun compoundInterest(
        principal: Double,
        annualRatePercent: Double,
        timeYears: Double,
        compoundingFreqPerYear: Int = 12
    ): FinanceResult {
        val r = annualRatePercent / 100.0
        val n = compoundingFreqPerYear.toDouble()
        val total = principal * (1.0 + r / n).pow(n * timeYears)
        val interest = total - principal
        return FinanceResult(
            title = "Compound Interest",
            outputs = mapOf(
                "Total Future Value" to "$${roundVal(total)}",
                "Total Interest" to "$${roundVal(interest)}"
            ),
            explanation = "A = P (1 + r/n)^(nt) with n = $compoundingFreqPerYear compoundings/year"
        )
    }

    fun loanEmi(principal: Double, annualRatePercent: Double, tenureYears: Double): FinanceResult {
        val monthlyRate = (annualRatePercent / 100.0) / 12.0
        val numMonths = (tenureYears * 12).toInt()
        val emi = if (monthlyRate > 0.0) {
            (principal * monthlyRate * (1.0 + monthlyRate).pow(numMonths)) /
                    ((1.0 + monthlyRate).pow(numMonths) - 1.0)
        } else {
            principal / numMonths
        }
        val totalPayment = emi * numMonths
        val totalInterest = totalPayment - principal
        return FinanceResult(
            title = "Loan Monthly Payment (EMI)",
            outputs = mapOf(
                "Monthly Payment (EMI)" to "$${roundVal(emi)}",
                "Total Interest" to "$${roundVal(totalInterest)}",
                "Total Repayment" to "$${roundVal(totalPayment)}"
            ),
            explanation = "EMI = [P × r × (1+r)^n] / [(1+r)^n - 1] for $numMonths months"
        )
    }

    fun percentageChange(oldValue: Double, newValue: Double): FinanceResult {
        if (oldValue == 0.0) throw ArithmeticException("Old value cannot be zero for percentage change")
        val change = newValue - oldValue
        val percent = (change / oldValue) * 100.0
        val isIncrease = percent >= 0
        return FinanceResult(
            title = "Percentage Change",
            outputs = mapOf(
                "Difference" to "${roundVal(change)}",
                "Percentage Change" to "${if (isIncrease) "+" else ""}${roundVal(percent)}%",
                "Direction" to if (isIncrease) "Increase" else "Decrease"
            ),
            explanation = "((New - Old) / Old) × 100 = (($newValue - $oldValue) / $oldValue) × 100"
        )
    }

    fun discountAndTax(originalPrice: Double, discountPercent: Double, taxPercent: Double): FinanceResult {
        val discountAmount = originalPrice * (discountPercent / 100.0)
        val discountedPrice = originalPrice - discountAmount
        val taxAmount = discountedPrice * (taxPercent / 100.0)
        val finalPrice = discountedPrice + taxAmount
        return FinanceResult(
            title = "Discount & Sales Tax",
            outputs = mapOf(
                "Discount Amount" to "-$${roundVal(discountAmount)}",
                "Price after Discount" to "$${roundVal(discountedPrice)}",
                "Sales Tax" to "+$${roundVal(taxAmount)}",
                "Final Total" to "$${roundVal(finalPrice)}"
            ),
            explanation = "Final = (Price - Discount) + Tax"
        )
    }

    private fun roundVal(v: Double): String {
        return MathEngine.formatNumber(v, NumberNotation.STANDARD, 2)
    }
}
