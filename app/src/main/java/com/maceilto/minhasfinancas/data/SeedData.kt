package com.maceilto.minhasfinancas.data

import java.util.UUID

object SeedData {
    private fun monthly(
        type: String,
        description: String,
        category: String,
        cents: Long,
        date: String,
        status: String = "pending",
        notes: String = ""
    ) = TransactionEntity(
        type = type,
        description = description,
        category = category,
        amountCents = cents,
        dueDate = date,
        status = status,
        recurrence = "monthly",
        recurrenceId = UUID.randomUUID().toString(),
        notes = notes
    )

    fun initial(): List<TransactionEntity> = listOf(
        TransactionEntity(
            type = "income",
            description = "Saldo inicial disponível",
            category = "Saldo inicial",
            amountCents = 422120,
            dueDate = "2026-09-14",
            status = "paid",
            notes = "R$ 3.735,20 em caixa + R$ 486,00 no Banco do Brasil"
        ),

        monthly("income", "Visão Ferragens", "Sistemas", 35000, "2026-09-20"),
        monthly("income", "Casa do Criador", "Sistemas", 16000, "2026-09-26"),
        monthly("income", "RFK", "Sistemas", 12000, "2026-09-30"),
        monthly("income", "Peixaria", "Sistemas", 38000, "2026-09-30"),
        monthly("expense", "Velo", "Sistemas", 50000, "2026-09-30"),

        TransactionEntity(
            type = "expense",
            description = "Cartão Banco do Brasil",
            category = "Cartão",
            amountCents = 96306,
            dueDate = "2026-09-20",
            status = "pending"
        ),
        TransactionEntity(
            type = "expense",
            description = "Pagamento da casa",
            category = "Casa",
            amountCents = 160000,
            dueDate = "2026-09-28",
            status = "pending"
        ),

        monthly("expense", "Aluguel", "Casa", 40000, "2026-09-10", "paid", "Setembro já pago"),
        monthly("expense", "Internet", "Casa", 12000, "2026-09-10", "paid", "Setembro já pago"),
        monthly("expense", "Claro móvel", "Telefone", 6000, "2026-09-15"),
        monthly("expense", "Compras do mês", "Alimentação", 50000, "2026-09-30"),

        monthly(
            "income",
            "Salário",
            "Salário",
            300000,
            "2026-10-07",
            "pending",
            "Valor aproximado informado"
        )
    )
}
