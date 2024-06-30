package com.scarlet.mockk

import com.google.common.truth.Truth.assertThat
import com.scarlet.mockk.MyObject.extensionInObject
import io.mockk.*
import org.junit.Test

/**
 * https://medium.com/@ByteSizedBit/mockk-testing-extension-functions-8b207bcad21d
 */

data class MyClass(val info: Int)

// Top-level extension function
fun MyClass.topLevelExtension() = info + 2

class SomeClass {
    // Class level extension function

    fun MyClass.extensionInClass() = info + 3
}

// Object level extension function
object MyObject {
    fun MyClass.extensionInObject() = info + 1
}

class M04_ExtensionFunctionTest {

    /**
     * You need to add `mockkStatic("file-name-path")`.
     * Make sure you add "Kt" at the end of the file name.
     * Note that this will mock the whole pkg.FileKt class, and not just extensionFunc.
     */
    @Test
    fun `top level extension function test`() {
        mockkStatic("com.scarlet.mockk.M04_ExtensionFunctionTestKt")
//        mockkStatic(MyClass::topLevelExtension)

        every { MyClass(1).topLevelExtension() } returns 3

        assertThat(MyClass(1).topLevelExtension()).isEqualTo(3)
        verify { MyClass(1).topLevelExtension() }

        unmockkStatic(MyClass::topLevelExtension)
    }

    @Test
    fun `Class level extension function test`() {
        with(mockk<SomeClass>()) {
            every { MyClass(1).extensionInClass() } returns 4

            assertThat(MyClass(1).extensionInClass()).isEqualTo(4)
            verify { MyClass(1).extensionInClass() }
        }
    }

    /**
     * The important thing to note here is the` mockkObject(MyObject)`
     * mockkObject is used to build an Object mock.
     */
    @Test
    fun `Object level extension function test`() {
        mockkObject(MyObject)

        with(MyObject) {
            every { MyClass(1).extensionInObject() } returns 2
        }

        assertThat(MyClass(1).extensionInObject()).isEqualTo(2)
        verify { MyClass(1).extensionInObject() }

        unmockkObject(MyObject)
    }
}