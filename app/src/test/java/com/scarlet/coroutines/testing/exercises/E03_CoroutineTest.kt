package com.scarlet.coroutines.testing.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ArticleViewModel() : ViewModel() {
    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message

    fun loadMessage() {
        viewModelScope.launch {
            delay(500);
            _message.value = "Kotlin Coroutine Rocks!"
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class A03_CoroutineTest {
    // SUT
    private lateinit var viewModel: ArticleViewModel

    // How to make this test pass?
    @Test
    fun viewModelTest() = runTest {
        viewModel = ArticleViewModel()

        viewModel.loadMessage()
        advanceUntilIdle()

        assertThat(viewModel.message.value).isEqualTo("Kotlin Coroutine Rocks!")
    }

}
