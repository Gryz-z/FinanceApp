package com.example.financeapp.ui.dashboard

import com.example.financeapp.domain.model.Transaction

sealed class TxRow {
    data class Section(val title: String): TxRow()
    data class Item(val tx: Transaction): TxRow()
}
