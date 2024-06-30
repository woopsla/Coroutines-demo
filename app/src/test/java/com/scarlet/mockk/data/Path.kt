package com.scarlet.mockk.data

interface Path {
    fun fileName(): String
    fun readText(): CharSequence
    fun writeText(text: CharSequence)

    suspend fun readAsync(): String
    suspend fun writeAsync(text: CharSequence)
    suspend fun doAsyncWork()
}