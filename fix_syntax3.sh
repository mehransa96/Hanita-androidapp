cat << 'INNER_EOF' > set_water.txt
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
INNER_EOF

sed -i '/fun decrementWater()/r set_water.txt' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
