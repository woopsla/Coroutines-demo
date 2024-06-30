package com.scarlet.mockk.data

class Engine(val dieselEngine: Boolean)

class Car(val model: String, val isDiesel: Boolean, val year: Int) {

    init {
        count++
    }

    private var odometer: Int = 0
    val dieselEngine = isDiesel

    fun drive(miles: Int, engineType: Engine): Int {
        odometer += miles
        return odometer
    }

    companion object {
        var count: Int = 0
            private set
    }
}