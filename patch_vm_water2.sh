cat << 'INNER_EOF' > water_funcs.txt
    fun incrementWater() {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            val existing = repository.getLogByDate(todayStr)
            val current = existing?.waterIntake ?: 0
            val newAmount = (current + 1).coerceAtMost(16)
            
            val newLog = existing?.copy(waterIntake = newAmount) ?: DailyLog(
                dateStr = todayStr,
                periodFlow = "NONE",
                symptoms = "",
                moods = "",
                notes = "",
                waterIntake = newAmount
            )
            repository.insertLog(newLog)
        }
    }

    fun decrementWater() {
        viewModelScope.launch {
            val todayStr = LocalDate.now().toString()
            val existing = repository.getLogByDate(todayStr)
            val current = existing?.waterIntake ?: 0
            val newAmount = (current - 1).coerceAtLeast(0)
            
            val newLog = existing?.copy(waterIntake = newAmount) ?: DailyLog(
                dateStr = todayStr,
                periodFlow = "NONE",
                symptoms = "",
                moods = "",
                notes = "",
                waterIntake = newAmount
            )
            repository.insertLog(newLog)
        }
    }
INNER_EOF

sed -i '/fun incrementWater() {/,/}/d' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
sed -i '/fun decrementWater() {/,/}/d' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt

# Wait, the previous sed command might not have caught the closing brace properly if there are nested blocks (though there aren't).
