package com.scarlet.junit

import org.junit.Test

/**
 * TODO: 1. Introduce two ways to create custom test rules.
 * TODO: 2. Introduce RuleChain to chain multiple rules.
 *   - RuleChain.outerRule(rule1).around(rule2).around(rule3)
 *   - @get:Rule(order = 1)
 */
class TestRuleTest {

//    @Before
//    fun init() {
//        println("before ...")
//    }
//
//    @After
//    fun teardown() {
//        println("after ...")
//    }

    @Test
    fun testRules() {
        // Arrange (Given)

        // Act (When)
        println("\ttesting ...")

        // Assert (Then)
    }
}
