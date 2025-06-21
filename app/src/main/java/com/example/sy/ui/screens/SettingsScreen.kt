package com.example.sy.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity

data class SettingsState(
    val downloadPath: String = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
    val videoQuality: String = "高清 (720P)",
    val audioFormat: String = "MP3",
    val autoDownload: Boolean = false,
    val notificationEnabled: Boolean = true,
    var cacheSize: String = "0 MB"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    val context = LocalContext.current
    
    // 存储权限对话框状态
    var showStoragePermissionDialog by remember { mutableStateOf(false) }
    
    // 下载位置对话框状态
    var showDownloadLocationDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        TopAppBar(
            title = { Text("设置") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        // 内容区域
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 基本设置
            item {
                Text(
                    text = "基本设置",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(16.dp)
                )
                
                // 存储权限
                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "存储权限",
                    subtitle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        "管理所有文件访问权限"
                    } else {
                        "读写外部存储权限"
                    },
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            if (!Environment.isExternalStorageManager()) {
                                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                                startActivity(context, intent, null)
                            }
                        } else {
                            showStoragePermissionDialog = true
                        }
                    }
                )
                
                // 下载位置
                SettingsItem(
                    icon = Icons.Default.Settings,
                    title = "下载位置",
                    subtitle = "选择视频和音频的保存位置",
                    onClick = { showDownloadLocationDialog = true }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color(0xFFEEEEEE)
                )
            }

            // 其他设置
            item {
                Text(
                    text = "其他设置",
                    fontSize = 14.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(16.dp)
                )
                
                // 关于
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "关于",
                    subtitle = "版本、开发者信息等",
                    onClick = onAboutClick
                )
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    color = Color(0xFF212121)
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "箭头",
                tint = Color(0xFF757575)
            )
        }
    }
}

private fun calculateCacheSize(_context: Context): String {
    // TODO: 实现缓存大小计算逻辑
    return "0 MB"
} 