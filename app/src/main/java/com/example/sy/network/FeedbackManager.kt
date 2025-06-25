package com.example.sy.network

import android.content.Context
import android.content.SharedPreferences
import java.util.*

object FeedbackManager {
    private const val PREFS_NAME = "feedback_prefs"
    private const val KEY_FEEDBACK_TIMES = "feedback_times"
    private const val KEY_LAST_RESET_TIME = "last_reset_time"
    private const val MAX_FEEDBACK_PER_DAY = 3
    private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L // 24小时的毫秒数

    private lateinit var prefs: SharedPreferences

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        checkAndResetCounter()
    }

    fun canSubmitFeedback(): Boolean {
        checkAndResetCounter()
        val currentCount = getFeedbackCount()
        return currentCount < MAX_FEEDBACK_PER_DAY
    }

    fun recordFeedbackSubmission() {
        val currentCount = getFeedbackCount()
        prefs.edit().putInt(KEY_FEEDBACK_TIMES, currentCount + 1).apply()
        LogManager.appendLog("用户提交第 ${currentCount + 1} 次反馈")
    }

    fun getRemainingFeedbacks(): Int {
        checkAndResetCounter()
        return MAX_FEEDBACK_PER_DAY - getFeedbackCount()
    }

    private fun getFeedbackCount(): Int {
        return prefs.getInt(KEY_FEEDBACK_TIMES, 0)
    }

    private fun checkAndResetCounter() {
        val lastResetTime = prefs.getLong(KEY_LAST_RESET_TIME, 0L)
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastResetTime >= MILLIS_PER_DAY) {
            // 如果超过24小时，重置计数器
            prefs.edit()
                .putInt(KEY_FEEDBACK_TIMES, 0)
                .putLong(KEY_LAST_RESET_TIME, currentTime)
                .apply()
            LogManager.appendLog("重置反馈次数计数器")
        }
    }

    fun getNextResetTime(): Long {
        val lastResetTime = prefs.getLong(KEY_LAST_RESET_TIME, 0L)
        return lastResetTime + MILLIS_PER_DAY
    }
} 