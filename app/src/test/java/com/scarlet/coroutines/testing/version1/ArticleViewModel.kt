package com.scarlet.coroutines.testing.version1

import androidx.lifecycle.*
import com.scarlet.coroutines.testing.ApiService
import com.scarlet.model.Article
import com.scarlet.util.Resource
import kotlinx.coroutines.*

class ArticleViewModel(
    private val apiService: ApiService,
    // TODO() - Add a coroutine dispatcher
) : ViewModel() {

    private val scope = CoroutineScope(SupervisorJob())

    private val _articles = MutableLiveData<Resource<List<Article>>>()
    val articles: LiveData<Resource<List<Article>>>
        get() = _articles

    fun onButtonClicked() {
        scope.launch {
            loadData()
        }
    }

    suspend fun loadData() {
        val articles = networkRequest()
        update(articles)
    }

    private suspend fun networkRequest(): Resource<List<Article>> {
        return apiService.getArticles()
    }

    private fun update(articles: Resource<List<Article>>) {
        _articles.value = articles
    }

    override fun onCleared() {
        super.onCleared()
        // make sure to call cancel()
        scope.cancel()
    }
}



