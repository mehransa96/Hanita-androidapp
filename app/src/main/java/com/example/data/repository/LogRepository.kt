package com.example.data.repository

import android.util.Log
import com.example.data.dao.DailyLogDao
import com.example.data.dao.PhysicalSymptomDao
import com.example.data.model.DailyLog
import com.example.data.model.PhysicalSymptomLog
import kotlinx.coroutines.flow.Flow

class LogRepository(
    private val dailyLogDao: DailyLogDao,
    private val physicalSymptomDao: PhysicalSymptomDao? = null
) {
    val allLogs: Flow<List<DailyLog>> = dailyLogDao.getAllLogs()
    val periodDays: Flow<List<DailyLog>> = dailyLogDao.getPeriodDays()

    fun getLogByDateFlow(dateStr: String): Flow<DailyLog?> {
        return dailyLogDao.getLogByDateFlow(dateStr)
    }

    suspend fun getLogByDate(dateStr: String): DailyLog? {
        return dailyLogDao.getLogByDate(dateStr)
    }

    suspend fun insertLog(log: DailyLog) {
        dailyLogDao.insertLog(log)
    }

    suspend fun deleteLog(dateStr: String) {
        dailyLogDao.deleteLogByDate(dateStr)
    }

    // Physical Symptoms Methods (Room Local Database)
    val allPhysicalSymptoms: Flow<List<PhysicalSymptomLog>>? = physicalSymptomDao?.getAllSymptoms()

    fun getPhysicalSymptomsForDate(dateStr: String): Flow<List<PhysicalSymptomLog>> {
        return physicalSymptomDao?.getSymptomsForDate(dateStr) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun getPhysicalSymptomsListForDate(dateStr: String): List<PhysicalSymptomLog> {
        return physicalSymptomDao?.getSymptomsListForDate(dateStr) ?: emptyList()
    }

    suspend fun insertPhysicalSymptoms(symptoms: List<PhysicalSymptomLog>) {
        physicalSymptomDao?.insertSymptoms(symptoms)
    }

    suspend fun insertPhysicalSymptom(symptom: PhysicalSymptomLog): Long {
        return physicalSymptomDao?.insertSymptom(symptom) ?: -1L
    }

    suspend fun deletePhysicalSymptomsForDate(dateStr: String) {
        physicalSymptomDao?.deleteSymptomsForDate(dateStr)
    }

    suspend fun deletePhysicalSymptomById(id: Long) {
        physicalSymptomDao?.deleteSymptomById(id)
    }
}

