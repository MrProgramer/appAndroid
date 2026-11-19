package ir.pergar.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.db.TxnEntity
import ir.pergar.app.data.insights.Analytics
import ir.pergar.app.data.insights.InsightEngine
import ir.pergar.app.data.model.InsightTone
import ir.pergar.app.data.model.TxnType
import ir.pergar.app.data.util.JalaliDate
import ir.pergar.app.data.util.formatCompactMoney
import ir.pergar.app.data.util.formatJalaliLong
import ir.pergar.app.data.util.formatJalaliShort
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.hasPermission
import ir.pergar.app.ui.AppViewModel
import ir.pergar.app.ui.Routes
import ir.pergar.app.ui.components.AnimatedAmount
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.EmptyState
import ir.pergar.app.ui.components.FeatureTile
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.GradientCard
import ir.pergar.app.ui.components.IncomeExpenseChart
import ir.pergar.app.ui.components.PergarLogo
import ir.pergar.app.ui.components.ScoreGauge
import ir.pergar.app.ui.components.SectionHeader
import ir.pergar.app.ui.components.StaggeredItem
import ir.pergar.app.ui.components.TransactionRow
import ir.pergar.app.ui.theme.PergarThemeInfo

@Composable
fun HomeScreen(
    vm: AppViewModel,
    asToman: Boolean,
    onOpen: (String) -> Unit,
    onAdd: (TxnType) -> Unit
) {
    val context = LocalContext.current
    val extras = PergarThemeInfo.colors
    val settings by vm.settings.collectAsState()
    val accounts by vm.accounts.collectAsState()
    val categories by vm.categories.collectAsState()
    val txns by vm.transactions.collectAsState()
    val budgets by vm.budgets.collectAsState()
    val loans by vm.loans.collectAsState()
    val debts by vm.debts.collectAsState()
    val goals by vm.goals.collectAsState()

    var smsGranted by remember {
        mutableStateOf(context.hasPermission(Manifest.permission.READ_SMS))
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        smsGranted = context.hasPermission(Manifest.permission.READ_SMS)
    }

    val today = remember { JalaliDate.today() }
    val range = remember(today) { Analytics.monthRange(today) }
    val income = remember(txns, range) { Analytics.sum(txns, TxnType.INCOME, range.first, range.second) }
    val expense = remember(txns, range) { Analytics.sum(txns, TxnType.EXPENSE, range.first, range.second) }
    val totalBalance = remember(accounts) { Analytics.totalBalance(accounts) }
    val flows = remember(txns, today) { Analytics.monthlyFlows(txns, 5, today) }
    val radar = remember(txns, categories, budgets, loans, debts, goals) {
        InsightEngine.analyze(txns, categories, budgets, loans, debts, goals)
    }
    val savings = income - expense
    val categoryMap = remember(categories) { categories.associateBy { it.id } }
    val accountMap = remember(accounts) { accounts.associateBy { it.id } }
    val recent = remember(txns) { txns.sortedByDescending(TxnEntity::dateEpochMillis).take(4) }
    val hidden = settings.hideBalance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PergarLogo(size = 42.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "پرگار",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = formatJalaliLong(System.currentTimeMillis()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = { onOpen(Routes.SMS) }) {
                Icon(
                    Icons.Rounded.Sms,
                    contentDescription = "مرکز پیامک",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onOpen(Routes.SETTINGS) }) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "تنظیمات",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(extras.gradientStart, extras.gradientEnd)))
                    .clickable { onAdd(TxnType.EXPENSE) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "ثبت تراکنش",
                    tint = Color.White
                )
            }
        }

        StaggeredItem(0) {
            GradientCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "موجودی کل",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = { vm.setHideBalance(!hidden) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (hidden) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = "نمایش موجودی",
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                if (hidden) {
                    Text(
                        text = "••••••",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    AnimatedAmount(
                        amountRial = totalBalance,
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        asToman = asToman
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (asToman) "تومان" else "ریال",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "درآمد این ماه",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = if (hidden) "•••" else formatCompactMoney(income, asToman),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                    Box(
                        Modifier
                            .width(1.dp)
                            .height(38.dp)
                            .background(Color.White.copy(alpha = 0.18f))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "هزینه این ماه",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = if (hidden) "•••" else formatCompactMoney(expense, asToman),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "پس‌انداز این ماه: ${if (hidden) "•••" else formatMoney(savings, withUnit = true, asToman = asToman)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = "${toPersianDigits(accounts.size.toString())} حساب",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (!smsGranted) {
            StaggeredItem(1) {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.RECEIVE_SMS,
                                Manifest.permission.READ_SMS,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        )
                    },
                    padding = 14.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Sms,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "خواندن پیامک فعال نیست",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "برای ثبت خودکار خریدها دسترسی بدهید",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            "فعال‌سازی",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        StaggeredItem(2) {
            GlassCard(
                modifier = Modifier.padding(horizontal = 20.dp),
                onClick = { onOpen(Routes.RADAR) },
                padding = 16.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ScoreGauge(score = radar.score, modifier = Modifier.size(86.dp), strokeWidth = 10.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "رادار مالی هوشمند",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val topInsight = radar.insights.firstOrNull()
                        Text(
                            text = topInsight?.title ?: "در حال تحلیل",
                            style = MaterialTheme.typography.bodySmall,
                            color = when (topInsight?.tone) {
                                InsightTone.POSITIVE -> extras.success
                                InsightTone.WARNING -> extras.warning
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = topInsight?.message ?: "با ثبت تراکنش‌ها، تحلیل دقیق‌تری ارائه می‌شود.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        Icons.Rounded.ChevronLeft,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader(
            title = "نگاه کلی",
            subtitle = "روند درآمد و هزینه شش ماه اخیر",
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(Modifier.height(10.dp))
        StaggeredItem(3) {
            GlassCard(modifier = Modifier.padding(horizontal = 20.dp)) {
                IncomeExpenseChart(flows = flows)
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader(
            title = "دسترسی سریع",
            modifier = Modifier.padding(horizontal = 20.dp),
            actionLabel = "همه امکانات",
            onAction = { onOpen(Routes.MORE) }
        )
        Spacer(Modifier.height(10.dp))
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureTile(
                    icon = ir.pergar.app.ui.components.iconForKey("wallet"),
                    title = "حساب‌ها",
                    subtitle = "نقدینگی و کارت‌ها",
                    accent = extras.gradientStart,
                    onClick = { onOpen(Routes.ACCOUNTS) },
                    modifier = Modifier.weight(1f)
                )
                FeatureTile(
                    icon = ir.pergar.app.ui.components.iconForKey("loan"),
                    title = "اقساط",
                    subtitle = "وام و سررسید",
                    accent = extras.gold,
                    onClick = { onOpen(Routes.LOANS) },
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureTile(
                    icon = ir.pergar.app.ui.components.iconForKey("people"),
                    title = "طلب و بدهی",
                    subtitle = "تسویه و پیگیری",
                    accent = extras.gradientEnd,
                    onClick = { onOpen(Routes.DEBTS) },
                    modifier = Modifier.weight(1f)
                )
                FeatureTile(
                    icon = ir.pergar.app.ui.components.iconForKey("flag"),
                    title = "اهداف مالی",
                    subtitle = "پس‌انداز هدفمند",
                    accent = extras.success,
                    onClick = { onOpen(Routes.GOALS) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionHeader(
            title = "تراکنش‌های اخیر",
            modifier = Modifier.padding(horizontal = 20.dp),
            actionLabel = "همه",
            onAction = { onOpen(Routes.TRANSACTIONS) }
        )
        Spacer(Modifier.height(8.dp))
        GlassCard(modifier = Modifier.padding(horizontal = 20.dp), padding = 12.dp) {
            if (recent.isEmpty()) {
                EmptyState(
                    icon = ir.pergar.app.ui.components.iconForKey("receipt"),
                    title = "هنوز تراکنشی ثبت نشده",
                    message = "اولین خرید یا درآمد خود را ثبت کنید یا پیامک‌های بانک را اسکن کنید.",
                    actionLabel = "ثبت تراکنش",
                    onAction = { onAdd(TxnType.EXPENSE) }
                )
            } else {
                recent.forEach { txn ->
                    TransactionRow(
                        txn = txn,
                        category = txn.categoryId?.let { categoryMap[it] },
                        accountName = txn.accountId?.let { accountMap[it]?.name },
                        asToman = asToman,
                        onClick = { onOpen(Routes.TRANSACTIONS) }
                    )
                }
            }
        }

        if (goals.isNotEmpty() && settings.onboardingDone) {
            Spacer(Modifier.height(16.dp))
            val goal = goals.maxByOrNull { if (it.targetRial > 0) it.savedRial.toFloat() / it.targetRial else 0f }
            if (goal != null) {
                GlassCard(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    onClick = { onOpen(Routes.GOALS) },
                    padding = 14.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CategoryIcon(
                            iconKey = goal.iconKey,
                            colorHex = goal.colorHex,
                            size = 40.dp,
                            iconSize = 20.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = "هدف «${goal.title}»",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تا ${formatJalaliShort(goal.targetEpochMillis ?: System.currentTimeMillis())} · ${formatCompactMoney(goal.targetRial - goal.savedRial, asToman)} مانده",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${toPersianDigits(((goal.savedRial.toFloat() / goal.targetRial.coerceAtLeast(1)) * 100).toInt().toString())}٪",
                            style = MaterialTheme.typography.titleSmall,
                            color = extras.success
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}
