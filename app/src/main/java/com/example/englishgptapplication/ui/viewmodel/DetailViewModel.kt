package com.example.englishgptapplication.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.englishgptapplication.logic.model.ArticleDetail
import com.example.englishgptapplication.logic.repository.ArticleRepository
import com.example.englishgptapplication.logic.repository.Result
import kotlinx.coroutines.launch
//DetailViewModel 类继承自 ViewModel，负责处理文章详情的加载和状态管理，使用 LiveData 来通知 UI 层数据的变化。
class DetailViewModel: ViewModel() {
    private val repository = ArticleRepository()
    private val _articleDetail = MutableLiveData<ArticleDetail>()
    val articleDetail: LiveData<ArticleDetail> = _articleDetail

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadArticleDetail(aid: Int) {
        viewModelScope.launch {
            repository.getArticleDetail(aid).observeForever { result ->
                when (result) {
                    is Result.Success -> {
                        result.data?.let {
                            _articleDetail.postValue(it)
                        } ?: run {
                            _errorMessage.postValue("文章详情为空")
                        }
                    }
                    is Result.Failure -> {
                        _errorMessage.postValue("加载失败: ${result.exception.message}")
                    }
                }
            }
        }
    }
}