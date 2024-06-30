package com.scarlet.mockk.data

class Phone {
    fun call(person: Person) {
        println("Place phone call from ${person.name}")
    }
}