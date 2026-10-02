package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OceanAccentEmerald
import com.example.ui.theme.OceanAccentGold
import com.example.ui.theme.OceanCard
import com.example.ui.theme.OceanPrimary
import com.example.ui.theme.OceanPrimaryLight

enum class CSToolSubTab(val title: String) {
    ASCII_ENCODER("ASCII & Character"),
    BASE_CONVERTER("Radix Base Converter"),
    BITWISE_LOGIC("Bitwise Operations"),
    DATA_STORAGE("Storage & Bandwidth")
}

@Composable
fun ComputerScienceTool() {
    var selectedSubTab by remember { mutableStateOf(CSToolSubTab.ASCII_ENCODER) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sub-navigation tabs
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab.ordinal,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = OceanPrimary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            CSToolSubTab.entries.forEach { tab ->
                Tab(
                    selected = selectedSubTab == tab,
                    onClick = { selectedSubTab = tab },
                    text = {
                        Text(
                            text = tab.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                )
            }
        }

        when (selectedSubTab) {
            CSToolSubTab.ASCII_ENCODER -> AsciiCharacterTool()
            CSToolSubTab.BASE_CONVERTER -> RadixBaseConverterTool()
            CSToolSubTab.BITWISE_LOGIC -> BitwiseLogicTool()
            CSToolSubTab.DATA_STORAGE -> DataStorageTool()
        }
    }
}

// -------------------------------------------------------------
// 1. ASCII & Character Encoding Tool
// -------------------------------------------------------------
@Composable
fun AsciiCharacterTool() {
    var inputText by remember { mutableStateOf("A") }
    val primaryChar = inputText.firstOrNull() ?: 'A'
    val code = primaryChar.code

    // Alphabet positioning: A=1, B=2, ..., Z=26
    val isAlphabet = primaryChar in 'a'..'z' || primaryChar in 'A'..'Z'
    val alphabetIndex = if (isAlphabet) {
        if (primaryChar in 'A'..'Z') primaryChar - 'A' + 1 else primaryChar - 'a' + 1
    } else null
    val alphabet5Bit = alphabetIndex?.toString(2)?.padStart(5, '0')

    val asciiBinary = code.toString(2).padStart(8, '0')
    val asciiHex = code.toString(16).uppercase().padStart(2, '0')
    val asciiOct = code.toString(8).padStart(3, '0')
    val isLowerCase = primaryChar in 'a'..'z'
    val isUpperCase = primaryChar in 'A'..'Z'

    val category = when {
        isUpperCase -> "Uppercase Latin Letter"
        isLowerCase -> "Lowercase Latin Letter"
        primaryChar in '0'..'9' -> "Decimal Digit"
        primaryChar == ' ' -> "Whitespace (Space)"
        code < 32 -> "Control Character"
        code == 127 -> "Delete Control Character"
        else -> "Punctuation / Symbol"
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Input text field
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter Letter or Text String") },
            placeholder = { Text("e.g. A, a, 42, Ocean") },
            leadingIcon = { Icon(Icons.Default.Code, contentDescription = "Code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OceanPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Quick Preset Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("A", "a", "B", "b", "Z", "z", "0", "7", "@", "#", "Space", "!").forEach { sample ->
                SuggestionChip(
                    onClick = { inputText = if (sample == "Space") " " else sample },
                    label = { Text(sample, fontFamily = FontFamily.Monospace, fontSize = 12.sp) }
                )
            }
        }

        // Executive Hero Card: Primary Character Analysis
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PRIMARY CHARACTER ANALYSIS",
                            style = MaterialTheme.typography.labelSmall,
                            color = OceanPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.typography.bodySmall.color.copy(alpha = 0.7f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OceanPrimary.copy(alpha = 0.15f))
                            .border(1.dp, OceanPrimary.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (primaryChar == ' ') "␣" else primaryChar.toString(),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanPrimary
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Highlighted Alphabet Index & 5-bit binary (Direct User Feature!)
                if (alphabetIndex != null && alphabet5Bit != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = OceanAccentGold.copy(alpha = 0.12f)
                        ),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(OceanAccentGold.copy(alpha = 0.4f)))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ALPHABET POSITION (${primaryChar.uppercaseChar()})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanAccentGold
                                )
                                Text(
                                    text = "Letter #${alphabetIndex} of 26",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "5-Bit Alphabet Code:",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = alphabet5Bit,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanAccentGold
                                )
                            }
                        }
                    }
                }

                // Four Fundamental Encodings: DEC, HEX, OCT, 8-BIT BIN
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MetricBox(modifier = Modifier.weight(1f), label = "DECIMAL", value = code.toString())
                    MetricBox(modifier = Modifier.weight(1f), label = "HEXADECIMAL", value = "0x$asciiHex")
                    MetricBox(modifier = Modifier.weight(1f), label = "OCTAL", value = "0$asciiOct")
                }

                // 8-Bit ASCII Binary Display with Bit 5 Highlight
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "8-BIT ASCII BYTE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Bit 7 ... Bit 0",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Byte representation with nibble spacing: 4 bits + space + 4 bits
                        val highNibble = asciiBinary.substring(0, 4)
                        val lowNibble = asciiBinary.substring(4, 8)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$highNibble $lowNibble",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimaryLight,
                                letterSpacing = 2.sp
                            )
                        }

                        // Case Bit Explanation if alphabet
                        if (isAlphabet) {
                            val caseBit = asciiBinary[2] // index 2 corresponds to Bit 5 (2^5 = 32)
                            Text(
                                text = "• Case Bit (Bit 5 / 0x20): '$caseBit' → ${if (isLowerCase) "1 (Lowercase)" else "0 (Uppercase)"}. Toggling bit 5 flips case between '${primaryChar.uppercaseChar()}' and '${primaryChar.lowercaseChar()}'.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // HTML Entity & URL encoding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "HTML: &#${code};",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Unicode: U+${asciiHex.padStart(4, '0')}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "URL: %$asciiHex",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Multi-Character String Stream (if input text has multiple characters)
        if (inputText.length > 1) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "STRING STREAM BREAKDOWN (${inputText.length} CHARACTERS)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = OceanPrimary
                    )

                    // Header row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Char", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(36.dp), fontWeight = FontWeight.Bold)
                        Text("Dec", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(42.dp), fontWeight = FontWeight.Bold)
                        Text("Hex", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(42.dp), fontWeight = FontWeight.Bold)
                        Text("8-Bit Binary", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    inputText.take(16).forEachIndexed { idx, ch ->
                        val cCode = ch.code
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (ch == ' ') "␣" else ch.toString(),
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = OceanPrimary,
                                modifier = Modifier.width(36.dp)
                            )
                            Text(
                                text = cCode.toString(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.width(42.dp)
                            )
                            Text(
                                text = "0x" + cCode.toString(16).uppercase().padStart(2, '0'),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.width(42.dp)
                            )
                            Text(
                                text = cCode.toString(2).padStart(8, '0'),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = OceanAccentEmerald
                            )
                        }
                    }

                    if (inputText.length > 16) {
                        Text(
                            text = "... and ${inputText.length - 16} more characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Radix Base Converter (Programmer Multi-Base Calculator)
// -------------------------------------------------------------
@Composable
fun RadixBaseConverterTool() {
    var rawInput by remember { mutableStateOf("42") }
    var activeBase by remember { mutableIntStateOf(10) } // 2, 8, 10, 16
    var bitWidth by remember { mutableIntStateOf(32) } // 8, 16, 32, 64

    val decimalVal: Long? = try {
        val clean = rawInput.trim().replace(" ", "").replace("_", "")
        if (clean.isEmpty()) 0L else clean.toLong(activeBase)
    } catch (e: Exception) {
        null
    }

    val valOrZero = decimalVal ?: 0L
    val mask = if (bitWidth == 64) -1L else (1L shl bitWidth) - 1L
    val maskedVal = valOrZero and mask

    val binString = maskedVal.toString(2).padStart(bitWidth, '0')
    val hexString = maskedVal.toString(16).uppercase().padStart(bitWidth / 4, '0')
    val octString = maskedVal.toString(8)

    // Signed Two's Complement check
    val isNegative = bitWidth < 64 && (maskedVal and (1L shl (bitWidth - 1))) != 0L
    val signedDecimal = if (isNegative) maskedVal - (1L shl bitWidth) else maskedVal

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Input Card with Base Selector
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "RADIX BASE INPUT & WORD SIZE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )

                // Word Size Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(8 to "BYTE (8)", 16 to "WORD (16)", 32 to "DWORD (32)", 64 to "QWORD (64)").forEach { (bits, label) ->
                        FilterChip(
                            selected = bitWidth == bits,
                            onClick = { bitWidth = bits },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Input base selector
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    listOf(16 to "HEX", 10 to "DEC", 8 to "OCT", 2 to "BIN").forEachIndexed { idx, (base, label) ->
                        SegmentedButton(
                            selected = activeBase == base,
                            onClick = {
                                if (decimalVal != null) {
                                    rawInput = when (base) {
                                        16 -> decimalVal.toString(16).uppercase()
                                        10 -> decimalVal.toString()
                                        8 -> decimalVal.toString(8)
                                        else -> decimalVal.toString(2)
                                    }
                                }
                                activeBase = base
                            },
                            shape = SegmentedButtonDefaults.itemShape(index = idx, count = 4)
                        ) {
                            Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = rawInput,
                    onValueChange = { rawInput = it },
                    label = { Text("Value in ${when (activeBase) { 16 -> "Hexadecimal"; 8 -> "Octal"; 2 -> "Binary"; else -> "Decimal" }}") },
                    keyboardOptions = KeyboardOptions(keyboardType = if (activeBase == 10) KeyboardType.Number else KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = decimalVal == null && rawInput.isNotBlank()
                )
            }
        }

        // Real-Time Simultaneous Base Conversion Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "SIMULTANEOUS RADIX CONVERSION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )

                ConversionResultRow(label = "HEX", value = "0x$hexString", isCurrent = activeBase == 16)
                ConversionResultRow(label = "DEC (Unsigned)", value = maskedVal.toString(), isCurrent = activeBase == 10)
                ConversionResultRow(label = "DEC (Signed 2's)", value = signedDecimal.toString(), isCurrent = false)
                ConversionResultRow(label = "OCT", value = "0$octString", isCurrent = activeBase == 8)

                // Binary with formatted 4-bit nibbles
                val formattedBin = binString.chunked(4).joinToString(" ")
                ConversionResultRow(label = "BIN", value = formattedBin, isCurrent = activeBase == 2)
            }
        }

        // Interactive 8-Bit / 16-Bit Switch Grid
        if (bitWidth <= 16) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "INTERACTIVE BIT SWITCHES (TAP TO TOGGLE)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 0 until bitWidth) {
                            val bitIndex = bitWidth - 1 - i
                            val isSet = (maskedVal and (1L shl bitIndex)) != 0L
                            BitToggleItem(
                                bitIndex = bitIndex,
                                isSet = isSet,
                                onToggle = {
                                    val newVal = maskedVal xor (1L shl bitIndex)
                                    rawInput = when (activeBase) {
                                        16 -> newVal.toString(16).uppercase()
                                        10 -> newVal.toString()
                                        8 -> newVal.toString(8)
                                        else -> newVal.toString(2)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BitToggleItem(bitIndex: Int, isSet: Boolean, onToggle: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onToggle() }
    ) {
        Text(
            text = "$bitIndex",
            fontSize = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSet) OceanPrimary else MaterialTheme.colorScheme.surfaceVariant)
                .border(1.dp, if (isSet) OceanPrimaryLight else MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isSet) "1" else "0",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isSet) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// -------------------------------------------------------------
// 3. Bitwise Logic Tool
// -------------------------------------------------------------
@Composable
fun BitwiseLogicTool() {
    var valAStr by remember { mutableStateOf("12") }
    var valBStr by remember { mutableStateOf("10") }
    var operation by remember { mutableStateOf("AND") }
    var bitSize by remember { mutableIntStateOf(8) }

    val a = valAStr.toLongOrNull() ?: 0L
    val b = valBStr.toLongOrNull() ?: 0L
    val mask = (1L shl bitSize) - 1L

    val resultVal = when (operation) {
        "AND" -> (a and b) and mask
        "OR" -> (a or b) and mask
        "XOR" -> (a xor b) and mask
        "NOT" -> (a.inv()) and mask
        "NAND" -> ((a and b).inv()) and mask
        "NOR" -> ((a or b).inv()) and mask
        "SHL (<<)" -> (a shl 1) and mask
        "SHR (>>)" -> (a ushr 1) and mask
        else -> 0L
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Operation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("AND", "OR", "XOR", "NOT", "NAND", "NOR", "SHL (<<)", "SHR (>>)").forEach { op ->
                FilterChip(
                    selected = operation == op,
                    onClick = { operation = op },
                    label = { Text(op, fontWeight = FontWeight.Bold, fontSize = 11.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = valAStr,
                onValueChange = { valAStr = it },
                label = { Text("Operand A (Dec)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            if (!operation.contains("NOT") && !operation.contains("SH")) {
                OutlinedTextField(
                    value = valBStr,
                    onValueChange = { valBStr = it },
                    label = { Text("Operand B (Dec)") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        }

        // Bitwise visual comparison card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "BITWISE EXECUTION (8-BIT ALIGNMENT)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )

                val aBin = (a and mask).toString(2).padStart(bitSize, '0')
                val bBin = (b and mask).toString(2).padStart(bitSize, '0')
                val resBin = resultVal.toString(2).padStart(bitSize, '0')

                BitwiseLine("A", aBin, a)
                if (!operation.contains("NOT") && !operation.contains("SH")) {
                    BitwiseLine(operation, bBin, b)
                } else {
                    Text(
                        text = "Operation: $operation",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                BitwiseLine("=", resBin, resultVal, isResult = true)
            }
        }
    }
}

@Composable
fun BitwiseLine(label: String, binary: String, decimal: Long, isResult: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label.padEnd(5),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isResult) OceanAccentEmerald else MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = binary.chunked(4).joinToString(" "),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = if (isResult) OceanAccentEmerald else OceanPrimaryLight
        )
        Text(
            text = "($decimal)",
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// -------------------------------------------------------------
// 4. Data Storage & Bandwidth Converter
// -------------------------------------------------------------
@Composable
fun DataStorageTool() {
    var rawSize by remember { mutableStateOf("1024") }
    var sourceUnit by remember { mutableStateOf("MB") }

    val sizeNum = rawSize.toDoubleOrNull() ?: 0.0

    // Compute standard bytes (binary 1024-based)
    val bytes = when (sourceUnit) {
        "Bits" -> sizeNum / 8.0
        "Bytes" -> sizeNum
        "KB" -> sizeNum * 1024.0
        "MB" -> sizeNum * 1024.0 * 1024.0
        "GB" -> sizeNum * 1024.0 * 1024.0 * 1024.0
        "TB" -> sizeNum * 1024.0 * 1024.0 * 1024.0 * 1024.0
        else -> sizeNum
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = rawSize,
                onValueChange = { rawSize = it },
                label = { Text("Size") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )

            var expanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.align(Alignment.CenterVertically)) {
                Button(onClick = { expanded = true }) {
                    Text(sourceUnit)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("Bits", "Bytes", "KB", "MB", "GB", "TB").forEach { u ->
                        DropdownMenuItem(
                            text = { Text(u) },
                            onClick = {
                                sourceUnit = u
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "STORAGE EQUIVALENTS (BINARY IEC STANDARD)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = OceanPrimary
                )

                StorageRow("Bits", "${String.format("%,.0f", bytes * 8.0)} bits")
                StorageRow("Bytes", "${String.format("%,.0f", bytes)} B")
                StorageRow("Kilobytes (KiB)", "${String.format("%.4f", bytes / 1024.0)} KiB")
                StorageRow("Megabytes (MiB)", "${String.format("%.4f", bytes / (1024.0 * 1024.0))} MiB")
                StorageRow("Gigabytes (GiB)", "${String.format("%.6f", bytes / (1024.0 * 1024.0 * 1024.0))} GiB")
                StorageRow("Terabytes (TiB)", "${String.format("%.8f", bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0))} TiB")
            }
        }
    }
}

@Composable
fun StorageRow(unit: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(unit, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MetricBox(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = FontFamily.Monospace,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = OceanPrimary
            )
        }
    }
}

@Composable
fun ConversionResultRow(label: String, value: String, isCurrent: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrent) OceanPrimary.copy(alpha = 0.12f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrent) OceanPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isCurrent) OceanPrimary else MaterialTheme.colorScheme.onSurface
        )
    }
}
