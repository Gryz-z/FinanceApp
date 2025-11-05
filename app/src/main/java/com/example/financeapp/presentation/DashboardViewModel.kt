package com.example.financeapp.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.domain.model.Range
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.ports.TransactionRepository
import kotlinx.coroutines.launch

class DashboardViewModel(private val repo: TransactionRepository) : ViewModel() {

    val range = MutableLiveData(Range.MONTH)
    val list = MutableLiveData<List<Transaction>>(emptyList())
    val byCategory = MutableLiveData<Map<String, Long>>(emptyMap())
    val totals = MutableLiveData(Pair(0L, 0L)) // (egresos, ingresos)

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
        }
    }

    // --- CORRECCIÓN: NUEVAS FUNCIONES ---

    /**
     * Guarda una NUEVA transacción en el repositorio y luego recarga todo.
     */
    fun addTx(tx: Transaction) {
        viewModelScope.launch {
            repo.add(tx) // 1. Guardar en el repo
            load()       // 2. Recargar todo (lista, totales y gráfico)
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

    // --- ELIMINADOS ---
    // Ya no necesitamos addTemp ni updateTemp, porque ahora
    // siempre guardamos en el repo y recargamos.
    /*
    fun addTemp(tx: Transaction) { ... }
    fun updateTemp(tx: Transaction) { ... }
    */
}