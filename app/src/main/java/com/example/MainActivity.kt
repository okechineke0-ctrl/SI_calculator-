package com.example

import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppNavTab
import com.example.ui.MainViewModel
import com.example.ui.screens.*
import com.example.ui.theme.OceanMathTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            OceanMathTheme {
                OceanMathApp(viewModel = viewModel)
            }
        }
    }
}

data class NavigationItem(
    val tab: AppNavTab,
    val icon: ImageVector,
    val label: String
)

@Composable
fun OceanMathApp(viewModel: MainViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()

    // BackHandler: When not on Calculator tab, back press returns to Calculator
    BackHandler(enabled = currentTab != AppNavTab.CALCULATOR) {
        viewModel.selectTab(AppNavTab.CALCULATOR)
    }

    // Exactly 5 Icons with AI Mode centered at index 2
    val navItems = remember {
        listOf(
            NavigationItem(AppNavTab.CALCULATOR, Icons.Default.Calculate, "Calc"),
            NavigationItem(AppNavTab.GRAPH, Icons.AutoMirrored.Filled.ShowChart, "Graph"),
            NavigationItem(AppNavTab.AI_SOLVE, Icons.Default.AutoAwesome, "AI Mode"),
            NavigationItem(AppNavTab.MATH_TOOLS, Icons.Default.Widgets, "Tools"),
            NavigationItem(AppNavTab.FORMULAS_HISTORY, Icons.AutoMirrored.Filled.MenuBook, "History")
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            OceanBottomNavigation(
                items = navItems,
                currentTab = currentTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppNavTab.CALCULATOR -> CalculatorScreen(viewModel)
                AppNavTab.GRAPH -> GraphScreen(viewModel)
                AppNavTab.AI_SOLVE -> AiSolveScreen(viewModel)
                AppNavTab.MATH_TOOLS -> MathToolsScreen(viewModel)
                AppNavTab.FORMULAS_HISTORY -> FormulasHistoryScreen(viewModel)
                AppNavTab.SETTINGS -> SettingsScreen(viewModel)
            }
        }
    }
}

/**
 * Mature, unified 5-icon navigation bar with shared background styling across all items
 * and a smooth animated scale hover effect on selection.
 */
@Composable
fun OceanBottomNavigation(
    items: List<NavigationItem>,
    currentTab: AppNavTab,
    onSelectTab: (AppNavTab) -> Unit
) {
    val view = LocalView.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentTab == item.tab
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()

                // Animated tactile press squish + selection scale bounce
                val targetScale = when {
                    isPressed -> 0.88f
                    isSelected -> 1.22f
                    else -> 1.0f
                }

                val scale by animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "tabScale"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                            onSelectTab(item.tab)
                        }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                } else {
                                    Color.Transparent
                                }
                            )
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(22.dp),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = item.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                        }
                    )
                }
            }
        }
    }
}
