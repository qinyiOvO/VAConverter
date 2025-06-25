package com.example.sy.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sy.ui.theme.SYTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.sy.network.ConversionManager
import com.example.sy.network.ConversionStatus
import com.example.sy.network.ConversionTask
import android.Manifest
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.runtime.rememberCoroutineScope
import java.io.File
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.runtime.mutableFloatStateOf
import com.google.accompanist.permissions.MultiplePermissionsState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val conversionManager = remember { ConversionManager(context) }
    val conversionTasks by conversionManager.conversionTasks.collectAsState()
    
    // 检查文件是否存在
    LaunchedEffect(Unit) {
        conversionManager.checkExistingFiles()
    }
    
    // 调试日志
    LaunchedEffect(conversionTasks) {
        Log.d("MainScreen", "当前任务数量: ${conversionTasks.size}")
        conversionTasks.forEach { task ->
            Log.d("MainScreen", "任务: ${task.fileName}, 状态: ${task.status}")
        }
    }

    // 权限请求
    val videoPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
    val videoPermissionsState = rememberMultiplePermissionsState(videoPermissions)
    val audioPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(Manifest.permission.READ_MEDIA_AUDIO)
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    val audioPermissionsState = rememberMultiplePermissionsState(audioPermissions)
    
    // 文件选择器
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                // 触发媒体扫描
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath),
                    null
                ) { path, uri ->
                    Log.d("MediaScanner", "Scanned path: $path, uri: $uri")
                }
                conversionManager.startConversion(uri)
            }
        }
    }

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
                    IconButton(onClick = onHelpClick) {
                        Icon(Icons.Default.QuestionMark, contentDescription = "帮助", tint = Color.White, modifier = Modifier.size(32.dp))
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
        ) {
            // 选择视频按钮
            Button(
                onClick = {
                    if (videoPermissionsState.allPermissionsGranted) {
                        videoPickerLauncher.launch("video/*")
                    } else {
                        videoPermissionsState.launchMultiplePermissionRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("选择视频", fontSize = 18.sp)
            }

            // 转换任务列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                // 如果没有任务，显示提示信息
                if (conversionTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "暂无转换任务",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // 正在转换的任务
                val convertingTasks = conversionTasks.filter { 
                    it.status == ConversionStatus.CONVERTING || it.status == ConversionStatus.WAITING 
                }
                if (convertingTasks.isNotEmpty()) {
                    item {
                        Text(
                            "正在转换 (${convertingTasks.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    items(convertingTasks) { task ->
                        ConversionTaskItem(task = task, conversionManager = conversionManager, audioPermissionsState = audioPermissionsState)
                    }
                }

                // 已完成的任务
                val completedTasks = conversionTasks.filter { 
                    it.status == ConversionStatus.COMPLETED 
                }
                if (completedTasks.isNotEmpty()) {
                    item {
                        Text(
                            "已完成 (${completedTasks.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    items(completedTasks) { task ->
                        ConversionTaskItem(task = task, conversionManager = conversionManager, audioPermissionsState = audioPermissionsState)
                    }
                }

                // 失败的任务
                val failedTasks = conversionTasks.filter { 
                    it.status == ConversionStatus.FAILED || it.status == ConversionStatus.CANCELLED 
                }
                if (failedTasks.isNotEmpty()) {
                    item {
                        Text(
                            "失败/已取消 (${failedTasks.size})",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                    items(failedTasks) { task ->
                        ConversionTaskItem(task = task, conversionManager = conversionManager, audioPermissionsState = audioPermissionsState)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConversionTaskItem(
    task: ConversionTask,
    conversionManager: ConversionManager,
    audioPermissionsState: MultiplePermissionsState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 对话框状态
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf(task.fileName) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 文件名
            Text(
                text = task.fileName,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            if (task.status == ConversionStatus.CONVERTING || task.status == ConversionStatus.WAITING) {
                var animatedProgress by remember { mutableFloatStateOf(0f) }
                
                LaunchedEffect(task.progress) {
                    // 确保进度值在0-1之间
                    val targetProgress = task.progress.coerceIn(0f, 1f)
                    animate(
                        initialValue = animatedProgress,
                        targetValue = targetProgress,
                        animationSpec = tween(
                            durationMillis = 500,
                            easing = FastOutSlowInEasing
                        )
                    ) { value, _ ->
                        animatedProgress = value
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // 进度百分比文字（移到进度条上方）
                    Text(
                        text = "转换进度：${(animatedProgress * 100).toInt()}%",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)  // 增加进度条高度
                    ) {
                        // 进度条背景
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(8.dp)  // 增加圆角
                                )
                        )
                        
                        // 进度条前景
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))  // 确保进度值不超过1
                                .fillMaxHeight()
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }

                    // 预计剩余时间
                    if (task.status == ConversionStatus.CONVERTING && animatedProgress > 0f && animatedProgress < 1f) {
                        val remainingTime = ((1 - animatedProgress) / animatedProgress * task.elapsedTime)
                            .toInt()
                            .coerceAtLeast(0)  // 确保不会出现负数
                        Text(
                            text = "预计剩余时间：${formatTime(remainingTime)}",
                            fontSize = 14.sp,  // 增大字体
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // 状态和操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (task.status) {
                        ConversionStatus.WAITING -> "等待转换"
                        ConversionStatus.CONVERTING -> "转换中 ${(task.progress * 100).toInt()}%"
                        ConversionStatus.COMPLETED -> "转换完成"
                        ConversionStatus.FAILED -> "转换失败"
                        ConversionStatus.CANCELLED -> "已取消"
                    },
                    color = when (task.status) {
                        ConversionStatus.COMPLETED -> Color(0xFF4CAF50)
                        ConversionStatus.FAILED -> Color(0xFFF44336)
                        ConversionStatus.CANCELLED -> Color(0xFF9E9E9E)
                        else -> Color(0xFF2196F3)
                    }
                )
                
                Row {
                    if (task.status == ConversionStatus.COMPLETED) {
                        // 播放按钮
                        IconButton(onClick = {
                            if (audioPermissionsState.allPermissionsGranted) {
                                try {
                                    val file = File(task.outputPath)
                                    val uri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.provider",
                                        file
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(uri, "audio/*")
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "无法打开文件", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                audioPermissionsState.launchMultiplePermissionRequest()
                            }
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "播放")
                        }
                        
                        // 分享按钮
                        IconButton(onClick = {
                            try {
                                val file = File(task.outputPath)
                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.provider",
                                    file
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "audio/*"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "分享音频"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "分享失败", Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "分享")
                        }
                        
                        // 重命名按钮
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "重命名")
                        }
                    }
                    
                    // 删除按钮
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, 
                            contentDescription = "删除",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
    
    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除任务") },
            text = { Text("是否同时删除转换后的文件？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            conversionManager.deleteTask(task.id, true)
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text("删除任务和文件")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            conversionManager.deleteTask(task.id, false)
                            showDeleteDialog = false
                        }
                    }
                ) {
                    Text("仅删除任务")
                }
            }
        )
    }
    
    // 重命名对话框
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("重命名文件") },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("文件名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFileName.isNotBlank() && newFileName != task.fileName) {
                            scope.launch {
                                conversionManager.renameTask(task.id, newFileName)
                                showRenameDialog = false
                            }
                        } else {
                            showRenameDialog = false
                        }
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SYTheme {
        MainScreen(
            onSettingsClick = {},
            onHelpClick = {}
        )
    }
}

// 添加一个格式化时间的函数
private fun formatTime(seconds: Int): String {
    return when {
        seconds < 60 -> "${seconds}秒"
        seconds < 3600 -> "${seconds / 60}分${seconds % 60}秒"
        else -> "${seconds / 3600}小时${(seconds % 3600) / 60}分${seconds % 60}秒"
    }
} 