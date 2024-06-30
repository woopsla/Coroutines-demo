package com.scarlet.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import java.util.*

/**
 * Get the current value from a LiveData without needing to register an observer.
 */
fun <T> LiveData<T>.getValueForTest(): T? {
    var value: T? = null
    val observer = Observer<T> {
        value = it
    }
    observeForever(observer)
    removeObserver(observer)
    return value
}

// Added by Jungsun Kim
@ExperimentalCoroutinesApi
fun <T> LiveData<T>.getValueForTest(dispatcher: TestDispatcher, ms: Long): T? {
    var value: T? = null
    val observer = Observer<T> {
        log("getValueForTest() - observer.onChanged()")
        value = it
        log("getValueForTest() - value = $value")
    }
    observeForever(observer)

    dispatcher.scheduler.advanceTimeBy(ms)
    dispatcher.scheduler.runCurrent()

    removeObserver(observer)
    return value
}

/**
 * Represents a list of capture values from a LiveData.
 */
class LiveDataValueCapture<T> {
    private val lock = Any()

    private val _values = mutableListOf<T?>()
    val values: List<T?>
        get() = synchronized(lock) {
            _values.toList() // copy to avoid returning reference to mutable list
        }

    fun addValue(value: T?) = synchronized(lock) {
        _values += value
    }
}

/**
 * Extension function to capture all values that are emitted to a LiveData<T> during the execution of
 * `captureBlock`.
 *
 * @param captureBlock a lambda that will
 */
inline fun <T> LiveData<T>.captureValues(block: LiveDataValueCapture<T>.() -> Unit) {
    val capture = LiveDataValueCapture<T>()
    val observer = Observer<T> {
        capture.addValue(it)
    }
    observeForever(observer)
    try {
        capture.block()
    } finally {
        removeObserver(observer)
    }
}



