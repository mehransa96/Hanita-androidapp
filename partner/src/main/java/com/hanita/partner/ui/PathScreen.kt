package com.hanita.partner.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.gentleman.GentlemanGuide.Category
import com.hanita.core.gentleman.Sources
import com.hanita.core.gentleman.Status

internal fun pillarIcon(id: String): ImageVector = when (id) {
    "manners" -> Icons.Filled.EmojiEvents
    "talk" -> Icons.Filled.Forum
    "style" -> Icons.Filled.Checkroom
    "health" -> Icons.Filled.FitnessCenter
    "money" -> Icons.Filled.AccountBalanceWallet
    "mind" -> Icons.Filled.Psychology
    "dating" -> Icons.Filled.LocalCafe
    "choosing" -> Icons.Filled.People
    "solo" -> Icons.Filled.Home
    "marriage" -> Icons.Filled.Favorite
    "conflict" -> Icons.Filled.SelfImprovement
    "cycle" -> Icons.Filled.Spa
    "esteem" -> Icons.Filled.HealthAndSafety
    "presence" -> Icons.Filled.Face
    "calm" -> Icons.Filled.Air
    "work" -> Icons.Filled.Timeline
    "friends" -> Icons.Filled.Contacts
    "father" -> Icons.Filled.ChildCare
    else -> Icons.AutoMirrored.Filled.TrendingUp
}

/** The gentleman's library: five categories, each with the pillars that fit the user's status. */
@Composable
internal fun PathScreen(padding: PaddingValues, status: Status, actions: AppActions) {
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    val pillars = GentlemanGuide.pillarsFor(status)
    val categories = Category.values().filter { c -> pillars.any { it.category == c } }
    ScreenList(padding) {
        item { Wordmark("مسیر جنتلمن") }
        item {
            MutedText(
                "جنتلمن بودن یعنی احترام، مسئولیت و آرامش؛ در رفتار، ظاهر، رابطه و زندگی. " +
                    "محتوا بر پایه کتاب‌ها، پژوهش‌ها و سازمان‌های معتبر است و منبع هر محور زیر آن آمده."
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(selected = filter == null, onClick = { filter = null }, label = { Text("همه") }) }
                categories.forEach { c ->
                    item {
                        FilterChip(
                            selected = filter == c.name,
                            onClick = { filter = if (filter == c.name) null else c.name },
                            label = { Text(c.title) }
                        )
                    }
                }
            }
        }
        categories.filter { filter == null || it.name == filter }.forEach { category ->
            item(key = "cat-${category.name}") {
                Column(Modifier) {
                    Label(category.title)
                    Text(category.subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            pillars.filter { it.category == category }.forEach { p ->
                item(key = p.id) { PillarCard(p) { actions.openPillar(p.id) } }
            }
        }
        item(key = "sources") {
            Panel(Modifier.clickable(role = Role.Button) { actions.open(Routes.SOURCES) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.AutoMirrored.Filled.MenuBook)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("منابع علمی برنامه", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        MutedText("${PersianNumbers.toPersianDigits(Sources.ALL.size)} کتاب، مقاله و راهنمای معتبر")
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun PillarCard(pillar: GentlemanGuide.Pillar, onClick: () -> Unit) {
    Panel(Modifier.clickable(role = Role.Button, onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(pillarIcon(pillar.id))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(pillar.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(pillar.subtitle, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val tag = when (pillar.audience) {
                    GentlemanGuide.Audience.ALL -> ""
                    GentlemanGuide.Audience.SINGLE -> " · ویژه مجردها"
                    GentlemanGuide.Audience.MARRIED -> " · ویژه متاهل‌ها"
                }
                Text(
                    "${PersianNumbers.toPersianDigits(pillar.principles.size)} اصل · ${PersianNumbers.toPersianDigits(pillar.challenges.size)} چالش$tag",
                    fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
internal fun PillarScreen(padding: PaddingValues, status: Status, pillarId: String, actions: AppActions) {
    val pillar = GentlemanGuide.pillar(pillarId)?.takeIf { it.fits(status) }
    if (pillar == null) {
        androidx.compose.runtime.LaunchedEffect(pillarId) { actions.back() }
        return
    }
    ScreenList(padding) {
        item {
            Wordmark(pillar.title, onBack = actions.back) { IconTile(pillarIcon(pillar.id)) }
        }
        item { Text("${pillar.category.title} · ${pillar.subtitle}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary) }
        pillar.principles.forEachIndexed { i, principle ->
            item(key = "p$i") {
                Panel {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            PersianNumbers.toPersianDigits(i + 1),
                            fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(principle.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(principle.text, fontSize = 14.5.sp, lineHeight = 24.sp)
                }
            }
        }
        item(key = "challenges") {
            Panel {
                Label("چالش‌های این محور")
                pillar.challenges.forEach { Bullet(it) }
            }
        }
        item(key = "sources") { SourcesPanel(Sources.forPillar(pillar.id), "بر پایه این منابع") }
    }
}

@Composable
internal fun SourcesScreen(padding: PaddingValues, actions: AppActions) {
    val context = LocalContext.current
    ScreenList(padding) {
        item { Wordmark("منابع علمی", onBack = actions.back) }
        item {
            MutedText(
                "محتوای $APP_NAME از این کتاب‌ها، پژوهش‌های داوری‌شده و راهنمای سازمان‌های بهداشتی گرفته و به زبان ساده بازنویسی شده است. " +
                    "این برنامه آموزشی است و جای پزشک، روان‌شناس یا مشاور را نمی‌گیرد. منبع‌های دارای لینک در مرورگر باز می‌شوند."
            )
        }
        Sources.ALL.forEach { source ->
            item(key = source.id) {
                Panel { SourceRow(source) { url -> openUrl(context, url) } }
            }
        }
    }
}
