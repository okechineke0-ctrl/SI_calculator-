package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.*
import com.example.ui.MainViewModel

enum class MathToolTab(val title: String) {
    EQUATIONS("Equations"),
    MATRICES("Matrices & Vectors"),
    STATISTICS("Statistics"),
    CALCULUS("Calculus & Series"),
    COMPLEX("Complex Numbers"),
    NUMBER_THEORY("Number Theory"),
    GEOMETRY("Geometry"),
    PHYSICS("Physics"),
    FINANCE("Finance"),
    UNIT_CONVERTER("Unit Converter")
}

@Composable
fun MathToolsScreen(viewModel: MainViewModel) {
    var selectedTool by remember { mutableStateOf(MathToolTab.EQUATIONS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable Tab Row for tools
        ScrollableTabRow(
            selectedTabIndex = selectedTool.ordinal,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            MathToolTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTool == tab,
                    onClick = { selectedTool = tab },
                    text = { Text(tab.title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                )
            }
        }

        // Active Tool Sub-screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            when (selectedTool) {
                MathToolTab.EQUATIONS -> EquationsTool()
                MathToolTab.MATRICES -> MatricesTool()
                MathToolTab.STATISTICS -> StatisticsTool()
                MathToolTab.CALCULUS -> CalculusTool()
                MathToolTab.COMPLEX -> ComplexTool()
                MathToolTab.NUMBER_THEORY -> NumberTheoryTool()
                MathToolTab.GEOMETRY -> GeometryTool()
                MathToolTab.PHYSICS -> PhysicsTool()
                MathToolTab.FINANCE -> FinanceTool()
                MathToolTab.UNIT_CONVERTER -> UnitConverterTool()
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Equations & Algebra Tool
// -------------------------------------------------------------
@Composable
fun EquationsTool() {
    var eqType by remember { mutableIntStateOf(0) } // 0: Quadratic, 1: Linear, 2: 2x2 System

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf("Quadratic", "Linear", "2x2 System").forEachIndexed { index, label ->
                SegmentedButton(
                    selected = eqType == index,
                    onClick = { eqType = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                ) {
                    Text(label, fontSize = 12.sp)
                }
            }
        }

        when (eqType) {
            0 -> {
                // Quadratic: ax² + bx + c = 0
                var aText by remember { mutableStateOf("1") }
                var bText by remember { mutableStateOf("-5") }
                var cText by remember { mutableStateOf("6") }
                var solution by remember { mutableStateOf<EquationSolution?>(null) }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Quadratic Equation: ax² + bx + c = 0", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = aText, onValueChange = { aText = it }, label = { Text("a") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = bText, onValueChange = { bText = it }, label = { Text("b") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = cText, onValueChange = { cText = it }, label = { Text("c") }, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = {
                                val a = aText.toDoubleOrNull() ?: 1.0
                                val b = bText.toDoubleOrNull() ?: 0.0
                                val c = cText.toDoubleOrNull() ?: 0.0
                                solution = AlgebraEngine.solveQuadratic(a, b, c)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Solve Quadratic Equation")
                        }
                    }
                }

                solution?.let { sol ->
                    SolutionDisplayCard(sol)
                }
            }
            1 -> {
                // Linear: ax + b = c
                var aText by remember { mutableStateOf("3") }
                var bText by remember { mutableStateOf("5") }
                var cText by remember { mutableStateOf("20") }
                var solution by remember { mutableStateOf<EquationSolution?>(null) }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Linear Equation: ax + b = c", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = aText, onValueChange = { aText = it }, label = { Text("a") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = bText, onValueChange = { bText = it }, label = { Text("b") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = cText, onValueChange = { cText = it }, label = { Text("c") }, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = {
                                val a = aText.toDoubleOrNull() ?: 1.0
                                val b = bText.toDoubleOrNull() ?: 0.0
                                val c = cText.toDoubleOrNull() ?: 0.0
                                solution = AlgebraEngine.solveLinear(a, b, c)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Solve Linear Equation")
                        }
                    }
                }

                solution?.let { sol ->
                    SolutionDisplayCard(sol)
                }
            }
            2 -> {
                // 2x2 System
                var a1 by remember { mutableStateOf("2") }; var b1 by remember { mutableStateOf("3") }; var c1 by remember { mutableStateOf("8") }
                var a2 by remember { mutableStateOf("5") }; var b2 by remember { mutableStateOf("-1") }; var c2 by remember { mutableStateOf("3") }
                var solution by remember { mutableStateOf<EquationSolution?>(null) }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("System: a₁x + b₁y = c₁  &  a₂x + b₂y = c₂", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Equation 1:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = a1, onValueChange = { a1 = it }, label = { Text("a₁") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = b1, onValueChange = { b1 = it }, label = { Text("b₁") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = c1, onValueChange = { c1 = it }, label = { Text("c₁") }, modifier = Modifier.weight(1f))
                        }
                        Text("Equation 2:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = a2, onValueChange = { a2 = it }, label = { Text("a₂") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = b2, onValueChange = { b2 = it }, label = { Text("b₂") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = c2, onValueChange = { c2 = it }, label = { Text("c₂") }, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = {
                                solution = AlgebraEngine.solveSystem2x2(
                                    a1.toDoubleOrNull() ?: 1.0, b1.toDoubleOrNull() ?: 0.0, c1.toDoubleOrNull() ?: 0.0,
                                    a2.toDoubleOrNull() ?: 1.0, b2.toDoubleOrNull() ?: 0.0, c2.toDoubleOrNull() ?: 0.0
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Solve System")
                        }
                    }
                }

                solution?.let { sol ->
                    SolutionDisplayCard(sol)
                }
            }
        }
    }
}

@Composable
fun SolutionDisplayCard(sol: EquationSolution) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Solution & Steps", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            HorizontalDivider()
            when (sol) {
                is EquationSolution.Linear -> {
                    Text("Result: x = ${MathEngine.formatNumber(sol.x, NumberNotation.STANDARD, 6)}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    sol.steps.forEach { Text("• $it", fontSize = 13.sp) }
                }
                is EquationSolution.Quadratic -> {
                    if (sol.x1Imag == 0.0) {
                        Text("x₁ = ${MathEngine.formatNumber(sol.x1Real, NumberNotation.STANDARD, 6)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("x₂ = ${MathEngine.formatNumber(sol.x2Real, NumberNotation.STANDARD, 6)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    } else {
                        Text("x₁ = ${MathEngine.formatNumber(sol.x1Real, NumberNotation.STANDARD, 4)} + ${MathEngine.formatNumber(sol.x1Imag, NumberNotation.STANDARD, 4)}i", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("x₂ = ${MathEngine.formatNumber(sol.x2Real, NumberNotation.STANDARD, 4)} - ${MathEngine.formatNumber(sol.x1Imag, NumberNotation.STANDARD, 4)}i", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    sol.steps.forEach { Text("• $it", fontSize = 13.sp) }
                }
                is EquationSolution.System2x2 -> {
                    Text("x = ${MathEngine.formatNumber(sol.x, NumberNotation.STANDARD, 6)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("y = ${MathEngine.formatNumber(sol.y, NumberNotation.STANDARD, 6)}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    sol.steps.forEach { Text("• $it", fontSize = 13.sp) }
                }
                is EquationSolution.Error -> {
                    Text(sol.message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                }
                else -> {}
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Matrices & Vectors Tool
// -------------------------------------------------------------
@Composable
fun MatricesTool() {
    var mode by remember { mutableIntStateOf(0) } // 0: Matrix (2x2), 1: Vectors (3D)

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf("2x2 Matrix", "3D Vectors").forEachIndexed { index, label ->
                SegmentedButton(selected = mode == index, onClick = { mode = index }, shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)) {
                    Text(label, fontSize = 12.sp)
                }
            }
        }

        if (mode == 0) {
            var m00 by remember { mutableStateOf("4") }; var m01 by remember { mutableStateOf("7") }
            var m10 by remember { mutableStateOf("2") }; var m11 by remember { mutableStateOf("6") }
            var resultText by remember { mutableStateOf("") }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Matrix A [2×2]", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = m00, onValueChange = { m00 = it }, label = { Text("A[0,0]") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = m01, onValueChange = { m01 = it }, label = { Text("A[0,1]") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = m10, onValueChange = { m10 = it }, label = { Text("A[1,0]") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = m11, onValueChange = { m11 = it }, label = { Text("A[1,1]") }, modifier = Modifier.weight(1f))
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val mat = MatrixEngine.create(2, 2) { r, c ->
                                    if (r == 0 && c == 0) m00.toDoubleOrNull() ?: 0.0
                                    else if (r == 0 && c == 1) m01.toDoubleOrNull() ?: 0.0
                                    else if (r == 1 && c == 0) m10.toDoubleOrNull() ?: 0.0
                                    else m11.toDoubleOrNull() ?: 0.0
                                }
                                val det = MatrixEngine.determinant(mat)
                                val tr = MatrixEngine.trace(mat)
                                val inv = MatrixEngine.inverse(mat)
                                val eig = MatrixEngine.eigenvalues2x2(mat)
                                val invStr = if (inv != null) "\nInverse:\n[ ${MathEngine.formatNumber(inv[0,0], NumberNotation.STANDARD, 3)}  ${MathEngine.formatNumber(inv[0,1], NumberNotation.STANDARD, 3)} ]\n[ ${MathEngine.formatNumber(inv[1,0], NumberNotation.STANDARD, 3)}  ${MathEngine.formatNumber(inv[1,1], NumberNotation.STANDARD, 3)} ]"
                                else "\nInverse: Singular (Det = 0)"
                                val eigStr = if (eig != null) "\nEigenvalues: λ₁ = ${MathEngine.formatNumber(eig.first, NumberNotation.STANDARD, 3)}, λ₂ = ${MathEngine.formatNumber(eig.second, NumberNotation.STANDARD, 3)}" else "\nEigenvalues: Complex"
                                resultText = "Determinant = ${MathEngine.formatNumber(det, NumberNotation.STANDARD, 4)}\nTrace = $tr$invStr$eigStr"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Compute Determinant, Trace, Inverse & Eigenvalues")
                        }
                    }
                }
            }

            if (resultText.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(resultText, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }
        } else {
            // 3D Vectors
            var v1x by remember { mutableStateOf("1") }; var v1y by remember { mutableStateOf("2") }; var v1z by remember { mutableStateOf("3") }
            var v2x by remember { mutableStateOf("4") }; var v2y by remember { mutableStateOf("5") }; var v2z by remember { mutableStateOf("6") }
            var vecResult by remember { mutableStateOf("") }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Vector u (x, y, z)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = v1x, onValueChange = { v1x = it }, label = { Text("u_x") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = v1y, onValueChange = { v1y = it }, label = { Text("u_y") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = v1z, onValueChange = { v1z = it }, label = { Text("u_z") }, modifier = Modifier.weight(1f))
                    }
                    Text("Vector v (x, y, z)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = v2x, onValueChange = { v2x = it }, label = { Text("v_x") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = v2y, onValueChange = { v2y = it }, label = { Text("v_y") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = v2z, onValueChange = { v2z = it }, label = { Text("v_z") }, modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            val u = Vector3D(v1x.toDoubleOrNull() ?: 0.0, v1y.toDoubleOrNull() ?: 0.0, v1z.toDoubleOrNull() ?: 0.0)
                            val v = Vector3D(v2x.toDoubleOrNull() ?: 0.0, v2y.toDoubleOrNull() ?: 0.0, v2z.toDoubleOrNull() ?: 0.0)
                            val dot = u dot v
                            val cross = u cross v
                            val angle = u.angleWith(v, true)
                            val proj = u.projectOnto(v)
                            vecResult = "Dot Product (u · v) = ${MathEngine.formatNumber(dot, NumberNotation.STANDARD, 4)}\n" +
                                    "Cross Product (u × v) = (${MathEngine.formatNumber(cross.x, NumberNotation.STANDARD, 3)}, ${MathEngine.formatNumber(cross.y, NumberNotation.STANDARD, 3)}, ${MathEngine.formatNumber(cross.z, NumberNotation.STANDARD, 3)})\n" +
                                    "Angle Between = ${MathEngine.formatNumber(angle, NumberNotation.STANDARD, 2)}°\n" +
                                    "|u| = ${MathEngine.formatNumber(u.magnitude, NumberNotation.STANDARD, 4)},  |v| = ${MathEngine.formatNumber(v.magnitude, NumberNotation.STANDARD, 4)}\n" +
                                    "Projection of u onto v = (${MathEngine.formatNumber(proj.x, NumberNotation.STANDARD, 3)}, ${MathEngine.formatNumber(proj.y, NumberNotation.STANDARD, 3)}, ${MathEngine.formatNumber(proj.z, NumberNotation.STANDARD, 3)})"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calculate Dot, Cross, Angle & Projection")
                    }
                }
            }

            if (vecResult.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(vecResult, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Statistics & Probability Tool
// -------------------------------------------------------------
@Composable
fun StatisticsTool() {
    var rawDataText by remember { mutableStateOf("12, 15, 18, 22, 25, 25, 29, 32, 38") }
    var statsResult by remember { mutableStateOf<DescriptiveStatistics?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Descriptive Statistics", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                OutlinedTextField(
                    value = rawDataText,
                    onValueChange = { rawDataText = it },
                    label = { Text("Comma-separated values") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val numbers = rawDataText.split(",", " ")
                            .mapNotNull { it.trim().toDoubleOrNull() }
                        if (numbers.isNotEmpty()) {
                            statsResult = StatisticsEngine.calculateDescriptive(numbers)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calculate Mean, SD, Quartiles & Variance")
                }
            }
        }

        statsResult?.let { s ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Summary Statistics", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("• Count (n): ${s.count}")
                    Text("• Sum: ${MathEngine.formatNumber(s.sum, NumberNotation.STANDARD, 4)}")
                    Text("• Mean: ${MathEngine.formatNumber(s.mean, NumberNotation.STANDARD, 4)}", fontWeight = FontWeight.Bold)
                    Text("• Median: ${MathEngine.formatNumber(s.median, NumberNotation.STANDARD, 4)}")
                    Text("• Mode: ${if (s.mode.isEmpty()) "None" else s.mode.joinToString(", ")}")
                    Text("• Min / Max: ${s.min} / ${s.max} (Range: ${s.range})")
                    Text("• Sample Std Dev (s): ${MathEngine.formatNumber(s.sampleStandardDeviation, NumberNotation.STANDARD, 4)}")
                    Text("• Population Std Dev (σ): ${MathEngine.formatNumber(s.populationStandardDeviation, NumberNotation.STANDARD, 4)}")
                    Text("• Sample Variance: ${MathEngine.formatNumber(s.sampleVariance, NumberNotation.STANDARD, 4)}")
                    Text("• Quartiles: Q1 = ${s.q1}, Q3 = ${s.q3} (IQR = ${s.iqr})")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Calculus & Series Tool
// -------------------------------------------------------------
@Composable
fun CalculusTool() {
    var mode by remember { mutableIntStateOf(0) } // 0: Derivative, 1: Definite Integral

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf("Derivative f'(x)", "Definite Integral ∫").forEachIndexed { index, label ->
                SegmentedButton(selected = mode == index, onClick = { mode = index }, shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)) {
                    Text(label, fontSize = 12.sp)
                }
            }
        }

        val evaluator = remember { MathEngine(angleMode = AngleMode.RAD) }

        if (mode == 0) {
            var expr by remember { mutableStateOf("x^3 - 3*x") }
            var x0Text by remember { mutableStateOf("2") }
            var derivResult by remember { mutableStateOf("") }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Numerical Differentiation", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = expr, onValueChange = { expr = it }, label = { Text("f(x)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = x0Text, onValueChange = { x0Text = it }, label = { Text("At point x₀") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            val x0 = x0Text.toDoubleOrNull() ?: 0.0
                            val f: (Double) -> Double = { x -> evaluator.evaluate(expr.replace("x", "($x)")).value }
                            val d1 = CalculusEngine.derivative(x0, f = f)
                            val d2 = CalculusEngine.secondDerivative(x0, f = f)
                            val tangent = CalculusEngine.tangentLineAt(x0, f = f)
                            derivResult = "f'($x0) = ${MathEngine.formatNumber(d1, NumberNotation.STANDARD, 6)}\n" +
                                    "f''($x0) = ${MathEngine.formatNumber(d2, NumberNotation.STANDARD, 6)}\n" +
                                    "Tangent Line: ${tangent.equationString}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calculate f'(x) & Tangent Line")
                    }
                }
            }

            if (derivResult.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(derivResult, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                }
            }
        } else {
            var expr by remember { mutableStateOf("x^2") }
            var aText by remember { mutableStateOf("0") }
            var bText by remember { mutableStateOf("3") }
            var integralResult by remember { mutableStateOf("") }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Definite Integral ∫ [a to b] f(x) dx", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(value = expr, onValueChange = { expr = it }, label = { Text("f(x)") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = aText, onValueChange = { aText = it }, label = { Text("Lower bound (a)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = bText, onValueChange = { bText = it }, label = { Text("Upper bound (b)") }, modifier = Modifier.weight(1f))
                    }
                    Button(
                        onClick = {
                            val a = aText.toDoubleOrNull() ?: 0.0
                            val b = bText.toDoubleOrNull() ?: 1.0
                            val f: (Double) -> Double = { x -> evaluator.evaluate(expr.replace("x", "($x)")).value }
                            val res = CalculusEngine.definiteIntegral(a, b, f = f)
                            integralResult = "∫_{$a}^{$b} ($expr) dx ≈ ${MathEngine.formatNumber(res, NumberNotation.STANDARD, 6)}"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Evaluate Simpson's 1/3 Integral")
                    }
                }
            }

            if (integralResult.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(integralResult, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. Complex Numbers Tool
// -------------------------------------------------------------
@Composable
fun ComplexTool() {
    var r1 by remember { mutableStateOf("3") }; var i1 by remember { mutableStateOf("4") }
    var r2 by remember { mutableStateOf("1") }; var i2 by remember { mutableStateOf("-2") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Complex Numbers z₁ and z₂", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("z₁ = a₁ + b₁i:", fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = r1, onValueChange = { r1 = it }, label = { Text("a₁ (Real)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = i1, onValueChange = { i1 = it }, label = { Text("b₁ (Imag)") }, modifier = Modifier.weight(1f))
                }
                Text("z₂ = a₂ + b₂i:", fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = r2, onValueChange = { r2 = it }, label = { Text("a₂ (Real)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = i2, onValueChange = { i2 = it }, label = { Text("b₂ (Imag)") }, modifier = Modifier.weight(1f))
                }
                Button(
                    onClick = {
                        val z1 = ComplexNumber(r1.toDoubleOrNull() ?: 0.0, i1.toDoubleOrNull() ?: 0.0)
                        val z2 = ComplexNumber(r2.toDoubleOrNull() ?: 0.0, i2.toDoubleOrNull() ?: 0.0)
                        val add = z1 + z2
                        val sub = z1 - z2
                        val mul = z1 * z2
                        val div = try { z1 / z2 } catch (e: Exception) { null }
                        resultText = "z₁ + z₂ = ${add.toRectangularString()}\n" +
                                "z₁ - z₂ = ${sub.toRectangularString()}\n" +
                                "z₁ × z₂ = ${mul.toRectangularString()}\n" +
                                "z₁ ÷ z₂ = ${div?.toRectangularString() ?: "Undefined (div by zero)"}\n\n" +
                                "Polar Form z₁: ${z1.toPolarString()}\n" +
                                "Euler Form z₁: ${z1.toEulerString()}"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Compute Arithmetic, Polar & Euler")
                }
            }
        }

        if (resultText.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(resultText, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// 6. Number Theory Tool
// -------------------------------------------------------------
@Composable
fun NumberTheoryTool() {
    var aText by remember { mutableStateOf("48") }
    var bText by remember { mutableStateOf("180") }
    var resultText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("GCD, LCM & Prime Factorization", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = aText, onValueChange = { aText = it }, label = { Text("Integer A") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = bText, onValueChange = { bText = it }, label = { Text("Integer B") }, modifier = Modifier.weight(1f))
                }
                Button(
                    onClick = {
                        val a = aText.toLongOrNull() ?: 1L
                        val b = bText.toLongOrNull() ?: 1L
                        val gcdVal = NumberTheoryEngine.gcd(a, b)
                        val lcmVal = NumberTheoryEngine.lcm(a, b)
                        val aPrime = NumberTheoryEngine.isPrime(a)
                        val bPrime = NumberTheoryEngine.isPrime(b)
                        val aFactors = NumberTheoryEngine.primeFactorsString(a)
                        val bFactors = NumberTheoryEngine.primeFactorsString(b)
                        resultText = "GCD($a, $b) = $gcdVal\n" +
                                "LCM($a, $b) = $lcmVal\n\n" +
                                "$a Prime Check: ${if (aPrime) "Prime" else "Composite"}\n" +
                                "$a Prime Factors: $aFactors\n\n" +
                                "$b Prime Check: ${if (bPrime) "Prime" else "Composite"}\n" +
                                "$b Prime Factors: $bFactors"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Compute GCD, LCM & Factorize")
                }
            }
        }

        if (resultText.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(resultText, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// 7. Geometry Tool
// -------------------------------------------------------------
@Composable
fun GeometryTool() {
    var shapeIndex by remember { mutableIntStateOf(0) }
    val shapes = listOf("Circle", "Triangle", "Rectangle", "Sphere", "Cylinder", "Cone")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScrollableTabRow(selectedTabIndex = shapeIndex, containerColor = MaterialTheme.colorScheme.surface) {
            shapes.forEachIndexed { i, s ->
                Tab(selected = shapeIndex == i, onClick = { shapeIndex = i }, text = { Text(s, fontSize = 12.sp) })
            }
        }

        var param1 by remember { mutableStateOf("5") }
        var param2 by remember { mutableStateOf("8") }
        var param3 by remember { mutableStateOf("10") }
        var geoResult by remember { mutableStateOf<GeometryResult?>(null) }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (shapeIndex) {
                    0 -> OutlinedTextField(value = param1, onValueChange = { param1 = it }, label = { Text("Radius r") }, modifier = Modifier.fillMaxWidth())
                    1 -> {
                        OutlinedTextField(value = param1, onValueChange = { param1 = it }, label = { Text("Side a") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = param2, onValueChange = { param2 = it }, label = { Text("Side b") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = param3, onValueChange = { param3 = it }, label = { Text("Side c") }, modifier = Modifier.fillMaxWidth())
                    }
                    2 -> {
                        OutlinedTextField(value = param1, onValueChange = { param1 = it }, label = { Text("Length l") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = param2, onValueChange = { param2 = it }, label = { Text("Width w") }, modifier = Modifier.fillMaxWidth())
                    }
                    3 -> OutlinedTextField(value = param1, onValueChange = { param1 = it }, label = { Text("Radius r") }, modifier = Modifier.fillMaxWidth())
                    4, 5 -> {
                        OutlinedTextField(value = param1, onValueChange = { param1 = it }, label = { Text("Radius r") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = param2, onValueChange = { param2 = it }, label = { Text("Height h") }, modifier = Modifier.fillMaxWidth())
                    }
                }

                Button(
                    onClick = {
                        val p1 = param1.toDoubleOrNull() ?: 0.0
                        val p2 = param2.toDoubleOrNull() ?: 0.0
                        val p3 = param3.toDoubleOrNull() ?: 0.0
                        geoResult = when (shapeIndex) {
                            0 -> GeometryEngine.circle(p1)
                            1 -> try { GeometryEngine.triangle(p1, p2, p3) } catch (e: Exception) { null }
                            2 -> GeometryEngine.rectangle(p1, p2)
                            3 -> GeometryEngine.sphere(p1)
                            4 -> GeometryEngine.cylinder(p1, p2)
                            5 -> GeometryEngine.cone(p1, p2)
                            else -> null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calculate Area, Volume & Perimeter")
                }
            }
        }

        geoResult?.let { res ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${res.shape} Outputs:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    res.outputs.forEach { (k, v) ->
                        Text("• $k: $v", fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Formulas:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    res.formulas.forEach { Text("  $it", fontSize = 12.sp) }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 8. Physics Tool
// -------------------------------------------------------------
@Composable
fun PhysicsTool() {
    var formulaIndex by remember { mutableIntStateOf(0) }
    val formulas = listOf("Kinematics (v=u+at)", "Force (F=ma)", "Kinetic Energy", "Ohm's Law (V=IR)", "Parallel Resistors")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScrollableTabRow(selectedTabIndex = formulaIndex, containerColor = MaterialTheme.colorScheme.surface) {
            formulas.forEachIndexed { i, f ->
                Tab(selected = formulaIndex == i, onClick = { formulaIndex = i }, text = { Text(f, fontSize = 12.sp) })
            }
        }

        var in1 by remember { mutableStateOf("10") }
        var in2 by remember { mutableStateOf("2") }
        var in3 by remember { mutableStateOf("5") }
        var physResult by remember { mutableStateOf<PhysicsCalculationResult?>(null) }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                when (formulaIndex) {
                    0 -> {
                        OutlinedTextField(value = in1, onValueChange = { in1 = it }, label = { Text("Initial velocity u (m/s)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = in2, onValueChange = { in2 = it }, label = { Text("Acceleration a (m/s²)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = in3, onValueChange = { in3 = it }, label = { Text("Time t (s)") }, modifier = Modifier.fillMaxWidth())
                    }
                    1 -> {
                        OutlinedTextField(value = in1, onValueChange = { in1 = it }, label = { Text("Mass m (kg)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = in2, onValueChange = { in2 = it }, label = { Text("Acceleration a (m/s²)") }, modifier = Modifier.fillMaxWidth())
                    }
                    2 -> {
                        OutlinedTextField(value = in1, onValueChange = { in1 = it }, label = { Text("Mass m (kg)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = in2, onValueChange = { in2 = it }, label = { Text("Velocity v (m/s)") }, modifier = Modifier.fillMaxWidth())
                    }
                    3 -> {
                        OutlinedTextField(value = in1, onValueChange = { in1 = it }, label = { Text("Current I (A)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = in2, onValueChange = { in2 = it }, label = { Text("Resistance R (Ω)") }, modifier = Modifier.fillMaxWidth())
                    }
                    4 -> {
                        OutlinedTextField(value = in1, onValueChange = { in1 = it }, label = { Text("Resistor R1 (Ω)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = in2, onValueChange = { in2 = it }, label = { Text("Resistor R2 (Ω)") }, modifier = Modifier.fillMaxWidth())
                    }
                }

                Button(
                    onClick = {
                        val v1 = in1.toDoubleOrNull() ?: 0.0
                        val v2 = in2.toDoubleOrNull() ?: 0.0
                        val v3 = in3.toDoubleOrNull() ?: 0.0
                        physResult = when (formulaIndex) {
                            0 -> PhysicsEngine.velocityFromAcceleration(v1, v2, v3)
                            1 -> PhysicsEngine.force(v1, v2)
                            2 -> PhysicsEngine.kineticEnergy(v1, v2)
                            3 -> PhysicsEngine.voltage(v1, v2)
                            4 -> PhysicsEngine.parallelResistance(v1, v2)
                            else -> null
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calculate Physics")
                }
            }
        }

        physResult?.let { res ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(res.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Formula: ${res.formula}", fontWeight = FontWeight.SemiBold)
                    Text("Substitution: ${res.substitution}")
                    Text("Answer: ${res.answer} ${res.unit}", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 9. Finance Tool
// -------------------------------------------------------------
@Composable
fun FinanceTool() {
    var mode by remember { mutableIntStateOf(0) } // 0: Compound Interest, 1: Loan EMI

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            listOf("Compound Interest", "Loan EMI").forEachIndexed { index, label ->
                SegmentedButton(selected = mode == index, onClick = { mode = index }, shape = SegmentedButtonDefaults.itemShape(index = index, count = 2)) {
                    Text(label, fontSize = 12.sp)
                }
            }
        }

        var pText by remember { mutableStateOf("10000") }
        var rText by remember { mutableStateOf("6.5") }
        var tText by remember { mutableStateOf("3") }
        var finResult by remember { mutableStateOf<FinanceResult?>(null) }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = pText, onValueChange = { pText = it }, label = { Text("Principal Amount ($)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rText, onValueChange = { rText = it }, label = { Text("Annual Rate (%)") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = tText, onValueChange = { tText = it }, label = { Text("Duration (Years)") }, modifier = Modifier.fillMaxWidth())

                Button(
                    onClick = {
                        val p = pText.toDoubleOrNull() ?: 1000.0
                        val r = rText.toDoubleOrNull() ?: 5.0
                        val t = tText.toDoubleOrNull() ?: 1.0
                        finResult = if (mode == 0) FinanceEngine.compoundInterest(p, r, t)
                        else FinanceEngine.loanEmi(p, r, t)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calculate Financial Breakdown")
                }
            }
        }

        finResult?.let { res ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(res.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    res.outputs.forEach { (k, v) ->
                        Text("• $k: $v", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                    Text(res.explanation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 10. Unit Converter Tool
// -------------------------------------------------------------
@Composable
fun UnitConverterTool() {
    val categories = UnitConversionEngine.categories
    var selectedCatIndex by remember { mutableIntStateOf(0) }
    val currentCat = categories[selectedCatIndex]

    var fromUnitIndex by remember { mutableIntStateOf(0) }
    var toUnitIndex by remember { mutableIntStateOf(if (currentCat.units.size > 1) 1 else 0) }
    var inputValue by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScrollableTabRow(selectedTabIndex = selectedCatIndex, containerColor = MaterialTheme.colorScheme.surface) {
            categories.forEachIndexed { i, cat ->
                Tab(
                    selected = selectedCatIndex == i,
                    onClick = {
                        selectedCatIndex = i
                        fromUnitIndex = 0
                        toUnitIndex = if (cat.units.size > 1) 1 else 0
                    },
                    text = { Text(cat.name, fontSize = 12.sp) }
                )
            }
        }

        val fromUnit = currentCat.units[fromUnitIndex.coerceIn(currentCat.units.indices)]
        val toUnit = currentCat.units[toUnitIndex.coerceIn(currentCat.units.indices)]

        val inVal = inputValue.toDoubleOrNull() ?: 0.0
        val outVal = UnitConversionEngine.convert(inVal, fromUnit, toUnit)
        val formattedOut = MathEngine.formatNumber(outVal, NumberNotation.STANDARD, 6)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it },
                    label = { Text("Input Value") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("From Unit:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        currentCat.units.forEachIndexed { idx, u ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(selected = fromUnitIndex == idx, onClick = { fromUnitIndex = idx })
                                Text("${u.name} (${u.symbol})", fontSize = 12.sp)
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("To Unit:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        currentCat.units.forEachIndexed { idx, u ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(selected = toUnitIndex == idx, onClick = { toUnitIndex = idx })
                                Text("${u.name} (${u.symbol})", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Result Card
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Converted Result", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    text = "$inputValue ${fromUnit.symbol} = $formattedOut ${toUnit.symbol}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
