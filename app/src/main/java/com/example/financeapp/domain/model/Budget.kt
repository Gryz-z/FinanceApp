package com.example.financeapp.domain.model

// Define la data class 'Budget' que faltaba
data class Budget(
    val monthEpoch: Long,
    val amount: Double,
    val description: String
    // ... agrega cualquier otra propiedad que tu 'Budget' necesite
)