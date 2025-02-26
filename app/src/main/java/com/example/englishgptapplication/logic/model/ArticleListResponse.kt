package com.example.englishgptapplication.logic.model

/**
 * 封装文章列表的响应数据
 */
data class ArticleListResponse(
    val total: Int,// 文章的总数
    val list: List<Article>,// 文章列表，包含多个 Article 对象
    val pageNum: Int,// 当前页码
    val pageSize: Int// 每页的文章数量
)