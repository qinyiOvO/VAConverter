package com.example.sy.network

data class ConversionTask(
    val id: String = java.util.UUID.randomUUID().toString(),
    val inputPath: String,
    val outputPath: String,
    val fileName: String,
    var status: ConversionStatus = ConversionStatus.WAITING,
    var progress: Float = 0f,
    var startTime: Long = System.currentTimeMillis(),
    var elapsedTime: Int = 0
)

enum class ConversionStatus {
    WAITING,
    CONVERTING,
    COMPLETED,
    FAILED,
    CANCELLED
} 