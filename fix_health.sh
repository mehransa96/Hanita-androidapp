cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt

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
