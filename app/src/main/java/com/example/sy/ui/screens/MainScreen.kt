package com.example.sy.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.sy.ui.theme.SYTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.FileProvider
import java.io.FileOutputStream
import java.io.IOException
import okhttp3.Request
import okhttp3.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.Exception
import android.Manifest
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import okhttp3.OkHttpClient
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.TopAppBarDefaults

// 下载任务状态
enum class DownloadStatus {
    DOWNLOADING, PAUSED, COMPLETED, FAILED
}

// 下载任务数据类
data class DownloadTask(
    val id: String,
    val fileName: String,
    var status: DownloadStatus,
    var progress: Float = 0f,
    var speed: String = "",
    var errorMessage: String = "",
    var totalSize: String = "",
    var remainingTime: String = "",
    var filePath: String = "",
    var downloadedBytes: Long = 0L,
    var totalBytes: Long = 0L,
    var lastDownloadedBytes: Long = 0L,
    var lastDownloadPosition: Long = 0L,  // 添加断点位置记录
    var resumeSupported: Boolean = true    // 添加是否支持断点续传标记
)

// 删除确认对话框数据类
data class DeleteConfirmationState(
    val task: DownloadTask? = null,
    val isVisible: Boolean = false
)

// 文件位置对话框状态
data class FileLocationDialogState(
    val isVisible: Boolean = false,
    val filePath: String = ""
)

// 添加下载任务持久化存储
object DownloadTaskManager {
    private const val PREF_NAME = "download_tasks"
    private const val KEY_COMPLETED_TASKS = "completed_tasks"
    private val gson = Gson()

    fun saveCompletedTasks(context: Context, tasks: List<DownloadTask>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val taskJson = gson.toJson(tasks)
        prefs.edit().putString(KEY_COMPLETED_TASKS, taskJson).apply()
    }

    fun loadCompletedTasks(context: Context): List<DownloadTask> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val taskJson = prefs.getString(KEY_COMPLETED_TASKS, "[]") ?: "[]"
        val type = object : TypeToken<List<DownloadTask>>() {}.type
        return try {
            gson.fromJson(taskJson, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
}

// 权限管理器
object PermissionManager {
    fun checkStoragePermissions(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
}

// 网络状态监听器
class NetworkMonitor(private val context: Context) {
    private val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    private var onNetworkAvailable: (() -> Unit)? = null
    private var onNetworkLost: (() -> Unit)? = null

    fun startMonitoring(onAvailable: () -> Unit, onLost: () -> Unit) {
        onNetworkAvailable = onAvailable
        onNetworkLost = onLost

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onNetworkAvailable?.invoke()
            }

            override fun onLost(network: Network) {
                onNetworkLost?.invoke()
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        networkCallback?.let { callback ->
            connectivityManager.registerNetworkCallback(networkRequest, callback)
        }
    }

    fun stopMonitoring() {
        networkCallback?.let { callback ->
            connectivityManager.unregisterNetworkCallback(callback)
        }
        networkCallback = null
    }

    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var linkInput by remember { mutableStateOf("") }
    
    // 状态变量
    var showProgressDialog by remember { mutableStateOf(false) }
    var progressDialogMessage by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(FileLocationDialogState()) }
    var showNetworkDialog by remember { mutableStateOf(false) }
    
    // 抽屉状态
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    
    // 取消确认对话框状态
    var cancelConfirmation by remember { mutableStateOf<DownloadTask?>(null) }
    
    // 下载任务列表
    var downloadingTasks by remember { mutableStateOf(listOf<DownloadTask>()) }
    var completedTasks by remember { mutableStateOf(listOf<DownloadTask>()) }
    
    // 临时通知状态
    var tempNotification by remember { mutableStateOf<Pair<String, DownloadTask?>?>(null) }
    
    // 删除确认对话框状态
    var deleteConfirmation by remember { mutableStateOf(DeleteConfirmationState()) }

    // 下载任务Job管理
    val downloadJobs = remember { mutableMapOf<String, Boolean>() }
    
    // 权限对话框
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionMessage by remember { mutableStateOf("") }
    var currentAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    
    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            currentAction?.invoke()
        } else {
            showPermissionDialog = true
            permissionMessage = "需要存储权限才能继续操作"
        }
    }

    // 网络状态监听
    val networkMonitor = remember { NetworkMonitor(context) }
    var isNetworkAvailable by remember { mutableStateOf(networkMonitor.isNetworkAvailable()) }

    // 加载已完成的任务
    LaunchedEffect(Unit) {
        completedTasks = DownloadTaskManager.loadCompletedTasks(context)
    }
    
    // 保存已完成的任务
    DisposableEffect(completedTasks) {
        onDispose {
            DownloadTaskManager.saveCompletedTasks(context, completedTasks)
        }
    }

    // 处理临时通知的消失
    LaunchedEffect(tempNotification) {
        if (tempNotification != null) {
            delay(2000) // 2秒后自动消失
            tempNotification = null
        }
    }

    // 处理进度对话框超时
    LaunchedEffect(showProgressDialog) {
        if (showProgressDialog) {
            delay(10000) // 10秒超时
            if (showProgressDialog) {
                showProgressDialog = false
                tempNotification = "处理超时" to null
            }
        }
    }
    
    // 启动网络监听
    DisposableEffect(Unit) {
        networkMonitor.startMonitoring(
            onAvailable = { isNetworkAvailable = true },
            onLost = { isNetworkAvailable = false }
        )
        onDispose {
            networkMonitor.stopMonitoring()
        }
    }

    LaunchedEffect(isNetworkAvailable) {
        showNetworkDialog = !isNetworkAvailable
    }
    
    if (showNetworkDialog) {
        AlertDialog(
            onDismissRequest = { showNetworkDialog = false },
            title = { Text("网络连接断开") },
            text = { Text("请检查网络连接并重试") },
            confirmButton = {
                TextButton(onClick = { showNetworkDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                SettingsScreen(
                    onBackClick = {
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    onAboutClick = onSettingsClick
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("视音下载转换器") },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "设置",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // 链接输入区域
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = linkInput,
                            onValueChange = { linkInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("粘贴分享链接") },
                            singleLine = true
                        )
                        // TODO: 需要添加粘贴图标
                        TextButton(onClick = { /* TODO: 实现粘贴功能 */ }) {
                            Text("粘贴")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 操作按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { /* TODO: 实现视频下载 */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        // TODO: 需要添加视频下载图标
                        Text("视频下载")
                    }
                    
                    Button(
                        onClick = { /* TODO: 实现音频下载 */ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFC107)
                        )
                    ) {
                        // TODO: 需要添加音频下载图标
                        Text("音频下载")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 测试按钮区域
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "测试功能区",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )
                        
                        // 网络状态显示
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "网络状态：${if (isNetworkAvailable) "正常" else "断开"}",
                                color = if (isNetworkAvailable) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                            TextButton(
                                onClick = { 
                                    isNetworkAvailable = !isNetworkAvailable
                                    if (!isNetworkAvailable) {
                                        // 如果断开网络，将所有正在下载的任务标记为失败
                                        downloadingTasks = downloadingTasks.map { task ->
                                            if (task.status == DownloadStatus.DOWNLOADING) {
                                                downloadJobs[task.id] = false
                                                task.copy(
                                                    status = DownloadStatus.FAILED,
                                                    errorMessage = "网络连接中断",
                                                    lastDownloadPosition = task.downloadedBytes  // 保存断点位置
                                                )
                                            } else task
                                        }
                                        tempNotification = "网络已断开，已保存下载进度" to null
                                    } else {
                                        tempNotification = "网络已恢复，可以继续下载" to null
                                    }
                                }
                            ) {
                                Text(if (isNetworkAvailable) "断开网络" else "恢复网络")
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        Button(
                            onClick = {
                                if (!isNetworkAvailable) {
                                    tempNotification = "网络未连接，无法开始下载" to null
                                    return@Button
                                }
                                scope.launch {
                                    // 模拟添加下载任务
                                    val newTask = DownloadTask(
                                        id = System.currentTimeMillis().toString(),
                                        fileName = "测试视频.mp4",
                                        status = DownloadStatus.DOWNLOADING,
                                        progress = 0f,
                                        speed = "1.2MB/s",
                                        totalSize = "56MB",
                                        remainingTime = "剩余 01:30"
                                    )
                                    downloadingTasks = downloadingTasks + newTask
                                    downloadJobs[newTask.id] = true
                                    
                                    // 模拟下载进度
                                    var progress = 0f
                                    var retryCount = 0
                                    val maxRetries = 3
                                    
                                    while (progress < 1f && downloadJobs[newTask.id] == true) {
                                        delay(500)
                                        // 检查网络状态
                                        if (!isNetworkAvailable) {
                                            retryCount++
                                            if (retryCount <= maxRetries) {
                                                // 等待网络恢复
                                                while (!isNetworkAvailable && downloadJobs[newTask.id] == true) {
                                                    delay(1000) // 每秒检查一次网络状态
                                                }
                                                // 如果任务被取消，直接返回
                                                if (downloadJobs[newTask.id] != true) {
                                                    return@launch
                                                }
                                                // 网络恢复后继续下载
                                                continue
                                            }
                                        
                                            // 超过最大重试次数
                                            downloadingTasks = downloadingTasks.map { task ->
                                                if (task.id == newTask.id) {
                                                    task.copy(
                                                        status = DownloadStatus.FAILED,
                                                        errorMessage = "网络连接不稳定，请稍后重试"
                                                    )
                                                } else task
                                            }
                                            tempNotification = "下载失败：网络连接不稳定" to newTask
                                            return@launch
                                        }

                                        progress += 0.1f
                                        downloadingTasks = downloadingTasks.map { task ->
                                            if (task.id == newTask.id && task.status == DownloadStatus.DOWNLOADING) {
                                                task.copy(progress = progress.coerceAtMost(1f))
                                            } else task
                                        }
                                    }
                                    
                                    // 检查是否是因为取消而结束
                                    if (downloadJobs[newTask.id] == false) {
                                        return@launch
                                    }
                                    
                                    // 检查是否已达到100%进度
                                    if (progress >= 1f) {
                                        // 下载完成，移动到已完成列表
                                        downloadingTasks = downloadingTasks.filter { it.id != newTask.id }
                                        completedTasks = completedTasks + newTask.copy(
                                            status = DownloadStatus.COMPLETED,
                                            progress = 1f,
                                            speed = "0KB/s",
                                            remainingTime = "已完成"
                                        )
                                        tempNotification = "下载完成" to newTask.copy(status = DownloadStatus.COMPLETED)
                                        downloadJobs.remove(newTask.id)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("测试下载任务")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 下载列表区域
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    var selectedTab by remember { mutableStateOf(0) }
                    
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 标签栏
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("下载中") }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("已完成") }
                            )
                        }

                        // 列表内容
                        when (selectedTab) {
                            0 -> DownloadingList(
                                tasks = downloadingTasks,
                                onPause = { task ->
                                    downloadingTasks = downloadingTasks.map { 
                                        if (it.id == task.id) it.copy(status = DownloadStatus.PAUSED)
                                        else it
                                    }
                                    downloadJobs[task.id] = false
                                    tempNotification = "已暂停下载" to task
                                },
                                onResume = { task ->
                                    if (!isNetworkAvailable) {
                                        tempNotification = "网络未连接，无法继续下载" to task
                                        return@DownloadingList
                                    }
                                    downloadingTasks = downloadingTasks.map { 
                                        if (it.id == task.id) it.copy(status = DownloadStatus.DOWNLOADING)
                                        else it
                                    }
                                    downloadJobs[task.id] = true
                                    // 重新开始下载
                                    scope.launch {
                                        var progress = task.progress
                                        var retryCount = 0
                                        val maxRetries = 3
                                        
                                        while (progress < 1f && downloadJobs[task.id] == true) {
                                            delay(500)
                                            // 检查网络状态
                                            if (!isNetworkAvailable) {
                                                retryCount++
                                                if (retryCount <= maxRetries) {
                                                    // 等待网络恢复
                                                    while (!isNetworkAvailable && downloadJobs[task.id] == true) {
                                                        delay(1000) // 每秒检查一次网络状态
                                                    }
                                                    // 如果任务被取消，直接返回
                                                    if (downloadJobs[task.id] != true) {
                                                        return@launch
                                                    }
                                                    // 网络恢复后继续下载
                                                    continue
                                                }
                                            
                                                // 超过最大重试次数
                                                downloadingTasks = downloadingTasks.map { task ->
                                                    if (task.id == task.id) {
                                                        task.copy(
                                                            status = DownloadStatus.FAILED,
                                                            errorMessage = "网络连接不稳定，请稍后重试"
                                                        )
                                                    } else task
                                                }
                                                tempNotification = "下载失败：网络连接不稳定" to task
                                                return@launch
                                            }

                                            progress += 0.1f
                                            downloadingTasks = downloadingTasks.map { t ->
                                                if (t.id == task.id && t.status == DownloadStatus.DOWNLOADING) {
                                                    t.copy(progress = progress.coerceAtMost(1f))
                                                } else t
                                            }
                                        }
                                        
                                        // 检查是否是因为取消而结束
                                        if (downloadJobs[task.id] == false) {
                                            return@launch
                                        }
                                        
                                        // 检查是否已达到100%进度
                                        if (progress >= 1f) {
                                            // 下载完成，移动到已完成列表
                                            downloadingTasks = downloadingTasks.filter { it.id != task.id }
                                            completedTasks = completedTasks + task.copy(
                                                status = DownloadStatus.COMPLETED,
                                                progress = 1f,
                                                speed = "0KB/s",
                                                remainingTime = "已完成"
                                            )
                                            tempNotification = "下载完成" to task.copy(status = DownloadStatus.COMPLETED)
                                            downloadJobs.remove(task.id)
                                        }
                                    }
                                },
                                onCancel = { task -> 
                                    cancelConfirmation = task
                                },
                                onRetry = { task ->
                                    scope.launch {
                                        // 先检查网络是否真正可用
                                        var networkChecks = 0
                                        while (networkChecks < 3) {
                                            if (!isNetworkAvailable) {
                                                delay(1000) // 等待1秒再检查
                                                networkChecks++
                                                if (networkChecks == 3) {
                                                    tempNotification = "网络未连接，无法重试下载" to task
                                                    return@launch
                                                }
                                                continue
                                            }
                                            break
                                        }

                                        // 更新任务状态为下载中，保持已下载进度
                                        downloadingTasks = downloadingTasks.map { 
                                            if (it.id == task.id) {
                                                it.copy(
                                                    status = DownloadStatus.DOWNLOADING,
                                                    progress = if (it.resumeSupported) it.progress else 0f,
                                                    errorMessage = "",
                                                    downloadedBytes = if (it.resumeSupported) it.lastDownloadPosition else 0L
                                                )
                                            }
                                            else it
                                        }
                                        downloadJobs[task.id] = true

                                        // 重新开始下载，从断点处继续
                                        var progress = if (task.resumeSupported) task.progress else 0f
                                        var retryCount = 0
                                        val maxRetries = 3
                                        
                                        while (progress < 1f && downloadJobs[task.id] == true) {
                                            delay(500)
                                            // 检查网络状态
                                            if (!isNetworkAvailable) {
                                                retryCount++
                                                if (retryCount <= maxRetries) {
                                                    // 等待网络恢复
                                                    var waitCount = 0
                                                    while (!isNetworkAvailable && downloadJobs[task.id] == true && waitCount < 10) {
                                                        delay(1000) // 每秒检查一次网络状态
                                                        waitCount++
                                                    }
                                                    // 如果等待超时或任务被取消，直接返回
                                                    if (!isNetworkAvailable || downloadJobs[task.id] != true) {
                                                        downloadingTasks = downloadingTasks.map { t ->
                                                            if (t.id == task.id) {
                                                                t.copy(
                                                                    status = DownloadStatus.FAILED,
                                                                    errorMessage = if (!isNetworkAvailable) 
                                                                        "网络连接不稳定，请稍后重试" 
                                                                    else 
                                                                        "下载已取消"
                                                                )
                                                            } else t
                                                        }
                                                        tempNotification = if (!isNetworkAvailable)
                                                            "下载失败：网络连接不稳定" to task
                                                        else
                                                            "下载已取消" to task
                                                        return@launch
                                                    }
                                                    // 网络恢复后继续下载
                                                    continue
                                                } else {
                                                    // 超过最大重试次数
                                                    downloadingTasks = downloadingTasks.map { t ->
                                                        if (t.id == task.id) {
                                                            t.copy(
                                                                status = DownloadStatus.FAILED,
                                                                errorMessage = "网络连接不稳定，请稍后重试"
                                                            )
                                                        } else t
                                                    }
                                                    tempNotification = "下载失败：网络连接不稳定" to task
                                                    return@launch
                                                }
                                            }

                                            progress += 0.1f
                                            downloadingTasks = downloadingTasks.map { t ->
                                                if (t.id == task.id && t.status == DownloadStatus.DOWNLOADING) {
                                                    t.copy(
                                                        progress = progress.coerceAtMost(1f),
                                                        speed = "1.2MB/s",
                                                        remainingTime = "剩余 ${((1f - progress) * 15).toInt()}秒"
                                                    )
                                                } else t
                                            }
                                        }
                                        
                                        // 检查是否是因为取消而结束
                                        if (downloadJobs[task.id] == false) {
                                            return@launch
                                        }
                                        
                                        // 检查是否已达到100%进度
                                        if (progress >= 1f) {
                                            // 下载完成，移动到已完成列表
                                            downloadingTasks = downloadingTasks.filter { it.id != task.id }
                                            completedTasks = completedTasks + task.copy(
                                                status = DownloadStatus.COMPLETED,
                                                progress = 1f,
                                                speed = "0KB/s",
                                                remainingTime = "已完成"
                                            )
                                            tempNotification = "下载完成" to task.copy(status = DownloadStatus.COMPLETED)
                                            downloadJobs.remove(task.id)
                                        }
                                    }
                                }
                            )
                            1 -> CompletedList(
                                tasks = completedTasks,
                                onDeleteClick = { task ->
                                    deleteConfirmation = DeleteConfirmationState(
                                        task = task,
                                        isVisible = true
                                    )
                                },
                                onShowLocation = { filePath ->
                                    showLocationDialog = FileLocationDialogState(
                                        isVisible = true,
                                        filePath = filePath
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 显示各种提示界面
    if (showProgressDialog) {
        ProgressDialog(
            message = progressDialogMessage,
            onDismiss = { showProgressDialog = false }
        )
    }
    
    if (showConfirmDialog) {
        ConfirmDialog(
            title = "确认操作",
            message = "确定要执行此操作吗？",
            onConfirm = { showConfirmDialog = false },
            onDismiss = { showConfirmDialog = false }
        )
    }
    
    // 显示临时通知
    tempNotification?.let { (message, task) ->
        when (task?.status) {
            DownloadStatus.COMPLETED -> {
                Toast(
                    message = "下载完成：${task.fileName}",
                    isVisible = true,
                    onDismiss = { tempNotification = null }
                )
            }
            DownloadStatus.FAILED -> {
                AlertDialog(
                    onDismissRequest = { tempNotification = null },
                    title = { 
                        Text(
                            "下载失败",
                            color = Color(0xFFF44336)
                        ) 
                    },
                    text = { 
                        Column {
                            Text("文件：${task.fileName}")
                            Text("原因：${task.errorMessage}")
                            if (task.resumeSupported) {
                                Text("已保存下载进度：${(task.progress * 100).toInt()}%")
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { tempNotification = null }
                        ) {
                            Text("确定")
                        }
                    }
                )
            }
            else -> {
                Toast(
                    message = message,
                    isVisible = true,
                    onDismiss = { tempNotification = null }
                )
            }
        }
    }

    // 显示删除确认对话框
    if (deleteConfirmation.isVisible) {
        val task = deleteConfirmation.task
        if (task != null) {
            AlertDialog(
                onDismissRequest = { deleteConfirmation = DeleteConfirmationState() },
                title = { Text("删除任务") },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                completedTasks = completedTasks.filter { it.id != task.id }
                                tempNotification = "已删除任务" to null
                                deleteConfirmation = DeleteConfirmationState()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("仅删除任务")
                        }
                        TextButton(
                            onClick = {
                                completedTasks = completedTasks.filter { it.id != task.id }
                                tempNotification = "已删除源文件" to null
                                deleteConfirmation = DeleteConfirmationState()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("删除源文件")
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { deleteConfirmation = DeleteConfirmationState() }
                            ) {
                                Text("取消")
                            }
                        }
                    }
                },
                text = null,
                dismissButton = null
            )
        }
    }

    // 文件位置对话框
    if (showLocationDialog.isVisible) {
        AlertDialog(
            onDismissRequest = { showLocationDialog = FileLocationDialogState() },
            title = { Text("文件位置") },
            text = { Text(showLocationDialog.filePath) },
            dismissButton = {
                TextButton(
                    onClick = { showLocationDialog = FileLocationDialogState() }
                ) {
                    Text("取消")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            openFileLocation(context, showLocationDialog.filePath)
                        }
                        showLocationDialog = FileLocationDialogState()
                    }
                ) {
                    Text("打开")
                }
            }
        )
    }

    // 权限对话框
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要权限") },
            text = { Text(permissionMessage) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(PermissionManager.getRequiredPermissions())
                    }
                ) {
                    Text("授权")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 取消确认对话框
    cancelConfirmation?.let { task ->
        AlertDialog(
            onDismissRequest = { cancelConfirmation = null },
            title = { Text("确认取消") },
            text = { Text(text = "确定要取消下载 ${task.fileName} 吗？") },
            dismissButton = {
                TextButton(
                    onClick = {
                        downloadJobs[task.id] = false
                        downloadingTasks = downloadingTasks.filter { it.id != task.id }
                        tempNotification = "已取消下载" to task
                        cancelConfirmation = null
                    }
                ) {
                    Text("确定")
                }
            },
            confirmButton = {
                TextButton(onClick = { cancelConfirmation = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun DownloadingList(
    tasks: List<DownloadTask>,
    onPause: (DownloadTask) -> Unit,
    onResume: (DownloadTask) -> Unit,
    onCancel: (DownloadTask) -> Unit,
    onRetry: (DownloadTask) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无下载任务", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (task.status) {
                            DownloadStatus.DOWNLOADING -> Color(0xFFF3F9FF) // 浅蓝色背景
                            DownloadStatus.PAUSED -> Color(0xFFFFF8E1) // 浅黄色背景
                            DownloadStatus.FAILED -> Color(0xFFFFEBEE) // 浅红色背景
                            DownloadStatus.COMPLETED -> Color(0xFFF1F8E9) // 浅绿色背景
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // 文件名和状态
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = task.fileName,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Card(
                                shape = RoundedCornerShape(4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = when (task.status) {
                                        DownloadStatus.DOWNLOADING -> Color(0xFF2196F3)
                                        DownloadStatus.PAUSED -> Color(0xFFFFC107)
                                        DownloadStatus.FAILED -> Color(0xFFF44336)
                                        DownloadStatus.COMPLETED -> Color(0xFF4CAF50)
                                    }
                                )
                            ) {
                                Text(
                                    text = when (task.status) {
                                        DownloadStatus.DOWNLOADING -> "下载中"
                                        DownloadStatus.PAUSED -> "已暂停"
                                        DownloadStatus.FAILED -> "下载失败"
                                        DownloadStatus.COMPLETED -> "已完成"
                                    },
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 进度条
                        LinearProgressIndicator(
                            progress = { task.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when (task.status) {
                                DownloadStatus.DOWNLOADING -> Color(0xFF2196F3)
                                DownloadStatus.PAUSED -> Color(0xFFFFC107)
                                DownloadStatus.FAILED -> Color(0xFFF44336)
                                DownloadStatus.COMPLETED -> Color(0xFF4CAF50)
                            },
                            trackColor = Color(0xFFE0E0E0)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 下载信息
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${(task.progress * 100).toInt()}% • ${task.speed}",
                                color = Color(0xFF757575),
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${task.totalSize} • ${task.remainingTime}",
                                color = Color(0xFF757575),
                                fontSize = 14.sp
                            )
                        }

                        if (task.status == DownloadStatus.FAILED) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = task.errorMessage,
                                color = Color(0xFFF44336),
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 操作按钮
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (task.status) {
                                DownloadStatus.DOWNLOADING -> {
                                    TextButton(onClick = { onPause(task) }) {
                                        Text("暂停")
                                    }
                                    TextButton(
                                        onClick = { onCancel(task) },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = Color(0xFFF44336)
                                        )
                                    ) {
                                        Text("取消")
                                    }
                                }
                                DownloadStatus.PAUSED -> {
                                    TextButton(onClick = { onResume(task) }) {
                                        Text("继续")
                                    }
                                    TextButton(
                                        onClick = { onCancel(task) },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = Color(0xFFF44336)
                                        )
                                    ) {
                                        Text("取消")
                                    }
                                }
                                DownloadStatus.FAILED -> {
                                    TextButton(onClick = { onRetry(task) }) {
                                        Text("重试")
                                    }
                                    TextButton(
                                        onClick = { onCancel(task) },
                                        colors = ButtonDefaults.textButtonColors(
                                            contentColor = Color(0xFFF44336)
                                        )
                                    ) {
                                        Text("取消")
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletedList(
    tasks: List<DownloadTask>,
    onDeleteClick: (DownloadTask) -> Unit,
    onShowLocation: (String) -> Unit
) {
    if (tasks.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("暂无已完成任务", color = Color.Gray)
        }
    } else {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tasks) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF1F8E9)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = task.fileName,
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = task.totalSize,
                                color = Color(0xFF757575),
                                fontSize = 14.sp
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    scope.launch {
                                        openFile(context, task)
                                    }
                                }
                            ) {
                                Text("打开")
                            }
                            TextButton(
                                onClick = { onShowLocation(task.filePath) }
                            ) {
                                Text("位置")
                            }
                            TextButton(
                                onClick = { onDeleteClick(task) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFF44336)
                                )
                            ) {
                                Text("删除")
                            }
                        }
                    }
                }
            }
        }
    }
}

// 将文件操作移到 Composable 函数外部
private suspend fun openFile(context: Context, task: DownloadTask) {
    withContext(Dispatchers.IO) {
        val file = File(task.filePath)
        if (file.exists()) {
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_VIEW)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
                
                // 根据文件类型设置不同的处理方式
                val mimeType = when {
                    task.fileName.endsWith(".mp4", true) -> "video/*"
                    task.fileName.endsWith(".mp3", true) -> "audio/*"
                    else -> "*/*"
                }
                
                intent.setDataAndType(uri, mimeType)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                
                // 如果是视频文件，优先尝试在相册中打开
                if (mimeType == "video/*") {
                    val galleryIntent = Intent(Intent.ACTION_VIEW)
                    galleryIntent.setDataAndType(uri, mimeType)
                    galleryIntent.setPackage("com.android.gallery3d")
                    galleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    
                    try {
                        context.startActivity(galleryIntent)
                    } catch (e: ActivityNotFoundException) {
                        // 如果没有相册应用，则显示选择器让用户选择应用打开
                        val chooser = Intent.createChooser(intent, "选择打开方式")
                        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(chooser)
                    }
                } else {
                    // 非视频文件显示选择器让用户选择应用打开
                    val chooser = Intent.createChooser(intent, "选择打开方式")
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(chooser)
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

private suspend fun openFileLocation(context: Context, filePath: String) {
    withContext(Dispatchers.IO) {
        withContext(Dispatchers.Main) {
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:${filePath}")
            intent.setDataAndType(uri, "resource/folder")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // 如果没有合适的应用处理，尝试使用系统文件管理器
                val fallbackIntent = Intent(Intent.ACTION_VIEW)
                fallbackIntent.setDataAndType(Uri.fromFile(File(filePath).parentFile), "resource/folder")
                fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(fallbackIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "未找到文件管理器应用", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    SYTheme {
        MainScreen(onSettingsClick = {})
    }
} 