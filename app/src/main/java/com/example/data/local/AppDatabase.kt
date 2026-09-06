package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.WashRecord
import com.example.data.model.Worker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [WashRecord::class, Worker::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun washDao(): WashDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `workers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isActive` INTEGER NOT NULL DEFAULT 1)"
                )
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `workers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isActive` INTEGER NOT NULL DEFAULT 1)"
                )
            }
        }

        private val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `workers` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `isActive` INTEGER NOT NULL DEFAULT 1)"
                )
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `wash_records` ADD COLUMN `createdBy` TEXT NOT NULL DEFAULT 'Kasir'")
                db.execSQL("ALTER TABLE `wash_records` ADD COLUMN `validationStatus` TEXT NOT NULL DEFAULT 'PENDING'")
                db.execSQL("ALTER TABLE `wash_records` ADD COLUMN `disputeReason` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `wash_records` ADD COLUMN `disputedBy` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `wash_records` ADD COLUMN `disputedAt` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "steam_motor_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Seed default workers
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).washDao()
                                dao.insertWorker(Worker(name = "Budi"))
                                dao.insertWorker(Worker(name = "Asep"))
                                dao.insertWorker(Worker(name = "Joko"))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
