package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
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
import com.example.data.CalculationEntity
import com.example.math.FormulaItem
import com.example.math.FormulaLibrary
import com.example.ui.MainViewModel
import com.example.ui.theme.OceanAccentEmerald
import com.example.ui.theme.OceanAccentGold
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FormulasHistoryScreen(viewModel: MainViewModel) {
    var activeSubTab by remember { mutableIntStateOf(1) } // Default to History so users see earlier solved problems immediately!
    val historyItems by viewModel.allHistory.collectAsState()
    val favorites by viewModel.favoriteHistory.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var formulaSearch by remember { mutableStateOf("") }
    var historySearch by remember { mutableStateOf("") }
    var historyFilter by remember { mutableStateOf("All") } // "All", "Photos", "Favorites"
    var selectedItemDetail by remember { mutableStateOf<CalculationEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Discreet Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Formulas & History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(
                onClick = { viewModel.selectTab(com.example.ui.AppNavTab.SETTINGS) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = activeSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeSubTab == 0,
                onClick = { activeSubTab = 0 },
                text = { Text("Formula Library (${FormulaLibrary.allFormulas.size})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
            Tab(
                selected = activeSubTab == 1,
                onClick = { activeSubTab = 1 },
                text = { Text("Solved Problems (${historyItems.size})", fontWeight = FontWeight.SemiBold) },
                icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(18.dp)) }
            )
        }

        if (activeSubTab == 0) {
            // Formula Library
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                OutlinedTextField(
                    value = formulaSearch,
                    onValueChange = { formulaSearch = it },
                    placeholder = { Text("Search formulas (e.g., quadratic, ohm, simpson)...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                )

                val filteredFormulas = FormulaLibrary.allFormulas.filter {
                    it.name.contains(formulaSearch, ignoreCase = true) ||
                            it.category.contains(formulaSearch, ignoreCase = true) ||
                            it.formula.contains(formulaSearch, ignoreCase = true)
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredFormulas, key = { it.id }) { item ->
                        FormulaCard(
                            formula = item,
                            onUseInCalculator = { viewModel.useFormulaInCalculator(item) }
                        )
                    }
                }
            }
        } else {
            // Solved Problems & Calculations History
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Search Bar & Clear Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = historySearch,
                        onValueChange = { historySearch = it },
                        placeholder = { Text("Search earlier problems & answers...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (historyItems.isNotEmpty()) {
                        IconButton(
                            onClick = { coroutineScope.launch { viewModel.repository.clearAll() } }
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                // Filter Chips (All, Photos, Favorites)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = historyFilter == "All",
                        onClick = { historyFilter = "All" },
                        label = { Text("All (${historyItems.size})", fontSize = 12.sp) }
                    )
                    val photoCount = historyItems.count { it.imageBase64 != null || it.calculationType == "Snap & Solve" }
                    FilterChip(
                        selected = historyFilter == "Photos",
                        onClick = { historyFilter = "Photos" },
                        label = { Text("📷 Snapped Photos ($photoCount)", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = historyFilter == "Favorites",
                        onClick = { historyFilter = "Favorites" },
                        label = { Text("⭐ Favorites (${favorites.size})", fontSize = 12.sp) }
                    )
                }

                // Apply Filters
                val baseList = when (historyFilter) {
                    "Favorites" -> favorites
                    "Photos" -> historyItems.filter { it.imageBase64 != null || it.calculationType == "Snap & Solve" }
                    else -> historyItems
                }

                val filteredHistory = baseList.filter {
                    it.expression.contains(historySearch, ignoreCase = true) ||
                            it.result.contains(historySearch, ignoreCase = true) ||
                            (it.solution?.contains(historySearch, ignoreCase = true) == true)
                }

                if (filteredHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.HistoryEdu, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Text(
                                text = when (historyFilter) {
                                    "Photos" -> "No snapped photos yet. Take a photo in AI Snap to solve!"
                                    "Favorites" -> "No starred favorite problems yet."
                                    else -> "No solved problems yet. Calculations will be saved here automatically."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredHistory, key = { it.id }) { item ->
                            HistoryItemCard(
                                item = item,
                                onClick = { selectedItemDetail = item },
                                onCopy = { clipboardManager.setText(AnnotatedString(item.result)) },
                                onToggleFav = { coroutineScope.launch { viewModel.repository.toggleFavorite(item.id, item.isFavorite) } },
                                onDelete = { coroutineScope.launch { viewModel.repository.delete(item) } }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog for Inspected Problem & Solution
    selectedItemDetail?.let { item ->
        CalculationDetailDialog(
            item = item,
            onDismiss = { selectedItemDetail = null },
            onUseInCalculator = {
                viewModel.useHistoryItem(item)
                selectedItemDetail = null
            },
            onShare = {
                shareMathProblem(context, item)
            },
            onCopy = {
                val fullText = buildString {
                    append("Problem: ${item.expression}\n")
                    append("Answer: ${item.result}\n")
                    if (!item.solution.isNullOrBlank()) {
                        append("\nSolution:\n${item.solution}\n")
                    }
                }
                clipboardManager.setText(AnnotatedString(fullText))
            }
        )
    }
}

private fun shareMathProblem(context: Context, item: CalculationEntity) {
    val shareContent = buildString {
        append("📐 Math Problem: ${item.expression}\n")
        append("✓ Answer: ${item.result}\n\n")
        if (!item.solution.isNullOrBlank()) {
            append("📖 Step-by-Step Explanation:\n${item.solution}\n\n")
        }
        append("Solved with Ocean Math AI")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Math Solution: ${item.expression.take(30)}")
        putExtra(Intent.EXTRA_TEXT, shareContent)
    }
    context.startActivity(Intent.createChooser(intent, "Share Math Problem & Solution"))
}

@Composable
fun CalculationDetailDialog(
    item: CalculationEntity,
    onDismiss: () -> Unit,
    onUseInCalculator: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit
) {
    val dateStr = remember(item.timestamp) {
        val sdf = SimpleDateFormat("EEEE, MMM dd, yyyy 'at' HH:mm", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    val photoBitmap = remember(item.imageBase64) {
        if (item.imageBase64 != null) {
            try {
                val clean = item.imageBase64.removePrefix("data:image/jpeg;base64,")
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(item.calculationType, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(dateStr.take(12), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // If photographed image exists, display high-resolution photo card
                photoBitmap?.let { bmp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Photographed Problem:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Original Math Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }
                }

                // Problem Statement
                Column {
                    Text("Problem / Expression:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item.expression, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                // Answer Card
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Result:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text(item.result, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                // Complete Step-by-Step Solution if available
                if (!item.solution.isNullOrBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Full Step-by-Step Derivation:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            HorizontalDivider()
                            Text(item.solution, fontSize = 13.sp, lineHeight = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = onUseInCalculator, shape = RoundedCornerShape(8.dp)) {
                    Text("Open in Calc", fontSize = 12.sp)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FormulaCard(
    formula: FormulaItem,
    onUseInCalculator: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formula.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = formula.category,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formula.formula,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            formula.variables.forEach { variable ->
                Text("• $variable", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Text("Example: ${formula.example}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Button(
                onClick = onUseInCalculator,
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open in Calculator", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun HistoryItemCard(
    item: CalculationEntity,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onToggleFav: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = remember(item.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    val thumbnailBitmap = remember(item.imageBase64) {
        if (item.imageBase64 != null) {
            try {
                val clean = item.imageBase64.removePrefix("data:image/jpeg;base64,")
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.calculationType,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (item.isSyncedToSupabase) {
                        Surface(
                            color = OceanAccentEmerald.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Cloud Backup",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanAccentEmerald,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFav, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (item.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (item.isFavorite) OceanAccentGold else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                thumbnailBitmap?.let { bmp ->
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Snapped problem thumbnail",
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.expression,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "= ${item.result}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dateStr,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        if (!item.solution.isNullOrBlank()) {
                            Text(
                                text = "Tap to view full solution →",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
