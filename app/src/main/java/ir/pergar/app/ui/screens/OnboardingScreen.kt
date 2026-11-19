package ir.pergar.app.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.pergar.app.data.util.toPersianDigits
import ir.pergar.app.hasPermission
import ir.pergar.app.ui.components.GhostButton
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.InfoPill
import ir.pergar.app.ui.components.PergarBackground
import ir.pergar.app.ui.components.PergarButton
import ir.pergar.app.ui.components.PergarLogo
import ir.pergar.app.ui.theme.PergarThemeInfo

private data class OnboardingFeature(
    val title: String,
    val description: String
)

private val features = listOf(
    OnboardingFeature("ثبت خودکار از پیامک بانک", "خرید، برداشت و موجودی همه بانک‌ها بدون تایپ دستی شناسایی می‌شود."),
    OnboardingFeature("تصویر کامل پول شما", "بودجه، اقساط، طلب و بدهی، اهداف و تقسیم هزینه در یک جا."),
    OnboardingFeature("رادار مالی هوشمند", "تحلیل عادت‌های خرج، هشدار بودجه و گزارش‌های شمسی خوانا."),
    OnboardingFeature("همه‌چیز روی گوشی شما", "اطلاعات مالی شما آفلاین و خصوصی می‌ماند؛ بدون ارسال به سرور.")
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val extras = PergarThemeInfo.colors
    var page by remember { mutableIntStateOf(0) }
    var smsGranted by remember {
        mutableStateOf(
            context.hasPermission(Manifest.permission.READ_SMS) &&
                context.hasPermission(Manifest.permission.RECEIVE_SMS)
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.READ_SMS] == true ||
            context.hasPermission(Manifest.permission.READ_SMS)
        smsGranted = granted && context.hasPermission(Manifest.permission.RECEIVE_SMS)
    }

    PergarBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(18.dp))
            PergarLogo(size = 92.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                text = "پرگار",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "مدیریت هوشمند امور مالی شخصی",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(26.dp))

            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInHorizontally(tween(300)) { if (targetState > initialState) -it / 4 else it / 4 })
                        .togetherWith(fadeOut(tween(180)))
                },
                label = "onboarding"
            ) { currentPage ->
                if (currentPage == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        features.forEach { feature ->
                            GlassCard(padding = 14.dp) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(Modifier.size(10.dp))
                                    Column {
                                        Text(
                                            feature.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            feature.description,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassCard(padding = 16.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(extras.gradientStart.copy(alpha = 0.14f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Rounded.Sms,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.size(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        "دسترسی به پیامک بانک‌ها",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "برای شناسایی خودکار تراکنش‌ها و موجودی، اجازه خواندن پیامک لازم است.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (smsGranted) {
                                    InfoPill("فعال", extras.success)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            PergarButton(
                                text = if (smsGranted) "دسترسی پیامک فعال است" else "اجازه دسترسی به پیامک",
                                onClick = {
                                    permissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.RECEIVE_SMS,
                                            Manifest.permission.READ_SMS,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        )
                                    )
                                },
                                enabled = !smsGranted
                            )
                        }

                        GlassCard(padding = 16.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = extras.gold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    "حریم خصوصی شما اولویت ماست",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "همه داده‌ها فقط روی همین گوشی ذخیره می‌شوند. هیچ اطلاعاتی به اینترنت ارسال نمی‌شود و می‌توانید هر زمان پشتیبان تهیه کنید.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        GlassCard(padding = 16.dp) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Notifications,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(Modifier.size(10.dp))
                                Text(
                                    "یادآور اقساط و کارها",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "برای دریافت یادآور سررسید اقساط و کارها، اعلان‌ها را فعال کنید. این کار اختیاری است.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(2) { index ->
                    Box(
                        modifier = Modifier
                            .size(width = if (index == page) 22.dp else 8.dp, height = 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == page) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
                            )
                    )
                }
            }
            Spacer(Modifier.height(18.dp))
            PergarButton(
                text = if (page == 0) "ادامه" else "شروع مدیریت مالی",
                onClick = {
                    if (page == 0) page = 1 else onFinish()
                }
            )
            Spacer(Modifier.height(8.dp))
            GhostButton(
                text = "رد کردن راهنما",
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "نسخه ${toPersianDigits("1.0.0")} · کوروش پرگار",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
