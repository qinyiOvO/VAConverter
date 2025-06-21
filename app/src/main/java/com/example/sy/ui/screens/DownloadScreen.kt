package com.example.sy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun DownloadScreen(
    fileName: String,
    progress: Float,
    downloadSpeed: String,
    fileSize: String,
    remainingTime: String,
    previewUrl: String?,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    isPaused: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶部进度区
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = fileName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = Color(0xFF2196F3)
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color(0xFF2196F3),
                    trackColor = Color(0xFFE0E0E0)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = downloadSpeed,
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 内容信息区
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 预览区域
                Box(
                    modifier = Modifier
                        .size(160.dp, 90.dp)
                        .background(
                            color = Color(0xFFE0E0E0),
                            shape = RoundedCornerShape(8.dp)
                        )
                ) {
                    if (previewUrl != null) {
                        AsyncImage(
                            model = previewUrl,
                            contentDescription = "视频预览",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // 文件信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "文件大小：$fileSize",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "剩余时间：$remainingTime",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 控制按钮区
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 暂停/继续按钮
                FloatingActionButton(
                    onClick = if (isPaused) onResume else onPause,
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Text(if (isPaused) "▶" else "⏸")
                }

                // 取消按钮
                TextButton(
                    onClick = onCancel,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFFF44336)
                    )
                ) {
                    Text("取消")
                }

                // 重试按钮
                TextButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF2196F3)
                    )
                ) {
                    Text("重试")
                }
            }
        }
    }
}

@Composable
fun PreviewDownloadScreen() {
    DownloadScreen(
        fileName = "示例视频.mp4",
        progress = 0.45f,
        downloadSpeed = "1.2MB/s",
        fileSize = "56MB",
        remainingTime = "01:30",
        previewUrl = null,
        onPause = {},
        onResume = {},
        onCancel = {},
        onRetry = {},
        isPaused = false,
        modifier = Modifier.fillMaxWidth()
    )
} 