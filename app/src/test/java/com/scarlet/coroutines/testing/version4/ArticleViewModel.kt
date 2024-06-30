package com.scarlet.coroutines.testing.version4

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scarlet.coroutines.testing.ApiService
import com.scarlet.model.Article
import com.scarlet.util.Resource
import kotlinx.coroutines.*

class ArticleViewModel(
    private val apiService: ApiService,
//    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _articles = MutableLiveData<Resource<List<Article>>>()
    val articles: LiveData<Resource<List<Article>>>
        get() = _articles

    fun onButtonClicked() {
        viewModelScope.launch {
            loadData()
        }
    }

    private suspend fun loadData() {
        doLongRunningCalculation()
        val articles = networkRequest()
        update(articles)
    }

    private suspend fun networkRequest(): Resource<List<Article>> {
        return withContext(Dispatchers.IO) {
            apiService.getArticles()
        }
    }

    private fun update(articles: Resource<List<Article>>) {
        _articles.value = articles
    }

    private suspend fun doLongRunningCalculation() {
        withContext(Dispatchers.Default) {
            delay(1_000)
        }
    }
}