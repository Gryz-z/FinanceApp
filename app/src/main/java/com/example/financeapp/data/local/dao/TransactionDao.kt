package com.example.financeapp.data.local.dao

import androidx.room.*
import com.example.financeapp.data.local.model.TransactionEntity
import java.util.Date

data class CategorySpending(
    val categoryId: Long,
    val total: Long
)

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    // Filtro por userId para que solo borres TUS gastos
    @Query("DELETE FROM transactions WHERE id = :transactionId AND userId = :userId")
    suspend fun deleteTransactionById(transactionId: Long, userId: Long)

    // --- CONSULTA CORREGIDA ---
    // (Añadí paréntesis y me aseguré de que 'BETWEEN' esté completo)
    @Query("SELECT * FROM transactions WHERE (date BETWEEN :startDate AND :endDate) AND userId = :userId ORDER BY date DESC")
    suspend fun getTransactionsByRange(startDate: Date, endDate: Date, userId: Long): List<TransactionEntity>

    // --- CONSULTA CORREGIDA ---
    // (Añadí un espacio antes de 'GROUP BY')
    @Query("SELECT categoryId, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' AND (date BETWEEN :startDate AND :endDate) AND userId = :userId GROUP BY categoryId")
    suspend fun getSpendingByCategory(startDate: Date, endDate: Date, userId: Long): List<CategorySpending>
}