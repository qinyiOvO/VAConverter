package com.example.sy.network

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import java.util.UUID

fun handleStartDownload(
    context: Context,
    shareText: String,
    scope: CoroutineScope,
    downloadTaskManager: DownloadTaskManager,
    onStatusUpdate: (String) -> Unit
) {
    if (shareText.isBlank()) {
        onStatusUpdate("请输入分享文本")
        return
    }

    // 创建新的下载任务
    val task = DownloadTask(
        id = UUID.randomUUID().toString(),
        fileName = "视频_${System.currentTimeMillis()}.mp4",
        status = DownloadStatus.DOWNLOADING
    )

    // 添加到下载列表
    downloadTaskManager.addDownloadingTask(task)
    
    // TODO: 实现实际的下载逻辑
    onStatusUpdate("开始下载...")
} 