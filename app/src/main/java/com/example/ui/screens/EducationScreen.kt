package com.example.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.bounceClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationScreen(onNavigateBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("آموزش و سلامت", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        val articles = getEducationalArticles()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.MenuBook, contentDescription = "آموزش", tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("مطالب آموزش سلامت زنان", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("منابع معتبر علمی برای آشنایی بیشتر، راه‌های تغذیه و تمرینات ورزشی اصولی و حرفه‌ای در زمان‌های سیکل", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=0.8f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            items(articles) { article ->
                ArticleCard(article)
            }
            
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

data class Article(
    val title: String,
    val phase: String,
    val summary: String,
    val nutrition: String,
    val exercise: String,
    val color: Color
)

fun getEducationalArticles(): List<Article> {
    return listOf(
        Article(
            title = "مرحله قاعدگی (Menstrual)",
            phase = "روز ۱ تا ۵",
            summary = "در این مرحله سطح هورمون‌ها در پایین‌ترین حد خود قرار دارد. استراحت و بازسازی انرژی در این فاز بیش از هر چیز اهمیت دارد.",
            nutrition = "توصیه می‌شود مصرف غذاهای غنی از آهن مانند گوشت قرمز بدون چربی و اسفناج را افزایش دهید. مصرف منابع ویتامین C همراه این غذاها به جذب بهتر آهن کمک می‌کند. برای کاهش انقباضات و تسکین درد، مایعات و نوشیدنی‌های گرم مانند دمنوش بابونه، زنجبیل و دارچین مصرف کنید.",
            exercise = "استراحت کافی داشته باشید. تمرینات بسیار سبک مانند یوگای مدیتیشن‌گونه، حرکات کششی، و پیاده‌روی کوتاه و آهسته مفید است. از انجام تمرینات با شدت بالا در روزهای اول که خونریزی شدید است پرهیز کنید.",
            color = Color(0xFFF43F5E) // Rose 500
        ),
        Article(
            title = "مرحله فولیکولی (Follicular)",
            phase = "روز ۶ تا ۱۴",
            summary = "پایان یافتن خونریزی همراه با افزایش پیوسته سطح استروژن است که معمولاً باعث بالا رفتن انرژی، تمرکز بهتر و بهبود خلق‌وخو می‌شود.",
            nutrition = "بدن برای بازسازی نیاز به مواد مغذی دارد. پروتئین‌های بدون چربی (مانند سینه مرغ و ماهی)، کربوهیدرات‌های پیچیده مثل جو دوسر و کینوا، و غذاهای سرشار از پروبیوتیک مانند سبزیجات تخمیری و ماست یونانی پیشنهاد می‌شود.",
            exercise = "انرژی در حال افزایش است. این زمان عالی برای انجام ورزش‌های هوازی، دویدن، دوچرخه‌سواری، و همچنین شروع تمرینات قدرتی و یادگیری مهارت‌های جدید ورزشی است.",
            color = Color(0xFF10B981) // Emerald 500
        ),
        Article(
            title = "مرحله تخمک‌گذاری (Ovulation)",
            phase = "حدود روز ۱۴",
            summary = "در این دوران انرژی شما در بالاترین سطح ممکن است. هورمون‌های استروژن و لوتئینی‌کننده (LH) به پیک خود می‌رسند و ممکن است احساس قدرت، شادابی و اعتماد به نفس بیشتری داشته باشید.",
            nutrition = "رژیم غذایی باید سرشار از آنتی‌اکسیدان باشد تا به کاهش استرس اکسیداتیو کمک کند. توت‌ها، کلم بروکلی، مغزها، دانه‌ها (به ویژه بادام و گردو) و سبزیجات برگ‌سبز. نوشیدن آب فراوان در این فاز بسیار ضروری است.",
            exercise = "از انرژی اوج خود نهایت بهره را ببرید. بهترین زمان برای تمرینات پرفشار مانند HIIT (تمرینات تناوبی با شدت بالا)، وزنه‌برداری سنگین و شرکت در ورزش‌های گروهی پویا است.",
            color = Color(0xFF8B5CF6) // Violet 500
        ),
        Article(
            title = "مرحله لوتئال (Luteal)",
            phase = "روز ۱۵ تا ۲۸",
            summary = "ترشح پروژسترون بالا می‌رود که می‌تواند باعث کاهش سطح انرژی، تمایل به استراحت بیشتر، افت خلق‌وخو، نفخ و علائم سندروم پیش از قاعدگی (PMS) در روزهای پایانی شود.",
            nutrition = "برای تعادل قند خون، وعده‌های غذایی کوچک‌تر و پرشمار مصرف کنید. غذاهای غنی از منیزیم (مثل شکلات تلخ بالای ۷۰٪ و تخم کدو) به کاهش گرفتگی‌های عضلانی کمک می‌کنند. مصرف غذاهای دارای B6 (مثل موز و سیب‌زمینی) برای بهبود خلق‌وخو موثر است. از مصرف مقادیر زیاد نمک و شکر که باعث احتباس آب و تشدید نفخ می‌شوند، به شدت بپرهیزید.",
            exercise = "با پیشروی در این مرحله، شدت ورزش را به تدریج کاهش دهید. بدن به سمت استراحت می‌رود. بازگشت به تمرینات ملایم‌تر، پیلاتس، شنا، تای‌چی، و یوگای ترمیمی در این زمان بسیار مناسب است.",
            color = Color(0xFFF59E0B) // Amber 500
        )
    )
}

@Composable
fun ArticleCard(article: Article) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(12.dp).background(article.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = article.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = article.phase, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = "بیشتر",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.3f))
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("خلاصه:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(article.summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("تغذیه مناسب:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(article.nutrition, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text("ورزش پیشنهادی:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(article.exercise, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
