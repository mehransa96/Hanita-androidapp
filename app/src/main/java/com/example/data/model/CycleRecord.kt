package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_records")
data class CycleRecord(
    @PrimaryKey
    val id: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val notes: String = "" // Overall cycle notes or concatenated daily notes
)
