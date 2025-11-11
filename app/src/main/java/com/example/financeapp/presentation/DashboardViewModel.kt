package com.example.financeapp.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.domain.model.Range
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.ports.TransactionRepository
import kotlinx.coroutines.launch
import java.util.Calendar // <-- 1. IMPORT AÑADIDO

class DashboardViewModel(private val repo: TransactionRepository) : ViewModel() {

    val range = MutableLiveData(Range.MONTH)
    val list = MutableLiveData<List<Transaction>>(emptyList())
    val byCategory = MutableLiveData<Map<String, Long>>(emptyMap())
    val totals = MutableLiveData(Pair(0L, 0L)) // (egresos, ingresos)

    // --- 2. LIVE DATA AÑADIDO (para el gráfico de barras) ---
    // Guardará los gastos sumados por día de la semana (Calendar.MONDAY, etc.)
    val byDay = MutableLiveData<Map<Int, Long>>(emptyMap())


    // Carga los datos desde el repositorio
    fun load(anchor: Long = System.currentTimeMillis()) {
        viewModelScope.launch {
            val r = range.value ?: Range.MONTH
            val txs = repo.getByRange(r, anchor)
            list.value = txs
            byCategory.value = repo.getTotalsByCategory(r, anchor)

            val egresos = txs.filter { it.type.name == "EXPENSE" }.sumOf { it.amount }
            val ingresos = txs.filter { it.type.name == "INCOME" }.sumOf { it.amount }
            totals.value = egresos to ingresos

            // --- 3. LÓGICA AÑADIDA (Tu parte, Isaac) ---
            // Llama a la nueva función para calcular los gastos por día
            byDay.value = calculateDailySpending(txs, r)
        }
    }

    /**
     * --- 4. FUNCIÓN DE LÓGICA DE NEGOCIO AÑADIDA ---
     * Toma una lista de transacciones y las agrupa por día de la semana.
     */
    private fun calculateDailySpending(txs: List<Transaction>, range: Range): Map<Int, Long> {
        // Solo nos interesan los gastos
        val expenses = txs.filter { it.type.name == "EXPENSE" }

        // 1. Crear un mapa vacío para los 7 días de la semana
        // (Calendar.SUNDAY = 1, MONDAY = 2, ..., SATURDAY = 7)
        val dailyTotals = mutableMapOf<Int, Long>().apply {
            (Calendar.SUNDAY..Calendar.SATURDAY).forEach { day ->
                put(day, 0L)
            }
        }

        // 2. Sumar los gastos en el día correspondiente
        val calendar = Calendar.getInstance()
        for (tx in expenses) {
            calendar.timeInMillis = tx.dateMillis
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            dailyTotals[dayOfWeek] = (dailyTotals[dayOfWeek] ?: 0L) + tx.amount
        }

        // 3. Si el rango es 'Día', el mapa es muy fome (solo 1 día).
        // En ese caso, devolvemos el mapa vacío para que el gráfico no se muestre.
        if (range == Range.DAY) {
            return emptyMap()
        }

        return dailyTotals
    }

    /**
     * Guarda una NUEVA transacción en el repositorio y luego recarga todo.
     */
    fun addTx(tx: Transaction) {
        viewModelScope.launch {
            repo.add(tx) // 1. Guardar en el repo
            load()       // 2. Recargar todo (lista, totales y gráficos)
        }
    }

    /**
     * Actualiza una transacción existente en el repositorio y luego recarga todo.
     */
    fun updateTx(tx: Transaction) {
        viewModelScope.launch {
            repo.update(tx) // 1. Actualizar en el repo
            load()          // 2. Recargar todo
        }
    }
}