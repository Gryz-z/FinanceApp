package com.example.financeapp.data

import android.content.Context
import com.example.financeapp.data.local.AppDatabase
import com.example.financeapp.data.repository.AuthServiceImpl
import com.example.financeapp.data.repository.TransactionRepositoryImpl
import com.example.financeapp.domain.model.ports.AuthService
import com.example.financeapp.domain.model.ports.TransactionRepository
import com.example.financeapp.security.SessionStore

class AppContainer(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    val sessionStore = SessionStore(context)

    // 4. AÑADIDO: Pasamos el 'sessionStore' al repositorio de transacciones
    val transactionRepository: TransactionRepository = TransactionRepositoryImpl(
        transactionDao = database.transactionDao(),
        categoryDao = database.categoryDao(),
        sessionStore = this.sessionStore // <-- AÑADIDO
    )

    val authService: AuthService = AuthServiceImpl(
        userDao = database.userDao(),
        sessionStore = this.sessionStore
    )
}