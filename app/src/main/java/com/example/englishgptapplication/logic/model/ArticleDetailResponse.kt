package com.example.englishgptapplication.logic.model

//用于封装文章详情的响应数据
data class ArticleDetail(
    val title: String = "",// 文章标题
    val bgmUrl: String = "",// 文章的背景音乐地址
    val contentList: List<Content> = emptyList(),// 文章内容列表
)

data class Content(
    val pageNum: Int = 0, // 内容所在的页码
    val imgUrl: String = "",// 内容的图片地址
    val audioUrl: String = "",// 内容的音频地址
    val audioDuration: Int = 0,// 音频的时长
    val sentence: String = "",//句子内容
    val sentenceByXFList: List<WordSegment> = emptyList(),// 句子的分词列表
)

data class WordSegment(
    val word: String = "",// 分词的字词
    val wb: Float = 0f, // 开始时间（帧）
    val we: Float = 0f  // 结束时间（帧）
)