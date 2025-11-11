package com.example.financeapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financeapp.FinanceApplication
import com.example.financeapp.R
import com.example.financeapp.databinding.FragmentDashboardBinding
import com.example.financeapp.domain.model.Range
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.presentation.DashboardViewModel
import com.example.financeapp.util.CurrencyCLP.money
// --- IMPORTS DE GRÁFICOS (CORREGIDOS) ---
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
// --- FIN IMPORTS DE GRÁFICOS ---
import com.google.android.material.snackbar.Snackbar
import java.util.Calendar

class DashboardFragment : Fragment() {

    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!

    private lateinit var adapter: TxAdapter

    private val vm by viewModels<DashboardViewModel> {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(c: Class<T>): T {
                val application = requireActivity().application as FinanceApplication
                val appContainer = application.appContainer
                return DashboardViewModel(appContainer.transactionRepository) as T
            }
        }
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDashboardBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(v: View, s: Bundle?) {
        // Filtros
        fun load(range: Range) { vm.range.value = range; vm.load() }
        b.chipMonth.setOnClickListener { load(Range.MONTH) }
        b.chipWeek.setOnClickListener  { load(Range.WEEK)  }
        b.chipDay.setOnClickListener   { load(Range.DAY)   }

        // Botones (Ingresar / Editar)
        b.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addEdit)
        }
        b.btnEdit.setOnClickListener {
            b.rootScroll.smoothScrollTo(0, b.recycler.top)
            Snackbar.make(b.root, getString(R.string.tip_tap_to_edit), Snackbar.LENGTH_SHORT).show()
        }

        // Configuración del Recycler (lista)
        adapter = TxAdapter { tx ->
            val args = bundleOf("txToEdit" to tx)
            findNavController().navigate(R.id.action_dashboard_to_addEdit, args)
        }
        val spanCount = 2
        b.recycler.layoutManager = GridLayoutManager(requireContext(), spanCount).apply {
            spanSizeLookup = adapter.spanSizeLookup(spanCount)
        }
        b.recycler.adapter = adapter

        // Resultados desde Add/Edit
        parentFragmentManager.setFragmentResultListener("add_result", viewLifecycleOwner) { _, bundle ->
            val tx = bundle.getParcelable<Transaction>("tx") ?: return@setFragmentResultListener
            vm.addTx(tx)
        }
        parentFragmentManager.setFragmentResultListener("update_result", viewLifecycleOwner) { _, bundle ->
            val tx = bundle.getParcelable<Transaction>("tx") ?: return@setFragmentResultListener
            vm.updateTx(tx)
        }

        // --- ESTE BLOQUE ESTÁ BIEN ---
        // Obtener el color de texto principal del tema
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        val textColor = typedValue.data
        // --- FIN DEL BLOQUE ---


        // --- CONFIGURACIÓN DE GRÁFICOS ---

        // 1. PieChart (Dona)
        setupPieChart(textColor)
        vm.byCategory.observe(viewLifecycleOwner) { map ->
            updatePieChart(map, textColor)
        }

        // 2. BAR CHART (Barras)
        setupBarChart(textColor)
        vm.byDay.observe(viewLifecycleOwner) { dailyTotals ->
            updateBarChart(dailyTotals, textColor)
        }
        // --- FIN DE CONFIGURACIÓN DE GRÁFICOS ---


        // Observador de la Lista (Recycler)
        vm.list.observe(viewLifecycleOwner) { txs ->
            adapter.submit(buildRows(txs))
        }

        // --- ERROR GRANDE CORREGIDO AQUÍ ---
        // (Decía 'viewDati' en lugar de 'viewLifecycleOwner')
        vm.totals.observe(viewLifecycleOwner) { (egresos, ingresos) ->
            b.totalText.text = getString(R.string.monto_total,  money(egresos))
            b.ingText.text   = getString(R.string.monto_ingresado, money(ingresos))
        }
        // --- FIN DE LA CORRECCIÓN ---

        // Carga inicial
        load(Range.MONTH)
    }

    // --- FUNCIONES DE GRÁFICOS (Separadas para ordenar) ---

    private fun setupPieChart(textColor: Int) {
        b.pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 55f
            setHoleColor(Color.TRANSPARENT)
            setEntryLabelColor(textColor)
            setEntryLabelTextSize(11f)
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
                setTextColor(textColor)
            }
            setNoDataTextColor(Color.LTGRAY)
        }
    }

    private fun updatePieChart(map: Map<String, Long>, textColor: Int) {
        val entries = map.entries.map { (label, value) -> PieEntry(value.toFloat(), label) }

        val set = PieDataSet(entries, getString(R.string.gasto_categoria)).apply {
            setColors(ColorTemplate.MATERIAL_COLORS, 255)
            valueTextColor = textColor
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String = "${value.toInt()}%"
            }
            sliceSpace = 2f
        }
        b.pieChart.data = PieData(set)
        b.pieChart.invalidate()
    }

    /**
     * Configura el estilo del BarChart
     */
    private fun setupBarChart(textColor: Int) {
        b.barChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setDrawValueAboveBar(true)

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                this.textColor = textColor // Asigna el color

                valueFormatter = object : ValueFormatter() {
                    private val days = listOf("D", "L", "M", "M", "J", "V", "S")
                    override fun getFormattedValue(value: Float): String {
                        return days.getOrNull(value.toInt()) ?: ""
                    }
                }
            }

            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            setNoDataTextColor(Color.LTGRAY)
        }
    }

    /**
     * Actualiza los datos del BarChart
     */
    private fun updateBarChart(dailyTotals: Map<Int, Long>, textColor: Int) {
        if (dailyTotals.isEmpty()) {
            b.barChart.data = null
            b.barChart.invalidate()
            return
        }

        val orderedEntries = listOf(
            BarEntry(0f, (dailyTotals[Calendar.SUNDAY] ?: 0L).toFloat()),
            BarEntry(1f, (dailyTotals[Calendar.MONDAY] ?: 0L).toFloat()),
            BarEntry(2f, (dailyTotals[Calendar.TUESDAY] ?: 0L).toFloat()),
            BarEntry(3f, (dailyTotals[Calendar.WEDNESDAY] ?: 0L).toFloat()),
            BarEntry(4f, (dailyTotals[Calendar.THURSDAY] ?: 0L).toFloat()),
            BarEntry(5f, (dailyTotals[Calendar.FRIDAY] ?: 0L).toFloat()),
            BarEntry(6f, (dailyTotals[Calendar.SATURDAY] ?: 0L).toFloat())
        )

        val set = BarDataSet(orderedEntries, "Gasto por día").apply {
            setColors(ColorTemplate.MATERIAL_COLORS, 255)
            valueTextColor = textColor
            valueTextSize = 10f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    if (value == 0f) return ""
                    return (value / 1000).toInt().toString() + "k"
                }
            }
        }

        b.barChart.data = BarData(set)
        b.barChart.invalidate()
    }


    override fun onDestroyView() { _b = null; super.onDestroyView() }
} // <-- FIN DE LA CLASE DashboardFragment


// --- FUNCIÓN 'buildRows' (FUERA de la clase) ---
fun buildRows(txs: List<Transaction>): List<Row> {
    val ingresos = txs.filter { it.type.name == "INCOME" }
    val expenses = txs.filter { it.type.name == "EXPENSE" }
    val catPasivos = listOf("arriendo", "luz", "agua", "internet")
    val catMensuales = listOf("mensual")
    val catOcio = listOf("comida", "salida", "ocio")
    val pasivos = mutableListOf<Transaction>()
    val mensuales = mutableListOf<Transaction>()
    val ocio = mutableListOf<Transaction>()
    val otros = mutableListOf<Transaction>()
    for (tx in expenses) {
        val catLower = tx.category.lowercase()
        when {
            catPasivos.any { catLower.contains(it) } -> pasivos.add(tx)
            catMensuales.any { catLower.contains(it) } -> mensuales.add(tx)
            catOcio.any { catLower.contains(it) } -> ocio.add(tx)
            else -> otros.add(tx)
        }
    }
    fun block(title: String, list: List<Transaction>) =
        if (list.isEmpty()) emptyList()
        else listOf(Section(title)) + list.map { RowTx(it) }
    return buildList {
        addAll(block("Ingresos", ingresos))
        addAll(block("Gastos pasivos", pasivos))
        addAll(block("Gastos mensuales", mensuales))
        addAll(block("Gastos de ocio", ocio))
        addAll(block("Otros gastos", otros))
    }
}