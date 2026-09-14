package com.maceilto.minhasfinancas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,               // income | expense
    val description: String,
    val category: String,
    val amountCents: Long,
    val dueDate: String,            // yyyy-MM-dd
    val status: String = "pending", // pending | paid
    val recurrence: String = "none",// none | monthly
    val recurrenceId: String? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
