package com.example.financeapp.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Transaction(
    val id: String,
    val type: TxType,
    val amount: Long,
    val dateMillis: Long,
    val category: String,
    val note: String? = null
) : Parcelable
