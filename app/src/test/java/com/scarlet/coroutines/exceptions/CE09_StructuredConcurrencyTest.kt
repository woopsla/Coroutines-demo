package com.scarlet.coroutines.exceptions

import com.google.common.truth.Truth.assertThat
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.lang.RuntimeException

@JvmInline
value class Image(val content: String)

interface ImageService {
    suspend fun loadImage(name: String): Image
}

fun combineImages(image1: Image, image2: Image): Image =
    Image("${image1.content}, ${image2.content} combined")


class StructuredConcurrencyTest {

    @MockK
    lateinit var imageService: ImageService

    @Before
    fun init() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `loadAndCombineImages - parent job cancelled`() = runTest {
        coEvery { imageService.loadImage(any()) } coAnswers {
            delay(1_000)
            Image("image1")
        } coAndThen {
            delay(2_000)
            Image("image2")
        }

        var image: Image? = null
        val job = launch {
            image = coroutineScope {
                val deferred1 = async { imageService.loadImage("image1") }.onCompletion("deferred1")
                val deferred2 = async { imageService.loadImage("image2") }.onCompletion("deferred2")

                combineImages(deferred1.await(), deferred2.await())
            }
        }.onCompletion("parent")

        delay(1_500)
        job.cancelAndJoin()

        assertThat(image).isNull()
    }

    @Test
    fun `loadAndCombineImages - child fails`() = runTest {
        coEvery { imageService.loadImage(any()) } coAnswers {
            delay(1_000)
            throw RuntimeException("oops(❌)")
        } coAndThen {
            delay(2_000)
            Image("image2")
        }

        var image: Image? = null
        launch {
            try {
                image = coroutineScope {
                    val deferred1 =
                        async { imageService.loadImage("image1") }.onCompletion("deferred1")
                    val deferred2 =
                        async { imageService.loadImage("image2") }.onCompletion("deferred2")

                    combineImages(deferred1.await(), deferred2.await())
                }
            } catch (ex: Exception) {
                log("Caught ex = $ex")
            }
        }.onCompletion("parent").join()

        assertThat(image).isNull()
    }
}


