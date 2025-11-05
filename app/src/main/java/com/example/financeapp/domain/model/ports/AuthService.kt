package com.example.financeapp.domain.model.ports

import com.example.financeapp.domain.model.User

/**
 * Puertos de autenticación (contrato de dominio).
 * Firmas usadas por LoginViewModel y los fakes reales.
 */
interface AuthService {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun currentUser(): User?
    suspend fun login(email: String, password: String): Result<String>
}
