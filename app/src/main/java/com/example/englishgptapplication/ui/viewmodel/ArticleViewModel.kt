package com.example.englishgptapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishgptapplication.logic.model.Article
import com.example.englishgptapplication.logic.model.ArticleListResponse
import com.example.englishgptapplication.logic.model.ArticleType
import com.example.englishgptapplication.logic.model.Lexile
import com.example.englishgptapplication.logic.repository.ArticleRepository
import com.example.englishgptapplication.logic.repository.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArticleViewModel : ViewModel() {
    // 新增累积文章列表
    private val _allArticles = MutableLiveData<MutableList<Article>>(mutableListOf())
    val allArticles: LiveData<MutableList<Article>> = _allArticles

    // 新增总页数和是否正在加载状态
    var totalPages = 1
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val repository = ArticleRepository()

    private val _articleTypes = MutableLiveData<List<ArticleType>?>()
    val articleTypes: MutableLiveData<List<ArticleType>?> = _articleTypes

    private val _lexileValues = MutableLiveData<List<Lexile>?>()
    val lexileValues: MutableLiveData<List<Lexile>?> = _lexileValues

    private val _articles = MutableLiveData<ArticleListResponse?>()
    val articles: MutableLiveData<ArticleListResponse?> = _articles

    private var currentLexile: Int = 5
    private var currentTypeId: Int? = null
    var currentPage = 1

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // 获取文章类型
            repository.getArticleTypes().observeForever { result ->
                when (result) {
                    is Result.Success -> {
                        _articleTypes.value = result.data
                    }
                    is Result.Failure -> {
                        Log.e("ArticleViewModel", "loadInitialData - getArticleTypes: ${result.exception.message}")
                    }
                    else -> {}
                }
            }
            // 获取难度值
            repository.getLexileValues().observeForever { result ->
                when (result) {
                    is Result.Success -> {
                        _lexileValues.value = result.data
                        Log.d("ArticleViewModel", "loadInitialData: ${_lexileValues.value}")
                    }
                    is Result.Failure -> {
                        Log.e("ArticleViewModel", "loadInitialData - getLexileValues: ${result.exception.message}")
                    }

                    else -> {Log.d("ArticleViewModel", "loadInitialData:意外 ")}
                }
            }
            loadArticles()
        }
    }

    fun loadArticles() {
        //先检查当前页数是否超过总页数或是否正在加载
        if (currentPage > totalPages || _isLoading.value == true) {
            _isLoading.value = false // 确保状态重置
            return
        }
        _isLoading.value = true

        viewModelScope.launch {
            val result = repository.getArticles(currentLexile, currentTypeId, currentPage, 10)
            result.observeForever { response ->
                when (response) {
                    is Result.Success -> {
                        response.data.let { data ->
                            totalPages = data.pageSize // 返回总页数字段为pageSize
                            val currentList = _allArticles.value ?: mutableListOf()

                            if (currentPage == 1) currentList.clear()

                            currentList.addAll(data.list)
                            _allArticles.postValue(currentList)
                            currentPage++
                        }
                    }
                    is Result.Failure -> {
                        // 加载失败时需要通知Footer
                        _isLoading.postValue(false)
                    }
                }
                _isLoading.postValue(false)
                result.removeObserver { }
            }
        }
    }

    fun setLexile(lexile: Int) {
        currentLexile = lexile
        currentPage = 1
        loadArticles()
    }

    fun setType(typeId: Int) {
        currentTypeId = typeId
        currentPage = 1
        loadArticles()
    }

    // 新增刷新方法
    fun refresh() {
        currentPage = 1
        _allArticles.value?.clear()
        loadArticles()
    }


    override fun onCleared() {
        super.onCleared()
        // 移除观察者，避免内存泄漏
        repository.getArticleTypes().removeObserver {}
        repository.getLexileValues().removeObserver {}
        repository.getArticles(currentLexile, currentTypeId, currentPage, 10).removeObserver {}
    }

}