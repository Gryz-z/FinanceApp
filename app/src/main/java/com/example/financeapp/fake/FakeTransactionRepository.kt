package com.example.financeapp.fake

import com.example.financeapp.domain.model.Range
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TxType
import com.example.financeapp.domain.model.ports.TransactionRepository // <-- CORREGIDO
import kotlin.math.max

class FakeTransactionRepository : TransactionRepository {

    private val data = mutableListOf<Transaction>(
        Transaction("t1", TxType.INCOME, 500_000, System.currentTimeMillis() - 2*86400000L, "sueldo", "Pago mensual"),
        Transaction("t2", TxType.EXPENSE, 15_000, System.currentTimeMillis() - 1*86400000L, "comida", "Pizza"),
        Transaction("t3", TxType.EXPENSE, 30_000, System.currentTimeMillis() - 6*86400000L, "internet", "Plan hogar"),
        Transaction("t4", TxType.EXPENSE, 10_000, System.currentTimeMillis() - 3*86400000L, "ocio", "Cine"),
    )

    override suspend fun add(tx: Transaction): Result<Unit> {
        data.add(tx); return Result.success(Unit)
    }

    override suspend fun update(tx: Transaction): Result<Unit> {
        val i = data.indexOfFirst { it.id == tx.id }
        if (i != -1) data[i] = tx
        return Result.success(Unit)
    }

    override suspend fun delete(id: String): Result<Unit> {
        data.removeAll { it.id == id }
        return Result.success(Unit)
    }

    override suspend fun getByRange(range: Range, anchorEpoch: Long): List<Transaction> {
        val window = when (range) {
            Range.DAY -> 1
            Range.WEEK -> 7
            Range.MONTH -> 30
        }
        val from = anchorEpoch - window * 86_400_000L
        return data.filter { it.dateMillis in (from..anchorEpoch) }
            .sortedByDescending { it.dateMillis }
    }

    override suspend fun getTotalsByCategory(range: Range, anchorEpoch: Long): Map<String, Long> {
        val list = getByRange(range, anchorEpoch)
        return list.filter { it.type == TxType.EXPENSE }
            .groupBy { it.category.lowercase() }
            .mapValues { (_, v) -> max(0L, v.sumOf { it.amount }) }
    }
}