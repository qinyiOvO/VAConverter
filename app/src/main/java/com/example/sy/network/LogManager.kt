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
            
            // 写入初始信息（不包含隐私信息）
            appendLog("=== 应用启动日志 ===")
            appendLog("应用版本：1.0.0")
            appendLog("Android版本：${android.os.Build.VERSION.RELEASE}")
            appendLog("设备型号：${android.os.Build.MODEL}")
            appendLog("制造商：${android.os.Build.MANUFACTURER}")
            appendLog("日志说明：所有日志仅保存在本地，不包含任何个人隐私信息")
            appendLog("隐私保护：已启用敏感信息过滤")
            appendLog("=== 日志开始 ===")
        } catch (e: Exception) {
            Log.e(TAG, "初始化日志管理器失败", e)
        }
    }

    fun appendLog(message: String) {
        try {
            // 过滤掉可能包含隐私信息的内容
            val sanitizedMessage = sanitizeLogMessage(message)
            val timestamp = dateFormat.format(Date())
            val logMessage = "[$timestamp] $sanitizedMessage"
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

    /**
     * 清理日志消息，移除可能包含隐私信息的内容
     */
    private fun sanitizeLogMessage(message: String): String {
        var sanitized = message
        
        // 移除可能的文件路径中的用户名和具体路径
        sanitized = sanitized.replace(Regex("/storage/emulated/0/.*?/"), "/storage/emulated/0/***/")
        sanitized = sanitized.replace(Regex("/data/data/.*?/"), "/data/data/***/")
        sanitized = sanitized.replace(Regex("/sdcard/.*?/"), "/sdcard/***/")
        
        // 移除可能的邮箱地址
        sanitized = sanitized.replace(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"), "***@***.***")
        
        // 移除可能的手机号
        sanitized = sanitized.replace(Regex("1[3-9]\\d{9}"), "***-****-****")
        
        // 移除可能的身份证号
        sanitized = sanitized.replace(Regex("\\d{17}[\\dXx]"), "*******************")
        
        // 移除可能的银行卡号
        sanitized = sanitized.replace(Regex("\\d{16,19}"), "****************")
        
        // 移除可能的IP地址
        sanitized = sanitized.replace(Regex("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"), "***.***.***.***")
        
        // 移除可能的MAC地址
        sanitized = sanitized.replace(Regex("([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})"), "**:**:**:**:**:**")
        
        // 移除可能的IMEI号
        sanitized = sanitized.replace(Regex("\\d{15}"), "***************")
        
        // 移除可能的设备ID
        sanitized = sanitized.replace(Regex("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}"), "***-****-****-****-***")
        
        // 移除可能的文件名中的个人信息
        sanitized = sanitized.replace(Regex("\\b[A-Za-z0-9._%+-]+\\.[A-Za-z]{2,}\\b"), "***.***")
        
        // 移除可能的联系人姓名
        sanitized = sanitized.replace(Regex("联系人[：:].*"), "联系人：***")
        
        // 移除可能的地址信息
        sanitized = sanitized.replace(Regex("地址[：:].*"), "地址：***")
        
        return sanitized
    }

    fun getLogContent(): String {
        return try {
            logFile?.readText() ?: "无法读取日志文件"
        } catch (e: Exception) {
            Log.e(TAG, "读取日志失败", e)
            "读取日志失败：${e.message}"
        }
    }

    /**
     * 获取日志文件大小（用于调试）
     */
    fun getLogFileSize(): Long {
        return logFile?.length() ?: 0L
    }

    /**
     * 清理日志文件
     */
    fun clearLogs() {
        try {
            logFile?.delete()
            logQueue.clear()
            appendLog("日志已清理")
        } catch (e: Exception) {
            Log.e(TAG, "清理日志失败", e)
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

    /**
     * 记录应用功能使用（不包含具体文件信息）
     */
    fun logFeatureUsage(feature: String) {
        appendLog("功能使用：$feature")
    }

    /**
     * 记录错误信息（不包含具体文件路径）
     */
    fun logError(error: String, exception: Exception? = null) {
        appendLog("错误：$error")
        exception?.let {
            appendLog("异常类型：${it.javaClass.simpleName}")
            appendLog("异常消息：${it.message}")
        }
    }

    /**
     * 记录转换任务状态（不包含具体文件路径）
     */
    fun logConversionStatus(taskId: String, status: String, progress: Float? = null) {
        val progressText = progress?.let { "，进度：${(it * 100).toInt()}%" } ?: ""
        appendLog("转换任务[$taskId]状态：$status$progressText")
    }

    /**
     * 记录权限申请（不包含具体权限名称）
     */
    fun logPermissionRequest(permissionType: String, granted: Boolean) {
        appendLog("权限申请：$permissionType，结果：${if (granted) "已授权" else "已拒绝"}")
    }

    /**
     * 记录应用生命周期事件
     */
    fun logAppLifecycle(event: String) {
        appendLog("应用生命周期：$event")
    }

    /**
     * 记录性能相关事件（不包含具体数据）
     */
    fun logPerformance(event: String, duration: Long? = null) {
        val durationText = duration?.let { "，耗时：${it}ms" } ?: ""
        appendLog("性能事件：$event$durationText")
    }
} 