package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.MahitaViewModel
import com.example.utils.JalaliCalendar
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    viewModel: MahitaViewModel,
    onBack: () -> Unit
) {
    var selectedFlow by remember { mutableStateOf("NONE") }
    val symptoms = remember { mutableStateListOf<String>() }
    val moods = remember { mutableStateListOf<String>() }
    var notes by remember { mutableStateOf("") }
    
    val today = LocalDate.now()
    val todayStr = today.toString()
    val jalaliToday = JalaliCalendar.gregorianToJalali(today)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ثبت علائم (${jalaliToday.formatLong()})", fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionTitle("شدت خونریزی")
            FlowSelector(selectedFlow) { selectedFlow = it }
            
            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("علائم جسمی")
            ChipGroup(
                items = listOf("درد شکم", "سردرد", "نفخ", "حساسیت سینه", "جوش"),
                selectedItems = symptoms
            ) { item, isSelected ->
                if (isSelected) symptoms.add(item) else symptoms.remove(item)
            }

            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("وضعیت روحی")
            ChipGroup(
                items = listOf("اضطراب", "افسردگی", "استرس", "انرژی پایین", "عدم تمرکز"),
                selectedItems = moods
            ) { item, isSelected ->
                if (isSelected) moods.add(item) else moods.remove(item)
            }

            Spacer(modifier = Modifier.height(32.dp))
            SectionTitle("یادداشت روزانه")
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                placeholder = { Text("امروز چطور بود؟", color = SleekTextLight) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SleekPrimary,
                    unfocusedBorderColor = SleekBorder,
                    focusedContainerColor = SleekSurface,
                    unfocusedContainerColor = SleekSurface,
                    focusedTextColor = SleekTextDark,
                    unfocusedTextColor = SleekTextMedium
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = {
                    viewModel.saveLog(
                        dateStr = todayStr,
                        flow = selectedFlow,
                        symptoms = symptoms,
                        moods = moods,
                        notes = notes
                    )
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekPrimary)
            ) {
                Text("ذخیره و بازگشت", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun FlowSelector(selected: String, onSelect: (String) -> Unit) {
    val options = listOf(
        "NONE" to "هیچ",
        "LIGHT" to "کم",
        "MEDIUM" to "متوسط",
        "HEAVY" to "زیاد"
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (key, label) ->
            val isSelected = selected == key
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(key) },
                label = { Text(label, style = MaterialTheme.typography.labelLarge) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SleekPrimary,
                    selectedLabelColor = Color.White,
                    containerColor = SleekSurface,
                    labelColor = SleekTextMedium
                ),
                shape = RoundedCornerShape(24.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = SleekBorder.copy(alpha = 0.5f),
                    selectedBorderColor = SleekPrimary
                ),
                modifier = Modifier.weight(1f).height(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChipGroup(
    items: List<String>,
    selectedItems: List<String>,
    onSelectionChange: (String, Boolean) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items.forEach { item ->
            val isSelected = selectedItems.contains(item)
            FilterChip(
                selected = isSelected,
                onClick = { onSelectionChange(item, !isSelected) },
                label = { Text(item, style = MaterialTheme.typography.labelLarge) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Indigo50,
                    selectedLabelColor = Indigo500,
                    containerColor = SleekSurface,
                    labelColor = SleekTextMedium
                ),
                shape = RoundedCornerShape(24.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = SleekBorder.copy(alpha = 0.5f),
                    selectedBorderColor = Indigo500
                ),
                modifier = Modifier.height(48.dp)
            )
        }
    }
}
