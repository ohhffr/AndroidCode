package com.example.englishgptapplication.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
/**
 * ArticleNetWork 对象封装了文章相关的网络请求操作，通过 await 方法将 Retrofit 的 Call 对象转换为可挂起的操作，方便在协程中使用。
 */
object ArticleNetWork {
    private val apiService = RetrofitClient.create<ApiService>()

    suspend fun getArticleTypes() = apiService.getArticleTypes().await()
    suspend fun getLexileValues() = apiService.getLexileValues().await()
    suspend fun getArticles(lexile: Int?, typeId: Int?, page: Int, size: Int) =
        apiService.getArticles(lexile, typeId, page, size).await()
    suspend fun getArticleDetail(id: Int) = apiService.getArticleDetail(id).await()

    private suspend fun <T> Call<T>.await(): T { //挂起函数,借助协程技术
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })
        }
    }
}