package com.example.financeapp.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.financeapp.R
import com.example.financeapp.databinding.FragmentDashboardBinding
import com.example.financeapp.domain.model.Range
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.presentation.DashboardViewModel
import com.example.financeapp.util.CurrencyCLP.money
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.snackbar.Snackbar
import com.example.financeapp.fake.FakeTransactionRepository


class DashboardFragment : Fragment() {

    private var _b: FragmentDashboardBinding? = null
    private val b get() = _b!!

    private lateinit var adapter: TxAdapter

    private val vm by viewModels<DashboardViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(c: Class<T>): T =
                DashboardViewModel(FakeTransactionRepository()) as T
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

        // Botón Ingresar
        b.btnAdd.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_addEdit)
        }

        // Botón Editar -> desplazarse a la lista + hint
        b.btnEdit.setOnClickListener {
            b.rootScroll.smoothScrollTo(0, b.recycler.top)
            Snackbar.make(b.root, getString(R.string.tip_tap_to_edit), Snackbar.LENGTH_SHORT).show()
        }

        // Recycler...
        adapter = TxAdapter { tx ->
            val args = bundleOf("txToEdit" to tx)
            findNavController().navigate(R.id.action_dashboard_to_addEdit, args)
        }
        val spanCount = 2
        b.recycler.layoutManager = GridLayoutManager(requireContext(), spanCount).apply {
            spanSizeLookup = adapter.spanSizeLookup(spanCount)
        }
        b.recycler.adapter = adapter

        // --- CORRECCIÓN CLAVE AQUÍ ---
        // Resultados desde Add/Edit
        parentFragmentManager.setFragmentResultListener("add_result", viewLifecycleOwner) { _, bundle ->
            // 1. Recuperamos la transacción
            val tx = bundle.getParcelable<Transaction>("tx") ?: return@setFragmentResultListener
            // 2. Llamamos a la nueva función que GUARDA y LUEGO recarga
            vm.addTx(tx)
        }
        parentFragmentManager.setFragmentResultListener("update_result", viewLifecycleOwner) { _, bundle ->
            // 1. Recuperamos la transacción
            val tx = bundle.getParcelable<Transaction>("tx") ?: return@setFragmentResultListener
            // 2. Llamamos a la nueva función que GUARDA y LUEGO recarga
            vm.updateTx(tx)
        }
        // --- FIN DE LA CORRECCIÓN ---


        // Obtener el color de texto principal del tema
        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        val textColor = typedValue.data

        // PieChart... (todo esto está bien)
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

        vm.byCategory.observe(viewLifecycleOwner) { map ->
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

        // Secciones... (esto está bien)
        vm.list.observe(viewLifecycleOwner) { txs ->
            adapter.submit(buildRows(txs))
        }

        // Totales... (esto está bien)
        vm.totals.observe(viewLifecycleOwner) { (egresos, ingresos) ->
            b.totalText.text = getString(R.string.monto_total,  money(egresos))
            b.ingText.text   = getString(R.string.monto_ingresado, money(ingresos))
        }

        load(Range.MONTH)
    }

    override fun onDestroyView() { _b = null; super.onDestroyView() }
}

/** Agrupa en secciones por tipo/categoría (ajústalo a tus criterios) */
private fun buildRows(txs: List<Transaction>): List<Row> {
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