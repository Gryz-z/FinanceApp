package com.example.financeapp.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.domain.model.Transaction
import com.example.financeapp.domain.model.TxType
import com.example.financeapp.util.CurrencyCLP.money

sealed interface Row
data class Section(val title: String) : Row
data class RowTx(val tx: Transaction) : Row

class TxAdapter(
    private val onEdit: (Transaction) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<Row>()

    companion object {
        private const val VIEW_SECTION = 0
        private const val VIEW_ITEM = 1
    }

    fun submit(rows: List<Row>) {
        items.clear()
        items.addAll(rows)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is Section -> VIEW_SECTION
        is RowTx -> VIEW_ITEM
    }

    /** Proveedor de SpanSizeLookup para GridLayoutManager */
    fun spanSizeLookup(spanCount: Int) = object : GridLayoutManager.SpanSizeLookup() {
        override fun getSpanSize(position: Int): Int =
            if (getItemViewType(position) == VIEW_SECTION) spanCount else 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inf = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_SECTION) {
            val v = inf.inflate(android.R.layout.simple_list_item_1, parent, false)
            SectionVH(v)
        } else {
            val v = inf.inflate(R.layout.item_tx_card, parent, false)
            TxVH(v, onEdit)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionVH -> holder.bind((items[position] as Section).title)
            is TxVH      -> holder.bind((items[position] as RowTx).tx)
        }
    }

    // ---- ViewHolders ----

    private class SectionVH(v: View) : RecyclerView.ViewHolder(v) {
        private val title = v.findViewById<TextView>(android.R.id.text1)
        fun bind(text: String) { title.text = text }
    }

    private class TxVH(
        v: View,
        private val onEdit: (Transaction) -> Unit
    ) : RecyclerView.ViewHolder(v) {

        private val icon = v.findViewById<ImageView>(R.id.iconType)
        private val category = v.findViewById<TextView>(R.id.txCategory)
        private val note = v.findViewById<TextView>(R.id.txNote)
        private val amount = v.findViewById<TextView>(R.id.txAmount)

        fun bind(tx: Transaction) {
            if (tx.type == TxType.INCOME) {
                icon.setImageResource(android.R.drawable.arrow_up_float)
                // CORRECCIÓN: Usar el color semántico para ingresos
                icon.imageTintList = icon.context.getColorStateList(R.color.accent_green)
            } else {
                icon.setImageResource(android.R.drawable.arrow_down_float)
                // CORRECCIÓN: Usar el color semántico para gastos
                icon.imageTintList = icon.context.getColorStateList(R.color.accent_red)
            }
            category.text = tx.category
            note.text = tx.note ?: ""
            amount.text = money(tx.amount)

            // --- ESTAS SON LAS LÍNEAS QUE ACTIVAN EL MARQUEE ---
            category.isSelected = true
            note.isSelected = true
            // --- FIN ---

            itemView.setOnClickListener { onEdit(tx) }
        }
    }
}