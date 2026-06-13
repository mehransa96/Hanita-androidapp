package com.example.data.repository

import android.util.Log
import com.example.data.dao.DailyLogDao
import com.example.data.model.DailyLog
import kotlinx.coroutines.flow.Flow

class LogRepository(private val dailyLogDao: DailyLogDao) {
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
}
