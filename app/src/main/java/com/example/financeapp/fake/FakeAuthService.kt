package com.example.financeapp.fake

import com.example.financeapp.domain.model.User
import com.example.financeapp.domain.model.ports.AuthService
import kotlinx.coroutines.delay

/**
 * Servicio de autenticación FALSO para pruebas.
 * Credenciales válidas: demo@demo.cl / 123456
 */
class FakeAuthService : AuthService {

    private var logged: User? = null

    // CORRECCIÓN: Añadida la función 'login' que faltaba
    override suspend fun login(email: String, password: String): Result<String> {
        delay(300)
        return if (email.equals("demo@demo.cl", ignoreCase = true) && password == "123456") {
            // El LoginViewModel espera un String (token), no un User
            Result.success("fake-token-123456")
        } else {
            Result.failure(IllegalArgumentException("Correo o contraseña inválidos"))
        }
    }

    override suspend fun signIn(email: String, password: String): Result<User> {
        delay(300)
        return if (email.equals("demo@demo.cl", ignoreCase = true) && password == "123456") {
            val user = User(id = "u1", email = email.lowercase())
            logged = user
            Result.success(user)
        } else {
            Result.failure(IllegalArgumentException("Correo o contraseña inválidos"))
        }
    }

    override suspend fun signUp(email: String, password: String): Result<User> {
        delay(300)
        val user = User(id = "u_new", email = email.lowercase())
        logged = user
        return Result.success(user)
    }

    override suspend fun logout() {
        delay(100)
        logged = null
    }

    override suspend fun currentUser(): User? = logged
}