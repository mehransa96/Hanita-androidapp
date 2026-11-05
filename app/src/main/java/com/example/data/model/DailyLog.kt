package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey val dateStr: String, // format "YYYY-MM-DD"
    val periodFlow: String, // "NONE", "LIGHT", "MEDIUM", "HEAVY"
    val symptoms: String, // comma separated symptoms e.g., "Cramps,Headache"
    val moods: String, // comma separated
    val notes: String,
    val emoji: String = "",
    val takenMeds: String = "",
    val waterIntake: Int = 0,
    val bbt: Float? = null,
    val weight: Float? = null,
    /** Epoch millis of the last local edit; used to resolve cloud-sync conflicts (newest wins). */
    val updatedAt: Long = 0L
)
