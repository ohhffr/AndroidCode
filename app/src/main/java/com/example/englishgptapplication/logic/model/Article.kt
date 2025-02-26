package com.example.englishgptapplication.logic.model

import com.example.englishgptapplication.R

/**
 * 用来表示文章的详细信息，还包含一个伴生对象，其中有一个映射表和一个根据类型 ID 获取颜色资源 ID 的方法。
 */
data class Article(
    val id: Int,
    val title: String,
    val wordNum: Int,
    val lexile: Int,
    val typeId: Int,
    val type: String,
    val cover: String,
    val stage: String?
){
    companion object {
        // 定义 typeId 到颜色资源 ID 的映射
        private val typeIdToColorMap = mapOf(
            1 to R.color.color_45D7B3,
            2 to R.color.color_02ACA2,
            3 to R.color.color_7E7DE5,
            4 to R.color.color_2FC5FD,
            5 to R.color.color_6DD303,
            6 to R.color.color_FC9508,
            7 to R.color.color_D4B07E,
            8 to R.color.color_F56379,
            9 to R.color.color_7AD3DB,
            10 to R.color.color_CC95D7,
            11 to R.color.color_3CBD83,
            12 to R.color.color_DB4BBC,
            13 to R.color.color_A78B43,
            14 to R.color.color_057646
        )

        fun getColorResIdByTypeId(typeId: Int): Int {
            return typeIdToColorMap[typeId] ?: R.color.color_00000000 // 默认颜色
        }
    }
}