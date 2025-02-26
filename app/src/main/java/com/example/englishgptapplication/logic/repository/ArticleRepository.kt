package com.example.englishgptapplication.logic.repository

import androidx.lifecycle.liveData
import com.example.englishgptapplication.logic.model.ArticleListResponse
import com.example.englishgptapplication.logic.network.ArticleNetWork
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}

//ArticleRepository 类通过定义 Result 密封类封装请求结果，利用 fire 私有方法在指定协程上下文（如 Dispatchers.IO）中执行网络请求
// （通过 ArticleNetWork 获取文章类型、难度值、文章列表及文章详情）并处理异常，最终以 LiveData 形式返回请求结果。

class ArticleRepository {

    fun getArticleTypes() = fire(Dispatchers.IO) {
        val types = ArticleNetWork.getArticleTypes()
        if (types.code == 0) {
            Result.Success(types.data)
        } else {
            Result.Success(emptyList())
        }
    }

    fun getLexileValues() = fire(Dispatchers.IO) {
        val values = ArticleNetWork.getLexileValues()

        if (values.code == 0) {
            Result.Success(values.data)
        } else {
            Result.Success(emptyList())
        }
    }

    fun getArticles(lexile: Int?, typeId: Int?, page: Int, size: Int) = fire(Dispatchers.IO) {
        val articles = ArticleNetWork.getArticles(lexile, typeId, page, size)
        if (articles.code == 0) {
            Result.Success(articles.data)
        } else {
            Result.Success(ArticleListResponse(0, emptyList(), 1, 10))
        }
    }

    fun getArticleDetail(aid: Int) = fire(Dispatchers.IO){
        val detail = ArticleNetWork.getArticleDetail(aid)
        if (detail.code == 0) {
            Result.Success(detail.data)
        }else{
            Result.Success(null)
        }
    }

    private fun <T> fire(context: CoroutineContext, block: suspend () -> Result<T>) = liveData(context) {
        val result = try {
            block()
        } catch (e: Exception) {
            Result.Failure(e)
        }
        emit(result)
    }
}