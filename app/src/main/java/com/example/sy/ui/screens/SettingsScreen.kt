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
import androidx.compose.material.icons.filled.Help
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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape

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
    onAboutClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    var audioOutputPath by remember { mutableStateOf("/storage/emulated/0/Music") }
    var audioFormat by remember { mutableStateOf("MP3") }
    var notifyOnComplete by remember { mutableStateOf(true) }
    var cacheSize by remember { mutableStateOf("0 MB") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("设置", fontSize = 28.sp) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            actions = {
                IconButton(onClick = onHelpClick) {
                    Icon(Icons.Default.Help, contentDescription = "帮助", modifier = Modifier.size(32.dp))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1976D2),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Text("音频输出路径", fontSize = 14.sp, color = Color(0xFF757575), modifier = Modifier.padding(16.dp))
                SettingItem(
                    icon = Icons.Default.Folder,
                    title = "音频输出路径",
                    subtitle = audioOutputPath,
                    onClick = { /* TODO: 选择路径 */ },
                    iconSize = 32.dp,
                    fontSize = 20.sp
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
            }
            item {
                Text("音频格式", fontSize = 14.sp, color = Color(0xFF757575), modifier = Modifier.padding(16.dp))
                SettingItem(
                    icon = Icons.Default.MusicNote,
                    title = "音频格式",
                    subtitle = audioFormat,
                    onClick = { /* TODO: 选择格式 */ },
                    iconSize = 32.dp,
                    fontSize = 20.sp
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
            }
            item {
                Text("通知设置", fontSize = 14.sp, color = Color(0xFF757575), modifier = Modifier.padding(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "通知",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("转换完成通知", fontSize = 16.sp, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                    Switch(checked = notifyOnComplete, onCheckedChange = { notifyOnComplete = it }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFEEEEEE))
            }
            item {
                Text("清除缓存", fontSize = 14.sp, color = Color(0xFF757575), modifier = Modifier.padding(16.dp))
                SettingItem(
                    icon = Icons.Default.Delete,
                    title = "清除缓存",
                    subtitle = cacheSize,
                    onClick = { /* TODO: 清除缓存 */ },
                    iconSize = 32.dp,
                    fontSize = 20.sp
                )
            }
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "关于",
                    subtitle = "版本、开发者信息等",
                    onClick = onAboutClick
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onHelpClick,
            modifier = Modifier.fillMaxWidth().height(64.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text("常见问题与帮助", fontSize = 22.sp)
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

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconSize: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit
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
                modifier = Modifier.size(iconSize)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = fontSize,
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