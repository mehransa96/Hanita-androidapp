package com.example

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.example.ai.HanitaAi
import com.example.utils.RescheduleWorker
import com.google.firebase.FirebaseApp

class HanitaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        installCrashRecorder()
        initFirebase()
        HanitaAi.init(this)
        // Daily safety net that keeps every reminder scheduled, even if the app is never opened.
        RescheduleWorker.schedulePeriodic(this)
    }

    private fun initFirebase() {
        try {
            val app = FirebaseApp.getApps(this).firstOrNull() ?: FirebaseApp.initializeApp(this)
            if (app == null) {
                Log.i(TAG, "Firebase is not configured; running in offline-only mode.")
                return
            }
            AppCheckInstaller.install(app)
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialisation failed; running in offline-only mode.", e)
        }
    }

    /**
     * Stores the last crash locally (never uploaded) so the recovery screen can offer a reset.
     * Only the exception type and stack frames are kept: no user data is written.
     */
    @android.annotation.SuppressLint("ApplySharedPref") // the process is about to die; write synchronously
    private fun installCrashRecorder() {
        val original = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val trace = throwable.stackTrace.take(25).joinToString("\n") { "  at $it" }
                getSharedPreferences(CRASH_PREFS, Context.MODE_PRIVATE).edit(commit = true) {
                    putString(KEY_LAST_CRASH, "${throwable.javaClass.name}\n$trace")
                }
            } catch (_: Exception) {
            }
            original?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        private const val TAG = "HanitaApplication"
        const val CRASH_PREFS = "crash_reports"
        const val KEY_LAST_CRASH = "last_crash"
    }
}
