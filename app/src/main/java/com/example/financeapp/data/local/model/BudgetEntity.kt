package com.example.financeapp.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val totalAmount: Double,
    val month: Int, // Ej. 11 para Noviembre
    val year: Int // Ej. 2025
)