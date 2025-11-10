package com.example.financeapp.data.utils

import com.example.financeapp.domain.model.Range
import java.util.Calendar
import java.util.Date

// Objeto para cálculos de fechas
object DateUtils {

    // Esta función convierte el Enum 'Range' en un par de fechas (Inicio, Fin)
    fun getStartAndEndDates(range: Range, anchorEpoch: Long): Pair<Date, Date> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = anchorEpoch
        }

        // Resetea la hora para el inicio del día
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val startDate: Date
        val endDate: Date

        when (range) {
            Range.DAY -> {
                startDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                endDate = calendar.time
            }
            Range.WEEK -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                startDate = calendar.time
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                endDate = calendar.time
            }
            Range.MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = calendar.time
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                endDate = calendar.time
            }
            // EL 'ELSE' FUE ELIMINADO PORQUE ES REDUNDANTE
        }
        return Pair(startDate, endDate)
    }
}