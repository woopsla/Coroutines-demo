package com.scarlet.junit

import org.junit.*

class JUnit4Test {
    @Before
    fun init() {
        println("\t@Before")
    }

    @After
    fun teardown() {
        println("\t@After")
    }

    @Test
    fun test() {
        println("\t\t@Test test")
    }

    @Test
    fun testAnother() {
        println("\t\t@Test another test")
    }

    @Ignore
    fun ignoredTest1() {
        Assert.fail("Not yet implemented")
    }

    @Ignore("Ignore for this testing")
    fun ignoredTest2() {
        /**/
    }

    companion object {
        @JvmStatic
        @BeforeClass
        fun setup(): Unit {
            println("@BeforeClass")
        }

        @JvmStatic
        @AfterClass
        fun wrapUp(): Unit {
            println("@AfterClass")
        }
    }
}
