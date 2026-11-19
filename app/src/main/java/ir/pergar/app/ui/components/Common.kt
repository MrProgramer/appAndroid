package ir.pergar.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.pergar.app.data.util.formatMoney
import ir.pergar.app.ui.theme.PergarThemeInfo
import kotlinx.coroutines.delay

@Composable
fun PergarBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val extras = PergarThemeInfo.colors
    val background = MaterialTheme.colorScheme.background
    val transition = rememberInfiniteTransition(label = "ambient")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ambientShift"
    )
    Box(modifier.fillMaxSize().background(background)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val blueRadius = w * 0.95f
            val cyanRadius = w * 0.8f
            val goldRadius = w * 0.55f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        extras.gradientStart.copy(alpha = if (extras.isDark) 0.20f else 0.10f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.12f, h * (0.10f + 0.06f * t)),
                    radius = blueRadius
                ),
                radius = blueRadius,
                center = Offset(w * 0.12f, h * (0.10f + 0.06f * t))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        extras.gradientEnd.copy(alpha = if (extras.isDark) 0.16f else 0.08f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.95f, h * (0.62f - 0.05f * t)),
                    radius = cyanRadius
                ),
                radius = cyanRadius,
                center = Offset(w * 0.95f, h * (0.62f - 0.05f * t))
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        extras.gold.copy(alpha = if (extras.isDark) 0.10f else 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.85f, h * (0.04f + 0.03f * t)),
                    radius = goldRadius
                ),
                radius = goldRadius,
                center = Offset(w * 0.85f, h * (0.04f + 0.03f * t))
            )
        }
        content()
    }
}

@Composable
fun Modifier.pressScale(interactionSource: MutableInteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "pressScale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val extras = PergarThemeInfo.colors
    val shape = RoundedCornerShape(24.dp)
    val surfaceColor = if (extras.isDark) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
    } else {
        Color.White.copy(alpha = 0.92f)
    }
    val interaction = remember { MutableInteractionSource() }
    var cardModifier = modifier
        .clip(shape)
        .background(surfaceColor)
        .border(1.dp, extras.glassBorder, shape)
    if (onClick != null) {
        cardModifier = cardModifier
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .pressScale(interaction)
    }
    Column(
        modifier = cardModifier.padding(padding),
        content = content
    )
}

@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    colors: List<Color>? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val extras = PergarThemeInfo.colors
    val gradient = colors ?: listOf(extras.gradientStart, extras.gradientEnd)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(gradient))
            .padding(20.dp),
        content = content
    )
}

@Composable
fun PergarButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    val extras = PergarThemeInfo.colors
    val interaction = remember { MutableInteractionSource() }
    val gradient = if (enabled) {
        Brush.horizontalGradient(listOf(extras.gradientStart, extras.gradientEnd))
    } else {
        Brush.horizontalGradient(
            listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
    val contentColor = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .pressScale(interaction)
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val extras = PergarThemeInfo.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressScale(interaction)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, extras.glassBorder, RoundedCornerShape(16.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAction)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun AnimatedAmount(
    amountRial: Long,
    style: TextStyle,
    color: Color,
    asToman: Boolean = true,
    animated: Boolean = true,
    modifier: Modifier = Modifier
) {
    val target = if (asToman) (amountRial / 10).toFloat() else amountRial.toFloat()
    val animatedValue by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "amountValue"
    )
    val value = if (animated) animatedValue else target
    val displayRial = if (asToman) (value.toLong() * 10) else value.toLong()
    Text(
        text = formatMoney(displayRial, withUnit = false, asToman = asToman),
        style = style,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val extras = PergarThemeInfo.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(extras.gradientStart.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(34.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(18.dp))
            GhostButton(text = actionLabel, onClick = onAction)
        }
    }
}

@Composable
fun StaggeredItem(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 45L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(380)) + slideInVertically(
            animationSpec = tween(380, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 5 }
        )
    ) {
        content()
    }
}

@Composable
fun PergarLogo(size: Dp, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val gold = PergarThemeInfo.colors.gold
    val ink = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension / 2f
        drawCircle(color = primary.copy(alpha = 0.14f), radius = r)
        drawCircle(
            color = gold.copy(alpha = 0.55f),
            radius = r * 0.94f,
            style = Stroke(width = r * 0.025f)
        )
        drawCircle(
            color = primary.copy(alpha = 0.9f),
            radius = r * 0.76f,
            style = Stroke(width = r * 0.09f)
        )

        val top = Path().apply {
            moveTo(center.x, center.y - r * 0.66f)
            lineTo(center.x + r * 0.21f, center.y - r * 0.04f)
            lineTo(center.x, center.y - r * 0.18f)
            lineTo(center.x - r * 0.21f, center.y - r * 0.04f)
            close()
        }
        drawPath(top, color = gold)

        val bottom = Path().apply {
            moveTo(center.x, center.y + r * 0.66f)
            lineTo(center.x + r * 0.21f, center.y + r * 0.04f)
            lineTo(center.x, center.y + r * 0.18f)
            lineTo(center.x - r * 0.21f, center.y + r * 0.04f)
            close()
        }
        drawPath(bottom, color = ink.copy(alpha = 0.55f))

        drawCircle(color = Color.White, radius = r * 0.12f)
        drawCircle(color = Color(0xFF0B1220), radius = r * 0.05f)
    }
}

@Composable
fun SplashScreen() {
    val extras = PergarThemeInfo.colors
    val transition = rememberInfiniteTransition(label = "splash")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        extras.gradientStart.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.background
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.graphicsLayer { scaleX = pulse; scaleY = pulse }) {
                PergarLogo(size = 120.dp)
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "پرگار",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "مدیریت هوشمند امور مالی شخصی",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(30.dp))
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun InfoPill(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.labelLarge, color = color)
        }
    }
}

@Composable
fun FeatureTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.height(132.dp),
        onClick = onClick,
        padding = 14.dp
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    height: Dp = 8.dp
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "progress"
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
fun DividerLine(modifier: Modifier = Modifier, alpha: Float = 0.08f) {
    Spacer(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
    )
}

@Composable
fun LabeledValueRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TinyTag(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    )
}

@Composable
fun StrokeRing(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(900, easing = FastOutSlowInEasing),
        label = "ring"
    )
    Canvas(modifier = modifier) {
        val stroke = size.minDimension * 0.12f
        drawCircle(
            color = trackColor,
            radius = (size.minDimension - stroke) / 2f,
            style = Stroke(width = stroke)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = animated * 360f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
            size = androidx.compose.ui.geometry.Size(
                size.minDimension - stroke,
                size.minDimension - stroke
            ),
            topLeft = Offset(stroke / 2f, stroke / 2f)
        )
    }
}

@Composable
fun WideInfoCard(
    icon: ImageVector,
    title: String,
    message: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(accent.copy(alpha = 0.10f))
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.widthIn(max = 520.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = accent)
            Spacer(Modifier.height(2.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
