cat << 'INNER_EOF' > button_code.txt

            if (selectedDate != null) {
                val localDateStr = selectedDate!!.toLocalDate().toString()
                Button(
                    onClick = {
                        viewModel.updateCycleSettings(localDateStr, averageCycleLength)
                        android.widget.Toast.makeText(context, "تاریخ شروع پریود به $localDateStr تغییر یافت.", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Icon(androidx.compose.material.icons.Icons.Filled.EditCalendar, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تنظیم این روز به عنوان شروع پریود", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
INNER_EOF

sed -i '/Spacer(modifier = Modifier.height(8.dp))/r button_code.txt' app/src/main/java/com/example/ui/screens/CalendarScreen.kt
