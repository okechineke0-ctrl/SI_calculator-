package com.example

import com.example.math.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class MathEngineTest {

    private val engine = MathEngine()

    @Test
    fun testBasicArithmetic() {
        val r1 = engine.evaluate("2 + 3 * 4")
        assertTrue(r1.isSuccess)
        assertEquals(14.0, r1.value, 1e-9)

        val r2 = engine.evaluate("(2 + 3) * 4")
        assertTrue(r2.isSuccess)
        assertEquals(20.0, r2.value, 1e-9)

        val r3 = engine.evaluate("10 / 2 + 5 * 2")
        assertTrue(r3.isSuccess)
        assertEquals(15.0, r3.value, 1e-9)
    }

    @Test
    fun testPowersAndRoots() {
        val r1 = engine.evaluate("2^3")
        assertEquals(8.0, r1.value, 1e-9)

        val r2 = engine.evaluate("sqrt(16)")
        assertEquals(4.0, r2.value, 1e-9)

        val r3 = engine.evaluate("cbrt(27)")
        assertEquals(3.0, r3.value, 1e-9)

        val r4 = engine.evaluate("root(4, 16)")
        assertEquals(2.0, r4.value, 1e-9)
    }

    @Test
    fun testTrigonometryAndAngleModes() {
        engine.angleMode = AngleMode.DEG
        val sin30 = engine.evaluate("sin(30)")
        assertEquals(0.5, sin30.value, 1e-6)

        val cos60 = engine.evaluate("cos(60)")
        assertEquals(0.5, cos60.value, 1e-6)

        val tan45 = engine.evaluate("tan(45)")
        assertEquals(1.0, tan45.value, 1e-6)

        engine.angleMode = AngleMode.RAD
        val sinPiOver2 = engine.evaluate("sin(pi / 2)")
        assertEquals(1.0, sinPiOver2.value, 1e-6)
    }

    @Test
    fun testLogarithmsAndExponentials() {
        val lnE = engine.evaluate("ln(e)")
        assertEquals(1.0, lnE.value, 1e-6)

        val log100 = engine.evaluate("log(100)")
        assertEquals(2.0, log100.value, 1e-6)

        val log2Of8 = engine.evaluate("log2(8)")
        assertEquals(3.0, log2Of8.value, 1e-6)

        val exp1 = engine.evaluate("exp(1)")
        assertEquals(Math.E, exp1.value, 1e-6)
    }

    @Test
    fun testFactorialAndCombinatorics() {
        val f5 = engine.evaluate("5!")
        assertEquals(120.0, f5.value, 1e-9)

        val npr = engine.evaluate("5 nPr 2")
        assertEquals(20.0, npr.value, 1e-9)

        val ncr = engine.evaluate("5 nCr 2")
        assertEquals(10.0, ncr.value, 1e-9)
    }

    @Test
    fun testDivisionByZeroHandledGracefully() {
        val r = engine.evaluate("5 / 0")
        assertFalse(r.isSuccess)
        assertNotNull(r.error)
    }

    @Test
    fun testQuadraticSolver() {
        // x² - 5x + 6 = 0 => x = 2, 3
        val sol = AlgebraEngine.solveQuadratic(1.0, -5.0, 6.0)
        assertTrue(sol is EquationSolution.Quadratic)
        val quad = sol as EquationSolution.Quadratic
        assertEquals(3.0, quad.x1Real, 1e-6)
        assertEquals(2.0, quad.x2Real, 1e-6)
        assertEquals(0.0, quad.x1Imag, 1e-6)
    }

    @Test
    fun testComplexQuadraticRoots() {
        // x² + 1 = 0 => x = ±i
        val sol = AlgebraEngine.solveQuadratic(1.0, 0.0, 1.0)
        assertTrue(sol is EquationSolution.Quadratic)
        val quad = sol as EquationSolution.Quadratic
        assertEquals(0.0, quad.x1Real, 1e-6)
        assertEquals(1.0, quad.x1Imag, 1e-6)
    }

    @Test
    fun testLinearSystemSolver() {
        // 2x + 3y = 8
        // 5x - y = 3
        // Solution: x = 1, y = 2
        val sol = AlgebraEngine.solveSystem2x2(2.0, 3.0, 8.0, 5.0, -1.0, 3.0)
        assertTrue(sol is EquationSolution.System2x2)
        val sys = sol as EquationSolution.System2x2
        assertEquals(1.0, sys.x, 1e-6)
        assertEquals(2.0, sys.y, 1e-6)
    }

    @Test
    fun testCalculusNumericalDerivativeAndIntegral() {
        // f(x) = x^2, f'(3) = 6
        val d = CalculusEngine.derivative(3.0) { x -> x * x }
        assertEquals(6.0, d, 1e-4)

        // \int_0^3 x^2 dx = 9.0
        val integ = CalculusEngine.definiteIntegral(0.0, 3.0) { x -> x * x }
        assertEquals(9.0, integ, 1e-4)
    }

    @Test
    fun testMatrixOperations() {
        val a = MatrixEngine.create(2, 2) { r, c ->
            if (r == 0 && c == 0) 4.0
            else if (r == 0 && c == 1) 7.0
            else if (r == 1 && c == 0) 2.0
            else 6.0
        }
        // det = 4*6 - 7*2 = 24 - 14 = 10
        val det = MatrixEngine.determinant(a)
        assertEquals(10.0, det, 1e-9)

        val tr = MatrixEngine.trace(a)
        assertEquals(10.0, tr, 1e-9)

        val inv = MatrixEngine.inverse(a)
        assertNotNull(inv)
        assertEquals(0.6, inv!![0, 0], 1e-6)
        assertEquals(-0.7, inv[0, 1], 1e-6)
    }

    @Test
    fun testDescriptiveStatistics() {
        val data = listOf(2.0, 4.0, 4.0, 4.0, 5.0, 5.0, 7.0, 9.0)
        val stats = StatisticsEngine.calculateDescriptive(data)
        assertEquals(5.0, stats.mean, 1e-6)
        assertEquals(4.5, stats.median, 1e-6)
        assertEquals(listOf(4.0), stats.mode)
        assertEquals(2.0, stats.min, 1e-6)
        assertEquals(9.0, stats.max, 1e-6)
    }

    @Test
    fun testUnitConversion() {
        val cat = UnitConversionEngine.categories.first { it.id == "length" }
        val m = cat.units.first { it.symbol == "m" }
        val km = cat.units.first { it.symbol == "km" }
        val ft = cat.units.first { it.symbol == "ft" }

        val kmVal = UnitConversionEngine.convert(1000.0, m, km)
        assertEquals(1.0, kmVal, 1e-6)

        val ftVal = UnitConversionEngine.convert(1.0, m, ft)
        assertEquals(3.28084, ftVal, 1e-4)
    }

    @Test
    fun testPhysicsFormulas() {
        val vRes = PhysicsEngine.velocityFromAcceleration(5.0, 2.0, 3.0)
        assertEquals("11", vRes.answer)

        val fRes = PhysicsEngine.force(10.0, 3.0)
        assertEquals("30", fRes.answer)

        val rPar = PhysicsEngine.parallelResistance(10.0, 10.0)
        assertEquals("5", rPar.answer)
    }
}
