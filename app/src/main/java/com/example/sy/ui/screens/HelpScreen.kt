package com.example.sy.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("如何使用", "功能说明")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        TopAppBar(
            title = { Text("使用帮助", fontSize = 24.sp) },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFF1976D2),
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        // 标签页
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1976D2))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEachIndexed { index, title ->
                TextButton(
                    onClick = { selectedTab = index },
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = if (selectedTab == index) Color(0xFF2196F3) else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        color = if (selectedTab == index) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 内容区域
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // 使用流程模块
                    item {
                        HelpSection(
                            title = "基础操作流程",
                            content = listOf(
                                HelpItem(
                                    title = "第一步：选择视频",
                                    details = listOf(
                                        "点击主界面中间的\"选择视频\"按钮",
                                        "在手机相册中选择想要转换的视频",
                                        "支持常见视频文件转换"
                                    )
                                ),
                                HelpItem(
                                    title = "第二步：等待转换",
                                    details = listOf(
                                        "选择视频后会自动开始转换",
                                        "界面上会显示转换进度",
                                        "转换期间请不要关闭应用"
                                    )
                                ),
                                HelpItem(
                                    title = "第三步：转换完成",
                                    details = listOf(
                                        "转换完成后会显示\"转换完成\"",
                                        "转换好的音频文件会自动保存在手机的\"Music\"文件夹中（打开路径：手机存储/Music）",
                                        "可以直接点击播放按钮试听"
                                    )
                                )
                            )
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        HelpSection(
                            title = "常见问题解答",
                            content = listOf(
                                HelpItem(
                                    title = "找不到转换后的文件？",
                                    details = listOf(
                                        "所有转换好的音频都保存在手机的\"Music\"文件夹中（打开路径：手机存储/Music）",
                                        "可以使用手机自带的文件管理器查看",
                                        "也可以用音乐播放器直接找到"
                                    )
                                ),
                                HelpItem(
                                    title = "转换需要多长时间？",
                                    details = listOf(
                                        "一般视频转换很快，通常几分钟内就能完成",
                                        "时间长短主要取决于视频的大小",
                                        "转换过程中会显示进度，您可以看到还需要多久"
                                    )
                                ),
                                HelpItem(
                                    title = "转换失败怎么办？",
                                    details = listOf(
                                        "检查手机存储空间是否足够",
                                        "确保视频文件没有损坏",
                                        "可以尝试重新选择该视频进行转换"
                                    )
                                )
                            )
                        )
                    }
                }
                1 -> {
                    // 功能说明模块
                    item {
                        HelpSection(
                            title = "主界面按钮说明",
                            content = listOf(
                                HelpItem(
                                    title = "顶部按钮",
                                    details = listOf(
                                        "⚙️ 设置：点击后可以进入设置界面",
                                        "❓ 帮助：点击后进入本帮助页面"
                                    )
                                ),
                                HelpItem(
                                    title = "选择视频按钮",
                                    details = listOf(
                                        "这是最主要的功能按钮",
                                        "点击后可以选择要转换的视频",
                                        "支持常见视频文件转换"
                                    )
                                )
                            )
                        )
                    }

                    item { Spacer(modifier = Modifier.height(24.dp)) }

                    item {
                        HelpSection(
                            title = "文件操作说明",
                            content = listOf(
                                HelpItem(
                                    title = "文件操作按钮",
                                    details = listOf(
                                        "▶️ 播放：点击可以试听转换好的音频",
                                        "↗️ 分享：点击可以把音频发送给他人",
                                        "✏️ 重命名：点击可以修改文件名称",
                                        "🗑️ 删除：点击可以删除不需要的文件"
                                    )
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpSection(
    title: String,
    content: List<HelpItem>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1976D2),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        content.forEach { item ->
            HelpItemCard(item)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpItemCard(item: HelpItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = item.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF212121)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            item.details.forEach { detail ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Circle,
                        contentDescription = null,
                        modifier = Modifier.size(8.dp),
                        tint = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = detail,
                        fontSize = 16.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
}

private data class HelpItem(
    val title: String,
    val details: List<String>
) 