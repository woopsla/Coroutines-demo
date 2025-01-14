package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.lang.RuntimeException
import kotlin.time.Duration.Companion.seconds

class ExceptionHandlerTest {

    /**
     * **Coroutine Exception Handler (CEH)**
     *
     * - CEH can handle only _uncaught propagated exceptions_.
     *      - Only `launch` propagated exceptions are considered.
     *      - CEH installed in `launch` root coroutines take effect.
     *      - But, CEH installed in `async` root coroutines _has no effect_ at all!
     * - CEH should be installed in either _scopes_ or the _root coroutines_.
     *      - The exception is handled by the parent only when all its children terminate.
     */
    private val handler = CoroutineExceptionHandler { context, exception ->
        log("Global CEH: Caught $exception, and handles it in: $context")
    }

    /**
     * Coroutine Exception Handlers installed at scope
     */

    // What happen to top-level scope's Job?
    @Test
    fun `CEH at the scope`() = runTest {
        val scope = CoroutineScope(Job() + handler)

        scope.launch {
            launch {
                delay(100)
                throw RuntimeException("oops(❌)")
            }.onCompletion("child1")

            launch {
                delay(200)
            }.onCompletion("child2")
            // Will child2 be cancelled? - What is the take-away from this fact?

        }.onCompletion("parent").join()

        scope.completeStatus("scope") // Is scope cancelled?
    }

    /**
     * Coroutine Exception Handlers installed at root coroutines
     */

    @Test
    fun `CEH at the root coroutine - child of scope`() = runTest {
        val scope = CoroutineScope(Job()).onCompletion("scope")

        scope.launch(handler) {
            launch {
                delay(100)
                throw RuntimeException("oops(❌)")
            }.onCompletion("child1")

            launch {
                delay(200)
            }.onCompletion("child2")
        }.onCompletion("parent").join()
    }

    @Test
    fun `CEH at the root coroutine - child of supervisorScope`() = runTest {
        supervisorScope {
            completeStatus("supervisorScope")

            launch(handler) {
                launch {
                    delay(100)
                    throw RuntimeException("oops(❌)")
                }.onCompletion("child1")

                launch {
                    delay(200)
                }.onCompletion("child2")
            }.onCompletion("parent").join()
        }
    }

    /**
     * CEHs installed neither at the scope nor at root coroutines do not take effect.
     */

    @Test
    fun `CEH not at the root coroutine - child of coroutineScope`() = runTest {
        coroutineScope {
            completeStatus("coroutineScope")

            launch(handler) {
                launch {
                    delay(100)
                    throw RuntimeException("oops(❌)")
                }.onCompletion("child1")

                launch { delay(200) }.onCompletion("child2")
            }.onCompletion("parent")
        }
    }

    @Test
    fun `CEH not at the root coroutine - not a direct child of scope`() = runTest {
        val scope = CoroutineScope(Job())

        scope.launch {
            launch(handler) {
                delay(100)
                throw RuntimeException("oops(❌)")
            }.onCompletion("child1")

            launch {
                delay(200)
            }.onCompletion("child2")
        }.onCompletion("parent").join()

        scope.completeStatus("scope") // Is scope cancelled?
    }

    ///////////////////////////////////////////////////////////////////////////
    // CEH & supervisorScope - Quiz: Select two places where CEH is of no use.
    ///////////////////////////////////////////////////////////////////////////
    private val handler1 = CoroutineExceptionHandler { context, exception ->
        log("1. Global CEH: Caught $exception, and handles it in: $context")
    }
    private val handler2 = CoroutineExceptionHandler { context, exception ->
        log("2. Global CEH: Caught $exception, and handles it in: $context")
    }
    private val handler3 = CoroutineExceptionHandler { context, exception ->
        log("3. Global CEH: Caught $exception, and handles it in: $context")
    }
    private val handler4 = CoroutineExceptionHandler { context, exception ->
        log("4. Global CEH: Caught $exception, and handles it in: $context")
    }
    private val handler5 = CoroutineExceptionHandler { context, exception ->
        log("5. Global CEH: Caught $exception, and handles it in: $context")
    }

    @Test
    fun `CEH and supervisorScope - quiz`() = runTest {
        val scope = CoroutineScope(Job() + handler1).onCompletion("top-level scope")

        scope.launch(handler2) {
            supervisorScope {
                onCompletion("supervisorScope")

                launch(handler3) {
                    launch(handler4) {// suspicious location
                        supervisorScope {
                            onCompletion("child supervisorScope")

                            launch(handler5) {
                                launch(handler) {
                                    delay(100); log("I am failing..."); throw RuntimeException("oops(❌)")
                                }.onCompletion("child1")
                                launch(handler) { delay(200) }.onCompletion("child2")
                            }.onCompletion("child supervisorScope's child")
                        }
                    }.onCompletion("supervisorScope's child's child")
                }.onCompletion("supervisorScope job's child")
            }
        }.onCompletion("top-level job").join()
    }
}

