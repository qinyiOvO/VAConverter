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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MainScreen(
    onSettingsClick: () -> Unit,
    onHelpClick: () -> Unit,
    onGuideClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val conversionManager = remember { ConversionManager(context) }
    val conversionTasks by conversionManager.conversionTasks.collectAsState()
    
    // 调试日志
    LaunchedEffect(conversionTasks) {
        Log.d("MainScreen", "当前任务数量: ${conversionTasks.size}")
        conversionTasks.forEach { task ->
            Log.d("MainScreen", "任务: ${task.fileName}, 状态: ${task.status}")
        }
    }

    // 权限请求
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_AUDIO
        )
    } else {
        listOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }
    
    val permissionsState = rememberMultiplePermissionsState(permissions)
    
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
                    IconButton(onClick = onGuideClick) {
                        Icon(Icons.Default.Info, contentDescription = "操作引导", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                    IconButton(onClick = onHelpClick) {
                        Icon(Icons.Default.QuestionMark, contentDescription = "帮助", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1976D2),
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
                    if (permissionsState.allPermissionsGranted) {
                        videoPickerLauncher.launch("video/*")
                    } else {
                        permissionsState.launchMultiplePermissionRequest()
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
                        ConversionTaskItem(task)
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
                        ConversionTaskItem(task)
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
                        ConversionTaskItem(task)
                    }
                }
            }
        }
    }
}

@Composable
fun ConversionTaskItem(task: ConversionTask) {
    val context = LocalContext.current
    
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
                LinearProgressIndicator(
                    progress = { task.progress },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                
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
                
                if (task.status == ConversionStatus.COMPLETED) {
                    Row {
                        // 播放按钮
                        IconButton(onClick = {
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
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    SYTheme {
        MainScreen(onSettingsClick = {}, onHelpClick = {}, onGuideClick = {})
    }
} 