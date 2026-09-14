package com.maceilto.minhasfinancas.data

import java.time.LocalDate
import java.time.YearMonth

object RecurrenceEngine {
    suspend fun ensureMonth(dao: TransactionDao, month: YearMonth) {
        val target = month.toString()
        val groups = dao.getRecurring().groupBy { it.recurrenceId }

        groups.forEach { (recurrenceId, items) ->
            if (recurrenceId == null || items.isEmpty()) return@forEach

            val first = items.minByOrNull { it.dueDate } ?: return@forEach
            val firstMonth = YearMonth.from(LocalDate.parse(first.dueDate))
            if (month < firstMonth) return@forEach

            if (dao.countRecurrenceInMonth(recurrenceId, target) == 0) {
                val source = items.maxByOrNull { it.dueDate } ?: first
                val sourceDate = LocalDate.parse(source.dueDate)
                val day = minOf(sourceDate.dayOfMonth, month.lengthOfMonth())
                val newDate = month.atDay(day).toString()

                dao.insert(
                    source.copy(
                        id = 0,
                        dueDate = newDate,
                        status = "pending",
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }
}
