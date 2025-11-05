package com.example.financeapp.domain.model

import com.example.financeapp.domain.model.*

interface BudgetRepository {
    suspend fun set(b: Budget): Result<Unit>
    suspend fun get(monthEpoch: Long): Budget
}