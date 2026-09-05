package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import kotlinx.coroutines.flow.Flow

@Dao
interface WashDao {
    @Query("SELECT * FROM wash_records ORDER BY timestamp DESC")
    fun getAllRecordsFlow(): Flow<List<WashRecord>>

    @Query("SELECT * FROM wash_records WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp ORDER BY timestamp DESC")
    fun getRecordsBetweenFlow(startTimestamp: Long, endTimestamp: Long): Flow<List<WashRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: WashRecord): Long

    @Update
    suspend fun updateRecord(record: WashRecord)

    @Delete
    suspend fun deleteRecord(record: WashRecord)

    @Query("DELETE FROM wash_records")
    suspend fun deleteAllRecords()

    @Query("DELETE FROM wash_records WHERE timestamp >= :startTimestamp AND timestamp <= :endTimestamp")
    suspend fun deleteRecordsBetween(startTimestamp: Long, endTimestamp: Long)

    @Query("SELECT * FROM workers WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveWorkersFlow(): Flow<List<Worker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorker(worker: Worker): Long

    @Delete
    suspend fun deleteWorker(worker: Worker)
}
