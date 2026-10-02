package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.AngleMode
import com.example.ui.CalculatorMode
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun CalculatorScreen(viewModel: MainViewModel) {
    val expression by viewModel.expression.collectAsState()
    val result by viewModel.result.collectAsState()
    val previewResult by viewModel.previewResult.collectAsState()
    val fractionResult by viewModel.fractionResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val memory by viewModel.memory.collectAsState()
    val calcMode by viewModel.calculatorMode.collectAsState()
    val angleMode by viewModel.preferences.angleMode.collectAsState()
    val hapticEnabled by viewModel.preferences.hapticEnabled.collectAsState()

    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Secondary / Shift function toggle
    var isShiftActive by remember { mutableStateOf(false) }
    var isHyperbolicActive by remember { mutableStateOf(false) }

    fun triggerHaptic() {
        if (hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    LaunchedEffect(expression) {
        scrollState.scrollTo(scrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top Toolbar: Mode selector & Angle badge & Memory indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Selector Tabs (Basic / Scientific / Engineering)
            SingleChoiceSegmentedButtonRow {
                CalculatorMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        selected = calcMode == mode,
                        onClick = {
                            triggerHaptic()
                            viewModel.setCalculatorMode(mode)
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = CalculatorMode.entries.size),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(mode.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Memory Badge
                if (memory != 0.0) {
                    Surface(
                        color = OceanAccentGold.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "M",
                            color = OceanAccentGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Angle Mode Badge (DEG / RAD / GRAD)
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.clickable {
                        triggerHaptic()
                        viewModel.toggleAngleMode()
                    }
                ) {
                    Text(
                        text = angleMode.name,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Display Screen Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                // Expression Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = expression.ifEmpty { "0" },
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 32.sp,
                            fontFamily = FontFamily.SansSerif,
                            color = if (expression.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else MaterialTheme.colorScheme.onSurface
                        ),
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }

                // Result / Preview / Fraction / Error
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        // Live Preview or Final Result
                        val displayText = if (result.isNotEmpty()) result else previewResult
                        if (displayText.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (fractionResult != null) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "= $fractionResult",
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = if (result.isNotEmpty()) "= $result" else displayText,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.isNotEmpty()) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.clickable {
                                        if (displayText.isNotEmpty()) {
                                            clipboardManager.setText(AnnotatedString(displayText.removePrefix("= ")))
                                            triggerHaptic()
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Direct AI Explanation & Verification Action
                    if (expression.isNotEmpty() || result.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable {
                                triggerHaptic()
                                viewModel.verifyExpressionWithAi()
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(13.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("AI Explain & Verify", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }
                }
            }
        }

        // Memory Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("MC", "MR", "M+", "M-", "MS").forEach { memKey ->
                CalculatorKey(
                    label = memKey,
                    bgColor = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f).height(36.dp),
                    fontSize = 11.sp,
                    onClick = {
                        triggerHaptic()
                        viewModel.onKeyPress(memKey)
                    }
                )
            }
        }

        // Scientific Function Rows (Visible in Scientific & Engineering modes)
        AnimatedVisibility(visible = calcMode != CalculatorMode.BASIC) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Row 1: Shift, Hyp, Trig functions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CalculatorKey(
                        label = if (isShiftActive) "2nd▲" else "2nd",
                        bgColor = if (isShiftActive) OceanAccentGold else MaterialTheme.colorScheme.surfaceVariant,
                        textColor = if (isShiftActive) Color.Black else OceanAccentGold,
                        modifier = Modifier.weight(1f).height(40.dp),
                        fontSize = 11.sp,
                        onClick = {
                            triggerHaptic()
                            isShiftActive = !isShiftActive
                        }
                    )
                    CalculatorKey(
                        label = if (isHyperbolicActive) "hyp▲" else "hyp",
                        bgColor = if (isHyperbolicActive) OceanPrimaryLight else MaterialTheme.colorScheme.surfaceVariant,
                        textColor = if (isHyperbolicActive) Color.Black else OceanPrimaryLight,
                        modifier = Modifier.weight(1f).height(40.dp),
                        fontSize = 11.sp,
                        onClick = {
                            triggerHaptic()
                            isHyperbolicActive = !isHyperbolicActive
                        }
                    )

                    val sinLabel = when {
                        isHyperbolicActive && isShiftActive -> "sinh⁻¹"
                        isHyperbolicActive -> "sinh"
                        isShiftActive -> "sin⁻¹"
                        else -> "sin"
                    }
                    val cosLabel = when {
                        isHyperbolicActive && isShiftActive -> "cosh⁻¹"
                        isHyperbolicActive -> "cosh"
                        isShiftActive -> "cos⁻¹"
                        else -> "cos"
                    }
                    val tanLabel = when {
                        isHyperbolicActive && isShiftActive -> "tanh⁻¹"
                        isHyperbolicActive -> "tanh"
                        isShiftActive -> "tan⁻¹"
                        else -> "tan"
                    }

                    CalculatorKey(
                        label = sinLabel,
                        bgColor = KeypadScientificBg,
                        textColor = OceanPrimaryLight,
                        modifier = Modifier.weight(1f).height(40.dp),
                        fontSize = 12.sp,
                        onClick = { triggerHaptic(); viewModel.onKeyPress(sinLabel) }
                    )
                    CalculatorKey(
                        label = cosLabel,
                        bgColor = KeypadScientificBg,
                        textColor = OceanPrimaryLight,
                        modifier = Modifier.weight(1f).height(40.dp),
                        fontSize = 12.sp,
                        onClick = { triggerHaptic(); viewModel.onKeyPress(cosLabel) }
                    )
                    CalculatorKey(
                        label = tanLabel,
                        bgColor = KeypadScientificBg,
                        textColor = OceanPrimaryLight,
                        modifier = Modifier.weight(1f).height(40.dp),
                        fontSize = 12.sp,
                        onClick = { triggerHaptic(); viewModel.onKeyPress(tanLabel) }
                    )
                }

                // Row 2: Powers, Roots, Logs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val p1 = if (isShiftActive) "x³" else "x²"
                    val p2 = if (isShiftActive) "∛" else "√"
                    val p3 = if (isShiftActive) "10ˣ" else "log"
                    val p4 = if (isShiftActive) "eˣ" else "ln"
                    val p5 = if (isShiftActive) "log₂" else "xʸ"

                    listOf(p1, p2, p3, p4, p5).forEach { fnKey ->
                        CalculatorKey(
                            label = fnKey,
                            bgColor = KeypadScientificBg,
                            textColor = TextPrimaryDark,
                            modifier = Modifier.weight(1f).height(40.dp),
                            fontSize = 12.sp,
                            onClick = { triggerHaptic(); viewModel.onKeyPress(fnKey) }
                        )
                    }
                }

                // Row 3: Constants & Combinatorics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val c1 = "π"
                    val c2 = "e"
                    val c3 = if (isShiftActive) "nCr" else "nPr"
                    val c4 = if (isShiftActive) "abs" else "mod"
                    val c5 = "!"

                    listOf(c1, c2, c3, c4, c5).forEach { fnKey ->
                        CalculatorKey(
                            label = fnKey,
                            bgColor = KeypadScientificBg,
                            textColor = TextPrimaryDark,
                            modifier = Modifier.weight(1f).height(40.dp),
                            fontSize = 12.sp,
                            onClick = { triggerHaptic(); viewModel.onKeyPress(fnKey) }
                        )
                    }
                }
            }
        }

        // Primary Keypad (Casio-Style)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Row 1: ( ) % AC DEL
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalculatorKey("(", KeypadOperatorBg, OceanPrimaryLight, Modifier.weight(1f).height(50.dp), 18.sp) {
                    triggerHaptic(); viewModel.onKeyPress("(")
                }
                CalculatorKey(")", KeypadOperatorBg, OceanPrimaryLight, Modifier.weight(1f).height(50.dp), 18.sp) {
                    triggerHaptic(); viewModel.onKeyPress(")")
                }
                CalculatorKey("%", KeypadOperatorBg, OceanPrimaryLight, Modifier.weight(1f).height(50.dp), 18.sp) {
                    triggerHaptic(); viewModel.onKeyPress("%")
                }
                CalculatorKey("DEL", KeypadOperatorBg, OceanAccentCoral, Modifier.weight(1f).height(50.dp), 14.sp) {
                    triggerHaptic(); viewModel.onKeyPress("DEL")
                }
                CalculatorKey("AC", KeypadClearBg, Color.White, Modifier.weight(1f).height(50.dp), 15.sp) {
                    triggerHaptic(); viewModel.onKeyPress("AC")
                }
            }

            // Row 2: 7 8 9 × ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalculatorKey("7", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("7")
                }
                CalculatorKey("8", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("8")
                }
                CalculatorKey("9", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("9")
                }
                CalculatorKey("×", KeypadOperatorBg, OceanPrimaryLight, Modifier.weight(1f).height(52.dp), 22.sp) {
                    triggerHaptic(); viewModel.onKeyPress("×")
                }
                CalculatorKey("÷", KeypadOperatorBg, OceanPrimaryLight, Modifier.weight(1f).height(52.dp), 22.sp) {
                    triggerHaptic(); viewModel.onKeyPress("÷")
                }
            }

            // Row 3: 4 5 6 + -
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalculatorKey("4", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("4")
                }
                CalculatorKey("5", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("5")
                }
                CalculatorKey("6", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("6")
                }
                CalculatorKey("+", KeypadOperatorBg, OceanPrimaryLight, Modifier.weight(1f).height(52.dp), 22.sp) {
                    triggerHaptic(); viewModel.onKeyPress("+")
                }
                CalculatorKey("−", KeypadOperatorBg, OceanPrimaryLight, Modifier.weight(1f).height(52.dp), 22.sp) {
                    triggerHaptic(); viewModel.onKeyPress("−")
                }
            }

            // Row 4: 1 2 3 ± Ans
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalculatorKey("1", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("1")
                }
                CalculatorKey("2", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("2")
                }
                CalculatorKey("3", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("3")
                }
                CalculatorKey("±", KeypadOperatorBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 18.sp) {
                    triggerHaptic(); viewModel.onKeyPress("±")
                }
                CalculatorKey("Ans", KeypadOperatorBg, OceanAccentGold, Modifier.weight(1f).height(52.dp), 14.sp) {
                    triggerHaptic(); viewModel.onKeyPress("Ans")
                }
            }

            // Row 5: 0 . ^ (EXP) =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CalculatorKey("0", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1.5f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress("0")
                }
                CalculatorKey(".", KeypadNumberBg, TextPrimaryDark, Modifier.weight(1f).height(52.dp), 20.sp) {
                    triggerHaptic(); viewModel.onKeyPress(".")
                }
                CalculatorKey("^", KeypadOperatorBg, OceanPrimaryLight, Modifier.weight(1f).height(52.dp), 18.sp) {
                    triggerHaptic(); viewModel.onKeyPress("^")
                }
                CalculatorKey("=", KeypadActionBg, Color.White, Modifier.weight(1.5f).height(52.dp), 24.sp) {
                    triggerHaptic(); viewModel.onKeyPress("=")
                }
            }
        }
    }
}

@Composable
fun CalculatorKey(
    label: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keyScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 3.dp,
        label = "keyElevation"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(elevation, RoundedCornerShape(14.dp))
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = if (isPressed) textColor.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
