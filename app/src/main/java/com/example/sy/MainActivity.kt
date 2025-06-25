package com.example.sy

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.sy.ui.screens.MainScreen
import com.example.sy.ui.screens.AboutScreen
import com.example.sy.ui.screens.FeedbackScreen
import com.example.sy.ui.screens.HelpScreen
import com.example.sy.ui.theme.SYTheme
import com.example.sy.network.LogManager
import com.example.sy.network.FeedbackManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // 初始化日志管理器
            LogManager.initialize(this)
            LogManager.appendLog("MainActivity onCreate")
            
            // 初始化反馈管理器
            FeedbackManager.initialize(this)
            LogManager.appendLog("FeedbackManager initialized")
        } catch (e: Exception) {
            Log.e("MainActivity", "初始化失败", e)
        }
        
        setContent {
            SYTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showAbout by remember { mutableStateOf(false) }
                    var showFeedback by remember { mutableStateOf(false) }
                    var showHelp by remember { mutableStateOf(false) }

                    // 拦截系统返回键
                    BackHandler(enabled = showAbout || showFeedback || showHelp) {
                        when {
                            showHelp -> showHelp = false
                            showFeedback -> showFeedback = false
                            showAbout -> showAbout = false
                        }
                    }

                    when {
                        showHelp -> {
                            HelpScreen(
                                onBackClick = { 
                                    LogManager.appendLog("退出帮助界面")
                                    showHelp = false 
                                }
                            )
                        }
                        showFeedback -> {
                            FeedbackScreen(
                                onBackClick = { 
                                    LogManager.appendLog("退出反馈界面")
                                    showFeedback = false 
                                }
                            )
                        }
                        showAbout -> {
                            AboutScreen(
                                onBackClick = { 
                                    LogManager.appendLog("退出关于界面")
                                    showAbout = false 
                                },
                                onCheckUpdate = { 
                                    LogManager.appendLog("检查更新")
                                    /* TODO: 实现检查更新 */ 
                                },
                                onUserAgreement = { 
                                    LogManager.appendLog("查看用户协议")
                                    /* TODO: 实现用户协议 */ 
                                },
                                onPrivacyPolicy = { 
                                    LogManager.appendLog("查看隐私政策")
                                    /* TODO: 实现隐私政策 */ 
                                },
                                onFeedback = { 
                                    LogManager.appendLog("打开反馈界面")
                                    showFeedback = true 
                                }
                            )
                        }
                        else -> {
                            MainScreen(
                                onSettingsClick = { 
                                    LogManager.appendLog("打开关于界面")
                                    showAbout = true 
                                },
                                onHelpClick = { 
                                    LogManager.appendLog("打开帮助界面")
                                    showHelp = true 
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LogManager.appendLog("MainActivity onDestroy")
    }
}