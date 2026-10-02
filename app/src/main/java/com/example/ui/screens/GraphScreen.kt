package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.AngleMode
import com.example.math.MathEngine
import com.example.ui.MainViewModel
import kotlin.math.*

@Composable
fun GraphScreen(viewModel: MainViewModel) {
    val functions by viewModel.graphFunctions.collectAsState()
    val activeIndex by viewModel.graphActiveIndex.collectAsState()

    var xMin by remember { mutableDoubleStateOf(-10.0) }
    var xMax by remember { mutableDoubleStateOf(10.0) }
    var yMin by remember { mutableDoubleStateOf(-10.0) }
    var yMax by remember { mutableDoubleStateOf(10.0) }

    var inspectedPoint by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var newEquationText by remember { mutableStateOf(functions.getOrNull(activeIndex) ?: "sin(x)") }

    val presetList = listOf("sin(x)", "cos(x)", "x^2 - 4", "0.5*x^3 - 2*x", "exp(x)", "ln(x)", "abs(x)")

    // Helper math evaluator for y = f(x)
    val mathEvaluator = remember { MathEngine(angleMode = AngleMode.RAD) }

    fun evalFunction(expr: String, x: Double): Double {
        return try {
            val sanitized = expr.replace("x", "($x)")
            val res = mathEvaluator.evaluate(sanitized)
            if (res.isSuccess) res.value else Double.NaN
        } catch (e: Exception) {
            Double.NaN
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Equation Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "y = ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    OutlinedTextField(
                        value = newEquationText,
                        onValueChange = {
                            newEquationText = it
                            viewModel.updateGraphFunction(activeIndex, it)
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("e.g. sin(x) or x^2 - 4") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }

                // Quick Presets Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetList.forEach { preset ->
                        FilterChip(
                            selected = (newEquationText == preset),
                            onClick = {
                                newEquationText = preset
                                viewModel.updateGraphFunction(activeIndex, preset)
                            },
                            label = { Text(preset, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Graph Interactive Canvas
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF07111E)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val xRange = xMax - xMin
                                val yRange = yMax - yMin

                                // Pan
                                val dx = (pan.x / size.width) * xRange
                                val dy = (pan.y / size.height) * yRange
                                xMin -= dx
                                xMax -= dx
                                yMin += dy
                                yMax += dy

                                // Zoom
                                if (zoom != 1f) {
                                    val newXRange = xRange / zoom
                                    val newYRange = yRange / zoom
                                    val xCenter = (xMin + xMax) / 2.0
                                    val yCenter = (yMin + yMax) / 2.0
                                    xMin = xCenter - newXRange / 2.0
                                    xMax = xCenter + newXRange / 2.0
                                    yMin = yCenter - newYRange / 2.0
                                    yMax = yCenter + newYRange / 2.0
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                val px = change.position.x
                                val py = change.position.y
                                val mathX = xMin + (px / size.width) * (xMax - xMin)
                                val currentEq = functions.getOrNull(activeIndex) ?: "x"
                                val mathY = evalFunction(currentEq, mathX)
                                if (!mathY.isNaN()) {
                                    inspectedPoint = Pair(mathX, mathY)
                                }
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height

                    fun toScreenX(x: Double) = ((x - xMin) / (xMax - xMin) * w).toFloat()
                    fun toScreenY(y: Double) = (h - (y - yMin) / (yMax - yMin) * h).toFloat()

                    // Draw Grid
                    val gridColor = Color(0xFF1E2E42)
                    val xStep = max(1.0, 10.0.pow(floor(log10((xMax - xMin) / 8.0))))
                    val yStep = max(1.0, 10.0.pow(floor(log10((yMax - yMin) / 8.0))))

                    var gx = floor(xMin / xStep) * xStep
                    while (gx <= xMax) {
                        val sx = toScreenX(gx)
                        drawLine(gridColor, Offset(sx, 0f), Offset(sx, h), strokeWidth = 1f)
                        gx += xStep
                    }

                    var gy = floor(yMin / yStep) * yStep
                    while (gy <= yMax) {
                        val sy = toScreenY(gy)
                        drawLine(gridColor, Offset(0f, sy), Offset(w, sy), strokeWidth = 1f)
                        gy += yStep
                    }

                    // Draw Axes (X and Y)
                    val axisColor = Color(0xFF94A3B8)
                    val originX = toScreenX(0.0)
                    val originY = toScreenY(0.0)

                    if (originX in 0f..w) {
                        drawLine(axisColor, Offset(originX, 0f), Offset(originX, h), strokeWidth = 2.5f)
                    }
                    if (originY in 0f..h) {
                        drawLine(axisColor, Offset(0f, originY), Offset(w, originY), strokeWidth = 2.5f)
                    }

                    // Plot Functions
                    val colors = listOf(Color(0xFF38BDF8), Color(0xFFF59E0B), Color(0xFF10B981))
                    functions.forEachIndexed { idx, eq ->
                        if (eq.isNotBlank()) {
                            val curveColor = colors.getOrElse(idx) { Color.Cyan }
                            val path = Path()
                            var first = true
                            val steps = 300
                            for (i in 0..steps) {
                                val mathX = xMin + (i.toDouble() / steps) * (xMax - xMin)
                                val mathY = evalFunction(eq, mathX)
                                if (!mathY.isNaN() && !mathY.isInfinite()) {
                                    val sx = toScreenX(mathX)
                                    val sy = toScreenY(mathY)
                                    if (first) {
                                        path.moveTo(sx, sy)
                                        first = false
                                    } else {
                                        path.lineTo(sx, sy)
                                    }
                                } else {
                                    first = true
                                }
                            }
                            drawPath(path, curveColor, style = Stroke(width = if (idx == activeIndex) 3.5f else 2f))
                        }
                    }

                    // Draw Crosshair inspected point
                    inspectedPoint?.let { (ix, iy) ->
                        val sx = toScreenX(ix)
                        val sy = toScreenY(iy)
                        if (sx in 0f..w && sy in 0f..h) {
                            drawLine(Color.White.copy(alpha = 0.5f), Offset(sx, 0f), Offset(sx, h), strokeWidth = 1f)
                            drawLine(Color.White.copy(alpha = 0.5f), Offset(0f, sy), Offset(w, sy), strokeWidth = 1f)
                            drawCircle(Color(0xFF38BDF8), radius = 6f, center = Offset(sx, sy))
                            drawCircle(Color.White, radius = 3f, center = Offset(sx, sy))
                        }
                    }
                }

                // Controls overlay (Zoom In, Zoom Out, Reset, Coordinates)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = {
                            val xr = (xMax - xMin) * 0.75
                            val yr = (yMax - yMin) * 0.75
                            val cx = (xMin + xMax) / 2
                            val cy = (yMin + yMax) / 2
                            xMin = cx - xr / 2; xMax = cx + xr / 2
                            yMin = cy - yr / 2; yMax = cy + yr / 2
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            val xr = (xMax - xMin) * 1.33
                            val yr = (yMax - yMin) * 1.33
                            val cx = (xMin + xMax) / 2
                            val cy = (yMin + yMax) / 2
                            xMin = cx - xr / 2; xMax = cx + xr / 2
                            yMin = cy - yr / 2; yMax = cy + yr / 2
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = Color.White)
                    }

                    IconButton(
                        onClick = {
                            xMin = -10.0; xMax = 10.0
                            yMin = -10.0; yMax = 10.0
                            inspectedPoint = null
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.White)
                    }
                }

                // Inspected Point Info Overlay
                inspectedPoint?.let { (x, y) ->
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp),
                        color = Color(0xCC0D1B2A),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "x: ${MathEngine.formatNumber(x, com.example.math.NumberNotation.STANDARD, 3)},  y: ${MathEngine.formatNumber(y, com.example.math.NumberNotation.STANDARD, 3)}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
