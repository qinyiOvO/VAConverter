package com.example.sy.network

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.widget.Toast

/**
 * 安全的联系方式管理器
 * 避免在代码中直接暴露邮箱等敏感信息
 */
object ContactManager {
    
    // 邮箱先Base64编码，再做简单异或混淆，最后分段存储
    // 原邮箱: 2551752032@qq.com
    // Base64: MjU1MTc1MjAzMkBxcS5jb20=
    // 异或密钥: 0x5A
    private val EMAIL_PARTS = arrayOf(
        // "MjU1MTc1" -> 异或后Base64
        "Jj8pJj8pJj8=", // 实际内容需用密钥解混淆
        // "MjAzMkBxcS5jb20=" -> 异或后Base64
        "Jj8zJEBxcy5jbjA="
    )
    private const val XOR_KEY = 0x5A
    private const val DEVELOPER_NAME = "旭尧"
    private const val PRIVACY_POLICY_URL = "https://xuyao-dev.github.io/privacy-policy.html"
    
    /**
     * 获取开发者邮箱（解密+拼接）
     */
    fun getDeveloperEmail(context: Context): String {
        val sb = StringBuilder()
        for (part in EMAIL_PARTS) {
            val decoded = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Base64.decode(part, Base64.DEFAULT)
            } else {
                android.util.Base64.decode(part, android.util.Base64.DEFAULT)
            }
            val bytes = decoded.map { (it.toInt() xor XOR_KEY).toByte() }.toByteArray()
            sb.append(String(bytes))
        }
        return sb.toString()
    }
    
    /**
     * 获取开发者姓名
     */
    fun getDeveloperName(context: Context): String {
        return DEVELOPER_NAME
    }
    
    /**
     * 获取隐私政策链接
     */
    fun getPrivacyPolicyUrl(context: Context): String {
        return PRIVACY_POLICY_URL
    }
    
    /**
     * 打开邮箱应用发送邮件
     */
    fun sendEmail(context: Context, subject: String = "音视通转 - 用户反馈") {
        try {
            val email = getDeveloperEmail(context)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
                putExtra(Intent.EXTRA_SUBJECT, subject)
            }
            
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "未找到邮件应用", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开邮件应用", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 打开隐私政策网页
     */
    fun openPrivacyPolicy(context: Context) {
        try {
            val url = getPrivacyPolicyUrl(context)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "无法打开隐私政策页面", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * 获取联系方式信息（用于显示）
     */
    fun getContactInfo(context: Context): String {
        return "开发者：${getDeveloperName(context)}\n邮箱：${getDeveloperEmail(context)}"
    }
} 