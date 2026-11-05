cat << 'INNER_EOF' > newsavelog.txt
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
INNER_EOF

sed -i '/fun saveLog(/,/^    }/d' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
sed -i '/fun addCustomPhysicalSymptom/r newsavelog.txt' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
