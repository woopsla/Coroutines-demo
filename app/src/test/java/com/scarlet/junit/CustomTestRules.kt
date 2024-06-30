package com.scarlet.junit

import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MyTestRule : TestRule {
    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {

            @Throws(Throwable::class)
            override fun evaluate() {
                try {
                    println("my rule before...")
                    base.evaluate()
                } finally {
                    println("my rule after...")

                    // TODO -- close some resources
                }
            }
        }
    }
}

class YourTestRule : TestWatcher() {
    override fun starting(description: Description) {
        println("your rule before...")
    }

    override fun finished(description: Description) {
        println("your rule after...")
    }
}
