package com.example.data.repository

import com.example.data.local.WashDao
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import kotlinx.coroutines.flow.Flow

class WashRepository(private val dao: WashDao) {
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
}
