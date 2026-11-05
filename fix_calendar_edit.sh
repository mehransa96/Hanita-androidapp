sed -i 's/val coroutineScope = rememberCoroutineScope()/val coroutineScope = rememberCoroutineScope()\n    val context = androidx.compose.ui.platform.LocalContext.current/' app/src/main/java/com/example/ui/screens/CalendarScreen.kt
sed -i 's/Icons.Filled.EditCalendar/Icons.Filled.Edit/g' app/src/main/java/com/example/ui/screens/CalendarScreen.kt
