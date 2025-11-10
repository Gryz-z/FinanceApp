package com.example.financeapp.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.financeapp.domain.model.TxType
import java.util.Date

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Long,
    val type: TxType,
    val date: Date,
    val description: String,
    val categoryId: Long,
    val userId: Long = 0 // <-- AÑADIDO VALOR POR DEFECTO
)