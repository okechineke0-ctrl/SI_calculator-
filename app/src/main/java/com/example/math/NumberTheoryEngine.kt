package com.example.math

import kotlin.math.*

object NumberTheoryEngine {

    tailrec fun gcd(a: Long, b: Long): Long {
        val absA = abs(a)
        val absB = abs(b)
        return if (absB == 0L) absA else gcd(absB, absA % absB)
    }

    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a / gcd(a, b) * b)
    }

    fun isPrime(n: Long): Boolean {
        if (n <= 1L) return false
        if (n <= 3L) return true
        if (n % 2L == 0L || n % 3L == 0L) return false
        var i = 5L
        while (i * i <= n) {
            if (n % i == 0L || n % (i + 2L) == 0L) return false
            i += 6L
        }
        return true
    }

    fun primeFactors(n: Long): Map<Long, Int> {
        require(n > 0L) { "Prime factorization defined for positive integers" }
        val factors = mutableMapOf<Long, Int>()
        var d = 2L
        var temp = n
        while (d * d <= temp) {
            if (temp % d == 0L) {
                var count = 0
                while (temp % d == 0L) {
                    count++
                    temp /= d
                }
                factors[d] = count
            }
            d = if (d == 2L) 3L else d + 2L
        }
        if (temp > 1L) {
            factors[temp] = (factors[temp] ?: 0) + 1
        }
        return factors
    }

    fun primeFactorsString(n: Long): String {
        val map = primeFactors(n)
        if (map.isEmpty()) return "$n"
        return map.entries.joinToString(" × ") { (factor, power) ->
            if (power == 1) "$factor" else "$factor^$power"
        }
    }

    fun modPow(base: Long, exp: Long, mod: Long): Long {
        if (mod <= 0L) throw ArithmeticException("Modulus must be positive")
        var res = 1L
        var b = (base % mod + mod) % mod
        var e = exp
        while (e > 0L) {
            if (e % 2L == 1L) res = (res * b) % mod
            b = (b * b) % mod
            e /= 2L
        }
        return res
    }

    fun modInverse(a: Long, m: Long): Long? {
        // Extended Euclidean algorithm
        var m0 = m
        var y = 0L
        var x = 1L
        var a0 = (a % m + m) % m

        if (m == 1L) return 0L

        while (a0 > 1L) {
            if (m0 == 0L) return null
            val q = a0 / m0
            var t = m0
            m0 = a0 % m0
            a0 = t
            t = y
            y = x - q * y
            x = t
        }

        if (x < 0L) x += m
        return if (gcd(a, m) == 1L) x else null
    }
}
