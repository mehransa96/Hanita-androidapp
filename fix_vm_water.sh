sed -i '/fun setWaterGlasses/,/}/d' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
sed -i '/val waterGlasses/r water_funcs.txt' app/src/main/java/com/example/ui/viewmodel/MahitaViewModel.kt
