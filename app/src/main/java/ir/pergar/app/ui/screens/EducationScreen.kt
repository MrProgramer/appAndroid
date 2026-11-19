package ir.pergar.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import ir.pergar.app.ui.components.CategoryIcon
import ir.pergar.app.ui.components.GlassCard
import ir.pergar.app.ui.components.ScreenFrame
import ir.pergar.app.ui.components.StaggeredItem
import ir.pergar.app.ui.theme.PergarThemeInfo

private data class Lesson(
    val title: String,
    val summary: String,
    val body: String,
    val iconKey: String,
    val colorHex: String
)

private val lessons = listOf(
    Lesson(
        title = "قانون ۵۰/۳۰/۲۰",
        summary = "ساده‌ترین چارچوب برای تقسیم درآمد ماهانه",
        body = "۵۰٪ درآمد برای نیازهای ضروری مثل اجاره، خوراک و قبوض.\n۳۰٪ برای خواسته‌ها مثل تفریح، سفر و رستوران.\n۲۰٪ برای پس‌انداز و سرمایه‌گذاری.\n\nاگر درآمد شما ثابت نیست، این نسبت‌ها را بر پایه میانگین سه ماه اخیر حساب کنید. در پرگار می‌توانید با بخش «گزارش‌ها» ببینید واقعاً این نسبت‌ها را رعایت می‌کنید یا نه.",
        iconKey = "chart",
        colorHex = "#4C7DFF"
    ),
    Lesson(
        title = "صندوق اضطراری",
        summary = "بالشتک مالی برای روزهای غیرمنتظره",
        body = "هدف: معادل ۳ تا ۶ ماه از هزینه‌های ضروری ماهانه.\n\nاین پول باید در دسترس و کم‌ریسک باشد (سپرده کوتاه‌مدت یا حساب جدا)، نه در دارایی پرنوسان.\n\nدر پرگار یک «هدف مالی» با عنوان صندوق اضطراری بسازید و هر ماه مبلغ ثابتی به آن اضافه کنید تا پیشرفت را ببینید.",
        iconKey = "savings",
        colorHex = "#22C55E"
    ),
    Lesson(
        title = "پس‌انداز قبل از خرج",
        summary = "اول به خودت پرداخت کن",
        body = "به‌جای اینکه آخر ماه هرچه ماند را پس‌انداز کنید، ابتدای ماه معادل پس‌انداز هدف را جدا کنید و بقیه را خرج کنید.\n\nمزیت: تصمیم‌گیری آسان‌تر و جلوگیری از خرج‌های ناخواسته.\n\nدر پرگار یک تراکنش انتقال به حساب پس‌انداز یا واریز به هدف ثبت کنید تا بودجه خرج شما واقع‌بینانه‌تر شود.",
        iconKey = "trend",
        colorHex = "#14B8A6"
    ),
    Lesson(
        title = "مدیریت بدهی",
        summary = "اول گران‌ترین بدهی را ببند",
        body = "دو روش اصلی:\n۱) گلوله برفی: کوچک‌ترین بدهی را زودتر تسویه کن تا انگیزه بگیری.\n۲) بهمن: بدهی با بالاترین نرخ بهره را اول پرداخت کن تا هزینه کمتری بدهی.\n\nهر قسط را در بخش «اقساط» ثبت کنید تا تاریخ سررسید بعدی را همیشه ببینید و دیرکرد نداشته باشید.",
        iconKey = "loan",
        colorHex = "#F97316"
    ),
    Lesson(
        title = "تورم و ارزش پول",
        summary = "عدد روی حساب ≠ قدرت خرید",
        body = "با تورم، قدرت خرید پول نقد کم می‌شود. نگه‌داشتن همه دارایی به‌صورت نقدی در بلندمدت زیان‌آور است.\n\nراهکار: بخشی نقد برای هزینه‌های جاری، بخشی در دارایی‌های مقاوم به تورم و بخشی در سرمایه‌گذاری با ریسک متناسب.\n\nدر بخش «رادار مالی» پرگار می‌توانید وضعیت نقدینگی و نرخ پس‌انداز خود را ببینید.",
        iconKey = "radar",
        colorHex = "#8B5CF6"
    ),
    Lesson(
        title = "سرمایه‌گذاری با پله",
        summary = "پله‌پله وارد شو، نه یک‌جا",
        body = "خرید دوره‌ای (میانگین‌گیری هزینه) ریسک زمان‌بندی را کاهش می‌دهد.\n\nقبل از سرمایه‌گذاری: صندوق اضطراری کامل، بدهی پرهزینه تسویه، اهداف کوتاه‌مدت جدا.\n\nهرگز پولی که تا کمتر از دو سال به آن نیاز دارید را در دارایی پرنوسان نگه ندارید.",
        iconKey = "target",
        colorHex = "#EC4899"
    ),
    Lesson(
        title = "پیامک بانکی هوشمند",
        summary = "ثبت خودکار تراکنش‌ها",
        body = "وقتی پیامک‌های بانکی را در پرگار اسکن کنید، برنامه مبلغ، نوع تراکنش و موجودی را می‌خواند.\n\nنکته: پیامک‌های «موجودی» را جدا از «برداشت» تشخیص می‌دهد و پیامک تکراری را دوباره ثبت نمی‌کند.\n\nپیامک‌ها فقط روی گوشی خودتان پردازش می‌شوند و به هیچ سروری ارسال نمی‌شوند.",
        iconKey = "sms",
        colorHex = "#F5B942"
    ),
    Lesson(
        title = "بودجه واقع‌بینانه",
        summary = "بر اساس گذشته، نه آرزو",
        body = "برای هر دسته، میانگین سه ماه گذشته را مبنا بگیرید و ۵ تا ۱۰ درصد کمتر هدف بگذارید.\n\nدر پرگار بودجه ماهانه هر دسته را مشخص کنید؛ کارت‌ها هنگام نزدیک شدن به سقف زرد و پس از عبور از بودجه قرمز می‌شوند.\n\nبازبینی ماهانه بودجه مهم‌تر از خودِ عدد است.",
        iconKey = "pie",
        colorHex = "#06B6D4"
    )
)

@Composable
fun EducationScreen(onBack: () -> Unit) {
    ScreenFrame(
        title = "آموزش مالی",
        subtitle = "هشت درس کوتاه برای مدیریت بهتر پول",
        onBack = onBack
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(lessons) { index, lesson ->
                StaggeredItem(index = index) {
                    LessonCard(lesson)
                }
            }
        }
    }
}

@Composable
private fun LessonCard(lesson: Lesson) {
    val extras = PergarThemeInfo.colors
    var expanded by remember { mutableStateOf(false) }
    GlassCard(padding = 14.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(spring(stiffness = 380f, dampingRatio = 0.85f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIcon(
                    iconKey = lesson.iconKey,
                    colorHex = lesson.colorHex,
                    size = 44.dp,
                    iconSize = 22.dp
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = lesson.summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Rounded.ExpandMore,
                    contentDescription = if (expanded) "بستن" else "باز کردن",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(if (expanded) 180f else 0f)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = lesson.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (!expanded) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = "برای مطالعه بزنید",
                style = MaterialTheme.typography.labelSmall,
                color = extras.gold
            )
        }
    }
}
