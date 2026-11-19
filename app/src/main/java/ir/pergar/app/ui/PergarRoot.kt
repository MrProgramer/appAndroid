package ir.pergar.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.prefs.AppSettings
import ir.pergar.app.ui.components.DividerLine
import ir.pergar.app.ui.components.PergarBackground
import ir.pergar.app.ui.components.SplashScreen
import ir.pergar.app.ui.screens.AccountsScreen
import ir.pergar.app.ui.screens.AddTransactionSheet
import ir.pergar.app.ui.screens.BudgetsScreen
import ir.pergar.app.ui.screens.CalendarScreen
import ir.pergar.app.ui.screens.DebtsScreen
import ir.pergar.app.ui.screens.EducationScreen
import ir.pergar.app.ui.screens.GoalsScreen
import ir.pergar.app.ui.screens.HomeScreen
import ir.pergar.app.ui.screens.LoansScreen
import ir.pergar.app.ui.screens.MoreScreen
import ir.pergar.app.ui.screens.OnboardingScreen
import ir.pergar.app.ui.screens.RadarScreen
import ir.pergar.app.ui.screens.ReportsScreen
import ir.pergar.app.ui.screens.SettingsScreen
import ir.pergar.app.ui.screens.SplitScreen
import ir.pergar.app.ui.screens.SmsCenterScreen
import ir.pergar.app.ui.screens.TasksScreen
import ir.pergar.app.ui.screens.TransactionsScreen
import kotlinx.coroutines.delay

object Routes {
    const val HOME = "home"
    const val TRANSACTIONS = "transactions"
    const val BUDGETS = "budgets"
    const val REPORTS = "reports"
    const val MORE = "more"
    const val ACCOUNTS = "accounts"
    const val LOANS = "loans"
    const val DEBTS = "debts"
    const val GOALS = "goals"
    const val TASKS = "tasks"
    const val SPLIT = "split"
    const val CALENDAR = "calendar"
    const val RADAR = "radar"
    const val EDUCATION = "education"
    const val SETTINGS = "settings"
    const val SMS = "sms"
}

private val topLevelRoutes = listOf(
    Routes.HOME, Routes.TRANSACTIONS, Routes.BUDGETS, Routes.REPORTS, Routes.MORE
)

@Composable
fun PergarRoot(settings: AppSettings) {
    val vm: AppViewModel = viewModel()
    var splashDone by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1250)
        splashDone = true
    }
    when {
        !splashDone -> SplashScreen()
        !settings.onboardingDone -> OnboardingScreen(onFinish = { vm.setOnboardingDone(true) })
        else -> MainShell(vm = vm, settings = settings)
    }
}

@Composable
private fun MainShell(vm: AppViewModel, settings: AppSettings) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
    var addType by remember { mutableStateOf<TxnType?>(null) }
    val asToman = settings.asToman

    val open: (String) -> Unit = { route ->
        if (route in topLevelRoutes) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        } else {
            navController.navigate(route) { launchSingleTop = true }
        }
    }
    val back: () -> Unit = { navController.popBackStack() }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                PergarBottomBar(current = currentRoute, onSelect = open)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PergarBackground {
                NavHost(
                    navController = navController,
                    startDestination = Routes.HOME,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(Routes.HOME) {
                        HomeScreen(
                            vm = vm,
                            asToman = asToman,
                            onOpen = open,
                            onAdd = { addType = it }
                        )
                    }
                    composable(Routes.TRANSACTIONS) {
                        TransactionsScreen(
                            vm = vm,
                            asToman = asToman,
                            onAdd = { addType = it }
                        )
                    }
                    composable(Routes.BUDGETS) {
                        BudgetsScreen(vm = vm, asToman = asToman)
                    }
                    composable(Routes.REPORTS) {
                        ReportsScreen(vm = vm, asToman = asToman)
                    }
                    composable(Routes.MORE) {
                        MoreScreen(onOpen = open)
                    }
                    composable(Routes.ACCOUNTS) {
                        AccountsScreen(vm = vm, asToman = asToman, onBack = back)
                    }
                    composable(Routes.LOANS) {
                        LoansScreen(vm = vm, asToman = asToman, onBack = back)
                    }
                    composable(Routes.DEBTS) {
                        DebtsScreen(vm = vm, asToman = asToman, onBack = back)
                    }
                    composable(Routes.GOALS) {
                        GoalsScreen(vm = vm, asToman = asToman, onBack = back)
                    }
                    composable(Routes.TASKS) {
                        TasksScreen(vm = vm, onBack = back)
                    }
                    composable(Routes.SPLIT) {
                        SplitScreen(vm = vm, asToman = asToman, onBack = back)
                    }
                    composable(Routes.CALENDAR) {
                        CalendarScreen(vm = vm, asToman = asToman, onBack = back)
                    }
                    composable(Routes.RADAR) {
                        RadarScreen(vm = vm, asToman = asToman, onBack = back)
                    }
                    composable(Routes.EDUCATION) {
                        EducationScreen(onBack = back)
                    }
                    composable(Routes.SETTINGS) {
                        SettingsScreen(
                            vm = vm,
                            settings = settings,
                            onBack = back,
                            onOpenSms = { open(Routes.SMS) }
                        )
                    }
                    composable(Routes.SMS) {
                        SmsCenterScreen(vm = vm, asToman = asToman, onBack = back)
                    }
                }
            }
        }
    }

    addType?.let { type ->
        AddTransactionSheet(
            vm = vm,
            asToman = asToman,
            initialType = type,
            onDismiss = { addType = null }
        )
    }
}

private data class BottomItem(val route: String, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Routes.HOME, "خانه", Icons.Rounded.Home),
    BottomItem(Routes.TRANSACTIONS, "تراکنش‌ها", Icons.Rounded.ReceiptLong),
    BottomItem(Routes.BUDGETS, "بودجه", Icons.Rounded.PieChart),
    BottomItem(Routes.REPORTS, "گزارش", Icons.Rounded.Insights),
    BottomItem(Routes.MORE, "بیشتر", Icons.Rounded.MoreHoriz)
)

@Composable
private fun PergarBottomBar(current: String, onSelect: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f))
    ) {
        DividerLine(alpha = 0.06f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            bottomItems.forEach { item ->
                val selected = item.route == current
                val color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelect(item.route) }
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = color,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = color
                    )
                }
            }
        }
    }
}
