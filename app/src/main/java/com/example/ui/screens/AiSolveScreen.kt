package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.AISolutionMode
import com.example.ui.MainViewModel
import com.example.ui.theme.OceanAccentGold
import com.example.ui.theme.OceanPrimaryLight
import kotlinx.coroutines.launch

@Composable
fun AiSolveScreen(viewModel: MainViewModel) {
    val prompt by viewModel.aiPrompt.collectAsState()
    val capturedBitmap by viewModel.aiCapturedBitmap.collectAsState()
    val recognizedProblem by viewModel.aiRecognizedProblem.collectAsState()
    val solutionMode by viewModel.aiSolutionMode.collectAsState()
    val solveResult by viewModel.aiSolveResult.collectAsState()
    val isLoading by viewModel.isAiLoading.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    var isEditingRecognized by remember { mutableStateOf(false) }

    // Universal File / Gallery Picker (Works on all Android versions and emulators)
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                viewModel.processCapturedImage(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Camera Launcher with Safe Fallback
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.processCapturedImage(bitmap)
        } else {
            Toast.makeText(context, "No photo captured", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = OceanPrimaryLight,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = "Ocean Math AI Assistant",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Snap mathematics problems or ask natural language questions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Snap & Photo Capture Actions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Snap & Solve (Camera / OCR)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                cameraLauncher.launch(null)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Camera not available on this emulator. Use gallery or samples below!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Take Photo", fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                filePickerLauncher.launch("image/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Gallery picker unavailable on emulator. Try sample problems below!", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Gallery", fontSize = 13.sp)
                    }
                }

                // Emulator-Friendly Quick Sample Photo Chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "📸 Or Test With Sample Math Photos (Instant Emulator Test):",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = {
                                val bmp = generateMathSampleBitmap("Algebra Problem #1", "2x² - 7x + 3 = 0", "Solve for all values of x.")
                                viewModel.processCapturedImage(bmp)
                            },
                            label = { Text("📐 Quadratic: 2x² - 7x + 3 = 0", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                val bmp = generateMathSampleBitmap("Calculus Problem #2", "∫ (3x² + 4x - 5) dx", "Evaluate the indefinite integral.")
                                viewModel.processCapturedImage(bmp)
                            },
                            label = { Text("∫ Calculus: ∫(3x² + 4x - 5)dx", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                val bmp = generateMathSampleBitmap("Physics Problem #3", "V = 24V, R = 8Ω", "Find current I using Ohm's Law.")
                                viewModel.processCapturedImage(bmp)
                            },
                            label = { Text("⚡ Physics: V=24V, R=8Ω", fontSize = 11.sp) }
                        )
                        AssistChip(
                            onClick = {
                                val bmp = generateMathSampleBitmap("Trigonometry Problem #4", "sin²(θ) + cos²(θ) = ?", "Prove the Pythagorean identity.")
                                viewModel.processCapturedImage(bmp)
                            },
                            label = { Text("📐 Trig: sin²θ + cos²θ", fontSize = 11.sp) }
                        )
                    }
                }

                // Photographed Bitmap Preview
                capturedBitmap?.let { bmp ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Captured Math Problem",
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                viewModel.aiCapturedBitmap.value = null
                                viewModel.aiRecognizedProblem.value = ""
                            },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Recognized Expression Verification & Correction
                if (recognizedProblem.isNotEmpty() || capturedBitmap != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recognized Problem:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanAccentGold
                                )
                                TextButton(onClick = { isEditingRecognized = !isEditingRecognized }) {
                                    Text(if (isEditingRecognized) "Done" else "Edit / Correct", fontSize = 12.sp)
                                }
                            }

                            if (isEditingRecognized) {
                                OutlinedTextField(
                                    value = recognizedProblem,
                                    onValueChange = { viewModel.aiRecognizedProblem.value = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Text(
                                    text = recognizedProblem.ifBlank { "Analyzing problem from photo..." },
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Text Question Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Type Equation or Word Problem",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { viewModel.aiPrompt.value = it },
                    placeholder = { Text("e.g. Find the derivative of x² + 3x, or solve 3x + 5 = 20") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp)
                )

                // Solution Mode Selector Chips
                Text(
                    text = "Solution Mode:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AISolutionMode.entries.forEach { mode ->
                        FilterChip(
                            selected = solutionMode == mode,
                            onClick = { viewModel.aiSolutionMode.value = mode },
                            label = { Text(mode.label, fontSize = 12.sp) }
                        )
                    }
                }

                // Solve Button
                Button(
                    onClick = { viewModel.solveWithAi() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && (prompt.isNotBlank() || recognizedProblem.isNotBlank()),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Thinking...", fontSize = 14.sp)
                    } else {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Solve & Explain", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Solution Output Card
        solveResult?.let { res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                if (res.isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (res.isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = if (res.isSuccess) "Solution & Explanation" else "Notice",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(res.explanation))
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider()

                    Text(
                        text = res.explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    // Auto-Saved Confirmation & Revisit Earlier Problems
                    if (res.isSuccess) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Text(
                                        text = "Saved to Solved Problems History. View picture & full solution anytime.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                TextButton(
                                    onClick = { viewModel.selectTab(com.example.ui.AppNavTab.FORMULAS_HISTORY) }
                                ) {
                                    Text("View History →", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Deterministic Engine Note
                    if (res.isOfflineFallback) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "💡 Tip: Ocean Math AI includes built-in offline solvers for Equations, Matrices, Statistics, and Calculus under the Math Tools tab!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Generates an actual mathematical notebook paper bitmap with handwritten-style typography
 * to enable instant testing on emulators and devices without needing a camera.
 */
fun generateMathSampleBitmap(title: String, equation: String, note: String): Bitmap {
    val width = 720
    val height = 440
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // White notebook paper background
    canvas.drawColor(Color.WHITE)

    // Notebook grid lines
    val gridPaint = Paint().apply {
        color = Color.parseColor("#E2E8F0")
        strokeWidth = 1.5f
    }
    for (y in 40 until height step 40) {
        canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), gridPaint)
    }

    // Left red margin line
    val marginPaint = Paint().apply {
        color = Color.parseColor("#FDA4AF")
        strokeWidth = 2.5f
    }
    canvas.drawLine(70f, 0f, 70f, height.toFloat(), marginPaint)

    // Category / Title
    val titlePaint = Paint().apply {
        color = Color.parseColor("#1D4ED8")
        textSize = 28f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    canvas.drawText(title, 90f, 80f, titlePaint)

    // Equation text
    val eqPaint = Paint().apply {
        color = Color.parseColor("#0F172A")
        textSize = 42f
        isAntiAlias = true
        typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
    }
    canvas.drawText(equation, 90f, 200f, eqPaint)

    // Note / Instruction
    val notePaint = Paint().apply {
        color = Color.parseColor("#64748B")
        textSize = 24f
        isAntiAlias = true
    }
    canvas.drawText(note, 90f, 300f, notePaint)

    return bitmap
}
