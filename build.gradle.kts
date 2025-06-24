// Top-level build file with configuration options for all sub-projects
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // 依赖分析插件通过版本 catalog 管理版本
}

// 移除 buildscript 块，使用 plugins 块管理 Android Gradle Plugin 和 Kotlin 插件

// 应用依赖分析插件