package com.maceilto.minhasfinancas.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE substr(dueDate, 1, 7) = :month ORDER BY dueDate ASC, id ASC")
    fun observeMonth(month: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY dueDate ASC, id ASC")
    suspend fun getAll(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE recurrence = 'monthly' AND recurrenceId IS NOT NULL ORDER BY dueDate ASC")
    suspend fun getRecurring(): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE recurrenceId = :recurrenceId
          AND substr(dueDate, 1, 7) = :month
    """)
    suspend fun countRecurrenceInMonth(recurrenceId: String, month: String): Int

    @Insert
    suspend fun insert(item: TransactionEntity): Long

    @Update
    suspend fun update(item: TransactionEntity)

    @Delete
    suspend fun delete(item: TransactionEntity)
}
