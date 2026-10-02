package com.example.math

import kotlin.math.*

data class Matrix(
    val rows: Int,
    val cols: Int,
    val data: Array<DoubleArray>
) {
    operator fun get(r: Int, c: Int): Double = data[r][c]
    operator fun set(r: Int, c: Int, v: Double) { data[r][c] = v }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix) return false
        if (rows != other.rows || cols != other.cols) return false
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (abs(data[r][c] - other.data[r][c]) > 1e-9) return false
            }
        }
        return true
    }

    override fun hashCode(): Int = 31 * rows + cols
}

data class Vector3D(val x: Double, val y: Double, val z: Double = 0.0) {
    val magnitude: Double get() = sqrt(x * x + y * y + z * z)

    fun unit(): Vector3D {
        val m = magnitude
        return if (m > 1e-12) Vector3D(x / m, y / m, z / m) else Vector3D(0.0, 0.0, 0.0)
    }

    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3D(x * scalar, y * scalar, z * scalar)

    infix fun dot(other: Vector3D): Double = x * other.x + y * other.y + z * other.z

    infix fun cross(other: Vector3D): Vector3D = Vector3D(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun angleWith(other: Vector3D, inDegrees: Boolean = true): Double {
        val dotProd = this dot other
        val mags = this.magnitude * other.magnitude
        if (mags < 1e-12) return 0.0
        val cosTheta = (dotProd / mags).coerceIn(-1.0, 1.0)
        val rad = acos(cosTheta)
        return if (inDegrees) Math.toDegrees(rad) else rad
    }

    fun projectOnto(other: Vector3D): Vector3D {
        val otherMagSq = other.magnitude * other.magnitude
        if (otherMagSq < 1e-12) return Vector3D(0.0, 0.0, 0.0)
        val scalar = (this dot other) / otherMagSq
        return other * scalar
    }
}

object MatrixEngine {

    fun create(rows: Int, cols: Int, init: (r: Int, c: Int) -> Double = { _, _ -> 0.0 }): Matrix {
        val arr = Array(rows) { r -> DoubleArray(cols) { c -> init(r, c) } }
        return Matrix(rows, cols, arr)
    }

    fun add(a: Matrix, b: Matrix): Matrix {
        require(a.rows == b.rows && a.cols == b.cols) { "Matrix dimensions must match for addition" }
        return create(a.rows, a.cols) { r, c -> a[r, c] + b[r, c] }
    }

    fun subtract(a: Matrix, b: Matrix): Matrix {
        require(a.rows == b.rows && a.cols == b.cols) { "Matrix dimensions must match for subtraction" }
        return create(a.rows, a.cols) { r, c -> a[r, c] - b[r, c] }
    }

    fun multiply(a: Matrix, b: Matrix): Matrix {
        require(a.cols == b.rows) { "Matrix A columns (${a.cols}) must match Matrix B rows (${b.rows})" }
        val res = create(a.rows, b.cols)
        for (i in 0 until a.rows) {
            for (j in 0 until b.cols) {
                var sum = 0.0
                for (k in 0 until a.cols) {
                    sum += a[i, k] * b[k, j]
                }
                res[i, j] = sum
            }
        }
        return res
    }

    fun scalarMultiply(a: Matrix, s: Double): Matrix {
        return create(a.rows, a.cols) { r, c -> a[r, c] * s }
    }

    fun transpose(a: Matrix): Matrix {
        return create(a.cols, a.rows) { r, c -> a[c, r] }
    }

    fun trace(a: Matrix): Double {
        require(a.rows == a.cols) { "Trace is only defined for square matrices" }
        var sum = 0.0
        for (i in 0 until a.rows) sum += a[i, i]
        return sum
    }

    fun determinant(a: Matrix): Double {
        require(a.rows == a.cols) { "Determinant is only defined for square matrices" }
        val n = a.rows
        if (n == 1) return a[0, 0]
        if (n == 2) return a[0, 0] * a[1, 1] - a[0, 1] * a[1, 0]
        if (n == 3) {
            return a[0, 0] * (a[1, 1] * a[2, 2] - a[1, 2] * a[2, 1]) -
                    a[0, 1] * (a[1, 0] * a[2, 2] - a[1, 2] * a[2, 0]) +
                    a[0, 2] * (a[1, 0] * a[2, 1] - a[1, 1] * a[2, 0])
        }

        // Gaussian elimination for NxN determinant
        val copy = Array(n) { r -> a.data[r].clone() }
        var det = 1.0
        var sign = 1.0

        for (i in 0 until n) {
            var pivot = i
            for (j in i + 1 until n) {
                if (abs(copy[j][i]) > abs(copy[pivot][i])) pivot = j
            }
            if (abs(copy[pivot][i]) < 1e-12) return 0.0

            if (pivot != i) {
                val temp = copy[i]
                copy[i] = copy[pivot]
                copy[pivot] = temp
                sign = -sign
            }

            det *= copy[i][i]

            for (j in i + 1 until n) {
                val factor = copy[j][i] / copy[i][i]
                for (k in i + 1 until n) {
                    copy[j][k] -= factor * copy[i][k]
                }
            }
        }

        return det * sign
    }

    fun inverse(a: Matrix): Matrix? {
        require(a.rows == a.cols) { "Inverse only exists for square matrices" }
        val n = a.rows
        val det = determinant(a)
        if (abs(det) < 1e-12) return null // Singular

        // Augmented matrix [A | I]
        val aug = Array(n) { r ->
            DoubleArray(2 * n) { c ->
                if (c < n) a[r, c] else if (c - n == r) 1.0 else 0.0
            }
        }

        // Jordan elimination
        for (i in 0 until n) {
            var maxRow = i
            for (k in i + 1 until n) {
                if (abs(aug[k][i]) > abs(aug[maxRow][i])) maxRow = k
            }
            val temp = aug[i]
            aug[i] = aug[maxRow]
            aug[maxRow] = temp

            val pivot = aug[i][i]
            if (abs(pivot) < 1e-12) return null

            for (j in 0 until 2 * n) {
                aug[i][j] /= pivot
            }

            for (k in 0 until n) {
                if (k != i) {
                    val factor = aug[k][i]
                    for (j in 0 until 2 * n) {
                        aug[k][j] -= factor * aug[i][j]
                    }
                }
            }
        }

        return create(n, n) { r, c -> aug[r][c + n] }
    }

    fun rref(a: Matrix): Pair<Matrix, Int> {
        val m = a.rows
        val n = a.cols
        val res = Array(m) { r -> a.data[r].clone() }
        var lead = 0
        var rank = 0

        for (r in 0 until m) {
            if (lead >= n) break
            var i = r
            while (abs(res[i][lead]) < 1e-12) {
                i++
                if (i == m) {
                    i = r
                    lead++
                    if (lead == n) break
                }
            }
            if (lead >= n) break

            val temp = res[i]
            res[i] = res[r]
            res[r] = temp

            val div = res[r][lead]
            if (abs(div) > 1e-12) {
                for (j in 0 until n) res[r][j] /= div
                rank++
            }

            for (k in 0 until m) {
                if (k != r) {
                    val factor = res[k][lead]
                    for (j in 0 until n) {
                        res[k][j] -= factor * res[r][j]
                    }
                }
            }
            lead++
        }

        return Pair(Matrix(m, n, res), rank)
    }

    fun eigenvalues2x2(a: Matrix): Pair<Double, Double>? {
        if (a.rows != 2 || a.cols != 2) return null
        val tr = trace(a)
        val det = determinant(a)
        val disc = tr * tr - 4 * det
        if (disc < 0) return null // Complex eigenvalues
        val lambda1 = (tr + sqrt(disc)) / 2.0
        val lambda2 = (tr - sqrt(disc)) / 2.0
        return Pair(lambda1, lambda2)
    }
}
