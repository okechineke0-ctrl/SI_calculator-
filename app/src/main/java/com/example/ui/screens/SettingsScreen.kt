package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.math.AngleMode
import com.example.math.NumberNotation
import com.example.ui.MainViewModel
import com.example.ui.theme.OceanPrimaryLight
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val angleMode by viewModel.preferences.angleMode.collectAsState()
    val precision by viewModel.preferences.precision.collectAsState()
    val notation by viewModel.preferences.notation.collectAsState()
    val hapticEnabled by viewModel.preferences.hapticEnabled.collectAsState()
    val customApiKey by viewModel.preferences.customApiKey.collectAsState()
    val supabaseUrl by viewModel.preferences.supabaseUrl.collectAsState()
    val supabaseAnonKey by viewModel.preferences.supabaseAnonKey.collectAsState()
    val pythonBackendUrl by viewModel.preferences.pythonBackendUrl.collectAsState()

    var apiKeyInput by remember(customApiKey) { mutableStateOf(customApiKey) }
    var urlInput by remember(supabaseUrl) { mutableStateOf(supabaseUrl) }
    var keyInput by remember(supabaseAnonKey) { mutableStateOf(supabaseAnonKey) }
    var pythonUrlInput by remember(pythonBackendUrl) { mutableStateOf(pythonBackendUrl) }
    var pythonTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingPython by remember { mutableStateOf(false) }
    var connectionTestResult by remember { mutableStateOf<Pair<Boolean, String>?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var showSqlDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val hasSystemKey = BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Identity Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    tint = OceanPrimaryLight,
                    modifier = Modifier.size(36.dp)
                )
                Column {
                    Text(
                        text = "Ocean Math AI",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ocean Technologies • Version 1.0 (Production)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Calculation Preferences
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Mathematical Precision & Format",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                // Angle Mode
                Column {
                    Text("Angle Mode", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        AngleMode.entries.forEachIndexed { index, mode ->
                            SegmentedButton(
                                selected = angleMode == mode,
                                onClick = { viewModel.preferences.setAngleMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = AngleMode.entries.size)
                            ) {
                                Text(mode.name, fontSize = 12.sp)
                            }
                        }
                    }
                }

                // Number Notation
                Column {
                    Text("Number Notation", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                        NumberNotation.entries.forEachIndexed { index, n ->
                            SegmentedButton(
                                selected = notation == n,
                                onClick = { viewModel.preferences.setNotation(n) },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = NumberNotation.entries.size)
                            ) {
                                Text(n.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Decimal Precision Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Displayed Decimal Precision", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("$precision digits", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = precision.toFloat(),
                        onValueChange = { viewModel.preferences.setPrecision(it.toInt()) },
                        valueRange = 2f..10f,
                        steps = 7,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()

                // Haptic Feedback Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Haptic Key Feedback", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Vibrate on keypad touch", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = hapticEnabled,
                        onCheckedChange = { viewModel.preferences.setHaptic(it) }
                    )
                }
            }
        }

        // AI Assistant Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI Mathematics Assistant (Gemini)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = if (hasSystemKey || customApiKey.isNotBlank()) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (hasSystemKey || customApiKey.isNotBlank()) "Active" else "Needs Key",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasSystemKey || customApiKey.isNotBlank()) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Ocean Math AI performs all core calculations (Arithmetic, Scientific, Matrices, Statistics, Calculus) 100% offline on your device. Live Gemini AI is used for step-by-step tutoring and photo problem recognition.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = apiKeyInput,
                    onValueChange = {
                        apiKeyInput = it
                        viewModel.preferences.setCustomApiKey(it)
                    },
                    label = { Text("Custom Gemini API Key (Optional)") },
                    placeholder = { Text("AIzaSy...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (apiKeyInput.isNotEmpty()) {
                            IconButton(onClick = {
                                apiKeyInput = ""
                                viewModel.preferences.setCustomApiKey("")
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    }
                )
            }
        }

        // Python Backend Server (FastAPI + SymPy CAS)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isPythonConfigured = pythonBackendUrl.isNotBlank()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Python Backend Server",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = if (isPythonConfigured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isPythonConfigured) "Python Active" else "Local Kotlin Engine",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPythonConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "A dedicated Python FastAPI server with SymPy (Symbolic Mathematics) and NumPy is included in the project under /backend. Connect your server URL for exact symbolic calculus, limits, and algebraic expansions.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = pythonUrlInput,
                    onValueChange = {
                        pythonUrlInput = it
                        viewModel.preferences.setPythonBackendUrl(it)
                    },
                    label = { Text("Python Server URL (Optional)") },
                    placeholder = { Text("http://10.0.2.2:8000") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Button(
                    onClick = {
                        if (pythonUrlInput.isBlank()) {
                            pythonTestResult = Pair(false, "Please enter a valid Python server URL")
                            return@Button
                        }
                        isTestingPython = true
                        pythonTestResult = null
                        coroutineScope.launch {
                            val res = com.example.ai.PythonBackendClient.evaluateWithPython(pythonUrlInput, "2 + 2")
                            isTestingPython = false
                            pythonTestResult = if (res.isSuccess) {
                                Pair(true, "Successfully connected to Python FastAPI + SymPy backend!")
                            } else {
                                Pair(false, res.error ?: "Failed to reach Python backend")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isTestingPython) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing Python Backend...")
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Python Backend Connection")
                    }
                }

                pythonTestResult?.let { (ok, msg) ->
                    Surface(
                        color = if (ok) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (ok) "✓ $msg" else "✗ $msg",
                            fontSize = 11.sp,
                            color = if (ok) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // Supabase Cloud Database & Storage Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isSupaConfigured = com.example.data.SupabaseService.isConfigured(
                    com.example.data.SupabaseService.getEffectiveUrl(supabaseUrl),
                    com.example.data.SupabaseService.getEffectiveKey(supabaseAnonKey)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Supabase Cloud Database & Storage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Surface(
                        color = if (isSupaConfigured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (isSupaConfigured) "Configured" else "Local Only",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSupaConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Sync calculations and snapped math photos across devices to your own Supabase project. Snapped pictures are stored in Supabase Storage ('math-snaps') and indexed in PostgreSQL ('snapped_calculations').",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        viewModel.preferences.setSupabaseUrl(it)
                    },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = keyInput,
                    onValueChange = {
                        keyInput = it
                        viewModel.preferences.setSupabaseAnonKey(it)
                    },
                    label = { Text("Supabase Anon Public API Key") },
                    placeholder = { Text("eyJhbGciOi...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            isTestingConnection = true
                            connectionTestResult = null
                            coroutineScope.launch {
                                val res = com.example.data.SupabaseService.testConnection(urlInput, keyInput)
                                connectionTestResult = res
                                isTestingConnection = false
                            }
                        },
                        enabled = !isTestingConnection && urlInput.isNotBlank() && keyInput.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text("Test Connection", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showSqlDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View SQL Script", fontSize = 12.sp)
                    }
                }

                connectionTestResult?.let { (ok, msg) ->
                    Surface(
                        color = if (ok) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (ok) "✓ $msg" else "✗ $msg",
                            fontSize = 11.sp,
                            color = if (ok) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }

        // About & Legal Card
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
                    text = "Ocean Technologies",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "Designed for engineers, mathematicians, students, researchers, and professional users worldwide.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showPrivacyDialog = true }) {
                        Text("Privacy Policy", fontSize = 12.sp)
                    }
                    OutlinedButton(onClick = { showTermsDialog = true }) {
                        Text("Terms of Service", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Text(
                    "Ocean Math AI by Ocean Technologies is committed to your privacy:\n\n" +
                            "• Offline First: Basic arithmetic, scientific functions, calculus, matrices, physics formulas, and history are computed and saved locally on your device.\n" +
                            "• Cloud AI: When using Snap & Solve or live AI explanations, images and text questions are securely transmitted to Google's Gemini API strictly for transcription and mathematical explanation.\n" +
                            "• No Advertising or Tracking: We do not sell your data or embed third-party advertising trackers."
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Terms of Service") },
            text = {
                Text(
                    "Ocean Math AI is provided by Ocean Technologies for educational, scientific, and professional computation.\n\n" +
                            "All calculations and derivations should be independently verified for critical engineering, medical, or life-safety applications."
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showSqlDialog) {
        val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { showSqlDialog = false },
            title = { Text("Supabase SQL Setup") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        "Run this SQL query in your Supabase Dashboard > SQL Editor to create the table and storage bucket:",
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = com.example.data.SupabaseService.SQL_SETUP_SCRIPT.trim(),
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    clipboard.setText(androidx.compose.ui.text.AnnotatedString(com.example.data.SupabaseService.SQL_SETUP_SCRIPT.trim()))
                    showSqlDialog = false
                }) {
                    Text("Copy SQL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSqlDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
