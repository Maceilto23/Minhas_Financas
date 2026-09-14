package com.maceilto.minhasfinancas.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.maceilto.minhasfinancas.FinanceApp
import com.maceilto.minhasfinancas.backup.BackupManager
import com.maceilto.minhasfinancas.data.RecurrenceEngine
import com.maceilto.minhasfinancas.data.SeedData
import com.maceilto.minhasfinancas.data.TransactionEntity
import com.maceilto.minhasfinancas.databinding.ActivityMainBinding
import com.maceilto.minhasfinancas.databinding.DialogEntryBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val db by lazy { (application as FinanceApp).database }
    private val dao by lazy { db.transactionDao() }
    private lateinit var adapter: TransactionAdapter
    private var currentMonth: YearMonth = YearMonth.now()
    private var observeJob: Job? = null

    private val folderPicker = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            BackupManager.saveFolder(this, uri)
            toast("Pasta de sincronização salva.")
            updateSyncLabel()
            lifecycleScope.launch {
                val result = BackupManager.backup(this@MainActivity)
                toast(result.getOrElse { it.message ?: "Falha no backup." })
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TransactionAdapter { showEntryDialog(it) }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.btnPrev.setOnClickListener {
            currentMonth = currentMonth.minusMonths(1)
            watchMonth()
        }
        binding.btnNext.setOnClickListener {
            currentMonth = currentMonth.plusMonths(1)
            watchMonth()
        }
        binding.btnAdd.setOnClickListener { showEntryDialog(null) }
        binding.btnPickFolder.setOnClickListener { folderPicker.launch(null) }
        binding.btnSync.setOnClickListener { doBackup() }
        binding.btnRestore.setOnClickListener { confirmRestore() }
        binding.btnSettings.setOnClickListener { showSettings() }

        lifecycleScope.launch {
            if (dao.count() == 0) {
                SeedData.initial().forEach { dao.insert(it) }
            }
            watchMonth()
        }

        updateSyncLabel()
    }

    private fun watchMonth() {
        observeJob?.cancel()
        binding.txtMonth.text = currentMonth.format(
            DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR"))
        ).replaceFirstChar { it.uppercase() }

        observeJob = lifecycleScope.launch {
            RecurrenceEngine.ensureMonth(dao, currentMonth)
            dao.observeMonth(currentMonth.toString()).collectLatest { items ->
                adapter.submitList(items)
                updateSummary(items)
            }
        }
    }

    private fun updateSummary(items: List<TransactionEntity>) {
        val income = items.filter { it.type == "income" }.sumOf { it.amountCents }
        val expense = items.filter { it.type == "expense" }.sumOf { it.amountCents }
        val balance = income - expense

        val prefs = getSharedPreferences("finance_settings", MODE_PRIVATE)
        val reserve = prefs.getLong("reserve_cents", 150000L)
        val goal = prefs.getLong("goal_cents", 661600L)
        val available = (balance - reserve).coerceAtLeast(0)

        binding.txtIncome.text = brl(income)
        binding.txtExpense.text = brl(expense)
        binding.txtBalance.text = brl(balance)
        binding.txtAvailable.text = brl(available)

        binding.txtGoal.text = if (available >= goal) {
            "✅ Meta da obra coberta. Disponível após reserva: ${brl(available)}"
        } else {
            "Meta obra: ${brl(goal)} • falta ${brl((goal - available).coerceAtLeast(0))}"
        }

        val pending = items.filter { it.status == "pending" }
        binding.txtPending.text = if (pending.isEmpty()) {
            "✓ Nenhuma pendência neste mês."
        } else {
            "${pending.size} pendência(s) • próxima: ${pending.first().description} em ${pending.first().dueDate}"
        }
    }

    private fun showEntryDialog(item: TransactionEntity?) {
        val d = DialogEntryBinding.inflate(layoutInflater)

        d.spinnerType.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Despesa", "Receita")
        )
        d.spinnerStatus.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Pendente", "Pago/Recebido")
        )
        d.spinnerRecurrence.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Não repetir", "Mensal")
        )

        if (item != null) {
            d.spinnerType.setSelection(if (item.type == "expense") 0 else 1)
            d.edDescription.setText(item.description)
            d.edCategory.setText(item.category)
            d.edAmount.setText(String.format(Locale.US, "%.2f", item.amountCents / 100.0))
            d.edDate.setText(item.dueDate)
            d.spinnerStatus.setSelection(if (item.status == "pending") 0 else 1)
            d.spinnerRecurrence.setSelection(if (item.recurrence == "monthly") 1 else 0)
            d.edNotes.setText(item.notes)
        } else {
            d.edDate.setText(currentMonth.atDay(1).toString())
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(if (item == null) "Novo lançamento" else "Editar lançamento")
            .setView(d.root)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .apply {
                if (item != null) setNeutralButton("Excluir", null)
            }
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val description = d.edDescription.text.toString().trim()
                val category = d.edCategory.text.toString().trim()
                val date = d.edDate.text.toString().trim()
                val amount = d.edAmount.text.toString().replace(",", ".").toDoubleOrNull()

                if (description.isBlank() || category.isBlank() || amount == null || amount <= 0) {
                    toast("Preencha descrição, categoria e valor.")
                    return@setOnClickListener
                }
                if (runCatching { LocalDate.parse(date) }.isFailure) {
                    toast("Use a data no formato AAAA-MM-DD.")
                    return@setOnClickListener
                }

                val recurrence = if (d.spinnerRecurrence.selectedItemPosition == 1) "monthly" else "none"
                val oldRecurrenceId = item?.recurrenceId
                val recurrenceId = if (recurrence == "monthly") {
                    oldRecurrenceId ?: UUID.randomUUID().toString()
                } else null

                val saved = TransactionEntity(
                    id = item?.id ?: 0,
                    type = if (d.spinnerType.selectedItemPosition == 0) "expense" else "income",
                    description = description,
                    category = category,
                    amountCents = (amount * 100).toLong(),
                    dueDate = date,
                    status = if (d.spinnerStatus.selectedItemPosition == 0) "pending" else "paid",
                    recurrence = recurrence,
                    recurrenceId = recurrenceId,
                    notes = d.edNotes.text.toString().trim(),
                    createdAt = item?.createdAt ?: System.currentTimeMillis()
                )

                lifecycleScope.launch {
                    if (item == null) dao.insert(saved) else dao.update(saved)
                    dialog.dismiss()
                }
            }

            if (item != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Excluir lançamento?")
                        .setMessage(item.description)
                        .setPositiveButton("Excluir") { _, _ ->
                            lifecycleScope.launch {
                                dao.delete(item)
                                dialog.dismiss()
                            }
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        }

        dialog.show()
    }

    private fun showSettings() {
        val box = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(40, 10, 40, 10)
        }
        val reserve = EditText(this).apply {
            hint = "Reserva mínima (R$)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }
        val goal = EditText(this).apply {
            hint = "Meta da obra (R$)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val prefs = getSharedPreferences("finance_settings", MODE_PRIVATE)
        reserve.setText(String.format(Locale.US, "%.2f", prefs.getLong("reserve_cents", 150000L) / 100.0))
        goal.setText(String.format(Locale.US, "%.2f", prefs.getLong("goal_cents", 661600L) / 100.0))
        box.addView(reserve)
        box.addView(goal)

        AlertDialog.Builder(this)
            .setTitle("Reserva e meta")
            .setView(box)
            .setPositiveButton("Salvar") { _, _ ->
                val r = reserve.text.toString().replace(",", ".").toDoubleOrNull() ?: 1500.0
                val g = goal.text.toString().replace(",", ".").toDoubleOrNull() ?: 6616.0
                prefs.edit()
                    .putLong("reserve_cents", (r * 100).toLong())
                    .putLong("goal_cents", (g * 100).toLong())
                    .apply()
                watchMonth()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun doBackup() {
        if (BackupManager.configuredFolder(this) == null) {
            folderPicker.launch(null)
            return
        }
        lifecycleScope.launch {
            binding.btnSync.isEnabled = false
            val result = BackupManager.backup(this@MainActivity)
            binding.btnSync.isEnabled = true
            toast(result.getOrElse { it.message ?: "Falha ao sincronizar." })
            updateSyncLabel()
        }
    }

    private fun confirmRestore() {
        AlertDialog.Builder(this)
            .setTitle("Restaurar backup?")
            .setMessage("O banco SQLite atual será substituído pelo backup salvo na pasta escolhida.")
            .setPositiveButton("Restaurar") { _, _ ->
                lifecycleScope.launch {
                    val result = BackupManager.restore(this@MainActivity)
                    toast(result.getOrElse { it.message ?: "Falha na restauração." })
                    if (result.isSuccess) {
                        val intent = packageManager.getLaunchIntentForPackage(packageName)
                        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finishAffinity()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun updateSyncLabel() {
        binding.txtSync.text = if (BackupManager.configuredFolder(this) == null) {
            "Google Drive: escolha uma pasta para o backup SQLite."
        } else {
            "Google Drive configurado • backup automático a cada 12 horas."
        }
    }

    private fun brl(cents: Long): String =
        NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(cents / 100.0)

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}
