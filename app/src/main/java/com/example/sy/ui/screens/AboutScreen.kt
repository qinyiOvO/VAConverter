package com.example.sy.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onCheckUpdate: () -> Unit,
    onUserAgreement: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onFeedback: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        TopAppBar(
            title = { Text("关于") },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        // 内容区域
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 应用信息区
            item {
                Spacer(modifier = Modifier.height(24.dp))
                
                // 应用图标
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "应用图标",
                        modifier = Modifier
                            .size(48.dp)
                            .padding(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 应用名称
                Text(
                    text = "音视通转",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF212121)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 版本信息
                Text(
                    text = "版本 1.0.0",
                    fontSize = 18.sp,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // 开发者信息
                Text(
                    text = "开发者：旭尧",
                    fontSize = 18.sp,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 应用描述
                Text(
                    text = "本应用支持将本地视频文件一键转换为音频（MP3/AAC/WAV）文件，操作简单，转换高效，适合音频提取等多种场景。",
                    fontSize = 18.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider(color = Color(0xFFEEEEEE))
            }

            // 功能按钮区
            item {
                // 检查更新
                AboutItem(
                    icon = Icons.Default.Refresh,
                    title = "检查更新",
                    onClick = onCheckUpdate,
                    showArrow = false,
                    textColor = MaterialTheme.colorScheme.primary
                )
                
                // 用户协议
                AboutItem(
                    icon = Icons.Default.Info,
                    title = "用户协议",
                    onClick = onUserAgreement,
                    showArrow = true
                )
                
                // 隐私政策
                AboutItem(
                    icon = Icons.Default.Lock,
                    title = "隐私政策",
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://xuyao-dev.github.io/privacy-policy.html"))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            onPrivacyPolicy()
                        }
                    },
                    showArrow = true
                )
                
                // 反馈问题
                AboutItem(
                    icon = Icons.Default.Email,
                    title = "反馈问题",
                    onClick = onFeedback,
                    showArrow = true
                )
            }
        }
    }
}

@Composable
private fun AboutItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    showArrow: Boolean = true,
    textColor: Color = Color(0xFF212121)
) {
    Surface(
        onClick = onClick,
        color = Color.White,
        modifier = Modifier.height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Text(
                text = title,
                fontSize = 20.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            
            if (showArrow) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
} 