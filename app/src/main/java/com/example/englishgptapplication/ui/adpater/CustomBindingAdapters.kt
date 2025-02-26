package com.example.englishgptapplication.ui.adpater

import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.example.englishgptapplication.R

//文章类型颜色映射
object CustomBindingAdapters {

    @BindingAdapter("backgroundColorByTypeId")
    @JvmStatic
    fun setBackgroundColorByTypeId(view: TextView, typeId: Int?) {
        if (typeId != null) {
            val color = when (typeId) {
                1 -> R.color.color_45D7B3
                2 -> R.color.color_02ACA2
                3 -> R.color.color_7E7DE5
                4 -> R.color.color_2FC5FD
                5 -> R.color.color_6DD303
                6 -> R.color.color_FC9508
                7 -> R.color.color_D4B07E
                8 -> R.color.color_F56379
                9 -> R.color.color_7AD3DB
                10 -> R.color.color_CC95D7
                11 -> R.color.color_3CBD83
                12 -> R.color.color_DB4BBC
                13 -> R.color.color_A78B43
                14 -> R.color.color_057646
                else -> R.color.color_00000000 // 默认透明
            }

            // 获取背景并设置圆角和颜色
            val drawable = GradientDrawable()
            val cornerRadius = view.context.resources.getDimension(R.dimen.dimen_corner_radius01)
            val cornerRadii = floatArrayOf(
                cornerRadius, cornerRadius, // 左上角 x 和 y 半径
                0f, 0f, // 右上角 x 和 y 半径
                0f, 0f, // 右下角 x 和 y 半径
                0f, 0f  // 左下角 x 和 y 半径
            )
            drawable.cornerRadii = cornerRadii
            drawable.setColor(ContextCompat.getColor(view.context, color))

            // 设置背景
            view.background = drawable
        }
    }
}