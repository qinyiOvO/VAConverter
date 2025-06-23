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
import com.example.sy.network.DownloadStatus
import com.example.sy.network.DownloadTask
import com.example.sy.network.DownloadTaskManager
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Help
import androidx.compose.ui.res.painterResource

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
    val filePath: String = "",
    val fileName: String = ""
)

// 取消确认对话框数据类
data class CancelConfirmationState(
    val task: DownloadTask? = null,
    val isVisible: Boolean = false
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
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onGuideClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedVideo by remember { mutableStateOf<String?>(null) }
    var convertingTasks by remember { mutableStateOf<List<String>>(emptyList()) }
    var completedTasks by remember { mutableStateOf<List<String>>(emptyList()) }
    var showFilePicker by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMsg by remember { mutableStateOf("") }

    // 顶部栏
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("视频音频转换器", fontSize = 28.sp) },
                navigationIcon = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                },
                actions = {
                    IconButton(onClick = onGuideClick) {
                        Icon(Icons.Default.Info, contentDescription = "操作引导", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = onHelpClick) {
                        Icon(Icons.Default.Help, contentDescription = "帮助", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1976D2), // 更高对比度蓝色
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
                .padding(24.dp)
        ) {
            // 选择视频按钮
            Button(
                onClick = { showFilePicker = true },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("选择本地视频", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            // 开始转换按钮
            Button(
                onClick = {
                    selectedVideo?.let {
                        convertingTasks = convertingTasks + it
                        snackbarMsg = "开始转换：$it"
                        showSnackbar = true
                        // TODO: 调用转换逻辑
                    }
                },
                enabled = selectedVideo != null,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Text("一键转换", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.height(32.dp))
            // 转换中列表
            Text("转换中", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(convertingTasks) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(file, modifier = Modifier.weight(1f), fontSize = 18.sp)
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                            IconButton(onClick = { convertingTasks = convertingTasks - file }) {
                                Icon(Icons.Default.Delete, contentDescription = "取消", modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            // 已完成列表
            Text("已完成", fontWeight = FontWeight.Bold, fontSize = 22.sp)
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(completedTasks) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(file, modifier = Modifier.weight(1f), fontSize = 18.sp)
                            IconButton(onClick = { /* TODO: 播放音频 */ }) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "播放", modifier = Modifier.size(28.dp))
                            }
                            IconButton(onClick = { /* TODO: 分享音频 */ }) {
                                Icon(Icons.Default.Share, contentDescription = "分享", modifier = Modifier.size(28.dp))
                            }
                            IconButton(onClick = { completedTasks = completedTasks - file }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(28.dp))
                            }
                        }
                    }
                }
            }
            // 文件选择器和Snackbar
            if (showFilePicker) {
                // TODO: 调用系统文件选择器，选择视频后赋值给selectedVideo
                showFilePicker = false
            }
            if (showSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(12.dp),
                    action = {
                        TextButton(onClick = { showSnackbar = false }) { Text("关闭", fontSize = 18.sp) }
                    }
                ) { Text(snackbarMsg, fontSize = 18.sp) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun MainScreenPreview() {
    SYTheme {
        MainScreen(onSettingsClick = {}, onHelpClick = {}, onGuideClick = {})
    }
} 