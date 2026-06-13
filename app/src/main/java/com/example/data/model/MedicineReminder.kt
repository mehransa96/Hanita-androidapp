package com.example.data.model

data class MedicineReminder(
    val id: String,
    val name: String,
    val time: String,
    val dosage: String,
    val isEnabled: Boolean = true,
    val type: String = "pill" // "pill", "capsule", "drop", "injection", "tea"
)
