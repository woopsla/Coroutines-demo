package com.scarlet.coroutines.advanced.parallel_decompose

import com.scarlet.util.log
import kotlinx.coroutines.*
import java.lang.RuntimeException

@JvmInline
value class Image(val name: String)

suspend fun loadImage(name: String): Image {
    log("Loading ${name}: started.")
    delay(1_000)
    log("Loading ${name}: done.")
    return Image(name)
}

suspend fun loadImageFail(name: String): Image {
    log("Loading ${name}: started.")
    delay(500)
    throw RuntimeException("oops")
}

fun combineImages(image1: Image, image2: Image): Image =
    Image("${image1.name} & ${image2.name}")

