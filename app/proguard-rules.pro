# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# 保留FFmpeg相关类
-keep class com.arthenica.ffmpegkit.** { *; }
-keep class com.arthenica.ffmpegkit.react.** { *; }

# 保留Compose相关类
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# 保留应用主要类
-keep class com.example.sy.MainActivity { *; }
-keep class com.example.sy.ui.screens.** { *; }
-keep class com.example.sy.network.** { *; }

# 移除未使用的代码
-dontwarn com.arthenica.ffmpegkit.**
-dontwarn org.jetbrains.kotlin.**

# 优化字符串
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# 移除日志
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}