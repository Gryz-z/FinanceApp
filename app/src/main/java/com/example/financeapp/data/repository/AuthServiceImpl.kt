package com.example.financeapp.data.repository

import android.database.sqlite.SQLiteConstraintException
import com.example.financeapp.data.local.dao.UserDao
import com.example.financeapp.data.local.model.UserEntity
import com.example.financeapp.data.toDomain
import com.example.financeapp.domain.model.User
import com.example.financeapp.domain.model.ports.AuthService
import com.example.financeapp.security.SessionStore

class AuthServiceImpl(
    private val userDao: UserDao,
    private val sessionStore: SessionStore
) : AuthService {

    // ¡¡MUY IMPORTANTE!!
    // Esto NO es seguro. En una app real, NUNCA guardes
    // contraseñas en texto plano. Deberías usar una librería
    // de Hashing (como BCrypt) para crear y comparar un "hash".
    // Por ahora, para que funcione, comparamos texto plano.
    private fun checkPassword(plain: String, hash: String): Boolean {
        return plain == hash
    }


    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val user = userDao.findUserByEmail(email)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            if (checkPassword(password, user.passwordHash)) {
                // Éxito: Guarda el ID del usuario como "token"
                sessionStore.saveToken(user.id.toString())
                Result.success(user.toDomain())
            } else {
                Result.failure(Exception("Contraseña incorrecta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            // El hash es la misma contraseña (INSEGURO, pero funcional para el mock)
            val newUserEntity = UserEntity(email = email, passwordHash = password)

            // Inserta en la DB
            val newId = userDao.registerUser(newUserEntity)

            // Guarda la sesión
            sessionStore.saveToken(newId.toString())

            // Devuelve el nuevo usuario
            val user = User(id = newId.toString(), email = email)
            Result.success(user)

        } catch (e: SQLiteConstraintException) {
            // Esto pasa si el email ya existe (por el "unique" en UserEntity)
            Result.failure(Exception("El email ya está registrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionStore.clear()
    }

    override suspend fun currentUser(): User? {
        return try {
            // 1. Obtiene el ID guardado en la sesión
            val userIdString = sessionStore.getToken()
                ?: return null // No hay sesión

            val userId = userIdString.toLongOrNull()
                ?: return null // El token no es un ID válido

            // 2. Busca al usuario en la DB con ese ID
            val userEntity = userDao.findUserById(userId)

            // 3. Lo convierte al modelo de dominio
            userEntity?.toDomain()
        } catch (e: Exception) {
            null
        }
    }

    // Esta función la pide la interfaz, la implementamos igual que signIn
    override suspend fun login(email: String, password: String): Result<String> {
        return try {
            val user = userDao.findUserByEmail(email)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            if (checkPassword(password, user.passwordHash)) {
                // Éxito: Guarda el ID y lo devuelve como "token"
                sessionStore.saveToken(user.id.toString())
                Result.success(user.id.toString())
            } else {
                Result.failure(Exception("Contraseña incorrecta"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}