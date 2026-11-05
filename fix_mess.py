import re

with open("app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt", "r") as f:
    content = f.read()

# I will find the class end (last '}' of the class)
# First, remove ALL `fun saveLog` blocks to be safe.
import sys

def remove_block(text, start_str):
    while True:
        idx = text.find(start_str)
        if idx == -1:
            break
        # Find the matching closing brace for this function block
        brace_count = 0
        in_block = False
        end_idx = -1
        for i in range(idx, len(text)):
            if text[i] == '{':
                brace_count += 1
                in_block = True
            elif text[i] == '}':
                brace_count -= 1
                if in_block and brace_count == 0:
                    end_idx = i
                    break
        if end_idx != -1:
            text = text[:idx] + text[end_idx+1:]
        else:
            break
    return text

content = remove_block(content, 'fun saveLog(')
content = remove_block(content, 'fun setWaterGlasses(')

# Now find where the class MahitaViewModel ends
brace_count = 0
in_class = False
class_end_idx = -1
for i, char in enumerate(content):
    if content[i:i+21] == "class MahitaViewModel":
        in_class = True
    
    if in_class:
        if char == '{':
            brace_count += 1
        elif char == '}':
            brace_count -= 1
            if brace_count == 0:
                class_end_idx = i
                break

if class_end_idx != -1:
    to_insert = """
    fun saveLog(dateStr: String, flow: String, symptoms: List<String>, moods: List<String>, notes: String, emoji: String = "", takenMeds: String = "") {
        viewModelScope.launch {
            val existing = repository.getLogByDate(dateStr)
            val log = DailyLog(
                dateStr = dateStr,
                periodFlow = flow,
                symptoms = symptoms.joinToString(","),
                moods = moods.joinToString(","),
                notes = notes,
                emoji = emoji,
                takenMeds = takenMeds,
                waterIntake = existing?.waterIntake ?: 0
            )
            repository.insertLog(log)
            
            // Sync this single log immediately to Firestore
            val name = userName.value
            if (name.isNotBlank()) {
                firestoreManager.uploadLog(name, log)
            }
        }
    }

    fun setWaterGlasses(count: Int) {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            val existing = repository.getLogByDate(todayStr)
            val newLog = existing?.copy(waterIntake = count) ?: DailyLog(
                dateStr = todayStr,
                periodFlow = "NONE",
                symptoms = "",
                moods = "",
                notes = "",
                waterIntake = count
            )
            repository.insertLog(newLog)
        }
    }
"""
    content = content[:class_end_idx] + to_insert + content[class_end_idx:]

with open("app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt", "w") as f:
    f.write(content)

