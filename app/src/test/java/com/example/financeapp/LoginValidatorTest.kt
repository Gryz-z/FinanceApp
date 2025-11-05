package com.example.financeapp

import org.junit.Assert.*
import org.junit.Test
import android.util.Patterns

class LoginValidatorTest {

    @Test
    fun `email valido`() {
        assertTrue(Patterns.EMAIL_ADDRESS.matcher("test@mail.com").matches())
        assertFalse(Patterns.EMAIL_ADDRESS.matcher("malcorreo@").matches())
    }

    @Test
    fun `password minimo 6 caracteres`() {
        fun valido(pass: String) = pass.length >= 6
        assertTrue(valido("123456"))
        assertFalse(valido("123"))
    }
}
