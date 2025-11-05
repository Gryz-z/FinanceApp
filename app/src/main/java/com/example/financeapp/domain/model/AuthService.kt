package com.example.financeapp.domain

import com.example.financeapp.domain.model.User
/**
 * Contrato mínimo de autenticación que usa el LoginViewModel.
 */
interface AuthService {
    suspend fun signIn(email: String, password: String): Result<User>
    suspend fun signUp(email: String, password: String): Result<User>
    suspend fun logout()
    suspend fun currentUser(): User?
    fun currentToken(): String?
}
