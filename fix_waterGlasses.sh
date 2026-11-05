sed -i '131d' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
sed -i '/initialValue = emptyList()/a \    )\n\n    val waterGlasses: StateFlow<Int> = allLogs.map { logs -> logs.find { it.dateStr == LocalDate.now().toString() }?.waterIntake ?: 0 }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
sed -i '132,136s/.*)//' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt # cleanup the extra paren if needed, wait, I'll just remove 131 and insert after 135 safely.
