package com.example.data.repository

import com.example.data.local.ExpenseDao
import com.example.data.local.WashDao
import com.example.data.model.StoreExpense
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class WashRepository(
    private val dao: WashDao,
    private val expenseDao: ExpenseDao? = null
) {
    fun getAllRecords(): Flow<List<WashRecord>> = dao.getAllRecordsFlow()

    fun getRecordsBetween(start: Long, end: Long): Flow<List<WashRecord>> =
        dao.getRecordsBetweenFlow(start, end)

    suspend fun insertRecord(record: WashRecord): Long = dao.insertRecord(record)

    suspend fun updateRecord(record: WashRecord) = dao.updateRecord(record)

    suspend fun deleteRecord(record: WashRecord) = dao.deleteRecord(record)

    suspend fun deleteAllRecords() = dao.deleteAllRecords()

    suspend fun deleteRecordsBetween(start: Long, end: Long) = dao.deleteRecordsBetween(start, end)

    fun getActiveWorkers(): Flow<List<Worker>> = dao.getActiveWorkersFlow()

    suspend fun insertWorker(worker: Worker): Long = dao.insertWorker(worker)

    suspend fun deleteWorker(worker: Worker) = dao.deleteWorker(worker)

    // Store Expenses (Pengeluaran Toko)
    fun getAllExpenses(): Flow<List<StoreExpense>> =
        expenseDao?.getAllExpensesFlow() ?: flowOf(emptyList())

    fun getExpensesBetween(start: Long, end: Long): Flow<List<StoreExpense>> =
        expenseDao?.getExpensesBetweenFlow(start, end) ?: flowOf(emptyList())

    suspend fun insertExpense(expense: StoreExpense): Long =
        expenseDao?.insertExpense(expense) ?: 0L

    suspend fun updateExpense(expense: StoreExpense) =
        expenseDao?.updateExpense(expense) ?: Unit

    suspend fun deleteExpense(expense: StoreExpense) =
        expenseDao?.deleteExpense(expense) ?: Unit

    suspend fun deleteAllExpenses() =
        expenseDao?.deleteAllExpenses() ?: Unit

    suspend fun deleteExpensesBetween(start: Long, end: Long) =
        expenseDao?.deleteExpensesBetween(start, end) ?: Unit
}

