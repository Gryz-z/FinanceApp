package com.example.financeapp.data.local.model

import androidx.room.TypeConverter
import java.util.Date
import com.example.financeapp.domain.model.TxType

class Converters {
    // Conversor para Date
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Conversor para TxType (Enum)
    @TypeConverter
    fun fromTransactionType(value: String?): TxType? {
        return value?.let { TxType.valueOf(it) }
    }

    @TypeConverter
    fun transactionTypeToString(type: TxType?): String? {
        return type?.name
    }
}