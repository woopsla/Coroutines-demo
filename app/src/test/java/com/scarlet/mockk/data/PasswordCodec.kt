package com.scarlet.mockk.data

interface PasswordCodec {
    fun encode(plainText: String, scheme: String = "RSA"): String
    fun decode(cipherText: String, scheme: String = "RSA"): String
    fun reset(): Unit
}