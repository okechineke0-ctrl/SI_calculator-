package com.example.math

import kotlin.math.*

data class GeometryResult(
    val shape: String,
    val outputs: Map<String, String>,
    val formulas: List<String>
)

object GeometryEngine {

    fun circle(radius: Double): GeometryResult {
        require(radius >= 0) { "Radius must be non-negative" }
        val area = Math.PI * radius * radius
        val circum = 2 * Math.PI * radius
        return GeometryResult(
            shape = "Circle",
            outputs = mapOf(
                "Area" to "${roundVal(area)} sq units",
                "Circumference" to "${roundVal(circum)} units",
                "Diameter" to "${roundVal(2 * radius)} units"
            ),
            formulas = listOf("Area = π r²", "Circumference = 2 π r", "Diameter = 2 r")
        )
    }

    fun triangle(a: Double, b: Double, c: Double): GeometryResult {
        require(a > 0 && b > 0 && c > 0) { "Side lengths must be positive" }
        require(a + b > c && a + c > b && b + c > a) { "Triangle Inequality violated: sum of any two sides must exceed the third" }
        val perimeter = a + b + c
        val s = perimeter / 2.0
        val area = sqrt(s * (s - a) * (s - b) * (s - c)) // Heron's formula
        return GeometryResult(
            shape = "Triangle",
            outputs = mapOf(
                "Area (Heron's)" to "${roundVal(area)} sq units",
                "Perimeter" to "${roundVal(perimeter)} units",
                "Semi-perimeter" to "${roundVal(s)} units"
            ),
            formulas = listOf("s = (a + b + c) / 2", "Area = √(s(s - a)(s - b)(s - c))", "Perimeter = a + b + c")
        )
    }

    fun rectangle(length: Double, width: Double): GeometryResult {
        require(length >= 0 && width >= 0) { "Dimensions must be non-negative" }
        val area = length * width
        val perim = 2 * (length + width)
        val diag = sqrt(length * length + width * width)
        return GeometryResult(
            shape = "Rectangle",
            outputs = mapOf(
                "Area" to "${roundVal(area)} sq units",
                "Perimeter" to "${roundVal(perim)} units",
                "Diagonal" to "${roundVal(diag)} units"
            ),
            formulas = listOf("Area = l × w", "Perimeter = 2(l + w)", "Diagonal = √(l² + w²)")
        )
    }

    fun sphere(radius: Double): GeometryResult {
        require(radius >= 0) { "Radius must be non-negative" }
        val volume = (4.0 / 3.0) * Math.PI * radius.pow(3)
        val surfaceArea = 4 * Math.PI * radius * radius
        return GeometryResult(
            shape = "Sphere",
            outputs = mapOf(
                "Volume" to "${roundVal(volume)} cubic units",
                "Surface Area" to "${roundVal(surfaceArea)} sq units"
            ),
            formulas = listOf("Volume = (4/3) π r³", "Surface Area = 4 π r²")
        )
    }

    fun cylinder(radius: Double, height: Double): GeometryResult {
        require(radius >= 0 && height >= 0) { "Radius and height must be non-negative" }
        val volume = Math.PI * radius * radius * height
        val lateralArea = 2 * Math.PI * radius * height
        val totalArea = lateralArea + 2 * Math.PI * radius * radius
        return GeometryResult(
            shape = "Cylinder",
            outputs = mapOf(
                "Volume" to "${roundVal(volume)} cubic units",
                "Lateral Surface Area" to "${roundVal(lateralArea)} sq units",
                "Total Surface Area" to "${roundVal(totalArea)} sq units"
            ),
            formulas = listOf("Volume = π r² h", "Lateral Area = 2 π r h", "Total Area = 2 π r h + 2 π r²")
        )
    }

    fun cone(radius: Double, height: Double): GeometryResult {
        require(radius >= 0 && height >= 0) { "Radius and height must be non-negative" }
        val slantHeight = sqrt(radius * radius + height * height)
        val volume = (1.0 / 3.0) * Math.PI * radius * radius * height
        val lateralArea = Math.PI * radius * slantHeight
        val totalArea = lateralArea + Math.PI * radius * radius
        return GeometryResult(
            shape = "Cone",
            outputs = mapOf(
                "Volume" to "${roundVal(volume)} cubic units",
                "Slant Height" to "${roundVal(slantHeight)} units",
                "Lateral Surface Area" to "${roundVal(lateralArea)} sq units",
                "Total Surface Area" to "${roundVal(totalArea)} sq units"
            ),
            formulas = listOf("Slant Height l = √(r² + h²)", "Volume = (1/3) π r² h", "Total Area = π r l + π r²")
        )
    }

    fun cuboid(l: Double, w: Double, h: Double): GeometryResult {
        require(l >= 0 && w >= 0 && h >= 0) { "Dimensions must be non-negative" }
        val volume = l * w * h
        val surfaceArea = 2 * (l * w + w * h + h * l)
        val diagonal = sqrt(l * l + w * w + h * h)
        return GeometryResult(
            shape = "Cuboid / Box",
            outputs = mapOf(
                "Volume" to "${roundVal(volume)} cubic units",
                "Surface Area" to "${roundVal(surfaceArea)} sq units",
                "Space Diagonal" to "${roundVal(diagonal)} units"
            ),
            formulas = listOf("Volume = l · w · h", "Surface Area = 2(lw + wh + hl)", "Diagonal = √(l² + w² + h²)")
        )
    }

    private fun roundVal(v: Double): String {
        return MathEngine.formatNumber(v, NumberNotation.STANDARD, 6)
    }
}
