package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StoreExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM store_expenses ORDER BY timestamp DESC")
    fun getAllExpensesFlow(): Flow<List<StoreExpense>>

    @Query("SELECT * FROM store_expenses WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    fun getExpensesBetweenFlow(startTimestamp: Long, endTimestamp: Long): Flow<List<StoreExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: StoreExpense): Long

    @Update
    suspend fun updateExpense(expense: StoreExpense)

    @Delete
    suspend fun deleteExpense(expense: StoreExpense)

    @Query("DELETE FROM store_expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM store_expenses WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    suspend fun deleteExpensesBetween(startTimestamp: Long, endTimestamp: Long)
}
