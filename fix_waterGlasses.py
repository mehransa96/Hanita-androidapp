with open("app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt", "r") as f:
    lines = f.readlines()

# find and delete the bad line
for i, line in enumerate(lines):
    if "val waterGlasses: StateFlow<Int> = allLogs.map" in line:
        del lines[i]
        break

# find the closing paren for allLogs
for i, line in enumerate(lines):
    if "val allLogs: StateFlow<List<DailyLog>>" in line:
        # find the end of this statement
        end_idx = i
        while "    )" not in lines[end_idx] and end_idx < i + 10:
            end_idx += 1
        
        lines.insert(end_idx + 1, "    val waterGlasses: StateFlow<Int> = allLogs.map { logs -> logs.find { it.dateStr == LocalDate.now().toString() }?.waterIntake ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)\n")
        break

with open("app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt", "w") as f:
    f.writelines(lines)
