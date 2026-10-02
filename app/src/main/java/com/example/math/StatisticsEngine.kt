package com.example.math

import kotlin.math.*

data class DescriptiveStatistics(
    val count: Int,
    val sum: Double,
    val mean: Double,
    val median: Double,
    val mode: List<Double>,
    val min: Double,
    val max: Double,
    val range: Double,
    val sampleVariance: Double,
    val populationVariance: Double,
    val sampleStandardDeviation: Double,
    val populationStandardDeviation: Double,
    val q1: Double,
    val q3: Double,
    val iqr: Double
)

data class LinearRegressionResult(
    val slope: Double,
    val intercept: Double,
    val r: Double,
    val rSquared: Double,
    val equation: String
)

object StatisticsEngine {

    fun calculateDescriptive(data: List<Double>): DescriptiveStatistics {
        require(data.isNotEmpty()) { "Data set must not be empty" }
        val sorted = data.sorted()
        val n = sorted.size
        val sum = sorted.sum()
        val mean = sum / n

        // Median
        val median = if (n % 2 == 1) {
            sorted[n / 2]
        } else {
            (sorted[n / 2 - 1] + sorted[n / 2]) / 2.0
        }

        // Mode
        val freqMap = mutableMapOf<Double, Int>()
        for (v in sorted) {
            freqMap[v] = (freqMap[v] ?: 0) + 1
        }
        val maxFreq = freqMap.values.maxOrNull() ?: 1
        val mode = if (maxFreq > 1) {
            freqMap.filter { it.value == maxFreq }.keys.sorted()
        } else {
            emptyList()
        }

        val min = sorted.first()
        val max = sorted.last()
        val range = max - min

        // Variance & SD
        var sumSqDiff = 0.0
        for (v in sorted) {
            val diff = v - mean
            sumSqDiff += diff * diff
        }

        val popVar = sumSqDiff / n
        val sampleVar = if (n > 1) sumSqDiff / (n - 1) else 0.0

        val popSd = sqrt(popVar)
        val sampleSd = sqrt(sampleVar)

        // Quartiles
        val q1 = percentile(sorted, 25.0)
        val q3 = percentile(sorted, 75.0)
        val iqr = q3 - q1

        return DescriptiveStatistics(
            count = n,
            sum = sum,
            mean = mean,
            median = median,
            mode = mode,
            min = min,
            max = max,
            range = range,
            sampleVariance = sampleVar,
            populationVariance = popVar,
            sampleStandardDeviation = sampleSd,
            populationStandardDeviation = popSd,
            q1 = q1,
            q3 = q3,
            iqr = iqr
        )
    }

    private fun percentile(sorted: List<Double>, p: Double): Double {
        if (sorted.isEmpty()) return 0.0
        if (sorted.size == 1) return sorted[0]
        val pos = (p / 100.0) * (sorted.size - 1)
        val low = pos.toInt()
        val frac = pos - low
        return if (low + 1 < sorted.size) {
            sorted[low] + frac * (sorted[low + 1] - sorted[low])
        } else {
            sorted[low]
        }
    }

    fun weightedMean(values: List<Double>, weights: List<Double>): Double {
        require(values.size == weights.size && values.isNotEmpty()) { "Values and weights must have identical non-zero size" }
        var sumW = 0.0
        var sumVW = 0.0
        for (i in values.indices) {
            sumVW += values[i] * weights[i]
            sumW += weights[i]
        }
        if (sumW == 0.0) throw ArithmeticException("Sum of weights cannot be zero")
        return sumVW / sumW
    }

    fun linearRegression(x: List<Double>, y: List<Double>): LinearRegressionResult {
        require(x.size == y.size && x.size >= 2) { "At least two coordinate pairs (x, y) are required" }
        val n = x.size
        val sumX = x.sum()
        val sumY = y.sum()
        val sumXY = x.indices.sumOf { x[it] * y[it] }
        val sumX2 = x.sumOf { it * it }
        val sumY2 = y.sumOf { it * it }

        val denomM = (n * sumX2 - sumX * sumX)
        if (abs(denomM) < 1e-12) {
            throw ArithmeticException("Vertical line or undefined slope in regression")
        }

        val slope = (n * sumXY - sumX * sumY) / denomM
        val intercept = (sumY - slope * sumX) / n

        // Pearson correlation r
        val numR = (n * sumXY - sumX * sumY)
        val denomR = sqrt((n * sumX2 - sumX * sumX) * (n * sumY2 - sumY * sumY))
        val r = if (abs(denomR) > 1e-12) numR / denomR else 0.0
        val rSquared = r * r

        val sign = if (intercept >= 0) "+ ${roundVal(intercept)}" else "- ${roundVal(abs(intercept))}"
        val eq = "y = ${roundVal(slope)}x $sign"

        return LinearRegressionResult(slope, intercept, r, rSquared, eq)
    }

    // Probability distributions
    fun binomialProbability(n: Int, k: Int, p: Double): Double {
        require(n >= 0 && k in 0..n && p in 0.0..1.0) { "Invalid binomial parameters: n>=0, 0<=k<=n, 0<=p<=1" }
        val nCr = nCr(n, k)
        return nCr * p.pow(k) * (1.0 - p).pow(n - k)
    }

    fun normalPdf(x: Double, mean: Double = 0.0, sd: Double = 1.0): Double {
        require(sd > 0.0) { "Standard deviation must be positive" }
        val z = (x - mean) / sd
        return (1.0 / (sd * sqrt(2 * Math.PI))) * exp(-0.5 * z * z)
    }

    // Abramowitz and Stegun approximation for standard normal CDF Φ(z)
    fun normalCdf(x: Double, mean: Double = 0.0, sd: Double = 1.0): Double {
        require(sd > 0.0) { "Standard deviation must be positive" }
        val z = (x - mean) / sd
        val t = 1.0 / (1.0 + 0.2316419 * abs(z))
        val poly = t * (0.319381530 + t * (-0.356563782 + t * (1.781477937 + t * (-1.821255978 + t * 1.330274429))))
        val cdf = 1.0 - (1.0 / sqrt(2 * Math.PI)) * exp(-0.5 * z * z) * poly
        return if (z >= 0) cdf else 1.0 - cdf
    }

    private fun nCr(n: Int, r: Int): Double {
        var k = r
        if (k > n - k) k = n - k
        var res = 1.0
        for (i in 1..k) {
            res = res * (n - i + 1) / i
        }
        return res
    }

    private fun roundVal(v: Double): String {
        return MathEngine.formatNumber(v, NumberNotation.STANDARD, 6)
    }
}
