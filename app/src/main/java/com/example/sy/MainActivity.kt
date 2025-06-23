package com.example.sy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.sy.ui.screens.MainScreen
import com.example.sy.ui.screens.AboutScreen
import com.example.sy.ui.screens.FeedbackScreen
import com.example.sy.ui.theme.SYTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SYTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showAbout by remember { mutableStateOf(false) }
                    var showFeedback by remember { mutableStateOf(false) }
                    val context = LocalContext.current

                    when {
                        showFeedback -> {
                            FeedbackScreen(
                                onBackClick = { showFeedback = false },
                                onSubmit = { _ ->
                                    // TODO: 实现反馈提交逻辑
                                    Toast.makeText(context, "反馈已提交", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        showAbout -> {
                            AboutScreen(
                                onBackClick = { showAbout = false },
                                onCheckUpdate = { /* TODO: 实现检查更新 */ },
                                onUserAgreement = { /* TODO: 实现用户协议 */ },
                                onPrivacyPolicy = { /* TODO: 实现隐私政策 */ },
                                onFeedback = { showFeedback = true }
                            )
                        }
                        else -> {
                            MainScreen(
                                onSettingsClick = { showAbout = true },
                                onHelpClick = { Toast.makeText(context, "帮助内容开发中", Toast.LENGTH_SHORT).show() },
                                onGuideClick = { Toast.makeText(context, "操作引导开发中", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }
            }
        }
    }
}