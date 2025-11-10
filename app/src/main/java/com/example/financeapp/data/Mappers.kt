package com.example.financeapp.data

import com.example.financeapp.data.local.dao.CategoryDao
import com.example.financeapp.data.local.model.CategoryEntity
import com.example.financeapp.data.local.model.TransactionEntity
import com.example.financeapp.data.local.model.UserEntity
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.User
import java.util.Date

/*
 * TRADUCTORES DE TRANSACCIONES
 */

fun Transaction.toEntity(): TransactionEntity {
    return TransactionEntity(
        id = this.id.toLongOrNull() ?: 0,
        amount = this.amount,
        type = this.type,
        date = Date(this.dateMillis),
        description = this.note ?: "",
        categoryId = 0
    )
}

fun TransactionEntity.toDomain(categoryName: String): Transaction {
    return Transaction(
        id = this.id.toString(),
        type = this.type,
        amount = this.amount,
        dateMillis = this.date.time,
        category = categoryName,
        note = this.description
    )
}

suspend fun getOrCreateCategory(categoryName: String, categoryDao: CategoryDao): Long {
    val existingCategory = categoryDao.findCategoryByName(categoryName)

    if (existingCategory != null) {
        return existingCategory.id
    } else {
        val newCategory = CategoryEntity(name = categoryName, colorHex = "#CCCCCC")
        return categoryDao.insertCategory(newCategory)
    }
}


// --- AÑADIDO: TRADUCTORES DE USUARIO ---

/**
 * Convierte un [User] (dominio) a un [UserEntity] (base de datos).
 * Necesita el hash de la contraseña que se generará en el repositorio.
 */
fun User.toEntity(passwordHash: String): UserEntity {
    return UserEntity(
        id = this.id.toLongOrNull() ?: 0,
        email = this.email,
        passwordHash = passwordHash
    )
}

/**
 * Convierte un [UserEntity] (base de datos) a un [User] (dominio).
 * Nótese que no expone el passwordHash.
 */
fun UserEntity.toDomain(): User {
    return User(
        id = this.id.toString(),
        email = this.email
    )
}