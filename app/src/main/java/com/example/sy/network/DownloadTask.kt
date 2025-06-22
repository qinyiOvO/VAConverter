package com.example.sy.network

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
    var lastDownloadPosition: Long = 0L,
    var resumeSupported: Boolean = true
) 