sed -i '42d' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
sed -i '/val allLogs: StateFlow/a \    val waterGlasses: StateFlow<Int> = allLogs.map { logs -> logs.find { it.dateStr == LocalDate.now().toString() }?.waterIntake ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
