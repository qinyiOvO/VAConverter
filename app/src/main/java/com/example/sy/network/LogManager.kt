package com.example.sy.network

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

object LogManager {
    private const val MAX_LOG_SIZE = 1024 * 1024 // 1MB
    private const val TAG = "LogManager"
    private val logQueue = ConcurrentLinkedQueue<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private var logFile: File? = null

    fun initialize(context: Context) {
        try {
            // 在应用私有目录下创建日志文件
            val logDir = File(context.getExternalFilesDir(null), "logs")
            if (!logDir.exists()) {
                logDir.mkdirs()
            }

            // 创建新的日志文件，使用时间戳作为文件名
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            logFile = File(logDir, "app_log_$timestamp.txt")
            
            // 删除旧的日志文件
            cleanOldLogs(logDir)
            
            // 写入初始信息
            appendLog("应用启动")
            appendLog("设备信息：")
            appendLog("Android版本：${android.os.Build.VERSION.RELEASE}")
            appendLog("设备型号：${android.os.Build.MODEL}")
            appendLog("制造商：${android.os.Build.MANUFACTURER}")
        } catch (e: Exception) {
            Log.e(TAG, "初始化日志管理器失败", e)
        }
    }

    fun appendLog(message: String) {
        try {
            val timestamp = dateFormat.format(Date())
            val logMessage = "[$timestamp] $message"
            logQueue.offer(logMessage)
            
            logFile?.let { file ->
                FileWriter(file, true).use { writer ->
                    while (logQueue.isNotEmpty()) {
                        writer.append(logQueue.poll())
                        writer.append("\n")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "写入日志失败", e)
        }
    }

    fun getLogContent(): String {
        return try {
            logFile?.readText() ?: "无法读取日志文件"
        } catch (e: Exception) {
            Log.e(TAG, "读取日志失败", e)
            "读取日志失败：${e.message}"
        }
    }

    private fun cleanOldLogs(logDir: File) {
        try {
            val files = logDir.listFiles() ?: return
            if (files.size > 5) { // 最多保留5个日志文件
                files.sortBy { it.lastModified() }
                for (i in 0 until files.size - 5) {
                    files[i].delete()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "清理旧日志失败", e)
        }
    }
} 