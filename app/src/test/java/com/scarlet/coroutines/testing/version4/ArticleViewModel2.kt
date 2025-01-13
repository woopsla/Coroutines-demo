package com.scarlet.coroutines.testing.version4

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scarlet.coroutines.testing.ApiService
import com.scarlet.model.Article
import com.scarlet.util.DefaultDispatcherProvider
import com.scarlet.util.DispatcherProvider
import com.scarlet.util.Resource
import kotlinx.coroutines.*

class ArticleViewModel2(
    private val apiService: ApiService,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _articles = MutableLiveData<Resource<List<Article>>>()
    val articles: LiveData<Resource<List<Article>>>
        get() = _articles

    fun onButtonClicked() {
        viewModelScope.launch(dispatchers.main) {
            loadData()
        }
    }

    private suspend fun loadData() {
        doLongRunningCalculation()
        val articles = networkRequest()
        update(articles)
    }

    private suspend fun networkRequest(): Resource<List<Article>> {
        return withContext(dispatchers.io) {
            apiService.getArticles()
        }
    }

    private fun update(articles: Resource<List<Article>>) {
        _articles.value = articles
    }

    private suspend fun doLongRunningCalculation() {
        withContext(dispatchers.default) {
            delay(1_000)
        }
    }
}