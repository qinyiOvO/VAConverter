package com.example.sy.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

// 通知栏提示基础颜色
private object NotificationColors {
    val Background = Color.White
    val Primary = Color(0xFF2196F3)    // 下载中
    val Success = Color(0xFF4CAF50)    // 完成
    val Error = Color(0xFFF44336)      // 失败
    val Warning = Color(0xFFFFC107)    // 暂停
    val TextPrimary = Color(0xFF212121)
    val TextSecondary = Color(0xFF757575)
}

// 下载进度提示
@Composable
fun DownloadProgressNotification(
    title: String,
    progress: Float,
    speed: String,
    onPause: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NotificationColors.TextPrimary
                )
                Row {
                    // TODO: 添加暂停/继续图标
                    TextButton(onClick = onPause) {
                        Text("暂停")
                    }
                    // TODO: 添加取消图标
                    TextButton(onClick = onCancel) {
                        Text("取消")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = NotificationColors.Primary,
                trackColor = Color(0xFFE0E0E0)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "${"%.0f".format(progress * 100)}% • $speed",
                fontSize = 12.sp,
                color = NotificationColors.TextSecondary
            )
        }
    }
}

// 下载完成提示
@Composable
fun DownloadCompleteNotification(
    fileName: String,
    onOpenFile: () -> Unit,
    onShowLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "下载完成",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NotificationColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = fileName,
                        fontSize = 12.sp,
                        color = NotificationColors.TextSecondary
                    )
                }
                Row {
                    TextButton(onClick = onOpenFile) {
                        Text("打开文件")
                    }
                    TextButton(onClick = onShowLocation) {
                        Text("查看位置")
                    }
                }
            }
        }
    }
}

// 下载失败提示
@Composable
fun DownloadFailedNotification(
    errorMessage: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "下载失败",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NotificationColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage,
                        fontSize = 12.sp,
                        color = NotificationColors.TextSecondary
                    )
                }
                Row {
                    TextButton(onClick = onRetry) {
                        Text("重试")
                    }
                    TextButton(onClick = onCancel) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

// 下载暂停提示
@Composable
fun DownloadPausedNotification(
    progress: String,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "下载已暂停",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = NotificationColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = progress,
                        fontSize = 12.sp,
                        color = NotificationColors.TextSecondary
                    )
                }
                Row {
                    TextButton(onClick = onResume) {
                        Text("继续")
                    }
                    TextButton(onClick = onCancel) {
                        Text("取消")
                    }
                }
            }
        }
    }
}

// 确认对话框
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "确认",
    dismissText: String = "取消"
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = NotificationColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = NotificationColors.TextSecondary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = dismissText,
                            color = NotificationColors.TextSecondary
                        )
                    }
                    TextButton(onClick = onConfirm) {
                        Text(
                            text = confirmText,
                            color = NotificationColors.Primary
                        )
                    }
                }
            }
        }
    }
}

// 进度对话框
@Composable
fun ProgressDialog(
    message: String,
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = NotificationColors.Primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = NotificationColors.TextPrimary
                )
            }
        }
    }
}

// Toast提示
@Composable
fun Toast(
    message: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize(),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF323232)
                )
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
        
        // 自动消失
        LaunchedEffect(isVisible) {
            if (isVisible) {
                kotlinx.coroutines.delay(2000)
                onDismiss()
            }
        }
    }
}

// 预览示例
@Preview(showBackground = true)
@Composable
private fun AlertScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DownloadProgressNotification(
            title = "正在下载视频.mp4",
            progress = 0.45f,
            speed = "1.2MB/s",
            onPause = {},
            onCancel = {}
        )
        
        DownloadCompleteNotification(
            fileName = "视频.mp4",
            onOpenFile = {},
            onShowLocation = {}
        )
        
        DownloadFailedNotification(
            errorMessage = "网络连接失败",
            onRetry = {},
            onCancel = {}
        )
        
        DownloadPausedNotification(
            progress = "已下载45% (25MB/56MB)",
            onResume = {},
            onCancel = {}
        )
    }
}

@Composable
fun ConvertProgressNotification(
    title: String,
    progress: Float,
    onPause: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = NotificationColors.TextPrimary
                )
                Row {
                    TextButton(onClick = onPause) { Text("暂停") }
                    TextButton(onClick = onCancel) { Text("取消") }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(2.dp),
                color = NotificationColors.Primary,
                trackColor = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = 12.sp,
                color = NotificationColors.TextSecondary
            )
        }
    }
}

@Composable
fun ConvertCompleteNotification(
    fileName: String,
    onOpenFile: () -> Unit,
    onShowLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("转换完成", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NotificationColors.TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(fileName, fontSize = 12.sp, color = NotificationColors.TextSecondary)
                }
                Row {
                    TextButton(onClick = onOpenFile) { Text("打开文件") }
                    TextButton(onClick = onShowLocation) { Text("查看位置") }
                }
            }
        }
    }
}

@Composable
fun ConvertFailedNotification(
    errorMessage: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("转换失败", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NotificationColors.TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage, fontSize = 12.sp, color = NotificationColors.TextSecondary)
                }
                Row {
                    TextButton(onClick = onRetry) { Text("重试") }
                    TextButton(onClick = onCancel) { Text("取消") }
                }
            }
        }
    }
}

@Composable
fun ConvertPausedNotification(
    progress: String,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("转换已暂停", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = NotificationColors.TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(progress, fontSize = 12.sp, color = NotificationColors.TextSecondary)
                }
                Row {
                    TextButton(onClick = onResume) { Text("继续") }
                    TextButton(onClick = onCancel) { Text("取消") }
                }
            }
        }
    }
} 