package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekBorder
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSurface
import com.example.ui.theme.SleekTextDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun QuickSymptomWidget(
    onSave: (mood: String, energy: String, sleep: String, pain: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMood by remember { mutableStateOf("") }
    var selectedEnergy by remember { mutableStateOf("") }
    var selectedSleep by remember { mutableStateOf("") }
    var selectedPain by remember { mutableStateOf("") }
    
    var isSaved by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    if (isSaved) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)), // Emerald 50
            border = BorderStroke(1.dp, Color(0xFFA7F3D0)), // Emerald 200
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Check, contentDescription = "ثبت شد", tint = Color(0xFF10B981), modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("ثبت سریع با موفقیت انجام شد!", color = Color(0xFF047857), fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SleekSurface),
        border = BorderStroke(1.dp, SleekBorder),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "ثبت سریع علائم امروز 📝",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextDark
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mood
            QuickSelectorRow(
                title = "خلق‌وخو",
                icon = Icons.Default.SentimentSatisfied,
                iconTint = Color(0xFFF59E0B),
                options = listOf("عالی" to "😄", "خوب" to "🙂", "معمولی" to "😐", "غمگین" to "😢", "عصبی" to "😠"),
                selected = selectedMood,
                onSelect = { selectedMood = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Energy
            QuickSelectorRow(
                title = "انرژی",
                icon = Icons.Default.Bolt,
                iconTint = Color(0xFF3B82F6),
                options = listOf("بالا" to "⚡", "متوسط" to "🔋", "پایین" to "🪫"),
                selected = selectedEnergy,
                onSelect = { selectedEnergy = it }
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Sleep
            QuickSelectorRow(
                title = "خواب",
                icon = Icons.Default.Bedtime,
                iconTint = Color(0xFF6366F1),
                options = listOf("عمیق" to "😴", "سبک" to "🥱", "بی‌خوابی" to "😵‍💫"),
                selected = selectedSleep,
                onSelect = { selectedSleep = it }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = SleekBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Pain
            QuickSelectorRow(
                title = "درد",
                icon = Icons.Default.LocalHospital,
                iconTint = Color(0xFFEF4444),
                options = listOf("ندارم" to "✅", "سردرد" to "🤕", "کرامپ" to "😣", "کمردرد" to "🦴"),
                selected = selectedPain,
                onSelect = { selectedPain = it }
            )

            Spacer(modifier = Modifier.height(20.dp))
            
            Button(
                onClick = {
                    onSave(selectedMood, selectedEnergy, selectedSleep, selectedPain)
                    isSaved = true
                    coroutineScope.launch {
                        delay(3000)
                        // Could reset or keep it saved state
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary),
                enabled = selectedMood.isNotBlank() || selectedEnergy.isNotBlank() || selectedSleep.isNotBlank() || selectedPain.isNotBlank()
            ) {
                Text("ثبت سریع در پرونده", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickSelectorRow(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.width(70.dp)) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(title, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            options.forEach { (text, emoji) ->
                val isSelected = selected == text
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) iconTint.copy(alpha = 0.15f) else Color.Transparent)
                        .clickable { onSelect(text) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(emoji, fontSize = 16.sp)
                        Text(
                            text, 
                            fontSize = 9.sp, 
                            color = if (isSelected) iconTint else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
