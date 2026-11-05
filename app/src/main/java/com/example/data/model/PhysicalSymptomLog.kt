package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "physical_symptom_logs")
data class PhysicalSymptomLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // Format: "YYYY-MM-DD"
    val symptomName: String, // e.g. "سردرد", "خستگی", "دل‌پیچه و درد شکم", "کمردرد", "نفخ"
    val severity: String, // "MILD", "MODERATE", "SEVERE"
    val severityScore: Int = 1, // 1: خفیف, 2: متوسط, 3: شدید
    val category: String = "عمومی", // e.g. "سر و اعصاب", "شکم و لگن", "عضلات و فیزیک", "گوارش و متابولیسم"
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
