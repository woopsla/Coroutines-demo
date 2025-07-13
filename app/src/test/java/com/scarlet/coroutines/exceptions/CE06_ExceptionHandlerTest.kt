package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ExceptionHandlerTest {

    /**
     * **Coroutine Exception Handler (CEH)**
     *
     * - `CoroutineExceptionHandler` is a last-resort mechanism for global "catch all" behavior.
     *      - You cannot recover from the exception in the `CoroutineExceptionHandler`.
     *      - Normally, the handler is used to log the exception, show some kind of error message,
     *        terminate, and/or restart the application.
     * - CEH should be installed in either _scopes_ or the _root coroutines_.
     *      - The exception is handled by the parent only after all its children terminate.
     * - CEH can handle only _uncaught propagated exceptions_.
     *      - Only `launch` propagated exceptions are considered. Normally, uncaught exceptions
     *        can only result from root coroutines created using the `launch` builder.
     *      - CEH installed in `launch` root coroutines takes effect.
     *      - But, CEH installed in `async` root coroutines _has no effect_ at all!
     */
    private val handler = CoroutineExceptionHandler { context, exception ->
        log("Global CEH: Caught $exception, and handles it in: $context")
    }

    /**
     * Coroutine Exception Handlers installed at scope
     */

    @Test
    fun `CEH at the scope`() = runTest {
        log("test started ...")

        // What happen to scope's Job?
        val scope = CoroutineScope(Job() + handler).onCompletion("scope")

        scope.launch {
            log("parent coroutine: $coroutineContext")

            launch {
                delay(100)
                throw RuntimeException("oops(❌)")
            }.onCompletion("child1")

            launch {
                delay(200)
            }.onCompletion("child2")
            // Will child2 be cancelled? - What is the take-away from this fact?
        }.onCompletion("parent").join()
    }

    /**
     * Coroutine Exception Handlers installed at root coroutines
     */

    @Test
    fun `CEH at the root coroutine - child of scope`() = runTest {
        // What happen to scope's Job?
        val scope = CoroutineScope(Job()).onCompletion("scope")

        scope.launch(handler) {
            launch {
                delay(100)
                throw RuntimeException("Oops(❌)")
            }.onCompletion("child1")

            launch {
                delay(200)
            }.onCompletion("child2")
        }.onCompletion("parent").join()
    }

    @Test
    fun `CEH at the root coroutine - child of supervisorScope`() = runTest {
        supervisorScope {
            onCompletion("supervisorScope")

            launch(handler) {
                launch {
                    delay(100)
                    throw RuntimeException("Oops(❌)")
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
            onCompletion("coroutineScope")

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
    // CEH & supervisorScope - Quiz: Select places where CEH is of no use.
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
        // #2
        val scope = CoroutineScope(Job() + handler1).onCompletion("top-level scope")

        // #3
        scope.launch(handler2) {
            // #3
            supervisorScope {
                onCompletion("supervisorScope")

                // #4
                launch(handler3) {
                    // #5
                    launch(handler4) {// suspicious location
                        // #5
                        supervisorScope {
                            onCompletion("child supervisorScope")

                            // #6
                            launch(handler5) {
                                // #7
                                launch(handler) {
                                    delay(100); log("I am failing...")
                                    throw RuntimeException("Oops(❌)")
                                }.onCompletion("child1")

                                // #8
                                launch(handler) { delay(200) }.onCompletion("child2")
                            }.onCompletion("child supervisorScope's child")
                        }
                    }.onCompletion("supervisorScope's child's child")
                }.onCompletion("supervisorScope job's child")
            }
        }.onCompletion("top-level job").join()
    }
}