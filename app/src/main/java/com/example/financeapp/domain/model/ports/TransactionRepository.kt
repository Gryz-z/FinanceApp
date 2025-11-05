package com.example.financeapp.domain.model.ports // <-- CORREGIDO

import com.example.financeapp.domain.model.*

interface TransactionRepository {
    suspend fun add(tx: Transaction): Result<Unit>
    suspend fun update(tx: Transaction): Result<Unit>
    suspend fun delete(id: String): Result<Unit>

    suspend fun getByRange(range: Range, anchorEpoch: Long): List<Transaction>
    suspend fun getTotalsByCategory(range: Range, anchorEpoch: Long): Map<String, Long>
}