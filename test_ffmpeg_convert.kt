import java.io.File

fun videoToMp3(ffmpegPath: String, inputVideo: String, outputMp3: String): Boolean {
    val command = arrayOf(
        ffmpegPath,
        "-i", inputVideo,
        "-vn",
        "-ar", "44100",
        "-ac", "2",
        "-b:a", "192k",
        "-f", "mp3",
        outputMp3
    )
    val process = ProcessBuilder(*command)
        .redirectErrorStream(true)
        .start()
    val result = process.inputStream.bufferedReader().readText()
    process.waitFor()
    println(result)
    return File(outputMp3).exists()
}

fun main() {
    val ffmpegPath = "ffmpeg-7.1.1-full_build/bin/ffmpeg.exe"
    val inputVideo = "../SP/VID_20250625_034111.mp4"
    val outputMp3 = "../YP/VID_20250625_034111.mp3"

    val success = videoToMp3(ffmpegPath, inputVideo, outputMp3)
    if (success) {
        println("转换成功！")
    } else {
        println("转换失败！")
    }
} 