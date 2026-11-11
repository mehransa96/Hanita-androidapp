package com.hanita.partner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.utils.PersianNumbers
import com.hanita.core.gentleman.GentlemanGuide
import com.hanita.core.gentleman.GentlemanGuide.Audience
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
    else -> Icons.AutoMirrored.Filled.TrendingUp
}

/** The gentleman's guide: pillars that fit the user's status, and each pillar's principles. */
@Composable
internal fun PathScreen(
    padding: PaddingValues,
    status: Status,
    pillarId: String?,
    onOpen: (String) -> Unit,
    onBack: () -> Unit
) {
    val pillar = pillarId?.let { GentlemanGuide.pillar(it) }?.takeIf { it.fits(status) }
    if (pillar != null) {
        BackHandler(onBack = onBack)
        PillarDetail(padding, pillar, onBack)
        return
    }
    val pillars = GentlemanGuide.pillarsFor(status)
    val shared = pillars.filter { it.audience == Audience.ALL }
    val special = pillars.filter { it.audience != Audience.ALL }
    ScreenList(padding) {
        item { Wordmark("مسیر جنتلمن") }
        item {
            MutedText("جنتلمن بودن یعنی احترام، مسئولیت و آرامش؛ در رفتار، ظاهر، رابطه و زندگی. هر محور را باز کن و هر روز یک قدم بردار.")
        }
        item { Label(if (status == Status.MARRIED) "ویژه متاهل‌ها" else "ویژه مجردها") }
        special.forEach { p -> item(key = p.id) { PillarCard(p) { onOpen(p.id) } } }
        item { Label("برای همه") }
        shared.forEach { p -> item(key = p.id) { PillarCard(p) { onOpen(p.id) } } }
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
                Text(
                    "${PersianNumbers.toPersianDigits(pillar.principles.size)} اصل · ${PersianNumbers.toPersianDigits(pillar.challenges.size)} چالش",
                    fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PillarDetail(padding: PaddingValues, pillar: GentlemanGuide.Pillar, onBack: () -> Unit) {
    ScreenList(padding) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                }
                Column(Modifier.weight(1f)) {
                    Text(pillar.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(pillar.subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                }
                IconTile(pillarIcon(pillar.id))
            }
        }
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
    }
}
