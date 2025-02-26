package com.example.englishgptapplication.logic.network

import com.example.englishgptapplication.logic.model.ArticleDetail
import com.example.englishgptapplication.logic.model.ArticleListResponse
import com.example.englishgptapplication.logic.model.ArticleType
import com.example.englishgptapplication.logic.model.Lexile
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query


//用于封装服务器返回的通用响应结构
data class BaseResponse<T>(
    val code: Int,
    val msg: String,
    val data: T
)
/**
 * 用于描述与文章相关的网络请求接口
 */
interface ApiService {
    @GET("englishgpt/library/articleTypeList")
    fun getArticleTypes(): Call<BaseResponse<List<ArticleType>>>

    @GET("englishgpt/appArticle/selectList")
    fun getLexileValues(): Call<BaseResponse<List<Lexile>>>

    @GET("englishgpt/library/articleList")
    fun getArticles(
        @Query("lexile") lexile: Int?,
        @Query("typeId") typeId: Int?,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Call<BaseResponse<ArticleListResponse>>

    @GET("/knowledge/article/getArticleDetail")
    fun getArticleDetail(@Query("aid") aid: Int = 6): Call<BaseResponse<ArticleDetail>>
}
