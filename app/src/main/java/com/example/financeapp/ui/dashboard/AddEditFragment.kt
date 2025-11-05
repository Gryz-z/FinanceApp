package com.example.financeapp.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
// CORRECCIÓN 1: Importar la clase necesaria
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.financeapp.R
import com.example.financeapp.databinding.FragmentAddEditBinding
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TxType

class AddEditFragment : Fragment() {
    private var _b: FragmentAddEditBinding? = null
    private val b get() = _b!!

    private var editingTx: Transaction? = null

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentAddEditBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Si viene algo para editar, precargar
        editingTx = arguments?.getParcelable("txToEdit")

        // Acción del botón atrás en la Toolbar
        b.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        // --- CORRECCIÓN 1: Sintaxis del addCallback ---
        // Manejar el botón "atrás" del sistema (del celular)
        val callback = object : OnBackPressedCallback(true /* enabled */) {
            override fun handleOnBackPressed() {
                // Ejecuta la misma acción que el botón de la toolbar
                findNavController().popBackStack()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        // --- FIN DE LA CORRECCIÓN 1 ---

        // Cambia título según modo
        if (editingTx != null) {
            b.toolbar.title = getString(R.string.edit_title)
            val tx = editingTx!!
            if (tx.type == TxType.INCOME) b.toggleType.check(R.id.btnIncome)
            else b.toggleType.check(R.id.btnExpense)
            b.inputCategory.setText(tx.category)
            b.inputAmount.setText(tx.amount.toString())
            b.inputNote.setText(tx.note ?: "")
        } else {
            b.toolbar.title = getString(R.string.add_title)
            b.toggleType.check(R.id.btnExpense)
        }

        b.btnCancel.setOnClickListener { findNavController().popBackStack() }

        b.btnSave.setOnClickListener {
            val category = b.inputCategory.text?.toString()?.ifBlank { "otros" } ?: "otros"
            val amount   = b.inputAmount.text?.toString()?.toLongOrNull() ?: 0L
            val note     = b.inputNote.text?.toString()

            // --- CORRECCIÓN 2: Bug en la lógica del 'else' ---
            val type     = if (b.toggleType.checkedButtonId == R.id.btnIncome)
                TxType.INCOME else TxType.EXPENSE // <-- Estaba como INCOME

            val now = System.currentTimeMillis()
            val out = (editingTx?.copy(
                type = type,
                amount = amount,
                category = category,
                note = note
            )) ?: Transaction(
                id = "local-$now",
                type = type,
                amount = amount,
                dateMillis = now,
                category = category,
                note = note
            )

            parentFragmentManager.setFragmentResult(
                if (editingTx != null) "update_result" else "add_result",
                bundleOf("tx" to out)
            )
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}