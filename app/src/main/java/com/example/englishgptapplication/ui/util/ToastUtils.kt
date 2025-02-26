
package com.example.englishgptapplication.ui.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.example.englishgptapplication.R

/**
 * 自定义Toast工具类
 * 特点：黑色背景、白色字体、无图标、显示在屏幕中间
 */
object ToastUtils {

    private var toast: Toast? = null

    /**
     * 显示自定义Toast
     * @param context 上下文
     * @param message 要显示的消息
     */
    fun showToast(context: Context, message: String) {
        showToast(context, message, Toast.LENGTH_SHORT)
    }

    /**
     * 显示自定义Toast，可指定时长
     * @param context 上下文
     * @param message 要显示的消息
     * @param duration 显示时长
     */
    private fun showToast(context: Context, message: String, duration: Int) {
        // 取消之前的Toast，避免排队显示
        toast?.cancel()

        // 创建自定义布局
        val view = LayoutInflater.from(context).inflate(R.layout.item_toast, null)
        val textView = view.findViewById<TextView>(R.id.toast_text)
        textView.text = message

        // 配置Toast对象
        toast = Toast(context.applicationContext).apply {
            setView(view)
            setDuration(duration)
            setGravity(Gravity.CENTER, 0, 0) // 设置在屏幕中间显示
            show()
        }
    }
}