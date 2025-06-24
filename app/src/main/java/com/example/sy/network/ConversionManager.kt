package com.example.sy.network

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegSession
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.Statistics
import com.arthenica.ffmpegkit.LogCallback
import com.arthenica.ffmpegkit.StatisticsCallback
import com.arthenica.ffmpegkit.SessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.provider.OpenableColumns
import java.io.FileOutputStream

class ConversionManager(private val context: Context) {
    private val _conversionTasks = MutableStateFlow<List<ConversionTask>>(emptyList())
    val conversionTasks: StateFlow<List<ConversionTask>> = _conversionTasks.asStateFlow()

    suspend fun startConversion(inputUri: Uri, outputFormat: String = "mp3") {
        withContext(Dispatchers.IO) {
            try {
                // 获取输入文件路径
                val inputPath = getPathFromUri(inputUri)
                if (inputPath == null) {
                    Log.e("ConversionManager", "无法获取文件路径")
                    return@withContext
                }

                // 获取文件名
                val fileName = getFileNameFromUri(inputUri)

                // 创建输出文件
                val outputFile = createOutputFile(inputPath, outputFormat)

                // 创建转换任务
                val task = ConversionTask(
                    inputPath = inputPath,
                    outputPath = outputFile.absolutePath,
                    fileName = fileName
                )

                // 添加到任务列表
                _conversionTasks.value = _conversionTasks.value + task
                Log.d("ConversionManager", "添加新任务: $fileName")

                // 开始转换
                val command = "-i \"$inputPath\" -vn -acodec libmp3lame \"${outputFile.absolutePath}\""
                
                FFmpegKit.executeAsync(
                    command,
                    { session ->
                        when {
                            ReturnCode.isSuccess(session.returnCode) -> {
                                updateTaskStatus(task.id, ConversionStatus.COMPLETED)
                                Log.d("ConversionManager", "转换完成: $fileName")
                            }
                            ReturnCode.isCancel(session.returnCode) -> {
                                updateTaskStatus(task.id, ConversionStatus.CANCELLED)
                                Log.d("ConversionManager", "转换取消: $fileName")
                            }
                            else -> {
                                updateTaskStatus(task.id, ConversionStatus.FAILED)
                                Log.e("ConversionManager", "转换失败: ${session.failStackTrace}")
                            }
                        }
                    },
                    { log ->
                        Log.d("ConversionManager", "FFmpeg日志: ${log.message}")
                    },
                    { statistics ->
                        // 更新进度
                        val timeInMs = statistics.time
                        val progress = (timeInMs.toDouble() / 1000.0).toFloat() // 转换为秒
                        updateTaskProgress(task.id, progress)
                    }
                )

            } catch (e: Exception) {
                Log.e("ConversionManager", "转换过程出错", e)
            }
        }
    }

    private fun updateTaskStatus(taskId: String, status: ConversionStatus) {
        val tasks = _conversionTasks.value.toMutableList()
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(status = status)
            _conversionTasks.value = tasks
            Log.d("ConversionManager", "更新任务状态: $taskId -> $status")
        }
    }

    private fun updateTaskProgress(taskId: String, progress: Float) {
        val tasks = _conversionTasks.value.toMutableList()
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(progress = progress)
            _conversionTasks.value = tasks
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "unknown"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    private fun getPathFromUri(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "content" -> {
                    // 创建临时文件
                    val tempFile = File(context.cacheDir, "temp_video_${System.currentTimeMillis()}")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(tempFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    tempFile.absolutePath
                }
                "file" -> uri.path
                else -> null
            }
        } catch (e: Exception) {
            Log.e("ConversionManager", "获取文件路径失败", e)
            null
        }
    }

    private fun createOutputFile(inputPath: String, outputFormat: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val inputFile = File(inputPath)
        val outputFileName = "${inputFile.nameWithoutExtension}_$timestamp.$outputFormat"
        
        val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        
        return File(outputDir, outputFileName)
    }
} 