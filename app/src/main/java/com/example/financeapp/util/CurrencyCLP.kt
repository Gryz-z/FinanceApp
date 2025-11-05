package com.example.financeapp.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyCLP {
    private val nf = NumberFormat.getCurrencyInstance(Locale("es", "CL")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    fun money(amount: Long): String = nf.format(amount)
}
