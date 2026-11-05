# Remove the stuff from the end
sed -i '/fun setWaterGlasses/,/^    }$/d' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt

# Insert into the class (before the last brace of the class, wait, MahitaViewModel doesn't end the file! The file ends with data class PredictionInfo).
