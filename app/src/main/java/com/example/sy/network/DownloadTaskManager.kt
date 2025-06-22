package com.example.sy.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DownloadTaskManager {
    private val _downloadingTasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val downloadingTasks: StateFlow<List<DownloadTask>> = _downloadingTasks.asStateFlow()

    private val _completedTasks = MutableStateFlow<List<DownloadTask>>(emptyList())
    val completedTasks: StateFlow<List<DownloadTask>> = _completedTasks.asStateFlow()

    private const val PREF_NAME = "download_tasks"
    private const val KEY_COMPLETED_TASKS = "completed_tasks"
    private val gson = Gson()

    fun addDownloadingTask(task: DownloadTask) {
        _downloadingTasks.value = _downloadingTasks.value + task
    }

    fun updateDownloadingTask(task: DownloadTask) {
        _downloadingTasks.value = _downloadingTasks.value.map { 
            if (it.id == task.id) task else it 
        }
    }

    fun removeDownloadingTask(taskId: String) {
        _downloadingTasks.value = _downloadingTasks.value.filter { it.id != taskId }
    }

    fun addCompletedTask(task: DownloadTask) {
        _completedTasks.value = _completedTasks.value + task
    }

    fun removeCompletedTask(taskId: String) {
        _completedTasks.value = _completedTasks.value.filter { it.id != taskId }
    }

    fun saveCompletedTasks(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val taskJson = gson.toJson(_completedTasks.value)
        prefs.edit().putString(KEY_COMPLETED_TASKS, taskJson).apply()
    }

    fun loadCompletedTasks(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val taskJson = prefs.getString(KEY_COMPLETED_TASKS, "[]") ?: "[]"
        val type = object : TypeToken<List<DownloadTask>>() {}.type
        try {
            _completedTasks.value = gson.fromJson(taskJson, type)
        } catch (e: Exception) {
            _completedTasks.value = emptyList()
        }
    }
} 