package com.example.financeapp.domain.ports

import com.example.financeapp.domain.model.Budget

interface BudgetRepository {
    suspend fun set(b: Budget): Result<Unit>
    suspend fun get(monthEpoch: Long): Budget
}
