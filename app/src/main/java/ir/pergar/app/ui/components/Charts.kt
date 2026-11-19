package ir.pergar.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.model.MonthlyFlow
import ir.pergar.app.ui.theme.PergarThemeInfo
import kotlin.math.cos
import kotlin.math.sin

data class DonutSlice(val value: Long, val color: Color)

@Composable
fun DonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 22.dp,
    gapDegrees: Float = 3f,
    center: @Composable () -> Unit = {}
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val animatedProgress by animateFloatAsState(
        targetValue = if (slices.isEmpty()) 0f else 1f,
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "donutProgress"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            drawCircle(
                color = trackColor,
                radius = diameter / 2f,
                style = Stroke(width = stroke)
            )
            val total = slices.sumOf { it.value }.coerceAtLeast(1)
            var start = -90f
            slices.forEach { slice ->
                val fullSweep = (slice.value.toFloat() / total) * 360f
                val sweep = fullSweep * animatedProgress
                if (sweep > 0.5f) {
                    drawArc(
                        color = slice.color,
                        startAngle = start + gapDegrees / 2f,
                        sweepAngle = (sweep - gapDegrees).coerceAtLeast(0.5f),
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = Size(diameter, diameter)
                    )
                }
                start += sweep
            }
        }
        center()
    }
}

@Composable
fun IncomeExpenseChart(
    flows: List<MonthlyFlow>,
    modifier: Modifier = Modifier,
    height: Dp = 168.dp
) {
    val extras = PergarThemeInfo.colors
    val maxValue = (flows.maxOfOrNull { maxOf(it.incomeRial, it.expenseRial) } ?: 1L).coerceAtLeast(1L)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot("درآمد", extras.chartIncome)
            LegendDot("هزینه", extras.chartExpense)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            flows.forEach { flow ->
                val incomeFraction by animateFloatAsState(
                    targetValue = (flow.incomeRial.toFloat() / maxValue).coerceIn(0.015f, 1f),
                    animationSpec = tween(750, easing = FastOutSlowInEasing),
                    label = "incomeBar"
                )
                val expenseFraction by animateFloatAsState(
                    targetValue = (flow.expenseRial.toFloat() / maxValue).coerceIn(0.015f, 1f),
                    animationSpec = tween(750, easing = FastOutSlowInEasing),
                    label = "expenseBar"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(incomeFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(extras.chartIncome.copy(alpha = 0.85f))
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(expenseFraction)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(extras.chartExpense.copy(alpha = 0.9f))
                        )
                    }
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = flow.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SparklineChart(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val reveal by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(950, easing = FastOutSlowInEasing),
        label = "sparkReveal"
    )
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val maxValue = values.max().coerceAtLeast(1f)
        val minValue = values.min()
        val range = (maxValue - minValue).coerceAtLeast(1f)
        val stepX = size.width / (values.size - 1)
        val linePath = Path()
        val fillPath = Path()
        fillPath.moveTo(0f, size.height)
        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minValue) / range) * (size.height * 0.86f) - size.height * 0.05f
            if (index == 0) {
                linePath.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }
        fillPath.lineTo(size.width, size.height)
        fillPath.close()
        clipRect(right = size.width * reveal) {
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.26f), Color.Transparent)
                )
            )
            drawPath(
                path = linePath,
                color = color,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun ScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 16.dp
) {
    val extras = PergarThemeInfo.colors
    val color = when {
        score >= 70 -> extras.success
        score >= 40 -> extras.warning
        else -> extras.danger
    }
    val animated by animateFloatAsState(
        targetValue = (score.coerceIn(0, 100) / 100f),
        animationSpec = tween(1100, easing = FastOutSlowInEasing),
        label = "gauge"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val diameter = size.minDimension - stroke
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val startAngle = 135f
            val totalSweep = 270f
            drawArc(
                color = MaterialTheme.colorScheme.surfaceVariant,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = Size(diameter, diameter)
            )
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = totalSweep * animated,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = Size(diameter, diameter)
            )
            val radius = diameter / 2f
            val angle = Math.toRadians((startAngle + totalSweep * animated).toDouble())
            val dotCenter = Offset(
                center.x + radius * cos(angle).toFloat(),
                center.y + radius * sin(angle).toFloat()
            )
            drawCircle(color = color, radius = stroke * 0.38f, center = dotCenter)
            drawCircle(color = Color.White, radius = stroke * 0.16f, center = dotCenter)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = ir.pergar.app.data.util.toPersianDigits(score.toString()),
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "امتیاز رادار",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MiniBars(
    values: List<Float>,
    color: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        values.forEach { value ->
            val fraction by animateFloatAsState(
                targetValue = (value / maxValue).coerceIn(0.08f, 1f),
                animationSpec = tween(700, easing = FastOutSlowInEasing),
                label = "miniBar"
            )
            Box(
                modifier = Modifier
                    .width(7.dp)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.85f))
            )
        }
    }
}

@Composable
fun UsageBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "usage"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(50))
                .background(if (animated > 1f) color else color)
        )
    }
}
