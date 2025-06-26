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
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConversionManager(private val context: Context) {
    private val _conversionTasks = MutableStateFlow<List<ConversionTask>>(emptyList())
    val conversionTasks: StateFlow<List<ConversionTask>> = _conversionTasks.asStateFlow()
    private val prefs: SharedPreferences = context.getSharedPreferences("conversion_tasks", Context.MODE_PRIVATE)
    private val gson = Gson()

    init {
        // 启动时恢复任务
        val json = prefs.getString("tasks", null)
        if (json != null) {
            val type = object : TypeToken<List<ConversionTask>>() {}.type
            val list: List<ConversionTask> = gson.fromJson(json, type) ?: emptyList()
            // 未完成的任务标记为失败
            val updated = list.map {
                if (it.status == ConversionStatus.CONVERTING || it.status == ConversionStatus.WAITING) {
                    it.copy(status = ConversionStatus.FAILED)
                } else it
            }
            _conversionTasks.value = updated
        }
    }

    private fun persistTasks() {
        val json = gson.toJson(_conversionTasks.value)
        prefs.edit().putString("tasks", json).apply()
    }

    // 检查已完成任务的文件是否存在
    suspend fun checkExistingFiles() {
        withContext(Dispatchers.IO) {
            val tasks = _conversionTasks.value.toMutableList()
            val tasksToRemove = mutableListOf<Int>()
            
            tasks.forEachIndexed { index, task ->
                if (task.status == ConversionStatus.COMPLETED) {
                    val file = File(task.outputPath)
                    if (!file.exists()) {
                        tasksToRemove.add(index)
                    }
                }
            }
            
            // 从后往前删除，避免索引变化
            tasksToRemove.reversed().forEach { index ->
                tasks.removeAt(index)
            }
            
            _conversionTasks.value = tasks
            persistTasks()
        }
    }

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
                val outputFile = createOutputFile(fileName, outputFormat)

                // 创建转换任务
                val task = ConversionTask(
                    inputPath = inputPath,
                    outputPath = outputFile.absolutePath,
                    fileName = outputFile.name
                )

                // 添加到任务列表
                _conversionTasks.value = _conversionTasks.value + task
                persistTasks()
                Log.d("ConversionManager", "添加新任务: ${outputFile.name}")

                // 开始转换
                val command = "-i \"$inputPath\" -vn -acodec libmp3lame \"${outputFile.absolutePath}\""
                
                FFmpegKit.executeAsync(
                    command,
                    { session ->
                        when {
                            ReturnCode.isSuccess(session.returnCode) -> {
                                updateTaskStatus(task.id, ConversionStatus.COMPLETED)
                                Log.d("ConversionManager", "转换完成: ${outputFile.name}")
                            }
                            ReturnCode.isCancel(session.returnCode) -> {
                                updateTaskStatus(task.id, ConversionStatus.CANCELLED)
                                Log.d("ConversionManager", "转换取消: ${outputFile.name}")
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
            persistTasks()
            Log.d("ConversionManager", "更新任务状态: $taskId -> $status")
        }
    }

    private fun updateTaskProgress(taskId: String, progress: Float) {
        val tasks = _conversionTasks.value.toMutableList()
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            val task = tasks[index]
            val currentTime = System.currentTimeMillis()
            val elapsedSeconds = ((currentTime - task.startTime) / 1000).toInt()
            tasks[index] = task.copy(
                progress = progress,
                elapsedTime = elapsedSeconds
            )
            _conversionTasks.value = tasks
            persistTasks()
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

    private fun createOutputFile(fileName: String, outputFormat: String): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val baseFileName = fileName.substringBeforeLast(".")
        var outputFileName = "${baseFileName}_$timestamp.$outputFormat"
        
        val outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        
        var outputFile = File(outputDir, outputFileName)
        var counter = 1
        
        // 如果文件已存在，添加序号
        while (outputFile.exists()) {
            outputFileName = "${baseFileName}_${timestamp}_${counter}.$outputFormat"
            outputFile = File(outputDir, outputFileName)
            counter++
        }
        
        return outputFile
    }

    suspend fun deleteTask(taskId: String, deleteFile: Boolean) {
        withContext(Dispatchers.IO) {
            val tasks = _conversionTasks.value.toMutableList()
            val taskIndex = tasks.indexOfFirst { it.id == taskId }
            
            if (taskIndex != -1) {
                val task = tasks[taskIndex]
                
                if (deleteFile) {
                    // 删除输出文件
                    try {
                        File(task.outputPath).delete()
                    } catch (e: Exception) {
                        Log.e("ConversionManager", "删除文件失败: ${task.outputPath}", e)
                    }
                }
                
                // 从列表中移除任务
                tasks.removeAt(taskIndex)
                _conversionTasks.value = tasks
                persistTasks()
                Log.d("ConversionManager", "删除任务: $taskId, 删除文件: $deleteFile")
            }
        }
    }

    suspend fun renameTask(taskId: String, newName: String) {
        withContext(Dispatchers.IO) {
            val tasks = _conversionTasks.value.toMutableList()
            val taskIndex = tasks.indexOfFirst { it.id == taskId }
            
            if (taskIndex != -1) {
                val task = tasks[taskIndex]
                val oldFile = File(task.outputPath)
                
                if (oldFile.exists()) {
                    // 确保新文件名有正确的扩展名
                    val extension = oldFile.extension
                    val newNameWithExt = if (newName.endsWith(".$extension", ignoreCase = true)) {
                        newName
                    } else {
                        "$newName.$extension"
                    }
                    
                    // 创建新文件名，处理重名情况
                    var newFile = File(oldFile.parent, newNameWithExt)
                    var counter = 1
                    
                    while (newFile.exists() && newFile.absolutePath != oldFile.absolutePath) {
                        val nameWithoutExt = newNameWithExt.substringBeforeLast(".")
                        newFile = File(oldFile.parent, "${nameWithoutExt}_${counter}.$extension")
                        counter++
                    }
                    
                    try {
                        // 重命名文件
                        if (oldFile.renameTo(newFile)) {
                            // 更新任务信息
                            tasks[taskIndex] = task.copy(
                                fileName = newFile.name,
                                outputPath = newFile.absolutePath
                            )
                            _conversionTasks.value = tasks
                            persistTasks()
                            Log.d("ConversionManager", "重命名任务: $taskId, 新名称: ${newFile.name}")
                        } else {
                            Log.e("ConversionManager", "重命名文件失败: ${oldFile.absolutePath} -> ${newFile.absolutePath}")
                        }
                    } catch (e: Exception) {
                        Log.e("ConversionManager", "重命名过程出错", e)
                    }
                }
            }
        }
    }
} 