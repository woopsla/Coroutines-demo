package com.scarlet.mockk

import com.google.common.truth.Truth.assertThat
import com.scarlet.mockk.data.*
import io.mockk.*
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.impl.annotations.SpyK
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import kotlin.concurrent.thread

@ExperimentalCoroutinesApi
class MockKTest {

    @MockK
    lateinit var car1: Car

    @RelaxedMockK   // same as @MockK(relaxed = true)
    lateinit var car2: Car

    @MockK(relaxUnitFun = true)
    lateinit var car3: Car

    @SpyK
    var car4 = Car("Ford", false, 2021)

    /**
     * Create a mock
     */

    @Before
    fun init() {
        // init mocks
        MockKAnnotations.init(this)

        //region Lenient Mocks
        // Applies to all mocks
//        MockKAnnotations.init(this, relaxed = true)
//        MockKAnnotations.init(this, relaxUnitFun = true)
        //endregion
    }

    /**
     * Mocked objects in MockK resemble Mockito’s STRICT_STUBS mode by default.
     * If a method is not stubbed, then it will throw. This makes it easier to
     * catch methods that are being called when you do not expect it, or when
     * methods are being called with different arguments.
     */
    @Test(expected = MockKException::class)
    fun `MockK - mock creation`() {
        val mockedPath = mockk<Path>()

        mockedPath.fileName() // throws because the method was not stubbed
    }

    /**
     * Mockito’s default lenient behaviour can be replicated with the relaxed setting.
     * Relaxed mocks will have default stubs for all methods.
     */
    @Test
    fun `MockK - lenient mock creation`() {
        val mockedPath = mockk<Path>(/*relaxed = true*/)

        val fileName = mockedPath.fileName()
        assertThat(fileName).isEmpty()
    }

    @Test
    fun `MockK - lenient mock creation, only for Unit return fun`() {
        val mockedPath = mockk<Path>(/*relaxUnitFun = true*/)

        mockedPath.writeText("hello")      // returns Unit, will not throw

//        mockedPath.fileName()   // throws as the method returns Boolean
    }

    /**
     * When and doXXX
     *
     * In MockK, all stubs can be written with the `every` method.
     * `every` starts a stubbing block and uses anonymous functions and infix
     * functions to define the stub.
     */
    @Test
    fun `MockK - returns - there is no doXXX`() {
        val mockedPath = mockk<Path>()

        every { mockedPath.readText() } returns "hello world"
    }

    @Test
    fun `MockK - throws`() {
        val mockedPath = mockk<Path>(relaxed = true)

        // when readText() throw RuntimeException
        every { mockedPath.readText() } throws RuntimeException()
    }

    @Test
    fun `MockK - answers`() {
        val mockedPath = mockk<Path>()

        every { mockedPath.writeText(any()) } answers { call ->
            println("called with arguments: " + call.invocation.args.joinToString())
        }

        every { mockedPath.writeText(any()) } answers { call ->
            println("called with an argument: " + call.invocation.args[0])
        }

        mockedPath.writeText("hello")
    }

    @Test
    fun `MockK - returns and other variants for void method`() {
        val mockedPath = mockk<Path>()

        every { mockedPath.writeText(any()) } returns Unit
        every { mockedPath.writeText(any()) } answers { }
        every { mockedPath.writeText(any()) } just Runs
        justRun { mockedPath.writeText(any()) }
    }

    /**
     * Consecutive calls
     */

    @Test
    fun `MockK - consecutive calls`() {
        val mockedPath = mockk<Path>()

        // Chain multiple calls
        every { mockedPath.readText() } returns "read 1" andThen "read 2" andThen "read 3"

        //region Shorthand using a list
//        every {
//            mockedPath.readText()
//        } returnsMany listOf("read 1", "read 2", "read 3") andThenMany listOf(
//            "read 4",
//            "read 5",
//            "read 6"
//        )
        //endregion

        //region Use Different Answer Types
//        every { mockedPath.readText() } returns "successful read" andThenThrows RuntimeException()
        //endregion

        println(mockedPath.readText())
        println(mockedPath.readText())
        println(mockedPath.readText())
        println(mockedPath.readText())
    }

    @Test
    fun `request many times()`() {
        val mockedPath = mockk<Path>()

        every { mockedPath.readText() } returns "hello" andThen "mellow"

        println(mockedPath.readText())
        println(mockedPath.readText())
        println(mockedPath.readText())
        println(mockedPath.readText())
        println(mockedPath.readText())
    }

    /**
     * `eq` and `anyXXX`
     *
     * When creating a stub or verifying a call, Mockito provides many different
     * "argument matchers". Besides `eq`, the most commonly used are the `any` family:
     * `any`, `anyBoolean`, `anyByte`, `anyChar`, `anyDouble`, `anyFloat`, `anyInt`, `anyLong`,
     * `anyObject`, `anyShort`, and `anyString`.
     *
     * In MockK, these variations are all replaced by a single `any` matcher.
     */

    @Test
    fun `MockK - eq by default`() {
        val mockCodec = mockk<PasswordCodec>()

        every { mockCodec.encode("hello", "RSA") } returns "olleh"
        every { mockCodec.encode(any(), any()) } returns "olleh"

        // No problem!
        every { mockCodec.encode("hello", any()) } returns "olleh"
    }

    /**
     * Verify
     *
     * MockK uses inline functions and keyword arguments in place of Mockito’s verification modes.
     */

    @Test
    fun `MockK - verify`() {
        val mockedPath = mockk<Path>()

        every { mockedPath.readText() } returns "hello, world"

        mockedPath.readText()

        verify { mockedPath.readText() }
    }

    /**
     * Verification Mode:
     *
     * Mockito lets extra arguments such as `never()` be passed to verify in the
     * second parameter, all of which implement a `VerificationMode` interface.
     *
     * MockK has equivalents for these modes as keyword arguments in `verify`.
     */

    @Test
//    @Ignore
    fun `MockK - verification mode1`() {
        val mockedPath = mockk<Path>(relaxed = true)

        mockedPath.writeText("olleh")
        verify(inverse = true) { mockedPath.writeText("hello") }

        mockedPath.readText()
        mockedPath.readText()
        mockedPath.readText()
        verify(atLeast = 3) { mockedPath.readText() }
        verify(atMost = 3) { mockedPath.readText() }
        verify(exactly = 3) { mockedPath.readText() }

        mockedPath.writeText("hello")
        verify(atLeast = 1) { mockedPath.writeText("hello") }
        verify(atMost = 1) { mockedPath.writeText("hello") }
    }

    @Test
    fun `MockK - verification mode2`() {
        val mockedPath = mockk<Path>()
        every { mockedPath.readText() } returns "hello, world"

        thread {
            Thread.sleep(100)
            mockedPath.readText()
        }

        verify(timeout = 200) { mockedPath.readText() }
    }

    @Test
    fun `MockK - verification mode3`() {
        val mockOne = mockk<Path>()
        val mockTwo = mockk<Path>()
        val mockThree = mockk<Path>()

        verify { mockOne wasNot Called }
        verify { listOf(mockTwo, mockThree) wasNot Called }

        val mockFour = mockk<Path>()
        every { mockFour.readText() } returns "hello"

        mockFour.readText()

        verify { mockFour.readText() }
        confirmVerified(mockFour /*, mockFive */)
    }

    @Test
    fun `Mockito - inOrder verification`() {
        // A single mock whose methods must be invoked in a particular order
        val singleMock = mockk<MutableList<String>> {
            every { add("was added first") } returns true
            every { add("was added second") } returns true
            every { add("was added third") } returns true
        }

        //using a single mock
        singleMock.add("was added first")
        singleMock.add("was added second")
        singleMock.add("was added third")

        //following will make sure that `add` is first called with "was added first",
        // then with "was added second"
        verifyOrder {
            singleMock.add("was added first")
            singleMock.add("was added third")
        }

        verifyOrder(inverse = true) {
            singleMock.add("was added third")
            singleMock.add("was added first")
        }

        verifySequence {
            singleMock.add("was added first")
            singleMock.add("was added second")
            singleMock.add("was added third")
        }

        verifyAll {
            singleMock.add("was added third")
            singleMock.add("was added first")
            singleMock.add("was added second")
        }
    }

    /**
     * argThat - match
     */

    /**
     * MockK has a similar argument matcher called `match`. Just like `argThat`,
     * you can pass a lambda that returns a boolean.
     */
    @Test
    fun `MockK - match`() {
        val mockedCar = mockk<Car>(relaxed = true)

        every {
            mockedCar.drive(1000, match { it.dieselEngine })
        } returns 1500

        assertThat(mockedCar.drive(1000, Engine(true))).isEqualTo(1500)
        assertThat(mockedCar.drive(1000, Engine(false))).isEqualTo(0)
    }

    /**
     * `ArgumentCaptor`
     *
     * MockK has a similar utility called a `CapturingSlot`. The functionality is
     * very similar to Mockito, but the usage is different. Rather than calling
     * the method `argumentCaptor.capture()` to create a argument matcher, you wrap
     * the slot in a `capture()` function. You access the captured value using the
     * `slot.captured` property rather than the `argumentCaptor.value` getter.
     *
     * As an alternative to `CapturingSlot`, a `MutableList` can be used to store
     * captured arguments. Simply pass an instance of a mutable list to capture
     * instead of the slot. This allows you to record all captured values, as
     * `CapturingSlot` only records the most recent value.
     */

    @Test
    fun `MockK - CapturingSlot`() {
        // Given
        val mockPhone = mockk<Phone>()
        val personSlot = slot<Person>()
        every { mockPhone.call(capture(personSlot)) } returns Unit

        // When
        mockPhone.call(Person("Sarah Jane", 33))

        // Then
        verify {
            mockPhone.call(Person("Sarah Jane", 33))
        }
        assertThat(personSlot.captured.name).isEqualTo("Sarah Jane")
    }

    @Test
    fun `MockK - MutableList - demo1`() {
        // Given
        val mockPhone = mockk<Phone>()
        val personSlots = mutableListOf<Person>()
        every { mockPhone.call(capture(personSlots)) } returns Unit

        // When
        mockPhone.call(Person("Sarah Jane", 33))
        mockPhone.call(Person("Peter Parker", 25))

        // Then
        verify(exactly = 2) {
            mockPhone.call(any())
        }

        assertThat(
            personSlots.map { it.name }
        ).isEqualTo(listOf("Sarah Jane", "Peter Parker"))
    }

    @Test
    fun `MockK - MutableList - demo2`() {
        // Given
        val mockPhone = mockk<Phone>()
        val personSlots = mutableListOf<Person>()
        every { mockPhone.call(any()) } returns Unit

        // When
        mockPhone.call(Person("Sarah Jane", 33))
        mockPhone.call(Person("Peter Parker", 25))

        // Then
        verify(exactly = 2) {
            mockPhone.call(capture(personSlots))
        }
        assertThat(
            personSlots.map { it.name }
        ).isEqualTo(listOf("Sarah Jane", "Peter Parker"))
    }


    /**
     * Inline Assertions: `withArg` - special matcher available in verification mode only
     */
    @Test
    fun `MockK - Inline Assertion`() {
        val mockPhone = mockk<Phone> {
            justRun { call(ofType(Person::class)) } // any() instead of ofType() works
        }

        mockPhone.call(Person("Sarah Jane", 33))

        verify {
            mockPhone.call(withArg { person ->
                assertThat(person.name).isEqualTo("Sarah Jane")
            })
        }
    }

    /**
     * Spy
     */

    @Test
    fun `MockK - Spy demo1`() {
        // Arrange (Given)
        val list: MutableList<String> = mutableListOf()
        val listSpy = spyk(list)

        // Act (When)
        listSpy.add("first-element")
        println(listSpy.size)

        // Assert (Then)
        assertThat(listSpy[0]).isEqualTo("first-element")
    }

    @Test
    fun `MockK - Spy demo2`() {
        // Arrange (Given)
        val list: MutableList<String> = mutableListOf()
        val listSpy = spyk(list)

        // Act (When)
        listSpy.add("first-element")

        // Assert (Then)
        assertThat(listSpy[0]).isEqualTo("first-element")

        // Act (When) -- Don't worry
        every { listSpy[0] } returns "second-element"

        // Assert (Then)
        assertThat(listSpy[0]).isEqualTo("second-element")
    }

    @Test
    fun `MockK - Spy demo3`() {
        // Arrange (Given)
        val list: List<String> = mutableListOf()
        val listSpy = spyk(list)

        // Act (When) -- no side effect!
        every { listSpy[0] } returns "second-element"

        // Assert (Then)
        assertThat(listSpy[0]).isEqualTo("second-element")
    }

}