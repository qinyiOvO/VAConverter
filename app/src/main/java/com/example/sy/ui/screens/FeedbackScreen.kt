package com.example.sy.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Build
import com.example.sy.network.LogManager
import com.example.sy.network.FeedbackManager
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onBackClick: () -> Unit
) {
    var selectedType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showLimitDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    
    val problemTypes = listOf(
        "功能异常",
        "界面问题",
        "性能问题",
        "转换相关",
        "建议优化",
        "其他问题"
    )
    
    val isFormValid = selectedType.isNotEmpty() && description.isNotEmpty()

    fun sendFeedbackEmail() {
        if (!FeedbackManager.canSubmitFeedback()) {
            LogManager.appendLog("用户超出反馈次数限制")
            showLimitDialog = true
            isSubmitting = false
            return
        }

        try {
            LogManager.appendLog("准备发送反馈 - 类型：$selectedType")
            
            val deviceInfo = """
                设备信息：
                Android版本：${Build.VERSION.RELEASE}
                设备型号：${Build.MODEL}
                制造商：${Build.MANUFACTURER}
            """.trimIndent()

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("2551752032@qq.com"))
                putExtra(Intent.EXTRA_SUBJECT, "视频音频转换器 - ${selectedType}反馈")
                putExtra(Intent.EXTRA_TEXT, """
                    问题类型：${selectedType}
                    
                    问题描述：
                    ${description}
                    
                    联系方式：
                    ${if (contact.isNotEmpty()) contact else "未提供"}
                    
                    $deviceInfo
                    
                    应用日志：
                    ${LogManager.getLogContent()}
                """.trimIndent())
            }

            context.startActivity(Intent.createChooser(emailIntent, "发送反馈"))
            FeedbackManager.recordFeedbackSubmission()
            LogManager.appendLog("反馈邮件已准备发送")
            showSuccessDialog = true
        } catch (e: Exception) {
            LogManager.appendLog("发送反馈失败：${e.message}")
            errorMessage = "发送失败，请检查是否安装邮件应用"
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
        } finally {
            isSubmitting = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        TopAppBar(
            title = { Text("问题反馈") },
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
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 错误消息显示
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // 剩余反馈次数提示
            val remainingFeedbacks = FeedbackManager.getRemainingFeedbacks()
            Text(
                text = "今日剩余反馈次数：$remainingFeedbacks",
                color = if (remainingFeedbacks > 0) Color.Gray else Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 问题类型选择
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("问题类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(4.dp)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    problemTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedType = type
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 问题描述输入框
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 500) description = it },
                label = { Text("问题描述") },
                placeholder = { Text("请详细描述您遇到的问题...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(4.dp),
                supportingText = { Text("${description.length}/500") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 联系方式输入
            OutlinedTextField(
                value = contact,
                onValueChange = { contact = it },
                label = { Text("联系方式（选填）") },
                placeholder = { Text("请留下您的邮箱或手机号，方便我们联系您...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(4.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 提示文本
            Text(
                text = "提交反馈后，系统会自动打开邮件应用，请确认发送。每天最多可提交3次反馈。",
                color = com.example.sy.ui.theme.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 提交按钮区域
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier.padding(16.dp)
            ) {
                Button(
                    onClick = {
                        if (isFormValid) {
                            isSubmitting = true
                            errorMessage = null
                            sendFeedbackEmail()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = CircleShape,
                    enabled = isFormValid && !isSubmitting && FeedbackManager.canSubmitFeedback()
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (FeedbackManager.canSubmitFeedback()) "提交反馈" else "今日反馈次数已用完",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // 成功提示对话框
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBackClick()
            },
            title = { Text("反馈已准备发送") },
            text = { Text("请在邮件应用中确认发送反馈内容。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccessDialog = false
                        onBackClick()
                    }
                ) {
                    Text("确定")
                }
            }
        )
    }

    // 次数限制提示对话框
    if (showLimitDialog) {
        val nextResetTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(FeedbackManager.getNextResetTime()))
        
        AlertDialog(
            onDismissRequest = { showLimitDialog = false },
            title = { Text("超出反馈次数限制") },
            text = { 
                Text("为了避免恶意信息，每台设备每24小时内最多可以提交3次反馈。\n\n下次可提交反馈时间：今天 $nextResetTime") 
            },
            confirmButton = {
                TextButton(onClick = { showLimitDialog = false }) {
                    Text("我知道了")
                }
            }
        )
    }
} 