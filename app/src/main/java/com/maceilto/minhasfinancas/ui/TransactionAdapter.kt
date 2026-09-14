package com.maceilto.minhasfinancas.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.maceilto.minhasfinancas.data.TransactionEntity
import com.maceilto.minhasfinancas.databinding.ItemTransactionBinding
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionAdapter(
    private val onClick: (TransactionEntity) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.Holder>() {

    private var items: List<TransactionEntity> = emptyList()

    fun submitList(newItems: List<TransactionEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    class Holder(val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return Holder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = items[position]
        val b = holder.binding
        val brl = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
        val value = brl.format(item.amountCents / 100.0)
        val date = runCatching {
            LocalDate.parse(item.dueDate)
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }.getOrDefault(item.dueDate)

        b.txtDescription.text = item.description
        b.txtMeta.text = "$date • ${item.category} • ${if (item.status == "paid") "OK" else "Pendente"}"
        b.txtAmount.text = "${if (item.type == "income") "+" else "−"} $value"
        b.txtAmount.setTextColor(
            b.root.context.getColor(
                if (item.type == "income") android.R.color.holo_green_light
                else android.R.color.holo_red_light
            )
        )
        b.root.setOnClickListener { onClick(item) }
    }
}
