package com.scarlet.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestDispatcher
import kotlin.coroutines.ContinuationInterceptor

val CoroutineScope.testDispatcher get() = coroutineContext[ContinuationInterceptor] as TestDispatcher